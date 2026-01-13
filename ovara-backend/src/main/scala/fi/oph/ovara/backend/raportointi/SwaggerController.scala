package fi.oph.ovara.backend.raportointi

import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}

@RequestMapping(path = Array("/swagger"))
@RestController
@Hidden
class SwaggerController {

  @Value("${ovara.backend.url}")
  val ovara_backend_url: String = null

  @GetMapping(path = Array(""))
  def redirect(response: HttpServletResponse): Unit = {
    response.sendRedirect(s"$ovara_backend_url/swagger-ui/index.html")
  }

}
