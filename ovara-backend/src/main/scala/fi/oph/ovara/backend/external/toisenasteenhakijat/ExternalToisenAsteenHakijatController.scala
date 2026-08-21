package fi.oph.ovara.backend.external.toisenasteenhakijat

import com.fasterxml.jackson.databind.ObjectMapper
import fi.oph.ovara.backend.opiskelijavalintatieto.ValidationError
import fi.oph.ovara.backend.service.UserService
import fi.oph.ovara.backend.utils.ParameterValidator.{validateHakukohdeOid, validateOid, validateOrganisaatioOid}
import fi.oph.ovara.backend.utils.{
  ApiException,
  AuditLog,
  AuditLogObj,
  AuditOperation,
  AuthoritiesUtil,
  ControllerUtils
}
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.http.{HttpHeaders, HttpStatus}
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters.*

@RestController
@RequestMapping(path = Array("api/external"))
class ExternalToisenAsteenHakijatController(
  val userService: UserService,
  hakijatService: ExternalToisenAsteenHakijatService,
  mapper: ObjectMapper,
  auditLog: AuditLog
) extends ControllerUtils(auditLog) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ExternalToisenAsteenHakijatController])

  private def auditParams(
    format: String,
    hakuOid: String,
    hakukohdeOid: Option[String],
    organisaatioOid: Option[String],
    valintarajaus: String
  ): Map[String, Any] = Map(
    "format"          -> format,
    "hakuOid"         -> hakuOid,
    "hakukohdeOid"    -> hakukohdeOid.getOrElse(""),
    "organisaatioOid" -> organisaatioOid.getOrElse(""),
    "valintarajaus"   -> valintarajaus
  )

  // OPH_PAAKAYTTAJA and users with at least one HAKENEET_<oid> authority both qualify for the endpoint.
  private def isAuthorized: Boolean = {
    val authorities = userService.getAuthorities
    authorities.contains(fi.oph.ovara.backend.utils.Constants.OPH_PAAKAYTTAJA_AUTHORITY) ||
    authorities.exists(_.startsWith(fi.oph.ovara.backend.utils.Constants.HAKENEET_AUTHORITY_PREFIX))
  }

  private def requireAuthorized[T](f: => T): T =
    if (isAuthorized) f
    else throw org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN)

  private def resolveKayttooikeusScope: KayttooikeusScope = {
    val authorities = userService.getAuthorities
    if (AuthoritiesUtil.hasOPHPaakayttajaRights(AuthoritiesUtil.getKayttooikeusOids(authorities))) {
      KayttooikeusScope.paakayttaja
    } else {
      // HAKENEET is org-scoped: only OIDs suffixed onto HAKENEET_ authorities count for
      // this endpoint. OIDs granted through 2ASTE / KK / other ovara roles are NOT merged in.
      val hakeneetOrgOids = AuthoritiesUtil.getKayttooikeusOids(
        authorities.filter(_.startsWith(fi.oph.ovara.backend.utils.Constants.HAKENEET_AUTHORITY_PREFIX))
      )
      KayttooikeusScope.limited(hakeneetOrgOids.toSet)
    }
  }

  @GetMapping(path = Array("toisenasteenhakijat"))
  @Operation(
    summary = "Palauttaa hakijoiden tiedot haun ja hakukohteen tai organisaation perusteella",
    description = "Vaatii hakuOid-parametrin sekä joko hakukohdeOid- tai organisaatioOid-parametrin. " +
      "Palauttaa tyhjän listan, jos hakijoita ei löydy. " +
      "Vain hakemuspalvelun (Ataru) hakemukset peruskoulun jälkeisestä yhteishausta palautetaan.",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Hakijoiden tiedot."),
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
  def getHakijat(
    @RequestParam("hakuOid", required = true) hakuOid: String,
    @RequestParam(value = "hakukohdeOid", required = false) hakukohdeOid: String,
    @RequestParam(value = "organisaatioOid", required = false) organisaatioOid: String,
    @RequestParam("valintarajaus", required = true) valintarajaus: String,
    request: HttpServletRequest
  ): HakijatResponse =
    requireAuthorized {
      val hakukohde    = Option(hakukohdeOid).filter(_.nonEmpty)
      val organisaatio = Option(organisaatioOid).filter(_.nonEmpty)
      val parsedRajaus = Valintarajaus.parse(valintarajaus)

      validate {
        List(
          validateOid(Some(hakuOid), "hakuOid"),
          validateHakukohdeOid(hakukohde, "hakukohdeOid"),
          validateOrganisaatioOid(organisaatio, "organisaatioOid"),
          Option.when(hakukohde.isEmpty && organisaatio.isEmpty)(
            "hakukohdeOid_or_organisaatioOid.required"
          ),
          Option.when(parsedRajaus.isEmpty)("valintarajaus.invalid")
        ).flatten
      }

      LOG.info(
        s"Haetaan toisen asteen hakijat. HakuOid: $hakuOid, HakukohdeOid: $hakukohdeOid, " +
          s"OrganisaatioOid: $organisaatioOid, Valintarajaus: $valintarajaus"
      )
      auditLog.logWithParams(
        request,
        AuditOperation.ExternalToisenAsteenHakijat,
        auditParams("json", hakuOid, hakukohde, organisaatio, valintarajaus)
      )
      handleRequest {
        hakijatService
          .getHakijat(hakuOid, hakukohde, organisaatio, parsedRajaus.get, resolveKayttooikeusScope)
          .map(HakijatResponse.apply)
      }
    }

  @GetMapping(path = Array("toisenasteenhakijat/excel"))
  @Operation(
    summary = "Palauttaa hakijoiden tiedot Excel-muodossa haun ja hakukohteen tai organisaation perusteella",
    description = "Sama hakulogiikka kuin JSON-rajapinnassa. Palauttaa .xlsx-tiedoston.",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Hakijoiden tiedot Excel-muodossa."),
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
  def getHakijatExcel(
    @RequestParam("hakuOid", required = true) hakuOid: String,
    @RequestParam(value = "hakukohdeOid", required = false) hakukohdeOid: String,
    @RequestParam(value = "organisaatioOid", required = false) organisaatioOid: String,
    @RequestParam("valintarajaus", required = true) valintarajaus: String,
    request: HttpServletRequest,
    response: HttpServletResponse
  ): Unit = {
    val hakukohde    = Option(hakukohdeOid).filter(_.nonEmpty)
    val organisaatio = Option(organisaatioOid).filter(_.nonEmpty)
    val parsedRajaus = Valintarajaus.parse(valintarajaus)

    if (!isAuthorized) {
      response.setStatus(HttpStatus.FORBIDDEN.value())
      return
    }

    val validationErrors = List(
      validateOid(Some(hakuOid), "hakuOid"),
      validateHakukohdeOid(hakukohde, "hakukohdeOid"),
      validateOrganisaatioOid(organisaatio, "organisaatioOid"),
      Option.when(hakukohde.isEmpty && organisaatio.isEmpty)("hakukohdeOid_or_organisaatioOid.required"),
      Option.when(parsedRajaus.isEmpty)("valintarajaus.invalid")
    ).flatten

    if (validationErrors.nonEmpty) {
      response.setStatus(HttpStatus.BAD_REQUEST.value())
      response.setContentType("application/json")
      val payload = mapper.writeValueAsString(
        Map(
          "status"  -> HttpStatus.BAD_REQUEST.value(),
          "message" -> "virhe.validointi",
          "details" -> validationErrors.asJava
        )
      )
      response.getWriter.write(payload)
      return
    }

    LOG.info(
      s"Haetaan toisen asteen hakijat Excel-muodossa. HakuOid: $hakuOid, HakukohdeOid: $hakukohdeOid, " +
        s"OrganisaatioOid: $organisaatioOid, Valintarajaus: $valintarajaus"
    )
    auditLog.logWithParams(
      request,
      AuditOperation.ExternalToisenAsteenHakijat,
      auditParams("excel", hakuOid, hakukohde, organisaatio, valintarajaus)
    )

    hakijatService.getHakijat(hakuOid, hakukohde, organisaatio, parsedRajaus.get, resolveKayttooikeusScope) match {
      case Left(errorKey) =>
        LOG.error(s"Excel-raportin haku epäonnistui: $errorKey")
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
        response.setContentType("application/json")
        response.getWriter.write(mapper.writeValueAsString(errorKey))

      case Right(hakijat) =>
        val workbook  = ExternalToisenAsteenHakijatExcelWriter.write(hakijat)
        val timestamp = LocalDateTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val filename  = s"toisenasteenhakijat-$timestamp.xlsx"
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, s"attachment; filename=$filename")
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
        try {
          workbook.write(response.getOutputStream)
        } finally {
          workbook.close()
        }
    }
  }

  private def handleRequest[T](block: => Either[String, T]): T =
    block match {
      case Right(result)      => result
      case Left(errorMessage) => throw ApiException(errorMessage)
    }
}
