package fi.oph.ovara.backend.external.toisenasteenhakijat

import java.time.OffsetDateTime

case class HakijaRow(
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
  koulutusmarkkinointilupa: Option[Boolean],
  kiinnostunutOppisopimuksesta: Option[Boolean],
  sahkoinenviestintalupa: Option[Boolean],
  valintatuloksenJulkaisulupa: Option[Boolean],
  jatetty: Option[OffsetDateTime],
  muokattu: Option[OffsetDateTime],
  aidinkieli: Option[String],
  opetuskieli: Option[String]
) {
  def asHakija(hakutoiveet: Seq[HakijaHakutoive]): ToisenAsteenHakija =
    ToisenAsteenHakija(
      oppijanumero = oppijanumero,
      sahkoposti = sahkoposti,
      matkapuhelin = puhelin,
      lahiosoite = lahiosoite,
      postinumero = postinumero,
      postitoimipaikka = postitoimipaikka,
      hetu = hetu,
      sukunimi = sukunimi,
      etunimet = etunimet,
      kutsumanimi = kutsumanimi,
      maa = asuinmaa,
      kansalaisuudet = kansalaisuudet,
      kotikunta = kotikunta,
      sukupuoli = sukupuoli.map(_.toString),
      aidinkieli = aidinkieli,
      opetuskieli = opetuskieli,
      koulutusmarkkinointilupa = koulutusmarkkinointilupa,
      kiinnostunutoppisopimuksesta = kiinnostunutOppisopimuksesta,
      sahkoisenAsioinninLupa = sahkoinenviestintalupa,
      hakemus = HakijaHakemus(
        hakemusnumero = hakemusOid,
        hakutoiveet = hakutoiveet,
        hakemuksenJattopaiva = jatetty,
        hakemuksenMuokkauspaiva = muokattu,
        julkaisulupa = valintatuloksenJulkaisulupa
      )
    )
}

case class HakijaHakutoiveRow(
  hakemusOid: String,
  hakukohdeOid: String,
  hakutoivenumero: Int,
  jarjestyspaikkaOid: Option[String],
  koulutusKoodiurit: Seq[String]
) {
  def asHakutoive(koodistot: Map[String, KoodistoArvo]): HakijaHakutoive =
    HakijaHakutoive(
      hakukohdeOid = hakukohdeOid,
      hakujno = hakutoivenumero,
      opetuspiste = jarjestyspaikkaOid,
      koulutus = koulutusKoodiurit.headOption.flatMap(koodistot.get)
    )
}
