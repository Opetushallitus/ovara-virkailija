package fi.oph.ovara.backend.raportointi

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
@Hidden
class SpaController {

  @GetMapping(path = Array("/ovara", "/ovara/", "/ovara/{path:[^\\.]*}"))
  def index(): String = "forward:/ovara/index.html"
}
