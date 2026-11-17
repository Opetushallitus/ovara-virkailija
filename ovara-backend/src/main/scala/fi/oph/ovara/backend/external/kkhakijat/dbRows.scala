package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.utils.Constants.HELSINKI_TIMEZONE
import org.slf4j.{Logger, LoggerFactory}

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, OffsetDateTime, ZonedDateTime}

private val LOG: Logger =
  LoggerFactory.getLogger("fi.oph.ovara.backend.external.kkhakijat.Kasittelymerkinnat")

// Suoritusrekisterin kkhakijat-rajapinnan muoto.
private val SyntymaaikaFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

private val KkSyntymaaikaFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyy")

/**
 * Suoritusrekisterin (KkHakijaUtil.toKkSyntymaaika) hetun korvike hakijalle, jolla ei ole hetua:
 *  syntymäaika muodossa ddMMyy ja vuosisatamerkki. Yksilönumero ja tarkiste puuttuvat.
 */
private def toKkSyntymaaika(syntymaaika: LocalDate): String =
  syntymaaika.format(KkSyntymaaikaFormatter) + (syntymaaika.getYear match {
    case y if y >= 2000 => "A"
    case y if y >= 1900 => "-"
    case _              => ""
  })

// Suoritusrekisterin tapaan kotikunta on kolminumeroinen kuntakoodi, muuten "999" (tuntematon).
private def kotikuntaTaiTuntematon(kotikunta: Option[String]): String =
  kotikunta.filter(k => k.length == 3 && k.forall(_.isDigit)).getOrElse("999")

/**
 * Suoritusrekisterin (KkHakijaUtil.getLasnaolot) tapaan ilmoittautumistila lukuvuoden syys- ja
 *  kevätlukukaudelle koulutuksen alkamiskauden mukaan. Ilman alkamiskautta ja -vuotta ei
 *  ilmoittautumisia. Kevätaloituksen lukuvuosi (v, v) on kopioitu Suresta sellaisenaan.
 */
private def ilmoittautumiset(
  alkamiskausi: Option[String],
  alkamisvuosi: Option[Int],
  ilmoittautumisenTila: Option[String]
): Seq[Ilmoittautuminen] = {
  import IlmoittautumisenTila.*
  (alkamiskausi, alkamisvuosi) match {
    case (Some(kausi @ ("S" | "K")), Some(vuosi)) =>
      val syksy = Ilmoittautuminen(_, "S", vuosi)
      val kevat = Ilmoittautuminen(_, "K", if (kausi == "S") vuosi + 1 else vuosi)
      ilmoittautumisenTila.map(Lasnaolo.parse) match {
        case None | Some(Some(Lasnaolo.EI_TEHTY | Lasnaolo.EI_ILMOITTAUTUNUT)) => Seq(syksy(Puuttuu), kevat(Puuttuu))
        case Some(Some(Lasnaolo.LASNA_KOKO_LUKUVUOSI))                         => Seq(syksy(Lasna), kevat(Lasna))
        case Some(Some(Lasnaolo.POISSA_KOKO_LUKUVUOSI))                        => Seq(syksy(Poissa), kevat(Poissa))
        case Some(Some(Lasnaolo.LASNA_SYKSY))                                  => Seq(syksy(Lasna), kevat(Poissa))
        case Some(Some(Lasnaolo.POISSA_SYKSY))                                 => Seq(syksy(Poissa), kevat(Lasna))
        case Some(Some(Lasnaolo.LASNA))                                        => Seq(kevat(Lasna))
        case Some(Some(Lasnaolo.POISSA))                                       => Seq(kevat(Poissa))
        case Some(None)                                                        => Nil
      }
    case _ => Nil
  }
}

// Hakutoiveen valinnan ja vastaanoton tila sekä se, onko tulos julkaistu.
private[kkhakijat] case class VtsTila(valinnanTila: Valintatila, vastaanottotila: Vastaanottotila, julkaistu: Boolean) {
  // Hakutoiveentulos.toKesken: tilat keskeneräisiksi ja tulos julkaisemattomaksi (piilottaa pisteet ja ehdollisuuden).
  def toKesken: VtsTila = VtsTila(Valintatila.KESKEN, Vastaanottotila.KESKEN, julkaistu = false)
}

// Haun ohjausparametrit, joilla valinta-tulos-service laskee valinnan ja vastaanoton tilat.
case class HaunOhjausparametrit(
  vastaanottoPaattyy: Option[OffsetDateTime],           // PH_OPVP
  hakijakohtainenVastaanottoaikaPaivia: Option[Int],    // PH_HPVOA
  valintaesitysHyvaksyttavissa: Option[OffsetDateTime], // PH_VEH
  varasijasaannotVoimaan: Option[OffsetDateTime],       // PH_VSSAV
  kaikkiJonotSijoittelussa: Option[OffsetDateTime],     // PH_VTSSV
  sijoittelu: Boolean,                                  // sijoittelu
  jarjestetytHakutoiveet: Boolean                       // jarjestetytHakutoiveet
) {
  // VastaanottoUtils.ehdollinenVastaanottoMahdollista: puuttuva parametri tulkitaan voimassa olevaksi.
  def varasijasaannotVoimassa(nyt: ZonedDateTime): Boolean =
    varasijasaannotVoimaan.forall(_.toInstant.isBefore(nyt.toInstant)) &&
      kaikkiJonotSijoittelussa.forall(_.toInstant.isBefore(nyt.toInstant))

  // Haku.sijoitteluJaPriorisointi: hakutoiveiden väliset säännöt ovat käytössä vain tällöin.
  def sijoitteluJaPriorisointi: Boolean = sijoittelu && jarjestetytHakutoiveet
}

/**
 * Vastaanoton määräaika valinta-tulos-servicen tapaan (VastaanottoUtils.laskeVastaanottoDeadline):
 *  haun määräaika (PH_OPVP) tai sitä myöhäisempi hakijakohtainen määräaika, joka lasketaan
 *  hyväksymisen julkaisusta (tai sitä myöhäisemmästä PH_VEH:stä) lisäämällä PH_HPVOA päivää ja
 *  asettamalla kellonajaksi haun määräajan kellonaika. Päivä lisätään VTS:n tapaan lähtöhetken omassa
 *  ajassa: julkaisuhetkelle kiinteällä aikaerolla (ilman kesäaikasiirtymää), PH_VEH:lle Suomen ajassa.
 *  Jos julkaisun kellonaika on myöhäisempi kuin haun määräajan, lisätään yksi päivä.
 */
private[kkhakijat] def vastaanottoDeadline(
  parametrit: HaunOhjausparametrit,
  hyvaksyttyJaJulkaistu: Option[OffsetDateTime]
): Option[ZonedDateTime] =
  parametrit.vastaanottoPaattyy.map(_.atZoneSameInstant(HELSINKI_TIMEZONE)).map { haunMaaraaika =>
    val hakijakohtainen = for {
      julkaistu <- hyvaksyttyJaJulkaistu.map(_.atZoneSameInstant(HELSINKI_TIMEZONE).toOffsetDateTime)
      paivia    <- parametrit.hakijakohtainenVastaanottoaikaPaivia
    } yield {
      val valintaesitys = parametrit.valintaesitysHyvaksyttavissa
        .map(_.atZoneSameInstant(HELSINKI_TIMEZONE))
        .getOrElse(Instant.EPOCH.atZone(HELSINKI_TIMEZONE))
      val julkaisuMyohemminPaivalla = julkaistu.toLocalTime
        .truncatedTo(ChronoUnit.SECONDS)
        .isAfter(haunMaaraaika.toLocalTime.truncatedTo(ChronoUnit.SECONDS))
      val lisapaivat = paivia + (if (julkaisuMyohemminPaivalla) 1 else 0)
      val siirretty  =
        if (julkaistu.toInstant.isAfter(valintaesitys.toInstant))
          julkaistu.plusDays(lisapaivat).atZoneSameInstant(HELSINKI_TIMEZONE)
        else valintaesitys.plusDays(lisapaivat)
      siirretty.`with`(haunMaaraaika.toLocalTime)
    }
    hakijakohtainen.filter(_.toInstant.isAfter(haunMaaraaika.toInstant)).getOrElse(haunMaaraaika)
  }

/**
 * Yhden hakutoiveen valinnan ja vastaanoton tila valinta-tulos-servicen tapaan, josta Sure ne saa
 *  (SijoittelutulosService.hakemuksenKevytYhteenveto: jononValintatila, laskeVastaanottotila,
 *  vastaanottotilanVaikutusValintatilaan ja Hakutoiveentulos.julkaistavaVersio). Hakutoiveiden
 *  väliset säännöt ja varasijasääntö tehdään tämän jälkeen (hakemuksenVtsTilat).
 *  Sijoittelemattomien jonojen sääntöä ei ole, koska tietoa ei ole ovarassa. Ilman yhtään
 *  valintarekisterin riviä (tallennettuTila None) tallennettu vastaanotto palautetaan sellaisenaan,
 *  ettei puuttuva valintarekisterin tieto nollaa myös vastaanottoa.
 */
private def vtsTila(
  tallennettuTila: Option[Valintatila],
  vastaanotto: Vastaanottotila,
  hyvaksyttyHarkinnanvaraisesti: Boolean,
  eiVarasijatayttoa: Boolean,
  merkitsevaJonoJulkaistavissa: Boolean,
  kaikkiJonotJulkaistu: Boolean,
  vastaanottoDeadline: Option[ZonedDateTime],
  nyt: ZonedDateTime
): VtsTila =
  tallennettuTila match {
    case None       => VtsTila(Valintatila.KESKEN, vastaanotto, julkaistu = false)
    case Some(tila) =>
      val jononTila = tila match {
        case t if Valintatila.hyvaksytyt(t) && hyvaksyttyHarkinnanvaraisesti =>
          Valintatila.HARKINNANVARAISESTI_HYVAKSYTTY
        case Valintatila.VARALLA if eiVarasijatayttoa => Valintatila.KESKEN
        case t                                        => t
      }
      // Hyväksytylle tai peruneelle ilman vastaanottoa määräajan jälkeen EI_VASTAANOTETTU_MAARA_AIKANA.
      val vastaanottotila =
        if (
          vastaanotto == Vastaanottotila.KESKEN &&
          (Valintatila.hyvaksytyt(jononTila) || jononTila == Valintatila.PERUNUT) &&
          vastaanottoDeadline.exists(_.toInstant.isBefore(nyt.toInstant))
        ) Vastaanottotila.EI_VASTAANOTETTU_MAARA_AIKANA
        else vastaanotto
      val valinnanTila = vastaanottotila match {
        case Vastaanottotila.VASTAANOTTANUT_SITOVASTI | Vastaanottotila.EHDOLLISESTI_VASTAANOTTANUT
            if !Valintatila.hyvaksytyt(jononTila) =>
          Valintatila.HYVAKSYTTY
        case Vastaanottotila.PERUNUT    => Valintatila.PERUNUT
        case Vastaanottotila.PERUUTETTU => Valintatila.PERUUTETTU
        case _                          => jononTila
      }
      val julkaistu = (Valintatila.hyvaksytyt(jononTila) && merkitsevaJonoJulkaistavissa) || kaikkiJonotJulkaistu
      val tulos     = VtsTila(valinnanTila, vastaanottotila, julkaistu = true)
      if (julkaistu) tulos else tulos.toKesken
  }

/**
 * Hakemuksen hakutoiveiden väliset säännöt valinta-tulos-servicen tapaan (ValintatulosService.julkaistavaTulos),
 *  VTS:n järjestyksessä hakutoivejärjestyksessä olevalle listalle. Jokainen sääntö laskee indeksinsä edellisen
 *  säännön tuloksesta. Kaikki kkhakijat-haut ovat korkeakouluhakuja (Haku.korkeakoulu).
 *  Ei toteutettu: näytäHyväksyttyäJulkaisematontaAlemmistaKorkeinHyvaksyttyOdottamassaYlempiä (vaatii valinnan
 *  tilojen historian) eikä yhden paikan säännöstä (asetaVastaanotettavuusValintarekisterinPerusteella).
 *  Muut VTS:n säännöt muuttavat vain jonokohtaisia tietoja, joita Sure ei lue.
 *
 * @param hakutoiveet hakukohde, hakutoivenumero ja hakutoiveen oma tila (vtsTila)
 * @return hakutoiveiden lopulliset tilat hakukohteittain
 */
private[kkhakijat] def hakemuksenVtsTilat[K](
  hakutoiveet: Seq[(K, Int, VtsTila)],
  sijoitteluJaPriorisointi: Boolean,
  varasijasaannotVoimassa: Boolean
): Map[K, VtsTila] = {
  import Valintatila.{HYVAKSYTTY, KESKEN, PERUNUT, PERUUNTUNUT, VARALLA, VARASIJALTA_HYVAKSYTTY, hyvaksytyt}

  def alempi(ensimmainen: Int, indeksi: Int): Boolean = ensimmainen >= 0 && indeksi > ensimmainen
  def vainSijoittelua(saanto: Seq[VtsTila] => Seq[VtsTila]): Seq[VtsTila] => Seq[VtsTila] =
    tilat => if (sijoitteluJaPriorisointi) saanto(tilat) else tilat

  // näytäJulkaisematontaAlemmatPeruuntuneetKeskeneräisinä
  val julkaisematontaAlemmatKeskeneraisiksi = vainSijoittelua { tilat =>
    val ensimmainenJulkaisematon = tilat.indexWhere(!_.julkaistu)
    tilat.zipWithIndex.map {
      case (t, i)
          if alempi(ensimmainenJulkaisematon, i) && (t.valinnanTila == PERUUNTUNUT || t.valinnanTila == KESKEN) =>
        t.toKesken
      case (t, _) => t
    }
  }
  // peruunnutaValmistaAlemmatKeskeneräisetJosKäytetäänSijoittelua
  val valmistaAlemmatKeskeneraisetPeruuntuneiksi = vainSijoittelua { tilat =>
    val ensimmainenValmis = tilat.indexWhere(t => hyvaksytyt(t.valinnanTila) || t.valinnanTila == PERUNUT)
    tilat.zipWithIndex.map {
      case (t, i) if alempi(ensimmainenValmis, i) && t.valinnanTila == KESKEN => t.copy(valinnanTila = PERUUNTUNUT)
      case (t, _)                                                             => t
    }
  }
  // näytäVarasijaltaHyväksytytHyväksyttyinäJosVarasijasäännötEiVoimassa
  val varasijaltaHyvaksytytHyvaksytyiksi: Seq[VtsTila] => Seq[VtsTila] = _.map {
    case t if t.valinnanTila == VARASIJALTA_HYVAKSYTTY && !varasijasaannotVoimassa => t.copy(valinnanTila = HYVAKSYTTY)
    case t                                                                         => t
  }
  // sovellaSijoitteluaKayttanvaKorkeakouluhaunSaantoja: sitovasti vastaanotetun lisäksi muut hyväksytyt
  // (vastaanottamattomat) sekä varalla ja kesken olevat peruuntuvat. Muut haarat muuttavat vain vastaanotettavuutta.
  val vastaanotetunMuutPeruuntuneiksi = vainSijoittelua { tilat =>
    val ensimmainenVastaanotettu     = tilat.indexWhere(_.vastaanottotila == Vastaanottotila.VASTAANOTTANUT_SITOVASTI)
    def muuKuinVastaanotettu(i: Int) = ensimmainenVastaanotettu >= 0 && i != ensimmainenVastaanotettu
    tilat.zipWithIndex.map {
      case (t, i) if hyvaksytyt(t.valinnanTila) && t.vastaanottotila == Vastaanottotila.KESKEN =>
        if (muuKuinVastaanotettu(i)) t.copy(valinnanTila = PERUUNTUNUT) else t
      case (t, i) if muuKuinVastaanotettu(i) && (t.valinnanTila == VARALLA || t.valinnanTila == KESKEN) =>
        t.copy(valinnanTila = PERUUNTUNUT)
      case (t, _) => t
    }
  }
  // näytäAlemmatPeruuntuneetHakukohteetKeskeneräisinäJosYlemmätKeskeneräisiä
  val keskeneraistaAlemmatPeruuntuneetKeskeneraisiksi = vainSijoittelua { tilat =>
    val ensimmainenKesken = tilat.indexWhere(_.valinnanTila == KESKEN)
    tilat.zipWithIndex.map {
      case (t, i) if alempi(ensimmainenKesken, i) && t.valinnanTila == PERUUNTUNUT => t.toKesken
      case (t, _)                                                                  => t
    }
  }

  val jarjestyksessa = hakutoiveet.sortBy(_._2)
  val saannot        = julkaisematontaAlemmatKeskeneraisiksi
    .andThen(valmistaAlemmatKeskeneraisetPeruuntuneiksi)
    .andThen(varasijaltaHyvaksytytHyvaksytyiksi)
    .andThen(vastaanotetunMuutPeruuntuneiksi)
    .andThen(keskeneraistaAlemmatPeruuntuneetKeskeneraisiksi)
  jarjestyksessa.map(_._1).zip(saannot(jarjestyksessa.map(_._3))).toMap
}

/**
 * Hakemuksen kaikkien hakutoiveiden lopulliset tilat hakukohteittain. Lasketaan kaikista hakemuksen
 *  hakutoiveista ennen rajausta hakukohteisiin tai organisaatioihin, kuten VTS:ssä.
 */
private[kkhakijat] def hakemuksenTilat(rivit: Seq[KKHakemusRow], nyt: ZonedDateTime): Map[String, VtsTila] =
  rivit.headOption match {
    case None       => Map.empty
    case Some(rivi) =>
      // Kaikki hakemuksen hakutoiveet ovat samassa haussa, joten ohjausparametrit ovat samat.
      hakemuksenVtsTilat(
        rivit.map(r => (r.hakukohdeOid, r.hakutoivenumero, r.hakutoiveenVtsTila(nyt))),
        sijoitteluJaPriorisointi = rivi.ohjausparametrit.sijoitteluJaPriorisointi,
        varasijasaannotVoimassa = rivi.ohjausparametrit.varasijasaannotVoimassa(nyt)
      )
  }

/**
 * Puuttuva käsittelymerkintä (tai NULL-tila) tarkoittaa NOT_CHECKEDia. Tuntematon tila
 *  lokitetaan ja pudotetaan: tulkitsematonta lähdekoodia ei päästetä rajapintaan. None siis
 *  erottuu NOT_CHECKEDista -- se tarkoittaa "merkintä on, mutta sitä ei osattu tulkita", ei
 *  "merkintää ei ole". Arvojoukot ovat Hakukelpoisuus- ja Maksuvelvollisuus-enumeissa, tässä
 *  vain puuttuvan ja tuntemattoman tiedon käsittely.
 */
private def kasittelymerkinnanTila[A](
  state: Option[String],
  parse: String => Option[A],
  defaultForNone: A,
  kentta: String
): Option[A] =
  state match {
    case None      => Some(defaultForNone)
    case Some(raw) =>
      parse(raw) match {
        case parsed @ Some(_) => parsed
        case None             =>
          LOG.warn(s"No mapping found for $kentta state: $raw")
          None
      }
  }

/** ONR:n master-henkilön tiedot (gen_henkilo-rivi, jonka henkilo_oid on hakijan oppijanumero). */
case class MasterHenkiloRow(
  hetu: Option[String],
  etunimet: Option[String],
  sukunimi: Option[String],
  sukupuoli: Option[Int],
  kansalaisuudet: Seq[String],
  aidinkieli: Option[String],
  syntymaaika: Option[LocalDate],
  turvakielto: Option[Boolean]
)

/**
 * hetu, etunimet, sukunimi ja sukupuoli ovat hakemukselta, kansalaisuudet, aidinkieli, syntymaaika ja
 *  turvakielto hakemukseen linkitetyltä henkilöltä. Niitä käytetään vain, jos master-riviä ei ole.
 */
case class KKHakijaRow(
  oppijanumero: String,
  hakemusOid: String,
  sahkoposti: String,
  puhelin: String,
  lahiosoite: String,
  postinumero: String,
  postitoimipaikka: String,
  hakuOid: String,
  etunimet: Option[String],
  kutsumanimi: Option[String],
  sukunimi: Option[String],
  hetu: Option[String],
  asuinmaa: Option[String],
  kansalaisuudet: Seq[String],
  kotikunta: Option[String],
  sukupuoli: Option[Int],
  asiointikieli: Option[Int],
  koulutusmarkkinointilupa: Option[Boolean],
  valintatuloksenJulkaisulupa: Option[Boolean],
  jatetty: Option[OffsetDateTime],
  muokattu: Option[OffsetDateTime],
  pohjakoulutus: Seq[String],
  aidinkieli: Option[String],
  syntymaaika: Option[LocalDate],
  turvakielto: Option[Boolean],
  ensikertalainen: Option[Boolean],
  vuosi: Option[Int],
  kausi: Option[String],
  master: Option[MasterHenkiloRow] = None
) {
  def asHakija(hakemukset: Seq[KKHakutoive], ylioppilas: Option[YlioppilasRow]): KKHakija = {
    // Suren tapaan henkilötiedot tulevat masterilta, myös silloin kun masterin tieto puuttuu.
    // Vain jos master-riviä ei ole lainkaan, käytetään hakemuksen ja linkitetyn henkilön tietoja.
    val henkilo = master.getOrElse(
      MasterHenkiloRow(hetu, etunimet, sukunimi, sukupuoli, kansalaisuudet, aidinkieli, syntymaaika, turvakielto)
    )
    KKHakija(
      hetu = henkilo.hetu.filter(_.nonEmpty).orElse(henkilo.syntymaaika.map(toKkSyntymaaika)).getOrElse(""),
      oppijanumero = oppijanumero,
      sukunimi = henkilo.sukunimi.getOrElse(""),
      etunimet = henkilo.etunimet.getOrElse(""),
      kutsumanimi = kutsumanimi.getOrElse(""),
      lahiosoite = lahiosoite,
      postinumero = postinumero,
      postitoimipaikka = postitoimipaikka,
      maa = asuinmaa.getOrElse(""),
      kotikunta = kotikuntaTaiTuntematon(kotikunta),
      sukupuoli = henkilo.sukupuoli.map(_.toString).getOrElse(""),
      // Suren tapaan tuntematon äidinkieli on "99".
      aidinkieli = henkilo.aidinkieli.getOrElse("99"),
      syntymaaika = henkilo.syntymaaika.map(_.format(SyntymaaikaFormatter)),
      asiointikieli = asiointikieli match {
        case None    => ""
        case Some(0) => "9" // Legacy-syyt: vanhassa Suoritusrekisterin rajapinnassa 9 merkitsi kieltä "muu".
        case Some(n) => n.toString
      },
      // Sure palauttaa listan aina, tyhjänäkin.
      kansalaisuudet = Some(henkilo.kansalaisuudet.toList),
      turvakielto = henkilo.turvakielto.getOrElse(false),
      matkapuhelin = Option(puhelin).filter(_.nonEmpty),
      sahkoposti = Option(sahkoposti).filter(_.nonEmpty),
      koulutusmarkkinointilupa = koulutusmarkkinointilupa,
      // TODO: koulusivistyskielet tulevat myöhemmin saataville. Siihen asti null eikä Suren tapaan tyhjä lista,
      // koska tyhjä lista antaisi ymmärtää ettei kieliä ole, vaikka tieto vain puuttuu.
      koulusivistyskielet = None,
      onYlioppilas = ylioppilas.flatMap(_.onYlioppilas).getOrElse(false),
      yoSuoritusVuosi = ylioppilas.flatMap(_.valmistumisVuosi).map(_.toString),
      ensikertalainen = ensikertalainen,
      hakemukset = hakemukset
    )
  }
}

case class YlioppilasRow(
  hakemusOid: String,
  onYlioppilas: Option[Boolean],
  valmistumisVuosi: Option[Int]
)

case class KKHakemusRow(
  hakemusOid: String,
  hakukohdeOid: String,
  hakutoivenumero: Int,
  organisaatioOid: Option[String],
  jarjestyspaikkaOid: Option[String],
  valintatieto: Option[String],
  vastaanottotieto: Option[String],
  ilmoittautumisenTila: Option[String],
  lukuvuosimaksu: Option[String],
  hakukohdeKkId: Option[String],
  koulutusOid: Option[String],
  koulutusKoodit: Seq[String],
  johtaaTutkintoon: Option[Boolean],
  toteutusAlkamisvuosi: Option[Int],
  toteutusAlkamiskausi: Option[String],
  valinnanTila: Option[String],
  valinnanAikaleima: Option[OffsetDateTime],
  pisteet: Option[BigDecimal],
  ehdollisestiHyvaksyttavissa: Option[Boolean],
  ehtoFI: Option[String],
  ehtoSV: Option[String],
  ehtoEN: Option[String],
  // Raaka tyyppi (valintatapajono-koodiuri) ja sen suomenkielinen nimi koodistosta.
  valintatapajononTyyppi: Option[String],
  valintatapajononNimi: Option[String],
  valintatapajononTyyppiNimiFi: Option[String],
  // Raakatilat gen_hakemus_kasittelymerkinnat-taulusta, ennen uudelleenmappausta.
  hKelpoisuusState: Option[String],
  hKelpoisuusMaksuvelvollisuusState: Option[String],
  // Merkitsevän jonon ja hakutoiveen jonojen tiedot valinnan tuloksen laskemiseen (vtsTila).
  merkitsevaJonoJulkaistavissa: Option[Boolean],
  hyvaksyttyHarkinnanvaraisesti: Option[Boolean],
  eiVarasijatayttoa: Option[Boolean],
  kaikkiJonotJulkaistu: Option[Boolean],
  ohjausparametrit: HaunOhjausparametrit
) {
  // Hakutoiveen oma tila ennen hakutoiveiden välisiä sääntöjä (ks. hakemuksenTilat).
  def hakutoiveenVtsTila(nyt: ZonedDateTime): VtsTila =
    vtsTila(
      tallennettuTila = valinnanTila.map(t => Valintatila.parse(t).getOrElse(Valintatila.KESKEN)),
      vastaanotto = vastaanottotieto.flatMap(Vastaanottotila.parse).getOrElse(Vastaanottotila.KESKEN),
      hyvaksyttyHarkinnanvaraisesti = hyvaksyttyHarkinnanvaraisesti.getOrElse(false),
      eiVarasijatayttoa = eiVarasijatayttoa.getOrElse(false),
      merkitsevaJonoJulkaistavissa = merkitsevaJonoJulkaistavissa.getOrElse(false),
      kaikkiJonotJulkaistu = kaikkiJonotJulkaistu.getOrElse(false),
      // Rivin valinnanAikaleima on hyväksymisen julkaisuhetki (hyvaksyttyjajulkaistu).
      vastaanottoDeadline = vastaanottoDeadline(ohjausparametrit, valinnanAikaleima),
      nyt = nyt
    )

  def asKKHakemus(
    hakuOid: String,
    hakuVuosi: Int,
    hakuKausi: String,
    hakemusnumero: String,
    hakemusJattoAikaleima: Option[String],
    hakemusViimeinenMuokkausAikaleima: Option[String],
    valinnanAikaleima: Option[String],
    pohjakoulutus: Seq[String],
    julkaisulupa: Option[Boolean],
    // Hakutoiveen lopullinen tila hakutoiveiden välisten sääntöjen jälkeen (hakemuksenTilat).
    tila: VtsTila
  ): KKHakutoive = {
    KKHakutoive(
      haku = hakuOid,
      hakuVuosi = hakuVuosi,
      hakuKausi = hakuKausi,
      hakemusnumero = hakemusnumero,
      organisaatio = organisaatioOid.getOrElse(""),
      hakukohde = hakukohdeOid,
      hakemusViimeinenMuokkausAikaleima = hakemusViimeinenMuokkausAikaleima,
      hakemusJattoAikaleima = hakemusJattoAikaleima,
      valinnanAikaleima = valinnanAikaleima,
      // -1 tarkoittaa haun järjestämättömiä hakutoiveita, jolloin Sure jättää prioriteetin pois.
      hakutoivePrioriteetti = Some(hakutoivenumero).filter(_ != -1),
      valinnanTila = Some(tila.valinnanTila),
      vastaanottotieto = Some(tila.vastaanottotila),
      ilmoittautumiset = ilmoittautumiset(toteutusAlkamiskausi, toteutusAlkamisvuosi, ilmoittautumisenTila),
      pohjakoulutus = pohjakoulutus,
      julkaisulupa = julkaisulupa,
      // Tuntematon tila jää Noneksi ja näkyy rajapinnassa tyhjänä; hKelpoisuusLahde ei tule
      // tästä taulusta, joten se jää yhä oletusarvoonsa.
      hKelpoisuus = kasittelymerkinnanTila(
        hKelpoisuusState,
        Hakukelpoisuus.parse,
        Hakukelpoisuus.NOT_CHECKED,
        "hKelpoisuus"
      ),
      hKelpoisuusMaksuvelvollisuus = kasittelymerkinnanTila(
        hKelpoisuusMaksuvelvollisuusState,
        Maksuvelvollisuus.parse,
        Maksuvelvollisuus.NOT_CHECKED,
        "hKelpoisuusMaksuvelvollisuus"
      ),
      // Suren tapaan maksuvelvolliselle, jolta maksutieto puuttuu, palautetaan MAKSAMATTA.
      lukuvuosimaksu = lukuvuosimaksu.orElse(
        Option.when(
          hKelpoisuusMaksuvelvollisuusState.flatMap(Maksuvelvollisuus.parse).contains(Maksuvelvollisuus.REQUIRED)
        )("MAKSAMATTA")
      ),
      hakukohdeKkId = hakukohdeKkId,
      // VTS ei näytä julkaisemattoman tuloksen pisteitä eikä ehdollista hyväksyntää; ehtotekstit se näyttää.
      pisteet = pisteet.filter(_ => tila.julkaistu),
      // Sure palauttaa ehdon aina; ilman julkaistua tietoa ehdollisesti hyväksyttävissä on false.
      hyvaksymisenEhto = Some(
        HyvaksymisenEhto(
          ehdollisestiHyvaksyttavissa = tila.julkaistu && ehdollisestiHyvaksyttavissa.getOrElse(false),
          ehtoKoodi = None,
          ehtoFI = ehtoFI,
          ehtoSV = ehtoSV,
          ehtoEN = ehtoEN
        )
      ),
      // Suren tapaan jonon tyyppi vain hyväksytyille, valintatapajono-koodiston suomenkielisenä nimenä.
      valintatapajononTyyppi = valintatapajononTyyppi
        .filter(_ => Valintatila.hyvaksytyt(tila.valinnanTila))
        .map(_ => valintatapajononTyyppiNimiFi.getOrElse("Tuntematon")),
      valintatapajononNimi = valintatapajononNimi,
      hakukohteenKoulutukset = koulutusOid.toSeq.flatMap { oid =>
        val koodit: Seq[Option[String]] =
          if (koulutusKoodit.isEmpty) Seq(None) else koulutusKoodit.map(Some(_))
        koodit.map(koodi =>
          KkHakukohteenkoulutus(
            komoOid = oid,
            koulutusKoodi = koodi,
            kkKoulutusId = hakukohdeKkId,
            koulutuksenAlkamisvuosi = toteutusAlkamisvuosi,
            koulutuksenAlkamiskausi = toteutusAlkamiskausi,
            johtaaTutkintoon = johtaaTutkintoon
          )
        )
      }
    )
  }
}
