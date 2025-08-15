package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.Kielistetty
import fi.oph.ovara.backend.raportointi.dto.{KoulutuksetToteutuksetHakukohteetReadableParams, KoulutuksetToteutuksetHakukohteetUtils, ValidatedKoulutuksetToteutuksetHakukohteetParams}
import fi.oph.ovara.backend.repository.{KoulutuksetToteutuksetHakukohteetRepository, ReadOnlyDatabase}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter, OrganisaatioUtils}
import org.slf4j.{Logger, LoggerFactory}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

import scala.util.{Failure, Success, Try}

@Component
@Service
class KoulutuksetToteutuksetHakukohteetService(
    koulutuksetToteutuksetHakukohteetRepository: KoulutuksetToteutuksetHakukohteetRepository,
    userService: UserService,
    commonService: CommonService,
    lokalisointiService: LokalisointiService
) {

  @Autowired
  val db: ReadOnlyDatabase = null

  val LOG: Logger = LoggerFactory.getLogger(classOf[KoulutuksetToteutuksetHakukohteetService])

  def get(
           haut: List[String],
           koulutustoimija: Option[String],
           oppilaitokset: List[String],
           toimipisteet: List[String],
           koulutuksenTila: Option[String],
           toteutuksenTila: Option[String],
           hakukohteenTila: Option[String],
           valintakoe: Option[Boolean]
         ): Either[String, XSSFWorkbook] = {
    val user = userService.getEnrichedUserDetails
    val asiointikieli = user.asiointikieli.getOrElse("fi")
    val authorities = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)
    val translations = lokalisointiService.getOvaraTranslations(asiointikieli)

    val (orgOidsForQuery, hierarkiat, raporttityyppi) = commonService.getAllowedOrgsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      koulutustoimijaOid = koulutustoimija,
      toimipisteOids = toimipisteet,
      oppilaitosOids = oppilaitokset
    )

    Try {
      val queryResult = db.run(
        koulutuksetToteutuksetHakukohteetRepository.selectWithParams(
          orgOidsForQuery,
          haut,
          koulutuksenTila,
          toteutuksenTila,
          hakukohteenTila,
          valintakoe
        ),
        "selectWithParams"
      )

      val groupedQueryResult = queryResult.groupBy(_.organisaatio_oid)

      val organisaationKoulutuksetHakukohteetToteutukset =
        OrganisaatioUtils.mapOrganisaationHakukohteetToParents(
          hierarkiat,
          groupedQueryResult
        )

      val raporttiParamNames = db.run(
        koulutuksetToteutuksetHakukohteetRepository.hakuParamNamesQuery(
          haut,
          koulutustoimija,
          oppilaitokset,
          toimipisteet
        ),
        "hakuParamNamesQuery"
      ).map(param => param.parametri -> param.nimet).toMap
      val raporttiParams = KoulutuksetToteutuksetHakukohteetUtils.buildParams(
        ValidatedKoulutuksetToteutuksetHakukohteetParams(
          haut,
          koulutustoimija,
          oppilaitokset,
          toimipisteet,
          koulutuksenTila,
          toteutuksenTila,
          hakukohteenTila,
          valintakoe), 
        raporttiParamNames
      )
        
      ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
        organisaationKoulutuksetHakukohteetToteutukset,
        asiointikieli,
        raporttityyppi,
        translations,
        raporttiParams
      )
    } match {
      case Success(excelFile) => Right(excelFile)
      case Failure(exception) =>
        LOG.error("Error generating Excel report", exception)
        Left("virhe.tietokanta")
    }
  }
}
