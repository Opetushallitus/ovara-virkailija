package fi.oph.ovara.backend.external.kkhakijat

import java.time.OffsetDateTime

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
  koulutusmarkkinointilupa: Option[Boolean],
  valintatuloksenJulkaisulupa: Option[Boolean],
  jatetty: Option[OffsetDateTime],
  muokattu: Option[OffsetDateTime],
  aidinkieli: Option[String],
  vuosi: Option[Int],
  kausi: Option[String]
) {
  def asHakija(hakemukset: Seq[KKHakemus]): KKHakija =
    KKHakija(
      hetu = hetu.getOrElse(""),
      oppijanumero = oppijanumero,
      sukunimi = sukunimi.getOrElse(""),
      etunimet = etunimet.getOrElse(""),
      kutsumanimi = kutsumanimi.getOrElse(""),
      lahiosoite = lahiosoite,
      postinumero = postinumero,
      postitoimipaikka = postitoimipaikka,
      maa = asuinmaa.getOrElse(""),
      kotikunta = kotikunta.getOrElse(""),
      sukupuoli = sukupuoli.map(_.toString).getOrElse(""),
      aidinkieli = aidinkieli.getOrElse(""),
      asiointikieli = "",
      kansalaisuudet = if (kansalaisuudet.isEmpty) None else Some(kansalaisuudet.toList),
      matkapuhelin = Option(puhelin).filter(_.nonEmpty),
      sahkoposti = Option(sahkoposti).filter(_.nonEmpty),
      koulutusmarkkinointilupa = koulutusmarkkinointilupa,
      hakemukset = hakemukset
    )
}

case class KKHakemusRow(
  hakemusOid: String,
  hakukohdeOid: String,
  hakutoivenumero: Int,
  organisaatioOid: Option[String],
  jarjestyspaikkaOid: Option[String],
  valintatieto: Option[String],
  vastaanottotieto: Option[String],
  ilmoittautumisenTila: Option[String],
  lukuvuosimaksu: Option[String]
) {
  def asKKHakemus(
    hakuOid: String,
    hakuVuosi: Int,
    hakuKausi: String,
    hakemusnumero: String,
    hakemusJattoAikaleima: Option[String],
    hakemusViimeinenMuokkausAikaleima: Option[String],
    julkaisulupa: Option[Boolean]
  ): KKHakemus =
    KKHakemus(
      haku = hakuOid,
      hakuVuosi = hakuVuosi,
      hakuKausi = hakuKausi,
      hakemusnumero = hakemusnumero,
      organisaatio = organisaatioOid.getOrElse(""),
      hakukohde = hakukohdeOid,
      hakemusViimeinenMuokkausAikaleima = hakemusViimeinenMuokkausAikaleima,
      hakemusJattoAikaleima = hakemusJattoAikaleima,
      hakutoivePrioriteetti = Some(hakutoivenumero),
      valinnanTila = valintatieto.flatMap(Valintatila.parse),
      vastaanottotieto = vastaanottotieto.flatMap(Vastaanottotila.parse),
      ilmoittautumiset = ilmoittautumisenTila.flatMap(Lasnaolo.parse).toSeq,
      julkaisulupa = julkaisulupa,
      lukuvuosimaksu = lukuvuosimaksu
    )
}
