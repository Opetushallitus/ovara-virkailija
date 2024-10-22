package fi.oph.ovara.backend.raportointi

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.oph.ovara.backend.domain.{User, UserResponse}
import fi.oph.ovara.backend.service.CommonService
import fi.oph.ovara.backend.utils.AuthoritiesUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import org.springframework.web.servlet.view.RedirectView

@RestController
@RequestMapping(path = Array("api"))
class Controller(commonService: CommonService) {

  @Value("${ovara.ui.url}")
  val ovaraUiUrl: String = null

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(SerializationFeature.INDENT_OUTPUT, true)

  @GetMapping(path = Array("ping"))
  def ping = "Ovara application is running!"

  @GetMapping(path = Array("user"))
  def user(@AuthenticationPrincipal userDetails: UserDetails): String = {
    mapper.writeValueAsString(
      UserResponse(
        user = if (userDetails == null)
          null
        else User(
          userOid = userDetails.getUsername,
          authorities = AuthoritiesUtil.getRaportointiAuthorities(userDetails.getAuthorities))
      ))
  }

  @GetMapping(path = Array("login"))
  def login = RedirectView(ovaraUiUrl)

  @GetMapping(path = Array("csrf"))
  def csrf(csrfToken: CsrfToken): String = mapper.writeValueAsString(csrfToken)
}
