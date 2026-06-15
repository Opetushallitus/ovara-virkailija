package fi.oph.ovara.backend.external.toisenasteenhakijat

case class HakijaRow(
  oppijanumero: String,
  hakemusOid: String,
  sahkoposti: String,
  puhelin: String,
  lahiosoite: String,
  postinumero: String,
  postitoimipaikka: String,
  hakuOid: String
) {
  def asHakija(hakutoiveet: Seq[HakijaHakutoive]): ToisenAsteenHakija =
    ToisenAsteenHakija(
      oppijanumero = oppijanumero,
      sahkoposti = sahkoposti,
      matkapuhelin = puhelin,
      lahiosoite = lahiosoite,
      postinumero = postinumero,
      postitoimipaikka = postitoimipaikka,
      hakemus = HakijaHakemus(
        hakemusnumero = hakemusOid,
        hakutoiveet = hakutoiveet
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
