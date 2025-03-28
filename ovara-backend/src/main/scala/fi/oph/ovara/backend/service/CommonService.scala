package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.*
import fi.oph.ovara.backend.repository.{CommonRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.Constants.{
  KOULUTUSTOIMIJARAPORTTI,
  OPH_PAAKAYTTAJA_OID,
  OPPILAITOSRAPORTTI,
  TOIMIPISTERAPORTTI
}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, OrganisaatioUtils}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.{CacheEvict, Cacheable}
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.{Component, Service}

@Component
@Service
class CommonService(commonRepository: CommonRepository, userService: UserService) {
  @Autowired
  val db: OvaraDatabase = null

  @Autowired
  val cacheManager: CacheManager = null

  val LOG: Logger = LoggerFactory.getLogger(classOf[LokalisointiService])

  @Cacheable(value = Array("alkamisvuodet"), key = "#root.methodName")
  def getAlkamisvuodet: Vector[String] = {
    db.run(commonRepository.selectDistinctAlkamisvuodet(), "selectDistinctAlkamisvuodet")
  }

  @CacheEvict(value = Array("alkamisvuodet"), allEntries = true)
  @Scheduled(fixedRateString = "${caching.spring.alkamisvuodetTTL}")
  def emptyAlkamisvuodetCache(): Unit = {
    LOG.info("Emptying alkamisvuodet cache")
  }

  def getHaut(alkamiskaudet: List[String], selectedHaut: List[String], haunTyyppi: String): Vector[Haku] = {
    db.run(commonRepository.selectDistinctExistingHaut(alkamiskaudet, selectedHaut, haunTyyppi), "selectDistinctExistingHaut")
  }

  def getHakukohteet(
                      oppilaitokset: List[String],
                      toimipisteet: List[String],
                      haut: List[String],
                      hakukohderyhmat: List[String]
                    ): Vector[Hakukohde] = {
    val user = userService.getEnrichedUserDetails
    val authorities = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)

    val allowedOrgOidsFromSelection =
      getAllowedOrgOidsFromOrgSelection(kayttooikeusOrganisaatiot, oppilaitokset, toimipisteet)

    if (allowedOrgOidsFromSelection.nonEmpty || hakukohderyhmat.nonEmpty) {
      db.run(
        commonRepository
          .selectDistinctExistingHakukohteetWithSelectedOrgsAsJarjestaja(
            allowedOrgOidsFromSelection,
            haut,
            hakukohderyhmat
          ),
        "selectDistinctExistingHakukohteetWithSelectedOrgsAsJarjestaja"
      )
    } else {
      Vector()
    }
  }

  def getPohjakoulutukset: Seq[Koodi] = {
    db.run(commonRepository.selectToisenAsteenPohjakoulutukset, "selectToisenAsteenPohjakoulutukset")
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

  def getOpetuskielet: Vector[Koodi] = {
    db.run(commonRepository.selectDistinctOpetuskielet, "selectDistinctOpetuskielet")
  }

  def getMaakunnat: Vector[Koodi] = {
    db.run(commonRepository.selectDistinctMaakunnat, "selectDistinctMaakunnat")
  }

  def getKunnat(maakunnat: List[String], selectedKunnat: List[String]): Vector[Koodi] = {
    db.run(commonRepository.selectDistinctKunnat(maakunnat, selectedKunnat), "selectDistinctKunnat")
  }

  def getKoulutusalat1: Vector[Koodi] = {
    db.run(commonRepository.selectDistinctKoulutusalat1(), "selectDistinctKoulutusalat1")
  }

  def getKoulutusalat2(koulutusalat1: List[String], selectedKoulutusalat2: List[String]): Vector[Koodi] = {
    db.run(commonRepository.selectDistinctKoulutusalat2(koulutusalat1, selectedKoulutusalat2), "selectDistinctKoulutusalat2")
  }

  def getKoulutusalat3(koulutusalat2: List[String], selectedKoulutusalat3: List[String]): Vector[Koodi] = {
    db.run(commonRepository.selectDistinctKoulutusalat3(koulutusalat2, selectedKoulutusalat3), "selectDistinctKoulutusalat3")
  }

  def getOkmOhjauksenAlat: Vector[Koodi] = {
    db.run(commonRepository.selectDistinctOkmOhjauksenAlat, "selectDistinctOkmOhjauksenAlat")
  }

  def getHakukohderyhmat(haut: List[String]): Vector[Hakukohderyhma] = {
    val user = userService.getEnrichedUserDetails
    val kayttooikeusOids = AuthoritiesUtil.getKayttooikeusOids(user.authorities)
    val hakukohderyhmaOids =
      if (AuthoritiesUtil.hasOPHPaakayttajaRights(kayttooikeusOids))
        List() // ei rajata listaa pääkäyttäjälle
      else
        kayttooikeusOids
    db.run(commonRepository.selectHakukohderyhmat(hakukohderyhmaOids, haut), "selectHakukohderyhmat")
  }

  def getOrganisaatioHierarkiatWithUserRights: List[OrganisaatioHierarkia] = {
    val user = userService.getEnrichedUserDetails
    val organisaatiot = AuthoritiesUtil.getKayttooikeusOids(user.authorities)

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

    kayttoOikeushierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterActiveOrgsWithoutPeruskoulu(hierarkia))
  }

  def getToimipistehierarkiat(toimipisteet: List[String]): List[OrganisaatioHierarkia] = {
    if (toimipisteet.isEmpty) {
      List()
    } else {
      val hierarkiat = db
        .run(
          commonRepository.selectToimipisteDescendants(toimipisteet),
          "selectToimipisteDescendants"
        )
        .toList

      hierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterActiveOrgsWithoutPeruskoulu(hierarkia))
    }
  }

  def getOppilaitoshierarkiat(oppilaitokset: List[String]): List[OrganisaatioHierarkia] = {
    if (oppilaitokset.isEmpty) {
      List()
    } else {
      val hierarkiat = db
        .run(
          commonRepository.selectOppilaitosDescendants(oppilaitokset),
          "selectOppilaitosDescendants"
        )
        .toList

      hierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterActiveOrgsWithoutPeruskoulu(hierarkia))
    }
  }

  def getKoulutustoimijahierarkia(koulutustoimijat: List[String]): List[OrganisaatioHierarkia] = {
    if (koulutustoimijat.isEmpty) {
      List()
    } else {
      val hierarkiat = db
        .run(
          commonRepository.selectKoulutustoimijaDescendants(koulutustoimijat),
          "selectKoulutustoimijaDescendants"
        )
        .toList

      hierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterActiveOrgsWithoutPeruskoulu(hierarkia))
    }
  }

  def getDistinctKoulutustoimijat(organisaatioOids: List[String]): List[Organisaatio] = {
    db.run(
      commonRepository.selectDistinctKoulutustoimijat(organisaatioOids),
      "selectDistinctKoulutustoimijat"
    ).toList
  }

  def getAllowedOrgsFromOrgSelection(
                                      kayttooikeusOrganisaatioOids: List[String],
                                      koulutustoimijaOid: Option[String],
                                      toimipisteOids: List[String],
                                      oppilaitosOids: List[String]
                                    ): (List[String], List[OrganisaatioHierarkia], String) = {

    def enrichHierarkiatWithKoulutustoimijaParent(oppilaitoshierarkiat: List[OrganisaatioHierarkia]) = {
      for (hierarkia <- oppilaitoshierarkiat) yield {
        val parentOids = hierarkia.parent_oids
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

        val hierarkiat = if (AuthoritiesUtil.hasOPHPaakayttajaRights(kayttooikeusOrganisaatioOids)) {
          koulutustoimijahierarkia
        } else {
          val oppilaitoshierarkia = getOppilaitoshierarkiat(oppilaitosOids)

          val toimipistehierarkia = getToimipistehierarkiat(toimipisteOids)

          koulutustoimijahierarkia concat oppilaitoshierarkia concat toimipistehierarkia
        }
        (hierarkiat, "koulutustoimijaraportti")
      }

    val hierarkiatWithExistingOrgs = OrganisaatioUtils.filterOnlyWantedOrgs(hierarkiat)

    val selectedOrgsDescendantOids =
      hierarkiatWithExistingOrgs.flatMap(hierarkia => OrganisaatioUtils.getDescendantOids(hierarkia)).distinct

    if (AuthoritiesUtil.hasOPHPaakayttajaRights(kayttooikeusOrganisaatioOids)) {
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

  def getAllowedOrgOidsFromOrgSelection(
                                         kayttooikeusOrganisaatioOids: List[String],
                                         oppilaitosOids: List[String],
                                         toimipisteOids: List[String],
                                         koulutustoimijaOid: Option[String] = None
                                       ): List[String] = {
    val hierarkiat =
      if (toimipisteOids.nonEmpty) {
        getToimipistehierarkiat(toimipisteOids)
      } else if (oppilaitosOids.nonEmpty) {
        getOppilaitoshierarkiat(oppilaitosOids)
      } else if (koulutustoimijaOid.isDefined) {
        getKoulutustoimijahierarkia(List(koulutustoimijaOid.get))
      } else {
        List()
      }

    val hierarkiatWithExistingOrgs = OrganisaatioUtils.filterOnlyWantedOrgs(hierarkiat)

    val selectedOrgsDescendantOids =
      hierarkiatWithExistingOrgs.flatMap(hierarkia => OrganisaatioUtils.getDescendantOids(hierarkia)).distinct

    val childKayttooikeusOrgs = hierarkiatWithExistingOrgs.flatMap(hierarkia =>
      OrganisaatioUtils.getKayttooikeusDescendantAndSelfOids(hierarkia, kayttooikeusOrganisaatioOids)
    )

    (childKayttooikeusOrgs intersect selectedOrgsDescendantOids).distinct
  }
}
