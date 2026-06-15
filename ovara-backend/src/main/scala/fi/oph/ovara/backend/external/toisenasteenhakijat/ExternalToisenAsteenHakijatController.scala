package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.opiskelijavalintatieto.ValidationError
import fi.oph.ovara.backend.service.UserService
import fi.oph.ovara.backend.utils.ParameterValidator.{validateOid, validateOrganisaatioOid}
import fi.oph.ovara.backend.utils.{ApiException, ControllerUtils}
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}

@RestController
@RequestMapping(path = Array("api/external"))
class ExternalToisenAsteenHakijatController(
  val userService: UserService,
  hakijatService: ExternalToisenAsteenHakijatService
) extends ControllerUtils {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ExternalToisenAsteenHakijatController])

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
    @RequestParam(value = "organisaatioOid", required = false) organisaatioOid: String
  ): HakijatResponse =
    withPaakayttajaRole {
      val hakukohde    = Option(hakukohdeOid).filter(_.nonEmpty)
      val organisaatio = Option(organisaatioOid).filter(_.nonEmpty)

      validate {
        List(
          validateOid(Some(hakuOid), "hakuOid"),
          validateOid(hakukohde, "hakukohdeOid"),
          validateOrganisaatioOid(organisaatio, "organisaatioOid"),
          Option.when(hakukohde.isEmpty && organisaatio.isEmpty)(
            "hakukohdeOid_or_organisaatioOid.required"
          )
        ).flatten
      }

      LOG.info(
        s"Haetaan toisen asteen hakijat. HakuOid: $hakuOid, HakukohdeOid: $hakukohdeOid, OrganisaatioOid: $organisaatioOid"
      )
      handleRequest {
        hakijatService.getHakijat(hakuOid, hakukohde, organisaatio).map(HakijatResponse.apply)
      }
    }

  private def handleRequest[T](block: => Either[String, T]): T =
    block match {
      case Right(result)      => result
      case Left(errorMessage) => throw ApiException(errorMessage)
    }
}
