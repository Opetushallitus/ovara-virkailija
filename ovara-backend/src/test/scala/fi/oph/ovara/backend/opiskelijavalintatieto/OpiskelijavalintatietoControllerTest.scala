package fi.oph.ovara.backend.opiskelijavalintatieto

import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import org.junit.jupiter.api.{Nested, Test}
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.{WithAnonymousUser, WithMockUser}
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.{MockMvc, ResultActions}
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, status}

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(Array("test"))
@WithMockUser(username = "testuser", roles = Array("APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA_1.2.246.562.10.00000000001"))
class OpiskelijavalintatietoControllerTest extends OpiskelijavalintatietoTestUtils {
  @Autowired
  private val mvc: MockMvc = null

  @Autowired
  override val db: ReadOnlyDatabase = null

  @Nested
  class Get {
    private def get(oppijanumero: String = "1.2.246.562.24.9") = {
      mvc.perform(
        MockMvcRequestBuilders
          .get("/api/opiskelijavalintatiedot")
          .param("ovara_oppijanumero", oppijanumero)
          .accept(MediaType.APPLICATION_JSON)
      )
    }

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
    def returnsNotFoundWhenUserNotFound(): Unit = {
      initSchema()

      get()
        .andExpect(status.isNotFound)
        .andExpect(content.string(""))
    }

    @Test
    def returnsHenkiloWhenNoHakemukset(): Unit = {
      initSchema()
      insertHenkilo()

      get()
        .andExpect(status.isOk)
        .andExpect(content.json(expectedOppija))
    }

    @Test
    def returnsHenkiloWithHakemukset(): Unit = {
      initSchema()
      insertHenkilo()
      insertHakemusData()

      get()
        .andExpect(status.isOk)
        .andExpect(content.json(expectedOppijaWithHakemukset))
    }
  }

  @Nested
  class Post {
    private def post(oppijanumero: String = "1.2.246.562.24.9"): ResultActions =
      mvc.perform(
        MockMvcRequestBuilders
          .post("/api/opiskelijavalintatiedot")
          .content(s"""["$oppijanumero"]""")
          .accept(MediaType.APPLICATION_JSON)
          .contentType(MediaType.APPLICATION_JSON)
      )

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
        .andExpect(status.isForbidden)
        .andExpect(content.string(""))
    }

    @ParameterizedTest
    @ValueSource(strings = Array("not-oid", "1.2", "1.2.246", "1.2.246.1", "1.2.247.1.1"))
    def returns400WhenParameterNotOid(oppijanumero: String): Unit = {
      post(oppijanumero)
        .andExpect(status.isBadRequest)
        .andExpect(
          content.json("""{"status": 400, "message": "virhe.validointi", "details": ["oppijanumerot.invalid.oid"] }""")
        )
    }

    @Test
    def returnsEmptyListWhenUserNotFound(): Unit = {
      initSchema()

      post()
        .andExpect(status.isOk)
        .andExpect(content.json("[]"))
    }

    @Test
    def returnsHenkiloWhenNoHakemukset(): Unit = {
      initSchema()
      insertHenkilo()

      post()
        .andExpect(status.isOk)
        .andExpect(content.json(s"""[$expectedOppija]""".stripMargin))
    }

    @Test
    def returnsHenkiloWithHakemukset(): Unit = {
      initSchema()
      insertHenkilo()
      insertHakemusData()

      post()
        .andExpect(status.isOk)
        .andExpect(content.json(s"[$expectedOppijaWithHakemukset]"))
    }
  }

  private val expectedOppija =
    """{
      |  "oppijanumero" : "1.2.246.562.24.9",
      |  "hetu" : "080872W943L",
      |  "syntymaaika" : "1977-01-15",
      |  "sukunimi" : "Testinen",
      |  "etunimet" : "Toivo Taneli",
      |  "hakemukset" : [ ]
      |}""".stripMargin

  private val expectedOppijaWithHakemukset =
    """{
      |  "oppijanumero" : "1.2.246.562.24.9",
      |  "hetu" : "080872W943L",
      |  "syntymaaika" : "1977-01-15",
      |  "sukunimi" : "Testinen",
      |  "etunimet" : "Toivo Taneli",
      |  "hakemukset" : [ {
      |    "hakemusOid" : "1.2.246.562.11.580",
      |    "haku" : {
      |      "oid" : "1.2.246.562.29.001",
      |      "nimi" : {
      |        "fi" : "Korkeakoulujen yhteishaku",
      |        "sv" : "Högskolornas gemensamma",
      |        "en" : "Joint application to higher education"
      |      }
      |    },
      |    "haunKohdejoukko" : "haunkohdejoukko_12#1",
      |    "hakutapa" : "hakutapa_01#1",
      |    "hakutoiveet" : [ {
      |      "hakukohde" : {
      |        "oid" : "1.2.246.562.20.012",
      |        "nimi" : {
      |          "fi" : "Maisterihaku",
      |          "sv" : "Magisteransökan",
      |          "en" : "Master's Admission"
      |        }
      |      },
      |      "tarjoaja" : {
      |        "oid" : "1.2.246.562.10.486",
      |        "nimi" : {
      |          "fi" : "Bio- ja ympäristötieteellinen tiedekunta",
      |          "sv" : "Bio- och miljövetenskapliga fakulteten"
      |        }
      |      },
      |      "koulutuksenAlkamiskausiUri" : "kausi_s#1",
      |      "koulutuksenAlkamisvuosi" : 2022,
      |      "valinnanTila" : null,
      |      "vastaanotonTila" : null,
      |      "ilmoittautumisenTila" : null
      |    } ]
      |  } ]
      |}""".stripMargin
}
