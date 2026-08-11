package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.opiskelijavalintatieto.ValidationError
import fi.oph.ovara.backend.service.UserService
import fi.oph.ovara.backend.utils.ParameterValidator.{validateOid, validateOidList}
import fi.oph.ovara.backend.utils.{AuditLog, AuditOperation, ControllerUtils}
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*

import scala.jdk.CollectionConverters.*

@RestController
@RequestMapping(path = Array("api"))
class ValpasController(
  val userService: UserService,
  valpasService: ValpasService,
  auditLog: AuditLog,
  @Value("${opintopolku.virkailija.url}") virkailijaUrl: String
) extends ControllerUtils(auditLog) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ValpasController])

  @GetMapping(path = Array("valpas"))
  @Operation(
    summary = "Palauttaa Valpas-tiedot henkilön oppijanumerolla",
    description = "Palauttaa tyhjän listan, jos oppijanumerolla ei löydy hakemuksia.",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Pyydetyn oppijan Valpas-tiedot."),
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
  def singleValpas(
    @RequestParam("ovara_oppijanumero", required = true) oppijanumero: String,
    @RequestParam("ovara_vain_aktiiviset", defaultValue = "false") vainAktiiviset: Boolean,
    request: HttpServletRequest
  ): java.util.List[HakemusResponse] =
    withPaakayttajaRole {
      validate {
        validateOid(Some(oppijanumero), "ovara_oppijanumero")
      }

      val params = Map("oppijanumero" -> oppijanumero, "vainAktiiviset" -> vainAktiiviset)
      LOG.info(s"Haetaan valpas-tiedot parametreillä: $params")

      handleApiRequest(
        request,
        AuditOperation.Valpastiedot,
        params, {
          valpasService.getValpasTiedot(List(oppijanumero), vainAktiiviset).map {
            _.map(h => HakemusResponse(h, virkailijaUrl)).asJava
          }
        }
      )
    }

  @PostMapping(path = Array("valpas"))
  @Operation(
    summary = "Palauttaa Valpas-tiedot henkilöiden oppijanumeroilla",
    description = "Palauttaa tyhjän listan, jos oppijanumerolla ei löydy hakemuksia.",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Pyydetyn oppijan Valpas-tiedot."),
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
  def manyValpas(
    @RequestBody oppijanumerot: java.util.Collection[String],
    @RequestParam("ovara_vain_aktiiviset", defaultValue = "false") vainAktiiviset: Boolean,
    request: HttpServletRequest
  ): java.util.List[HakemusResponse] =
    withPaakayttajaRole {
      val numeroList = getListParamAsScalaList(oppijanumerot)
      validate {
        validateOidList(numeroList, "ovara_oppijanumero")
      }

      val params = Map("oppijanumerot" -> oppijanumerot, "vainAktiiviset" -> vainAktiiviset)
      LOG.info(s"Haetaan valpas-tiedot parametreillä: $params")

      handleApiRequest(
        request,
        AuditOperation.Valpastiedot,
        params, {
          valpasService.getValpasTiedot(numeroList, vainAktiiviset).map {
            _.map(h => HakemusResponse(h, virkailijaUrl)).asJava
          }
        }
      )
    }
}
