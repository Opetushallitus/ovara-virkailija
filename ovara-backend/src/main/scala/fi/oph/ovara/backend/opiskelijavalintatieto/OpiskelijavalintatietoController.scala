package fi.oph.ovara.backend.opiskelijavalintatieto

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.oph.ovara.backend.service.UserService
import fi.oph.ovara.backend.utils.Constants.OPH_PAAKAYTTAJA_AUTHORITY
import fi.oph.ovara.backend.utils.ControllerUtils.{getListParamAsScalaList, handleRequest}
import fi.oph.ovara.backend.utils.ParameterValidator.{validateOid, validateOidList}
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.{
  GetMapping,
  PostMapping,
  RequestBody,
  RequestMapping,
  RequestParam,
  RestController
}

@RestController
@RequestMapping(path = Array("api"))
class OpiskelijavalintatietoController(
    userService: UserService,
    opiskelijavalintatietoService: OpiskelijavalintatietoService
) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[OpiskelijavalintatietoController])

  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(SerializationFeature.INDENT_OUTPUT, true)
  mapper.configure(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL, true)

  @GetMapping(path = Array("opiskelijavalintatiedot"))
  def opiskelijavalintatiedot(
      @RequestParam("ovara_oppijanumero", required = true) oppijanumero: String
  ): ResponseEntity[String] = withPaakayttajaRole {
    val errors = validateOid(Some(oppijanumero), "ovara_oppijanumero").toList

    handleRequest(errors, mapper) {
      opiskelijavalintatietoService.get(List(oppijanumero)).map(_.headOption.orNull)
    }
  }

  @PostMapping(path = Array("opiskelijavalintatiedot"))
  def opiskelijavalintatiedot(@RequestBody oppijanumerot: java.util.Collection[String]): ResponseEntity[String] =
    withPaakayttajaRole {
      val numeroList = getListParamAsScalaList(oppijanumerot)
      val errors     = validateOidList(numeroList, "oppijanumerot")

      handleRequest(errors, mapper) {
        opiskelijavalintatietoService.get(numeroList)
      }
    }

  private def withPaakayttajaRole(f: => ResponseEntity[String]): ResponseEntity[String] = {
    val user = userService.getEnrichedUserDetails

    if (user.authorities.contains(OPH_PAAKAYTTAJA_AUTHORITY)) {
      f
    } else {
      ResponseEntity.status(HttpServletResponse.SC_FORBIDDEN).build()
    }

  }
}
