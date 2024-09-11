package fi.oph.ovara.backend.raportointi

import fi.oph.ovara.backend.repository.OvaraDatabase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}

import java.util.Properties

@RestController
@RequestMapping("/")
class Controller {
  @Autowired

  @GetMapping("/")
  def index = "Greetings from index!"

  //Health check api
  @GetMapping(path = Array("/hello"))
  def hello: String = {
    "Hello World."
  }

  //@GetMapping(path = Array("/korkeakoulujen-koulutukset-ja-toteutukset"))
  //val toteutukset = OvaraDatabase
}
