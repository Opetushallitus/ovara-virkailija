package fi.oph.ovara.backend.external.toisenasteenhakijat

import java.time.OffsetDateTime

object ExternalToisenAsteenHakijatTestData {
  val OPPIJANUMERO      = "1.2.246.562.24.9"
  val HAKEMUS_OID       = "1.2.246.562.11.00000000000003511892"
  val HAKEMUS_OID_2     = "1.2.246.562.11.00000000000003511893"
  val HAKU_OID          = "1.2.246.562.29.100"
  val HAKU_OID_2        = "1.2.246.562.29.101"
  val HAKUKOHDE_OID     = "1.2.246.562.20.012"
  val HAKUKOHDE_OID_2   = "1.2.246.562.20.013"
  val TOTEUTUS_OID      = "1.2.246.562.17.122"
  val KOULUTUS_OID      = "1.2.246.562.13.022"
  val ORGANISAATIO_OID  = "1.2.246.562.10.486"
  val KOULUTUS_KOODIURI = "koulutus_621702#12"

  val EMAIL        = "oppija@example.test"
  val MATKAPUHELIN = "+358401234567"
  val LAHIOSOITE   = "Katu 1"
  val POSTINUMERO  = "00100"
  val HELSINKI     = "Helsinki"
  val SUOMI_KOODI  = "246"

  // Raw values mimic what the production DB actually holds:
  // gen_henkilo.aidinkieli is a lowercase code, gen_supa_tieto.arvo is a quoted lowercase code.
  // Both are normalized (unquoted, uppercased) by the extractor.
  val AIDINKIELI_RAW              = "fi"
  val AIDINKIELI                  = "FI"
  val OPETUSKIELI_RAW             = "\"sv\""
  val OPETUSKIELI                 = "SV"
  val KOULUTUKSEN_ALKAMISVUOSI    = 2026
  val VUOSI                       = "2026"
  val KOULUTUKSEN_ALKAMISKAUSIURI = "kausi_s#1"
  val KAUSI                       = "S"
  val OPPILAITOSNUMERO            = "00123"
  val OPPILAITOS                  = "00123"

  val HUOLTAJA1 = Huoltaja(
    etunimi = "Maija",
    sukunimi = "Huoltaja",
    puhelinnumero = "+358401111111",
    sahkoposti = "maija@example.test"
  )
  val HUOLTAJA2 = Huoltaja(
    etunimi = "Pekka",
    sukunimi = "Huoltaja",
    puhelinnumero = "+358402222222",
    sahkoposti = "pekka@example.test"
  )

  val lukioKysymykset = UrheilijanLisakysymykset(
    laji = Some("Jääkiekko"),
    seura = Some("HIFK"),
    liitto = Some("SJL"),
    sivulaji = Some("Maila"),
    keskiarvo = Some("8.5"),
    tamakausi = Some("Mestis"),
    peruskoulu = Some("Kallion peruskoulu"),
    viimekausi = Some("II div"),
    toissakausi = Some("III div"),
    valmentaja_puh = Some("+358500000001"),
    valmentaja_nimi = Some("Lukio Valmentaja"),
    valmentaja_email = Some("vlk@example.test"),
    valmennusryhma_maajoukkue = Some("ei"),
    valmennusryhma_piirijoukkue = Some("kyllä"),
    valmennusryhma_seurajoukkue = Some("kyllä")
  )
  val ammatillisetKysymykset = UrheilijanLisakysymykset(
    laji = Some("Jalkapallo"),
    seura = Some("HJK"),
    liitto = Some("SPL"),
    sivulaji = Some("Futsal"),
    keskiarvo = Some("9.0"),
    tamakausi = Some("Liiga"),
    peruskoulu = Some("Espoon peruskoulu"),
    viimekausi = Some("Ykkönen"),
    toissakausi = Some("Kakkonen"),
    valmentaja_puh = Some("+358500000002"),
    valmentaja_nimi = Some("Amm Valmentaja"),
    valmentaja_email = Some("vam@example.test"),
    valmennusryhma_maajoukkue = Some("kyllä"),
    valmennusryhma_piirijoukkue = Some("ei"),
    valmennusryhma_seurajoukkue = Some("kyllä")
  )

  val TERVEYS                     = true
  val AIEMPI_PERUMINEN            = false
  val KAKSOISTUTKINTO             = true
  val VALINTATIETO                = "HYVAKSYTTY"
  val VASTAANOTTOTIETO            = "VASTAANOTTANUT_SITOVASTI"
  val ILMOITTAUTUMISEN_TILA       = "LASNA_KOKO_LUKUVUOSI"
  val TOINENASTE_HAKUKOHTEET_JSON =
    s"""[{"oid":"$HAKUKOHDE_OID","terveys":$TERVEYS,"aiempiPeruminen":$AIEMPI_PERUMINEN,"kiinnostunutKaksoistutkinnosta":$KAKSOISTUTKINTO}]"""

  val LINJA_URHEILIJA_LUKIO_JSON    = """{"linja":"lukiopainotukset_0105"}"""
  val LINJA_TAVALLINEN_JSON         = """{"linja":"jokumuu"}"""
  val ETUNIMET                      = "Matti Ilmari"
  val KUTSUMANIMI                   = "Matti"
  val SUKUNIMI                      = "Meikäläinen"
  val HETU                          = "010101A1234"
  val KOTIKUNTA                     = "091"
  val SUKUPUOLI                     = 1
  val KANSALAISUUS_JSON             = """["246"]"""
  val KOULUTUSMARKKINOINTILUPA      = true
  val KIINNOSTUNUT_OPPISOPIMUKSESTA = false
  val SAHKOINENVIESTINTALUPA        = true
  val VALINTATULOKSEN_JULKAISULUPA  = true
  val JATETTY                       = OffsetDateTime.parse("2025-08-01T10:00:30+03:00")
  val MUOKATTU                      = OffsetDateTime.parse("2025-08-13T14:52:14+03:00")
}
