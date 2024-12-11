package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.{Haku, KOULUTUSTOIMIJAORGANISAATIOTYYPPI, Organisaatio}
import fi.oph.ovara.backend.repository.{CommonRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.AuthoritiesUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CommonService(commonRepository: CommonRepository, userService: UserService) {
  @Autowired
  val db: OvaraDatabase = null

  def getAlkamisvuodet: Vector[String] = {
    db.run(commonRepository.selectDistinctAlkamisvuodet())
  }

  def getHaut: Vector[Haku] = {
    db.run(commonRepository.selectDistinctExistingHaut())
  }

  def getKoulutustoimijat: Vector[Organisaatio] = {
    val u = userService.getEnrichedUserDetails
    println("getKoulutustoimijat")
    println(u)
    val organisaatiot = AuthoritiesUtil.getOrganisaatiot(u.authorities)
    println(organisaatiot)
    val res = db.run(commonRepository.selectDistinctOrganisaatiotByOrganisaatiotyyppi(organisaatiot, KOULUTUSTOIMIJAORGANISAATIOTYYPPI))
    println(res)
    res
  }
}