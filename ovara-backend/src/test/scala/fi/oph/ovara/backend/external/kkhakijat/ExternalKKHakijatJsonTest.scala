package fi.oph.ovara.backend.external.kkhakijat

import com.fasterxml.jackson.databind.JsonNode
import fi.oph.ovara.backend.utils.JsonConfiguration
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters.*

// Rajapinnan JSON-muodon sopimus: kenttien järjestys on kiinteä ja null-arvoiset kentät
// jätetään pois. Testi sarjallistaa vastauksen samalla ObjectMapperilla kuin sovellus.
class ExternalKKHakijatJsonTest extends AnyFlatSpec with Matchers {

  private val mapper = new JsonConfiguration().objectMapper()

  private def keys(node: JsonNode): Seq[String] = node.fieldNames().asScala.toSeq

  private def serialize(hakija: KKHakija): JsonNode =
    mapper.readTree(mapper.writeValueAsString(KKHakijatResponse(Seq(hakija)))).get("hakijat").get(0)

  private val taysiHakija = KKHakija(
    hetu = "020202B4321",
    oppijanumero = "1.2.246.562.24.00000000019",
    sukunimi = "Yliopistolainen",
    etunimet = "Jaakko Petteri",
    kutsumanimi = "Jaakko",
    lahiosoite = "Yliopistokatu 5",
    postinumero = "00200",
    postitoimipaikka = "Helsinki",
    maa = "246",
    kotikunta = "091",
    sukupuoli = "1",
    aidinkieli = "FI",
    asiointikieli = "FI",
    kansalaisuus = Some("246"),
    kaksoiskansalaisuus = Some("752"),
    kansalaisuudet = Some(List("246", "752")),
    syntymaaika = Some("15.04.2005"),
    matkapuhelin = Some("+358409999999"),
    puhelin = Some("+35891234567"),
    sahkoposti = Some("kk-oppija@example.test"),
    koulusivistyskieli = Some("FI"),
    koulusivistyskielet = Some(Seq("FI", "SV")),
    koulutusmarkkinointilupa = Some(true),
    onYlioppilas = true,
    yoSuoritusVuosi = Some("2024"),
    turvakielto = false,
    hakemukset = Seq(
      KKHakutoive(
        haku = "1.2.246.562.29.00000000000000000200",
        hakuVuosi = 2026,
        hakuKausi = "S",
        hakemusnumero = "1.2.246.562.11.00000000000004511892",
        organisaatio = "1.2.246.562.10.00000000000000000586",
        hakukohde = "1.2.246.562.20.00000000000000000112",
        hakemusViimeinenMuokkausAikaleima = Some("13.09.2025 14:52"),
        hakemusJattoAikaleima = Some("01.09.2025 10:00"),
        valinnanAikaleima = Some("01.06.2026 12:00"),
        hakutoivePrioriteetti = Some(1),
        hakukohdeKkId = Some("kk-id-1"),
        avoinVayla = Some(false),
        valinnanTila = Some(Valintatila.HYVAKSYTTY),
        valintatapajononTyyppi = Some("valintatapajono_yp"),
        valintatapajononNimi = Some("Yhteispisteet"),
        hyvaksymisenEhto = Some(
          HyvaksymisenEhto(
            ehdollisestiHyvaksyttavissa = true,
            ehtoKoodi = Some("muu"),
            ehtoFI = Some("ehto"),
            ehtoSV = Some("villkor"),
            ehtoEN = Some("condition")
          )
        ),
        vastaanottotieto = Some(Vastaanottotila.VASTAANOTTANUT_SITOVASTI),
        pisteet = Some(BigDecimal("12.5")),
        ilmoittautumiset = Seq(Lasnaolo.LASNA_KOKO_LUKUVUOSI),
        pohjakoulutus = Seq("pohjakoulutus_yo"),
        julkaisulupa = Some(true),
        hKelpoisuus = Some(Hakukelpoisuus.ELIGIBLE),
        hKelpoisuusLahde = Some("lahde"),
        hKelpoisuusMaksuvelvollisuus = Some(Maksuvelvollisuus.NOT_REQUIRED),
        lukuvuosimaksu = Some("EI_MAKSUVELVOLLINEN"),
        hakukohteenKoulutukset = Seq(
          KkHakukohteenkoulutus(
            komoOid = "1.2.246.562.13.00000000000000000122",
            koulutusKoodi = Some("koulutus_655101"),
            kkKoulutusId = Some("kk-id-1"),
            koulutuksenAlkamisvuosi = Some(2026),
            koulutuksenAlkamiskausi = Some("S"),
            johtaaTutkintoon = Some(true)
          )
        ),
        liitteet = Some(
          Seq(
            Liite(
              hakuId = Some("h"),
              hakuRyhmaId = Some("r"),
              tila = Some("t"),
              saapumisenTila = Some("s"),
              nimi = Some("n"),
              vastaanottaja = Some("v")
            )
          )
        )
      )
    ),
    ensikertalainen = Some(true)
  )

  private val minimiHakija = taysiHakija.copy(
    kansalaisuus = None,
    kaksoiskansalaisuus = None,
    kansalaisuudet = None,
    syntymaaika = None,
    matkapuhelin = None,
    puhelin = None,
    sahkoposti = None,
    koulusivistyskieli = None,
    koulusivistyskielet = None,
    koulutusmarkkinointilupa = None,
    yoSuoritusVuosi = None,
    ensikertalainen = None,
    hakemukset = Seq(
      KKHakutoive(
        haku = "1.2.246.562.29.00000000000000000200",
        hakuVuosi = 2026,
        hakuKausi = "S",
        hakemusnumero = "1.2.246.562.11.00000000000004511892",
        organisaatio = "1.2.246.562.10.00000000000000000586",
        hakukohde = "1.2.246.562.20.00000000000000000112",
        hakukohteenKoulutukset = Seq(KkHakukohteenkoulutus(komoOid = "1.2.246.562.13.00000000000000000122"))
      )
    )
  )

  "KK-hakijat JSON" should "list hakija fields in the fixed order" in {
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
      "kansalaisuus",
      "kaksoiskansalaisuus",
      "kansalaisuudet",
      "syntymaaika",
      "matkapuhelin",
      "puhelin",
      "sahkoposti",
      "kotikunta",
      "sukupuoli",
      "aidinkieli",
      "asiointikieli",
      "koulusivistyskieli",
      "koulusivistyskielet",
      "koulutusmarkkinointilupa",
      "onYlioppilas",
      "yoSuoritusVuosi",
      "turvakielto",
      "hakemukset",
      "ensikertalainen"
    )
  }

  it should "list hakemus fields in the fixed order" in {
    keys(serialize(taysiHakija).get("hakemukset").get(0)) shouldBe Seq(
      "haku",
      "hakuVuosi",
      "hakuKausi",
      "hakemusnumero",
      "hakemusViimeinenMuokkausAikaleima",
      "hakemusJattoAikaleima",
      "valinnanAikaleima",
      "organisaatio",
      "hakukohde",
      "hakukohdeKkId",
      "hakutoivePrioriteetti",
      "avoinVayla",
      "valinnanTila",
      "valintatapajononTyyppi",
      "valintatapajononNimi",
      "hyvaksymisenEhto",
      "vastaanottotieto",
      "pisteet",
      "ilmoittautumiset",
      "pohjakoulutus",
      "julkaisulupa",
      "hKelpoisuus",
      "hKelpoisuusLahde",
      "hKelpoisuusMaksuvelvollisuus",
      "lukuvuosimaksu",
      "hakukohteenKoulutukset",
      "liitteet"
    )
  }

  it should "list nested object fields in the fixed order" in {
    val hakemus = serialize(taysiHakija).get("hakemukset").get(0)
    keys(hakemus.get("hyvaksymisenEhto")) shouldBe Seq(
      "ehdollisestiHyvaksyttavissa",
      "ehtoKoodi",
      "ehtoFI",
      "ehtoSV",
      "ehtoEN"
    )
    keys(hakemus.get("hakukohteenKoulutukset").get(0)) shouldBe Seq(
      "komoOid",
      "tkKoulutuskoodi",
      "kkKoulutusId",
      "koulutuksenAlkamiskausi",
      "koulutuksenAlkamisvuosi",
      "johtaaTutkintoon"
    )
    keys(hakemus.get("liitteet").get(0)) shouldBe Seq(
      "hakuId",
      "hakuRyhmaId",
      "tila",
      "saapumisenTila",
      "nimi",
      "vastaanottaja"
    )
  }

  it should "serialize values with their JSON types" in {
    val hakija  = serialize(taysiHakija)
    val hakemus = hakija.get("hakemukset").get(0)
    hakija.get("koulutusmarkkinointilupa").isBoolean shouldBe true
    hakija.get("kansalaisuudet").isArray shouldBe true
    hakemus.get("hakutoivePrioriteetti").isInt shouldBe true
    hakemus.get("pisteet").decimalValue() shouldBe new java.math.BigDecimal("12.5")
    hakemus.get("hKelpoisuus").asText shouldBe "ELIGIBLE"
    hakemus.get("hKelpoisuusMaksuvelvollisuus").asText shouldBe "NOT_REQUIRED"
  }

  it should "omit null fields but keep empty lists and empty strings" in {
    val hakija  = serialize(minimiHakija)
    val hakemus = hakija.get("hakemukset").get(0)

    keys(hakija) shouldBe Seq(
      "hetu",
      "oppijanumero",
      "sukunimi",
      "etunimet",
      "kutsumanimi",
      "lahiosoite",
      "postinumero",
      "postitoimipaikka",
      "maa",
      "kotikunta",
      "sukupuoli",
      "aidinkieli",
      "asiointikieli",
      "onYlioppilas",
      "turvakielto",
      "hakemukset"
    )
    keys(hakemus) shouldBe Seq(
      "haku",
      "hakuVuosi",
      "hakuKausi",
      "hakemusnumero",
      "organisaatio",
      "hakukohde",
      "ilmoittautumiset",
      "pohjakoulutus",
      "hKelpoisuus",
      "hakukohteenKoulutukset"
    )
    hakemus.get("ilmoittautumiset").size shouldBe 0
    hakemus.get("pohjakoulutus").size shouldBe 0
    hakemus.get("hKelpoisuus").asText shouldBe ""
    keys(hakemus.get("hakukohteenKoulutukset").get(0)) shouldBe Seq("komoOid")
  }
}
