package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.external.toisenasteenhakijat.ExternalToisenAsteenHakijatTestData.*
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import org.junit.jupiter.api.{BeforeEach, Test}
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, jsonPath, status}
import org.hamcrest.Matchers.*
import slick.jdbc.H2Profile.api.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(Array("test"))
@WithMockUser(
  username = "testuser",
  roles = Array("APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA_1.2.246.562.10.00000000001")
)
class ExternalToisenAsteenHakijatControllerTest extends ExternalToisenAsteenHakijatTestUtils {

  @Autowired
  private val mvc: MockMvc = null

  @Autowired
  override val db: ReadOnlyDatabase = null

  @BeforeEach
  def clearDb(): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "Drop everything")
  }

  private def get(
    hakuOid: String = HAKU_OID,
    hakukohdeOid: Option[String] = Some(HAKUKOHDE_OID),
    organisaatioOid: Option[String] = None
  )(mutator: MockHttpServletRequestBuilder => MockHttpServletRequestBuilder = identity): ResultActions = {
    var req = MockMvcRequestBuilders
      .get("/api/external/toisenasteenhakijat")
      .param("hakuOid", hakuOid)
      .accept(MediaType.APPLICATION_JSON)
    hakukohdeOid.foreach(v => req = req.param("hakukohdeOid", v))
    organisaatioOid.foreach(v => req = req.param("organisaatioOid", v))
    mvc.perform(mutator(req))
  }

  @Test
  @WithAnonymousUser
  def returns401WhenNoUser(): Unit = {
    get()()
      .andExpect(status.isUnauthorized)
      .andExpect(content.string(""))
  }

  @Test
  @WithMockUser(username = "testuser", roles = Array("USER"))
  def returns403WhenUserMissingRole(): Unit = {
    get()()
      .andExpect(status.isForbidden)
      .andExpect(content.string(""))
  }

  @ParameterizedTest
  @ValueSource(strings = Array("not-oid", "1.2", "1.2.246", "1.2.246.1", "1.2.247.1.1"))
  def returns400WhenHakuOidNotOid(hakuOid: String): Unit = {
    get(hakuOid = hakuOid)()
      .andExpect(status.isBadRequest)
      .andExpect(
        content.json(
          """{"status": 400, "message": "virhe.validointi", "details": ["hakuOid.invalid.oid"] }"""
        )
      )
  }

  @Test
  def returns400WhenHakukohdeOidNotOid(): Unit = {
    get(hakukohdeOid = Some("not-oid"))()
      .andExpect(status.isBadRequest)
      .andExpect(
        content.json(
          """{"status": 400, "message": "virhe.validointi", "details": ["hakukohdeOid.invalid.oid"] }"""
        )
      )
  }

  @Test
  def returns400WhenOrganisaatioOidNotOrganisaatioOid(): Unit = {
    get(hakukohdeOid = None, organisaatioOid = Some("1.2.246.562.20.1"))()
      .andExpect(status.isBadRequest)
      .andExpect(
        content.json(
          """{"status": 400, "message": "virhe.validointi", "details": ["organisaatioOid.invalid.org"] }"""
        )
      )
  }

  @Test
  def returns400WhenNeitherHakukohdeNorOrganisaatioProvided(): Unit = {
    get(hakukohdeOid = None, organisaatioOid = None)()
      .andExpect(status.isBadRequest)
      .andExpect(
        content.json(
          """{"status": 400, "message": "virhe.validointi", "details": ["hakukohdeOid_or_organisaatioOid.required"] }"""
        )
      )
  }

  @Test
  def returns500WhenDatabaseError(): Unit = {
    get()()
      .andExpect(status.isInternalServerError)
      .andExpect(content.json("\"virhe.tietokanta\""))
  }

  @Test
  def returnsEmptyListWhenNoHakijatMatch(): Unit = {
    initSchema()

    get()()
      .andExpect(status.isOk)
      .andExpect(content.json("""{"hakijat": []}"""))
  }

  @Test
  def returnsHakijaFilteredByHakukohdeOid(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertToteutusJaKoulutus()

    get()()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
      .andExpect(jsonPath("$.hakijat[0].sahkoposti").value(EMAIL))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakemusnumero").value(HAKEMUS_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].hakukohdeOid").value(HAKUKOHDE_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].opetuspiste").value(ORGANISAATIO_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].koulutus.koodiarvo").value("621702"))
  }

  @Test
  def returnsHakijaFilteredByOrganisaatioOid(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()

    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
  }

  @Test
  def placeholderFieldsArePresentAsNullOrEmpty(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()

    get()()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hetu").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].sukunimi").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].etunimet").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].kutsumanimi").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].huoltaja1").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].huoltaja2").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet").isEmpty)
      .andExpect(jsonPath("$.hakijat[0].lisakysymykset").isEmpty)
      .andExpect(jsonPath("$.hakijat[0].hakemus.vuosi").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.kausi").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.osaaminen.yleinen_kielitutkinto_fi").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].terveys").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].urheilijanLisakysymykset").value(nullValue()))
  }

  @Test
  def excludesShortHakemusOid(): Unit = {
    initSchema()
    insertHakemus(hakemusOid = "1.2.246.562.11.3511892")
    insertHakukohde()
    insertHakutoive(hakemusOid = "1.2.246.562.11.3511892")

    get()()
      .andExpect(status.isOk)
      .andExpect(content.json("""{"hakijat": []}"""))
  }

  @Test
  def excludesHaunkohdejoukkoOtherThan11(): Unit = {
    initSchema()
    insertHenkilo()
    insertHaku(kohdejoukkoKoodiuri = "haunkohdejoukko_12#1")
    insertHakemus(insertHenkilo = false, insertHaku = false)
    insertHakukohde()
    insertHakutoive()

    get()()
      .andExpect(status.isOk)
      .andExpect(content.json("""{"hakijat": []}"""))
  }
}
