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
import org.springframework.http.{HttpHeaders, ResponseEntity}
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}
import org.springframework.web.servlet.view.RedirectView

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util
import scala.jdk.CollectionConverters.*

@RestController
@RequestMapping(path = Array("api"))
class Controller(
    commonService: CommonService,
    koulutuksetToteutuksetHakukohteetService: KoulutuksetToteutuksetHakukohteetService,
    kkKoulutuksetToteutuksetHakukohteetService: KorkeakouluKoulutuksetToteutuksetHakukohteetService,
    hakijatService: ToisenAsteenHakijatService,
    kkHakijatService: KkHakijatService,
    hakeneetHyvaksytytVastaanottaneetService: HakeneetHyvaksytytVastaanottaneetService,
    kkHakeneetHyvaksytytVastaanottaneetService: KkHakeneetHyvaksytytVastaanottaneetService,
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

  private def getListParamAsScalaList(listParam: util.Collection[String]) = {
    if (listParam == null) List() else listParam.asScala.toList
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

  @GetMapping(path = Array("session"))
  def response: ResponseEntity[Map[String, String]] = {
    // Palautetaan jokin paluuarvo koska client-kirjasto sellaisen haluaa
    ResponseEntity.ok(Map("status" -> "ok"))
  }

  @GetMapping(path = Array("csrf"))
  def csrf(csrfToken: CsrfToken): String = mapper.writeValueAsString(csrfToken)

  @GetMapping(path = Array("alkamisvuodet"))
  def alkamisvuodet: String = mapper.writeValueAsString(commonService.getAlkamisvuodet)

  @GetMapping(path = Array("haut"))
  def haut(
      @RequestParam("alkamiskausi", required = false) alkamiskaudet: java.util.Collection[String],
      @RequestParam("haku", required = false) selectedHaut: java.util.Collection[String],
      @RequestParam("haun_tyyppi", required = false) haun_tyyppi: String
  ): String = {
    val alkamiskaudetList =
      getListParamAsScalaList(alkamiskaudet)
    val selectedHautList = getListParamAsScalaList(selectedHaut)
    val haunTyyppi = if (haun_tyyppi == null) "" else haun_tyyppi

    mapper.writeValueAsString(commonService.getHaut(alkamiskaudetList, selectedHautList, haunTyyppi))
  }

  @GetMapping(path = Array("hakukohteet"))
  def hakukohteet(
      @RequestParam("haku") haku: java.util.Collection[String],
      @RequestParam("oppilaitos", required = false) oppilaitos: java.util.Collection[String],
      @RequestParam("toimipiste", required = false) toimipiste: java.util.Collection[String],
      @RequestParam("hakukohderyhmat", required = false) hakukohderyhmat: java.util.Collection[String]
  ): String = mapper.writeValueAsString(
    commonService.getHakukohteet(
      getListParamAsScalaList(oppilaitos),
      getListParamAsScalaList(toimipiste),
      getListParamAsScalaList(haku),
      getListParamAsScalaList(hakukohderyhmat)
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
  def kunnat(@RequestParam("maakunnat", required = false) maakunnat: java.util.Collection[String],
             @RequestParam("selectedKunnat", required = false) selectedKunnat: java.util.Collection[String]): String =
    mapper.writeValueAsString(commonService.getKunnat(getListParamAsScalaList(maakunnat), getListParamAsScalaList(selectedKunnat)))

  @GetMapping(path = Array("koulutusalat1"))
  def koulutusalat1: String = mapper.writeValueAsString(commonService.getKoulutusalat1)

  @GetMapping(path = Array("koulutusalat2"))
  def koulutusalat2(
      @RequestParam("koulutusalat1", required = false) koulutusalat1: java.util.Collection[String],
      @RequestParam("selectedKoulutusalat2", required = false) selectedKoulutusalat2: java.util.Collection[String]
  ): String =
    mapper.writeValueAsString(
      commonService.getKoulutusalat2(getListParamAsScalaList(koulutusalat1), getListParamAsScalaList(selectedKoulutusalat2))
    )

  @GetMapping(path = Array("koulutusalat3"))
  def koulutusalat3(
      @RequestParam("koulutusalat2", required = false) koulutusalat2: java.util.Collection[String],
      @RequestParam("selectedKoulutusalat3", required = false) selectedKoulutusalat3: java.util.Collection[String]
  ): String =
    mapper.writeValueAsString(
      commonService.getKoulutusalat3(getListParamAsScalaList(koulutusalat2), getListParamAsScalaList(selectedKoulutusalat3))
    )

  @GetMapping(path = Array("hakukohderyhmat"))
  def hakukohderyhmat(
      @RequestParam("haku", required = true) haut: java.util.Collection[String]
  ): String =
    mapper.writeValueAsString(
      commonService.getHakukohderyhmat(getListParamAsScalaList(haut))
    )

  @GetMapping(path = Array("okm-ohjauksen-alat"))
  def okmOhjauksenAlat: String = mapper.writeValueAsString(commonService.getOkmOhjauksenAlat)
  // RAPORTIT

  private def sendExcel(
      wb: Option[Workbook],
      response: HttpServletResponse,
      request: HttpServletRequest,
      id: String,
      raporttiParamsForLogs: Map[String, Any]
  ): Unit = {
    try {
      auditLog.logWithParams(request, KoulutuksetToteutuksetHakukohteet, raporttiParamsForLogs)
      wb match {
        case Some(wb) =>
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
        case None =>
          LOG.error("Could not create excel.")
          response.sendError(400)
      }
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
    val oppilaitosList = getListParamAsScalaList(oppilaitos)
    val toimipisteList = getListParamAsScalaList(toimipiste)
    val hakuList       = getListParamAsScalaList(haku)

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

    sendExcel(Some(wb), response, request, "koulutukset-toteutukset-hakukohteet", raporttiParams)
  }

  @GetMapping(path = Array("kk-koulutukset-toteutukset-hakukohteet"))
  def kk_koulutukset_toteutukset_hakukohteet(
      @RequestParam("haku") haku: java.util.Collection[String],
      @RequestParam("tulostustapa", defaultValue = "koulutuksittain") tulostustapa: String,
      @RequestParam("oppilaitos", required = false) oppilaitos: java.util.Collection[String],
      @RequestParam("toimipiste", required = false) toimipiste: java.util.Collection[String],
      @RequestParam("hakukohderyhmat", required = false) hakukohderyhmat: java.util.Collection[String],
      @RequestParam("koulutuksen-tila", required = false) koulutuksenTila: String,
      @RequestParam("toteutuksen-tila", required = false) toteutuksenTila: String,
      @RequestParam("hakukohteen-tila", required = false) hakukohteenTila: String,
      @RequestParam("tutkinnon-taso", required = false) tutkinnonTaso: java.util.Collection[String],
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val maybeKoulutuksenTila = Option(koulutuksenTila)
    val maybeToteutuksenTila = Option(toteutuksenTila)
    val maybeHakukohteenTila = Option(hakukohteenTila)

    val oppilaitosList     = if (oppilaitos == null) List() else oppilaitos.asScala.toList
    val toimipisteList     = if (toimipiste == null) List() else toimipiste.asScala.toList
    val hakukohderyhmaList = if (hakukohderyhmat == null) List() else hakukohderyhmat.asScala.toList
    val hakuList           = if (haku == null) List() else haku.asScala.toList
    val tutkinnonTasoList = if (tutkinnonTaso == null) List() else tutkinnonTaso.asScala.toList

    val wb = kkKoulutuksetToteutuksetHakukohteetService.get(
      hakuList,
      oppilaitosList,
      toimipisteList,
      hakukohderyhmaList,
      maybeKoulutuksenTila,
      maybeToteutuksenTila,
      maybeHakukohteenTila,
      tutkinnonTasoList,
      tulostustapa
    )

    val raporttiParams = Map(
      "haku"            -> Option(hakuList).filterNot(_.isEmpty),
      "oppilaitos"      -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipiste"      -> Option(toimipisteList).filterNot(_.isEmpty),
      "hakukohderyhmat" -> Option(hakukohderyhmaList).filterNot(_.isEmpty),
      "koulutuksenTila" -> maybeKoulutuksenTila,
      "toteutuksenTila" -> maybeToteutuksenTila,
      "hakukohteenTila" -> maybeHakukohteenTila,
      "tutkinnonTaso"    -> Option(tutkinnonTasoList).filterNot(_.isEmpty),
    ).collect { case (key, Some(value)) => key -> value } // jätetään pois tyhjät parametrit

    sendExcel(Some(wb), response, request, "kk-koulutukset-toteutukset-hakukohteet", raporttiParams)
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
    val hakuList               = getListParamAsScalaList(haku)
    val oppilaitosList         = getListParamAsScalaList(oppilaitos)
    val toimipisteList         = getListParamAsScalaList(toimipiste)
    val hakukohdeList          = getListParamAsScalaList(hakukohde)
    val pohjakoulutusList      = getListParamAsScalaList(pohjakoulutus)
    val valintatietoList       = getListParamAsScalaList(valintatieto)
    val vastaanottotietoList   = getListParamAsScalaList(vastaanottotieto)
    val harkinnanvaraisuusList = getListParamAsScalaList(harkinnanvaraisuus)

    val maybeKaksoistutkintoKiinnostaa   = strToOptionBoolean(kaksoistutkinto)
    val maybeUrheilijatutkintoKiinnostaa = strToOptionBoolean(urheilijatutkinto)
    val maybeSoraTerveys                 = strToOptionBoolean(soraterveys)
    val maybeSoraAiempi                  = strToOptionBoolean(soraAiempi)
    val maybeMarkkinointilupa            = strToOptionBoolean(markkinointilupa)
    val maybeJulkaisulupa                = strToOptionBoolean(julkaisulupa)

    val maybeWb = if (oppilaitosList.nonEmpty || toimipisteList.nonEmpty) {
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
      Some(wb)
    } else {
      None
    }

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

    sendExcel(maybeWb, response, request, "hakijat", raporttiParams)
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
    val hakuList             = getListParamAsScalaList(haku)
    val oppilaitosList       = getListParamAsScalaList(oppilaitos)
    val toimipisteList       = getListParamAsScalaList(toimipiste)
    val hakukohdeList        = getListParamAsScalaList(hakukohde)
    val valintatietoList     = getListParamAsScalaList(valintatieto)
    val vastaanottotietoList = getListParamAsScalaList(vastaanottotieto)
    val hakukohderyhmaList   = getListParamAsScalaList(hakukohderyhmat)
    val kansalaisuusList     = getListParamAsScalaList(kansalaisuus)

    val maybeMarkkinointilupa = strToOptionBoolean(markkinointilupa)

    val maybeWb = if (oppilaitosList.nonEmpty || toimipisteList.nonEmpty || hakukohderyhmaList.nonEmpty) {
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
      Some(wb)
    } else {
      None
    }

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

    sendExcel(maybeWb, response, request, "kk-hakijat", raporttiParams)
  }

  @GetMapping(path = Array("hakeneet-hyvaksytyt-vastaanottaneet"))
  def hakeneet_hyvaksytyt_vastaanottaneet(
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
    val hakuList                       = getListParamAsScalaList(haku)
    val oppilaitosList                 = getListParamAsScalaList(oppilaitos)
    val toimipisteList                 = getListParamAsScalaList(toimipiste)
    val hakukohdeList                  = getListParamAsScalaList(hakukohde)
    val koulutusala1List               = getListParamAsScalaList(koulutusala1)
    val koulutusala2List               = getListParamAsScalaList(koulutusala2)
    val koulutusala3List               = getListParamAsScalaList(koulutusala3)
    val maakuntaList                   = getListParamAsScalaList(maakunta).map("maakunta_" + _)
    val kuntaList                      = getListParamAsScalaList(kunta).map("kunta_" + _)
    val opetuskieliList =
      getListParamAsScalaList(opetuskieli).map("oppilaitoksenopetuskieli_" + _)
    val harkinnanvaraisuusList = getListParamAsScalaList(harkinnanvaraisuudet)

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

    sendExcel(Some(wb), response, request, "hakeneet-hyvaksytyt-vastaanottaneet", raporttiParams)
  }

  @GetMapping(path = Array("kk-hakeneet-hyvaksytyt-vastaanottaneet"))
  def kk_hakeneet_hyvaksytyt_vastaanottaneet(
      @RequestParam("haku") haku: java.util.Collection[String],
      @RequestParam("tulostustapa") tulostustapa: String,
      @RequestParam("koulutustoimija", required = false) koulutustoimija: String,
      @RequestParam("oppilaitos", required = false) oppilaitos: java.util.Collection[String],
      @RequestParam("toimipiste", required = false) toimipiste: java.util.Collection[String],
      @RequestParam("hakukohde", required = false) hakukohde: java.util.Collection[String],
      @RequestParam("hakukohderyhmat", required = false) hakukohderyhmat: java.util.Collection[String],
      @RequestParam("okm-ohjauksen-ala", required = false) okmOhjauksenAla: java.util.Collection[String],
      @RequestParam("tutkinnon-taso", required = false) tutkinnonTaso: java.util.Collection[String],
      @RequestParam("aidinkieli", required = false) aidinkieli: java.util.Collection[String],
      @RequestParam("kansalaisuus", required = false) kansalaisuus: java.util.Collection[String],
      @RequestParam("sukupuoli", required = false) sukupuoli: String,
      @RequestParam("ensikertalainen", required = false) ensikertalainen: String,
      @RequestParam("nayta-hakutoiveet", required = false) naytaHakutoiveet: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val maybeKoulutustoimija                  = Option(koulutustoimija)
    val tulostustapaValinta                   = Option(tulostustapa).getOrElse("hakukohteittain")
    val naytaHakutoiveetBool                  = Option(naytaHakutoiveet).exists(_.toBoolean)
    val maybeSukupuoli: Option[String]        = if (sukupuoli == "neutral") None else Option(sukupuoli)
    val maybeEnsikertalainen: Option[Boolean] = strToOptionBoolean(ensikertalainen)
    val hakuList                              = if (haku == null) List() else haku.asScala.toList
    val oppilaitosList                        = if (oppilaitos == null) List() else oppilaitos.asScala.toList
    val toimipisteList                        = if (toimipiste == null) List() else toimipiste.asScala.toList
    val hakukohdeList                         = if (hakukohde == null) List() else hakukohde.asScala.toList
    val hakukohdeRyhmaList                    = if (hakukohderyhmat == null) List() else hakukohderyhmat.asScala.toList
    val okmOhjauksenAlaList                   = if (okmOhjauksenAla == null) List() else okmOhjauksenAla.asScala.toList
    val tutkinnonTasoList                     = if (tutkinnonTaso == null) List() else tutkinnonTaso.asScala.toList
    val aidinkieliList                        = if (aidinkieli == null) List() else aidinkieli.asScala.toList
    val kansalaisuusList                      = if (kansalaisuus == null) List() else kansalaisuus.asScala.toList

    val wb = kkHakeneetHyvaksytytVastaanottaneetService.get(
      hakuList,
      tulostustapaValinta,
      maybeKoulutustoimija,
      oppilaitosList,
      toimipisteList,
      hakukohdeList,
      hakukohdeRyhmaList,
      okmOhjauksenAlaList,
      tutkinnonTasoList,
      aidinkieliList,
      kansalaisuusList,
      maybeSukupuoli,
      maybeEnsikertalainen,
      naytaHakutoiveetBool
    )

    val raporttiParams = Map(
      "haku"             -> Option(hakuList).filterNot(_.isEmpty),
      "koulutustoimija"  -> maybeKoulutustoimija,
      "oppilaitos"       -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipiste"       -> Option(toimipisteList).filterNot(_.isEmpty),
      "hakukohde"        -> Option(hakukohdeList).filterNot(_.isEmpty),
      "hakukohderyhma"   -> Option(hakukohdeRyhmaList).filterNot(_.isEmpty),
      "okmOhjauksenAla"  -> Option(okmOhjauksenAlaList).filterNot(_.isEmpty),
      "tutkinnonTaso"    -> Option(tutkinnonTasoList).filterNot(_.isEmpty),
      "aidinkieli"       -> Option(aidinkieliList).filterNot(_.isEmpty),
      "kansalaisuus"     -> Option(kansalaisuusList).filterNot(_.isEmpty),
      "sukupuoli"        -> maybeSukupuoli,
      "ensikertalainen"  -> maybeEnsikertalainen,
      "naytaHakutoiveet" -> naytaHakutoiveetBool
    ).collect { case (key, Some(value)) => key -> value } // jätetään pois tyhjät parametrit

    sendExcel(Some(wb), response, request, "kk-hakeneet-hyvaksytyt-vastaanottaneet", raporttiParams)
  }

}
