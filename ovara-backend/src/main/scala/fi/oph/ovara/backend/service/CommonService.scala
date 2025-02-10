package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.{Haku, Hakukohde, Organisaatio, OrganisaatioHierarkia}
import fi.oph.ovara.backend.repository.{CommonRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.Constants.{
  KOULUTUSTOIMIJARAPORTTI,
  OPH_PAAKAYTTAJA_OID,
  OPPILAITOSRAPORTTI,
  TOIMIPISTERAPORTTI
}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, OrganisaatioUtils}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CommonService(commonRepository: CommonRepository, userService: UserService) {
  @Autowired
  val db: OvaraDatabase = null

  def getAlkamisvuodet: Vector[String] = {
    db.run(commonRepository.selectDistinctAlkamisvuodet(), "selectDistinctAlkamisvuodet")
  }

  def getHaut(alkamiskaudet: List[String]): Vector[Haku] = {
    db.run(commonRepository.selectDistinctExistingHaut(alkamiskaudet), "selectDistinctExistingHaut")
  }

  def getHakukohteet(oppilaitokset: List[String], toimipisteet: List[String], haut: List[String]): Vector[Hakukohde] = {
    val user                      = userService.getEnrichedUserDetails
    val authorities               = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getOrganisaatiot(authorities)

    val allowedOrgOidsFromSelection =
      getAllowedOrgsFromOrgSelection(kayttooikeusOrganisaatiot, oppilaitokset, toimipisteet)

    if (allowedOrgOidsFromSelection.nonEmpty) {
      db.run(
        commonRepository
          .selectDistinctExistingHakukohteetWithSelectedOrgsAsJarjestaja(allowedOrgOidsFromSelection, haut),
        "selectDistinctExistingHakukohteet"
      )
    } else {
      Vector()
    }
  }

  def getHarkinnanvaraisuudet: Vector[String] = {
    db.run(commonRepository.selectDistinctHarkinnanvaraisuudet(), "selectDistinctHarkinnanvaraisuudet")
  }

  def getValintatiedot: Vector[String] = {
    db.run(commonRepository.selectDistinctValintatiedot, "selectDistinctValintatiedot")
  }

  def getVastaanottotiedot: Vector[String] = {
    db.run(commonRepository.selectDistinctVastaanottotiedot, "selectDistinctVastaanottotiedot")
  }

  def getOrganisaatioHierarkiatWithUserRights: List[OrganisaatioHierarkia] = {
    val user          = userService.getEnrichedUserDetails
    val organisaatiot = AuthoritiesUtil.getOrganisaatiot(user.authorities)

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
      val oppilaitoshierarkia = getOppilaitoshierarkiat(parentOids)

      val toimipistehierarkia = getToimipistehierarkiat(parentOids)

      koulutustoimijahierarkia concat oppilaitoshierarkia concat toimipistehierarkia
    }

    kayttoOikeushierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterExistingOrgs(hierarkia))
  }

  def getToimipistehierarkiat(toimipisteet: List[String]): List[OrganisaatioHierarkia] = {
    val hierarkiat = db
      .run(
        commonRepository.selectToimipisteDescendants(toimipisteet),
        "selectToimipisteDescendants"
      )
      .toList

    hierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterExistingOrgs(hierarkia))
  }

  def getOppilaitoshierarkiat(oppilaitokset: List[String]): List[OrganisaatioHierarkia] = {
    val hierarkiat = db
      .run(
        commonRepository.selectOppilaitosDescendants(oppilaitokset),
        "selectOppilaitosDescendants"
      )
      .toList

    hierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterExistingOrgs(hierarkia))
  }

  def getKoulutustoimijahierarkia(koulutustoimijat: List[String]): List[OrganisaatioHierarkia] = {
    val hierarkiat = db
      .run(
        commonRepository.selectKoulutustoimijaDescendants(koulutustoimijat),
        "selectKoulutustoimijaDescendants"
      )
      .toList

    hierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterExistingOrgs(hierarkia))
  }

  def getDistinctKoulutustoimijat(organisaatioOids: List[String]): List[Organisaatio] = {
    db.run(
      commonRepository.selectDistinctKoulutustoimijat(organisaatioOids),
      "selectDistinctKoulutustoimijat"
    ).toList
  }

  private def hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot: List[String]) = {
    kayttooikeusOrganisaatiot.contains(OPH_PAAKAYTTAJA_OID)
  }

  def getAllowedOrgsFromOrgSelection(
      kayttooikeusOrganisaatioOids: List[String],
      koulutustoimijaOid: Option[String],
      toimipisteOids: List[String],
      oppilaitosOids: List[String]
  ): (List[String], List[OrganisaatioHierarkia], String) = {

    def enrichHierarkiatWithKoulutustoimijaParent(oppilaitoshierarkiat: List[OrganisaatioHierarkia]) = {
      for (hierarkia <- oppilaitoshierarkiat) yield {
        val parentOids            = hierarkia.parent_oids
        val parentKoulutustoimija = getDistinctKoulutustoimijat(parentOids).headOption
        OrganisaatioUtils.addKoulutustoimijaParentToHierarkiaDescendants(hierarkia, parentKoulutustoimija)
      }
    }

    val (hierarkiat, raporttityyppi) =
      if (toimipisteOids.nonEmpty) {
        val toimipistehierarkiat = getToimipistehierarkiat(toimipisteOids)
        (enrichHierarkiatWithKoulutustoimijaParent(toimipistehierarkiat), TOIMIPISTERAPORTTI)
      } else if (oppilaitosOids.nonEmpty) {
        val oppilaitoshierarkiat = getOppilaitoshierarkiat(oppilaitosOids)
        (enrichHierarkiatWithKoulutustoimijaParent(oppilaitoshierarkiat), OPPILAITOSRAPORTTI)
      } else if (koulutustoimijaOid.nonEmpty) {
        val hierarkiat = koulutustoimijaOid match {
          case Some(koulutustoimija) =>
            getKoulutustoimijahierarkia(List(koulutustoimija))
          case None => List()
        }
        (hierarkiat, KOULUTUSTOIMIJARAPORTTI)
      } else {
        val koulutustoimijahierarkia = getKoulutustoimijahierarkia(kayttooikeusOrganisaatioOids)

        val hierarkiat = if (hasOPHPaakayttajaRights(kayttooikeusOrganisaatioOids)) {
          koulutustoimijahierarkia
        } else {
          val oppilaitoshierarkia = getOppilaitoshierarkiat(oppilaitosOids)

          val toimipistehierarkia = getToimipistehierarkiat(toimipisteOids)

          koulutustoimijahierarkia concat oppilaitoshierarkia concat toimipistehierarkia
        }
        (hierarkiat, "koulutustoimijaraportti")
      }

    val hierarkiatWithExistingOrgs = OrganisaatioUtils.filterExistingOrgs(hierarkiat)

    val selectedOrgsDescendantOids =
      hierarkiatWithExistingOrgs.flatMap(hierarkia => OrganisaatioUtils.getDescendantOids(hierarkia)).distinct

    if (hasOPHPaakayttajaRights(kayttooikeusOrganisaatioOids)) {
      (selectedOrgsDescendantOids, hierarkiatWithExistingOrgs, raporttityyppi)
    } else {
      val childKayttooikeusOrgs = hierarkiatWithExistingOrgs.flatMap(hierarkia =>
        OrganisaatioUtils.getKayttooikeusDescendantAndSelfOids(hierarkia, kayttooikeusOrganisaatioOids)
      )
      (
        (childKayttooikeusOrgs intersect selectedOrgsDescendantOids).distinct,
        hierarkiatWithExistingOrgs,
        raporttityyppi
      )
    }
  }

  def getAllowedOrgsFromOrgSelection(
      kayttooikeusOrganisaatioOids: List[String],
      oppilaitosOids: List[String],
      toimipisteOids: List[String]
  ): List[String] = {
    val hierarkiat =
      if (toimipisteOids.nonEmpty) {
        getToimipistehierarkiat(toimipisteOids)
      } else if (oppilaitosOids.nonEmpty) {
        getOppilaitoshierarkiat(oppilaitosOids)
      } else {
        List()
      }

    val hierarkiatWithExistingOrgs = OrganisaatioUtils.filterExistingOrgs(hierarkiat)

    val selectedOrgsDescendantOids =
      hierarkiatWithExistingOrgs.flatMap(hierarkia => OrganisaatioUtils.getDescendantOids(hierarkia)).distinct

    val childKayttooikeusOrgs = hierarkiatWithExistingOrgs.flatMap(hierarkia =>
      OrganisaatioUtils.getKayttooikeusDescendantAndSelfOids(hierarkia, kayttooikeusOrganisaatioOids)
    )

    (childKayttooikeusOrgs intersect selectedOrgsDescendantOids).distinct
  }
}
