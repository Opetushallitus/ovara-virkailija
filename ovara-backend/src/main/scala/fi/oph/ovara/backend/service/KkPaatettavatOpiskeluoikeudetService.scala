package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.{Fi, KkPaatettavaOpiskeluoikeus}
import fi.oph.ovara.backend.raportointi.dto.{
  buildKkPaatettavatOpiskeluoikeudetParamsForExcel,
  KkPaatettavatOpiskeluoikeudetParams
}
import fi.oph.ovara.backend.repository.{
  KkPaatettavatOpiskeluoikeudetRepository,
  KorkeakouluKoulutuksetToteutuksetHakukohteetRepository,
  ReadOnlyDatabase
}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, CommonExcelParams, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

import java.time.LocalDateTime
import scala.util.{Failure, Success, Try}

@Component
@Service
class KkPaatettavatOpiskeluoikeudetService(
  kkPaatettavatOpiskeluoikeudetRepository: KkPaatettavatOpiskeluoikeudetRepository,
  userService: UserService,
  commonService: CommonService,
  lokalisointiService: LokalisointiService
) {

  @Autowired
  val db: ReadOnlyDatabase = null

  val LOG: Logger = LoggerFactory.getLogger(classOf[KorkeakouluKoulutuksetToteutuksetHakukohteetService])

  def get(
    oppilaitos: String,
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
      oppilaitosOids = List(oppilaitos),
      List.empty
    )
    // placeholder havainnollistamaan sisältöä
    val mockData = List(
      KkPaatettavaOpiskeluoikeus(
        oppijanumero = "1.2.246.562.24.10002324020",
        hetu = Some("010101-1234"),
        syntymaaika = "1901-01-01",
        sukunimi = "Testinen",
        etunimet = "Testi",
        kutsumanimi = "Testi",
        opiskelijaAvain = "opiskelija-avain-1",
        opiskeluoikeusAvain = "opiskeluoikeus-avain-1",
        opiskeluoikeudenNimi = Map(Fi -> "Meteorologi, Hurrikaanien tutkimislinja"),
        opiskeluoikeudenPaattymispvm = Some("2026-12-31"),
        opiskeluoikeudenViimeisinTila = "Loma",
        hakemusOid = "1.2.246.562.11.00000000000000000001",
        hakuOid = "1.2.246.562.20.00000000000000000001",
        hakuNimi = Map(Fi -> "Hurrikaaniopiston erillishaku 2026"),
        hakukohdeOid = "1.2.246.562.20.00000000000000000002",
        hakukohdeNimi = Map(Fi -> "Meteorologi, Hurrikaanien tutkimislinja"),
        oppilaitosOid = "1.2.246.562.10.00000000000000000001",
        oppilaitosNimi = Map(Fi -> "Hurrikaaniopisto"),
        vastaanottoAjankohta = "2026-01-15T12:00:00",
        koulutusluokitusKoodi = "12345",
        uudenOpiskeluoikeudenAlkamispvm = "2026-01-15"
      )
    )
    Try {
      // TODO varsinainen data raportille
      val raporttiParamNames = db
        .run(
          kkPaatettavatOpiskeluoikeudetRepository.hakuParamNamesQuery(oppilaitos),
          "hakuParamNamesQuery"
        )
        .map(param => param.parametri -> param.nimet.head)
        .toMap

      val raporttiParams = buildKkPaatettavatOpiskeluoikeudetParamsForExcel(
        KkPaatettavatOpiskeluoikeudetParams(
          oppilaitos,
          sukunimi,
          etunimet,
          hetu,
          oppijanumero,
          opiskeluoikeudenTila
        ),
        raporttiParamNames
      )
      ExcelWriter.writeKorkeakouluPaatettavatOpiskeluoikeudetRaportti(
        mockData,
        CommonExcelParams(asiointikieli, translations, raporttiParams, LocalDateTime.now())
      )
    } match {
      case Success(excelFile) => Right(excelFile)
      case Failure(exception) =>
        LOG.error("Error generating Excel report", exception)
        Left("virhe.tietokanta")
    }
  }
}
