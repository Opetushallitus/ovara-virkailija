package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.valpas.ValpasFactory.*
import org.junit.jupiter.api.{BeforeEach, Nested, Test}
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.{WithAnonymousUser, WithMockUser}
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.{MockMvc, ResultActions}
import org.springframework.test.web.servlet.request.{MockHttpServletRequestBuilder, MockMvcRequestBuilders}
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, status}
import slick.jdbc.H2Profile.api.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(Array("test"))
@WithMockUser(username = "testuser", roles = Array("APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA_1.2.246.562.10.00000000001"))
class ValpasControllerTest extends ValpasTestUtils {

  @Autowired
  private val mvc: MockMvc = null

  @Autowired
  override val db: ReadOnlyDatabase = null

  @BeforeEach
  def clearDb(): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "Drop everything")
  }

  @Nested
  class SingleValpas {
    private def get(
      oppijanumero: String
    )(mutator: MockHttpServletRequestBuilder => MockHttpServletRequestBuilder): ResultActions = {
      mvc.perform(
        mutator(
          MockMvcRequestBuilders
            .get("/api/valpas")
            .param("ovara_oppijanumero", oppijanumero)
            .accept(MediaType.APPLICATION_JSON)
        )
      )
    }

    private def get(mutator: MockHttpServletRequestBuilder => MockHttpServletRequestBuilder): ResultActions =
      get(OPPIJANUMERO)(mutator)

    private def get(oppijanumero: String): ResultActions = get(oppijanumero)(a => a)

    private def get(): ResultActions = get(OPPIJANUMERO)(a => a)

    @Test
    @WithAnonymousUser
    def returns401WhenNoUser(): Unit = {
      get()
        .andExpect(status.isUnauthorized)
        .andExpect(content.string(""))
    }

    @Test
    @WithMockUser(username = "testuser", roles = Array("USER"))
    def returns403WhenUserMissingRole(): Unit = {
      get()
        .andExpect(status.isForbidden)
        .andExpect(content.string(""))
    }

    @ParameterizedTest
    @ValueSource(strings = Array("not-oid", "1.2", "1.2.246", "1.2.246.1", "1.2.247.1.1"))
    def returns400WhenParameterNotOid(oppijanumero: String): Unit = {
      get(oppijanumero)
        .andExpect(status.isBadRequest)
        .andExpect(
          content.json(
            """{"status": 400, "message": "virhe.validointi", "details": ["ovara_oppijanumero.invalid.oid"] }"""
          )
        )
    }

    @Test
    def returns500WhenDatabaseError(): Unit = {
      get()
        .andExpect(status().isInternalServerError)
        .andExpect(content().json("\"virhe.tietokanta\""))
    }

    @Test
    def returnsEmptyArrayWhenHakemusNotFound(): Unit = {
      initSchema()

      get()
        .andExpect(status.isOk)
        .andExpect(content.json("[]"))
    }

    @Test
    def returnsHakemusWhenNoHakutoive(): Unit = {
      initSchema()
      insertHakemus()

      get()
        .andExpect(status.isOk)
        .andExpect(content.json(expectedHakemus()))
    }

    @Test
    def returnsHakemusWithHakutoive(): Unit = {
      initSchema()
      insertHakemus()
      insertHakutoive()

      get()
        .andExpect(status.isOk)
        .andExpect(content.json(expectedHakemus(expectedHakutoive)))
    }

    @Test
    def returnsOnlyActiveHaut(): Unit = {
      initSchema()
      insertHakemus()
      insertHakutoive()

      get(_.param("ovara_vain_aktiiviset", "true"))
        .andExpect(status.isOk)
        .andExpect(content.json(expectedHakemus(expectedHakutoive)))
    }
  }

  @Nested
  class ManyValpas {
    private def post(oppijanumero: String = OPPIJANUMERO) = {
      mvc.perform(
        MockMvcRequestBuilders
          .post("/api/valpas")
          .content(s"""["$oppijanumero"]""")
          .accept(MediaType.APPLICATION_JSON)
          .contentType(MediaType.APPLICATION_JSON)
      )
    }

    @Test
    @WithAnonymousUser
    def returns401WhenNoUser(): Unit = {
      post()
        .andExpect(status.isUnauthorized)
        .andExpect(content.string(""))
    }

    @Test
    @WithMockUser(username = "testuser", roles = Array("USER"))
    def returns403WhenUserMissingRole(): Unit = {
      post()
        .andExpect(content.string(""))
        .andExpect(status.isForbidden)
    }

    @ParameterizedTest
    @ValueSource(strings = Array("not-oid", "1.2", "1.2.246", "1.2.246.1", "1.2.247.1.1"))
    def returns400WhenParameterNotOid(oppijanumero: String): Unit = {
      post(oppijanumero)
        .andExpect(status.isBadRequest)
        .andExpect(
          content.json(
            """{"status": 400, "message": "virhe.validointi", "details": ["ovara_oppijanumero.invalid.oid"] }"""
          )
        )
    }
    @Test
    def returns500WhenDatabaseError(): Unit = {
      post()
        .andExpect(status().isInternalServerError)
        .andExpect(content().json("\"virhe.tietokanta\""))
    }

    @Test
    def returnsEmptyArrayWhenHakemusNotFound(): Unit = {
      initSchema()

      post()
        .andExpect(status.isOk)
        .andExpect(content.json("[]"))
    }

    @Test
    def returnsHakemusWhenNoHakutoive(): Unit = {
      initSchema()
      insertHakemus()

      post()
        .andExpect(status.isOk)
        .andExpect(content.json(expectedHakemus()))
    }

    @Test
    def returnsHakemusWithHakutoive(): Unit = {
      initSchema()
      insertHakemus()
      insertHakutoive()

      post()
        .andExpect(status.isOk)
        .andExpect(content.json(expectedHakemus(expectedHakutoive)))
    }

  }

  private def expectedHakemus(hakutoive: String = "") =
    s"""
       |[ {
       |  "hakemusOid" : "1.2.246.562.11.580",
       |  "hakemusUrl" : "https://virkailija.testiopintopolku.fi/lomake-editori/applications/search?term=1.2.246.562.11.580",
       |  "hakemuksenMuokkauksenAikaleima" : "2025-08-13T14:52:14+03:00",
       |  "email" : "oppija@example.test",
       |  "matkapuhelin" : "+358401234567",
       |  "lahiosoite" : "Katu 1",
       |  "postinumero" : "00100",
       |  "postitoimipaikka" : "Helsinki",
       |  "maa" : {
       |    "versioituUri" : "maatjavaltiot2_246#2",
       |    "koodiarvo" : "246",
       |    "koodistoUri" : "maatjavaltiot2",
       |    "koodistoVersio" : 2,
       |    "nimi" : {
       |      "fi" : "Suomi",
       |      "sv" : "Finland",
       |      "en" : "Finland"
       |    }
       |  },
       |  "hakuOid" : "1.2.246.562.29.100",
       |  "hakuNimi" : {
       |    "fi" : "Yhteishaku",
       |    "sv" : "Gemensamma",
       |    "en" : "Joint application"
       |  },
       |  "hakutapa" : {
       |    "versioituUri" : "hakutapa_03#1",
       |    "koodiarvo" : "03",
       |    "koodistoUri" : "hakutapa",
       |    "koodistoVersio" : 1,
       |    "nimi" : {
       |      "fi" : "Jatkuva haku",
       |      "sv" : "Kontinuerlig ansökan",
       |      "en" : "Rolling admission (upper secondary level)"
       |    }
       |  },
       |  "haunAlkamispaivamaara" : "2022-08-31T23:59:00",
       |  "oppijaOid" : "1.2.246.562.24.9",
       |  "hakutoiveet" : [ $hakutoive ]
       |} ]""".stripMargin

  private val expectedHakutoive =
    s"""
       |{
       |    "hakukohdeOid" : "$HAKUKOHDE_OID",
       |    "hakukohdeNimi" : {
       |      "fi" : "Elokuvaleikkaus",
       |      "sv" : "Filmklippning",
       |      "en" : "Film Editing"
       |    },
       |    "hakutoivenumero" : 3,
       |    "hakukohdeOrganisaatio" : "$ORGANISAATIO_OID",
       |    "organisaatioNimi" : {
       |      "fi" : "Porin toimipaikka",
       |      "sv" : "Björneborg verksamhetspunkt"
       |    },
       |    "koulutusOid" : "$KOULUTUS_OID",
       |    "koulutusNimi" : {
       |      "fi" : "Kulttuurituottaja",
       |      "sv" : "Kulturproducent",
       |      "en" : "Kulttuurituottaja"
       |    },
       |    "hakukohdeKoulutuskoodi" : [ {
       |      "versioituUri" : "$KOULUTUS_KOODIURI",
       |      "koodiarvo" : "621702",
       |      "koodistoUri" : "koulutus",
       |      "koodistoVersio" : 12,
       |      "nimi" : {
       |        "fi" : "Kulttuurituottaja",
       |        "sv" : "Kulturproducent",
       |        "en" : "Bachelor of Culture and Arts, Cultural Manager"
       |      }
       |    } ],
       |    "harkinnanvaraisuus" : "EI_HARKINNANVARAINEN",
       |    "alinHyvaksyttyPistemaara" : 21.1,
       |    "pisteet" : 23.7,
       |    "varasijanumero" : 4
       |}""".stripMargin
}
