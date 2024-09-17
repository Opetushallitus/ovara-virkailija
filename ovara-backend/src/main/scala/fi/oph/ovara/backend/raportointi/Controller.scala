package fi.oph.ovara.backend.raportointi

import fi.oph.ovara.backend.repository.OvaraDatabase
import fi.oph.ovara.backend.service.CommonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}

@RestController
@RequestMapping(path = Array("api"))
class Controller(commonService: CommonService) {
  @Autowired
  val db: OvaraDatabase = null

  @GetMapping(path = Array("ping"))
  def ping = "Ovara application is running!"

  @GetMapping(path = Array("/koulutukset-toteutukset-hakukohteet"))
  def getKoulutuksetToteutuksetHakukohteet(@RequestParam oid: String) =
    val res = commonService.getToteutus(oid)
    println(res)
    res
}
