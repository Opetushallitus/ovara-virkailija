package fi.oph.ovara.backend.raportointi

import fi.oph.ovara.backend.repository.OvaraDatabase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}

import java.util.Properties

@RestController
@RequestMapping(path = Array("api"))
class Controller {
  @Autowired
  val db: OvaraDatabase = null

  @GetMapping(path = Array("ping"))
  def ping = "Ovara application is running!"

  @GetMapping(path = Array("/koulutukset-toteutukset-hakukohteet"))
  def toteutus = {
    println("!!!!")
    println(db.url)
    println("!!!!")
  }
}
