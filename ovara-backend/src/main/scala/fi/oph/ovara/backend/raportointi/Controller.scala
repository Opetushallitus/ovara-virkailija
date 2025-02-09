package fi.oph.ovara.backend.raportointi

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.oph.ovara.backend.domain.UserResponse
import fi.oph.ovara.backend.service.{CommonService, KoulutuksetToteutuksetHakukohteetService, UserService}
import fi.oph.ovara.backend.utils.AuditLog
import fi.oph.ovara.backend.utils.AuditOperation.KoulutuksetToteutuksetHakukohteet
import fi.vm.sade.auditlog.{Changes, Target}
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
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
  def haut: String = mapper.writeValueAsString(commonService.getHaut)

  @GetMapping(path = Array("organisaatiot"))
  def organisaatiot: String = mapper.writeValueAsString(commonService.getOrganisaatioHierarkiatWithUserRights)

  // RAPORTIT

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
    val oppilaitosList = if (oppilaitos == null) List() else oppilaitos.asScala.toList
    val toimipisteList = if (toimipiste == null) List() else toimipiste.asScala.toList

    val wb = koulutuksetToteutuksetHakukohteetService.get(
      alkamiskausi.asScala.toList,
      haku.asScala.toList,
      maybeKoulutustoimija,
      oppilaitosList,
      toimipisteList,
      maybeKoulutuksenTila,
      maybeToteutuksenTila,
      maybeHakukohteenTila,
      maybeValintakoe
    )
    val alkamiskausiList = if (alkamiskausi == null) List() else alkamiskausi.asScala.toList
    val hakuList = if (haku == null) List() else haku.asScala.toList

    val raporttiParams = Map(
      "alkamiskausi" -> Option(alkamiskausiList).filterNot(_.isEmpty),
      "haku" -> Option(hakuList).filterNot(_.isEmpty),
      "koulutustoimija" -> maybeKoulutustoimija,
      "oppilaitos" -> Option(oppilaitosList).filterNot(_.isEmpty),
      "toimipiste" -> Option(toimipisteList).filterNot(_.isEmpty),
      "koulutuksenTila" -> maybeKoulutuksenTila,
      "toteutuksenTila" -> maybeToteutuksenTila,
      "hakukohteenTila" -> maybeHakukohteenTila,
      "valintakoe" -> maybeValintakoe
    ).collect { case (key, Some(value)) => key -> value } // jätetään pois tyhjät parametrit
    try {
      auditLog.logWithParams(request, KoulutuksetToteutuksetHakukohteet, raporttiParams)
      LOG.info(s"Sending excel in the response")
      val date: LocalDateTime = LocalDateTime.now().withNano(0)
      val dateTimeStr         = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
      val out                 = response.getOutputStream
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      response.setHeader(
        HttpHeaders.CONTENT_DISPOSITION,
        s"attachment; filename=\"koulutukset-toteutukset-hakukohteet-$dateTimeStr.xlsx\""
      )
      wb.write(out)
      out.close()
      wb.close()
    } catch {
      case e: Exception =>
        LOG.error(s"Error sending excel: ${e.getMessage}")
        response.sendError(500)
    }
  }
}
