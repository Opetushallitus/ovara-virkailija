package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.*
import fi.oph.ovara.backend.repository.{CommonRepository, ReadOnlyDatabase}
import fi.oph.ovara.backend.utils.Constants.{KOULUTUSTOIMIJARAPORTTI, OPH_PAAKAYTTAJA_OID, OPPILAITOSRAPORTTI, TOIMIPISTERAPORTTI}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, OrganisaatioUtils}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.{CacheEvict, Cacheable}
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.{Component, Service}
import scala.util.{Try, Failure, Success}

@Component
@Service
class CommonService(commonRepository: CommonRepository, userService: UserService) {
  @Autowired
  val db: ReadOnlyDatabase = null

  @Autowired
  val cacheManager: CacheManager = null

  val LOG: Logger = LoggerFactory.getLogger(classOf[CommonService])

  @Cacheable(value = Array("alkamisvuodet"), key = "#root.methodName")
  def getAlkamisvuodet: Either[String, Vector[String]] = {
    Try {
      db.run(commonRepository.selectDistinctAlkamisvuodet(), "selectDistinctAlkamisvuodet")
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching alkamisvuodet", exception)
        Left("virhe.tietokanta")
    }
  }

  @CacheEvict(value = Array("alkamisvuodet"), allEntries = true)
  @Scheduled(fixedRateString = "${caching.spring.alkamisvuodetTTL}")
  def emptyAlkamisvuodetCache(): Unit = {
    LOG.info("Emptying alkamisvuodet cache")
  }

  def getHaut(alkamiskaudet: List[String], selectedHaut: List[String], haunTyyppi: String): Either[String, Vector[Haku]] = {
    Try {
      db.run(
        commonRepository.selectDistinctExistingHaut(alkamiskaudet, selectedHaut, haunTyyppi),
        "selectDistinctExistingHaut"
      )
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching haut", exception)
        Left("virhe.tietokanta")
    }
  }

  def getHakukohteet(
                      koulutustoimija: Option[String],
                      oppilaitokset: List[String],
                      toimipisteet: List[String],
                      haut: List[String],
                      hakukohderyhmat: List[String],
                      hakukohteet: List[String]
                    ): Either[String, Vector[Hakukohde]] = {
    val user = userService.getEnrichedUserDetails
    val authorities = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)

    val allowedOrgOidsFromSelection =
      getAllowedOrgOidsFromOrgSelection(kayttooikeusOrganisaatiot, oppilaitokset, toimipisteet, koulutustoimija)

    val isOphPaakayttaja = AuthoritiesUtil.hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot)

    val allowedHakukohderyhmaOids = if (isOphPaakayttaja) {
      hakukohderyhmat
    } else {
      kayttooikeusOrganisaatiot intersect hakukohderyhmat
    }

    Try {
      db.run(
        commonRepository
          .selectDistinctExistingHakukohteetWithSelectedOrgsAsJarjestaja(
            allowedOrgOidsFromSelection,
            haut,
            allowedHakukohderyhmaOids,
            hakukohteet
          ),
        "selectDistinctExistingHakukohteetWithSelectedOrgsAsJarjestaja"
      )
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching hakukohteet", exception)
        Left("virhe.tietokanta")
    }
  }

  def getPohjakoulutukset: Either[String, Seq[Koodi]] = {
    Try {
      db.run(commonRepository.selectToisenAsteenPohjakoulutukset, "selectToisenAsteenPohjakoulutukset")
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching pohjakoulutukset", exception)
        Left("virhe.tietokanta")
    }
  }

  def getHarkinnanvaraisuudet: Either[String, Vector[String]] = {
    Try {
      db.run(commonRepository.selectDistinctHarkinnanvaraisuudet(), "selectDistinctHarkinnanvaraisuudet")
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching harkinnanvaraisuudet", exception)
        Left("virhe.tietokanta")
    }
  }

  def getValintatiedot: Either[String, Vector[String]] = {
    Try {
      db.run(commonRepository.selectDistinctValintatiedot, "selectDistinctValintatiedot")
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching valintatiedot", exception)
        Left("virhe.tietokanta")
    }
  }

  def getVastaanottotiedot: Either[String, Vector[String]] = {
    Try {
      db.run(commonRepository.selectDistinctVastaanottotiedot, "selectDistinctVastaanottotiedot")
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching vastaanottotiedot", exception)
        Left("virhe.tietokanta")
    }
  }

  def getOpetuskielet: Either[String, Vector[Koodi]] = {
    Try {
      db.run(commonRepository.selectDistinctOpetuskielet, "selectDistinctOpetuskielet")
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching opetuskielet", exception)
        Left("virhe.tietokanta")
    }
  }

  def getMaakunnat: Either[String, Vector[Koodi]] = {
    Try {
      db.run(commonRepository.selectDistinctMaakunnat, "selectDistinctMaakunnat")
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching maakunnat", exception)
        Left("virhe.tietokanta")
    }
  }

  def getKunnat(maakunnat: List[String], selectedKunnat: List[String]): Either[String, Vector[Koodi]] = {
    Try {
      db.run(commonRepository.selectDistinctKunnat(maakunnat, selectedKunnat), "selectDistinctKunnat")
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching kunnat", exception)
        Left("virhe.tietokanta")
    }
  }

  def getKoulutusalat1: Either[String, Vector[Koodi]] = {
    Try {
      db.run(commonRepository.selectDistinctKoulutusalat1(), "selectDistinctKoulutusalat1")
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching koulutusalat1", exception)
        Left("virhe.tietokanta")
    }
  }

  def getKoulutusalat2(koulutusalat1: List[String], selectedKoulutusalat2: List[String]): Either[String, Vector[Koodi]] = {
    Try {
      db.run(
        commonRepository.selectDistinctKoulutusalat2(koulutusalat1, selectedKoulutusalat2),
        "selectDistinctKoulutusalat2"
      )
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching koulutusalat2", exception)
        Left("virhe.tietokanta")
    }
  }

  def getKoulutusalat3(koulutusalat2: List[String], selectedKoulutusalat3: List[String]): Either[String, Vector[Koodi]] = {
    Try {
      db.run(
        commonRepository.selectDistinctKoulutusalat3(koulutusalat2, selectedKoulutusalat3),
        "selectDistinctKoulutusalat3"
      )
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching koulutusalat3", exception)
        Left("virhe.tietokanta")
    }
  }

  def getOkmOhjauksenAlat: Either[String, Vector[Koodi]] = {
    Try {
      db.run(commonRepository.selectDistinctOkmOhjauksenAlat, "selectDistinctOkmOhjauksenAlat")
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching okm-ohjauksen-alat", exception)
        Left("virhe.tietokanta")
    }
  }

  def getHakukohderyhmat(haut: List[String]): Either[String, Vector[Hakukohderyhma]] = {
    Try {
      val user = userService.getEnrichedUserDetails
      val kayttooikeusOids = AuthoritiesUtil.getKayttooikeusOids(user.authorities)
      val hakukohderyhmaOids =
        if (AuthoritiesUtil.hasOPHPaakayttajaRights(kayttooikeusOids))
          List() // ei rajata listaa pääkäyttäjälle
        else
          kayttooikeusOids
      db.run(commonRepository.selectHakukohderyhmat(hakukohderyhmaOids, haut), "selectHakukohderyhmat")
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching hakukohderyhmat", exception)
        Left("virhe.tietokanta")
    }
  }

  def getOrganisaatioHierarkiatWithUserRights: Either[String, List[OrganisaatioHierarkia]] = {
    Try {
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
        koulutustoimijahierarkia ++ oppilaitoshierarkia ++ toimipistehierarkia
      }

      kayttoOikeushierarkiat.flatMap(hierarkia => OrganisaatioUtils.filterActiveOrgsWithoutPeruskoulu(hierarkia))
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        LOG.error("Error fetching organisaatio hierarkiat with user rights", exception)
        Left("virhe.tietokanta")
    }
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
        val hierarkiat =
          if (AuthoritiesUtil.hasOPHPaakayttajaRights(kayttooikeusOrganisaatioOids)) {
            getKoulutustoimijahierarkia(kayttooikeusOrganisaatioOids)
          } else {
            // ei-pääkäyttäjälle haetaan hierarkiat oikeuksien perusteella
            val toimipisteHierarkia = getToimipistehierarkiat(kayttooikeusOrganisaatioOids)
            val oppilaitosHierarkia = getOppilaitoshierarkiat(kayttooikeusOrganisaatioOids)
            val koulutustoimijaHierarkia = getKoulutustoimijahierarkia(kayttooikeusOrganisaatioOids)
            toimipisteHierarkia ++ oppilaitosHierarkia ++ koulutustoimijaHierarkia
          }

        (hierarkiat, KOULUTUSTOIMIJARAPORTTI)
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
        getOrganisaatioHierarkiatWithUserRights match {
          case Right(hierarkiat) => hierarkiat
          case Left(error) =>
            throw new RuntimeException(error)
        }
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
