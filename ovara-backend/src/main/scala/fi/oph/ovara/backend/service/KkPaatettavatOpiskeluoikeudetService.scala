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

import java.time.{LocalDate, LocalDateTime}
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
    Try {
      val data = getPaattyvatOpiskeluOikeudet(orgOidsForQuery)
      val raporttiParamNames = db
        .run(
          kkPaatettavatOpiskeluoikeudetRepository.organisaatioNameQuery(oppilaitos),
          "hakuParamNamesQuery"
        )
        .map(param => param.parametri -> param.nimi)
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
        data,
        CommonExcelParams(asiointikieli, translations, raporttiParams, LocalDateTime.now())
      )
    } match {
      case Success(excelFile) => Right(excelFile)
      case Failure(exception) =>
        LOG.error("Error generating Excel report", exception)
        Left("virhe.tietokanta")
    }
  }

  private def getPaattyvatOpiskeluOikeudet(orgOids: List[String]): List[KkPaatettavaOpiskeluoikeus] = {
    val opiskeluoikeudet = db
      .run(
        kkPaatettavatOpiskeluoikeudetRepository.opiskeluoikeudetQuery(orgOids),
        "opiskeluoikeudetQuery"
      ).filter(o => o.koulutusaste.isDefined)
      .toList
    val henkiloOids = opiskeluoikeudet.map(o => o.opiskelijaAvain).distinct
    val sitovastiVastaanottaneet = db
      .run(
        kkPaatettavatOpiskeluoikeudetRepository.vastaanottaneetQuery(henkiloOids),
        "sitovastiVastaanottaneetQuery"
      ).toList
    opiskeluoikeudet.map(o => {
      sitovastiVastaanottaneet.find(v => v.oppijanumero.equals(o.opiskelijaAvain)).map(v =>
        KkPaatettavaOpiskeluoikeus(
          oppijanumero = v.oppijanumero,
          hetu = Some("010101-1234"),
          syntymaAika = LocalDate.of(2001, 1, 1),
          sukunimi = "Testinen",
          etunimet = "Testi",
          kutsumanimi = "Testi",
          opiskelijaAvain = o.opiskelijaAvain,
          opiskeluoikeusAvain = o.opiskeluoikeusAvain,
          opiskeluoikeudenNimi = o.opiskeluoikeudenNimi,
          opiskeluoikeudenPaattymispvm = Some(LocalDate.of(2026, 12, 31)),
          opiskeluoikeudenViimeisinTila = o.opiskeluoikeudenViimeisinTila,
          hakemusOid = v.hakemusOid,
          hakuOid = v.hakuOid,
          hakuNimi = Map(Fi -> "Hurrikaaniopiston erillishaku 2026"),
          hakukohdeOid = "1.2.246.562.20.00000000000000000002",
          hakukohdeNimi = v.hakukohdeNimi,
          oppilaitosOid = "1.2.246.562.10.00000000000000000001",
          oppilaitosNimi = Map(Fi -> "Hurrikaaniopisto"),
          vastaanottoAjankohta = v.vastaanottoAjankohta.get,
          koulutusluokitusKoodit = "12345",
          uudenOpiskeluoikeudenAlkamispvm = LocalDate.of(2026, 9, 1)
        ))
      }).filter(_.isDefined).map(_.get)
  }
}
