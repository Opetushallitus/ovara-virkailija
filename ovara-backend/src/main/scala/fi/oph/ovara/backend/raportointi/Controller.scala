package fi.oph.ovara.backend.raportointi

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.oph.ovara.backend.domain.{User, UserResponse}
import fi.oph.ovara.backend.service.{CommonService, KoulutuksetToteutuksetHakukohteetService, OnrService}
import fi.oph.ovara.backend.utils.AuthoritiesUtil
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.{HttpHeaders, MediaType}
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}
import org.springframework.web.servlet.view.RedirectView

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters.*

@RestController
@RequestMapping(path = Array("api"))
class Controller(
    onrService: OnrService,
    commonService: CommonService,
    koulutuksetToteutuksetHakukohteetService: KoulutuksetToteutuksetHakukohteetService
) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[Controller]);

  @Value("${ovara.ui.url}")
  val ovaraUiUrl: String = null

  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(SerializationFeature.INDENT_OUTPUT, true)

  @GetMapping(path = Array("ping"))
  def ping = "Ovara application is running!"

  @GetMapping(path = Array("user"))
  def user(@AuthenticationPrincipal userDetails: UserDetails): String = {
    mapper.writeValueAsString(
      UserResponse(
        user =
          if (userDetails == null)
            null
          else
            val asiointikieli = onrService.getAsiointikieli(userDetails.getUsername) match
              case Left(e) => None
              case Right(v) => Some(v)

            User(
              userOid = userDetails.getUsername,
              authorities = AuthoritiesUtil.getRaportointiAuthorities(userDetails.getAuthorities),
              asiointikieli = asiointikieli
            )
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

  @GetMapping(path = Array("koulutukset-toteutukset-hakukohteet"))
  def koulutukset_toteutukset_hakukohteet(
      @RequestParam("alkamiskausi") alkamiskausi: java.util.Collection[String],
      @RequestParam("haku") haku: java.util.Collection[String],
      @RequestParam("koulutuksenTila", required = false) koulutuksenTila: String,
      @RequestParam("toteutuksenTila", required = false) toteutuksenTila: String,
      @RequestParam("hakukohteenTila", required = false) hakukohteenTila: String,
      @RequestParam("valintakoe", required = false) valintakoe: Boolean,
      response: HttpServletResponse
  ): Unit = {
    val maybeKoulutuksenTila = Option(koulutuksenTila)
    val maybeToteutuksenTila = Option(toteutuksenTila)
    val maybeHakukohteenTila = Option(hakukohteenTila)
    val maybeValintakoe      = Option(valintakoe)
    val wb = koulutuksetToteutuksetHakukohteetService.
      get(
      alkamiskausi.asScala.toList,
      haku.asScala.toList,
      maybeKoulutuksenTila,
      maybeToteutuksenTila,
      maybeHakukohteenTila,
      maybeValintakoe
    )
    try {
      LOG.info(s"Sending excel in the response")
      val date: LocalDateTime = LocalDateTime.now().withNano(0)
      val dateTimeStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
      val out = response.getOutputStream
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, s"attachment; filename=\"koulutukset-toteutukset-hakukohteet-$dateTimeStr.xlsx\"")
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
