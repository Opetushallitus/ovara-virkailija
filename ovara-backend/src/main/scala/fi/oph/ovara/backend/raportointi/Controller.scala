package fi.oph.ovara.backend.raportointi

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.oph.ovara.backend.domain.UserResponse
import fi.oph.ovara.backend.service.*
import fi.oph.ovara.backend.utils.AuditLog
import fi.oph.ovara.backend.utils.AuditOperation.KoulutuksetToteutuksetHakukohteet
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.apache.poi.ss.usermodel.Workbook
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}
import org.springframework.web.servlet.view.RedirectView

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters.*

@RestController
@RequestMapping(path = Array("api"))
class Controller(
    commonService: CommonService,
    koulutuksetToteutuksetHakukohteetService: KoulutuksetToteutuksetHakukohteetService,
    hakijatService: HakijatService,
    kkHakijatService: KkHakijatService,
    hakeneetHyvaksytytVastaanottaneetService: HakeneetHyvaksytytVastaanottaneetService,
    userService: UserService,
    val auditLog: AuditLog = AuditLog
) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[Controller]);

  @Value("${ovara.ui.url}")
  val ovaraUiUrl: String = null

  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(SerializationFeature.INDENT_OUTPUT, true)

  private def strToOptionBoolean(str: String) = {
    if (str == null) {
      None
    } else {
      Option(str.toBoolean)
    }
  }

  @GetMapping(path = Array("healthcheck"))
  def healthcheck = "Ovara application is running!"

  @GetMapping(path = Array("user"))
  def user(): String = {
    val enrichedUserDetails = userService.getEnrichedUserDetails
    mapper.writeValueAsString(
      UserResponse(
        user =
          if (enrichedUserDetails == null)
            null
          else
            enrichedUserDetails
      )
    )
  }

  @GetMapping(path = Array("login"))
  def login = RedirectView(ovaraUiUrl)

  @GetMapping(path = Array("csrf"))
  def csrf(csrfToken: CsrfToken): String = mapper.writeValueAsString(csrfToken)

  @GetMapping(path = Array("alkamisvuodet"))
  def alkamisvuodet: String = mapper.writeValueAsString(commonService.getAlkamisvuodet)

  @GetMapping(path = Array("haut"))
  def haut(
      @RequestParam("alkamiskausi", required = false) alkamiskaudet: java.util.Collection[String],
      @RequestParam("haun_tyyppi", required = false) haun_tyyppi: String
  ): String = {
    val alkamiskaudetList =
      if (alkamiskaudet == null) List() else alkamiskaudet.asScala.toList

    val haunTyyppi = if (haun_tyyppi == null) "" else haun_tyyppi

    mapper.writeValueAsString(commonService.getHaut(alkamiskaudetList, haunTyyppi))
  }

  @GetMapping(path = Array("hakukohteet"))
  def hakukohteet(
      @RequestParam("oppilaitos", required = false) oppilaitos: java.util.Collection[String],
      @RequestParam("toimipiste", required = false) toimipiste: java.util.Collection[String],
      @RequestParam("haku", required = false) haku: java.util.Collection[String]
  ): String = mapper.writeValueAsString(
    commonService.getHakukohteet(
      if (oppilaitos == null) List() else oppilaitos.asScala.toList,
      if (toimipiste == null) List() else toimipiste.asScala.toList,
      if (haku == null) List() else haku.asScala.toList
    )
  )

  @GetMapping(path = Array("pohjakoulutukset-toinen-aste"))
  def pohjakoulutuksetToinenAste(): String = mapper.writeValueAsString(commonService.getPohjakoulutukset)

  @GetMapping(path = Array("organisaatiot"))
  def organisaatiot: String = mapper.writeValueAsString(commonService.getOrganisaatioHierarkiatWithUserRights)

  @GetMapping(path = Array("harkinnanvaraisuudet"))
  def harkinnanvaraisuudet: String = mapper.writeValueAsString(commonService.getHarkinnanvaraisuudet)

  @GetMapping(path = Array("valintatiedot"))
  def valintatiedot: String = mapper.writeValueAsString(commonService.getValintatiedot)

  @GetMapping(path = Array("vastaanottotiedot"))
  def vastaanottotiedot: String = mapper.writeValueAsString(commonService.getVastaanottotiedot)

  @GetMapping(path = Array("opetuskielet"))
  def opetuskielet: String = mapper.writeValueAsString(commonService.getOpetuskielet)

  @GetMapping(path = Array("maakunnat"))
  def maakunnat: String = mapper.writeValueAsString(commonService.getMaakunnat)

  @GetMapping(path = Array("kunnat"))
  def kunnat(@RequestParam("maakunnat", required = false) maakunnat: java.util.Collection[String]): String =
    mapper.writeValueAsString(commonService.getKunnat(if (maakunnat == null) List() else maakunnat.asScala.toList))

  @GetMapping(path = Array("koulutusalat1"))
  def koulutusalat1: String = mapper.writeValueAsString(commonService.getKoulutusalat1)

  @GetMapping(path = Array("koulutusalat2"))
  def koulutusalat2(
      @RequestParam("koulutusalat1", required = false) koulutusalat1: java.util.Collection[String]
  ): String =
    mapper.writeValueAsString(
      commonService.getKoulutusalat2(if (koulutusalat1 == null) List() else koulutusalat1.asScala.toList)
    )

  @GetMapping(path = Array("koulutusalat3"))
  def koulutusalat3(
      @RequestParam("koulutusalat2", required = false) koulutusalat2: java.util.Collection[String]
  ): String =
    mapper.writeValueAsString(
      commonService.getKoulutusalat3(if (koulutusalat2 == null) List() else koulutusalat2.asScala.toList)
    )

  @GetMapping(path = Array("hakukohderyhmat"))
  def hakukohderyhmat(
      @RequestParam("haku", required = true) haut: java.util.Collection[String]
  ): String =
    mapper.writeValueAsString(
      commonService.getHakukohderyhmat(if (haut == null) List() else haut.asScala.toList)
    )

  // RAPORTIT

  private def sendExcel(
      wb: Workbook,
      response: HttpServletResponse,
      request: HttpServletRequest,
      id: String,
      raporttiParamsForLogs: Map[String, Any]
  ): Unit = {
    try {
      auditLog.logWithParams(request, KoulutuksetToteutuksetHakukohteet, raporttiParamsForLogs)
      LOG.info(s"Sending excel in the response")
      val date: LocalDateTime = LocalDateTime.now().withNano(0)
      val dateTimeStr         = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
      val out                 = response.getOutputStream
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      response.setHeader(
        HttpHeaders.CONTENT_DISPOSITION,
        s"attachment; filename=$id-$dateTimeStr.xlsx"
      )
      response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
      wb.write(out)
      out.close()
      wb.close()
    } catch {
      case e: Exception =>
        LOG.error(s"Error sending excel: ${e.getMessage}")
        response.sendError(500)
    }
  }

  @GetMapping(path = Array("koulutukset-toteutukset-hakukohteet"))
  def koulutukset_toteutukset_hakukohteet(
      @RequestParam("haku") haku: java.util.Collection[String],
      @RequestParam("koulutustoimija", required = false) koulutustoimija: String,
      @RequestParam("oppilaitos", required = false) oppilaitos: java.util.Collection[String],
      @RequestParam("toimipiste", required = false) toimipiste: java.util.Collection[String],
      @RequestParam("koulutuksen-tila", required = false) koulutuksenTila: String,
      @RequestParam("toteutuksen-tila", required = false) toteutuksenTila: String,
      @RequestParam("hakukohteen-tila", required = false) hakukohteenTila: String,
      @RequestParam("valintakoe", required = false) valintakoe: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val maybeKoulutustoimija = Option(koulutustoimija)
    val maybeKoulutuksenTila = Option(koulutuksenTila)
    val maybeToteutuksenTila = Option(toteutuksenTila)
    val maybeHakukohteenTila = Option(hakukohteenTila)
    val maybeValintakoe = if (valintakoe == null) {
      None
    } else {
      Option(valintakoe.toBoolean)
    }
    val oppilaitosList = if (oppilaitos == null) List() else oppilaitos.asScala.toList
    val toimipisteList = if (toimipiste == null) List() else toimipiste.asScala.toList
    val hakuList       = if (haku == null) List() else haku.asScala.toList

    val wb = koulutuksetToteutuksetHakukohteetService.get(
      hakuList,
      maybeKoulutustoimija,
      oppilaitosList,
      toimipisteList,
      maybeKoulutuksenTila,
      maybeToteutuksenTila,
      maybeHakukohteenTila,
      maybeValintakoe
    )

    val raporttiParams = Map(
      "haku"            -> Option(hakuList).filterNot(_.isEmpty),
      "koulutustoimija" -> maybeKoulutustoimija,
      "oppilaitos"      -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipiste"      -> Option(toimipisteList).filterNot(_.isEmpty),
      "koulutuksenTila" -> maybeKoulutuksenTila,
      "toteutuksenTila" -> maybeToteutuksenTila,
      "hakukohteenTila" -> maybeHakukohteenTila,
      "valintakoe"      -> maybeValintakoe
    ).collect { case (key, Some(value)) => key -> value } // jätetään pois tyhjät parametrit

    sendExcel(wb, response, request, "koulutukset-toteutukset-hakukohteet", raporttiParams)
  }

  @GetMapping(path = Array("hakijat"))
  def hakijat(
      @RequestParam("haku") haku: java.util.Collection[String],
      @RequestParam("oppilaitos", required = false) oppilaitos: java.util.Collection[String],
      @RequestParam("toimipiste", required = false) toimipiste: java.util.Collection[String],
      @RequestParam("hakukohde", required = false) hakukohde: java.util.Collection[String],
      @RequestParam("pohjakoulutus", required = false) pohjakoulutus: java.util.Collection[String],
      @RequestParam("valintatieto", required = false) valintatieto: java.util.Collection[String],
      @RequestParam("vastaanottotieto", required = false) vastaanottotieto: java.util.Collection[String],
      @RequestParam("harkinnanvaraisuus", required = false) harkinnanvaraisuus: java.util.Collection[String],
      @RequestParam("kaksoistutkinto", required = false) kaksoistutkinto: String,
      @RequestParam("urheilijatutkinto", required = false) urheilijatutkinto: String,
      @RequestParam("sora_terveys", required = false) soraterveys: String,
      @RequestParam("sora_aiempi", required = false) soraAiempi: String,
      @RequestParam("markkinointilupa", required = false) markkinointilupa: String,
      @RequestParam("julkaisulupa", required = false) julkaisulupa: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val hakuList               = if (haku == null) List() else haku.asScala.toList
    val oppilaitosList         = if (oppilaitos == null) List() else oppilaitos.asScala.toList
    val toimipisteList         = if (toimipiste == null) List() else toimipiste.asScala.toList
    val hakukohdeList          = if (hakukohde == null) List() else hakukohde.asScala.toList
    val pohjakoulutusList      = if (pohjakoulutus == null) List() else pohjakoulutus.asScala.toList
    val valintatietoList       = if (valintatieto == null) List() else valintatieto.asScala.toList
    val vastaanottotietoList   = if (vastaanottotieto == null) List() else vastaanottotieto.asScala.toList
    val harkinnanvaraisuusList = if (harkinnanvaraisuus == null) List() else harkinnanvaraisuus.asScala.toList

    val maybeKaksoistutkintoKiinnostaa   = strToOptionBoolean(kaksoistutkinto)
    val maybeUrheilijatutkintoKiinnostaa = strToOptionBoolean(urheilijatutkinto)
    val maybeSoraTerveys                 = strToOptionBoolean(soraterveys)
    val maybeSoraAiempi                  = strToOptionBoolean(soraAiempi)
    val maybeMarkkinointilupa            = strToOptionBoolean(markkinointilupa)
    val maybeJulkaisulupa                = strToOptionBoolean(julkaisulupa)

    val wb = hakijatService.get(
      hakuList,
      oppilaitosList,
      toimipisteList,
      hakukohdeList,
      pohjakoulutusList,
      valintatietoList,
      vastaanottotietoList,
      harkinnanvaraisuusList,
      maybeKaksoistutkintoKiinnostaa,
      maybeUrheilijatutkintoKiinnostaa,
      maybeSoraTerveys,
      maybeSoraAiempi,
      maybeMarkkinointilupa,
      maybeJulkaisulupa
    )

    val raporttiParams = Map(
      "haku"               -> Option(hakuList).filterNot(_.isEmpty),
      "oppilaitos"         -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipiste"         -> Option(toimipisteList).filterNot(_.isEmpty),
      "hakukohde"          -> Option(hakukohdeList).filterNot(_.isEmpty),
      "pohjakoulutus"      -> Option(pohjakoulutusList).filterNot(_.isEmpty),
      "valintatieto"       -> Option(vastaanottotietoList).filterNot(_.isEmpty),
      "vastaanottotieto"   -> Option(vastaanottotietoList).filterNot(_.isEmpty),
      "harkinnanvaraisuus" -> Option(harkinnanvaraisuusList).filterNot(_.isEmpty),
      "kaksoistutkinto"    -> maybeKaksoistutkintoKiinnostaa,
      "urheilijatutkinto"  -> maybeUrheilijatutkintoKiinnostaa,
      "soraTerveys"        -> maybeSoraTerveys,
      "soraAiempi"         -> maybeSoraAiempi,
      "markkinointilupa"   -> maybeMarkkinointilupa,
      "julkaisulupa"       -> maybeJulkaisulupa
    ).collect { case (key, Some(value)) => key -> value } // jätetään pois tyhjät parametrit

    sendExcel(wb, response, request, "hakijat", raporttiParams)
  }

  @GetMapping(path = Array("kk-hakijat"))
  def kk_hakijat(
      @RequestParam("haku") haku: java.util.Collection[String],
      @RequestParam("oppilaitos", required = false) oppilaitos: java.util.Collection[String],
      @RequestParam("toimipiste", required = false) toimipiste: java.util.Collection[String],
      @RequestParam("hakukohde", required = false) hakukohde: java.util.Collection[String],
      @RequestParam("valintatieto", required = false) valintatieto: java.util.Collection[String],
      @RequestParam("vastaanottotieto", required = false) vastaanottotieto: java.util.Collection[String],
      @RequestParam("hakukohderyhmat", required = false) hakukohderyhmat: java.util.Collection[String],
      @RequestParam("kansalaisuus", required = false) kansalaisuus: java.util.Collection[String],
      @RequestParam("markkinointilupa", required = false) markkinointilupa: String,
      @RequestParam("nayta-yo-arvosanat", required = true) naytaYoArvosanat: String,
      @RequestParam("nayta-hetu", required = true) naytaHetu: String,
      @RequestParam("nayta-postiosoite", required = true) naytaPostiosoite: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val hakuList             = if (haku == null) List() else haku.asScala.toList
    val oppilaitosList       = if (oppilaitos == null) List() else oppilaitos.asScala.toList
    val toimipisteList       = if (toimipiste == null) List() else toimipiste.asScala.toList
    val hakukohdeList        = if (hakukohde == null) List() else hakukohde.asScala.toList
    val valintatietoList     = if (valintatieto == null) List() else valintatieto.asScala.toList
    val vastaanottotietoList = if (vastaanottotieto == null) List() else vastaanottotieto.asScala.toList
    val hakukohderyhmaList   = if (hakukohderyhmat == null) List() else hakukohderyhmat.asScala.toList
    val kansalaisuusList     = if (kansalaisuus == null) List() else kansalaisuus.asScala.toList

    val maybeMarkkinointilupa = strToOptionBoolean(markkinointilupa)

    val wb = kkHakijatService.get(
      hakuList,
      oppilaitosList,
      toimipisteList,
      hakukohdeList,
      valintatietoList,
      vastaanottotietoList,
      hakukohderyhmaList,
      kansalaisuusList,
      maybeMarkkinointilupa,
      naytaYoArvosanat.toBoolean,
      naytaHetu.toBoolean,
      naytaPostiosoite.toBoolean
    )

    val raporttiParams = Map(
      "haku"             -> Option(hakuList).filterNot(_.isEmpty),
      "oppilaitos"       -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipiste"       -> Option(toimipisteList).filterNot(_.isEmpty),
      "hakukohde"        -> Option(hakukohdeList).filterNot(_.isEmpty),
      "valintatieto"     -> Option(vastaanottotietoList).filterNot(_.isEmpty),
      "vastaanottotieto" -> Option(vastaanottotietoList).filterNot(_.isEmpty),
      "hakukohderyhmat"  -> Option(vastaanottotietoList).filterNot(_.isEmpty),
      "kansalaisuus"     -> Option(kansalaisuusList).filterNot(_.isEmpty),
      "markkinointilupa" -> maybeMarkkinointilupa,
      "naytaYoArvosanat" -> naytaYoArvosanat,
      "naytaHetu"        -> naytaHetu,
      "naytaPostiosoite" -> naytaPostiosoite
    ).collect { case (key, Some(value)) => key -> value } // jätetään pois tyhjät parametrit

    sendExcel(wb, response, request, "kk-hakijat", raporttiParams)
  }

  @GetMapping(path = Array("hakeneet-hyvaksytyt-vastaanottaneet"))
  def hakeneet_hyvaksytyt_vastaanottaneet(
      @RequestParam("alkamiskausi") alkamiskausi: java.util.Collection[String],
      @RequestParam("haku") haku: java.util.Collection[String],
      @RequestParam("tulostustapa") tulostustapa: String,
      @RequestParam("koulutustoimija", required = false) koulutustoimija: String,
      @RequestParam("oppilaitos", required = false) oppilaitos: java.util.Collection[String],
      @RequestParam("toimipiste", required = false) toimipiste: java.util.Collection[String],
      @RequestParam("hakukohde", required = false) hakukohde: java.util.Collection[String],
      @RequestParam("koulutusala1", required = false) koulutusala1: java.util.Collection[String],
      @RequestParam("koulutusala2", required = false) koulutusala2: java.util.Collection[String],
      @RequestParam("koulutusala3", required = false) koulutusala3: java.util.Collection[String],
      @RequestParam("opetuskieli", required = false) opetuskieli: java.util.Collection[String],
      @RequestParam("maakunta", required = false) maakunta: java.util.Collection[String],
      @RequestParam("kunta", required = false) kunta: java.util.Collection[String],
      @RequestParam("harkinnanvaraisuus", required = false) harkinnanvaraisuudet: java.util.Collection[String],
      @RequestParam("nayta-hakutoiveet", required = false) naytaHakutoiveet: String,
      @RequestParam("sukupuoli", required = false) sukupuoli: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val maybeKoulutustoimija           = Option(koulutustoimija)
    val tulostustapaValinta            = Option(tulostustapa).getOrElse("hakukohteittain")
    val naytaHakutoiveetBool           = Option(naytaHakutoiveet).exists(_.toBoolean)
    val maybeSukupuoli: Option[String] = if (sukupuoli == "neutral") None else Option(sukupuoli)
    val alkamiskausiList               = if (alkamiskausi == null) List() else alkamiskausi.asScala.toList
    val hakuList                       = if (haku == null) List() else haku.asScala.toList
    val oppilaitosList                 = if (oppilaitos == null) List() else oppilaitos.asScala.toList
    val toimipisteList                 = if (toimipiste == null) List() else toimipiste.asScala.toList
    val hakukohdeList                  = if (hakukohde == null) List() else hakukohde.asScala.toList
    val koulutusala1List               = if (koulutusala1 == null) List() else koulutusala1.asScala.toList
    val koulutusala2List               = if (koulutusala2 == null) List() else koulutusala2.asScala.toList
    val koulutusala3List               = if (koulutusala3 == null) List() else koulutusala3.asScala.toList
    val maakuntaList                   = if (maakunta == null) List() else maakunta.asScala.toList.map("maakunta_" + _)
    val kuntaList                      = if (kunta == null) List() else kunta.asScala.toList.map("kunta_" + _)
    val opetuskieliList =
      if (opetuskieli == null) List() else opetuskieli.asScala.toList.map("oppilaitoksenopetuskieli_" + _)
    val harkinnanvaraisuusList = if (harkinnanvaraisuudet == null) List() else harkinnanvaraisuudet.asScala.toList

    val wb = hakeneetHyvaksytytVastaanottaneetService.get(
      hakuList,
      tulostustapaValinta,
      maybeKoulutustoimija,
      oppilaitosList,
      toimipisteList,
      hakukohdeList,
      koulutusala1List,
      koulutusala2List,
      koulutusala3List,
      opetuskieliList,
      maakuntaList,
      kuntaList,
      harkinnanvaraisuusList,
      maybeSukupuoli,
      naytaHakutoiveetBool
    )

    val raporttiParams = Map(
      "alkamiskausi"         -> Option(alkamiskausiList).filterNot(_.isEmpty),
      "haku"                 -> Option(hakuList).filterNot(_.isEmpty),
      "koulutustoimija"      -> maybeKoulutustoimija,
      "oppilaitos"           -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipiste"           -> Option(toimipisteList).filterNot(_.isEmpty),
      "hakukohde"            -> Option(hakukohdeList).filterNot(_.isEmpty),
      "koulutusala1"         -> Option(koulutusala1).filterNot(_.isEmpty),
      "koulutusala2"         -> Option(koulutusala2).filterNot(_.isEmpty),
      "koulutusala3"         -> Option(koulutusala3).filterNot(_.isEmpty),
      "opetuskieli"          -> Option(opetuskieliList).filterNot(_.isEmpty),
      "maakunta"             -> Option(maakuntaList).filterNot(_.isEmpty),
      "kunta"                -> Option(kuntaList).filterNot(_.isEmpty),
      "harkinnanvaraisuudet" -> Option(harkinnanvaraisuusList).filterNot(_.isEmpty),
      "naytaHakutoiveet"     -> naytaHakutoiveetBool,
      "sukupuoli"            -> maybeSukupuoli
    ).collect { case (key, Some(value)) => key -> value } // jätetään pois tyhjät parametrit

    sendExcel(wb, response, request, "hakeneet-hyvaksytyt-vastaanottaneet", raporttiParams)
  }

}
