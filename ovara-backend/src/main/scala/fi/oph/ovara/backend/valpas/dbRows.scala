package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.domain.Kielistetty

import java.time.OffsetDateTime

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
    haunAlku: Option[OffsetDateTime],
    haunLoppu: Option[OffsetDateTime],
    hakutapaKoodiuri: String,
    hakutyyppiKoodiuri: String,
    oppijanumero: String,
    huoltajanNimi: Option[String],
    huoltajanPuhelinnumero: Option[String],
    huoltajanSahkoposti: Option[String]
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
      hakutyyppi = koodistot(hakutyyppiKoodiuri),
      aktiivinenHaku = isAktiivinen,
      haunAlkamispaivamaara = haunAlku.map(_.toLocalDate),
      oppijaOid = oppijanumero,
      huoltajanNimi = huoltajanNimi,
      huoltajanPuhelinnumero = huoltajanPuhelinnumero,
      huoltajanSahkoposti = huoltajanSahkoposti,
      hakutoiveet = hakutoiveet
    )
  }

  private def isAktiivinen: Option[Boolean] = {
    val hakuaika = for (a <- haunAlku; l <- haunLoppu) yield (a, l)
    hakuaika.map { case (alku, loppu) =>
      val now = OffsetDateTime.now()
      !alku.isAfter(now) && loppu.isAfter(now)
    }
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
    valintatila: String,
    ilmoittautumistila: String,
    harkinnanvaraisuus: String
) {
  def asHakutoive(koodistot: Map[String, KoodistoArvo]): Hakutoive = {
    Hakutoive(
      hakukohdeOid = hakukohdeOid,
      hakukohdeNimi = hakukohdeNimi,
      hakutoivenumero = hakutoivenumero,
      hakukohdeOrganisaatio = hakukohdeOrganisaatio,
      organisaatioNimi = organisaatioNimi,
      koulutusOid = koulutusOid,
      koulutusNimi = koulutusNimi,
      hakukohdeKoulutuskoodi = hakukohdeKoulutuskoodi.map(koodistot),
      vastaanottotieto = vastaanottotieto,
      valintatila = valintatila,
      ilmoittautumistila = ilmoittautumistila,
      harkinnanvaraisuus = harkinnanvaraisuus,
      paasykoe = None,
      kielikoe = None,
      lisanaytto = None,
      liitteetTarkastettu = false,
      valintakoe = Seq.empty,
      alinHyvaksyttyPistemaara = null,
      alinValintaPistemaara = 0,
      pisteet = 0,
      varasijanumero = 0
    )
  }
}
