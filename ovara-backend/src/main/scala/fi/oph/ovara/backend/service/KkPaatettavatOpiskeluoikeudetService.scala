package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.raportointi.dto.{
  ValidatedKkKoulutuksetToteutuksetHakukohteetParams,
  ValidatedKkPaatettavatOpiskeluoikeudetParams,
  buildKkPaatettavatOpiskeluoikeudetParamsForExcel
}
import fi.oph.ovara.backend.repository.{KorkeakouluKoulutuksetToteutuksetHakukohteetRepository, ReadOnlyDatabase}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

import scala.util.{Failure, Success, Try}

@Component
@Service
class KkPaatettavatOpiskeluoikeudetService(
    userService: UserService,
    commonService: CommonService,
    lokalisointiService: LokalisointiService
) {

  @Autowired
  val db: ReadOnlyDatabase = null

  val LOG: Logger = LoggerFactory.getLogger(classOf[KorkeakouluKoulutuksetToteutuksetHakukohteetService])

  def get(
      oppilaitokset: List[String],
      sukunimi: Option[String],
      etunimet: Option[String],
      hetu: Option[String],
      oppijanumero: Option[String],
      opiskeluoikeudenTila: Option[String]
  ): Either[String, XSSFWorkbook] = {
    val user                      = userService.getEnrichedUserDetails
    val asiointikieli             = user.asiointikieli.getOrElse("fi")
    val authorities               = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)
    val translations              = lokalisointiService.getOvaraTranslations(asiointikieli)

    val orgOidsForQuery = commonService.getAllowedOrgOidsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      oppilaitosOids = oppilaitokset,
      List.empty
    )
    LOG.info(s"organization OIDs for query: $orgOidsForQuery")
    val isOrganisaatioRajain = oppilaitokset.nonEmpty && orgOidsForQuery.nonEmpty

    Try {
      // TODO OPHYOS-193
      val raporttiParams = buildKkPaatettavatOpiskeluoikeudetParamsForExcel(
        ValidatedKkPaatettavatOpiskeluoikeudetParams(
          oppilaitokset,
          sukunimi,
          etunimet,
          hetu,
          oppijanumero,
          opiskeluoikeudenTila
        ),
        Map.empty
      )
      ExcelWriter.writeKorkeakouluPaatettavatOpiskeluoikeudetRaportti(
        asiointikieli,
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
