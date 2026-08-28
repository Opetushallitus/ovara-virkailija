package fi.oph.ovara.backend.external.kkhakijat

import java.time.OffsetDateTime

enum Valintarajaus {
  case HAKENEET, HYVAKSYTYT, VASTAANOTTANEET
}

object Valintarajaus {
  def parse(s: String): Option[Valintarajaus] =
    values.find(_.toString == s)
}

/**
 * Käyttöoikeus scope carried through the request. OPH_PAAKAYTTAJA is unrestricted
 * (`isPaakayttaja = true`, the other fields ignored). Any other caller is limited to the
 * union of two grants, either of which alone is enough to let a hakutoive through:
 *   - `allowedOrgOids`: organisaatio-oikeudet, verrataan hakukohteen järjestyspaikkaan.
 *   - `allowedHakukohderyhmaOids`: hakukohderyhmäoikeudet sellaisina kuin ne on myönnetty.
 *     Palvelu laajentaa nämä haun hakukohteiksi `allowedHakukohdeOids`-kenttään, koska
 *     ryhmätieto on vain pub-skeemassa eikä hakijakyselyn ulottuvilla.
 * Kaikkien tyhjyys tarkoittaa, ettei kutsujalla ole oikeuksia → tyhjä tulos.
 */
case class KayttooikeusScopeKK(
  isPaakayttaja: Boolean,
  allowedOrgOids: Set[String],
  allowedHakukohderyhmaOids: Set[String] = Set.empty,
  allowedHakukohdeOidsFromHakukohderyhmat: Set[String] = Set.empty
) {

  /**
   * Oikeus kaikkiin tämän rajapinnan tietoihin ilman organisaatio- tai ryhmärajausta. Tällä
   * oikeudella saa myös hakea yksittäisen henkilön tiedot oppijanumerolla.
   *
   * Nyt vain rekisterinpitäjällä eli OPH-pääkäyttäjällä. Kun erillinen "kaikki KK-hakijat"
   * -oikeus aikanaan lisätään, se lisätään tähän uutena disjunktina -- ei kutsupaikkoihin, joita
   * on useita (kontrollerin oppijanumero-tarkistus ja palvelun rivitason `allowedMatch`).
   */
  def saaKaikkiTiedot: Boolean = isPaakayttaja
}

object KayttooikeusScopeKK {
  val paakayttaja: KayttooikeusScopeKK =
    KayttooikeusScopeKK(isPaakayttaja = true, allowedOrgOids = Set.empty)

  // Yksi metodi oletusarvolla, ei kahta ylikuormitusta: `limited(Set(...))` olisi
  // ylikuormitusten kanssa monitulkintainen.
  def limited(orgs: Set[String], hakukohderyhmat: Set[String] = Set.empty): KayttooikeusScopeKK =
    KayttooikeusScopeKK(
      isPaakayttaja = false,
      allowedOrgOids = orgs,
      allowedHakukohderyhmaOids = hakukohderyhmat
    )
}

// Enum wrappers over the raw DB strings. Scala 3's default `toString` returns the case
// name (e.g. Fi -> "Fi"), so we surface the DB code directly via `name` without a cycle.
enum Valintatila {
  case HYVAKSYTTY, HARKINNANVARAISESTI_HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY,
    VARALLA, HYLATTY, PERUNUT, PERUUNTUNUT, PERUUTETTU, KESKEN
  def name: String = toString
}

object Valintatila {
  def parse(s: String): Option[Valintatila] = values.find(_.toString == s)
}

enum Vastaanottotila {
  case VASTAANOTTANUT_SITOVASTI, EHDOLLISESTI_VASTAANOTTANUT, PERUNUT,
    EI_VASTAANOTETTU_MAARA_AIKANA, PERUUTETTU, KESKEN
  def name: String = toString
}

object Vastaanottotila {
  def parse(s: String): Option[Vastaanottotila] = values.find(_.toString == s)
}

enum Lasnaolo {
  case EI_TEHTY, LASNA_KOKO_LUKUVUOSI, POISSA_KOKO_LUKUVUOSI, EI_ILMOITTAUTUNUT,
    LASNA_SYKSY, POISSA_SYKSY, LASNA, POISSA
  def name: String = toString
}

object Lasnaolo {
  def parse(s: String): Option[Lasnaolo] = values.find(_.toString == s)
}

case class HyvaksymisenEhto(
  ehdollisestiHyvaksyttavissa: Boolean = false,
  ehtoKoodi: Option[String] = None,
  ehtoFI: Option[String] = None,
  ehtoSV: Option[String] = None,
  ehtoEN: Option[String] = None
)

case class KkHakukohteenkoulutus(
  komoOid: String,
  koulutusKoodi: Option[String] = None,
  kkKoulutusId: Option[String] = None,
  koulutuksenAlkamisvuosi: Option[Int] = None,
  koulutuksenAlkamiskausi: Option[String] = None,
  johtaaTutkintoon: Option[Boolean] = None
)

case class Liite(
  hakuId: Option[String] = None,
  hakuRyhmaId: Option[String] = None,
  tila: Option[String] = None,
  saapumisenTila: Option[String] = None,
  nimi: Option[String] = None,
  vastaanottaja: Option[String] = None
)

case class KKHakija(
  hetu: String,
  oppijanumero: String,
  sukunimi: String,
  etunimet: String,
  kutsumanimi: String,
  lahiosoite: String,
  postinumero: String,
  postitoimipaikka: String,
  maa: String,
  kotikunta: String,
  sukupuoli: String,
  aidinkieli: String,
  asiointikieli: String,
  kansalaisuus: Option[String] = None,
  kaksoiskansalaisuus: Option[String] = None,
  kansalaisuudet: Option[List[String]] = None,
  syntymaaika: Option[String] = None,
  matkapuhelin: Option[String] = None,
  puhelin: Option[String] = None,
  sahkoposti: Option[String] = None,
  koulusivistyskieli: Option[String] = None,
  koulusivistyskielet: Option[Seq[String]] = None,
  koulutusmarkkinointilupa: Option[Boolean] = None,
  onYlioppilas: Boolean = false,
  yoSuoritusVuosi: Option[String] = None,
  turvakielto: Boolean = false,
  hakemukset: Seq[KKHakutoive] = Nil,
  ensikertalainen: Option[Boolean] = None
)

case class KKHakutoive(
  haku: String,
  hakuVuosi: Int,
  hakuKausi: String,
  hakemusnumero: String,
  organisaatio: String,
  hakukohde: String,
  hakemusViimeinenMuokkausAikaleima: Option[String] = None,
  hakemusJattoAikaleima: Option[String] = None,
  valinnanAikaleima: Option[String] = None,
  hakutoivePrioriteetti: Option[Int] = None,
  hakukohdeKkId: Option[String] = None,
  avoinVayla: Option[Boolean] = None,
  valinnanTila: Option[Valintatila] = None,
  valintatapajononTyyppi: Option[String] = None,
  valintatapajononNimi: Option[String] = None,
  hyvaksymisenEhto: Option[HyvaksymisenEhto] = None,
  vastaanottotieto: Option[Vastaanottotila] = None,
  pisteet: Option[BigDecimal] = None,
  ilmoittautumiset: Seq[Lasnaolo] = Nil,
  pohjakoulutus: Seq[String] = Nil,
  julkaisulupa: Option[Boolean] = None,
  hKelpoisuus: String = "",
  hKelpoisuusLahde: Option[String] = None,
  hKelpoisuusMaksuvelvollisuus: Option[String] = None,
  lukuvuosimaksu: Option[String] = None,
  hakukohteenKoulutukset: Seq[KkHakukohteenkoulutus] = Nil,
  liitteet: Option[Seq[Liite]] = None
)
