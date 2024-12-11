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

  def getOrganisaatiotByOrganisaatiotyyppi: Map[String, Vector[Organisaatio]] = {
    val user = userService.getEnrichedUserDetails
    val organisaatiot = AuthoritiesUtil.getOrganisaatiot(user.authorities)
    //val res = db.run(commonRepository.selectDistinctOrganisaatiotByOrganisaatiotyyppi(organisaatiot, KOULUTUSTOIMIJAORGANISAATIOTYYPPI))

    val parentChildOrgs = db.run(commonRepository.selectChildOrganisaatiot(organisaatiot))
    val parentOids = parentChildOrgs.groupBy(_.parent_oid).keys.toList
    db.run(commonRepository.selectDistinctOrganisaatiot(parentOids)).groupBy(_.organisaatiotyyppi)
  }
}