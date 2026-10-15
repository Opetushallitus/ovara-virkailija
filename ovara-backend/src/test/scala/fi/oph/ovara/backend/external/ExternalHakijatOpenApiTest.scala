package fi.oph.ovara.backend.external

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.{BeforeAll, Test, TestInstance}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import scala.jdk.CollectionConverters.*

// Varmistaa, että springdoc kuvaa ulkoisten hakijarajapintojen vastaukset samassa muodossa
// kuin ne oikeasti sarjallistetaan: ei Scalan Option-sisäisiä kenttiä (defined/empty),
// oikeat tyypit ja kiinteä kenttäjärjestys.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(Array("test"))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExternalHakijatOpenApiTest {

  @Autowired
  private val mvc: MockMvc = null

  private var schemas: JsonNode = null

  @BeforeAll
  def fetchApiDocs(): Unit = {
    val body = mvc
      .perform(MockMvcRequestBuilders.get("/v3/api-docs/ovara-apis"))
      .andExpect(status.isOk)
      .andReturn()
      .getResponse
      .getContentAsString
    schemas = new ObjectMapper().readTree(body).at("/components/schemas")
  }

  private def properties(schema: String): JsonNode = {
    val props = schemas.at(s"/$schema/properties")
    assertFalse(props.isMissingNode, s"schema $schema puuttuu")
    props
  }

  private def propertyNames(schema: String): Seq[String] =
    properties(schema).fieldNames().asScala.toSeq

  private def property(schema: String, name: String): JsonNode = {
    val p = properties(schema).get(name)
    assertTrue(p != null, s"$schema.$name puuttuu")
    p
  }

  private def assertNoOptionInternals(schema: String): Unit =
    properties(schema).properties().asScala.foreach { entry =>
      val inner = entry.getValue.path("properties")
      assertFalse(
        inner.has("defined") || inner.has("empty"),
        s"$schema.${entry.getKey} kuvataan Scalan Optionina: ${entry.getValue}"
      )
    }

  private def assertType(schema: String, name: String, expected: String): Unit =
    assertEquals(expected, property(schema, name).path("type").asText, s"$schema.$name tyyppi")

  private def assertRef(schema: String, name: String, target: String): Unit =
    assertEquals(s"#/components/schemas/$target", property(schema, name).path("$ref").asText, s"$schema.$name viittaus")

  // ---- KK ----

  @Test
  def kkSchemasHaveNoOptionInternals(): Unit =
    Seq(
      "KKHakijaResponse",
      "KKHakemusResponse",
      "KkHakukohteenkoulutusResponse",
      "HyvaksymisenEhtoResponse",
      "LiiteResponse"
    ).foreach(assertNoOptionInternals)

  @Test
  def kkHakemusPropertyTypesAndNames(): Unit = {
    assertType("KKHakemusResponse", "valinnanAikaleima", "string")
    assertType("KKHakemusResponse", "pisteet", "number")
    assertType("KKHakemusResponse", "hakutoivePrioriteetti", "integer")
    assertType("KKHakemusResponse", "avoinVayla", "boolean")
    assertType("KKHakemusResponse", "liitteet", "array")
    assertRef("KKHakemusResponse", "hyvaksymisenEhto", "HyvaksymisenEhtoResponse")
    assertType("KKHakijaResponse", "kansalaisuudet", "array")
    assertType("KKHakijaResponse", "ensikertalainen", "boolean")

    val names = propertyNames("KKHakemusResponse")
    assertTrue(names.contains("hKelpoisuus"), names.toString)
    assertTrue(names.contains("hKelpoisuusLahde"), names.toString)
    assertTrue(names.contains("hKelpoisuusMaksuvelvollisuus"), names.toString)
    assertFalse(names.exists(_.startsWith("hkelpoisuus")), names.toString)
  }

  @Test
  def kkPropertyOrder(): Unit = {
    assertEquals(
      Seq(
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
      ),
      propertyNames("KKHakijaResponse")
    )
    assertEquals(
      Seq(
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
      ),
      propertyNames("KKHakemusResponse")
    )
    assertEquals(
      Seq(
        "komoOid",
        "tkKoulutuskoodi",
        "kkKoulutusId",
        "koulutuksenAlkamiskausi",
        "koulutuksenAlkamisvuosi",
        "johtaaTutkintoon"
      ),
      propertyNames("KkHakukohteenkoulutusResponse")
    )
  }

  // ---- Toinen aste ----

  @Test
  def toinenAsteSchemasHaveNoOptionInternals(): Unit =
    Seq(
      "HakijaResponse",
      "HuoltajaResponse",
      "HakijaHakemusResponse",
      "HakijaHakutoiveResponse",
      "UrheilijanLisakysymyksetResponse",
      "OsaaminenResponse",
      "LisakysymysResponse",
      "KoodistoArvoResponse"
    ).foreach(assertNoOptionInternals)

  @Test
  def toinenAstePropertyTypes(): Unit = {
    assertType("HakijaResponse", "hetu", "string")
    assertType("HakijaResponse", "sahkoisenAsioinninLupa", "boolean")
    assertRef("HakijaResponse", "huoltaja1", "HuoltajaResponse")
    assertRef("HakijaResponse", "hakemus", "HakijaHakemusResponse")
    assertType("HakijaHakemusResponse", "yhteisetaineet", "number")
    assertType("HakijaHakemusResponse", "hakemuksenJattopaiva", "string")
    assertEquals("date-time", property("HakijaHakemusResponse", "hakemuksenJattopaiva").path("format").asText)
    assertType("HakijaHakemusResponse", "julkaisulupa", "boolean")
    assertType("HakijaHakutoiveResponse", "yhteispisteet", "number")
    assertType("HakijaHakutoiveResponse", "terveys", "boolean")
    assertRef("HakijaHakutoiveResponse", "koulutus", "KoodistoArvoResponse")
    assertRef("HakijaHakutoiveResponse", "urheilijanLisakysymykset", "UrheilijanLisakysymyksetResponse")
  }

  @Test
  def toinenAstePropertyOrder(): Unit = {
    assertEquals(
      Seq(
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
      ),
      propertyNames("HakijaResponse")
    )
    assertEquals(
      Seq(
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
      ),
      propertyNames("HakijaHakemusResponse")
    )
    assertEquals(
      Seq(
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
      ),
      propertyNames("HakijaHakutoiveResponse")
    )
  }
}
