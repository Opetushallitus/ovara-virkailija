package fi.oph.ovara.backend.raportointi

import fi.oph.ovara.backend.opiskelijavalintatieto.ValidationError
import fi.oph.ovara.backend.raportointi.dto.{
  buildKkPaatettavatOpiskeluoikeudetAuditParams,
  buildKkPaatettavatOpiskeluoikeudetResponse,
  KkPaatettavatOpiskeluoikeudetParams,
  KkPaatettavatOpiskeluoikeudetResponse
}
import fi.oph.ovara.backend.service.{KkPaatettavatOpiskeluoikeudetService, UserService}
import fi.oph.ovara.backend.utils.AuditOperation.KkPaatettavatOpiskeluoikeudet
import fi.oph.ovara.backend.utils.Constants.OPH_PAAKAYTTAJA_AUTHORITY
import fi.oph.ovara.backend.utils.ParameterValidator.validateKkPaatettavatOpiskeluoikeudetParams
import fi.oph.ovara.backend.utils.{ApiException, AuditLog, AuditLogObj, ControllerUtils}
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.{HttpStatus, MediaType}
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}
import org.springframework.web.server.ResponseStatusException

import java.time.ZonedDateTime

@RestController
@RequestMapping(path = Array("api/external"))
class ExternalController(
  val userService: UserService,
  kkPaatettavatOpiskeluoikeudetService: KkPaatettavatOpiskeluoikeudetService,
  val auditLog: AuditLog = AuditLogObj,
  @Value("${yos-json-rajapinta-enabled:false}") yosJsonRajapintaEnabled: Boolean = true
) extends ControllerUtils {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ExternalController])

  private def hasYosOrPaakayttajaRole: Boolean = {
    val authorities = userService.getAuthorities
    authorities.contains(OPH_PAAKAYTTAJA_AUTHORITY) ||
    authorities.exists(_.startsWith("ROLE_APP_OVARA-VIRKAILIJA_KK_YOS"))
  }

  private def handleRequest[T](block: => Either[String, T]): T = {
    block match {
      case Right(result) =>
        result
      case Left(errorMessage) =>
        throw ApiException(errorMessage)
    }
  }

  @GetMapping(path = Array("kk-paatettavat-opiskeluoikeudet"), produces = Array(MediaType.APPLICATION_JSON_VALUE))
  @Operation(
    summary = "Palauttaa päätettävät opiskeluoikeudet JSON-muodossa.",
    description = "Palauttaa samat päätettävät opiskeluoikeudet kuin Excel-muotoinen raportti " +
      "(/api/kk-paatettavat-opiskeluoikeudet), mutta JSON-muodossa. Tarkoitettu ulkopuolisten " +
      "järjestelmien käyttöön.",
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Päätettävät opiskeluoikeudet henkilöittäin ryhmiteltynä.",
        content = Array(
          new Content(schema = new Schema(implementation = classOf[KkPaatettavatOpiskeluoikeudetResponse]))
        )
      ),
      new ApiResponse(
        responseCode = "400",
        description = "Validointivirhe",
        content = Array(new Content(schema = new Schema(implementation = classOf[ValidationError])))
      ),
      new ApiResponse(
        responseCode = "401",
        description = "Käyttäjällä ei ole voimassa olevaa istuntoa",
        content = Array(new Content(schema = new Schema(implementation = classOf[ValidationError])))
      ),
      new ApiResponse(
        responseCode = "403",
        description = "Käyttäjällä on voimassa oleva istunto, mutta hänellä ei ole oikeuksia käyttää tätä rajapintaa",
        content = Array(new Content())
      ),
      new ApiResponse(
        responseCode = "404",
        description = "Organisaatiota ei löytynyt",
        content = Array(new Content())
      ),
      new ApiResponse(
        responseCode = "404",
        description = "Rajapinta ei ole käytössä",
        content = Array(new Content())
      ),
      new ApiResponse(
        responseCode = "500",
        description = "Muu virhe",
        content = Array(new Content(schema = new Schema(implementation = classOf[String])))
      )
    )
  )
  def kkPaatettavatOpiskeluoikeudet(
    @Parameter(description = "Oppilaitoksen organisaatio-oid", required = true)
    @RequestParam("ovara_oppilaitos", required = true) oppilaitos: String,
    @Parameter(description = "Sukunimi")
    @RequestParam("ovara_sukunimi", required = false) sukunimi: String,
    @Parameter(description = "Etunimet")
    @RequestParam("ovara_etunimi", required = false) etunimet: String,
    @Parameter(description = "Hetu")
    @RequestParam("ovara_hetu", required = false) hetu: String,
    @Parameter(description = "Oppijanumero")
    @RequestParam("ovara_oppijanumero", required = false) oppijanumero: String,
    @Parameter(description = "Opiskeluoikeuden tila")
    @RequestParam("ovara_opiskeluoikeuden_tila", required = false) opiskeluoikeudenTila: String,
    request: HttpServletRequest
  ): KkPaatettavatOpiskeluoikeudetResponse = {
    if (!yosJsonRajapintaEnabled) {
      throw ResponseStatusException(HttpStatus.NOT_FOUND, "Rajapinta ei ole käytössä")
    }

    if (!hasYosOrPaakayttajaRole) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val params = KkPaatettavatOpiskeluoikeudetParams(
      oppilaitos = oppilaitos,
      sukunimi = Option(sukunimi),
      etunimet = Option(etunimet),
      hetu = Option(hetu),
      oppijanumero = Option(oppijanumero),
      opiskeluoikeudenTila = Option(opiskeluoikeudenTila)
    )

    validate {
      validateKkPaatettavatOpiskeluoikeudetParams(params).left.getOrElse(Nil)
    }

    if (!kkPaatettavatOpiskeluoikeudetService.organisaatioExists(params.oppilaitos)) {
      throw ResponseStatusException(HttpStatus.NOT_FOUND, "Organisaatiota ei löytynyt")
    }

    handleRequest {
      kkPaatettavatOpiskeluoikeudetService.getData(params).map { data =>
        auditLog.logWithParams(
          request,
          KkPaatettavatOpiskeluoikeudet,
          buildKkPaatettavatOpiskeluoikeudetAuditParams(params)
        )
        val asiointikieli = userService.getEnrichedUserDetails.asiointikieli.getOrElse("fi")
        buildKkPaatettavatOpiskeluoikeudetResponse(data, ZonedDateTime.now(), asiointikieli)
      }
    }
  }
}
