package fi.oph.ovara.backend.opiskelijavalintatieto

import fi.oph.ovara.backend.service.UserService
import fi.oph.ovara.backend.utils.ParameterValidator.{validateOid, validateOidList}
import fi.oph.ovara.backend.utils.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, MediaType}
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOption

@RestController
@RequestMapping(path = Array("api"))
class OpiskelijavalintatietoController @Autowired() (
  val userService: UserService,
  opiskelijavalintatietoService: OpiskelijavalintatietoService,
  val auditLog: AuditLog = AuditLogObj
) extends ControllerUtils {
  val LOG: Logger = LoggerFactory.getLogger(classOf[OpiskelijavalintatietoController])

  @GetMapping(path = Array("opiskelijavalintatiedot"), produces = Array(MediaType.APPLICATION_JSON_VALUE))
  @Operation(
    summary = "Palauttaa pyydetyn oppijan opiskelijavalintatiedot.",
    description =
      "Palauttaa pyydetyn oppijan hakemukset ja hakemusten hakutoiveet. Jos näitä ei ole, palauttaa vain oppijan tiedot.",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Pyydetyn oppijan opiskelijavalintatiedot."),
      new ApiResponse(
        responseCode = "400",
        description = "Validointivirhe",
        content = Array(new Content(schema = new Schema(implementation = classOf[ValidationError])))
      ),
      new ApiResponse(responseCode = "403", description = "403 virhe", content = Array(new Content())),
      new ApiResponse(responseCode = "404", description = "Henkilöä ei löytynyt", content = Array(new Content())),
      new ApiResponse(
        responseCode = "500",
        description = "Muu virhe",
        content = Array(new Content(schema = new Schema(implementation = classOf[String])))
      )
    )
  )
  def opiskelijavalintatiedot(
    @RequestParam("ovara_oppijanumero", required = true) oppijanumero: String,
    request: HttpServletRequest
  ): OpiskelijavalintatietoResponse =
    withPaakayttajaRole {
      auditLog.logWithParams(request, AuditOperation.Opiskelijavalintatiedot, Map("oppijanumero" -> oppijanumero))

      validate {
        validateOid(Some(oppijanumero), "ovara_oppijanumero")
      }

      LOG.info(s"Haetaan opiskelijavalintatiedot oppijanumerolla: $oppijanumero")

      handleRequest {
        opiskelijavalintatietoService
          .get(List(oppijanumero))
          .map(_.headOption.map(OpiskelijavalintatietoResponse(_)).toJava.orElse(null))
      }
    }

  @PostMapping(path = Array("opiskelijavalintatiedot"))
  @Operation(
    summary = "Palauttaa pyydettyjen oppijoiden opiskelijavalintatiedot.",
    description =
      "Palauttaa pyydettyjen oppijoiden hakemukset ja hakemusten hakutoiveet. Jos näitä ei ole, palauttaa vain oppijoiden tiedot. Palauttaa vain ne oppijat, joiden tiedot löytyvät järjestelmästä.",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Pyydettyjen oppijoiden opiskelijavalintatiedot."),
      new ApiResponse(
        responseCode = "400",
        description = "Validointivirhe",
        content = Array(new Content(schema = new Schema(implementation = classOf[ValidationError])))
      ),
      new ApiResponse(responseCode = "403", description = "403 virhe", content = Array(new Content())),
      new ApiResponse(
        responseCode = "500",
        description = "Muu virhe",
        content = Array(new Content(schema = new Schema(implementation = classOf[String])))
      )
    )
  )
  def opiskelijavalintatiedot(
    @RequestBody oppijanumerot: java.util.Collection[String],
    request: HttpServletRequest
  ): java.util.List[OpiskelijavalintatietoResponse] =
    withPaakayttajaRole {
      auditLog.logWithParams(request, AuditOperation.Opiskelijavalintatiedot, Map("oppijanumerot" -> oppijanumerot))

      val numeroList = getListParamAsScalaList(oppijanumerot)
      validate {
        validateOidList(numeroList, "oppijanumerot")
      }

      LOG.info(s"Haetaan opiskelijavalintatiedot oppijanumeroilla: $oppijanumerot")

      handleRequest {
        opiskelijavalintatietoService
          .get(numeroList)
          .map(_.map(OpiskelijavalintatietoResponse.apply).asJava)
      }
    }

  private def handleRequest[T](block: => Either[String, T]): T = {
    block match {
      case Right(null) =>
        throw ResponseStatusException(HttpStatus.NOT_FOUND)
      case Right(result) =>
        result
      case Left(errorMessage) =>
        // odottamattomista virheistä vain virheviesti
        throw ApiException(errorMessage)
    }
  }
}
