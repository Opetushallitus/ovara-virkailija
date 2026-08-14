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

  private def getExcel(
    hakuOid: String = HAKU_OID,
    hakukohdeOid: Option[String] = Some(HAKUKOHDE_OID),
    organisaatioOid: Option[String] = None,
    valintarajaus: Option[String] = Some("HAKENEET"),
    headers: Map[String, String] = Map.empty
  ): ResultActions = {
    var req = MockMvcRequestBuilders
      .get("/api/external/toisenasteenhakijat/excel")
      .param("hakuOid", hakuOid)
    hakukohdeOid.foreach(v => req = req.param("hakukohdeOid", v))
    organisaatioOid.foreach(v => req = req.param("organisaatioOid", v))
    valintarajaus.foreach(v => req = req.param("valintarajaus", v))
    headers.foreach { case (k, v) => req = req.header(k, v) }
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
  @WithMockUser(username = "hakeneet-user", roles = Array("APP_OVARA-VIRKAILIJA_HAKENEET_1.2.246.562.10.00000000001"))
  def returns200ForHakeneetRole(): Unit = {
    initSchema()

    get()
      .andExpect(status.isOk)
      .andExpect(content.json("""{"hakijat": []}"""))
  }

  @Test
  @WithMockUser(
    username = "hakeneet-org-a-user",
    roles = Array("APP_OVARA-VIRKAILIJA_HAKENEET_1.2.246.562.10.00000000000000000486")
  )
  def hakeneetUserSeesOnlyHakijatFromOwnOrg(): Unit = {
    // orgA = ORGANISAATIO_OID = "1.2.246.562.10.00000000000000000486" — user's HAKENEET-suffixed org.
    // orgB is a different jarjestyspaikka — the user must NOT see hakijat from there.
    val orgB          = ORGANISAATIO_OID_2
    val hakukohdeOidB = HAKUKOHDE_OID_2
    val oppijanumeroB = "1.2.246.562.24.00000000010"
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertHakemus(oppijanumero = oppijanumeroB, hakemusOid = HAKEMUS_OID_2, insertHaku = false)
    insertHakukohde(hakukohdeOid = hakukohdeOidB, jarjestyspaikkaOid = orgB, organisaatioOid = Some(orgB))
    insertHakutoive(hakemusOid = HAKEMUS_OID_2, hakukohdeOid = hakukohdeOidB)

    // Filter by hakuOid only — no organisaatioOid override — so the scope decides who's visible.
    // (Endpoint contract still requires one of hakukohde/organisaatio, so we pass organisaatioOid = orgA.)
    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
  }

  @Test
  @WithMockUser(
    username = "mixed-authorities-user",
    roles = Array(
      "APP_OVARA-VIRKAILIJA_HAKENEET_1.2.246.562.10.00000000000000000486",
      "APP_OVARA-VIRKAILIJA_2ASTE_1.2.246.562.10.00000000000000000487"
    )
  )
  def hakeneetUserIgnoresOidsFromOtherOvaraAuthorities(): Unit = {
    // User has HAKENEET_<orgA> and 2ASTE_<orgB>. Only orgA should count as scope for this endpoint.
    val orgB          = ORGANISAATIO_OID_2
    val hakukohdeOidB = HAKUKOHDE_OID_2
    val oppijanumeroB = "1.2.246.562.24.00000000010"
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertHakemus(oppijanumero = oppijanumeroB, hakemusOid = HAKEMUS_OID_2, insertHaku = false)
    insertHakukohde(hakukohdeOid = hakukohdeOidB, jarjestyspaikkaOid = orgB, organisaatioOid = Some(orgB))
    insertHakutoive(hakemusOid = HAKEMUS_OID_2, hakukohdeOid = hakukohdeOidB)

    get(hakukohdeOid = None, organisaatioOid = Some(orgB))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](0)))
  }

  @Test
  @WithMockUser(
    username = "hakeneet-no-oid-user",
    // HAKENEET_ prefix matches, but the trailing token has no digits → getKayttooikeusOids yields empty.
    roles = Array("APP_OVARA-VIRKAILIJA_HAKENEET_")
  )
  def hakeneetUserWithNoOidYieldsEmptyResults(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](0)))
  }

  @Test
  @WithMockUser(
    username = "paakayttaja-plus-hakeneet",
    roles = Array(
      "APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA_1.2.246.562.10.00000000001",
      "APP_OVARA-VIRKAILIJA_HAKENEET_1.2.246.562.10.00000000000000000486"
    )
  )
  def paakayttajaAndHakeneetTogetherGetPaakayttajaScope(): Unit = {
    // Hakija applies at orgB, which is NOT in the HAKENEET-suffixed OID set — paakayttaja wins,
    // so the hakija must still surface.
    val orgB          = ORGANISAATIO_OID_2
    val hakukohdeOidB = HAKUKOHDE_OID_2
    initSchema()
    insertHakemus()
    insertHakukohde(hakukohdeOid = hakukohdeOidB, jarjestyspaikkaOid = orgB, organisaatioOid = Some(orgB))
    insertHakutoive(hakukohdeOid = hakukohdeOidB)

    get(hakukohdeOid = None, organisaatioOid = Some(orgB))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
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
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
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
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(LAHTOKOULU_KOODI))
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulunnimi").value(LAHTOKOULU_NIMI_FI))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokka").value(LAHTOKOULU_LUOKKA))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokkataso").value(LAHTOKOULU_LUOKKATASO))
  }

  @Test
  def lahtokouluPicksLatestSuorituksenAlkuWhenOverlapping(): Unit = {
    seedMinimalHakija()
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      oppilaitosnumero = Some("09999"),
      nimiFi = Some("Vanha koulu")
    )
    val otherOid = "1.2.246.562.10.00000000000000000901"
    insertOrganisaatio(
      organisaatioOid = otherOid,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
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
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(LAHTOKOULU_KOODI))
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulunnimi").value(LAHTOKOULU_NIMI_FI))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokka").value(LAHTOKOULU_LUOKKA))
  }

  // Määrittelee UUDEN käyttäytymisen, ei vanhan: `suorituksen_alku = (SELECT MAX(...))` palautti
  // kaikki samalla alkupäivällä olevat rivit, ja service kutisti ne mielivaltaisesti .toMapilla.
  // ROW_NUMBER valitsee yhden, ja tasatilanne ratkaistaan oppilaitos_oid:lla -> sama tulos
  // riippumatta rivien lisäysjärjestyksestä tai kannan palautusjärjestyksestä.
  @Test
  def lahtokouluPicksDeterministicallyWhenTwoRowsShareSuorituksenAlku(): Unit = {
    seedMinimalHakija()
    val pienempiOid = "1.2.246.562.10.00000000000000000901"
    val suurempiOid = "1.2.246.562.10.00000000000000000902"
    insertOrganisaatio(
      organisaatioOid = suurempiOid,
      oppilaitosnumero = Some("09999"),
      nimiFi = Some("Suurempi oid")
    )
    insertOrganisaatio(
      organisaatioOid = pienempiOid,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
    // Lisätään suurempi oid ensin, jotta lisäysjärjestys ei ole se mikä testin läpäisee.
    insertHenkiloLahtokoulu(
      luokka = Some("9Z"),
      oppilaitosOid = Some(suurempiOid),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(pienempiOid),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(LAHTOKOULU_KOODI))
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
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
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
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
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
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(LAHTOKOULU_KOODI))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokka").value(LAHTOKOULU_LUOKKA))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokkataso").value(LAHTOKOULU_LUOKKATASO))
  }

  @Test
  def lahtokouluIsNullWhenHakemusJatettyIsNull(): Unit = {
    initSchema()
    insertHakemus(jatetty = None)
    insertHakukohde()
    insertHakutoive()
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
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
  def lahtokouluAndNimiAreNullWhenOrganisaatioRowMissing(): Unit = {
    seedMinimalHakija()
    // Deliberately omit insertOrganisaatio for LAHTOKOULU_OID — LEFT JOIN yields nulls
    // for both oppilaitosnumero (→ lahtokoulu) and nimi_fi/sv (→ lahtokoulunnimi).
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(nullValue()))
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

    val oppilaitoskoodiB = "05678"
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
    insertOrganisaatio(
      organisaatioOid = oppilaitosB,
      oppilaitosnumero = Some(oppilaitoskoodiB),
      nimiFi = Some("Toisen koulu")
    )
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
    assert(hA.get("lahtokoulu").asText == LAHTOKOULU_KOODI)
    assert(hA.get("luokka").asText == LAHTOKOULU_LUOKKA)
    assert(hA.get("luokkataso").asText == "9")
    val hB = byOppijanumero(oppijanumeroB).get("hakemus")
    assert(hB.get("lahtokoulu").asText == oppilaitoskoodiB)
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
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    val bytes    = getExcel().andExpect(status.isOk).andReturn().getResponse.getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val sheet     = workbook.getSheetAt(0)
      val headerRow = sheet.getRow(0)
      assert(headerRow.getCell(34).getStringCellValue == "Lahtokoulu")
      assert(headerRow.getCell(35).getStringCellValue == "Lahtokoulunnimi")
      assert(headerRow.getCell(36).getStringCellValue == "Luokka")
      assert(headerRow.getCell(37).getStringCellValue == "Luokkataso")

      val dataRow = sheet.getRow(1)
      assert(dataRow.getCell(34).getStringCellValue == LAHTOKOULU_KOODI)
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
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
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
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(LAHTOKOULU_KOODI))
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
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
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
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
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
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
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
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
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
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
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
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
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

    val bytes    = getExcel().andExpect(status.isOk).andReturn().getResponse.getContentAsByteArray
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

  // ---- Lisapistekoulutus (gen_supa_tieto, priority-picked LISAKOULUTUS_* key) ----

  @Test
  def lisapistekoulutusIsPopulatedWhenSingleKeyIsTrue(): Unit = {
    seedMinimalHakija()
    insertLisakoulutus(avain = "LISAKOULUTUS_TUVA")

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lisapistekoulutus").value("LISAKOULUTUS_TUVA"))
  }

  @Test
  def lisapistekoulutusPicksFirstByPriorityWhenMultipleAreTrue(): Unit = {
    seedMinimalHakija()
    insertLisakoulutus(avain = "LISAKOULUTUS_TUVA")
    insertLisakoulutus(avain = "LISAKOULUTUS_KYMPPI")

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lisapistekoulutus").value("LISAKOULUTUS_KYMPPI"))
  }

  @Test
  def lisapistekoulutusIgnoresRowsWithArvoFalse(): Unit = {
    seedMinimalHakija()
    insertLisakoulutus(avain = "LISAKOULUTUS_KYMPPI", arvo = Some("false"))
    insertLisakoulutus(avain = "LISAKOULUTUS_TUVA")

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lisapistekoulutus").value("LISAKOULUTUS_TUVA"))
  }

  @Test
  def lisapistekoulutusIsNullWhenAllRowsFalse(): Unit = {
    seedMinimalHakija()
    insertLisakoulutus(avain = "LISAKOULUTUS_KYMPPI", arvo = Some("false"))
    insertLisakoulutus(avain = "LISAKOULUTUS_TUVA", arvo = Some("false"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lisapistekoulutus").value(nullValue()))
  }

  @Test
  def lisapistekoulutusIsNullWhenAbsent(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lisapistekoulutus").value(nullValue()))
  }

  @Test
  def lisapistekoulutusAcceptsJsonQuotedTrue(): Unit = {
    seedMinimalHakija()
    insertLisakoulutus(avain = "LISAKOULUTUS_VALMA", arvo = Some("\"true\""))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lisapistekoulutus").value("LISAKOULUTUS_VALMA"))
  }

  @ParameterizedTest
  @ValueSource(
    strings = Array(
      "LISAKOULUTUS_KYMPPI",
      "LISAKOULUTUS_VAMMAISTEN",
      "LISAKOULUTUS_TALOUS",
      "LISAKOULUTUS_AMMATTISTARTTI",
      "LISAKOULUTUS_KANSANOPISTO",
      "LISAKOULUTUS_MAAHANMUUTTO",
      "LISAKOULUTUS_MAAHANMUUTTO_LUKIO",
      "LISAKOULUTUS_VALMA",
      "LISAKOULUTUS_OPISTOVUOSI",
      "LISAKOULUTUS_TUVA"
    )
  )
  def lisapistekoulutusMapsEveryKnownKey(avain: String): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "reset for parameterized case")
    seedMinimalHakija()
    insertLisakoulutus(avain = avain)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.lisapistekoulutus").value(avain))
  }

  @Test
  def excelWritesLisapistekoulutusCell(): Unit = {
    seedMinimalHakija()
    insertToteutusJaKoulutus()
    insertHakemusToinenAsteYhteishaku()
    insertLisakoulutus(avain = "LISAKOULUTUS_VAMMAISTEN")

    val bytes    = getExcel().andExpect(status.isOk).andReturn().getResponse.getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val sheet     = workbook.getSheetAt(0)
      val headerRow = sheet.getRow(0)
      assert(headerRow.getCell(44).getStringCellValue == "Lisapistekoulutus")

      val dataRow = sheet.getRow(1)
      assert(dataRow.getCell(44).getStringCellValue == "LISAKOULUTUS_VAMMAISTEN")
    } finally workbook.close()
  }

  // ---- Yhteispisteet (gen_valintarekisteri) ----

  @Test
  def yhteispisteetIsPopulatedWhenSingleRowExists(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(pisteet = Some(BigDecimal("82.5")))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].yhteispisteet").value(82.5))
  }

  @Test
  def yhteispisteetTiebreaksVarallaByVarasijanNumero(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-a",
      valinnanTila = Some("VARALLA"),
      varasijanNumero = Some(5),
      pisteet = Some(BigDecimal("90"))
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-b",
      valinnanTila = Some("VARALLA"),
      varasijanNumero = Some(1),
      pisteet = Some(BigDecimal("50"))
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].yhteispisteet").value(50))
  }

  @ParameterizedTest
  @ValueSource(
    strings = Array(
      "HYVAKSYTTY",
      "HARKINNANVARAISESTI_HYVAKSYTTY",
      "VARASIJALTA_HYVAKSYTTY",
      "PERUUTETTU",
      "PERUNUT",
      "PERUUNTUNUT",
      "HYLATTY",
      "KESKEN"
    )
  )
  def yhteispisteetTiebreaksNonVarallaStatesByPrioriteetti(state: String): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "reset for parameterized case")
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-a",
      valinnanTila = Some(state),
      prioriteetti = Some(2),
      pisteet = Some(BigDecimal("90"))
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-b",
      valinnanTila = Some(state),
      prioriteetti = Some(1),
      pisteet = Some(BigDecimal("60"))
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].yhteispisteet").value(60))
  }

  @Test
  def yhteispisteetVarallaWithNullVarasijanNumeroLosesToNumbered(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-a",
      valinnanTila = Some("VARALLA"),
      varasijanNumero = None,
      pisteet = Some(BigDecimal("90"))
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-b",
      valinnanTila = Some("VARALLA"),
      varasijanNumero = Some(5),
      pisteet = Some(BigDecimal("50"))
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].yhteispisteet").value(50))
  }

  @Test
  def yhteispisteetNonVarallaWithNullPrioriteettiLosesToNumbered(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-a",
      valinnanTila = Some("HYVAKSYTTY"),
      prioriteetti = None,
      pisteet = Some(BigDecimal("90"))
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-b",
      valinnanTila = Some("HYVAKSYTTY"),
      prioriteetti = Some(5),
      pisteet = Some(BigDecimal("50"))
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].yhteispisteet").value(50))
  }

  @Test
  def yhteispisteetExcludesJulkaistavissaFalse(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(julkaistavissa = Some(false), pisteet = Some(BigDecimal("77")))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].yhteispisteet").value(nullValue()))
  }

  @Test
  def yhteispisteetIsNullWhenNoRows(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].yhteispisteet").value(nullValue()))
  }

  @ParameterizedTest
  @ValueSource(
    strings = Array(
      "HYVAKSYTTY",
      "HARKINNANVARAISESTI_HYVAKSYTTY",
      "VARASIJALTA_HYVAKSYTTY",
      "VARALLA",
      "PERUUTETTU",
      "PERUNUT",
      "PERUUNTUNUT",
      "HYLATTY",
      "KESKEN"
    )
  )
  def yhteispisteetSurvivesEveryValinnanTilaWhenAlone(state: String): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "reset for parameterized case")
    seedMinimalHakija()
    insertValintarekisteri(valinnanTila = Some(state), pisteet = Some(BigDecimal("42")))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].yhteispisteet").value(42))
  }

  @ParameterizedTest
  @CsvSource(
    Array(
      "HYVAKSYTTY,                     HARKINNANVARAISESTI_HYVAKSYTTY",
      "HARKINNANVARAISESTI_HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY",
      "VARASIJALTA_HYVAKSYTTY,         VARALLA",
      "VARALLA,                        PERUUTETTU",
      "PERUUTETTU,                     PERUNUT",
      "PERUNUT,                        PERUUNTUNUT",
      "PERUUNTUNUT,                    HYLATTY",
      "HYLATTY,                        KESKEN"
    )
  )
  def yhteispisteetPrefersHigherPriorityStateOverLower(winner: String, loser: String): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "reset for parameterized case")
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-winner",
      valinnanTila = Some(winner),
      pisteet = Some(BigDecimal("80"))
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-loser",
      valinnanTila = Some(loser),
      pisteet = Some(BigDecimal("99"))
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].yhteispisteet").value(80))
  }

  @Test
  def excelWritesYhteispisteetCell(): Unit = {
    seedMinimalHakija()
    insertToteutusJaKoulutus()
    insertHakemusToinenAsteYhteishaku()
    insertValintarekisteri(pisteet = Some(BigDecimal("77.5")))

    val bytes    = getExcel().andExpect(status.isOk).andReturn().getResponse.getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val sheet     = workbook.getSheetAt(0)
      val headerRow = sheet.getRow(0)
      assert(headerRow.getCell(55).getStringCellValue == "Yhteispisteet")

      val dataRow = sheet.getRow(1)
      assert(dataRow.getCell(55).getStringCellValue == "77.5")
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
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
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
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(LAHTOKOULU_KOODI))
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
  def excelReturns403WhenUserMissingRole(): Unit =
    getExcel().andExpect(status.isForbidden)

  // 401 anonymous is proved by the JSON returns401WhenNoUser test — the Spring Security
  // filter chain fires before the controller, so it's not endpoint-specific.

  @ParameterizedTest
  @CsvSource(
    value = Array(
      "not-oid                             | 1.2.246.562.20.00000000000000000012 | null              | HAKENEET  | hakuOid.invalid.oid",
      "1.2.246.562.29.00000000000000000100 | not-oid                             | null              | HAKENEET  | hakukohdeOid.invalid.oid",
      "1.2.246.562.29.00000000000000000100 | null                                | 1.2.246.562.20.1  | HAKENEET  | organisaatioOid.invalid.org",
      "1.2.246.562.29.00000000000000000100 | null                                | null              | HAKENEET  | hakukohdeOid_or_organisaatioOid.required",
      "1.2.246.562.29.00000000000000000100 | 1.2.246.562.20.00000000000000000012 | null              | BOGUS     | valintarajaus.invalid",
      "1.2.246.562.29.00000000000000000100 | 1.2.246.562.20.00000000000000000012 | null              | hyvaksytyt| valintarajaus.invalid"
    ),
    delimiter = '|',
    nullValues = Array("null")
  )
  def excelReturns400OnValidationFailure(
    hakuOid: String,
    hakukohdeOid: String,
    organisaatioOid: String,
    valintarajaus: String,
    expectedDetail: String
  ): Unit = {
    getExcel(
      hakuOid = hakuOid,
      hakukohdeOid = Option(hakukohdeOid),
      organisaatioOid = Option(organisaatioOid),
      valintarajaus = Option(valintarajaus)
    )
      .andExpect(status.isBadRequest)
      .andExpect(content.contentType("application/json"))
      .andExpect(jsonPath("$.message").value("virhe.validointi"))
      .andExpect(jsonPath("$.details[0]").value(expectedDetail))
  }

  @Test
  def excelReturns500WhenServiceFails(): Unit =
    // No initSchema → gen.* tables missing → service returns Left("virhe.tietokanta")
    getExcel()
      .andExpect(status.isInternalServerError)
      .andExpect(content.json("\"virhe.tietokanta\""))

  @Test
  @WithMockUser(username = "hakeneet-user", roles = Array("APP_OVARA-VIRKAILIJA_HAKENEET_1.2.246.562.10.00000000001"))
  def excelReturns200ForHakeneetRole(): Unit = {
    seedMinimalHakija()
    insertToteutusJaKoulutus()
    insertHakemusToinenAsteYhteishaku()

    getExcel()
      .andExpect(status.isOk)
      .andExpect(content.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
  }

  @Test
  def excelReturns400WhenNeitherHakukohdeNorOrganisaatioProvided(): Unit =
    getExcel(hakukohdeOid = None, organisaatioOid = None)
      .andExpect(status.isBadRequest)
      .andExpect(
        content.json(
          """{"status": 400, "message": "virhe.validointi", "details": ["hakukohdeOid_or_organisaatioOid.required"] }"""
        )
      )

  @Test
  def excelEmitsOneRowPerHakutoive(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, hakutoivenumero = 1)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2)

    val bytes = getExcel(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andReturn()
      .getResponse
      .getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val sheet = workbook.getSheetAt(0)
      assert(
        sheet.getLastRowNum == 2,
        s"expected header + 2 data rows; last row index = ${sheet.getLastRowNum}"
      )
      val row1 = sheet.getRow(1)
      val row2 = sheet.getRow(2)
      // Same hakija-level fields on both rows
      assert(row1.getCell(1).getStringCellValue == OPPIJANUMERO)
      assert(row2.getCell(1).getStringCellValue == OPPIJANUMERO)
      assert(row1.getCell(31).getStringCellValue == HAKEMUS_OID)
      assert(row2.getCell(31).getStringCellValue == HAKEMUS_OID)
      // Per-hakutoive fields differ
      assert(row1.getCell(47).getStringCellValue == "1") // Hakujno
      assert(row2.getCell(47).getStringCellValue == "2")
      assert(row1.getCell(52).getStringCellValue == HAKUKOHDE_OID)
      assert(row2.getCell(52).getStringCellValue == HAKUKOHDE_OID_2)
    } finally workbook.close()
  }

  @Test
  def excelExportsHeaderOnlySheetWhenNoHakijatMatch(): Unit = {
    initSchema()
    val bytes = getExcel()
      .andExpect(status.isOk)
      .andExpect(content.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
      .andReturn()
      .getResponse
      .getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val sheet = workbook.getSheetAt(0)
      assert(
        sheet.getLastRowNum == 0,
        s"expected header-only sheet; last row index = ${sheet.getLastRowNum}"
      )
      assert(sheet.getRow(0).getCell(0).getStringCellValue == "Hetu")
    } finally workbook.close()
  }

  @Test
  def excelHasHeadersInExpectedOrderAndDataRow(): Unit = {
    seedMinimalHakija()
    insertToteutusJaKoulutus()
    insertHakemusToinenAsteYhteishaku()

    val result = getExcel()
      .andExpect(status.isOk)
      .andExpect(content.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
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
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(harkinnanvaraisuudenSyy = Some("ATARU_OPPIMISVAIKEUDET")) // → cell 53 = "1"
    insertToteutusJaKoulutus()
    insertOpetuskieli()
    insertOrganisaatio(nimiFi =
      Some(OPETUSPISTE_NIMI_FI)
    ) // ORGANISAATIO_OID; also joined via jarjestyspaikka_oid → cells 48 + 50
    insertOrganisaatio(
      organisaatioOid = LAHTOKOULU_OID,
      oppilaitosnumero = Some(LAHTOKOULU_KOODI),
      nimiFi = Some(LAHTOKOULU_NIMI_FI)
    )
    insertHakemusToinenAsteYhteishaku()
    insertPohjakoulutus(arvo = Some("2"))
    insertTodistusvuosi(arvo = Some("2025"))
    insertLisakoulutus(avain = "LISAKOULUTUS_TUVA")
    insertValintarekisteri(pisteet = Some(BigDecimal("82.5")))
    insertValintalaskentaFunktiotulos(tunniste = "keskiarvo_pk", arvo = Some("8.75")) // → cell 46
    insertHenkiloLahtokoulu(
      luokka = Some(LAHTOKOULU_LUOKKA),
      oppilaitosOid = Some(LAHTOKOULU_OID),
      suoritusTyyppi = Some(LAHTOKOULU_SUORITUSTYYPPI),
      suorituksenAlku = Some(LAHTOKOULU_ALKU),
      suorituksenLoppu = Some(LAHTOKOULU_LOPPU)
    )

    val result   = getExcel().andExpect(status.isOk).andReturn()
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
        34 -> LAHTOKOULU_KOODI,
        35 -> LAHTOKOULU_NIMI_FI,
        36 -> LAHTOKOULU_LUOKKA,
        37 -> LAHTOKOULU_LUOKKATASO,
        38 -> "2",
        39 -> "2025",
        40 -> "X",
        41 -> "0",
        42 -> "0",
        43 -> "0",
        44 -> "LISAKOULUTUS_TUVA",
        45 -> "0",
        46 -> "8.75",
        47 -> "1",
        48 -> OPPILAITOS,
        49 -> ORGANISAATIO_OID,
        50 -> OPETUSPISTE_NIMI_FI,
        51 -> "Kulttuurituottaja",
        52 -> HAKUKOHDE_OID,
        53 -> "1",
        54 -> "",
        55 -> "82.5",
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
  def opetuspisteennimiPopulatedFromJarjestyspaikkaOid(): Unit = {
    // Distinct jarjestyspaikka_oid vs organisaatio_oid, distinct nimi values on each org.
    // Proves the opetuspisteennimi join uses jarjestyspaikka_oid, not organisaatio_oid.
    initSchema()
    insertHakemus()
    insertHakukohde(jarjestyspaikkaOid = JARJESTYSPAIKKA_OID)
    insertHakutoive()
    insertOrganisaatio(nimiFi = Some("Wrong nimi from organisaatio_oid row"))
    insertOrganisaatio(
      organisaatioOid = JARJESTYSPAIKKA_OID,
      oppilaitosnumero = None,
      nimiFi = Some(OPETUSPISTE_NIMI_FI),
      nimiSv = Some(OPETUSPISTE_NIMI_SV),
      nimiEn = Some(OPETUSPISTE_NIMI_EN)
    )

    get(organisaatioOid = Some(JARJESTYSPAIKKA_OID), hakukohdeOid = None)
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].opetuspiste").value(JARJESTYSPAIKKA_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].opetuspisteennimi.fi").value(OPETUSPISTE_NIMI_FI))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].opetuspisteennimi.sv").value(OPETUSPISTE_NIMI_SV))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].opetuspisteennimi.en").value(OPETUSPISTE_NIMI_EN))
  }

  @Test
  def opetuspisteennimiEmptyWhenAllNimiNull(): Unit = {
    seedMinimalHakija()
    insertOrganisaatio() // ORGANISAATIO_OID row with nimi_fi/sv/en all None (defaults)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].opetuspisteennimi").value(nullValue()))
  }

  @Test
  def opetuspisteennimiPartialWhenSomeNimiNull(): Unit = {
    seedMinimalHakija()
    insertOrganisaatio(nimiFi = Some(OPETUSPISTE_NIMI_FI)) // sv + en null

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].opetuspisteennimi.fi").value(OPETUSPISTE_NIMI_FI))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].opetuspisteennimi.sv").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].opetuspisteennimi.en").doesNotExist())
  }

  @ParameterizedTest
  @CsvSource(
    Array(
      "ATARU_OPPIMISVAIKEUDET,                    1",
      "ATARU_SOSIAALISET_SYYT,                    2",
      "ATARU_KOULUTODISTUSTEN_VERTAILUVAIKEUDET,  3",
      "ATARU_ULKOMAILLA_OPISKELTU,                3",
      "SURE_EI_PAATTOTODISTUSTA,                  4",
      "ATARU_EI_PAATTOTODISTUSTA,                 4",
      "ATARU_RIITTAMATON_TUTKINTOKIELEN_TAITO,    5",
      "SURE_YKS_MAT_AI,                           6",
      "ATARU_YKS_MAT_AI,                          6"
    )
  )
  def harkinnanvaraisuusperusteMapsKnownCodes(syy: String, expected: String): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "reset for parameterized case")
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(harkinnanvaraisuudenSyy = Some(syy))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].harkinnanvaraisuusperuste").value(expected))
  }

  @ParameterizedTest
  @ValueSource(strings = Array("EI_HARKINNANVARAINEN", "EI_HARKINNANVARAINEN_HAKUKOHDE"))
  def harkinnanvaraisuusperusteNullForEiHarkinnanvarainen(syy: String): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "reset for parameterized case")
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(harkinnanvaraisuudenSyy = Some(syy))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].harkinnanvaraisuusperuste").value(nullValue()))
  }

  @Test
  def harkinnanvaraisuusperusteNullWhenColumnNull(): Unit = {
    seedMinimalHakija() // insertHakutoive default harkinnanvaraisuudenSyy = None

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].harkinnanvaraisuusperuste").value(nullValue()))
  }

  @Test
  def harkinnanvaraisuusperusteUnknownMapsTo999(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(harkinnanvaraisuudenSyy = Some("NEW_CODE_XYZ"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].harkinnanvaraisuusperuste").value("999"))
  }

  @ParameterizedTest
  @ValueSource(strings = Array("keskiarvo_pk", "keskiarvo_lk", "painotettu_keskiarvo"))
  def keskiarvoPopulatedFromValintalaskentaFunktiotulokset(tunniste: String): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "reset for parameterized case")
    seedMinimalHakija()
    insertValintalaskentaFunktiotulos(tunniste = tunniste, arvo = Some("8.75"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].keskiarvo").value("8.75"))
  }

  @Test
  def keskiarvoNullWhenFunktiotuloksetIsEmpty(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].keskiarvo").value(nullValue()))
  }

  @ParameterizedTest
  @CsvSource(
    value = Array(
      "keskiarvo_pk | ''",
      "keskiarvo_lk | null"
    ),
    delimiter = '|',
    nullValues = Array("null"),
    quoteCharacter = '\''
  )
  def keskiarvoNullWhenArvoIsEmptyOrNull(tunniste: String, arvo: String): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "reset for parameterized case")
    seedMinimalHakija()
    insertValintalaskentaFunktiotulos(tunniste = tunniste, arvo = Option(arvo))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].keskiarvo").value(nullValue()))
  }

  @Test
  def keskiarvoIgnoresIrrelevantTunniste(): Unit = {
    seedMinimalHakija()
    insertValintalaskentaFunktiotulos(tunniste = "some_other_tunniste", arvo = Some("9.5"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].keskiarvo").value(nullValue()))
  }

  @Test
  def keskiarvoPerHakutoiveDiffersOnSameHakemus(): Unit = {
    seedMinimalHakija()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2)
    insertValintalaskentaFunktiotulos(
      hakukohdeOid = HAKUKOHDE_OID,
      tunniste = "keskiarvo_pk",
      arvo = Some("7.20")
    )
    insertValintalaskentaFunktiotulos(
      hakukohdeOid = HAKUKOHDE_OID_2,
      tunniste = "painotettu_keskiarvo",
      arvo = Some("9.10")
    )

    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[0].keskiarvo").value("7.20"))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakutoiveet[1].keskiarvo").value("9.10"))
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

    getExcel(
      hakukohdeOid = None,
      organisaatioOid = Some(ORGANISAATIO_OID),
      headers = Map("User-Agent" -> "ovara-integration-test/1.0")
    ).andExpect(status.isOk)

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
