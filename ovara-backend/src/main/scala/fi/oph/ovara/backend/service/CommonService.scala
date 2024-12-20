package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.{Haku, OrganisaatioPerOrganisaatiotyyppi}
import fi.oph.ovara.backend.repository.{CommonRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.AuthoritiesUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CommonService(commonRepository: CommonRepository, userService: UserService) {
  @Autowired
  val db: OvaraDatabase = null

  def getAlkamisvuodet: Vector[String] = {
    db.run(commonRepository.selectDistinctAlkamisvuodet(), "selectDistinctAlkamisvuodet")
  }

  def getHaut: Vector[Haku] = {
    db.run(commonRepository.selectDistinctExistingHaut(), "selectDistinctExistingHaut")
  }

  def getUserOrganisaatiotByOrganisaatiotyyppi: Map[String, Vector[OrganisaatioPerOrganisaatiotyyppi]] = {
    val user          = userService.getEnrichedUserDetails
    val organisaatiot = AuthoritiesUtil.getOrganisaatiot(user.authorities)

    val parentChildOrgs = db.run(commonRepository.selectChildOrganisaatiot(organisaatiot), "selectChildOrganisaatiot")
    val parentOids      = parentChildOrgs.groupBy(_.parent_oid).keys.toList
    db.run(
      commonRepository.selectOrganisaatiotPerOrganisaatiotyyppi(parentOids),
      "selectOrganisaatiotPerOrganisaatiotyyppi"
    ).groupBy(_.organisaatiotyyppi)
  }
}
