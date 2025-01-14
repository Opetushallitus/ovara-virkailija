package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.{Haku, OrganisaatioHierarkia}
import fi.oph.ovara.backend.repository.{CommonRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, OrganisaatioUtils}
import fi.oph.ovara.backend.utils.Constants.OPH_PAAKAYTTAJA_OID
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

  def getOrganisaatioHierarkiatWithUserRights: List[OrganisaatioHierarkia] = {
    val user                = userService.getEnrichedUserDetails
    val organisaatiot       = AuthoritiesUtil.getOrganisaatiot(user.authorities)

    val parentOids = if (organisaatiot.contains(OPH_PAAKAYTTAJA_OID)) {
      List(OPH_PAAKAYTTAJA_OID)
    } else {
      val parentChildOrgs = db.run(commonRepository.selectChildOrganisaatiot(organisaatiot), "selectChildOrganisaatiot")
      parentChildOrgs.groupBy(_.parent_oid).keys.toList
    }

    // TODO: Haetaan käyttäjän organisaatioille organisaatiotyyppi ja sen perusteella haetaan kannasta toimipisteet, oppilaitokset ja koulutustoimijat?
    // TODO: Nyt haetaan kaikilla parentOidseilla joka tasolta riippumatta parentoidin organisaation tyypistä
    val koulutustoimijahierarkia = getKoulutustoimijahierarkia(parentOids)

    val kayttoOikeushierarkiat = if (organisaatiot.contains(OPH_PAAKAYTTAJA_OID)) {
      koulutustoimijahierarkia
    } else {
      val oppilaitoshierarkia = getOppilaitoshierarkia(parentOids)

      val toimipistehierarkia = getToimipistehierarkia(parentOids)

      koulutustoimijahierarkia concat oppilaitoshierarkia concat toimipistehierarkia
    }
    
    kayttoOikeushierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterExistingOrgs(hierarkia))
  }

  def getToimipistehierarkia(toimipisteet: List[String]): List[OrganisaatioHierarkia] = {
    val hierarkiat = db.run(
      commonRepository.selectToimipisteDescendants(toimipisteet),
      "selectToimipisteDescendants"
    ).toList
    
    hierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterExistingOrgs(hierarkia))
  }

  def getOppilaitoshierarkia(oppilaitokset: List[String]): List[OrganisaatioHierarkia] = {
    val hierarkiat = db.run(
      commonRepository.selectOppilaitosDescendants(oppilaitokset),
      "selectOppilaitosDescendants"
    ).toList
    
    hierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterExistingOrgs(hierarkia))
  }

  def getKoulutustoimijahierarkia(koulutustoimijat: List[String]): List[OrganisaatioHierarkia] = {
    val hierarkiat = db.run(
      commonRepository.selectKoulutustoimijaDescendants(koulutustoimijat),
      "selectKoulutustoimijaDescendants"
    ).toList

    hierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterExistingOrgs(hierarkia))
  }
}
