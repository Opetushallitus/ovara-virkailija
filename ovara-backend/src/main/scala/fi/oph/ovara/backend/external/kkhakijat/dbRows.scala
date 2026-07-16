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
  asiointikieli: Option[Int],
  koulutusmarkkinointilupa: Option[Boolean],
  valintatuloksenJulkaisulupa: Option[Boolean],
  jatetty: Option[OffsetDateTime],
  muokattu: Option[OffsetDateTime],
  aidinkieli: Option[String],
  ensikertalainen: Option[Boolean],
  vuosi: Option[Int],
  kausi: Option[String]
) {
  def asHakija(hakemukset: Seq[KKHakutoive], ylioppilas: Option[YlioppilasRow]): KKHakija =
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
      asiointikieli = asiointikieli match {
        case None    => ""
        case Some(0) => "9"
        case Some(n) => n.toString
      },
      kansalaisuudet = if (kansalaisuudet.isEmpty) None else Some(kansalaisuudet.toList),
      matkapuhelin = Option(puhelin).filter(_.nonEmpty),
      sahkoposti = Option(sahkoposti).filter(_.nonEmpty),
      koulutusmarkkinointilupa = koulutusmarkkinointilupa,
      onYlioppilas = ylioppilas.flatMap(_.onYlioppilas).getOrElse(false),
      yoSuoritusVuosi = ylioppilas.flatMap(_.valmistumisVuosi).map(_.toString),
      ensikertalainen = ensikertalainen,
      hakemukset = hakemukset
    )
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
  valinnanTila: Option[String],
  valinnanAikaleima: Option[OffsetDateTime],
  pisteet: Option[BigDecimal],
  ehdollistiHyvaksyttavissa: Option[Boolean],
  ehtoFI: Option[String],
  ehtoSV: Option[String],
  ehtoEN: Option[String]
) {
  def asKKHakemus(
    hakuOid: String,
    hakuVuosi: Int,
    hakuKausi: String,
    hakemusnumero: String,
    hakemusJattoAikaleima: Option[String],
    hakemusViimeinenMuokkausAikaleima: Option[String],
    valinnanAikaleima: Option[String],
    julkaisulupa: Option[Boolean]
  ): KKHakutoive =
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
      hakutoivePrioriteetti = Some(hakutoivenumero),
      valinnanTila = valinnanTila.flatMap(Valintatila.parse),
      vastaanottotieto = vastaanottotieto.flatMap(Vastaanottotila.parse),
      ilmoittautumiset = ilmoittautumisenTila.flatMap(Lasnaolo.parse).toSeq,
      julkaisulupa = julkaisulupa,
      lukuvuosimaksu = lukuvuosimaksu,
      hakukohdeKkId = hakukohdeKkId,
      pisteet = pisteet,
      hyvaksymisenEhto = ehdollistiHyvaksyttavissa.map(ehd =>
        HyvaksymisenEhto(
          ehdollisestiHyvaksyttavissa = ehd,
          ehtoKoodi = None,
          ehtoFI = ehtoFI,
          ehtoSV = ehtoSV,
          ehtoEN = ehtoEN
        )
      )
    )
}
