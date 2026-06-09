package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.domain.{En, Fi, Kieli, Kielistetty, Sv}

import java.time.{LocalDateTime, OffsetDateTime}

object ValpasFactory {
  val OPPIJANUMERO       = "1.2.246.562.24.9"
  val OPPIJANUMERO_2     = "1.2.246.562.24.10"
  val HAKEMUS_OID        = "1.2.246.562.11.00000000000003511892"
  val HAKEMUS_OID_2      = "1.2.246.562.11.00000000000003511893"
  val HAKU_OID           = "1.2.246.562.29.100"
  val HAKU_OID_2         = "1.2.246.562.29.101"
  val HAKUKOHDE_OID      = "1.2.246.562.20.012"
  val TOTEUTUS_OID       = "1.2.246.562.17.122"
  val KOULUTUS_OID       = "1.2.246.562.13.022"
  val ORGANISAATIO_OID   = "1.2.246.562.10.486"
  val VALINTATAPAJONO_ID = "16799"
  val KOULUTUS_KOODIURI  = "koulutus_621702#12"

  val YESTERDAY: OffsetDateTime = OffsetDateTime.now().minusDays(1)
  val TOMORROW: OffsetDateTime  = OffsetDateTime.now().plusDays(2)

  val HAKEMUKSEN_MUOKKAUSAIKA = "2025-08-13T14:52:14+03"
  val EMAIL                   = "oppija@example.test"
  val MATKAPUHELIN            = "+358401234567"
  val LAHIOSOITE              = "Katu 1"
  val POSTINUMERO             = "00100"
  val HELSINKI                = "Helsinki"
  val SUOMI_KOODI             = "246"

  val ORGANISAATIO_NIMI: Kielistetty = Map(Fi -> "Porin toimipaikka", Sv -> "Björneborg verksamhetspunkt")
  val KOULUTUS_NIMI: Kielistetty     = createKielistetty("Kulttuurituottaja", "Kulturproducent", "Kulttuurituottaja")

  def createKielistetty(
    fi: String = "Elokuvaleikkaus",
    sv: String = "Filmklippning",
    en: String = "Film Editing"
  ): Map[Kieli, String] =
    Map(Fi -> fi, Sv -> sv, En -> en)

  def createHakutoiveRow(
    vastaanottotieto: Option[String] = Some("VASTAANOTTANUT_SITOVASTI"),
    ilmoittautumistila: Option[String] = Some("LASNA_KOKO_LUKUVUOSI"),
    valintatila: Option[String] = Some("HYVAKSYTTY"),
    julkaistavissa: Option[Boolean] = Some(true)
  ): HakutoiveRow = HakutoiveRow(
    hakemusOid = HAKEMUS_OID,
    hakukohdeOid = HAKUKOHDE_OID,
    hakukohdeNimi = createKielistetty(),
    hakutoivenumero = 1,
    hakukohdeOrganisaatio = ORGANISAATIO_OID,
    organisaatioNimi = ORGANISAATIO_NIMI,
    koulutusOid = KOULUTUS_OID,
    koulutusNimi = KOULUTUS_NIMI,
    hakukohdeKoulutuskoodi = List(KOULUTUS_KOODIURI),
    vastaanottotieto = vastaanottotieto,
    ilmoittautumistila = ilmoittautumistila,
    valintatila = valintatila,
    harkinnanvaraisuus = "EI_HARKINNANVARAINEN",
    valintatapajonoId = VALINTATAPAJONO_ID,
    alinHyvaksyttyPistemaara = 21.1,
    pisteet = Some(23.7),
    varasijanNumero = Some(4),
    julkaistavissa = julkaistavissa
  )

  def createValpasHakuaika(alkaa: LocalDateTime, paattyy: LocalDateTime) =
    ValpasHakuaika(Option(alkaa), Option(paattyy))

  def createHakemusRow(
    hakuajat: List[ValpasHakuaika] = List(createValpasHakuaika(YESTERDAY.toLocalDateTime, TOMORROW.toLocalDateTime)),
    hakukierrosPaattyy: Option[OffsetDateTime] = Some(TOMORROW)
  ) =
    HakemusRow(
      hakemusOid = HAKEMUS_OID,
      muokattu = Some(OffsetDateTime.parse(HAKEMUKSEN_MUOKKAUSAIKA)),
      sahkoposti = EMAIL,
      puhelin = MATKAPUHELIN,
      lahiosoite = LAHIOSOITE,
      postinumero = POSTINUMERO,
      postitoimipaikka = HELSINKI,
      asuinmaa = SUOMI_KOODI,
      hakuOid = HAKU_OID,
      haunNimi = createKielistetty("Yhteishaku", "Gemensamma", "Joint application"),
      hakuajat = hakuajat,
      hakukierrosPaattyy = hakukierrosPaattyy,
      hakutapaKoodiuri = "hakutapa_03#1",
      oppijanumero = OPPIJANUMERO,
      julkaisuAika = Some(YESTERDAY)
    )

  def createKoulutusKoodi() =
    KoodistoArvo(
      KOULUTUS_KOODIURI,
      "621702",
      "koulutus",
      12,
      Map(Fi -> "Kulttuurituottaja", Sv -> "Kulturproducent", En -> "Bachelor of Culture and Arts, Cultural Manager")
    )
}
