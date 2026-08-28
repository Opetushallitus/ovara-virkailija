package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.service.CommonService
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Service

import java.time.format.DateTimeFormatter
import scala.util.Try

@Service
class ExternalKKHakijatService(repository: ExternalKKHakijatRepository, commonService: CommonService) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ExternalKKHakijatService])

  private val AikaleimaFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

  def getKKHakijat(
    hakuOid: Option[String],
    hakukohdeOid: Option[String],
    organisaatioOid: Option[String],
    valintarajaus: Valintarajaus,
    scope: KayttooikeusScopeKK,
    hakukohderyhmaOid: Option[String] = None,
    oppijanumero: Option[String] = None
  ): Either[String, Seq[KKHakija]] = {
    Try {
      // Sekä rajapinnan organisaatioOid (rajaava parametri) että käyttäjän organisaatio-oikeudet
      // vertaillaan hakukohteen jarjestyspaikka_oidia vasten, joka on tyypillisesti toimipiste.
      // Molemmat on siis laajennettava lapsiorganisaatioihin, jotta koulutustoimija- tai
      // oppilaitostason valinta ja oikeus osuvat alempana oleviin järjestyspaikkoihin.
      // Hakukohderyhmäoikeudet eivät kulje tätä kautta, vaan ne laajennetaan hakukohde-oideiksi.
      val organisaatioOids =
        organisaatioOid.map(oid => commonService.getOrganisaatioidenJaLastenOidit(List(oid))).getOrElse(Nil)

      // Ryhmäoikeudet huomioidaan vain kun pyynnössä ei ole organisaatioOid-rajainta:
      // organisaatiorajaus on katettava käyttäjän organisaatio-oikeuksilla.
      val huomioiRyhmaoikeudet = !scope.isPaakayttaja && organisaatioOid.isEmpty
      val kayttooikeusryhmat   = if (huomioiRyhmaoikeudet) scope.allowedHakukohderyhmaOids else Set.empty[String]

      // Sama hakukohderyhmä voi esiintyä sekä rajaimena että käyttöoikeutena, joten laajennus
      // haetaan kannasta kertaalleen per ryhmä. Laajennus tapahtuu haun sisällä, joten se on
      // mahdollista vain kun hakuOid tiedetään: oppijanumerohaussa hakua ei tarvitse antaa, ja
      // validointi estää hakukohderyhmaOidin ilman hakuOidia.
      val ryhmanHakukohteet: Map[String, Set[String]] =
        hakuOid.fold(Map.empty[String, Set[String]]) { haku =>
          (hakukohderyhmaOid.toSet ++ kayttooikeusryhmat)
            .map(ryhma => ryhma -> commonService.getHakukohderyhmanHakukohdeOids(ryhma, haku).toSet)
            .toMap
        }

      val kayttooikeusHakukohdeOids = kayttooikeusryhmat.flatMap(ryhmanHakukohteet)
      if (kayttooikeusryhmat.nonEmpty) {
        LOG.debug(
          s"Käyttäjän ${kayttooikeusryhmat.size} hakukohderyhmäoikeutta laajeni " +
            s"${kayttooikeusHakukohdeOids.size} hakukohteeksi haussa $hakuOid"
        )
      }

      // copy eikä limited: limited rakentaisi uuden scopen ja pudottaisi ryhmäoikeudet pois.
      val expandedScope =
        if (scope.isPaakayttaja) scope
        else
          scope.copy(
            allowedOrgOids = commonService.getOrganisaatioidenJaLastenOidit(scope.allowedOrgOids.toList).toSet,
            allowedHakukohdeOidsFromHakukohderyhmat = kayttooikeusHakukohdeOids
          )

      val rajaaHakukohteilla = hakukohdeOid.isDefined || hakukohderyhmaOid.isDefined
      val hakukohdeRajaus    = resolveHakukohdeRajaus(hakukohdeOid, hakukohderyhmaOid, ryhmanHakukohteet)

      // Käyttäjän oikeuksia ei yhdistetä hakukohdeRajaukseen eikä viedä kyselyyn: kyselyn
      // hakukohde- ja organisaatiolistat JA:taan keskenään (ks. hakuFilterSqlFragment) ja
      // rajaaHakukohteilla kertoo nimenomaan pyynnön rajaimista. Rivikohtainen oikeustarkistus
      // tehdään matchesFiltersissä.
      //
      // Jos rajaimia annettiin mutta leikkaus on tyhjä, yksikään hakukohde ei täsmää. Kyselyä ei
      // saa ajaa: tyhjä hakukohdelista jättäisi voimaan vain organisaatiorajauksen ja laajentaisi
      // tuloksen koko organisaatioon.
      // saaKaikkiTiedot eikä isPaakayttaja: täyden oikeuden käyttäjällä ei välttämättäole organisaatio-
      // eikä ryhmäoikeuksia, joten hän näyttäisi muuten oikeudettomalta ja saisi tyhjän tuloksen.
      val eiOikeuksiaPyynnonRajaimiin =
        !scope.saaKaikkiTiedot && expandedScope.allowedOrgOids.isEmpty && expandedScope.allowedHakukohdeOidsFromHakukohderyhmat.isEmpty
      val kkHakijaRows =
        if (eiOikeuksiaPyynnonRajaimiin) {
          LOG.info("Käyttäjän oikeudet eivät kata pyynnön rajaimia, palautetaan tyhjä tulos")
          Seq.empty
        } else if (rajaaHakukohteilla && hakukohdeRajaus.isEmpty) Seq.empty
        else
          repository.selectKKHakijat(hakuOid, hakukohdeRajaus.toSeq, organisaatioOids, valintarajaus, oppijanumero)

      if (kkHakijaRows.isEmpty) {
        Nil
      } else {
        val hakemusOids          = kkHakijaRows.map(_.hakemusOid).toSet
        val hakemusRows          = repository.selectHakemukset(hakemusOids)
        val ylioppilaatByHakemus =
          repository.selectYlioppilaat(hakemusOids).map(y => y.hakemusOid -> y).toMap
        val hakemuksetByOid = hakemusRows.groupBy(_.hakemusOid)

        kkHakijaRows.flatMap { row =>
          val matchingRows = hakemuksetByOid
            .getOrElse(row.hakemusOid, Seq.empty)
            .filter(matchesFilters(_, hakukohdeRajaus, organisaatioOids, valintarajaus, expandedScope))
          if (matchingRows.isEmpty) None
          else {
            val hakemukset = matchingRows.map(mRow =>
              mRow.asKKHakemus(
                hakuOid = row.hakuOid,
                hakuVuosi = row.vuosi.getOrElse(0),
                hakuKausi = row.kausi.getOrElse(""),
                hakemusnumero = row.hakemusOid,
                hakemusJattoAikaleima = row.jatetty.map(_.format(AikaleimaFormatter)),
                hakemusViimeinenMuokkausAikaleima = row.muokattu.map(_.format(AikaleimaFormatter)),
                valinnanAikaleima = mRow.valinnanAikaleima.map(_.format(AikaleimaFormatter)),
                pohjakoulutus = row.pohjakoulutus,
                julkaisulupa = row.valintatuloksenJulkaisulupa
              )
            )
            Some(row.asHakija(hakemukset, ylioppilaatByHakemus.get(row.hakemusOid)))
          }
        }
      }
    }.toEither.left
      .map { exception =>
        LOG.error("Error fetching kk hakijat", exception)
        "virhe.tietokanta"
      }
  }

  /**
   * Leikkaa hakukohde- ja hakukohderyhmäparametrit yhdeksi joukoksi hakukohde-oideja:
   * hakukohde JA hakukohderyhmä yhdessä tarkoittavat "tämä hakukohde, jos se kuuluu ryhmään".
   * Hakukohderyhmä laajennetaan sen hakukohde-oideiksi (`ryhmanHakukohteet`, ks. kutsuja).
   *
   * Tyhjä joukko tarkoittaa joko ettei rajaimia annettu tai ettei leikkaus osu mihinkään --
   * kutsuja erottaa nämä parametreista (ks. rajaaHakukohteilla).
   */
  private def resolveHakukohdeRajaus(
    hakukohdeOid: Option[String],
    hakukohderyhmaOid: Option[String],
    ryhmanHakukohteet: Map[String, Set[String]]
  ): Set[String] = {
    val rajaavanRyhmanHakukohteet =
      hakukohderyhmaOid.map(ryhma => ryhmanHakukohteet.getOrElse(ryhma, Set.empty))

    List(hakukohdeOid.map(Set(_)), rajaavanRyhmanHakukohteet).flatten
      .reduceOption(_ intersect _)
      .getOrElse(Set.empty)
  }

  private def matchesFilters(
    row: KKHakemusRow,
    hakukohdeOids: Set[String],    // Hakukohde- ja hakukohderyhmäparametreista leikattu
    organisaatioOids: Seq[String], // Rajapinnan parametri lapsiorganisaatioineen
    valintarajaus: Valintarajaus,
    scope: KayttooikeusScopeKK // Käyttäjän oikeudet: organisaatiot lapsineen ja ryhmien hakukohteet
  ): Boolean = {
    // selectHakemukset hakee kaikki osumien hakemusten hakutoiveet, joten sama rajaus on
    // toistettava tässä -- muuten ryhmän ulkopuoliset hakutoiveet palaisivat mukaan. Tyhjä
    // rajausjoukko tarkoittaa tässä aina "ei rajausta": tyhjän leikkauksen tapaus on karsittu jo
    // kutsujassa. Huomaa että käyttöoikeusjoukoissa (allowedOrgOids, allowedHakukohdeOidsFromHakukohderyhmat)
    // tyhjyys tarkoittaa päinvastaista: ei oikeuksia, ei osumia.
    val hakukohdeMatch = hakukohdeOids.isEmpty || hakukohdeOids.contains(row.hakukohdeOid)
    val orgMatch       = organisaatioOids.isEmpty || row.jarjestyspaikkaOid.exists(organisaatioOids.contains)
    // Organisaatio-oikeus TAI hakukohderyhmäoikeus riittää.
    val allowedMatch = scope.saaKaikkiTiedot ||
      row.jarjestyspaikkaOid.exists(scope.allowedOrgOids.contains) ||
      scope.allowedHakukohdeOidsFromHakukohderyhmat.contains(row.hakukohdeOid)
    val stateMatch = valintarajaus match {
      case Valintarajaus.HAKENEET   => true
      case Valintarajaus.HYVAKSYTYT =>
        row.valintatieto.exists(HyvaksytytValintatiedot.contains)
      case Valintarajaus.VASTAANOTTANEET =>
        row.vastaanottotieto.contains(
          "VASTAANOTTANUT_SITOVASTI"
        ) // Todo, otetaanko myös VASTAANOTTANUT_EHDOLLISESTI mukaan?
    }
    hakukohdeMatch && orgMatch && allowedMatch && stateMatch
  }

  private val HyvaksytytValintatiedot: Set[String] =
    Set("HYVAKSYTTY", "HARKINNANVARAISESTI_HYVAKSYTTY", "VARASIJALTA_HYVAKSYTTY")
}
