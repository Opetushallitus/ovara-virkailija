package fi.oph.ovara.backend.external.kkhakijat

import org.slf4j.{Logger, LoggerFactory}

import java.time.{LocalDate, OffsetDateTime}

private val LOG: Logger =
  LoggerFactory.getLogger("fi.oph.ovara.backend.external.kkhakijat.Kasittelymerkinnat")

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
  kansalaisuus: Option[String],
  turvakielto: Option[Boolean],
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
      syntymaaika = syntymaaika.map(_.toString),
      asiointikieli = asiointikieli match {
        case None    => ""
        case Some(0) => "9" // Legacy-syyt: vanhassa Suoritusrekisterin rajapinnassa 9 merkitsi kieltä "muu".
        case Some(n) => n.toString
      },
      kansalaisuus = kansalaisuus,
      kansalaisuudet = if (kansalaisuudet.isEmpty) None else Some(kansalaisuudet.toList),
      turvakielto = turvakielto.getOrElse(false),
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
  koulutusOid: Option[String],
  koulutusKoodit: Seq[String],
  johtaaTutkintoon: Option[Boolean],
  toteutusAlkamisvuosi: Option[Int],
  toteutusAlkamiskausi: Option[String],
  valinnanTila: Option[String],
  valinnanAikaleima: Option[OffsetDateTime],
  pisteet: Option[BigDecimal],
  ehdollistiHyvaksyttavissa: Option[Boolean],
  ehtoFI: Option[String],
  ehtoSV: Option[String],
  ehtoEN: Option[String],
  valintatapajononTyyppi: Option[String],
  valintatapajononNimi: Option[String],
  // Raakatilat gen_hakemus_kasittelymerkinnat-taulusta, ennen uudelleenmappausta.
  hKelpoisuusState: Option[String],
  hKelpoisuusMaksuvelvollisuusState: Option[String]
) {
  def asKKHakemus(
    hakuOid: String,
    hakuVuosi: Int,
    hakuKausi: String,
    hakemusnumero: String,
    hakemusJattoAikaleima: Option[String],
    hakemusViimeinenMuokkausAikaleima: Option[String],
    valinnanAikaleima: Option[String],
    pohjakoulutus: Seq[String],
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
      valinnanTila = valinnanTila.flatMap(Valintatila.parse).orElse(Some(Valintatila.KESKEN)),
      vastaanottotieto = vastaanottotieto.flatMap(Vastaanottotila.parse),
      ilmoittautumiset = ilmoittautumisenTila.flatMap(Lasnaolo.parse).toSeq,
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
      ),
      valintatapajononTyyppi = valintatapajononTyyppi,
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
