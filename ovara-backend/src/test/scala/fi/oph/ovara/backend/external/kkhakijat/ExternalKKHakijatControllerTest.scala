package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.external.kkhakijat.ExternalKKHakijatTestData.*
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.utils.{AuditLog, AuditOperation}
import fi.vm.sade.auditlog.Operation
import jakarta.servlet.http.HttpServletRequest
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.{BeforeEach, Test}
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.{CsvSource, ValueSource}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.{SpringBootTest, TestConfiguration}
import org.springframework.context.annotation.{Bean, Import, Primary}
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.{WithAnonymousUser, WithMockUser}
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, header, jsonPath, status}
import org.springframework.test.web.servlet.{MockMvc, ResultActions}
import slick.jdbc.H2Profile.api.*

import java.io.ByteArrayInputStream
import scala.jdk.CollectionConverters.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(Array("test"))
@Import(Array(classOf[ExternalKKHakijatControllerTest.RecordingAuditConfig]))
@WithMockUser(
  username = "testuser",
  roles = Array("APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA_1.2.246.562.10.00000000001")
)
class ExternalKKHakijatControllerTest extends ExternalKKHakijatTestUtils {

  @Autowired
  private val mvc: MockMvc = null

  @Autowired
  override val db: ReadOnlyDatabase = null

  @BeforeEach
  def clearDb(): Unit =
    db.run(sqlu"""DROP ALL OBJECTS""", "Drop everything")

  @BeforeEach
  def clearAuditRecord(): Unit =
    ExternalKKHakijatControllerTest.recordedAuditCalls.clear()

  private def recordedAuditCalls: List[ExternalKKHakijatControllerTest.AuditCall] =
    ExternalKKHakijatControllerTest.recordedAuditCalls.asScala.toList

  private def get(
    hakuOid: String = HAKU_OID,
    hakukohdeOid: Option[String] = Some(HAKUKOHDE_OID),
    organisaatioOid: Option[String] = None,
    valintarajaus: Option[String] = Some("HAKENEET")
  ): ResultActions = {
    var req = MockMvcRequestBuilders
      .get("/api/external/kkhakijat")
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
      .get("/api/external/kkhakijat/excel")
      .param("hakuOid", hakuOid)
    hakukohdeOid.foreach(v => req = req.param("hakukohdeOid", v))
    organisaatioOid.foreach(v => req = req.param("organisaatioOid", v))
    valintarajaus.foreach(v => req = req.param("valintarajaus", v))
    headers.foreach { case (k, v) => req = req.header(k, v) }
    mvc.perform(req)
  }

  // ---- Auth ----

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
  @WithMockUser(
    username = "hakeneet-user",
    roles = Array("APP_OVARA-VIRKAILIJA_HAKENEET_1.2.246.562.10.00000000001")
  )
  def returns403ForHakeneetRoleFirstSlice(): Unit = {
    // First slice: only OPH_PAAKAYTTAJA — HAKENEET (2Aste authority) has no access here.
    get().andExpect(status.isForbidden)
  }

  @Test
  def returns200ForPaakayttajaRole(): Unit = {
    initSchema()

    get()
      .andExpect(status.isOk)
      .andExpect(content.json("""{"hakijat": []}"""))
  }

  // ---- Validation ----

  @ParameterizedTest
  @ValueSource(strings = Array("not-oid", "1.2", "1.2.246", "1.2.246.1"))
  def returns400WhenHakuOidInvalid(bad: String): Unit = {
    get(hakuOid = bad)
      .andExpect(status.isBadRequest)
      .andExpect(jsonPath("$.details", hasItem[Any]("hakuOid.invalid.oid")))
  }

  @Test
  def returns400WhenNeitherHakukohdeNorOrganisaatioProvided(): Unit = {
    get(hakukohdeOid = None, organisaatioOid = None)
      .andExpect(status.isBadRequest)
      .andExpect(jsonPath("$.details", hasItem[Any]("hakukohdeOid_or_organisaatioOid.required")))
  }

  @Test
  def returns400WhenValintarajausInvalid(): Unit = {
    get(valintarajaus = Some("SOMETHING_ELSE"))
      .andExpect(status.isBadRequest)
      .andExpect(jsonPath("$.details", hasItem[Any]("valintarajaus.invalid")))
  }

  // ---- Happy path JSON ----

  @Test
  def returnsHakijaFilteredByHakukohdeOid(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(valinnanTila = Some(VALINTATIETO))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
      .andExpect(jsonPath("$.hakijat[0].hetu").value(HETU))
      .andExpect(jsonPath("$.hakijat[0].sukunimi").value(SUKUNIMI))
      .andExpect(jsonPath("$.hakijat[0].etunimet").value(ETUNIMET))
      .andExpect(jsonPath("$.hakijat[0].kutsumanimi").value(KUTSUMANIMI))
      .andExpect(jsonPath("$.hakijat[0].lahiosoite").value(LAHIOSOITE))
      .andExpect(jsonPath("$.hakijat[0].postinumero").value(POSTINUMERO))
      .andExpect(jsonPath("$.hakijat[0].postitoimipaikka").value(HELSINKI))
      .andExpect(jsonPath("$.hakijat[0].maa").value(SUOMI_KOODI))
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet[0]").value("246"))
      .andExpect(jsonPath("$.hakijat[0].matkapuhelin").value(MATKAPUHELIN))
      .andExpect(jsonPath("$.hakijat[0].sahkoposti").value(EMAIL))
      .andExpect(jsonPath("$.hakijat[0].kotikunta").value(KOTIKUNTA))
      .andExpect(jsonPath("$.hakijat[0].sukupuoli").value(SUKUPUOLI.toString))
      .andExpect(jsonPath("$.hakijat[0].aidinkieli").value(AIDINKIELI))
      .andExpect(jsonPath("$.hakijat[0].koulutusmarkkinointilupa").value(KOULUTUSMARKKINOINTILUPA))
      .andExpect(jsonPath("$.hakijat[0].hakemukset", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].haku").value(HAKU_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakuVuosi").value(VUOSI))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakuKausi").value(KAUSI))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakemusnumero").value(HAKEMUS_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakemusJattoAikaleima").value(JATETTY_STR))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakemusViimeinenMuokkausAikaleima").value(MUOKATTU_STR))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].organisaatio").value(ORGANISAATIO_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohde").value(HAKUKOHDE_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakutoivePrioriteetti").value(1))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value(VALINTATIETO))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value(VASTAANOTTOTIETO))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].ilmoittautumiset[0]").value(ILMOITTAUTUMISEN_TILA))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].julkaisulupa").value(VALINTATULOKSEN_JULKAISULUPA))
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
  def deferredFieldsShipAsDefaults(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].asiointikieli").value(""))
      .andExpect(jsonPath("$.hakijat[0].syntymaaika").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].puhelin").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].onYlioppilas").value(false))
      .andExpect(jsonPath("$.hakijat[0].yoSuoritusVuosi").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].turvakielto").value(false))
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohdeKkId").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].avoinVayla").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanAikaleima").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].lukuvuosimaksu").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pohjakoulutus").isEmpty)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuus").value(""))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset").isEmpty)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].liitteet").value(nullValue()))
  }

  // ---- Filter correctness ----

  @Test
  def excludesHaunkohdejoukkoOtherThan12(): Unit = {
    initSchema()
    insertHenkilo()
    insertHaku(kohdejoukkoKoodiuri = "haunkohdejoukko_11#1")
    insertHakemus(insertHenkilo = false, insertHaku = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(content.json("""{"hakijat": []}"""))
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

  // ---- Enum parsing ----

  @ParameterizedTest
  @ValueSource(
    strings = Array(
      "HYVAKSYTTY",
      "HARKINNANVARAISESTI_HYVAKSYTTY",
      "VARASIJALTA_HYVAKSYTTY",
      "VARALLA",
      "HYLATTY",
      "PERUNUT",
      "PERUUNTUNUT",
      "PERUUTETTU",
      "KESKEN"
    )
  )
  def valinnanTilaParsesKnownStates(state: String): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "reset for parameterized case")
    seedMinimalHakija()
    insertValintarekisteri(valinnanTila = Some(state))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value(state))
  }

  @Test
  def valinnanTilaIsNullForUnknownValue(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(valinnanTila = Some("SOMETHING_ELSE"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value(nullValue()))
  }

  // ---- Lukuvuosimaksu ----

  @ParameterizedTest
  @ValueSource(strings = Array("MAKSETTU", "MAKSAMATTA", "VAPAUTETTU"))
  def lukuvuosimaksuPopulatedFromValintarekisteri(tila: String): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "reset for parameterized case")
    seedMinimalHakija()
    insertValintarekisteri(maksunTila = Some(tila))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].lukuvuosimaksu").value(tila))
  }

  @Test
  def lukuvuosimaksuNullWhenValintarekisteriEmpty(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].lukuvuosimaksu").value(nullValue()))
  }

  @Test
  def lukuvuosimaksuNullWhenMaksunTilaIsNull(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(maksunTila = None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].lukuvuosimaksu").value(nullValue()))
  }

  @Test
  def lukuvuosimaksuPerHakemusHakukohdeDiffers(): Unit = {
    seedMinimalHakija()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2)
    insertValintarekisteri(hakukohdeOid = HAKUKOHDE_OID, maksunTila = Some("MAKSETTU"))
    insertValintarekisteri(hakukohdeOid = HAKUKOHDE_OID_2, valintatapajonoId = "vtj-2", maksunTila = Some("VAPAUTETTU"))

    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].lukuvuosimaksu").value("MAKSETTU"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[1].lukuvuosimaksu").value("VAPAUTETTU"))
  }

  // ---- HakukohdeKkId ----

  @Test
  def hakukohdeKkIdPopulatedFromKoulutus(): Unit = {
    seedMinimalHakija()
    insertToteutus()
    insertKoulutus(ulkoinenTunniste = Some("TKID-1"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohdeKkId").value("TKID-1"))
  }

  @Test
  def hakukohdeKkIdNullWhenKoulutusRowMissing(): Unit = {
    seedMinimalHakija()
    insertToteutus() // no matching gen_koulutus row

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohdeKkId").value(nullValue()))
  }

  @Test
  def hakukohdeKkIdNullWhenToteutusRowMissing(): Unit = {
    seedMinimalHakija() // no toteutus row; hakukohde.toteutus_oid dangles

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohdeKkId").value(nullValue()))
  }

  @Test
  def hakukohdeKkIdNullWhenUlkoinenTunnisteIsNull(): Unit = {
    seedMinimalHakija()
    insertToteutus()
    insertKoulutus(ulkoinenTunniste = None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohdeKkId").value(nullValue()))
  }

  @Test
  def hakukohdeKkIdPerHakemusDiffers(): Unit = {
    val toteutusOid2 = "1.2.246.562.17.99999999999999999999"
    val koulutusOid2 = "1.2.246.562.13.99999999999999999999"
    seedMinimalHakija()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2, toteutusOid = toteutusOid2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2)
    insertToteutus(toteutusOid = TOTEUTUS_OID, koulutusOid = KOULUTUS_OID)
    insertToteutus(toteutusOid = toteutusOid2, koulutusOid = koulutusOid2)
    insertKoulutus(koulutusOid = KOULUTUS_OID, ulkoinenTunniste = Some("TKID-A"))
    insertKoulutus(koulutusOid = koulutusOid2, ulkoinenTunniste = Some("TKID-B"))

    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohdeKkId").value("TKID-A"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[1].hakukohdeKkId").value("TKID-B"))
  }

  // ---- ValinnanAikaleima ----

  private val TS_HYVAKSYTTY = java.time.OffsetDateTime.parse("2025-10-01T12:00:00+03:00")
  private val TS_VARALLA    = java.time.OffsetDateTime.parse("2025-10-02T12:00:00+03:00")

  @Test
  def valinnanAikaleimaPopulatedFromValintarekisteri(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valinnanTila = Some("HYVAKSYTTY"),
      hyvaksyttyjajulkaistu = Some(TS_HYVAKSYTTY)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanAikaleima").value("2025-10-01T12:00:00+03:00"))
  }

  @Test
  def valinnanAikaleimaNullWhenTimestampIsNull(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"), hyvaksyttyjajulkaistu = None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanAikaleima").value(nullValue()))
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
  def valinnanTilaPrefersHigherPriorityStateOverLower(winner: String, loser: String): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "reset for parameterized case")
    seedMinimalHakija()
    insertValintarekisteri(valintatapajonoId = "vtj-winner", valinnanTila = Some(winner))
    insertValintarekisteri(valintatapajonoId = "vtj-loser", valinnanTila = Some(loser))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value(winner))
  }

  @Test
  def valinnanTilaAndAikaleimaFromSamePriorityRow(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-winner",
      valinnanTila = Some("HYVAKSYTTY"),
      hyvaksyttyjajulkaistu = Some(TS_HYVAKSYTTY)
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-loser",
      valinnanTila = Some("VARALLA"),
      hyvaksyttyjajulkaistu = Some(TS_VARALLA)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("HYVAKSYTTY"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanAikaleima").value("2025-10-01T12:00:00+03:00"))
  }

  // ---- Pisteet ----

  @Test
  def pisteetPopulatedFromValintarekisteri(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valinnanTila = Some("HYVAKSYTTY"),
      pisteet = Some(BigDecimal("82.5"))
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").value(82.5))
  }

  @Test
  def pisteetNullWhenNoValintarekisteriRow(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").value(nullValue()))
  }

  @Test
  def pisteetIgnoresUnpublishedRows(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valinnanTila = Some("HYVAKSYTTY"),
      julkaistavissa = Some(false),
      pisteet = Some(BigDecimal("80"))
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").value(nullValue()))
  }

  @Test
  def pisteetPrefersHigherPriorityState(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-winner",
      valinnanTila = Some("HYVAKSYTTY"),
      pisteet = Some(BigDecimal("80"))
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-loser",
      valinnanTila = Some("VARALLA"),
      pisteet = Some(BigDecimal("99"))
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").value(80))
  }

  @Test
  def pisteetSecondaryTiebreakByPrioriteetti(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-winner",
      valinnanTila = Some("HYVAKSYTTY"),
      prioriteetti = Some(1),
      pisteet = Some(BigDecimal("88"))
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-loser",
      valinnanTila = Some("HYVAKSYTTY"),
      prioriteetti = Some(2),
      pisteet = Some(BigDecimal("99"))
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").value(88))
  }

  @Test
  def pisteetSecondaryTiebreakByVarasijanNumeroForVaralla(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-winner",
      valinnanTila = Some("VARALLA"),
      varasijanNumero = Some(1),
      pisteet = Some(BigDecimal("70"))
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-loser",
      valinnanTila = Some("VARALLA"),
      varasijanNumero = Some(2),
      pisteet = Some(BigDecimal("90"))
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").value(70))
  }

  // ---- HyvaksymisenEhto ----

  @Test
  def hyvaksymisenEhtoPopulatedFromValintarekisteri(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valinnanTila = Some("HYVAKSYTTY"),
      ehdollistiHyvaksyttavissa = Some(true),
      ehdollistiHyvaksyttavissaEhtoFi = Some("Ehto FI"),
      ehdollistiHyvaksyttavissaEhtoSv = Some("Ehto SV"),
      ehdollistiHyvaksyttavissaEhtoEn = Some("Ehto EN")
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehdollisestiHyvaksyttavissa").value(true))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehtoFI").value("Ehto FI"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehtoSV").value("Ehto SV"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehtoEN").value("Ehto EN"))
  }

  @Test
  def hyvaksymisenEhtoNullWhenNoValintarekisteriRow(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto").value(nullValue()))
  }

  @Test
  def hyvaksymisenEhtoNullWhenAllRowsUnpublished(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valinnanTila = Some("HYVAKSYTTY"),
      julkaistavissa = Some(false),
      ehdollistiHyvaksyttavissa = Some(true),
      ehdollistiHyvaksyttavissaEhtoFi = Some("Ehto FI")
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto").value(nullValue()))
  }

  @Test
  def hyvaksymisenEhtoAllRawFieldsNullProducesEmptyEhto(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valinnanTila = Some("HYVAKSYTTY"),
      ehdollistiHyvaksyttavissa = Some(false)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehdollisestiHyvaksyttavissa").value(false))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehtoFI").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehtoSV").value(nullValue()))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehtoEN").value(nullValue()))
  }

  @Test
  def hyvaksymisenEhtoFromSamePriorityRowAsPisteet(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-winner",
      valinnanTila = Some("HYVAKSYTTY"),
      pisteet = Some(BigDecimal("90")),
      ehdollistiHyvaksyttavissa = Some(true),
      ehdollistiHyvaksyttavissaEhtoFi = Some("winner-fi")
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-loser",
      valinnanTila = Some("VARALLA"),
      pisteet = Some(BigDecimal("99")),
      ehdollistiHyvaksyttavissa = Some(false),
      ehdollistiHyvaksyttavissaEhtoFi = Some("loser-fi")
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehtoFI").value("winner-fi"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").value(90))
  }

  // ---- Asiointikieli ----

  @ParameterizedTest
  @CsvSource(
    Array(
      "1, 1",
      "2, 2",
      "3, 3",
      "0, 9"
    )
  )
  def asiointikieliMapsKnownCodes(input: Int, expected: String): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "reset for parameterized case")
    initSchema()
    insertHakemus(asiointikieli = Some(input))
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].asiointikieli").value(expected))
  }

  @Test
  def asiointikieliEmptyWhenNull(): Unit = {
    seedMinimalHakija() // insertHakemus default asiointikieli = None

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].asiointikieli").value(""))
  }

  // ---- Syntymäaika ----

  @Test
  def syntymaaikaPopulatedFromHenkilo(): Unit = {
    initSchema()
    insertHenkilo(syntymaaika = Some(SYNTYMAAIKA))
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].syntymaaika").value(SYNTYMAAIKA_STR))
  }

  @Test
  def syntymaaikaNullWhenColumnNull(): Unit = {
    seedMinimalHakija() // insertHenkilo default syntymaaika = None

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].syntymaaika").value(nullValue()))
  }

  // ---- Kansalaisuus / kansalaisuudet / turvakielto (gen_henkilo-sourced) ----

  @Test
  def kansalaisuusPopulatedFromHenkilo(): Unit = {
    seedMinimalHakija() // insertHenkilo default kansalaisuus = Some("246")

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].kansalaisuus").value(SUOMI_KOODI))
  }

  @Test
  def kansalaisuusNullWhenColumnNull(): Unit = {
    initSchema()
    insertHenkilo(kansalaisuus = None)
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].kansalaisuus").value(nullValue()))
  }

  @Test
  def kansalaisuudetSourcedFromHenkilo(): Unit = {
    initSchema()
    insertHenkilo(kaikkiKansalaisuudet = Some("""["246", "643"]"""))
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet", hasSize[Any](2)))
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet[0]").value("246"))
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet[1]").value("643"))
  }

  @Test
  def kansalaisuudetIgnoresHakemusColumn(): Unit = {
    initSchema()
    insertHenkilo(kaikkiKansalaisuudet = Some("""["246"]"""))
    insertHakemus(insertHenkilo = false, kansalaisuusJson = Some("""["999"]"""))
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet[0]").value("246"))
  }

  @Test
  def turvakieltoTrueWhenColumnTrue(): Unit = {
    initSchema()
    insertHenkilo(turvakielto = Some(true))
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].turvakielto").value(true))
  }

  @Test
  def turvakieltoDefaultsFalseWhenNull(): Unit = {
    initSchema()
    insertHenkilo(turvakielto = None)
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].turvakielto").value(false))
  }

  // ---- Ylioppilas ----

  @Test
  def onYlioppilasTrueWhenRowSaysTrue(): Unit = {
    seedMinimalHakija()
    insertYlioppilas(onYlioppilas = true)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].onYlioppilas").value(true))
  }

  @Test
  def onYlioppilasFalseWhenNoRowExists(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].onYlioppilas").value(false))
  }

  @Test
  def onYlioppilasFalseWhenRowSaysFalse(): Unit = {
    seedMinimalHakija()
    insertYlioppilas(onYlioppilas = false)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].onYlioppilas").value(false))
  }

  @Test
  def yoSuoritusVuosiPopulatedFromRow(): Unit = {
    seedMinimalHakija()
    insertYlioppilas(onYlioppilas = true, valmistumisVuosi = Some(2024))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].yoSuoritusVuosi").value("2024"))
  }

  @Test
  def yoSuoritusVuosiNullWhenNoRow(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].yoSuoritusVuosi").value(nullValue()))
  }

  @Test
  def yoSuoritusVuosiNullWhenValmistumisVuosiIsNull(): Unit = {
    seedMinimalHakija()
    insertYlioppilas(onYlioppilas = true, valmistumisVuosi = None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].yoSuoritusVuosi").value(nullValue()))
  }

  @Test
  def ylioppilasResolvedThroughHenkiloAlias(): Unit = {
    val aliasHenkiloOid = "1.2.246.562.24.99999"
    seedMinimalHakija()
    insertHenkiloAlias(oppijanumero = OPPIJANUMERO, henkiloOid = aliasHenkiloOid)
    // Attach the ylioppilas row to the ALIAS, not the primary henkilo. Proves the
    // query resolves through gen_henkilo.oppijanumero — a naive henkilo_oid join fails here.
    insertYlioppilas(henkiloOid = aliasHenkiloOid, onYlioppilas = true, valmistumisVuosi = Some(2024))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].onYlioppilas").value(true))
      .andExpect(jsonPath("$.hakijat[0].yoSuoritusVuosi").value("2024"))
  }

  // ---- Ensikertalainen ----

  @Test
  def ensikertalainenTrueWhenSupaTietoTrueRowExists(): Unit = {
    seedMinimalHakija()
    insertSupaTieto(avain = "ensikertalainen", arvo = Some("true"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").value(true))
  }

  @Test
  def ensikertalainenTrueWhenSupaTietoQuotedTrueRowExists(): Unit = {
    seedMinimalHakija()
    insertSupaTieto(avain = "ensikertalainen", arvo = Some("\"true\""))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").value(true))
  }

  @Test
  def ensikertalainenNullWhenNoRowExists(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").value(nullValue()))
  }

  @Test
  def ensikertalainenNullWhenArvoIsFalse(): Unit = {
    seedMinimalHakija()
    insertSupaTieto(avain = "ensikertalainen", arvo = Some("false"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").value(nullValue()))
  }

  @Test
  def ensikertalainenIgnoresIrrelevantAvain(): Unit = {
    seedMinimalHakija()
    insertSupaTieto(avain = "some_other_key", arvo = Some("true"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").value(nullValue()))
  }

  // ---- Excel ----

  @Test
  def excelReturns200(): Unit = {
    initSchema()

    getExcel()
      .andExpect(status.isOk)
      .andExpect(header.string("Content-Disposition", containsString("kkhakijat-")))
  }

  @Test
  def excelHasHeadersInExpectedOrder(): Unit = {
    seedMinimalHakija()

    val bytes    = getExcel().andExpect(status.isOk).andReturn().getResponse.getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val sheet           = workbook.getSheetAt(0)
      val header          = sheet.getRow(0)
      val expectedHeaders = Seq(
        "Hetu",
        "Syntymäaika",
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
        "Puhelin",
        "Sahkoposti",
        "Lukuvuosimaksu",
        "Kotikunta",
        "Sukupuoli",
        "Aidinkieli",
        "Asiointikieli",
        "Koulusivistyskielet",
        "Koulutusmarkkinointilupa",
        "On ylioppilas",
        "Suoritusvuosi",
        "On ensikertalainen",
        "Haku",
        "Hakuvuosi",
        "Hakukausi",
        "Hakemusnumero",
        "Hakemuksen jättämisen aikaleima",
        "Hakemuksen viimeinen muokkausaikaleima",
        "Organisaatio",
        "Hakukohde",
        "Hakukohteen kk-id",
        "Hakutoiveen prioriteetti",
        "Avoin vayla",
        "Valinnan tila",
        "Valinnan aikaleima",
        "Pisteet"
      )
      expectedHeaders.zipWithIndex.foreach { case (name, idx) =>
        assert(
          header.getCell(idx).getStringCellValue == name,
          s"header cell $idx expected [$name] but was [${header.getCell(idx).getStringCellValue}]"
        )
      }
    } finally workbook.close()
  }

  @Test
  def excelHasFullColumnCoverageForPopulatedHakija(): Unit = {
    initSchema()
    insertHenkilo(syntymaaika = Some(SYNTYMAAIKA))                // → cell 1 = SYNTYMAAIKA_STR
    insertHakemus(insertHenkilo = false, asiointikieli = Some(1)) // → cell 18 = "1"
    insertHakukohde()
    insertHakutoive()
    insertValintarekisteri(
      maksunTila = Some("MAKSETTU"),
      valinnanTila = Some(VALINTATIETO),
      hyvaksyttyjajulkaistu = Some(TS_HYVAKSYTTY),
      pisteet = Some(BigDecimal("82.5")),
      ehdollistiHyvaksyttavissa = Some(true),
      ehdollistiHyvaksyttavissaEhtoFi = Some("Ehto FI"),
      ehdollistiHyvaksyttavissaEhtoSv = Some("Ehto SV"),
      ehdollistiHyvaksyttavissaEhtoEn = Some("Ehto EN")
    ) // → cell 14 = "MAKSETTU", cell 35 = VALINTATIETO, cell 36 = TS_HYVAKSYTTY, cell 37 = "82.5", cell 38 = HyvaksymisenEhto(...)
    insertYlioppilas(onYlioppilas = true, valmistumisVuosi = Some(2024)) // → cell 21 = "X", cell 22 = "2024"
    insertSupaTieto(avain = "ensikertalainen", arvo = Some("true"))      // → cell 23 = "X"
    insertToteutus()
    insertKoulutus(ulkoinenTunniste = Some("TKID-1")) // → cell 32 = "TKID-1"

    val result   = getExcel().andExpect(status.isOk).andReturn()
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(result.getResponse.getContentAsByteArray))
    try {
      val sheet         = workbook.getSheetAt(0)
      val dataRow       = sheet.getRow(1)
      val expectedCells = Map(
        0  -> HETU,
        1  -> SYNTYMAAIKA_STR,
        2  -> OPPIJANUMERO,
        3  -> SUKUNIMI,
        4  -> ETUNIMET,
        5  -> KUTSUMANIMI,
        6  -> LAHIOSOITE,
        7  -> POSTINUMERO,
        8  -> HELSINKI,
        9  -> SUOMI_KOODI,
        10 -> "246",
        11 -> MATKAPUHELIN,
        13 -> EMAIL,
        14 -> "MAKSETTU",
        15 -> KOTIKUNTA,
        16 -> SUKUPUOLI.toString,
        17 -> AIDINKIELI,
        18 -> "1",
        20 -> "X",
        21 -> "X",
        22 -> "2024",
        23 -> "X",
        24 -> HAKU_OID,
        25 -> VUOSI.toString,
        26 -> KAUSI,
        27 -> HAKEMUS_OID,
        28 -> JATETTY_STR,
        29 -> MUOKATTU_STR,
        30 -> ORGANISAATIO_OID,
        31 -> HAKUKOHDE_OID,
        32 -> "TKID-1",
        33 -> "1",
        35 -> VALINTATIETO,
        36 -> "2025-10-01T12:00:00+03:00",
        37 -> "82.5",
        38 -> "HyvaksymisenEhto(X,,Ehto FI,Ehto SV,Ehto EN)",
        41 -> VASTAANOTTOTIETO,
        42 -> ILMOITTAUTUMISEN_TILA,
        44 -> "X"
      )
      expectedCells.foreach { case (idx, value) =>
        assert(
          dataRow.getCell(idx).getStringCellValue == value,
          s"cell $idx expected [$value] but was [${dataRow.getCell(idx).getStringCellValue}]"
        )
      }
      // deferred fields: cells 12, 19, 34, 39, 40, 45-59 stay ""
      Seq(12, 19, 34, 39, 40, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59)
        .foreach { idx =>
          assert(
            dataRow.getCell(idx).getStringCellValue == "",
            s"deferred cell $idx expected empty but was [${dataRow.getCell(idx).getStringCellValue}]"
          )
        }
    } finally workbook.close()
  }

  @Test
  def excelCell38RendersEmptyEhtoTupleWhenNoValintarekisteriRow(): Unit = {
    seedMinimalHakija() // no valintarekisteri seeded → hyvaksymisenEhto is None

    val bytes    = getExcel().andExpect(status.isOk).andReturn().getResponse.getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val cell38 = workbook.getSheetAt(0).getRow(1).getCell(38).getStringCellValue
      assert(cell38 == "HyvaksymisenEhto(,,,,)", s"cell 38 expected [HyvaksymisenEhto(,,,,)] but was [$cell38]")
    } finally workbook.close()
  }

  @Test
  def excelEmitsOneRowPerHakemusEntry(): Unit = {
    seedMinimalHakija()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2)

    val bytes = getExcel(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andReturn()
      .getResponse
      .getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val sheet = workbook.getSheetAt(0)
      assert(sheet.getLastRowNum == 2, s"expected 1 header + 2 data rows, got ${sheet.getLastRowNum}")
      assert(sheet.getRow(1).getCell(31).getStringCellValue == HAKUKOHDE_OID)
      assert(sheet.getRow(2).getCell(31).getStringCellValue == HAKUKOHDE_OID_2)
    } finally workbook.close()
  }

  // ---- Audit ----

  @Test
  def jsonEndpointEmitsAuditLogEntryOnSuccess(): Unit = {
    seedMinimalHakija()

    get(organisaatioOid = None).andExpect(status.isOk)

    val calls = recordedAuditCalls
    assert(calls.size == 1, s"expected exactly one audit entry, got $calls")
    val call = calls.head
    assert(call.operation == AuditOperation.ExternalKKHakijat)
    assert(call.params("format") == "json")
    assert(call.params("hakuOid") == HAKU_OID)
    assert(call.params("hakukohdeOid") == HAKUKOHDE_OID)
    assert(call.params("organisaatioOid") == "")
    assert(call.principalName.contains("testuser"))
  }

  @Test
  def excelEndpointEmitsAuditLogEntryOnSuccess(): Unit = {
    seedMinimalHakija()

    getExcel(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID)).andExpect(status.isOk)

    val calls = recordedAuditCalls
    assert(calls.size == 1, s"expected exactly one audit entry, got $calls")
    val call = calls.head
    assert(call.operation == AuditOperation.ExternalKKHakijat)
    assert(call.params("format") == "excel")
    assert(call.params("organisaatioOid") == ORGANISAATIO_OID)
  }

  @Test
  @WithMockUser(username = "testuser", roles = Array("USER"))
  def forbiddenRequestEmitsNoAuditEntry(): Unit = {
    get().andExpect(status.isForbidden)
    assert(recordedAuditCalls.isEmpty, s"expected no audit entry, got $recordedAuditCalls")
  }
}

object ExternalKKHakijatControllerTest {

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
