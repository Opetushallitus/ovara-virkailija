package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.domain.Kielistetty

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
 * (`isPaakayttaja = true`, `allowedOrgOids` ignored). Any other caller is limited to
 * `allowedOrgOids` — an empty set means the caller has no rights → empty results.
 */
case class KayttooikeusScope(
  isPaakayttaja: Boolean,
  allowedOrgOids: Set[String]
)

object KayttooikeusScope {
  val paakayttaja: KayttooikeusScope =
    KayttooikeusScope(isPaakayttaja = true, allowedOrgOids = Set.empty)

  def limited(orgs: Set[String]): KayttooikeusScope =
    KayttooikeusScope(isPaakayttaja = false, allowedOrgOids = orgs)
}

case class ToisenAsteenHakija(
  oppijanumero: String,
  sahkoposti: String,
  matkapuhelin: String,
  lahiosoite: String,
  postinumero: String,
  postitoimipaikka: String,
  hakemus: HakijaHakemus,
  hetu: Option[String] = None,
  sukunimi: Option[String] = None,
  etunimet: Option[String] = None,
  kutsumanimi: Option[String] = None,
  maa: Option[String] = None,
  kansalaisuudet: Seq[String] = Nil,
  muupuhelin: Option[String] = None,
  kotikunta: Option[String] = None,
  sukupuoli: Option[String] = None,
  aidinkieli: Option[String] = None,
  opetuskieli: Option[String] = None,
  koulutusmarkkinointilupa: Option[Boolean] = None,
  kiinnostunutoppisopimuksesta: Option[Boolean] = None,
  huoltaja1: Option[Huoltaja] = None,
  huoltaja2: Option[Huoltaja] = None,
  oikeusMaksuttomaanKoulutukseenVoimassaAsti: Option[String] = None,
  oppivelvollisuusVoimassaAsti: Option[String] = None,
  sahkoisenAsioinninLupa: Option[Boolean] = None,
  lisakysymykset: Seq[Lisakysymys] = Nil
)

case class HakijaHakemus(
  hakemusnumero: String,
  hakutoiveet: Seq[HakijaHakutoive],
  vuosi: Option[String] = None,
  kausi: Option[String] = None,
  hakemuksenJattopaiva: Option[OffsetDateTime] = None,
  hakemuksenMuokkauspaiva: Option[OffsetDateTime] = None,
  lahtokoulu: Option[String] = None,
  lahtokoulunnimi: Option[String] = None,
  luokka: Option[String] = None,
  luokkataso: Option[String] = None,
  pohjakoulutus: Option[String] = None,
  todistusvuosi: Option[String] = None,
  muukoulutus: Option[String] = None,
  julkaisulupa: Option[Boolean] = None,
  yhteisetaineet: Option[BigDecimal] = None,
  lukiontasapisteet: Option[BigDecimal] = None,
  lisapistekoulutus: Option[String] = None,
  yleinenkoulumenestys: Option[BigDecimal] = None,
  painotettavataineet: Option[BigDecimal] = None,
  osaaminen: Osaaminen = Osaaminen()
)

case class HakijaHakutoive(
  hakukohdeOid: String,
  hakujno: Int,
  oppilaitos: Option[String] = None,
  opetuspiste: Option[String] = None,
  opetuspisteennimi: Option[Kielistetty] = None,
  koulutus: Option[KoodistoArvo] = None,
  harkinnanvaraisuusperuste: Option[String] = None,
  urheilijanammatillinenkoulutus: Option[Boolean] = None,
  yhteispisteet: Option[BigDecimal] = None,
  valinta: Option[String] = None,
  vastaanotto: Option[String] = None,
  lasnaolo: Option[String] = None,
  terveys: Option[Boolean] = None,
  aiempiperuminen: Option[Boolean] = None,
  kaksoistutkinto: Option[Boolean] = None,
  koulutuksenKieli: Option[String] = None,
  keskiarvo: Option[String] = None,
  urheilijanLisakysymykset: Option[UrheilijanLisakysymykset] = None
)

case class Huoltaja(
  etunimi: String,
  sukunimi: String,
  puhelinnumero: String,
  sahkoposti: String
)

case class Osaaminen(
  yleinen_kielitutkinto_fi: Option[String] = None,
  valtionhallinnon_kielitutkinto_fi: Option[String] = None,
  yleinen_kielitutkinto_sv: Option[String] = None,
  valtionhallinnon_kielitutkinto_sv: Option[String] = None,
  yleinen_kielitutkinto_en: Option[String] = None,
  valtionhallinnon_kielitutkinto_en: Option[String] = None
)

case class UrheilijanLisakysymykset(
  peruskoulu: Option[String] = None,
  keskiarvo: Option[String] = None,
  tamakausi: Option[String] = None,
  viimekausi: Option[String] = None,
  toissakausi: Option[String] = None,
  sivulaji: Option[String] = None,
  valmennusryhma_seurajoukkue: Option[String] = None,
  valmennusryhma_piirijoukkue: Option[String] = None,
  valmennusryhma_maajoukkue: Option[String] = None,
  valmentaja_nimi: Option[String] = None,
  valmentaja_email: Option[String] = None,
  valmentaja_puh: Option[String] = None,
  laji: Option[String] = None,
  liitto: Option[String] = None,
  seura: Option[String] = None
)

case class Lisakysymys(
  kysymysid: String,
  hakukohdeOids: Seq[String],
  kysymystyyppi: String,
  kysymysteksti: String,
  vastaukset: Seq[LisakysymysVastaus]
)

case class LisakysymysVastaus(
  vastausid: String,
  vastausteksti: String
)

case class KoodistoArvo(
  versioituUri: String,
  koodiarvo: String,
  koodistoUri: String,
  koodistoVersio: Int,
  nimi: Kielistetty
)
