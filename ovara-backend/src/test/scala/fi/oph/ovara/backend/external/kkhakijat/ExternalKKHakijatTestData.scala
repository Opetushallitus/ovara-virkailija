package fi.oph.ovara.backend.external.kkhakijat

import java.time.OffsetDateTime

object ExternalKKHakijatTestData {
  // KK-specific OIDs (distinct from 2Aste's test data to avoid mental confusion).
  val OPPIJANUMERO       = "1.2.246.562.24.00000000019"
  val HAKEMUS_OID        = "1.2.246.562.11.00000000000004511892"
  val HAKEMUS_OID_2      = "1.2.246.562.11.00000000000004511893"
  val HAKU_OID           = "1.2.246.562.29.00000000000000000200"
  val HAKUKOHDE_OID      = "1.2.246.562.20.00000000000000000112"
  val HAKUKOHDE_OID_2    = "1.2.246.562.20.00000000000000000113"
  val TOTEUTUS_OID       = "1.2.246.562.17.00000000000000000222"
  val KOULUTUS_OID       = "1.2.246.562.13.00000000000000000122"
  val ORGANISAATIO_OID   = "1.2.246.562.10.00000000000000000586"
  val ORGANISAATIO_OID_2 = "1.2.246.562.10.00000000000000000587"
  // Ylemmän tason organisaatio jota ei koskaan aseteta hakukohteen järjestyspaikaksi:
  // osuu hakijoihin vain jos hierarkialaajennus toimii.
  val KOULUTUSTOIMIJA_OID = "1.2.246.562.10.00000000000000000588"
  // Hakukohderyhmien oidit ovat 1.2.246.562.28 -alkuisia. Ryhmätiedot ovat vain
  // pub-skeemassa, jota näiden testien H2-kanta ei sisällä, joten laajennus stubataan.
  val HAKUKOHDERYHMA_OID = "1.2.246.562.28.00000000000000000012"

  val EMAIL        = "kk-oppija@example.test"
  val MATKAPUHELIN = "+358409999999"
  val LAHIOSOITE   = "Yliopistokatu 5"
  val POSTINUMERO  = "00200"
  val HELSINKI     = "Helsinki"
  val SUOMI_KOODI  = "246"

  val AIDINKIELI_RAW = "fi"
  val AIDINKIELI     = "FI"

  val SYNTYMAAIKA     = java.time.LocalDate.parse("2005-04-15")
  val SYNTYMAAIKA_STR = "2005-04-15"

  val KOULUTUKSEN_ALKAMISVUOSI    = 2026
  val VUOSI                       = 2026
  val KOULUTUKSEN_ALKAMISKAUSIURI = "kausi_s#1"
  val KAUSI                       = "S"

  val ETUNIMET                      = "Jaakko Petteri"
  val KUTSUMANIMI                   = "Jaakko"
  val SUKUNIMI                      = "Yliopistolainen"
  val HETU                          = "020202B4321"
  val KOTIKUNTA                     = "091"
  val SUKUPUOLI                     = 1
  val KANSALAISUUS_JSON             = """["246"]"""
  val KOULUTUSMARKKINOINTILUPA      = true
  val KIINNOSTUNUT_OPPISOPIMUKSESTA = false
  val SAHKOINENVIESTINTALUPA        = true
  val VALINTATULOKSEN_JULKAISULUPA  = true

  val JATETTY      = OffsetDateTime.parse("2025-09-01T10:00:30+03:00")
  val MUOKATTU     = OffsetDateTime.parse("2025-09-13T14:52:14+03:00")
  val JATETTY_STR  = "2025-09-01T10:00:30+03:00"
  val MUOKATTU_STR = "2025-09-13T14:52:14+03:00"

  val VALINTATIETO          = "HYVAKSYTTY"
  val VASTAANOTTOTIETO      = "VASTAANOTTANUT_SITOVASTI"
  val ILMOITTAUTUMISEN_TILA = "LASNA_KOKO_LUKUVUOSI"
}
