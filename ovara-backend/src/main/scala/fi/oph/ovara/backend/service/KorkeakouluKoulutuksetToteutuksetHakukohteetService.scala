package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.raportointi.dto.{ValidatedKkKoulutuksetToteutuksetHakukohteetParams, buildHakijatParamsForExcel, buildKkKoulutuksetToteutuksetHakukohteetParamsForExcel}
import fi.oph.ovara.backend.repository.{KorkeakouluKoulutuksetToteutuksetHakukohteetRepository, ReadOnlyDatabase}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}

@Component
@Service
class KorkeakouluKoulutuksetToteutuksetHakukohteetService(
    korkeakouluKoulutuksetToteutuksetHakukohteetRepository: KorkeakouluKoulutuksetToteutuksetHakukohteetRepository,
    userService: UserService,
    commonService: CommonService,
    lokalisointiService: LokalisointiService
) {

  @Autowired
  val db: ReadOnlyDatabase = null

  val LOG: Logger = LoggerFactory.getLogger(classOf[KorkeakouluKoulutuksetToteutuksetHakukohteetService])

  def get(
           haut: List[String],
           oppilaitokset: List[String],
           toimipisteet: List[String],
           hakukohderyhmat: List[String],
           koulutuksenTila: Option[String],
           toteutuksenTila: Option[String],
           hakukohteenTila: Option[String],
           tutkinnonTasot: List[String],
           tulostustapa: String
         ): Either[String, XSSFWorkbook] = {
    val user = userService.getEnrichedUserDetails
    val asiointikieli = user.asiointikieli.getOrElse("fi")
    val authorities = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)
    val kayttooikeusHakukohderyhmat = AuthoritiesUtil.filterHakukohderyhmaOids(kayttooikeusOrganisaatiot)
    LOG.info(s"kayttooikeusOrganisaatiot: $kayttooikeusOrganisaatiot")
    val translations = lokalisointiService.getOvaraTranslations(asiointikieli)

    val orgOidsForQuery = commonService.getAllowedOrgOidsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      toimipisteOids = toimipisteet,
      oppilaitosOids = oppilaitokset
    )
    LOG.info(s"organization OIDs for query: $orgOidsForQuery")
    val isOrganisaatioRajain = (oppilaitokset.nonEmpty || toimipisteet.nonEmpty) && orgOidsForQuery.nonEmpty

    Try {
      val queryResult = db.run(
        korkeakouluKoulutuksetToteutuksetHakukohteetRepository.selectWithParams(
          orgOidsForQuery,
          isOrganisaatioRajain,
          kayttooikeusHakukohderyhmat,
          hakukohderyhmat,
          haut,
          koulutuksenTila,
          toteutuksenTila,
          hakukohteenTila,
          tutkinnonTasot
        ),
        "selectWithParams"
      )
      val raporttiParamNames = db.run(
        korkeakouluKoulutuksetToteutuksetHakukohteetRepository.hakuParamNamesQuery(
          haut,
          oppilaitokset,
          toimipisteet,
          hakukohderyhmat
        ),
        "hakuParamNamesQuery"
      ).map(param => param.parametri -> param.nimet).toMap
      val raporttiParams = buildKkKoulutuksetToteutuksetHakukohteetParamsForExcel(
        ValidatedKkKoulutuksetToteutuksetHakukohteetParams(
          haut,
          tulostustapa,
          oppilaitokset,
          toimipisteet,
          hakukohderyhmat,
          koulutuksenTila,
          toteutuksenTila,
          hakukohteenTila,
          tutkinnonTasot),
        raporttiParamNames
      )
      ExcelWriter.writeKorkeakouluKoulutuksetToteutuksetHakukohteetRaportti(
        queryResult,
        asiointikieli,
        translations,
        tulostustapa,
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
