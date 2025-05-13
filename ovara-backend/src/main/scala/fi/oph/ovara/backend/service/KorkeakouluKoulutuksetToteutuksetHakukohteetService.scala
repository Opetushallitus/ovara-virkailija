package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.repository.{KorkeakouluKoulutuksetToteutuksetHakukohteetRepository, ReadOnlyDatabase}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}
import org.slf4j.{Logger, LoggerFactory}
import scala.util.{Try, Failure, Success}

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
    val isOphPaakayttaja          = AuthoritiesUtil.hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot)

    val translations = lokalisointiService.getOvaraTranslations(asiointikieli)

    val orgOidsForQuery = commonService.getAllowedOrgOidsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      toimipisteOids = toimipisteet,
      oppilaitosOids = oppilaitokset
    )

    Try {
      val allowedHakukohderyhmat = if (isOphPaakayttaja) {
        hakukohderyhmat
      } else {
        kayttooikeusOrganisaatiot intersect hakukohderyhmat
      }

      val queryResult = db.run(
        korkeakouluKoulutuksetToteutuksetHakukohteetRepository.selectWithParams(
          orgOidsForQuery,
          allowedHakukohderyhmat,
          haut,
          koulutuksenTila,
          toteutuksenTila,
          hakukohteenTila,
          tutkinnonTasot
        ),
        "selectWithParams"
      )

      ExcelWriter.writeKorkeakouluKoulutuksetToteutuksetHakukohteetRaportti(
        queryResult,
        asiointikieli,
        translations,
        tulostustapa
      )
    } match {
      case Success(excelFile) => Right(excelFile)
      case Failure(exception) =>
        LOG.error("Error generating Excel report", exception)
        Left("virhe.tietokanta")
    }
  }
}
