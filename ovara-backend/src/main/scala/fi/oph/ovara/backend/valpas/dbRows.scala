package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.domain.Kielistetty
import fi.oph.ovara.backend.utils.Constants.HELSINKI_TIMEZONE

import java.time.{LocalDateTime, OffsetDateTime}

case class HakemusRow(
    hakemusOid: String,
    muokattu: Option[OffsetDateTime],
    sahkoposti: String,
    puhelin: String,
    lahiosoite: String,
    postinumero: String,
    postitoimipaikka: String,
    asuinmaa: String,
    hakuOid: String,
    haunNimi: Kielistetty,
    hakuajat: List[ValpasHakuaika],
    hakukierrosPaattyy: Option[OffsetDateTime],
    hakutapaKoodiuri: String,
    oppijanumero: String,
    julkaisuAika: Option[OffsetDateTime]
) {
  def asHakemus(koodistot: Map[String, KoodistoArvo], hakutoiveet: Seq[Hakutoive]): Hakemus = {
    Hakemus(
      hakemusOid = hakemusOid,
      hakemuksenMuokkauksenAikaleima = muokattu.map(t => OffsetDateTime.from(t)),
      email = sahkoposti,
      matkapuhelin = puhelin,
      lahiosoite = lahiosoite,
      postinumero = postinumero,
      postitoimipaikka = postitoimipaikka,
      maa = koodistot(ValpasService.toMaaKoodiUri(asuinmaa)),
      hakuOid = hakuOid,
      hakuNimi = haunNimi,
      hakutapa = koodistot(hakutapaKoodiuri),
      aktiivinenHaku = isAktiivinen,
      haunAlkamispaivamaara = firstHakuajanAlku,
      oppijaOid = oppijanumero,
      hakutoiveet = hakutoiveet
    )
  }

  lazy val firstHakuajanAlku: Option[LocalDateTime] =
    hakuajat.flatMap(_.alkaa).minOption

  lazy val isAktiivinen: Option[Boolean] = {
    val alkuaika = firstHakuajanAlku.map(_.atZone(HELSINKI_TIMEZONE).toOffsetDateTime)
    val now      = OffsetDateTime.now()
    for (a <- alkuaika; l <- hakukierrosPaattyy) yield !a.isAfter(now) && l.isAfter(now)
  }
}

case class HakutoiveRow(
    hakemusOid: String,
    hakukohdeOid: String,
    hakukohdeNimi: Kielistetty,
    hakutoivenumero: Int,
    hakukohdeOrganisaatio: String,
    organisaatioNimi: Kielistetty,
    koulutusOid: String,
    koulutusNimi: Kielistetty,
    hakukohdeKoulutuskoodi: Seq[String], // Koutan toteutus.koulutuksetKoodiUri? koulutus.koulutuksetKoodiUri?
    vastaanottotieto: String,
    ilmoittautumistila: String,
    valintatila: String,
    harkinnanvaraisuus: String,
    valintatapajonoId: String,
    alinHyvaksyttyPistemaara: BigDecimal,
    pisteet: Option[BigDecimal],
    varasijanNumero: Option[Int],
    julkaistavissa: Option[Boolean]
) {
  def asHakutoive(koodistot: Map[String, KoodistoArvo], julkaisuAika: Option[OffsetDateTime]): Hakutoive = {
    val julkaistu = julkaistavissa.contains(true) && julkaisuAika.forall(_.isBefore(OffsetDateTime.now()))

    Hakutoive(
      hakukohdeOid = hakukohdeOid,
      hakukohdeNimi = hakukohdeNimi,
      hakutoivenumero = hakutoivenumero,
      hakukohdeOrganisaatio = hakukohdeOrganisaatio,
      organisaatioNimi = organisaatioNimi,
      koulutusOid = koulutusOid,
      koulutusNimi = koulutusNimi,
      hakukohdeKoulutuskoodi = hakukohdeKoulutuskoodi.map(koodistot),
      vastaanottotieto = if (julkaistu) Some(vastaanottotieto) else None,
      ilmoittautumistila = if (julkaistu) Some(ilmoittautumistila) else None,
      valintatila = if (julkaistu) Some(valintatila) else None,
      harkinnanvaraisuus = harkinnanvaraisuus,
      alinHyvaksyttyPistemaara = if (julkaistu) Some(alinHyvaksyttyPistemaara) else None,
      pisteet = if (julkaistu) pisteet else None,
      varasijanumero = if (julkaistu) varasijanNumero else None
    )
  }
}
