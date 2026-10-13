package fi.oph.ovara.backend.external.toisenasteenhakijat

import com.fasterxml.jackson.databind.JsonNode
import fi.oph.ovara.backend.domain.{En, Fi, Sv}
import fi.oph.ovara.backend.utils.JsonConfiguration
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.OffsetDateTime
import scala.jdk.CollectionConverters.*

// Rajapinnan JSON-muodon sopimus: kenttien järjestys on kiinteä ja null-arvoiset kentät
// jätetään pois. Testi sarjallistaa vastauksen samalla ObjectMapperilla kuin sovellus.
class ExternalToisenAsteenHakijatJsonTest extends AnyFlatSpec with Matchers {

  private val mapper = new JsonConfiguration().objectMapper()

  private def keys(node: JsonNode): Seq[String] = node.fieldNames().asScala.toSeq

  private def serialize(hakija: ToisenAsteenHakija): JsonNode =
    mapper.readTree(mapper.writeValueAsString(HakijatResponse(Seq(hakija)))).get("hakijat").get(0)

  private val taysiHakutoive = HakijaHakutoive(
    hakukohdeOid = "1.2.246.562.20.00000000000000000012",
    hakujno = 1,
    oppilaitos = Some("01234"),
    opetuspiste = Some("1.2.246.562.10.00000000000000000586"),
    opetuspisteennimi = Some(Map(Fi -> "Koulu", Sv -> "Skola", En -> "School")),
    koulutus = Some(
      KoodistoArvo(
        versioituUri = "koulutus_351101#12",
        koodiarvo = "351101",
        koodistoUri = "koulutus",
        koodistoVersio = 12,
        nimi = Map(Fi -> "Koulutus")
      )
    ),
    harkinnanvaraisuusperuste = Some("ATARU_OPPIMISVAIKEUDET"),
    urheilijanammatillinenkoulutus = Some(true),
    yhteispisteet = Some(BigDecimal("18.25")),
    valinta = Some("HYVAKSYTTY"),
    vastaanotto = Some("VASTAANOTTANUT_SITOVASTI"),
    lasnaolo = Some("LASNA"),
    terveys = Some(false),
    aiempiperuminen = Some(false),
    kaksoistutkinto = Some(true),
    keskiarvo = Some("8.5"),
    urheilijanLisakysymykset = Some(
      UrheilijanLisakysymykset(
        peruskoulu = Some("a"),
        keskiarvo = Some("b"),
        tamakausi = Some("c"),
        viimekausi = Some("d"),
        toissakausi = Some("e"),
        sivulaji = Some("f"),
        valmennusryhma_seurajoukkue = Some("g"),
        valmennusryhma_piirijoukkue = Some("h"),
        valmennusryhma_maajoukkue = Some("i"),
        valmentaja_nimi = Some("j"),
        valmentaja_email = Some("k"),
        valmentaja_puh = Some("l"),
        laji = Some("m"),
        liitto = Some("n"),
        seura = Some("o")
      )
    )
  )

  private val taysiHakija = ToisenAsteenHakija(
    oppijanumero = "1.2.246.562.24.00000000001",
    sahkoposti = "oppija@example.test",
    matkapuhelin = "+358401234567",
    lahiosoite = "Koulukatu 1",
    postinumero = "00100",
    postitoimipaikka = "Helsinki",
    hakemus = HakijaHakemus(
      hakemusnumero = "1.2.246.562.11.00000000000003511892",
      hakutoiveet = Seq(taysiHakutoive),
      vuosi = Some("2026"),
      kausi = Some("S"),
      hakemuksenJattopaiva = Some(OffsetDateTime.parse("2025-03-01T10:00:30+02:00")),
      hakemuksenMuokkauspaiva = Some(OffsetDateTime.parse("2025-03-13T14:52:14+02:00")),
      lahtokoulu = Some("01234"),
      lahtokoulunnimi = Some("Peruskoulu"),
      luokka = Some("9A"),
      luokkataso = Some("9"),
      pohjakoulutus = Some("1"),
      todistusvuosi = Some("2025"),
      julkaisulupa = Some(true),
      yhteisetaineet = Some(BigDecimal("8.0")),
      lukiontasapisteet = Some(BigDecimal("9.0")),
      lisapistekoulutus = Some("LISAKOULUTUS_KYMPPI"),
      yleinenkoulumenestys = Some(BigDecimal("8.5")),
      painotettavataineet = Some(BigDecimal("9.5")),
      osaaminen = Osaaminen(
        yleinen_kielitutkinto_fi = Some("1"),
        valtionhallinnon_kielitutkinto_fi = Some("2"),
        yleinen_kielitutkinto_sv = Some("3"),
        valtionhallinnon_kielitutkinto_sv = Some("4"),
        yleinen_kielitutkinto_en = Some("5"),
        valtionhallinnon_kielitutkinto_en = Some("6")
      )
    ),
    hetu = Some("010101A123B"),
    sukunimi = Some("Oppija"),
    etunimet = Some("Olli Oskari"),
    kutsumanimi = Some("Olli"),
    maa = Some("246"),
    kansalaisuudet = Seq("246"),
    muupuhelin = Some("+35891234567"),
    kotikunta = Some("091"),
    sukupuoli = Some("1"),
    aidinkieli = Some("FI"),
    opetuskieli = Some("FI"),
    koulutusmarkkinointilupa = Some(true),
    kiinnostunutoppisopimuksesta = Some(false),
    huoltaja1 = Some(Huoltaja("Hanna", "Huoltaja", "+358401111111", "hanna@example.test")),
    huoltaja2 = Some(Huoltaja("Heikki", "Huoltaja", "+358402222222", "heikki@example.test")),
    oikeusMaksuttomaanKoulutukseenVoimassaAsti = Some("2028-12-31"),
    oppivelvollisuusVoimassaAsti = Some("2027-12-31"),
    sahkoisenAsioinninLupa = Some(true),
    lisakysymykset = Seq(
      Lisakysymys(
        kysymysid = "k1",
        hakukohdeOids = Seq("1.2.246.562.20.00000000000000000012"),
        kysymystyyppi = "singleChoice",
        kysymysteksti = "Kysymys?",
        vastaukset = Seq(LisakysymysVastaus("v1", "Kyllä"))
      )
    )
  )

  private val minimiHakija = ToisenAsteenHakija(
    oppijanumero = "1.2.246.562.24.00000000001",
    sahkoposti = "oppija@example.test",
    matkapuhelin = "+358401234567",
    lahiosoite = "Koulukatu 1",
    postinumero = "00100",
    postitoimipaikka = "Helsinki",
    hakemus = HakijaHakemus(
      hakemusnumero = "1.2.246.562.11.00000000000003511892",
      hakutoiveet = Seq(HakijaHakutoive(hakukohdeOid = "1.2.246.562.20.00000000000000000012", hakujno = 1))
    )
  )

  "Toisen asteen hakijat JSON" should "list hakija fields in the fixed order" in {
    keys(serialize(taysiHakija)) shouldBe Seq(
      "hetu",
      "oppijanumero",
      "sukunimi",
      "etunimet",
      "kutsumanimi",
      "lahiosoite",
      "postinumero",
      "postitoimipaikka",
      "maa",
      "kansalaisuudet",
      "matkapuhelin",
      "muupuhelin",
      "sahkoposti",
      "kotikunta",
      "sukupuoli",
      "aidinkieli",
      "opetuskieli",
      "koulutusmarkkinointilupa",
      "kiinnostunutoppisopimuksesta",
      "huoltaja1",
      "huoltaja2",
      "hakemus",
      "sahkoisenAsioinninLupa",
      "oppivelvollisuusVoimassaAsti",
      "oikeusMaksuttomaanKoulutukseenVoimassaAsti",
      "lisakysymykset"
    )
  }

  it should "list huoltaja fields in the fixed order" in {
    keys(serialize(taysiHakija).get("huoltaja1")) shouldBe Seq("etunimi", "sukunimi", "puhelinnumero", "sahkoposti")
  }

  it should "list hakemus fields in the fixed order" in {
    keys(serialize(taysiHakija).get("hakemus")) shouldBe Seq(
      "vuosi",
      "kausi",
      "hakemusnumero",
      "hakemuksenJattopaiva",
      "hakemuksenMuokkauspaiva",
      "lahtokoulu",
      "lahtokoulunnimi",
      "luokka",
      "luokkataso",
      "pohjakoulutus",
      "todistusvuosi",
      "julkaisulupa",
      "yhteisetaineet",
      "lukiontasapisteet",
      "yleinenkoulumenestys",
      "lisapistekoulutus",
      "painotettavataineet",
      "hakutoiveet",
      "osaaminen"
    )
  }

  it should "list hakutoive fields in the fixed order" in {
    keys(serialize(taysiHakija).get("hakemus").get("hakutoiveet").get(0)) shouldBe Seq(
      "hakukohdeOid",
      "hakujno",
      "oppilaitos",
      "opetuspiste",
      "opetuspisteennimi",
      "koulutus",
      "harkinnanvaraisuusperuste",
      "urheilijanammatillinenkoulutus",
      "yhteispisteet",
      "valinta",
      "vastaanotto",
      "lasnaolo",
      "terveys",
      "aiempiperuminen",
      "kaksoistutkinto",
      "keskiarvo",
      "urheilijanLisakysymykset"
    )
  }

  it should "list nested object fields in the fixed order" in {
    val hakija    = serialize(taysiHakija)
    val hakutoive = hakija.get("hakemus").get("hakutoiveet").get(0)
    keys(hakutoive.get("urheilijanLisakysymykset")) shouldBe Seq(
      "peruskoulu",
      "keskiarvo",
      "tamakausi",
      "viimekausi",
      "toissakausi",
      "sivulaji",
      "valmennusryhma_seurajoukkue",
      "valmennusryhma_piirijoukkue",
      "valmennusryhma_maajoukkue",
      "valmentaja_nimi",
      "valmentaja_email",
      "valmentaja_puh",
      "laji",
      "liitto",
      "seura"
    )
    keys(hakutoive.get("koulutus")) shouldBe Seq("versioituUri", "koodiarvo", "koodistoUri", "koodistoVersio", "nimi")
    keys(hakija.get("hakemus").get("osaaminen")) shouldBe Seq(
      "yleinen_kielitutkinto_fi",
      "valtionhallinnon_kielitutkinto_fi",
      "yleinen_kielitutkinto_sv",
      "valtionhallinnon_kielitutkinto_sv",
      "yleinen_kielitutkinto_en",
      "valtionhallinnon_kielitutkinto_en"
    )
    keys(hakija.get("lisakysymykset").get(0)) shouldBe Seq(
      "kysymysid",
      "hakukohdeOids",
      "kysymystyyppi",
      "kysymysteksti",
      "vastaukset"
    )
    keys(hakija.get("lisakysymykset").get(0).get("vastaukset").get(0)) shouldBe Seq("vastausid", "vastausteksti")
  }

  it should "serialize values with their JSON types" in {
    val hakemus   = serialize(taysiHakija).get("hakemus")
    val hakutoive = hakemus.get("hakutoiveet").get(0)
    hakemus.get("hakemuksenJattopaiva").asText shouldBe "2025-03-01T10:00:30+02:00"
    hakemus.get("julkaisulupa").isBoolean shouldBe true
    hakemus.get("yleinenkoulumenestys").decimalValue() shouldBe new java.math.BigDecimal("8.5")
    hakutoive.get("yhteispisteet").decimalValue() shouldBe new java.math.BigDecimal("18.25")
    hakutoive.get("hakujno").isInt shouldBe true
    hakutoive.get("opetuspisteennimi").get("sv").asText shouldBe "Skola"
  }

  it should "omit null fields but keep empty lists and empty objects" in {
    val hakija    = serialize(minimiHakija)
    val hakemus   = hakija.get("hakemus")
    val hakutoive = hakemus.get("hakutoiveet").get(0)

    keys(hakija) shouldBe Seq(
      "oppijanumero",
      "lahiosoite",
      "postinumero",
      "postitoimipaikka",
      "kansalaisuudet",
      "matkapuhelin",
      "sahkoposti",
      "hakemus",
      "lisakysymykset"
    )
    keys(hakemus) shouldBe Seq("hakemusnumero", "hakutoiveet", "osaaminen")
    keys(hakutoive) shouldBe Seq("hakukohdeOid", "hakujno")
    hakija.get("kansalaisuudet").size shouldBe 0
    hakija.get("lisakysymykset").size shouldBe 0
    keys(hakemus.get("osaaminen")) shouldBe empty
  }
}
