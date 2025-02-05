package fi.oph.ovara.backend.raportointi

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.oph.ovara.backend.domain.UserResponse
import fi.oph.ovara.backend.service.{
  CommonService,
  HakijatService,
  KoulutuksetToteutuksetHakukohteetService,
  UserService
}
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
      @RequestParam("alkamiskausi", required = false) alkamiskaudet: java.util.Collection[String]
  ): String = {
    val alkamiskaudetList =
      if (alkamiskaudet == null) List() else alkamiskaudet.asScala.toList

    mapper.writeValueAsString(commonService.getHaut(alkamiskaudetList))
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

  @GetMapping(path = Array("organisaatiot"))
  def organisaatiot: String = mapper.writeValueAsString(commonService.getOrganisaatioHierarkiatWithUserRights)

  @GetMapping(path = Array("harkinnanvaraisuudet"))
  def harkinnanvaraisuudet: String = mapper.writeValueAsString(commonService.getHarkinnanvaraisuudet)

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
      @RequestParam("alkamiskausi") alkamiskausi: java.util.Collection[String],
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
    val oppilaitosList   = if (oppilaitos == null) List() else oppilaitos.asScala.toList
    val toimipisteList   = if (toimipiste == null) List() else toimipiste.asScala.toList
    val alkamiskausiList = if (alkamiskausi == null) List() else alkamiskausi.asScala.toList
    val hakuList         = if (haku == null) List() else haku.asScala.toList

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
      "alkamiskausi"    -> Option(alkamiskausiList).filterNot(_.isEmpty),
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
      @RequestParam("vastaanottotieto", required = false) vastaanottotieto: java.util.Collection[String],
      @RequestParam("markkinointilupa", required = false) markkinointilupa: String,
      @RequestParam("julkaisulupa", required = false) julkaisulupa: String,
      request: HttpServletRequest,
      response: HttpServletResponse
  ): Unit = {
    val hakuList             = if (haku == null) List() else haku.asScala.toList
    val oppilaitosList       = if (oppilaitos == null) List() else oppilaitos.asScala.toList
    val toimipisteList       = if (toimipiste == null) List() else toimipiste.asScala.toList
    val hakukohdeList        = if (hakukohde == null) List() else hakukohde.asScala.toList
    val vastaanottotietoList = if (vastaanottotieto == null) List() else vastaanottotieto.asScala.toList

    val maybeMarkkinointilupa = if (markkinointilupa == null) {
      None
    } else {
      Option(markkinointilupa.toBoolean)
    }

    val maybeJulkaisulupa = if (julkaisulupa == null) {
      None
    } else {
      Option(julkaisulupa.toBoolean)
    }

    val wb = hakijatService.get(
      hakuList,
      oppilaitosList,
      toimipisteList,
      hakukohdeList,
      vastaanottotietoList,
      maybeMarkkinointilupa,
      maybeJulkaisulupa
    )

    val raporttiParams = Map(
      "haku"             -> Option(hakuList).filterNot(_.isEmpty),
      "oppilaitos"       -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipiste"       -> Option(toimipisteList).filterNot(_.isEmpty),
      "hakukohde"        -> Option(hakukohdeList).filterNot(_.isEmpty),
      "vastaanottotieto" -> Option(vastaanottotietoList).filterNot(_.isEmpty),
      "markkinointilupa" -> maybeMarkkinointilupa,
      "julkaisulupa"     -> maybeJulkaisulupa
    ).collect { case (key, Some(value)) => key -> value } // jätetään pois tyhjät parametrit

    sendExcel(wb, response, request, "hakijat", raporttiParams)
  }
}
