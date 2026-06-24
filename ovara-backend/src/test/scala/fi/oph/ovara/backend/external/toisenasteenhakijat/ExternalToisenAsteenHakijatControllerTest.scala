package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.external.toisenasteenhakijat.ExternalToisenAsteenHakijatTestData.*
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.utils.{AuditLog, AuditOperation}
import fi.vm.sade.auditlog.Operation
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.{BeforeEach, Test}
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Import, Primary}
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.{WithAnonymousUser, WithMockUser}
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.{MockMvc, ResultActions}
import org.springframework.test.web.servlet.request.{MockHttpServletRequestBuilder, MockMvcRequestBuilders}
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
    insertOpetuskieli()

    get()()
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
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakemusnumero").value(HAKEMUS_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemus.julkaisulupa").value(VALINTATULOKSEN_JULKAISULUPA))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakemuksenJattopaiva").value(JATETTY.toString))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakemuksenMuokkauspaiva").value(MUOKATTU.toString))
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
  def fieldsWithoutDataSourceAreNullOrEmpty(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()

    get()()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].muupuhelin").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].huoltaja1").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].huoltaja2").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].oikeusMaksuttomaanKoulutukseenVoimassaAsti").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].oppivelvollisuusVoimassaAsti").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].lisakysymykset").isEmpty)
      .andExpect(jsonPath("$.hakijat[0].hakemus.vuosi").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.kausi").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.lahtokoulu").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.luokka").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemus.pohjakoulutus").value(nullValue()))
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

    get()()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakemuksenJattopaiva").value("2025-08-01T10:00:00+03:00"))
      .andExpect(jsonPath("$.hakijat[0].hakemus.hakemuksenMuokkauspaiva").value("2025-08-13T14:52:00+03:00"))
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
      )
      .andExpect(status.isForbidden)
  }

  @Test
  def excelReturns400WhenNeitherHakukohdeNorOrganisaatioProvided(): Unit = {
    mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/external/toisenasteenhakijat/excel")
          .param("hakuOid", HAKU_OID)
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
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertToteutusJaKoulutus()

    val result = mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/external/toisenasteenhakijat/excel")
          .param("hakuOid", HAKU_OID)
          .param("hakukohdeOid", HAKUKOHDE_OID)
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
      assert(headerRow.getCell(77).getStringCellValue == "Sähköisen asioinnin lupa")

      val dataRow = sheet.getRow(1)
      assert(dataRow.getCell(0).getStringCellValue == HETU)
      assert(dataRow.getCell(1).getStringCellValue == OPPIJANUMERO)
      assert(dataRow.getCell(2).getStringCellValue == SUKUNIMI)
      assert(dataRow.getCell(3).getStringCellValue == ETUNIMET)
      assert(dataRow.getCell(4).getStringCellValue == KUTSUMANIMI)
      assert(dataRow.getCell(8).getStringCellValue == SUOMI_KOODI)
      assert(dataRow.getCell(9).getStringCellValue == "246")
      assert(dataRow.getCell(25).getStringCellValue == "Kyllä") // koulutusmarkkinointilupa = true
      assert(dataRow.getCell(26).getStringCellValue == "Ei")    // kiinnostunutoppisopimuksesta = false
      assert(dataRow.getCell(31).getStringCellValue == HAKEMUS_OID)
      assert(dataRow.getCell(40).getStringCellValue == "Kyllä") // julkaisulupa
      assert(dataRow.getCell(47).getStringCellValue == "1")     // hakujno
      assert(dataRow.getCell(49).getStringCellValue == ORGANISAATIO_OID)
      assert(dataRow.getCell(52).getStringCellValue == HAKUKOHDE_OID)
      assert(dataRow.getCell(77).getStringCellValue == "Kyllä") // sahkoinenviestintalupa
    } finally workbook.close()
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

  @Test
  def jsonEndpointEmitsAuditLogEntryOnSuccess(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()

    get(organisaatioOid = None)().andExpect(status.isOk)

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
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertToteutusJaKoulutus()

    mvc
      .perform(
        MockMvcRequestBuilders
          .get("/api/external/toisenasteenhakijat/excel")
          .param("hakuOid", HAKU_OID)
          .param("organisaatioOid", ORGANISAATIO_OID)
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
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()

    get(organisaatioOid = None)().andExpect(status.isOk)

    assert(
      recordedAuditCalls.head.principalName.contains("another-user"),
      s"audit entry must carry the calling user; got ${recordedAuditCalls.head.principalName}"
    )
  }

  @Test
  @WithMockUser(username = "testuser", roles = Array("USER"))
  def forbiddenRequestEmitsNoAuditEntry(): Unit = {
    get()().andExpect(status.isForbidden)
    assert(recordedAuditCalls.isEmpty, s"expected no audit entry, got $recordedAuditCalls")
  }

  @Test
  def validationErrorEmitsNoAuditEntry(): Unit = {
    get(hakukohdeOid = None, organisaatioOid = None)().andExpect(status.isBadRequest)
    assert(recordedAuditCalls.isEmpty, s"expected no audit entry, got $recordedAuditCalls")
  }

  @Test
  def auditEntryRecordedEvenWhenDbQueryFails(): Unit = {
    // No initSchema call → gen.* tables don't exist → service returns Left("virhe.tietokanta")
    get()().andExpect(status.isInternalServerError)

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
