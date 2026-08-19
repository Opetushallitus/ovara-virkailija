package fi.oph.ovara.backend.external.kkhakijat

import com.fasterxml.jackson.databind.ObjectMapper
import fi.oph.ovara.backend.opiskelijavalintatieto.ValidationError
import fi.oph.ovara.backend.service.UserService
import fi.oph.ovara.backend.utils.ParameterValidator.{
  validateHakukohdeOid,
  validateHakukohderyhmaOid,
  validateOid,
  validateOrganisaatioOid
}
import fi.oph.ovara.backend.utils.{
  ApiException,
  AuditLog,
  AuditLogObj,
  AuditOperation,
  AuthoritiesUtil,
  Constants,
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
class ExternalKKHakijatController(
  val userService: UserService,
  kkHakijatService: ExternalKKHakijatService,
  mapper: ObjectMapper,
  auditLog: AuditLog
) extends ControllerUtils(auditLog) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ExternalKKHakijatController])

  private def auditParams(
    format: String,
    hakuOid: String,
    hakukohdeOid: Option[String],
    hakukohderyhmaOid: Option[String],
    organisaatioOid: Option[String],
    valintarajaus: String
  ): Map[String, Any] = Map(
    "format"            -> format,
    "hakuOid"           -> hakuOid,
    "hakukohdeOid"      -> hakukohdeOid.getOrElse(""),
    "hakukohderyhmaOid" -> hakukohderyhmaOid.getOrElse(""),
    "organisaatioOid"   -> organisaatioOid.getOrElse(""),
    "valintarajaus"     -> valintarajaus
  )

  private def validationErrors(
    hakuOid: String,
    hakukohdeOid: Option[String],
    hakukohderyhmaOid: Option[String],
    organisaatioOid: Option[String],
    parsedRajaus: Option[Valintarajaus]
  ): List[String] = List(
    validateOid(Some(hakuOid), "hakuOid"),
    validateHakukohdeOid(hakukohdeOid, "hakukohdeOid"),
    validateHakukohderyhmaOid(hakukohderyhmaOid, "hakukohderyhmaOid"),
    validateOrganisaatioOid(organisaatioOid, "organisaatioOid"),
    Option.when(hakukohdeOid.isEmpty && hakukohderyhmaOid.isEmpty && organisaatioOid.isEmpty)(
      "hakukohdeOid_or_organisaatioOid_or_hakukohderyhmaOid.required"
    ),
    Option.when(parsedRajaus.isEmpty)("valintarajaus.invalid")
  ).flatten

  // OPH_PAAKAYTTAJA and users with at least one KK_HAKENEET_<oid> authority both qualify for the endpoint.
  private def isAuthorized: Boolean = {
    val authorities = userService.getAuthorities
    authorities.contains(Constants.OPH_PAAKAYTTAJA_AUTHORITY) ||
    authorities.exists(_.startsWith(Constants.KK_HAKENEET_AUTHORITY_PREFIX))
  }

  private def requireAuthorized[T](f: => T): T =
    if (isAuthorized) f
    else throw org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN)

  private def resolveKayttooikeusScope: KayttooikeusScope = {
    val authorities = userService.getAuthorities
    if (AuthoritiesUtil.hasOPHPaakayttajaRights(AuthoritiesUtil.getKayttooikeusOids(authorities))) {
      KayttooikeusScope.paakayttaja
    } else {
      // KK_HAKENEET is org-scoped: only OIDs suffixed onto KK_HAKENEET_ authorities count for
      // this endpoint. OIDs granted through 2ASTE / HAKENEET / other ovara roles are NOT merged in.
      val kkHakeneetOrgOids = AuthoritiesUtil.getKayttooikeusOids(
        authorities.filter(_.startsWith(Constants.KK_HAKENEET_AUTHORITY_PREFIX))
      )
      KayttooikeusScope.limited(kkHakeneetOrgOids.toSet)
    }
  }

  @GetMapping(path = Array("kkhakijat"))
  @Operation(
    summary = "Palauttaa KK-hakijoiden tiedot haun ja hakukohteen, hakukohderyhmän tai organisaation perusteella",
    description = "Vaatii hakuOid-parametrin sekä vähintään yhden rajaavista parametreista " +
      "hakukohdeOid, hakukohderyhmaOid tai organisaatioOid. Useampi rajaava parametri leikataan " +
      "keskenään: hakukohderyhmaOid laajennetaan ryhmään kuuluviksi hakukohteiksi. " +
      "Palauttaa tyhjän listan, jos hakijoita ei löydy. " +
      "Vain hakemuspalvelun (Ataru) hakemukset korkeakoulujen yhteishausta palautetaan.",
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
    @RequestParam(value = "hakukohderyhmaOid", required = false) hakukohderyhmaOid: String,
    @RequestParam(value = "organisaatioOid", required = false) organisaatioOid: String,
    @RequestParam("valintarajaus", required = true) valintarajaus: String,
    request: HttpServletRequest
  ): KKHakijatResponse =
    requireAuthorized {
      val hakukohde      = Option(hakukohdeOid).filter(_.nonEmpty)
      val hakukohderyhma = Option(hakukohderyhmaOid).filter(_.nonEmpty)
      val organisaatio   = Option(organisaatioOid).filter(_.nonEmpty)
      val parsedRajaus   = Valintarajaus.parse(valintarajaus)

      validate {
        validationErrors(hakuOid, hakukohde, hakukohderyhma, organisaatio, parsedRajaus)
      }

      LOG.info(
        s"Haetaan KK-hakijat. HakuOid: $hakuOid, HakukohdeOid: $hakukohdeOid, " +
          s"HakukohderyhmaOid: $hakukohderyhmaOid, OrganisaatioOid: $organisaatioOid, " +
          s"Valintarajaus: $valintarajaus"
      )
      auditLog.logWithParams(
        request,
        AuditOperation.ExternalKKHakijat,
        auditParams("json", hakuOid, hakukohde, hakukohderyhma, organisaatio, valintarajaus)
      )
      handleRequest {
        kkHakijatService
          .getKKHakijat(
            hakuOid,
            hakukohde,
            organisaatio,
            parsedRajaus.get,
            resolveKayttooikeusScope,
            hakukohderyhma
          )
          .map(KKHakijatResponse.apply)
      }
    }

  @GetMapping(path = Array("kkhakijat/excel"))
  @Operation(
    summary =
      "Palauttaa KK-hakijoiden tiedot Excel-muodossa haun ja hakukohteen, hakukohderyhmän tai organisaation perusteella",
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
    @RequestParam(value = "hakukohderyhmaOid", required = false) hakukohderyhmaOid: String,
    @RequestParam(value = "organisaatioOid", required = false) organisaatioOid: String,
    @RequestParam("valintarajaus", required = true) valintarajaus: String,
    request: HttpServletRequest,
    response: HttpServletResponse
  ): Unit = {
    val hakukohde      = Option(hakukohdeOid).filter(_.nonEmpty)
    val hakukohderyhma = Option(hakukohderyhmaOid).filter(_.nonEmpty)
    val organisaatio   = Option(organisaatioOid).filter(_.nonEmpty)
    val parsedRajaus   = Valintarajaus.parse(valintarajaus)

    if (!isAuthorized) {
      response.setStatus(HttpStatus.FORBIDDEN.value())
      return
    }

    val errors = validationErrors(hakuOid, hakukohde, hakukohderyhma, organisaatio, parsedRajaus)

    if (errors.nonEmpty) {
      response.setStatus(HttpStatus.BAD_REQUEST.value())
      response.setContentType("application/json")
      val payload = mapper.writeValueAsString(
        Map(
          "status"  -> HttpStatus.BAD_REQUEST.value(),
          "message" -> "virhe.validointi",
          "details" -> errors.asJava
        )
      )
      response.getWriter.write(payload)
      return
    }

    LOG.info(
      s"Haetaan KK-hakijat Excel-muodossa. HakuOid: $hakuOid, HakukohdeOid: $hakukohdeOid, " +
        s"HakukohderyhmaOid: $hakukohderyhmaOid, OrganisaatioOid: $organisaatioOid, " +
        s"Valintarajaus: $valintarajaus"
    )
    auditLog.logWithParams(
      request,
      AuditOperation.ExternalKKHakijat,
      auditParams("excel", hakuOid, hakukohde, hakukohderyhma, organisaatio, valintarajaus)
    )

    kkHakijatService.getKKHakijat(
      hakuOid,
      hakukohde,
      organisaatio,
      parsedRajaus.get,
      resolveKayttooikeusScope,
      hakukohderyhma
    ) match {
      case Left(errorKey) =>
        LOG.error(s"Excel-raportin haku epäonnistui: $errorKey")
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
        response.setContentType("application/json")
        response.getWriter.write(mapper.writeValueAsString(errorKey))

      case Right(hakijat) =>
        val workbook  = ExternalKKHakijatExcelWriter.write(hakijat)
        val timestamp = LocalDateTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val filename  = s"kkhakijat-$timestamp.xlsx"
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
