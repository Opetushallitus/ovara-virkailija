package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.{Haku, OrganisaatioHierarkia}
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

  def getOrganisaatioHierarkiat: Vector[OrganisaatioHierarkia] = {
    val user                = userService.getEnrichedUserDetails
    val OPH_PAAKAYTTAJA_OID = "1.2.246.562.10.00000000001"
    val organisaatiot       = AuthoritiesUtil.getOrganisaatiot(user.authorities)

    // TODO: Haetaan käyttäjän organisaatioille organisaatiotyyppi ja sen perusteella haetaan kannasta toimipisteet, oppilaitokset ja koulutustoimijat?
    val parentOids = if (organisaatiot.contains(OPH_PAAKAYTTAJA_OID)) {
      List(OPH_PAAKAYTTAJA_OID)
    } else {
      val parentChildOrgs = db.run(commonRepository.selectChildOrganisaatiot(organisaatiot), "selectChildOrganisaatiot")
      parentChildOrgs.groupBy(_.parent_oid).keys.toList
    }

    val koulutustoimijaHierarkia = db.run(
      commonRepository.selectKoulutustoimijaDescendants(parentOids),
      "selectKoulutustoimijaDescendants"
    )

    if (organisaatiot.contains(OPH_PAAKAYTTAJA_OID)) {
      koulutustoimijaHierarkia
    } else {
      val oppilaitoshierarkia = db.run(
        commonRepository.selectOppilaitosDescendants(parentOids),
        "selectOppilaitosDescendants"
      )

      val toimipistehierarkia = db.run(
        commonRepository.selectToimipisteDescendants(parentOids),
        "selectToimipisteDescendants"
      )

      oppilaitoshierarkia concat koulutustoimijaHierarkia concat toimipistehierarkia
    }
  }
}
