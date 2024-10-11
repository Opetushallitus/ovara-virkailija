package fi.oph.ovara.backend.raportointi

import fi.oph.ovara.backend.domain.{UserResponse, UserResponseUser}
import fi.oph.ovara.backend.repository.OvaraDatabase
import fi.oph.ovara.backend.service.CommonService
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import org.springframework.web.servlet.view.RedirectView

@RestController
@RequestMapping(path = Array("api"))
class Controller(commonService: CommonService) {
  
  @Autowired
  val db: OvaraDatabase = null

  @Value("${ovara.ui.url}")
  val ovaraUiUrl: String = null
  

  @GetMapping(path = Array("ping"))
  def ping = "Ovara application is running!"

  @GetMapping(path = Array("user"))
  def user(@AuthenticationPrincipal userDetails: UserDetails): UserResponse = {
    UserResponse(
      user = if (userDetails == null) null else UserResponseUser(userOid = userDetails.getUsername)
    )
  }

  @GetMapping(path = Array("login"))
  def login = RedirectView(ovaraUiUrl)
}
