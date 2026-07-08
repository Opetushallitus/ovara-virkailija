package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.external.toisenasteenhakijat.ExternalToisenAsteenHakijatTestData.*
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.utils.{AuditLog, AuditOperation}
import fi.vm.sade.auditlog.Operation
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.{BeforeEach, Test}
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.{CsvSource, ValueSource}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Import, Primary}
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.{WithAnonymousUser, WithMockUser}
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.{MockMvc, ResultActions}
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, header, jsonPath, status}
import org.hamcrest.Matchers.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import slick.jdbc.H2Profile.api.*

import java.io.ByteArrayInputStream
import scala.jdk.CollectionConverters.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(Array("test"))
@Import(Array(classOf[ExternalToisenAsteenHakijatControllerTest.RecordingAuditConfig]))
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

  @BeforeEach
  def clearAuditRecord(): Unit = {
    ExternalToisenAsteenHakijatControllerTest.recordedAuditCalls.clear()
  }

  private def recordedAuditCalls: List[ExternalToisenAsteenHakijatControllerTest.AuditCall] =
    ExternalToisenAsteenHakijatControllerTest.recordedAuditCalls.asScala.toList

  private def get(
    hakuOid: String = HAKU_OID,
    hakukohdeOid: Option[String] = Some(HAKUKOHDE_OID),
    organisaatioOid: Option[String] = None,
    valintarajaus: Option[String] = Some("HAKENEET")
  ): ResultActions = {
    var req = MockMvcRequestBuilders
      .get("/api/external/toisenasteenhakijat")
      .param("hakuOid", hakuOid)
      .accept(MediaType.APPLICATION_JSON)
    hakukohdeOid.foreach(v => req = req.param("hakukohdeOid", v))
    organisaatioOid.foreach(v => req = req.param("organisaatioOid", v))
    valintarajaus.foreach(v => req = req.param("valintarajaus", v))
    mvc.perform(req)
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

  @Test
  @WithMockUser(username = "hakeneet-user", roles = Array("APP_OVARA-VIRKAILIJA_HAKENEET"))
  def returns200ForHakeneetRole(): Unit = {
    initSchema()

    get()
      .andExpect(status.isOk)
      .andExpect(content.json("""{"hakijat": []}"""))
  }

  @ParameterizedTest
  @ValueSource(strings = Array("not-oid", "1.2", "1.2.246", "1.2.246.1", "1.2.247.1.1"))
  def returns400WhenHakuOidNotOid(hakuOid: String): Unit = {
    get(hakuOid = hakuOid)
      .andExpect(status.isBadRequest)
      .andExpect(
        content.json(
          """{"status": 400, "message": "virhe.validointi", "details": ["hakuOid.invalid.oid"] }"""
        )
      )
  }

  @Test
  def returns400WhenHakukohdeOidNotOid(): Unit = {
    get(hakukohdeOid = Some("not-oid"))
      .andExpect(status.isBadRequest)
      .andExpect(
        content.json(
          """{"status": 400, "message": "virhe.validointi", "details": ["hakukohdeOid.invalid.oid"] }"""
        )
      )
  }

  @Test
  def returns400WhenOrganisaatioOidNotOrganisaatioOid(): Unit = {
    get(hakukohdeOid = None, organisaatioOid = Some("1.2.246.562.20.1"))
      .andExpect(status.isBadRequest)
      .andExpect(
        content.json(
          """{"status": 400, "message": "virhe.validointi", "details": ["organisaatioOid.invalid.org"] }"""
        )
      )
  }

  @Test
  def returns400WhenNeitherHakukohdeNorOrganisaatioProvided(): Unit = {
    get(hakukohdeOid = None, organisaatioOid = None)
      .andExpect(status.isBadRequest)
      .andExpect(
        content.json(
          """{"status": 400, "message": "virhe.validointi", "details": ["hakukohdeOid_or_organisaatioOid.required"] }"""
        )
      )
  }

  @Test
  def returns400WhenValintarajausMissing(): Unit = {
    get(valintarajaus = None)
      .andExpect(status.isBadRequest)
  }

  @Test
  def returns400WhenValintarajausInvalid(): Unit = {
    get(valintarajaus = Some("BOGUS"))
      .andExpect(status.isBadRequest)
      .andExpect(
        content.json(
          """{"status": 400, "message": "virhe.validointi", "details": ["valintarajaus.invalid"] }"""
        )
      )
  }

  @Test
  def returns400WhenValintarajausLowercase(): Unit = {
    get(valintarajaus = Some("hyvaksytyt"))
      .andExpect(status.isBadRequest)
      .andExpect(
        content.json(
          """{"status": 400, "message": "virhe.validointi", "details": ["valintarajaus.invalid"] }"""
        )
      )
  }

  @Test
  def hyvaksytytFiltersOutHylattyHakija(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(valintatieto = Some("HYLATTY"))

    get(valintarajaus = Some("HYVAKSYTYT"))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](0)))
  }

  @Test
  def returns500WhenDatabaseError(): Unit = {
    get()
      .andExpect(status.isInternalServerError)
      .andExpect(content.json("\"virhe.tietokanta\""))
  }

  @Test
  def returnsEmptyListWhenNoHakijatMatch(): Unit = {
    initSchema()

    get()
      .andExpect(status.isOk)
      .andExpect(content.json("""{"hakijat": []}"""))
  }

  @Test
  def returnsHakijaFilteredByHakukohdeOid(): Unit = {
    seedMinimalHakija()
    insertToteutusJaKoulutus()
    insertOpetuskieli()
    insertOrganisaatio()
    insertHakemusToinenAsteYhteishaku()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
      .andExpect(jsonPath("$.hakijat[0].sahkoposti").value(EMAIL))
      .andExpect(jsonPath("$.hakijat[0].hetu").value(HETU))
      .andExpect(jsonPath("$.hakijat[0].sukunimi").value(SUKUNIMI))
      .andExpect(jsonPath("$.hakijat[0].etunimet").value(ETUNIMET))
      .andExpect(jsonPath("$.hakijat[0].kutsumanimi").value(KUTSUMANIMI))
      .andExpect(jsonPath("$.hakijat[0].kotikunta").value(KOTIKUNTA))
      .andExpect(jsonPath("$.hakijat[0].maa").value(SUOMI_KOODI))
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet[0]").value("246"))
      .andExpect(jsonPath("$.hakijat[0].sukupuoli").value(SUKUPUOLI.toString))
      .andExpect(jsonPath("$.hakijat[0].aidinkieli").value(AIDINKIELI))
      .andExpect(jsonPath("$.hakijat[0].opetuskieli").value(OPETUSKIELI))
      .andExpect(jsonPath("$.hakijat[0].koulutusmarkkinointilupa").value(KOULUTUSMARKKINOINTILUPA))
      .andExpect(jsonPath("$.hakijat[0].kiinnostunutoppisopimuksesta").value(KIINNOSTUNUT_OPPISOPIMUKSESTA))
      .andExpect(jsonPath("$.hakijat[0].sahkoisenAsioinninLupa").value(SAHKOINENVIESTINTALUPA))
      .andExpect(jsonPath("$.hakijat[0].huoltaja1.etunimi").value(HUOLTAJA1.etunimi))
      .andExpect(jsonPath("$.hakijat[0].huoltaja1.sukunimi").value(HUOLTAJA1.sukunimi))
      .andExpect(jsonPath("$.hakijat[0].huoltaja2.etunimi").value(HUOLTAJA2.etunimi))
      .andExpect(jsonPath("$.hakijat[0].huoltaja2.sukunimi").value(HUOLTAJA2.sukunimi))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakemusnumero").value(HAKEMUS_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemus.vuosi").value(VUOSI))
      .andExpect(jsonPath("$.hakijat[0].hakemus.kausi").value(KAUSI))
      .andExpect(jsonPath("$.hakijat[0].hakemus.julkaisulupa").value(VALINTATULOKSEN_JULKAISULUPA))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakemuksenJattopaiva").value(JATETTY.toString))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakemuksenMuokkauspaiva").value(MUOKATTU.toString))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].hakukohdeOid").value(HAKUKOHDE_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].opetuspiste").value(ORGANISAATIO_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].oppilaitos").value(OPPILAITOS))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].koulutus.koodiarvo").value("621702"))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].valinta").value(VALINTA_CODE))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].vastaanotto").value(VASTAANOTTO_CODE))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].lasnaolo").value(LASNAOLO_CODE))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].terveys").value(TERVEYS))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].aiempiperuminen").value(AIEMPI_PERUMINEN))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].kaksoistutkinto").value(KAKSOISTUTKINTO))
  }

  @Test
  def returnsHakijaFilteredByOrganisaatioOid(): Unit = {
    seedMinimalHakija()

    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
  }

  @Test
  def fieldsWithoutDataSourceAreNullOrEmpty(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].muupuhelin").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].oikeusMaksuttomaanKoulutukseenVoimassaAsti").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].oppivelvollisuusVoimassaAsti").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].lisakysymykset").isEmpty)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulunnimi").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokka").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokkataso").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.osaaminen.yleinen_kielitutkinto_fi").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].terveys").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].urheilijanLisakysymykset").value(nullValue()))
  }

  @Test
  def datesWithZeroSecondsRetainSecondsInJson(): Unit = {
    initSchema()
    insertHakemus(
      jatetty = Some(java.time.OffsetDateTime.parse("2025-08-01T10:00:00+03:00")),
      muokattu = Some(java.time.OffsetDateTime.parse("2025-08-13T14:52:00+03:00"))
    )
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakemuksenJattopaiva").value("2025-08-01T10:00:00+03:00"))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakemuksenMuokkauspaiva").value("2025-08-13T14:52:00+03:00"))
  }

  @Test
  def lahtokouluFieldsPopulatedFromActiveRow(): Unit = {
    seedMinimalHakija()
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      nimiFi = Some(LAHTOKOULU_NIMI_FI),
      nimiSv = Some(LAHTOKOULU_NIMI_SV)
    )
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(LAHTOKOULU_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulunnimi").value(LAHTOKOULU_NIMI_FI))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokka").value(LAHTOKOULU_LUOKKA))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokkataso").value(LAHTOKOULU_LUOKKATASO))
  }

  @Test
  def lahtokouluPicksLatestSuorituksenAlkuWhenOverlapping(): Unit = {
    seedMinimalHakija()
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      nimiFi = Some("Vanha koulu")
    )
    val otherOid = "1.2.246.562.10.00000000000000000901"
    insertOrganisaatio(
      organisaatioOid = otherOid,
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
    // Earlier-started row — should lose.
    insertHenkiloLahtokoulu(
      luokka = Some("8A"),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some("VUOSILUOKKA_8"),
      suorituksenAlku = Some(java.time.LocalDate.parse("2023-08-01")),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )
    // Later-started row that still covers hakemus.jatetty — should win.
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(otherOid),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(otherOid))
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulunnimi").value(LAHTOKOULU_NIMI_FI))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokka").value(LAHTOKOULU_LUOKKA))
  }

  @Test
  def lahtokouluIgnoresRowsOutsideJatettyWindow(): Unit = {
    seedMinimalHakija()
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
    // Ended before jatetty.
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(java.time.LocalDate.parse("2023-08-01")),
      suorituksenLoppu = Some(java.time.LocalDate.parse("2024-06-01"))
    )

    expectAllLahtokouluFieldsNull(get())
  }

  @Test
  def luokkatasoPassesThroughForNamedSuoritusTyyppi(): Unit = {
    seedMinimalHakija()
    insertOrganisaatio(organisaatioOid = LAHTOKOULU_OID)
    insertHenkiloLahtokoulu(
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some("TELMA"),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokkataso").value("TELMA"))
  }

  @Test
  def luokkatasoIsNullForUnknownSuoritusTyyppi(): Unit = {
    seedMinimalHakija()
    insertOrganisaatio(organisaatioOid = LAHTOKOULU_OID)
    insertHenkiloLahtokoulu(
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some("JOKUMUU_TYYPPI"),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokkataso").value(nullValue()))
  }

  @Test
  def lahtokouluIgnoresRowsStartingAfterJatetty(): Unit = {
    seedMinimalHakija()
    insertOrganisaatio(organisaatioOid = LAHTOKOULU_OID, nimiFi = Some(LAHTOKOULU_NIMI_FI))
    // Started after jatetty (which is 2025-08-01) → must be excluded.
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(java.time.LocalDate.parse("2025-09-01")),
      suorituksenLoppu = Some(java.time.LocalDate.parse("2026-06-01"))
    )

    expectAllLahtokouluFieldsNull(get())
  }

  @Test
  def lahtokouluTreatsNullSuorituksenLoppuAsActive(): Unit = {
    seedMinimalHakija()
    insertOrganisaatio(organisaatioOid = LAHTOKOULU_OID, nimiFi = Some(LAHTOKOULU_NIMI_FI))
    // No end date → treat as still active.
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = None
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(LAHTOKOULU_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokka").value(LAHTOKOULU_LUOKKA))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokkataso").value(LAHTOKOULU_LUOKKATASO))
  }

  @Test
  def lahtokouluIsNullWhenHakemusJatettyIsNull(): Unit = {
    initSchema()
    insertHakemus(jatetty = None)
    insertHakukohde()
    insertHakutoive()
    insertOrganisaatio(organisaatioOid = LAHTOKOULU_OID, nimiFi = Some(LAHTOKOULU_NIMI_FI))
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    expectAllLahtokouluFieldsNull(get())
  }

  @Test
  def lahtokoulunnimiIsNullWhenOrganisaatioRowMissing(): Unit = {
    seedMinimalHakija()
    // Deliberately omit insertOrganisaatio for LAHTOKOULU_OID.
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(LAHTOKOULU_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulunnimi").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokka").value(LAHTOKOULU_LUOKKA))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokkataso").value(LAHTOKOULU_LUOKKATASO))
  }

  @Test
  def lahtokouluIsIsolatedPerHakija(): Unit = {
    initSchema()
    // Hakija A
    insertHakemus()
    insertHakutoive()
    // Hakija B — different henkilo, different hakemus, applies to the same hakukohde.
    val oppijanumeroB = "1.2.246.562.24.00000000010"
    val oppilaitosB   = "1.2.246.562.10.00000000000000000901"
    insertHakemus(oppijanumero = oppijanumeroB, hakemusOid = HAKEMUS_OID_2, insertHaku = false)
    insertHakutoive(hakemusOid = HAKEMUS_OID_2)
    insertHakukohde()

    insertOrganisaatio(organisaatioOid = LAHTOKOULU_OID, nimiFi = Some(LAHTOKOULU_NIMI_FI))
    insertOrganisaatio(organisaatioOid = oppilaitosB, nimiFi = Some("Toisen koulu"))
    insertHenkiloLahtokoulu(
      henkiloOid = OPPIJANUMERO,
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some("VUOSILUOKKA_9"),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )
    insertHenkiloLahtokoulu(
      henkiloOid = oppijanumeroB,
      luokka = Some("7B"),
      oppilaitosOid = Some(oppilaitosB),
      suoritusTyyppi = Some("VUOSILUOKKA_7"),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    val body    = get().andExpect(status.isOk).andReturn().getResponse.getContentAsString
    val mapper  = new com.fasterxml.jackson.databind.ObjectMapper
    val hakijat = mapper.readTree(body).get("hakijat")
    assert(hakijat.size == 2, s"expected 2 hakijat, got: $body")
    val byOppijanumero = (0 until hakijat.size)
      .map(hakijat.get)
      .map(h => h.get("oppijanumero").asText -> h)
      .toMap
    val hA = byOppijanumero(OPPIJANUMERO).get("hakemus")
    assert(hA.get("lahtokoulu").asText == LAHTOKOULU_OID)
    assert(hA.get("luokka").asText == LAHTOKOULU_LUOKKA)
    assert(hA.get("luokkataso").asText == "9")
    val hB = byOppijanumero(oppijanumeroB).get("hakemus")
    assert(hB.get("lahtokoulu").asText == oppilaitosB)
    assert(hB.get("luokka").asText == "7B")
    assert(hB.get("luokkataso").asText == "7")
  }

  @Test
  def excelWritesLahtokouluCells(): Unit = {
    seedMinimalHakija()
    insertToteutusJaKoulutus()
    insertHakemusToinenAsteYhteishaku()
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    val result = mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/external/toisenasteenhakijat/excel")
          .param("hakuOid", HAKU_OID)
          .param("hakukohdeOid", HAKUKOHDE_OID)
          .param("valintarajaus", "HAKENEET")
      )
      .andExpect(status.isOk)
      .andReturn()

    val bytes    = result.getResponse.getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val sheet     = workbook.getSheetAt(0)
      val headerRow = sheet.getRow(0)
      assert(headerRow.getCell(34).getStringCellValue == "Lahtokoulu")
      assert(headerRow.getCell(35).getStringCellValue == "Lahtokoulunnimi")
      assert(headerRow.getCell(36).getStringCellValue == "Luokka")
      assert(headerRow.getCell(37).getStringCellValue == "Luokkataso")

      val dataRow = sheet.getRow(1)
      assert(dataRow.getCell(34).getStringCellValue == LAHTOKOULU_OID)
      assert(dataRow.getCell(35).getStringCellValue == LAHTOKOULU_NIMI_FI)
      assert(dataRow.getCell(36).getStringCellValue == LAHTOKOULU_LUOKKA)
      assert(dataRow.getCell(37).getStringCellValue == LAHTOKOULU_LUOKKATASO)
    } finally workbook.close()
  }

  @ParameterizedTest
  @CsvSource(
    Array(
      "VUOSILUOKKA_7, 7",
      "VUOSILUOKKA_8, 8",
      "VUOSILUOKKA_9, 9",
      "AIKUISTEN_PERUSOPETUS, AIKUISTEN_PERUSOPETUS",
      "PERUSOPETUKSEEN_VALMISTAVA_OPETUS, PERUSOPETUKSEEN_VALMISTAVA_OPETUS",
      "TELMA, TELMA",
      "TUVA, TUVA",
      "VAPAA_SIVISTYSTYO, VAPAA_SIVISTYSTYO"
    )
  )
  def suoritusTyyppiMapsToExpectedLuokkataso(input: String, expected: String): Unit = {
    assert(LahtokouluRow.suoritusTyyppiToLuokkataso(input).contains(expected))
  }

  @ParameterizedTest
  @ValueSource(strings = Array("JOKUMUU", "vuosiluokka_9", "VUOSILUOKKA_10", "", " "))
  def suoritusTyyppiReturnsNoneForUnknown(input: String): Unit = {
    assert(LahtokouluRow.suoritusTyyppiToLuokkataso(input).isEmpty)
  }

  @Test
  def lahtokoulunnimiFallsBackToSvWhenFiMissing(): Unit = {
    seedMinimalHakija()
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      nimiFi = None,
      nimiSv = Some(LAHTOKOULU_NIMI_SV)
    )
    insertHenkiloLahtokoulu(
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulunnimi").value(LAHTOKOULU_NIMI_SV))
  }

  // ---- alias resolution (gen_henkilo master/linked henkilo_oid) ----

  private def seedActiveLahtokoulu(henkiloOid: String): Unit =
    insertHenkiloLahtokoulu(
      henkiloOid = henkiloOid,
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

  private def expectLahtokouluPopulated(actions: ResultActions): Unit = {
    actions
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(LAHTOKOULU_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokka").value(LAHTOKOULU_LUOKKA))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokkataso").value(LAHTOKOULU_LUOKKATASO))
  }

  private def expectAllLahtokouluFieldsNull(actions: ResultActions): Unit = {
    actions
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulunnimi").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokka").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokkataso").value(nullValue()))
  }

  @Test
  def lahtokouluResolvesWhenHakemusUsesPrimaryLahtokouluUsesAlias(): Unit = {
    initSchema()
    val aliasA = "1.2.246.562.24.90000000001"
    insertHenkilo() // (OPPIJANUMERO, OPPIJANUMERO, …)
    insertHenkiloAlias(OPPIJANUMERO, aliasA)
    insertHakemus(insertHenkilo = false) // hakemus.henkilo_oid = OPPIJANUMERO
    insertHakukohde()
    insertHakutoive()
    insertOrganisaatio(organisaatioOid = LAHTOKOULU_OID, nimiFi = Some(LAHTOKOULU_NIMI_FI))
    seedActiveLahtokoulu(henkiloOid = aliasA)

    expectLahtokouluPopulated(get())
  }

  @Test
  def lahtokouluResolvesWhenHakemusUsesAliasLahtokouluUsesPrimary(): Unit = {
    initSchema()
    val aliasA = "1.2.246.562.24.90000000002"
    insertHenkilo() // (OPPIJANUMERO, OPPIJANUMERO, …)
    insertHenkiloAlias(OPPIJANUMERO, aliasA)
    // hakemus.henkilo_oid = aliasA — the `oppijanumero` param on insertHakemus becomes gen_hakemus.henkilo_oid.
    insertHakemus(oppijanumero = aliasA, insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()
    insertOrganisaatio(organisaatioOid = LAHTOKOULU_OID, nimiFi = Some(LAHTOKOULU_NIMI_FI))
    seedActiveLahtokoulu(henkiloOid = OPPIJANUMERO)

    expectLahtokouluPopulated(get())
  }

  @Test
  def lahtokouluResolvesAcrossTwoAliasesOfSamePerson(): Unit = {
    initSchema()
    val aliasA = "1.2.246.562.24.90000000003"
    val aliasB = "1.2.246.562.24.90000000004"
    insertHenkilo()
    insertHenkiloAlias(OPPIJANUMERO, aliasA)
    insertHenkiloAlias(OPPIJANUMERO, aliasB)
    insertHakemus(oppijanumero = aliasA, insertHenkilo = false) // hakemus under alias A
    insertHakukohde()
    insertHakutoive()
    insertOrganisaatio(organisaatioOid = LAHTOKOULU_OID, nimiFi = Some(LAHTOKOULU_NIMI_FI))
    seedActiveLahtokoulu(henkiloOid = aliasB) // lahtokoulu under alias B

    expectLahtokouluPopulated(get())
  }

  @Test
  def lahtokouluPicksLatestWinnerAcrossAliases(): Unit = {
    initSchema()
    val aliasB   = "1.2.246.562.24.90000000005"
    val otherOid = "1.2.246.562.10.00000000000000000902"
    insertHenkilo()
    insertHenkiloAlias(OPPIJANUMERO, aliasB)
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()
    insertOrganisaatio(organisaatioOid = otherOid, nimiFi = Some("Aiempi koulu"))
    insertOrganisaatio(organisaatioOid = LAHTOKOULU_OID, nimiFi = Some(LAHTOKOULU_NIMI_FI))
    // Earlier row filed under primary OID.
    insertHenkiloLahtokoulu(
      henkiloOid = OPPIJANUMERO,
      luokka = Some("8A"),
      oppilaitosOid = Some(otherOid),
      suoritusTyyppi = Some("VUOSILUOKKA_8"),
      suorituksenAlku = Some(java.time.LocalDate.parse("2023-08-01")),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )
    // Later row filed under alias B — this should win.
    seedActiveLahtokoulu(henkiloOid = aliasB)

    expectLahtokouluPopulated(get())
  }

  @Test
  def lahtokouluDoesNotLeakAcrossPersons(): Unit = {
    initSchema()
    val personY = "1.2.246.562.24.90000000006"
    insertHenkilo()                       // person X's primary self-link
    insertHenkilo(oppijanumero = personY) // separate person Y
    insertHakemus(insertHenkilo = false)  // hakemus for person X
    insertHakukohde()
    insertHakutoive()
    insertOrganisaatio(organisaatioOid = LAHTOKOULU_OID, nimiFi = Some(LAHTOKOULU_NIMI_FI))
    // Lahtokoulu belongs to person Y — must NOT surface on person X's hakemus.
    seedActiveLahtokoulu(henkiloOid = personY)

    expectAllLahtokouluFieldsNull(get())
  }

  @Test
  def lahtokouluResolvesWhenPersonHasAliasButOnlySelfLinkedRow(): Unit = {
    initSchema()
    val aliasA = "1.2.246.562.24.90000000007"
    insertHenkilo()
    insertHenkiloAlias(OPPIJANUMERO, aliasA) // alias exists but no lahtokoulu under it
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()
    insertOrganisaatio(organisaatioOid = LAHTOKOULU_OID, nimiFi = Some(LAHTOKOULU_NIMI_FI))
    seedActiveLahtokoulu(henkiloOid = OPPIJANUMERO)

    expectLahtokouluPopulated(get())
  }

  // ---- Pohjakoulutus + Todistusvuosi (gen_supa_tieto) ----

  @Test
  def pohjakoulutusIsPopulatedFromSupaTieto(): Unit = {
    seedMinimalHakija()
    insertPohjakoulutus(arvo = Some("1"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.pohjakoulutus").value("1"))
  }

  @Test
  def todistusvuosiIsPopulatedFromSupaTieto(): Unit = {
    seedMinimalHakija()
    insertTodistusvuosi(arvo = Some("2025"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.todistusvuosi").value("2025"))
  }

  @Test
  def pohjakoulutusAndTodistusvuosiAreNullWhenAbsent(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.pohjakoulutus").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.todistusvuosi").value(nullValue()))
  }

  @Test
  def pohjakoulutusStripsJsonQuotes(): Unit = {
    seedMinimalHakija()
    insertPohjakoulutus(arvo = Some("\"3\""))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.pohjakoulutus").value("3"))
  }

  @Test
  def todistusvuosiStripsJsonQuotes(): Unit = {
    seedMinimalHakija()
    insertTodistusvuosi(arvo = Some("\"2024\""))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.todistusvuosi").value("2024"))
  }

  @Test
  def excelWritesPohjakoulutusAndTodistusvuosiCells(): Unit = {
    seedMinimalHakija()
    insertToteutusJaKoulutus()
    insertHakemusToinenAsteYhteishaku()
    insertPohjakoulutus(arvo = Some("1"))
    insertTodistusvuosi(arvo = Some("2025"))

    val result = mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/external/toisenasteenhakijat/excel")
          .param("hakuOid", HAKU_OID)
          .param("hakukohdeOid", HAKUKOHDE_OID)
          .param("valintarajaus", "HAKENEET")
      )
      .andExpect(status.isOk)
      .andReturn()

    val bytes    = result.getResponse.getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val sheet     = workbook.getSheetAt(0)
      val headerRow = sheet.getRow(0)
      assert(headerRow.getCell(38).getStringCellValue == "Pohjakoulutus")
      assert(headerRow.getCell(39).getStringCellValue == "Todistusvuosi")

      val dataRow = sheet.getRow(1)
      assert(dataRow.getCell(38).getStringCellValue == "1")
      assert(dataRow.getCell(39).getStringCellValue == "2025")
    } finally workbook.close()
  }

  // ---- kansalaisuudet JSON extraction edges ----

  @Test
  def kansalaisuudetEmptyArrayYieldsEmptyList(): Unit = {
    initSchema()
    insertHakemus(kansalaisuusJson = Some("[]"))
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet", hasSize[Any](0)))
  }

  @Test
  def kansalaisuudetMultipleEntries(): Unit = {
    initSchema()
    insertHakemus(kansalaisuusJson = Some("[\"246\",\"752\"]"))
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet", hasSize[Any](2)))
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet[0]").value("246"))
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet[1]").value("752"))
  }

  // ---- parseHakukohteet malformed JSON ----

  @Test
  def hakukohteetJsonMalformedYieldsNullPerHakutoiveFlags(): Unit = {
    seedMinimalHakija()
    insertHakemusToinenAsteYhteishaku(hakukohteetJson = Some("not-json"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].terveys").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].aiempiperuminen").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].kaksoistutkinto").value(nullValue()))
  }

  // ---- missing gen_hakemus_toinenaste_yhteishaku row ----

  @Test
  def hakijaWithoutToinenAsteYhteishakuRowNullsOutHuoltajatAndUrheilija(): Unit = {
    seedMinimalHakija()
    // Deliberately omit insertHakemusToinenAsteYhteishaku()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet[0]").value("246"))
      .andExpect(jsonPath("$.hakijat[0].huoltaja1").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].huoltaja2").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].urheilijanLisakysymykset").value(nullValue()))
  }

  // ---- aidinkieli / opetuskieli normalization edges ----

  @ParameterizedTest
  @CsvSource(
    value = Array(
      "'\"en\"', EN",
      "'FI', FI",
      "'  sv  ', SV",
      "'', null",
      "'\"\"', null"
    ),
    nullValues = Array("null")
  )
  def opetuskieliNormalizationEdges(arvo: String, expected: String): Unit = {
    seedMinimalHakija()
    insertOpetuskieli(arvo = Some(arvo))
    val actions = get().andExpect(status.isOk)
    if (expected == null)
      actions.andExpect(jsonPath("$.hakijat[0].opetuskieli").value(nullValue()))
    else
      actions.andExpect(jsonPath("$.hakijat[0].opetuskieli").value(expected))
  }

  @ParameterizedTest
  @CsvSource(
    value = Array(
      "'\"en\"', EN",
      "'FI', FI",
      "'  sv  ', SV",
      "'', null",
      "'\"\"', null"
    ),
    nullValues = Array("null")
  )
  def aidinkieliNormalizationEdges(arvo: String, expected: String): Unit = {
    initSchema()
    insertHakemus(insertHenkilo = false)
    insertHenkilo(aidinkieli = Some(arvo))
    insertHakukohde()
    insertHakutoive()
    val actions = get().andExpect(status.isOk)
    if (expected == null)
      actions.andExpect(jsonPath("$.hakijat[0].aidinkieli").value(nullValue()))
    else
      actions.andExpect(jsonPath("$.hakijat[0].aidinkieli").value(expected))
  }

  // ---- multi-hakutoive hakija with lahtokoulu ----

  @Test
  def lahtokouluAttachesOnceWhenHakijaHasMultipleHakutoiveet(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, hakutoivenumero = 1)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2)
    insertOrganisaatio(organisaatioOid = LAHTOKOULU_OID, nimiFi = Some(LAHTOKOULU_NIMI_FI))
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    // Filter by organisaatioOid rather than hakukohdeOid so both hakutoiveet (same jarjestyspaikka) surface.
    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet", hasSize[Any](2)))
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(LAHTOKOULU_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokka").value(LAHTOKOULU_LUOKKA))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokkataso").value(LAHTOKOULU_LUOKKATASO))
  }

  // ---- empty koodistot early-return branch ----

  @Test
  def hakijaWithoutKoulutusKoodiuriProducesNullKoulutus(): Unit = {
    seedMinimalHakija()
    // Deliberately omit insertToteutusJaKoulutus() → no koodiUrit → koodistot short-circuits to Map.empty.

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].koulutus").value(nullValue()))
  }

  @Test
  @WithMockUser(username = "testuser", roles = Array("USER"))
  def excelReturns403WhenUserMissingRole(): Unit = {
    mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/external/toisenasteenhakijat/excel")
          .param("hakuOid", HAKU_OID)
          .param("hakukohdeOid", HAKUKOHDE_OID)
          .param("valintarajaus", "HAKENEET")
      )
      .andExpect(status.isForbidden)
  }

  @Test
  @WithMockUser(username = "hakeneet-user", roles = Array("APP_OVARA-VIRKAILIJA_HAKENEET"))
  def excelReturns200ForHakeneetRole(): Unit = {
    seedMinimalHakija()
    insertToteutusJaKoulutus()
    insertHakemusToinenAsteYhteishaku()

    mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/external/toisenasteenhakijat/excel")
          .param("hakuOid", HAKU_OID)
          .param("hakukohdeOid", HAKUKOHDE_OID)
          .param("valintarajaus", "HAKENEET")
      )
      .andExpect(status.isOk)
      .andExpect(
        content.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      )
  }

  @Test
  def excelReturns400WhenNeitherHakukohdeNorOrganisaatioProvided(): Unit = {
    mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/external/toisenasteenhakijat/excel")
          .param("hakuOid", HAKU_OID)
          .param("valintarajaus", "HAKENEET")
      )
      .andExpect(status.isBadRequest)
      .andExpect(
        content.json(
          """{"status": 400, "message": "virhe.validointi", "details": ["hakukohdeOid_or_organisaatioOid.required"] }"""
        )
      )
  }

  @Test
  def excelHasHeadersInExpectedOrderAndDataRow(): Unit = {
    seedMinimalHakija()
    insertToteutusJaKoulutus()
    insertHakemusToinenAsteYhteishaku()

    val result = mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/external/toisenasteenhakijat/excel")
          .param("hakuOid", HAKU_OID)
          .param("hakukohdeOid", HAKUKOHDE_OID)
          .param("valintarajaus", "HAKENEET")
      )
      .andExpect(status.isOk)
      .andExpect(
        content.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      )
      .andExpect(header.string("Content-Disposition", startsWith("attachment; filename=toisenasteenhakijat-")))
      .andReturn()

    val bytes    = result.getResponse.getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val sheet     = workbook.getSheetAt(0)
      val headerRow = sheet.getRow(0)
      assert(headerRow.getCell(0).getStringCellValue == "Hetu")
      assert(headerRow.getCell(1).getStringCellValue == "Oppijanumero")
      assert(headerRow.getCell(2).getStringCellValue == "Sukunimi")
      assert(headerRow.getCell(31).getStringCellValue == "Hakemusnumero")
      assert(headerRow.getCell(47).getStringCellValue == "Hakujno")
      assert(headerRow.getCell(52).getStringCellValue == "HakukohdeOid")
      assert(headerRow.getCell(56).getStringCellValue == "Valinta")
      assert(headerRow.getCell(57).getStringCellValue == "Vastaanotto")
      assert(headerRow.getCell(58).getStringCellValue == "Lasnaolo")
      assert(headerRow.getCell(59).getStringCellValue == "Terveys")
      assert(headerRow.getCell(60).getStringCellValue == "Aiempiperuminen")
      assert(headerRow.getCell(61).getStringCellValue == "Kaksoistutkinto")
      assert(headerRow.getCell(77).getStringCellValue == "Sähköisen asioinnin lupa")

      val dataRow = sheet.getRow(1)
      assert(dataRow.getCell(0).getStringCellValue == HETU)
      assert(dataRow.getCell(1).getStringCellValue == OPPIJANUMERO)
      assert(dataRow.getCell(2).getStringCellValue == SUKUNIMI)
      assert(dataRow.getCell(3).getStringCellValue == ETUNIMET)
      assert(dataRow.getCell(4).getStringCellValue == KUTSUMANIMI)
      assert(dataRow.getCell(8).getStringCellValue == SUOMI_KOODI)
      assert(dataRow.getCell(9).getStringCellValue == "246")
      assert(dataRow.getCell(25).getStringCellValue == "X") // koulutusmarkkinointilupa = true
      assert(dataRow.getCell(26).getStringCellValue == "")  // kiinnostunutoppisopimuksesta = false
      assert(dataRow.getCell(31).getStringCellValue == HAKEMUS_OID)
      assert(dataRow.getCell(40).getStringCellValue == "X") // julkaisulupa = true
      assert(dataRow.getCell(47).getStringCellValue == "1") // hakujno
      assert(dataRow.getCell(49).getStringCellValue == ORGANISAATIO_OID)
      assert(dataRow.getCell(52).getStringCellValue == HAKUKOHDE_OID)
      assert(dataRow.getCell(56).getStringCellValue == VALINTA_CODE)
      assert(dataRow.getCell(57).getStringCellValue == VASTAANOTTO_CODE)
      assert(dataRow.getCell(58).getStringCellValue == LASNAOLO_CODE)
      assert(dataRow.getCell(59).getStringCellValue == "X")     // terveys = true
      assert(dataRow.getCell(60).getStringCellValue == "")      // aiempiperuminen = false
      assert(dataRow.getCell(61).getStringCellValue == "X")     // kaksoistutkinto = true
      assert(dataRow.getCell(77).getStringCellValue == "Kyllä") // sähköisen asioinnin lupa
    } finally workbook.close()
  }

  @Test
  def excelHasFullColumnCoverageForPopulatedHakija(): Unit = {
    // Seed every populatable data source so every column that CAN have a value does.
    seedMinimalHakija()
    insertToteutusJaKoulutus()
    insertOpetuskieli()
    insertOrganisaatio() // hakukohde's organisaatio_oid → cell 48 (Oppilaitos)
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
    insertHakemusToinenAsteYhteishaku()
    insertPohjakoulutus(arvo = Some("2"))
    insertTodistusvuosi(arvo = Some("2025"))
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    val result = mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/external/toisenasteenhakijat/excel")
          .param("hakuOid", HAKU_OID)
          .param("hakukohdeOid", HAKUKOHDE_OID)
          .param("valintarajaus", "HAKENEET")
      )
      .andExpect(status.isOk)
      .andReturn()

    val workbook = new XSSFWorkbook(new ByteArrayInputStream(result.getResponse.getContentAsByteArray))
    try {
      val sheet   = workbook.getSheetAt(0)
      val header  = sheet.getRow(0)
      val dataRow = sheet.getRow(1)

      // Header row: all 80 columns compared elementwise.
      val expectedHeaders = Seq(
        "Hetu",
        "Oppijanumero",
        "Sukunimi",
        "Etunimet",
        "Kutsumanimi",
        "Lahiosoite",
        "Postinumero",
        "Postitoimipaikka",
        "Maa",
        "Kansalaisuudet",
        "Matkapuhelin",
        "Muupuhelin",
        "Sahkoposti",
        "Kotikunta",
        "Sukupuoli",
        "Aidinkieli",
        "Opetuskieli",
        "Huoltaja 1 etunimi",
        "Huoltaja 1 sukunimi",
        "Huoltaja 1 puh",
        "Huoltaja 1 email",
        "Huoltaja 2 etunimi",
        "Huoltaja 2 sukunimi",
        "Huoltaja 2 puh",
        "Huoltaja 2 email",
        "Koulutusmarkkinointilupa",
        "Kiinnostunut oppisopimuskoulutuksesta",
        "Oppivelvollisuus voimassa asti",
        "Oikeus maksuttomaan koulutukseen voimassa asti",
        "Vuosi",
        "Kausi",
        "Hakemusnumero",
        "Hakemus jätetty",
        "Hakemusta viimeksi muokattu",
        "Lahtokoulu",
        "Lahtokoulunnimi",
        "Luokka",
        "Luokkataso",
        "Pohjakoulutus",
        "Todistusvuosi",
        "Julkaisulupa",
        "Yhteisetaineet",
        "Lukiontasapisteet",
        "Yleinenkoulumenestys",
        "Lisapistekoulutus",
        "Painotettavataineet",
        "Keskiarvo valintalaskennasta",
        "Hakujno",
        "Oppilaitos",
        "Opetuspiste",
        "Opetuspisteennimi",
        "Koulutus",
        "HakukohdeOid",
        "Harkinnanvaraisuuden peruste",
        "Urheilijan ammatillinen koulutus",
        "Yhteispisteet",
        "Valinta",
        "Vastaanotto",
        "Lasnaolo",
        "Terveys",
        "Aiempiperuminen",
        "Kaksoistutkinto",
        "Urheilija-peruskoulu",
        "Urheilija-keskiarvo",
        "Urheilija-tamakausi",
        "Urheilija.viimekausi",
        "Urheilija-toissakausi",
        "Urheilija-sivulaji",
        "Urheilija-valmennusryhma-seurajoukkue",
        "Urheilija-valmennusryhma-piirijoukkue",
        "Urheilija-valmennusryhma-maajoukkue",
        "Urheilija-valmentaja-nimi",
        "Urheilija-valmentaja-email",
        "Urheilija-valmentaja-puh",
        "Urheilija-laji",
        "Urheilija-liitto",
        "Urheilija-seura",
        "Sähköisen asioinnin lupa"
      )
      expectedHeaders.zipWithIndex.foreach { case (name, idx) =>
        assert(
          header.getCell(idx).getStringCellValue == name,
          s"header cell $idx expected [$name] but was [${header.getCell(idx).getStringCellValue}]"
        )
      }

      // Data row: cells that carry values in this scenario. Urheilija fields (62–76) stay empty
      // because the hakukohde is not urheilija-related; other unpopulated fields default to "" or "0".
      val expectedCells = Map(
        0  -> HETU,
        1  -> OPPIJANUMERO,
        2  -> SUKUNIMI,
        3  -> ETUNIMET,
        4  -> KUTSUMANIMI,
        5  -> LAHIOSOITE,
        6  -> POSTINUMERO,
        7  -> HELSINKI,
        8  -> SUOMI_KOODI,
        9  -> "246",
        10 -> MATKAPUHELIN,
        11 -> "",
        12 -> EMAIL,
        13 -> KOTIKUNTA,
        14 -> SUKUPUOLI.toString,
        15 -> AIDINKIELI,
        16 -> OPETUSKIELI,
        17 -> HUOLTAJA1.etunimi,
        18 -> HUOLTAJA1.sukunimi,
        19 -> HUOLTAJA1.puhelinnumero,
        20 -> HUOLTAJA1.sahkoposti,
        21 -> HUOLTAJA2.etunimi,
        22 -> HUOLTAJA2.sukunimi,
        23 -> HUOLTAJA2.puhelinnumero,
        24 -> HUOLTAJA2.sahkoposti,
        25 -> "X",
        26 -> "",
        27 -> "",
        28 -> "",
        29 -> VUOSI,
        30 -> KAUSI,
        31 -> HAKEMUS_OID,
        32 -> JATETTY.toString,
        33 -> MUOKATTU.toString,
        34 -> LAHTOKOULU_OID,
        35 -> LAHTOKOULU_NIMI_FI,
        36 -> LAHTOKOULU_LUOKKA,
        37 -> LAHTOKOULU_LUOKKATASO,
        38 -> "2",
        39 -> "2025",
        40 -> "X",
        41 -> "0",
        42 -> "0",
        43 -> "0",
        44 -> "",
        45 -> "0",
        46 -> "",
        47 -> "1",
        48 -> OPPILAITOS,
        49 -> ORGANISAATIO_OID,
        50 -> "",
        51 -> "Kulttuurituottaja",
        52 -> HAKUKOHDE_OID,
        53 -> "",
        54 -> "",
        55 -> "0",
        56 -> VALINTA_CODE,
        57 -> VASTAANOTTO_CODE,
        58 -> LASNAOLO_CODE,
        59 -> "X",
        60 -> "",
        61 -> "X",
        // 62-76: urheilija cells all empty (non-urheilija hakukohde).
        62 -> "",
        63 -> "",
        64 -> "",
        65 -> "",
        66 -> "",
        67 -> "",
        68 -> "",
        69 -> "",
        70 -> "",
        71 -> "",
        72 -> "",
        73 -> "",
        74 -> "",
        75 -> "",
        76 -> "",
        77 -> "Kyllä"
      )
      expectedCells.foreach { case (idx, value) =>
        assert(
          dataRow.getCell(idx).getStringCellValue == value,
          s"cell $idx expected [$value] but was [${dataRow.getCell(idx).getStringCellValue}]"
        )
      }
    } finally workbook.close()
  }

  @Test
  def excludesShortHakemusOid(): Unit = {
    initSchema()
    insertHakemus(hakemusOid = "1.2.246.562.11.3511892")
    insertHakukohde()
    insertHakutoive(hakemusOid = "1.2.246.562.11.3511892")

    get()
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

    get()
      .andExpect(status.isOk)
      .andExpect(content.json("""{"hakijat": []}"""))
  }

  @Test
  def jsonEndpointEmitsAuditLogEntryOnSuccess(): Unit = {
    seedMinimalHakija()

    get(organisaatioOid = None).andExpect(status.isOk)

    val calls = recordedAuditCalls
    assert(calls.size == 1, s"expected exactly one audit entry, got $calls")
    val call = calls.head
    assert(call.operation == AuditOperation.ExternalToisenAsteenHakijat)
    assert(call.params("format") == "json")
    assert(call.params("hakuOid") == HAKU_OID)
    assert(call.params("hakukohdeOid") == HAKUKOHDE_OID)
    assert(call.params("organisaatioOid") == "")
    assert(
      call.principalName.contains("testuser"),
      s"expected authenticated principal 'testuser' in audit entry, got ${call.principalName}"
    )
  }

  @Test
  def excelEndpointEmitsAuditLogEntryOnSuccess(): Unit = {
    seedMinimalHakija()
    insertToteutusJaKoulutus()

    mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/external/toisenasteenhakijat/excel")
          .param("hakuOid", HAKU_OID)
          .param("organisaatioOid", ORGANISAATIO_OID)
          .param("valintarajaus", "HAKENEET")
          .header("User-Agent", "ovara-integration-test/1.0")
      )
      .andExpect(status.isOk)

    val calls = recordedAuditCalls
    assert(calls.size == 1, s"expected exactly one audit entry, got $calls")
    val call = calls.head
    assert(call.operation == AuditOperation.ExternalToisenAsteenHakijat)
    assert(call.params("format") == "excel")
    assert(call.params("hakuOid") == HAKU_OID)
    assert(call.params("hakukohdeOid") == "")
    assert(call.params("organisaatioOid") == ORGANISAATIO_OID)
    assert(
      call.principalName.contains("testuser"),
      s"expected authenticated principal 'testuser' in audit entry, got ${call.principalName}"
    )
    assert(
      call.userAgent.contains("ovara-integration-test/1.0"),
      s"expected request User-Agent in audit entry, got ${call.userAgent}"
    )
  }

  @Test
  @WithMockUser(
    username = "another-user",
    roles = Array("APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA_1.2.246.562.10.00000000001")
  )
  def auditEntryCarriesTheCallersPrincipalName(): Unit = {
    seedMinimalHakija()

    get(organisaatioOid = None).andExpect(status.isOk)

    assert(
      recordedAuditCalls.head.principalName.contains("another-user"),
      s"audit entry must carry the calling user; got ${recordedAuditCalls.head.principalName}"
    )
  }

  @Test
  @WithMockUser(username = "testuser", roles = Array("USER"))
  def forbiddenRequestEmitsNoAuditEntry(): Unit = {
    get().andExpect(status.isForbidden)
    assert(recordedAuditCalls.isEmpty, s"expected no audit entry, got $recordedAuditCalls")
  }

  @Test
  def validationErrorEmitsNoAuditEntry(): Unit = {
    get(hakukohdeOid = None, organisaatioOid = None).andExpect(status.isBadRequest)
    assert(recordedAuditCalls.isEmpty, s"expected no audit entry, got $recordedAuditCalls")
  }

  @Test
  def auditEntryRecordedEvenWhenDbQueryFails(): Unit = {
    // No initSchema call → gen.* tables don't exist → service returns Left("virhe.tietokanta")
    get().andExpect(status.isInternalServerError)

    val calls = recordedAuditCalls
    assert(calls.size == 1, s"audit must fire before the DB query; got $calls")
    assert(calls.head.operation == AuditOperation.ExternalToisenAsteenHakijat)
    assert(calls.head.params("format") == "json")
  }
}

object ExternalToisenAsteenHakijatControllerTest {

  case class AuditCall(
    operation: Operation,
    params: Map[String, Any],
    principalName: Option[String],
    sessionId: Option[String],
    userAgent: Option[String]
  )

  val recordedAuditCalls = new java.util.concurrent.ConcurrentLinkedQueue[AuditCall]()

  @TestConfiguration
  class RecordingAuditConfig {
    @Bean
    @Primary
    def recordingAuditLog(): AuditLog = new AuditLog(fi.oph.ovara.backend.utils.AuditLogger) {
      override def logWithParams(
        request: HttpServletRequest,
        operation: Operation,
        raporttiParams: Map[String, Any]
      ): Unit = {
        val principal = Option(
          org.springframework.security.core.context.SecurityContextHolder.getContext.getAuthentication
        ).map(_.getName)
        val session = Option(request.getSession(false)).map(_.getId)
        val agent   = Option(request.getHeader("User-Agent"))
        recordedAuditCalls.add(AuditCall(operation, raporttiParams, principal, session, agent))
      }
    }
  }
}
