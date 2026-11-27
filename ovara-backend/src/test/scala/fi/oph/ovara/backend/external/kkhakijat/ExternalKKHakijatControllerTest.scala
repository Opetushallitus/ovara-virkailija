package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.external.{HakukohderyhmaStub, OrganisaatioHierarkiaStub}
import fi.oph.ovara.backend.external.kkhakijat.ExternalKKHakijatTestData.*
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.service.CommonService
import fi.oph.ovara.backend.utils.{AuditLog, AuditOperation}
import fi.vm.sade.auditlog.Operation
import jakarta.servlet.http.HttpServletRequest
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.{BeforeEach, Test}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify}
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.{CsvSource, ValueSource}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.{SpringBootTest, TestConfiguration}
import org.springframework.context.annotation.{Bean, Import, Primary}
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.{WithAnonymousUser, WithMockUser}
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.`override`.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, header, jsonPath, status}
import org.springframework.test.web.servlet.{MockMvc, ResultActions}
import slick.jdbc.PostgresProfile.api.*

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
class ExternalKKHakijatControllerTest
    extends ExternalKKHakijatTestUtils
    with OrganisaatioHierarkiaStub
    with HakukohderyhmaStub {

  @Autowired
  private val mvc: MockMvc = null

  @Autowired
  override val db: ReadOnlyDatabase = null

  // Organisaatiohierarkia luetaan pub-skeemasta, jota näiden testien testikanta ei sisällä
  // (initSchema luo vain gen-taulut). Ks. OrganisaatioHierarkiaStub.
  @MockitoBean
  override val commonService: CommonService = null

  @BeforeEach
  def clearDb(): Unit =
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "Drop everything")

  @BeforeEach
  def stubOrganisaatiohierarkia(): Unit =
    stubOrganisaatioHierarkiaAsIdentity()

  @BeforeEach
  def clearAuditRecord(): Unit =
    ExternalKKHakijatControllerTest.recordedAuditCalls.clear()

  private def recordedAuditCalls: List[ExternalKKHakijatControllerTest.AuditCall] =
    ExternalKKHakijatControllerTest.recordedAuditCalls.asScala.toList

  private def get(
    hakuOid: Option[String] = Some(HAKU_OID),
    hakukohdeOid: Option[String] = Some(HAKUKOHDE_OID),
    organisaatioOid: Option[String] = None,
    valintarajaus: Option[String] = Some("HAKENEET"),
    hakukohderyhmaOid: Option[String] = None,
    oppijanumero: Option[String] = None
  ): ResultActions = {
    var req = MockMvcRequestBuilders
      .get("/api/external/kkhakijat")
      .accept(MediaType.APPLICATION_JSON)
    hakuOid.foreach(v => req = req.param("hakuOid", v))
    hakukohdeOid.foreach(v => req = req.param("hakukohdeOid", v))
    hakukohderyhmaOid.foreach(v => req = req.param("hakukohderyhmaOid", v))
    organisaatioOid.foreach(v => req = req.param("organisaatioOid", v))
    valintarajaus.foreach(v => req = req.param("valintarajaus", v))
    oppijanumero.foreach(v => req = req.param("oppijanumero", v))
    mvc.perform(req)
  }

  /** Oppijanumerohaku: pelkkä oppijanumero, ei hakua eikä muita rajaimia. */
  private def getByOppijanumero(oppijanumero: String): ResultActions =
    get(hakuOid = None, hakukohdeOid = None, valintarajaus = None, oppijanumero = Some(oppijanumero))

  private def getExcel(
    hakuOid: Option[String] = Some(HAKU_OID),
    hakukohdeOid: Option[String] = Some(HAKUKOHDE_OID),
    organisaatioOid: Option[String] = None,
    valintarajaus: Option[String] = Some("HAKENEET"),
    headers: Map[String, String] = Map.empty,
    hakukohderyhmaOid: Option[String] = None,
    oppijanumero: Option[String] = None
  ): ResultActions = {
    var req = MockMvcRequestBuilders
      .get("/api/external/kkhakijat/excel")
    hakuOid.foreach(v => req = req.param("hakuOid", v))
    hakukohdeOid.foreach(v => req = req.param("hakukohdeOid", v))
    hakukohderyhmaOid.foreach(v => req = req.param("hakukohderyhmaOid", v))
    organisaatioOid.foreach(v => req = req.param("organisaatioOid", v))
    valintarajaus.foreach(v => req = req.param("valintarajaus", v))
    oppijanumero.foreach(v => req = req.param("oppijanumero", v))
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
    username = "toinen-asteen-hakeneet-user",
    roles = Array("APP_OVARA-VIRKAILIJA_HAKENEET_1.2.246.562.10.00000000001")
  )
  def returns403ForToisenAsteenHakeneetRole(): Unit = {
    // The 2Aste HAKENEET_ right must NOT grant access to the KK endpoint — only KK_HAKENEET_ does.
    get().andExpect(status.isForbidden)
  }

  @Test
  def returns200ForPaakayttajaRole(): Unit = {
    initSchema()

    get()
      .andExpect(status.isOk)
      .andExpect(content.json("""{"hakijat": []}"""))
  }

  @Test
  // Tavallinen organisaatio, ei OPH-organisaatio: muuten tämä testaisi pääkäyttäjän päästämistä
  // sisään (ks. kkHakeneetRoleOnOphOrganisaatioGrantsPaakayttajaScope) eikä roolin omaa pääsyä.
  @WithMockUser(
    username = "kk-hakeneet-user",
    roles = Array("APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.10.00000000000000000586")
  )
  def returns200ForKkHakeneetRole(): Unit = {
    initSchema()

    get()
      .andExpect(status.isOk)
      .andExpect(content.json("""{"hakijat": []}"""))
  }

  @Test
  @WithMockUser(
    username = "kk-hakeneet-org-a-user",
    roles = Array("APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.10.00000000000000000586")
  )
  def kkHakeneetUserSeesOnlyHakijatFromOwnOrg(): Unit = {
    // orgA = ORGANISAATIO_OID = "1.2.246.562.10.00000000000000000586" — user's KK_HAKENEET-suffixed org.
    // orgB is a different jarjestyspaikka — the user must NOT see hakijat from there.
    val orgB          = ORGANISAATIO_OID_2
    val hakukohdeOidB = HAKUKOHDE_OID_2
    val oppijanumeroB = "1.2.246.562.24.00000000020"
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertHakemus(oppijanumero = oppijanumeroB, hakemusOid = HAKEMUS_OID_2, insertHaku = false)
    insertHakukohde(hakukohdeOid = hakukohdeOidB, jarjestyspaikkaOid = orgB, organisaatioOid = Some(orgB))
    insertHakutoive(hakemusOid = HAKEMUS_OID_2, hakukohdeOid = hakukohdeOidB)

    // Query orgA only — the scope must hide orgB's hakija.
    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
  }

  @Test
  @WithMockUser(
    username = "kk-mixed-authorities-user",
    roles = Array(
      "APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.10.00000000000000000586",
      "APP_OVARA-VIRKAILIJA_2ASTE_1.2.246.562.10.00000000000000000587"
    )
  )
  def kkHakeneetUserIgnoresOidsFromOtherOvaraAuthorities(): Unit = {
    // User has KK_HAKENEET_<orgA> and 2ASTE_<orgB>. Only orgA counts as scope for this endpoint —
    // the OID granted through the 2ASTE role must NOT widen access to orgB.
    val orgB          = ORGANISAATIO_OID_2
    val hakukohdeOidB = HAKUKOHDE_OID_2
    val oppijanumeroB = "1.2.246.562.24.00000000020"
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
    username = "kk-oph-oid-via-other-role-user",
    roles = Array(
      "APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.10.00000000000000000586",
      "APP_OVARA-VIRKAILIJA_2ASTE_1.2.246.562.10.00000000001"
    )
  )
  def ophOidOnAnotherOvaraRoleDoesNotGrantPaakayttajaScope(): Unit = {
    // OPH-organisaation oid on myönnetty 2ASTE-roolilla, ei KK_HAKENEET-roolilla. Se ei tee
    // käyttäjästä pääkäyttäjää tälle rajapinnalle: oikeus on yhä vain orgA:han.
    val orgB          = ORGANISAATIO_OID_2
    val hakukohdeOidB = HAKUKOHDE_OID_2
    val oppijanumeroB = "1.2.246.562.24.00000000020"
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertHakemus(oppijanumero = oppijanumeroB, hakemusOid = HAKEMUS_OID_2, insertHaku = false)
    insertHakukohde(hakukohdeOid = hakukohdeOidB, jarjestyspaikkaOid = orgB, organisaatioOid = Some(orgB))
    insertHakutoive(hakemusOid = HAKEMUS_OID_2, hakukohdeOid = hakukohdeOidB)

    // Pääkäyttäjänä orgB:n hakija näkyisi; rajatulla oikeudella ei.
    get(hakukohdeOid = None, organisaatioOid = Some(orgB))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](0)))

    // Oma organisaatio toimii silti -- käyttäjä ei ole lukittu ulos.
    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
  }

  @Test
  @WithMockUser(
    username = "kk-hakeneet-oph-user",
    roles = Array("APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.10.00000000001")
  )
  def kkHakeneetRoleOnOphOrganisaatioGrantsPaakayttajaScope(): Unit = {
    // Edellisen testin peilikuva: OPH-organisaation oid KK_HAKENEET-roolilla eli tämän rajapinnan
    // omalla oikeudella tekee käyttäjästä pääkäyttäjän. Käyttäjällä ei ole erillistä
    // OPH_PAAKAYTTAJA-oikeutta, joten tämä pinnaa nimenomaan oid-pohjaisen tunnistuksen.
    val orgB          = ORGANISAATIO_OID_2
    val hakukohdeOidB = HAKUKOHDE_OID_2
    val oppijanumeroB = "1.2.246.562.24.00000000020"
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertHakemus(oppijanumero = oppijanumeroB, hakemusOid = HAKEMUS_OID_2, insertHaku = false)
    insertHakukohde(hakukohdeOid = hakukohdeOidB, jarjestyspaikkaOid = orgB, organisaatioOid = Some(orgB))
    insertHakutoive(hakemusOid = HAKEMUS_OID_2, hakukohdeOid = hakukohdeOidB)

    // Rajatulla oikeudella orgB olisi tyhjä; pääkäyttäjänä hakija näkyy.
    get(hakukohdeOid = None, organisaatioOid = Some(orgB))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(oppijanumeroB))

    // Myös orgA näkyy, eli näkymä ei ole vaihtunut orgB:hen vaan on rajaamaton.
    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
  }

  @Test
  @WithMockUser(
    username = "kk-hakeneet-no-oid-user",
    // KK_HAKENEET_ prefix matches, but the trailing token has no digits → getKayttooikeusOids yields empty.
    roles = Array("APP_OVARA-VIRKAILIJA_KK_HAKENEET_")
  )
  def kkHakeneetUserWithNoOidYieldsEmptyResults(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](0)))
    // Ilman oideja ei ole myöskään ryhmäoikeuksia laajennettavaksi. Stubbaamaton laajennus
    // palauttaisi nullin ja päätyisi 500:aan, joten varmistetaan ettei sitä kutsuta lainkaan.
    verify(commonService, never()).getHakukohderyhmanHakukohdeOids(any[String](), any[String]())
  }

  @Test
  @WithMockUser(
    username = "paakayttaja-plus-kk-hakeneet",
    roles = Array(
      "APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA_1.2.246.562.10.00000000001",
      "APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.10.00000000000000000586"
    )
  )
  def paakayttajaAndKkHakeneetTogetherGetPaakayttajaScope(): Unit = {
    // Hakija applies at orgB, which is NOT in the KK_HAKENEET-suffixed OID set — paakayttaja wins,
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

  // ---- Hakukohderyhmäoikeudet ----
  // KK_HAKENEET voi olla myönnetty hakukohderyhmälle (oid 1.2.246.562.28.*) organisaation
  // sijaan. seedHakijatInTwoOrgs seedaa kaksi hakijaa: OPPIJANUMERO hakee ORGANISAATIO_OID:n
  // ja OPPIJANUMERO_B ORGANISAATIO_OID_2:n järjestämään hakukohteeseen.

  @Test
  @WithMockUser(
    username = "kk-hakeneet-hakukohderyhma-user",
    roles = Array("APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.28.00000000000000000012")
  )
  def hakukohderyhmaRightAloneGrantsAccessToRyhmanHakukohteet(): Unit = {
    seedHakijatInTwoOrgs()
    withHakukohderyhmat(Map(HAKUKOHDERYHMA_OID -> List(HAKUKOHDE_OID_2)))

    // Käyttäjällä ei ole organisaatio-oikeutta kumpaankaan järjestäjään -- pelkkä ryhmäoikeus
    // riittää ryhmän hakukohteeseen.
    get(hakukohdeOid = None, hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO_B))
  }

  @Test
  @WithMockUser(
    username = "kk-hakeneet-hakukohderyhma-user",
    roles = Array("APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.28.00000000000000000012")
  )
  def hakukohderyhmaRightDoesNotGrantAccessToAnotherRyhma(): Unit = {
    seedHakijatInTwoOrgs()
    withHakukohderyhmat(
      Map(
        HAKUKOHDERYHMA_OID   -> List(HAKUKOHDE_OID_2),
        HAKUKOHDERYHMA_OID_2 -> List(HAKUKOHDE_OID)
      )
    )

    get(hakukohdeOid = None, hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID_2))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](0)))
  }

  @Test
  @WithMockUser(
    username = "kk-hakeneet-hakukohderyhma-user",
    roles = Array("APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.28.00000000000000000012")
  )
  def hakukohderyhmaRightDoesNotCoverOrganisaatioRajaus(): Unit = {
    seedHakijatInTwoOrgs()

    // organisaatioOid on katettava organisaatio-oikeuksilla, joten tulos on tyhjä eikä
    // ryhmälaajennusta edes haeta.
    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID_2))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](0)))
    verify(commonService, never()).getHakukohderyhmanHakukohdeOids(any[String](), any[String]())
  }

  @Test
  @WithMockUser(
    username = "kk-hakeneet-org-and-hakukohderyhma-user",
    roles = Array(
      "APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.10.00000000000000000586",
      "APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.28.00000000000000000012"
    )
  )
  def organisaatioAndHakukohderyhmaRightsAreUnioned(): Unit = {
    seedHakijatInTwoOrgs()
    withHakukohderyhmat(Map(HAKUKOHDERYHMA_OID -> List(HAKUKOHDE_OID, HAKUKOHDE_OID_2)))

    // orgA:n hakija osuu organisaatio-oikeudella, orgB:n hakija ryhmäoikeudella.
    get(hakukohdeOid = None, hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](2)))
  }

  @Test
  @WithMockUser(
    username = "kk-hakeneet-org-and-hakukohderyhma-user",
    roles = Array(
      "APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.10.00000000000000000586",
      "APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.28.00000000000000000012"
    )
  )
  def organisaatioRajausDropsRowsThatOnlyRyhmaRightWouldAllow(): Unit = {
    seedHakijatInTwoOrgs()
    withHakukohderyhmat(Map(HAKUKOHDERYHMA_OID -> List(HAKUKOHDE_OID_2)))

    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
    // Pelkkä rivimäärä ei erottaisi tätä suppressoinnin poistamisesta, koska orgB:n rivi
    // karsiutuu jo organisaatioparametrilla. Ryhmälaajennuksen kutsumattomuus erottaa.
    verify(commonService, never()).getHakukohderyhmanHakukohdeOids(any[String](), any[String]())
  }

  @Test
  @WithMockUser(
    username = "kk-hakeneet-ryhma-from-other-role-user",
    roles = Array(
      "APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.10.00000000000000000586",
      "APP_OVARA-VIRKAILIJA_2ASTE_1.2.246.562.28.00000000000000000012"
    )
  )
  def hakukohderyhmaFromOtherOvaraRoleGrantsNothing(): Unit = {
    seedHakijatInTwoOrgs()
    withHakukohderyhmat(Map(HAKUKOHDERYHMA_OID -> List(HAKUKOHDE_OID_2)))

    // Ryhmäoikeus on myönnetty 2ASTE-roolilla, joten se ei laajenna tämän rajapinnan oikeuksia.
    get(hakukohdeOid = None, hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](0)))
  }

  @Test
  @WithMockUser(
    username = "paakayttaja-plus-kk-hakeneet-hakukohderyhma",
    roles = Array(
      "APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA_1.2.246.562.10.00000000001",
      "APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.28.00000000000000000012"
    )
  )
  def paakayttajaWithHakukohderyhmaRightGetsPaakayttajaScope(): Unit = {
    seedHakijatInTwoOrgs()

    // Pääkäyttäjä näkee ryhmän ulkopuoliset hakukohteet, eikä ryhmälaajennusta tarvita.
    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID_2))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO_B))
    verify(commonService, never()).getHakukohderyhmanHakukohdeOids(any[String](), any[String]())
  }

  @Test
  @WithMockUser(
    username = "kk-hakeneet-hakukohderyhma-user",
    roles = Array("APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.28.00000000000000000012")
  )
  def hakukohderyhmaRightEmitsAuditLogEntry(): Unit = {
    seedHakijatInTwoOrgs()
    withHakukohderyhmat(Map(HAKUKOHDERYHMA_OID -> List(HAKUKOHDE_OID_2)))

    get(hakukohdeOid = None, hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID)).andExpect(status.isOk)

    val calls = recordedAuditCalls
    assert(calls.size == 1, s"expected exactly one audit entry, got $calls")
    val call = calls.head
    assert(call.operation == AuditOperation.ExternalKKHakijat)
    assert(call.params("hakukohderyhmaOid") == HAKUKOHDERYHMA_OID)
    assert(call.principalName.contains("kk-hakeneet-hakukohderyhma-user"))
  }

  @Test
  @WithMockUser(
    username = "kk-hakeneet-hakukohderyhma-user",
    roles = Array("APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.28.00000000000000000012")
  )
  def excelHonoursHakukohderyhmaRight(): Unit = {
    seedHakijatInTwoOrgs()
    withHakukohderyhmat(Map(HAKUKOHDERYHMA_OID -> List(HAKUKOHDE_OID_2)))

    val bytes = getExcel(hakukohdeOid = None, hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID))
      .andExpect(status.isOk)
      .andReturn()
      .getResponse
      .getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val sheet      = workbook.getSheetAt(0)
      val cellValues = (1 to sheet.getLastRowNum)
        .flatMap(i => Option(sheet.getRow(i)))
        .flatMap(row => (0 until row.getLastCellNum).map(row.getCell(_)))
        .flatMap(cell => Option(cell).map(_.toString))
        .toSet
      assert(cellValues.contains(OPPIJANUMERO_B), s"expected the ryhmä's hakija in the excel, got $cellValues")
      assert(!cellValues.contains(OPPIJANUMERO), "hakija outside the ryhmä must not be in the excel")
    } finally {
      workbook.close()
    }
  }

  @Test
  @WithMockUser(
    username = "kk-hakeneet-hakukohde-oid-user",
    // Oid ei ole organisaatio eikä hakukohderyhmä (tässä hakukohteen oid), joten se ei ole
    // oikeus mihinkään: pyyntö läpäisee roolitarkistuksen mutta tulos on tyhjä.
    roles = Array("APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.20.00000000000000000112")
  )
  def unrecognisedOidTypeGrantsNothing(): Unit = {
    seedHakijatInTwoOrgs()

    get(hakukohdeOid = Some(HAKUKOHDE_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](0)))
    verify(commonService, never()).getHakukohderyhmanHakukohdeOids(any[String](), any[String]())
  }

  private val OPPIJANUMERO_B = "1.2.246.562.24.00000000020"

  /**
   * Kaksi hakijaa eri organisaatioiden järjestämiin hakukohteisiin: OPPIJANUMERO hakee
   * ORGANISAATIO_OID:n HAKUKOHDE_OID:iin ja OPPIJANUMERO_B ORGANISAATIO_OID_2:n
   * HAKUKOHDE_OID_2:een.
   */
  private def seedHakijatInTwoOrgs(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertHakemus(oppijanumero = OPPIJANUMERO_B, hakemusOid = HAKEMUS_OID_2, insertHaku = false)
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID_2,
      jarjestyspaikkaOid = ORGANISAATIO_OID_2,
      organisaatioOid = Some(ORGANISAATIO_OID_2)
    )
    insertHakutoive(hakemusOid = HAKEMUS_OID_2, hakukohdeOid = HAKUKOHDE_OID_2)
  }

  // ---- Oppijanumerohaku ----

  private val TOINEN_HAKU_OID          = "1.2.246.562.29.00000000000000000201"
  private val ALIAS_HENKILO_OID        = "1.2.246.562.24.00000000099"
  private val TOINEN_OPPIJANUMERO      = "1.2.246.562.24.00000000097"
  private val TOINEN_ALIAS_HENKILO_OID = "1.2.246.562.24.00000000096"
  private val ALIAS_HENKILO_OID_2      = "1.2.246.562.24.00000000095"
  private val HAKEMUS_OID_3            = "1.2.246.562.11.00000000000004511894"
  private val HAKEMUS_OID_4            = "1.2.246.562.11.00000000000004511895"
  private val HAKEMUS_OID_5            = "1.2.246.562.11.00000000000004511896"

  /**
   * Sama henkilö kahdessa haussa. Toinen haku on oleellinen: se on ainoa tapa osoittaa, ettei
   * kysely enää rajaa hakuun -- yhdellä haulla testi menisi läpi myös vanhalla pakollisella
   * hakuOidilla.
   */
  private def seedHakijaKahdessaHaussa(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertHakemus(hakemusOid = HAKEMUS_OID_2, hakuOid = TOINEN_HAKU_OID, insertHenkilo = false)
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID_2,
      jarjestyspaikkaOid = ORGANISAATIO_OID_2,
      organisaatioOid = Some(ORGANISAATIO_OID_2)
    )
    insertHakutoive(hakemusOid = HAKEMUS_OID_2, hakukohdeOid = HAKUKOHDE_OID_2)
    // Toinen, eri henkilö samassa haussa: ilman tätä oppijanumerorajauksen poistaminen ei näkyisi
    // näissä testeissä lainkaan, koska kannassa olisi vain yksi henkilö.
    insertHakemus(oppijanumero = TOINEN_OPPIJANUMERO, hakemusOid = HAKEMUS_OID_3, insertHaku = false)
    insertHakutoive(hakemusOid = HAKEMUS_OID_3)
  }

  /**
   * Master-rivi (henkilo_oid == oppijanumero) hakemuksineen, ja alias-rivi jolla on oma
   * henkilo_oid ja oma hakemus. Kummallakin oidilla haettuna pitää palautua molemmat.
   *
   * Mukana on myös toinen, täysin linkittämätön henkilö omine aliaksineen. Ilman sitä kanta
   * sisältäisi vain yhden henkilön, jolloin alias-testien hasSize-väitteet kertoisivat vain että
   * rivejä tulee tarpeeksi -- liian laaja alikysely menisi läpi huomaamatta.
   */
  private def seedHakijaAliaksella(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertHenkiloAlias(OPPIJANUMERO, ALIAS_HENKILO_OID)
    insertHakemus(
      oppijanumero = ALIAS_HENKILO_OID,
      hakemusOid = HAKEMUS_OID_2,
      insertHenkilo = false,
      insertHaku = false
    )
    insertHakutoive(hakemusOid = HAKEMUS_OID_2)
    seedToinenHenkiloAliaksella()
  }

  /**
   * Saman henkilön toinen alias, jonka hakemus on eri haussa ja eri hakukohteeseen kuin masterin
   * ja ensimmäisen aliaksen hakemukset. Erottaa alias-ratkaisun hakurajauksesta: kun hakuOidia ei
   * anneta, aliaksia yhdistää oppijanumero eikä yhteinen haku.
   */
  private def insertToinenAliasToisessaHaussa(): Unit = {
    insertHenkiloAlias(OPPIJANUMERO, ALIAS_HENKILO_OID_2)
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID_2,
      jarjestyspaikkaOid = ORGANISAATIO_OID_2,
      organisaatioOid = Some(ORGANISAATIO_OID_2)
    )
    // insertHaku = true (oletus) luo TOINEN_HAKU_OID:n gen_haku-rivin.
    insertHakemus(
      oppijanumero = ALIAS_HENKILO_OID_2,
      hakemusOid = HAKEMUS_OID_5,
      hakuOid = TOINEN_HAKU_OID,
      insertHenkilo = false
    )
    insertHakutoive(hakemusOid = HAKEMUS_OID_5, hakukohdeOid = HAKUKOHDE_OID_2)
  }

  /** Toinen henkilö aliaksineen: ei saa koskaan palautua ensimmäisen oideilla haettaessa. */
  private def seedToinenHenkiloAliaksella(): Unit = {
    insertHakemus(oppijanumero = TOINEN_OPPIJANUMERO, hakemusOid = HAKEMUS_OID_3, insertHaku = false)
    insertHenkiloAlias(TOINEN_OPPIJANUMERO, TOINEN_ALIAS_HENKILO_OID)
    insertHakemus(
      oppijanumero = TOINEN_ALIAS_HENKILO_OID,
      hakemusOid = HAKEMUS_OID_4,
      insertHenkilo = false,
      insertHaku = false
    )
    insertHakutoive(hakemusOid = HAKEMUS_OID_3)
    insertHakutoive(hakemusOid = HAKEMUS_OID_4)
  }

  @Test
  def oppijanumeroReturnsHakijaFromEveryHaku(): Unit = {
    seedHakijaKahdessaHaussa()

    // Yksi hakija-alkio per hakemus, joten kaksi hakua -> kaksi alkiota samalla oppijanumerolla.
    getByOppijanumero(OPPIJANUMERO)
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](2)))
      .andExpect(jsonPath("$.hakijat[*].oppijanumero", everyItem(equalTo[Any](OPPIJANUMERO))))
      .andExpect(
        jsonPath("$.hakijat[*].hakemukset[*].hakukohde", containsInAnyOrder[Any](HAKUKOHDE_OID, HAKUKOHDE_OID_2))
      )
  }

  @Test
  def oppijanumeroWithHakuOidLimitsToThatHaku(): Unit = {
    seedHakijaKahdessaHaussa()

    get(hakuOid = Some(TOINEN_HAKU_OID), hakukohdeOid = None, valintarajaus = None, oppijanumero = Some(OPPIJANUMERO))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohde").value(HAKUKOHDE_OID_2))
  }

  @Test
  def oppijanumeroWithOrganisaatioOidLimitsToThatJarjestyspaikka(): Unit = {
    seedHakijaKahdessaHaussa()

    get(
      hakuOid = None,
      hakukohdeOid = None,
      organisaatioOid = Some(ORGANISAATIO_OID_2),
      valintarajaus = None,
      oppijanumero = Some(OPPIJANUMERO)
    )
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohde").value(HAKUKOHDE_OID_2))
  }

  @Test
  def unknownOppijanumeroReturnsEmptyList(): Unit = {
    seedHakijaKahdessaHaussa()

    getByOppijanumero("1.2.246.562.24.00000000098")
      .andExpect(status.isOk)
      .andExpect(content.json("""{"hakijat": []}"""))
  }

  @Test
  def oppijanumeroFindsHakemuksetOfEveryAlias(): Unit = {
    seedHakijaAliaksella()
    insertToinenAliasToisessaHaussa()

    // Kolme hakemusta: masterin, ensimmäisen aliaksen (sama haku) ja toisen aliaksen (eri haku).
    // Kaikki raportoivat masterin oppijanumeron. Hakemusnumerot ja haut varmistetaan erikseen,
    // jottei pelkkä lukumäärä voi täsmätä väärillä riveillä.
    getByOppijanumero(OPPIJANUMERO)
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](3)))
      .andExpect(jsonPath("$.hakijat[*].oppijanumero", everyItem(equalTo[Any](OPPIJANUMERO))))
      .andExpect(
        jsonPath(
          "$.hakijat[*].hakemukset[*].hakemusnumero",
          containsInAnyOrder[Any](HAKEMUS_OID, HAKEMUS_OID_2, HAKEMUS_OID_5)
        )
      )
      .andExpect(
        jsonPath(
          "$.hakijat[*].hakemukset[*].haku",
          containsInAnyOrder[Any](HAKU_OID, HAKU_OID, TOINEN_HAKU_OID)
        )
      )
      .andExpect(
        jsonPath(
          "$.hakijat[*].hakemukset[*].hakukohde",
          containsInAnyOrder[Any](HAKUKOHDE_OID, HAKUKOHDE_OID, HAKUKOHDE_OID_2)
        )
      )
  }

  // Tämä testi erottaa alias-ratkaisun pelkästä hlo.oppijanumero = <oid> -yhtäsuuruudesta.
  @Test
  def aliasHenkiloOidFindsSamePersonAsOppijanumero(): Unit = {
    seedHakijaAliaksella()

    getByOppijanumero(ALIAS_HENKILO_OID)
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](2)))
      .andExpect(jsonPath("$.hakijat[*].oppijanumero", everyItem(equalTo[Any](OPPIJANUMERO))))
  }

  // Alias jolla ei ole omia hakemuksia: masterin hakemukset pitää silti löytyä sen oidilla.
  @Test
  def aliasWithoutOwnHakemuksetStillFindsMastersHakemukset(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertHenkiloAlias(OPPIJANUMERO, ALIAS_HENKILO_OID)
    seedToinenHenkiloAliaksella()

    getByOppijanumero(ALIAS_HENKILO_OID)
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
      .andExpect(jsonPath("$.hakijat[0].hakemukset", hasSize[Any](1)))
  }

  /**
   * Alias-ratkaisu ratkaisee annetun oidin masteriksi ja rajaa sillä. Tämä varmistaa ettei
   * tulosjoukko laajene liikaa: toinen henkilö omine aliaksineen ei saa vuotaa mukaan.
   */
  @Test
  def aliasSearchDoesNotLeakOtherHenkilot(): Unit = {
    seedHakijaAliaksella()

    getByOppijanumero(ALIAS_HENKILO_OID)
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](2)))
      .andExpect(jsonPath("$.hakijat[*].oppijanumero", everyItem(not(equalTo[Any](TOINEN_OPPIJANUMERO)))))
  }

  /**
   * Oppijanumerohaku vaatii oikeuden kaikkiin rajapinnan tietoihin. Molemmat puolet varmistetaan,
   * jottei tarkistus ole liian laaja: sama käyttäjä pääsee yhä normaaliin hakuun.
   */
  @Test
  @WithMockUser(
    username = "kk-hakeneet-org-a-user",
    roles = Array("APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.10.00000000000000000586")
  )
  def limitedUserGets403ForOppijanumeroSearch(): Unit = {
    seedMinimalHakija()

    getByOppijanumero(OPPIJANUMERO)
      .andExpect(status.isForbidden)

    get(organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
  }

  @Test
  @WithMockUser(
    username = "kk-hakeneet-org-a-user",
    roles = Array("APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.10.00000000000000000586")
  )
  def limitedUserGets403ForOppijanumeroExcel(): Unit = {
    seedMinimalHakija()

    getExcel(hakuOid = None, hakukohdeOid = None, valintarajaus = None, oppijanumero = Some(OPPIJANUMERO))
      .andExpect(status.isForbidden)
  }

  /**
   * OILI on erillinen oikeus kaikkiin tämän rajapinnan tietoihin: sillä pääsee
   * oppijanumerohakuun ilman rekisterinpitäjyyttä, ja se riittää yksinään myös rajapinnalle
   * (käyttäjällä ei ole lainkaan KK_HAKENEET-oikeuksia).
   */
  @Test
  @WithMockUser(username = "oili-user", roles = Array("APP_OVARA-VIRKAILIJA_OILI"))
  def oiliUserMaySearchByOppijanumero(): Unit = {
    seedHakijaKahdessaHaussa()

    getByOppijanumero(OPPIJANUMERO)
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](2)))
      .andExpect(jsonPath("$.hakijat[*].oppijanumero", everyItem(equalTo[Any](OPPIJANUMERO))))
  }

  // Oikeus voidaan myöntää myös organisaatiolle, jolloin rooli tulee oid-suffiksilla. Sekin
  // tarkoittaa samaa täyttä oikeutta, eikä myöntävä organisaatio rajaa tulosta.
  @Test
  @WithMockUser(
    username = "oili-org-user",
    roles = Array("APP_OVARA-VIRKAILIJA_OILI_1.2.246.562.10.00000000000000000587")
  )
  def oiliGrantedForOrganisaatioMaySearchByOppijanumero(): Unit = {
    seedMinimalHakija()

    getByOppijanumero(OPPIJANUMERO)
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
  }

  // Täysi oikeus koskee koko rajapintaa, ei vain oppijanumerohakua: tavallinen haku palauttaa
  // hakijat myös organisaatiosta, johon käyttäjällä ei ole omaa oikeutta. Vrt.
  // kkHakeneetUserIgnoresOidsFromOtherOvaraAuthorities, jossa sama kysely palauttaa tyhjän.
  @Test
  @WithMockUser(
    username = "oili-org-a-user",
    roles = Array("APP_OVARA-VIRKAILIJA_OILI_1.2.246.562.10.00000000000000000586")
  )
  def oiliUserSeesHakijatOutsideOwnOrganisaatiot(): Unit = {
    seedHakijatInTwoOrgs()

    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID_2))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO_B))
  }

  @Test
  @WithMockUser(username = "oili-user", roles = Array("APP_OVARA-VIRKAILIJA_OILI"))
  def oiliUserMayDownloadOppijanumeroExcel(): Unit = {
    seedMinimalHakija()

    getExcel(hakuOid = None, hakukohdeOid = None, valintarajaus = None, oppijanumero = Some(OPPIJANUMERO))
      .andExpect(status.isOk)
      .andExpect(content.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
  }

  @Test
  def oppijanumeroSearchIsAudited(): Unit = {
    seedMinimalHakija()

    getByOppijanumero(OPPIJANUMERO).andExpect(status.isOk)

    val call = recordedAuditCalls.head
    assert(call.params("oppijanumero") == OPPIJANUMERO, s"oppijanumero missing from audit params: ${call.params}")
  }

  @Test
  def oppijanumeroReturns400WhenNotAHenkiloOid(): Unit = {
    initSchema()

    // Organisaation oid on kelvollinen oph-oid mutta väärä nimiavaruus.
    getByOppijanumero(ORGANISAATIO_OID)
      .andExpect(status.isBadRequest)
      .andExpect(jsonPath("$.details", hasItem[Any]("oppijanumero.invalid.oid")))
  }

  @Test
  def returns400WhenNeitherHakuOidNorOppijanumeroProvided(): Unit = {
    get(hakuOid = None)
      .andExpect(status.isBadRequest)
      .andExpect(jsonPath("$.details", hasItem[Any]("hakuOid.required")))
  }

  // Ryhmä laajennetaan hakukohteiksi haun sisällä, joten ilman hakua se ei ole ratkaistavissa.
  @Test
  def returns400WhenHakukohderyhmaGivenWithoutHakuOid(): Unit = {
    get(
      hakuOid = None,
      hakukohdeOid = None,
      hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID),
      oppijanumero = Some(OPPIJANUMERO)
    )
      .andExpect(status.isBadRequest)
      .andExpect(jsonPath("$.details", hasItem[Any]("hakuOid.required.with.hakukohderyhmaOid")))
  }

  @Test
  def excelSupportsOppijanumero(): Unit = {
    seedMinimalHakija()

    getExcel(hakuOid = None, hakukohdeOid = None, valintarajaus = None, oppijanumero = Some(OPPIJANUMERO))
      .andExpect(status.isOk)
      .andExpect(content.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
  }

  // ---- Validation ----

  @ParameterizedTest
  @ValueSource(strings = Array("not-oid", "1.2", "1.2.246", "1.2.246.1"))
  def returns400WhenHakuOidInvalid(bad: String): Unit = {
    get(hakuOid = Some(bad))
      .andExpect(status.isBadRequest)
      .andExpect(jsonPath("$.details", hasItem[Any]("hakuOid.invalid.oid")))
  }

  @Test
  def returns400WhenNoRajaavaParametriProvided(): Unit = {
    get(hakukohdeOid = None, organisaatioOid = None)
      .andExpect(status.isBadRequest)
      .andExpect(
        jsonPath("$.details", hasItem[Any]("hakukohdeOid_or_organisaatioOid_or_hakukohderyhmaOid.required"))
      )
  }

  @Test
  def hakukohderyhmaAloneSatisfiesTheRequiredRajaus(): Unit = {
    // Ryhmälaajennus tulee pub-skeemasta, jota testien testikanta ei sisällä -- stubataan tyhjäksi.
    // Riittää että validointi läpäisee eikä 400:aa tule.
    stubHakukohderyhma(List.empty)
    seedMinimalHakija()

    get(hakukohdeOid = None, organisaatioOid = None, hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](0)))
  }

  // Viimeinen arvo on hakukohteen oid: hakukohderyhmällä on oma patterninsa, joten
  // väärän tyypin oid ei mene läpi ryhmärajaimena.
  @ParameterizedTest
  @ValueSource(
    strings = Array("not-oid", "1.2", "1.2.246", "1.2.246.1", "1.2.246.562.20.00000000000000000112")
  )
  def returns400WhenHakukohderyhmaOidInvalid(bad: String): Unit = {
    get(hakukohderyhmaOid = Some(bad))
      .andExpect(status.isBadRequest)
      .andExpect(jsonPath("$.details", hasItem[Any]("hakukohderyhmaOid.invalid.oid")))
  }

  // Vastaavasti hakukohderyhmän oid ei kelpaa hakukohderajaimena.
  @ParameterizedTest
  @ValueSource(
    strings = Array("not-oid", "1.2", "1.2.246", "1.2.246.1", "1.2.246.562.28.00000000000000000012")
  )
  def returns400WhenHakukohdeOidInvalid(bad: String): Unit = {
    get(hakukohdeOid = Some(bad))
      .andExpect(status.isBadRequest)
      .andExpect(jsonPath("$.details", hasItem[Any]("hakukohdeOid.invalid.oid")))
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
    insertToteutus(koulutuksenAlkamisvuosi = Some(2026), koulutuksenAlkamiskausiuri = Some("kausi_s#1"))
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
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].ilmoittautumiset", hasSize[Any](2)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].ilmoittautumiset[0].kausi").value("2026S"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].ilmoittautumiset[0].tila").value(1))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].ilmoittautumiset[1].kausi").value("2027K"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].ilmoittautumiset[1].tila").value(1))
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
      .andExpect(jsonPath("$.hakijat[0].syntymaaika").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].puhelin").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].onYlioppilas").value(false))
      .andExpect(jsonPath("$.hakijat[0].yoSuoritusVuosi").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].turvakielto").value(false))
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohdeKkId").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].avoinVayla").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("KESKEN"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanAikaleima").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehdollisestiHyvaksyttavissa").value(false))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].lukuvuosimaksu").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pohjakoulutus").isEmpty)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuusLahde").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset").isEmpty)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].liitteet").doesNotExist())
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

  /**
   * Kuten seedMinimalHakija, mutta hakutoiveella ei ole vastaanottoa. Valinnan tila riippuu VTS:n
   *  tapaan vastaanotosta (vtsTila), joten tallennetun tilan testit tehdään ilman sitä.
   */
  private def seedHakijaIlmanVastaanottoa(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(vastaanottotieto = None)
  }

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
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valinnanTila = Some(state))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value(state))
  }

  @Test
  def valinnanTilaDefaultsToKeskenForUnknownValue(): Unit = {
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valinnanTila = Some("SOMETHING_ELSE"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("KESKEN"))
  }

  @Test
  def valinnanTilaDefaultsToKeskenWhenNoVrRow(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("KESKEN"))
  }

  // ---- Vastaanottotieto ja hakutoivePrioriteetti ----

  @Test
  def vastaanottotietoDefaultsToKeskenWhenMissing(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(vastaanottotieto = None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
  }

  @Test
  def hakutoivePrioriteettiOmittedForUnorderedHakutoiveet(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(hakutoivenumero = -1)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakutoivePrioriteetti").doesNotExist())
  }

  // ---- Lukuvuosimaksu ----

  @Test
  def lukuvuosimaksuMaksamattaWhenObligatedAndNoMaksu(): Unit = {
    seedMinimalHakija()
    insertKasittelymerkinta(PAYMENT_REQUIREMENT, Some("obligated"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].lukuvuosimaksu").value("MAKSAMATTA"))
  }

  @Test
  def lukuvuosimaksuFromValintarekisteriWhenObligated(): Unit = {
    seedMinimalHakija()
    insertKasittelymerkinta(PAYMENT_REQUIREMENT, Some("obligated"))
    insertValintarekisteri(maksunTila = Some("MAKSETTU"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].lukuvuosimaksu").value("MAKSETTU"))
  }

  @Test
  def lukuvuosimaksuOmittedWhenNotObligatedAndNoMaksu(): Unit = {
    seedMinimalHakija()
    insertKasittelymerkinta(PAYMENT_REQUIREMENT, Some("not-obligated"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].lukuvuosimaksu").doesNotExist())
  }

  @ParameterizedTest
  @ValueSource(strings = Array("MAKSETTU", "MAKSAMATTA", "VAPAUTETTU"))
  def lukuvuosimaksuPopulatedFromValintarekisteri(tila: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
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
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].lukuvuosimaksu").doesNotExist())
  }

  @Test
  def lukuvuosimaksuNullWhenMaksunTilaIsNull(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(maksunTila = None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].lukuvuosimaksu").doesNotExist())
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

  // ---- Käsittelymerkinnät (hKelpoisuus, hKelpoisuusMaksuvelvollisuus) ----

  @ParameterizedTest
  @CsvSource(
    Array(
      "eligible,                       ELIGIBLE",
      "uneligible,                     INELIGIBLE",
      "unreviewed,                     NOT_CHECKED",
      "conditionally-eligible,         CONDITIONALLY_ELIGIBLE",
      "automatically-checked-eligible, AUTOMATICALLY_CHECKED_ELIGIBLE"
    )
  )
  def hKelpoisuusStateMapsToApiValue(state: String, expected: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedMinimalHakija()
    insertKasittelymerkinta(ELIGIBILITY_REQUIREMENT, Some(state))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuus").value(expected))
  }

  @ParameterizedTest
  @CsvSource(
    Array(
      "obligated,     REQUIRED",
      "not-obligated, NOT_REQUIRED",
      "unreviewed,    NOT_CHECKED"
    )
  )
  def hKelpoisuusMaksuvelvollisuusStateMapsToApiValue(state: String, expected: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedMinimalHakija()
    insertKasittelymerkinta(PAYMENT_REQUIREMENT, Some(state))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuusMaksuvelvollisuus").value(expected))
  }

  @Test
  def bothKasittelymerkinnatPopulateIndependently(): Unit = {
    seedMinimalHakija()
    insertKasittelymerkinta(ELIGIBILITY_REQUIREMENT, Some("eligible"))
    insertKasittelymerkinta(PAYMENT_REQUIREMENT, Some("not-obligated"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuus").value("ELIGIBLE"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuusMaksuvelvollisuus").value("NOT_REQUIRED"))
  }

  // Vain toinen requirement kirjattuna: varmistaa ettei requirement-rajaus ole pudonnut pois
  // eikä kenttiä ole ristiinkytketty -- ilman tätä väärä rajaus läpäisisi muut testit.
  // Kirjaamaton kenttä on NOT_CHECKED, ei tyhjä: tyhjä on varattu tuntemattomalle tilalle.
  @Test
  def eligibilityMerkintaDoesNotFillMaksuvelvollisuus(): Unit = {
    seedMinimalHakija()
    insertKasittelymerkinta(ELIGIBILITY_REQUIREMENT, Some("eligible"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuus").value("ELIGIBLE"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuusMaksuvelvollisuus").value(NOT_CHECKED))
  }

  @Test
  def paymentMerkintaDoesNotFillKelpoisuus(): Unit = {
    seedMinimalHakija()
    insertKasittelymerkinta(PAYMENT_REQUIREMENT, Some("obligated"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuus").value(NOT_CHECKED))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuusMaksuvelvollisuus").value("REQUIRED"))
  }

  @Test
  def kasittelymerkinnatNotCheckedWhenNoRows(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuus").value(NOT_CHECKED))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuusMaksuvelvollisuus").value(NOT_CHECKED))
  }

  @Test
  def kasittelymerkinnatNotCheckedWhenStateIsNull(): Unit = {
    seedMinimalHakija()
    insertKasittelymerkinta(ELIGIBILITY_REQUIREMENT, None)
    insertKasittelymerkinta(PAYMENT_REQUIREMENT, None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuus").value(NOT_CHECKED))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuusMaksuvelvollisuus").value(NOT_CHECKED))
  }

  // Tuntematon tila pudotetaan tyhjäksi eikä NOT_CHECKEDiksi: tulkitsematonta lähdekoodia ei
  // päästetä rajapintaan, mutta se pitää myös erottua kirjaamattomasta merkinnästä.
  @Test
  def unknownEligibilityStateFallsBackToEmpty(): Unit = {
    seedMinimalHakija()
    insertKasittelymerkinta(ELIGIBILITY_REQUIREMENT, Some("some-new-state"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuus").value(""))
  }

  @Test
  def unknownMaksuvelvollisuusStateFallsBackToNull(): Unit = {
    seedMinimalHakija()
    insertKasittelymerkinta(PAYMENT_REQUIREMENT, Some("some-new-state"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuusMaksuvelvollisuus").doesNotExist())
  }

  @Test
  def kasittelymerkinnatPerHakemusHakukohdeDiffers(): Unit = {
    seedMinimalHakija()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2)
    insertKasittelymerkinta(ELIGIBILITY_REQUIREMENT, Some("eligible"), hakukohdeOid = HAKUKOHDE_OID)
    insertKasittelymerkinta(ELIGIBILITY_REQUIREMENT, Some("uneligible"), hakukohdeOid = HAKUKOHDE_OID_2)

    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuus").value("ELIGIBLE"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[1].hKelpoisuus").value("INELIGIBLE"))
  }

  // Toisen hakukohteen merkintä ei saa vuotaa tähän hakutoiveeseen.
  @Test
  def kasittelymerkintaFromOtherHakukohdeDoesNotLeak(): Unit = {
    seedMinimalHakija()
    insertKasittelymerkinta(ELIGIBILITY_REQUIREMENT, Some("eligible"), hakukohdeOid = HAKUKOHDE_OID_2)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hKelpoisuus").value(NOT_CHECKED))
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
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohdeKkId").doesNotExist())
  }

  @Test
  def hakukohdeKkIdNullWhenToteutusRowMissing(): Unit = {
    seedMinimalHakija() // no toteutus row; hakukohde.toteutus_oid dangles

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohdeKkId").doesNotExist())
  }

  @Test
  def hakukohdeKkIdNullWhenUlkoinenTunnisteIsNull(): Unit = {
    seedMinimalHakija()
    insertToteutus()
    insertKoulutus(ulkoinenTunniste = None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohdeKkId").doesNotExist())
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
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanAikaleima").value("2025-10-01T12:00:00"))
  }

  @Test
  def valinnanAikaleimaConvertedToHelsinkiTime(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valinnanTila = Some("HYVAKSYTTY"),
      hyvaksyttyjajulkaistu = Some(java.time.OffsetDateTime.parse("2025-10-01T09:00:00Z"))
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanAikaleima").value("2025-10-01T12:00:00"))
  }

  @Test
  def valinnanAikaleimaNullWhenTimestampIsNull(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"), hyvaksyttyjajulkaistu = None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanAikaleima").doesNotExist())
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
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedHakijaIlmanVastaanottoa()
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
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanAikaleima").value("2025-10-01T12:00:00"))
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

  // jsonPath vertaa lukuja tyyppimuunnoksen jälkeen (400.0 == 400), joten muoto tarkistetaan vastauksen tekstistä.
  @Test
  def wholeNumberPisteetHaveDecimalPartAsInSure(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"), pisteet = Some(BigDecimal("400")))

    val body = get().andExpect(status.isOk).andReturn().getResponse.getContentAsString
    assert(""""pisteet"\s*:\s*400\.0\b""".r.findFirstIn(body).isDefined, s"pisteet expected as 400.0 in [$body]")

    val bytes    = getExcel().andExpect(status.isOk).andReturn().getResponse.getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val cell37 = workbook.getSheetAt(0).getRow(1).getCell(37).getStringCellValue
      assert(cell37 == "400.0", s"cell 37 expected [400.0] but was [$cell37]")
    } finally workbook.close()
  }

  @Test
  def pisteetNullWhenNoValintarekisteriRow(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").doesNotExist())
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
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").doesNotExist())
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
  def pisteetPrefersHigherPriorityStateOverLower(winner: String, loser: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
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
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").value(80))
  }

  @Test
  def pisteetSecondaryTiebreakByPrioriteetti(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-winner",
      valinnanTila = Some("HYVAKSYTTY"),
      pisteet = Some(BigDecimal("88"))
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-loser",
      valinnanTila = Some("HYVAKSYTTY"),
      pisteet = Some(BigDecimal("99"))
    )
    insertValintarekisteriValintatapajono(valintatapajonoId = "vtj-winner", prioriteetti = Some(1))
    insertValintarekisteriValintatapajono(valintatapajonoId = "vtj-loser", prioriteetti = Some(2))

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
  def hyvaksymisenEhtoFalseWhenNoValintarekisteriRow(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehdollisestiHyvaksyttavissa").value(false))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehtoFI").doesNotExist())
  }

  @Test
  def hyvaksymisenEhtoFalseWhenAllRowsUnpublished(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valinnanTila = Some("HYVAKSYTTY"),
      julkaistavissa = Some(false),
      ehdollistiHyvaksyttavissa = Some(true),
      ehdollistiHyvaksyttavissaEhtoFi = Some("Ehto FI")
    )

    // VTS:n tapaan julkaisemattomalle ei ehdollista hyväksyntää, mutta ehtoteksti näytetään.
    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehdollisestiHyvaksyttavissa").value(false))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehtoFI").value("Ehto FI"))
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
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehtoFI").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehtoSV").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehtoEN").doesNotExist())
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

  // ---- Valinnan tulos valinta-tulos-servicen tapaan (vtsTila) ----

  @Test
  def unpublishedHakutoiveShownAsKesken(): Unit = {
    seedMinimalHakija() // vastaanotto VASTAANOTTANUT_SITOVASTI
    insertValintarekisteri(
      valinnanTila = Some("HYVAKSYTTY"),
      julkaistavissa = Some(false),
      pisteet = Some(BigDecimal("75")),
      ehdollistiHyvaksyttavissa = Some(true),
      ehdollistiHyvaksyttavissaEhtoFi = Some("Ehto FI")
    )
    insertValintaperusteValintatapajono(
      valintatapajonoTyyppi = Some("valintatapajono_kp"),
      valintatapajonoNimi = Some("Koepistejono")
    )
    insertValintatapajonoKoodi("valintatapajono_kp", Some("Koepisteet"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("KESKEN"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehdollisestiHyvaksyttavissa").value(false))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hyvaksymisenEhto.ehtoFI").value("Ehto FI"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononTyyppi").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononNimi").value("Koepistejono"))
  }

  @Test
  def varallaInUnpublishedJonoShownAsKeskenWhenOtherJonoPublished(): Unit = {
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valintatapajonoId = "vtj-1", valinnanTila = Some("VARALLA"), julkaistavissa = Some(false))
    insertValintarekisteri(valintatapajonoId = "vtj-2", valinnanTila = Some("HYLATTY"), julkaistavissa = Some(true))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("KESKEN"))
  }

  @Test
  def unpublishedRowWithoutTilaDoesNotHideOtherJonot(): Unit = {
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valintatapajonoId = "vtj-1", valinnanTila = Some("VARALLA"), julkaistavissa = Some(true))
    insertValintarekisteri(valintatapajonoId = "vtj-2", valinnanTila = None, julkaistavissa = Some(false))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("VARALLA"))
  }

  @Test
  def notHyvaksyttyShownWhenAllJonotPublished(): Unit = {
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valintatapajonoId = "vtj-1", valinnanTila = Some("HYLATTY"), julkaistavissa = Some(true))
    insertValintarekisteri(valintatapajonoId = "vtj-2", valinnanTila = Some("HYLATTY"), julkaistavissa = Some(true))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("HYLATTY"))
  }

  @Test
  def hyvaksyttyInPublishedMerkitsevaJonoShownWhenOtherJonoUnpublished(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-1",
      valinnanTila = Some("HYVAKSYTTY"),
      julkaistavissa = Some(true),
      pisteet = Some(BigDecimal("75"))
    )
    insertValintarekisteri(valintatapajonoId = "vtj-2", valinnanTila = Some("VARALLA"), julkaistavissa = Some(false))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("HYVAKSYTTY"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value(VASTAANOTTOTIETO))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").value(75))
  }

  @ParameterizedTest
  @CsvSource(
    Array(
      "HYVAKSYTTY,             true,  HARKINNANVARAISESTI_HYVAKSYTTY",
      "VARASIJALTA_HYVAKSYTTY, true,  HARKINNANVARAISESTI_HYVAKSYTTY",
      "VARASIJALTA_HYVAKSYTTY, false, VARASIJALTA_HYVAKSYTTY",
      "VARALLA,                true,  VARALLA"
    )
  )
  def harkinnanvarainenHyvaksyntaShownAsHarkinnanvaraisestiHyvaksytty(
    tallennettu: String,
    harkinnanvarainen: Boolean,
    odotettu: String
  ): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valinnanTila = Some(tallennettu), hyvaksyttyHarkinnanvaraisesti = Some(harkinnanvarainen))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value(odotettu))
  }

  @ParameterizedTest
  @CsvSource(Array("true, KESKEN", "false, VARALLA"))
  def varallaWithoutVarasijatayttoShownAsKesken(eiVarasijatayttoa: Boolean, odotettu: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valinnanTila = Some("VARALLA"))
    insertValintarekisteriValintatapajono(eiVarasijatayttoa = Some(eiVarasijatayttoa))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value(odotettu))
  }

  @ParameterizedTest
  @CsvSource(
    Array(
      "HYVAKSYTTY, PERUNUT,                  PERUNUT",
      "HYVAKSYTTY, PERUUTETTU,               PERUUTETTU",
      "VARALLA,    VASTAANOTTANUT_SITOVASTI, HYVAKSYTTY",
      "VARALLA,    EHDOLLISESTI_VASTAANOTTANUT, HYVAKSYTTY",
      "HYVAKSYTTY, VASTAANOTTANUT_SITOVASTI, HYVAKSYTTY"
    )
  )
  def vastaanottoChangesValinnanTila(tallennettu: String, vastaanotto: String, odotettu: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(vastaanottotieto = Some(vastaanotto))
    insertValintarekisteri(valinnanTila = Some(tallennettu))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value(odotettu))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value(vastaanotto))
  }

  @Test
  def vastaanottoKeptWhenNoValintarekisteriRow(): Unit = {
    seedMinimalHakija() // vastaanotto VASTAANOTTANUT_SITOVASTI, ei valintarekisterin riviä

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("KESKEN"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value(VASTAANOTTOTIETO))
  }

  // ---- Vastaanoton määräaika ja varasijasäännöt (VTS) ----

  private def paivienPaasta(paivia: Int): java.time.OffsetDateTime = java.time.OffsetDateTime.now().plusDays(paivia)

  @ParameterizedTest
  @CsvSource(
    Array(
      "HYVAKSYTTY, -1, EI_VASTAANOTETTU_MAARA_AIKANA",
      "HYVAKSYTTY,  1, KESKEN",
      "PERUNUT,    -1, EI_VASTAANOTETTU_MAARA_AIKANA",
      "VARALLA,    -1, KESKEN"
    )
  )
  def vastaanottoEiMaaraaikanaWhenDeadlinePassed(tila: String, paivia: Int, odotettu: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valinnanTila = Some(tila))
    insertOhjausparametri("PH_OPVP", aikaleima = Some(paivienPaasta(paivia)))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value(odotettu))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value(tila))
  }

  @Test
  def vastaanottoKeskenWithoutDeadlineParameter(): Unit = {
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
  }

  @Test
  def vastaanottoKeskenWhenHakijakohtainenDeadlineNotPassed(): Unit = {
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"), hyvaksyttyjajulkaistu = Some(paivienPaasta(-2)))
    insertOhjausparametri("PH_OPVP", aikaleima = Some(paivienPaasta(-1)))
    insertOhjausparametri("PH_HPVOA", arvo = Some("14"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
  }

  @Test
  def vastaanottoKeptWhenDeadlinePassed(): Unit = {
    seedMinimalHakija() // vastaanotto VASTAANOTTANUT_SITOVASTI
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"))
    insertOhjausparametri("PH_OPVP", aikaleima = Some(paivienPaasta(-1)))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value(VASTAANOTTOTIETO))
  }

  @Test
  def unpublishedKeskenEvenWhenDeadlinePassed(): Unit = {
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"), julkaistavissa = Some(false))
    insertOhjausparametri("PH_OPVP", aikaleima = Some(paivienPaasta(-1)))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("KESKEN"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
  }

  @Test
  def vastaanottoKeskenWithoutValintarekisteriRowEvenWhenDeadlinePassed(): Unit = {
    seedHakijaIlmanVastaanottoa()
    insertOhjausparametri("PH_OPVP", aikaleima = Some(paivienPaasta(-1)))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
  }

  /** varasijasaannotVoimaan/kaikkiJonotSijoittelussa: päiviä nykyhetkestä, tyhjä = parametria ei ole. */
  @ParameterizedTest
  @CsvSource(
    Array(
      " 1,   , HYVAKSYTTY",
      "-1, -1, VARASIJALTA_HYVAKSYTTY",
      "   ,  , VARASIJALTA_HYVAKSYTTY",
      "   , 1, HYVAKSYTTY"
    )
  )
  def varasijaltaHyvaksyttyShownAsHyvaksyttyUntilVarasijasaannotVoimassa(
    varasijasaannotVoimaan: Integer,
    kaikkiJonotSijoittelussa: Integer,
    odotettu: String
  ): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valinnanTila = Some("VARASIJALTA_HYVAKSYTTY"))
    Option(varasijasaannotVoimaan).foreach(p => insertOhjausparametri("PH_VSSAV", aikaleima = Some(paivienPaasta(p))))
    Option(kaikkiJonotSijoittelussa).foreach(p => insertOhjausparametri("PH_VTSSV", aikaleima = Some(paivienPaasta(p))))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value(odotettu))
  }

  // ---- Hakutoiveiden väliset säännöt (VTS) ----

  /**
   * Hakemus kahteen hakukohteeseen: ylempi hakutoive annetulla tilalla ja vastaanotolla, alempi (HAKUKOHDE_OID_2)
   *  annetulla tilalla. Molemmat tulokset on julkaistu.
   */
  private def seedKaksiHakutoivetta(
    ylempiTila: String,
    ylempiVastaanotto: Option[String],
    alempiTila: String
  ): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2)
    insertHakutoive(hakutoivenumero = 1, vastaanottotieto = ylempiVastaanotto)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2, vastaanottotieto = None)
    insertValintarekisteri(valintatapajonoId = "vtj-1", valinnanTila = Some(ylempiTila))
    insertValintarekisteri(hakukohdeOid = HAKUKOHDE_OID_2, valintatapajonoId = "vtj-2", valinnanTila = Some(alempiTila))
  }

  /** sijoittelu/jarjestetytHakutoiveet: onko ohjausparametri asetettu todeksi; säännöt vain kun molemmat. */
  @ParameterizedTest
  @CsvSource(Array("true, true, PERUUNTUNUT", "true, false, KESKEN", "false, false, KESKEN"))
  def keskenBelowHyvaksyttyShownAsPeruuntunutWhenQueriedByItsHakukohde(
    sijoittelu: Boolean,
    jarjestetytHakutoiveet: Boolean,
    odotettu: String
  ): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedKaksiHakutoivetta(ylempiTila = "HYVAKSYTTY", ylempiVastaanotto = None, alempiTila = "KESKEN")
    if (sijoittelu) insertOhjausparametri("sijoittelu", alkuperainenArvo = Some("true"))
    if (jarjestetytHakutoiveet) insertOhjausparametri("jarjestetytHakutoiveet", alkuperainenArvo = Some("true"))

    // Haku vain alemman hakutoiveen hakukohteella: sääntö näkee silti ylemmän hakutoiveen.
    get(hakukohdeOid = Some(HAKUKOHDE_OID_2))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohde").value(HAKUKOHDE_OID_2))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value(odotettu))
  }

  @Test
  def varallaShownAsPeruuntunutWhenOtherToiveVastaanotettuSitovasti(): Unit = {
    seedKaksiHakutoivetta(
      ylempiTila = "HYVAKSYTTY",
      ylempiVastaanotto = Some("VASTAANOTTANUT_SITOVASTI"),
      alempiTila = "VARALLA"
    )
    insertSijoitteluJaPriorisointi()

    get(hakukohdeOid = Some(HAKUKOHDE_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("HYVAKSYTTY"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("VASTAANOTTANUT_SITOVASTI"))
    get(hakukohdeOid = Some(HAKUKOHDE_OID_2))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("PERUUNTUNUT"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
  }

  @Test
  def ohjausparametriFalseKeepsRulesOff(): Unit = {
    seedKaksiHakutoivetta(
      ylempiTila = "HYVAKSYTTY",
      ylempiVastaanotto = Some("VASTAANOTTANUT_SITOVASTI"),
      alempiTila = "VARALLA"
    )
    insertOhjausparametri("sijoittelu", alkuperainenArvo = Some("false"))
    insertOhjausparametri("jarjestetytHakutoiveet", alkuperainenArvo = Some("true"))

    get(hakukohdeOid = Some(HAKUKOHDE_OID_2))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("VARALLA"))
  }

  @Test
  def vastaanottoKeptBelowUnpublishedToiveWhenNoValintarekisteriRow(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2)
    insertHakutoive(hakutoivenumero = 1, vastaanottotieto = None)
    insertHakutoive(
      hakukohdeOid = HAKUKOHDE_OID_2,
      hakutoivenumero = 2,
      vastaanottotieto = Some("VASTAANOTTANUT_SITOVASTI")
    )
    // Ylemmän hakutoiveen tulos on julkaisematon; alemmalla ei ole valintarekisterin riviä.
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"), julkaistavissa = Some(false))
    insertSijoitteluJaPriorisointi()

    get(hakukohdeOid = Some(HAKUKOHDE_OID_2))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("KESKEN"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("VASTAANOTTANUT_SITOVASTI"))
  }

  // ---- Yhden paikan säännös ----

  private val MUU_HAKU_OID      = "1.2.246.562.29.00000000000000000201"
  private val MUU_HAKEMUS_OID   = "1.2.246.562.11.00000000000004511894"
  private val MUU_HAKUKOHDE_OID = "1.2.246.562.20.00000000000000000114"
  private val ALIAS_OID         = "1.2.246.562.24.00000000020"
  private val MUU_TOTEUTUS_OID  = "1.2.246.562.17.00000000000000000223"
  private val MUU_KOULUTUS_OID  = "1.2.246.562.13.00000000000000000123"

  /**
   * Kuten seedKaksiHakutoivetta, mutta hakukohteet ovat yhden paikan säännön piirissä: tutkintoon johtava
   *  yliopistokoulutus, joka alkaa syksyllä 2026 (insertYpsToteutus). Haussa ei ole sijoittelua.
   */
  private def seedKaksiYpsHakutoivetta(
    ylempiTila: String,
    ylempiVastaanotto: Option[String],
    alempiTila: String
  ): Unit = {
    seedKaksiHakutoivetta(ylempiTila, ylempiVastaanotto, alempiTila)
    insertYpsToteutus()
    insertKoulutus(johtaaTutkintoon = Some(true), koulutustyyppi = Some("yo"))
  }

  // Hakemuksen hakukohteiden toteutus, joka alkaa syksyllä 2026 kuten insertVastaanottoToisessaHaussan hakukohde.
  private def insertYpsToteutus(): Unit =
    insertToteutus(
      koulutuksenAlkamisvuosi = Some(KOULUTUKSEN_ALKAMISVUOSI),
      koulutuksenAlkamiskausiuri = Some(KOULUTUKSEN_ALKAMISKAUSIURI)
    )

  /**
   * Hakijan hakemus toisessa haussa ja vastaanotto sen hakukohteeseen. Hakukohteella on oletuksena sama
   *  toteutus ja koulutus kuin hakemuksen hakukohteilla.
   */
  private def insertVastaanottoToisessaHaussa(
    henkiloOid: String = OPPIJANUMERO,
    vastaanotto: String = "VASTAANOTTANUT_SITOVASTI",
    koulutuksenAlkamiskausiuri: String = KOULUTUKSEN_ALKAMISKAUSIURI,
    koulutuksenAlkamisvuosi: Int = KOULUTUKSEN_ALKAMISVUOSI,
    toteutusOid: String = TOTEUTUS_OID,
    kohdejoukonTarkenneKoodiuri: Option[String] = None
  ): Unit = {
    insertHakemus(
      oppijanumero = henkiloOid,
      hakemusOid = MUU_HAKEMUS_OID,
      hakuOid = MUU_HAKU_OID,
      insertHenkilo = false,
      insertHaku = false
    )
    insertHaku(hakuOid = MUU_HAKU_OID, kohdejoukonTarkenneKoodiuri = kohdejoukonTarkenneKoodiuri)
    insertHakukohde(
      hakukohdeOid = MUU_HAKUKOHDE_OID,
      toteutusOid = toteutusOid,
      koulutuksenAlkamisvuosi = Some(koulutuksenAlkamisvuosi),
      koulutuksenAlkamiskausiuri = Some(koulutuksenAlkamiskausiuri)
    )
    insertHakutoive(
      hakemusOid = MUU_HAKEMUS_OID,
      hakukohdeOid = MUU_HAKUKOHDE_OID,
      vastaanottotieto = Some(vastaanotto)
    )
  }

  @Test
  def otherToiveOttanutVastaanToisenPaikanWhenVastaanotettuSitovastiWithoutSijoittelu(): Unit = {
    seedKaksiYpsHakutoivetta(
      ylempiTila = "HYVAKSYTTY",
      ylempiVastaanotto = Some("VASTAANOTTANUT_SITOVASTI"),
      alempiTila = "HYVAKSYTTY"
    )

    get(hakukohdeOid = Some(HAKUKOHDE_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("HYVAKSYTTY"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("VASTAANOTTANUT_SITOVASTI"))
    // Haku vain alemman hakutoiveen hakukohteella: sääntö näkee silti ylemmän hakutoiveen vastaanoton.
    get(hakukohdeOid = Some(HAKUKOHDE_OID_2))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("PERUUNTUNUT"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("OTTANUT_VASTAAN_TOISEN_PAIKAN"))
  }

  @Test
  def toiveetOttanutVastaanToisenPaikanWhenVastaanottoInAnotherHaku(): Unit = {
    seedKaksiYpsHakutoivetta(ylempiTila = "HYVAKSYTTY", ylempiVastaanotto = None, alempiTila = "VARALLA")
    insertVastaanottoToisessaHaussa()

    get(hakukohdeOid = Some(HAKUKOHDE_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("PERUUNTUNUT"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("OTTANUT_VASTAAN_TOISEN_PAIKAN"))
    get(hakukohdeOid = Some(HAKUKOHDE_OID_2))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("PERUUNTUNUT"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("OTTANUT_VASTAAN_TOISEN_PAIKAN"))
  }

  @Test
  def ottanutVastaanToisenPaikanWhenVastaanottoUnderLinkedHenkiloOid(): Unit = {
    seedKaksiYpsHakutoivetta(ylempiTila = "HYVAKSYTTY", ylempiVastaanotto = None, alempiTila = "VARALLA")
    insertHenkiloAlias(oppijanumero = OPPIJANUMERO, henkiloOid = ALIAS_OID)
    insertVastaanottoToisessaHaussa(henkiloOid = ALIAS_OID, vastaanotto = "EHDOLLISESTI_VASTAANOTTANUT")

    get(hakukohdeOid = Some(HAKUKOHDE_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("OTTANUT_VASTAAN_TOISEN_PAIKAN"))
  }

  @ParameterizedTest
  @CsvSource(Array("kausi_k#1, 2026", "kausi_s#1, 2027"))
  def vastaanottoInAnotherHakuOnOtherKausiKeepsTilat(kausiuri: String, vuosi: Int): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedKaksiYpsHakutoivetta(ylempiTila = "HYVAKSYTTY", ylempiVastaanotto = None, alempiTila = "VARALLA")
    insertVastaanottoToisessaHaussa(koulutuksenAlkamiskausiuri = kausiuri, koulutuksenAlkamisvuosi = vuosi)

    get(hakukohdeOid = Some(HAKUKOHDE_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("HYVAKSYTTY"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
  }

  @Test
  def ehdollinenVastaanottoOnHakemusKeepsOtherToiveet(): Unit = {
    seedKaksiYpsHakutoivetta(
      ylempiTila = "HYVAKSYTTY",
      ylempiVastaanotto = Some("EHDOLLISESTI_VASTAANOTTANUT"),
      alempiTila = "VARALLA"
    )
    insertVastaanottoToisessaHaussa()

    get(hakukohdeOid = Some(HAKUKOHDE_OID_2))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("VARALLA"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
  }

  @Test
  def hakukohdeOutsideYhdenPaikanSaantoKeepsTilat(): Unit = {
    seedKaksiHakutoivetta(ylempiTila = "HYVAKSYTTY", ylempiVastaanotto = None, alempiTila = "VARALLA")
    insertYpsToteutus()
    insertKoulutus(johtaaTutkintoon = Some(true), koulutustyyppi = Some("amm"))
    insertVastaanottoToisessaHaussa()

    get(hakukohdeOid = Some(HAKUKOHDE_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("HYVAKSYTTY"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
  }

  @Test
  def vastaanottoOnKoulutusOutsideYhdenPaikanSaantoKeepsTilat(): Unit = {
    seedKaksiYpsHakutoivetta(ylempiTila = "HYVAKSYTTY", ylempiVastaanotto = None, alempiTila = "VARALLA")
    // Hakemuksen hakukohteet ovat säännön piirissä, toisen haun hakukohteen ammatillinen koulutus ei.
    insertToteutus(toteutusOid = MUU_TOTEUTUS_OID, koulutusOid = MUU_KOULUTUS_OID)
    insertKoulutus(koulutusOid = MUU_KOULUTUS_OID, johtaaTutkintoon = Some(true), koulutustyyppi = Some("amm"))
    insertVastaanottoToisessaHaussa(toteutusOid = MUU_TOTEUTUS_OID)

    get(hakukohdeOid = Some(HAKUKOHDE_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("HYVAKSYTTY"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
  }

  @Test
  def vastaanottoInHakuOutsideYhdenPaikanSaantoKeepsTilat(): Unit = {
    seedKaksiYpsHakutoivetta(ylempiTila = "HYVAKSYTTY", ylempiVastaanotto = None, alempiTila = "VARALLA")
    // Sama koulutus, mutta toisen haun kohdejoukon tarkenne rajaa sen hakukohteet säännön ulkopuolelle.
    insertVastaanottoToisessaHaussa(kohdejoukonTarkenneKoodiuri = Some("haunkohdejoukontarkenne_1#1"))

    get(hakukohdeOid = Some(HAKUKOHDE_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("HYVAKSYTTY"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
  }

  @Test
  def yhdenPaikanSaantoUsesHaunAlkamiskausiWhenHakukohdeHasNone(): Unit = {
    initSchema()
    insertHakemus(insertHaku = false)
    insertHaku(koulutuksenAlkamisvuosi = Some(KOULUTUKSEN_ALKAMISVUOSI), koulutuksenAlkamiskausiuri = Some("kausi_s#1"))
    insertHakukohde(koulutuksenAlkamisvuosi = None, koulutuksenAlkamiskausiuri = None)
    insertHakutoive(vastaanottotieto = None)
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"))
    insertToteutus()
    insertKoulutus(johtaaTutkintoon = Some(true), koulutustyyppi = Some("amk"))
    insertVastaanottoToisessaHaussa()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("OTTANUT_VASTAAN_TOISEN_PAIKAN"))
  }

  @ParameterizedTest
  @ValueSource(strings = Array("PERUNUT", "PERUUTETTU", "EI_VASTAANOTETTU_MAARA_AIKANA"))
  def otherVastaanottotilaInAnotherHakuKeepsTilat(vastaanotto: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedKaksiYpsHakutoivetta(ylempiTila = "HYVAKSYTTY", ylempiVastaanotto = None, alempiTila = "VARALLA")
    insertVastaanottoToisessaHaussa(vastaanotto = vastaanotto)

    get(hakukohdeOid = Some(HAKUKOHDE_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("HYVAKSYTTY"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
  }

  @Test
  def vastaanottoOfAnotherPersonCountsOnlyForThatPerson(): Unit = {
    val toinenHakija = "1.2.246.562.24.00000000021"
    seedKaksiYpsHakutoivetta(ylempiTila = "HYVAKSYTTY", ylempiVastaanotto = None, alempiTila = "VARALLA")
    // Toinen hakija samaan hakukohteeseen; vain hänellä on vastaanotto toisessa haussa.
    insertHakemus(oppijanumero = toinenHakija, hakemusOid = HAKEMUS_OID_2, insertHaku = false)
    insertHakutoive(hakemusOid = HAKEMUS_OID_2, vastaanottotieto = None)
    insertValintarekisteri(hakemusOid = HAKEMUS_OID_2, valinnanTila = Some("HYVAKSYTTY"))
    insertVastaanottoToisessaHaussa(henkiloOid = toinenHakija)

    def hakijanVastaanottotieto(oppijanumero: String) =
      jsonPath("$.hakijat[?(@.oppijanumero == '" + oppijanumero + "')].hakemukset[0].vastaanottotieto")
    get(hakukohdeOid = Some(HAKUKOHDE_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](2)))
      .andExpect(hakijanVastaanottotieto(OPPIJANUMERO).value(contains("KESKEN")))
      .andExpect(hakijanVastaanottotieto(toinenHakija).value(contains("OTTANUT_VASTAAN_TOISEN_PAIKAN")))
  }

  @Test
  def sitovastiVastaanotettuWithSijoitteluLeavesOtherToiveToHakutoiveidenValisetSaannot(): Unit = {
    seedKaksiYpsHakutoivetta(
      ylempiTila = "HYVAKSYTTY",
      ylempiVastaanotto = Some("VASTAANOTTANUT_SITOVASTI"),
      alempiTila = "HYVAKSYTTY"
    )
    insertSijoitteluJaPriorisointi()

    // VTS:n järjestyksessä hakutoiveiden väliset säännöt peruunnuttavat alemman hakutoiveen ensin, jolloin
    // yhden paikan säännös ei enää koske sitä.
    get(hakukohdeOid = Some(HAKUKOHDE_OID_2))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("PERUUNTUNUT"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("KESKEN"))
  }

  @Test
  def toiveWithPassedVastaanottoDeadlineOttanutVastaanToisenPaikan(): Unit = {
    seedKaksiYpsHakutoivetta(ylempiTila = "HYVAKSYTTY", ylempiVastaanotto = None, alempiTila = "VARALLA")
    // Määräaika on mennyt, joten ilman yhden paikan säännöstä vastaanottotieto olisi EI_VASTAANOTETTU_MAARA_AIKANA.
    insertOhjausparametri("PH_OPVP", aikaleima = Some(paivienPaasta(-1)))
    insertVastaanottoToisessaHaussa()

    get(hakukohdeOid = Some(HAKUKOHDE_OID))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value("PERUUNTUNUT"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("OTTANUT_VASTAAN_TOISEN_PAIKAN"))
  }

  @Test
  def ottanutVastaanToisenPaikanWhenHakemusUnderLinkedHenkiloOid(): Unit = {
    initSchema()
    insertHenkilo()
    insertHenkiloAlias(oppijanumero = OPPIJANUMERO, henkiloOid = ALIAS_OID)
    // Hakemus on linkitetyllä oidilla, vastaanotto toisessa haussa master-oidilla.
    insertHakemus(oppijanumero = ALIAS_OID, insertHenkilo = false)
    insertHakukohde()
    insertHakutoive(vastaanottotieto = None)
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"))
    insertYpsToteutus()
    insertKoulutus(johtaaTutkintoon = Some(true), koulutustyyppi = Some("yo"))
    insertVastaanottoToisessaHaussa(henkiloOid = OPPIJANUMERO)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value("OTTANUT_VASTAAN_TOISEN_PAIKAN"))
  }

  // ---- Valintarajaus lasketulla tilalla ----

  @ParameterizedTest
  @CsvSource(
    Array(
      // valintarekisterin tila, julkaistavissa, gen_hakutoiveen valintatieto ja vastaanotto, odotettu valinnanTila.
      // Tyhjä arvo puuttuu; tyhjä odotettu tila: hakijaa ei palauteta.
      "HYVAKSYTTY, true,  HYVAKSYTTY,                         , HYVAKSYTTY",
      "HYVAKSYTTY, false, HYVAKSYTTY,                         ,",
      "VARALLA,    true,  VARALLA,    VASTAANOTTANUT_SITOVASTI, HYVAKSYTTY",
      "HYVAKSYTTY, true,            ,                         , HYVAKSYTTY",
      "HYLATTY,    true,  HYVAKSYTTY,                         ,"
    )
  )
  def hyvaksytytFiltersOnComputedValinnanTila(
    valinnanTila: String,
    julkaistavissa: Boolean,
    valintatieto: String,
    vastaanotto: String,
    odotettu: String
  ): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(valintatieto = Option(valintatieto), vastaanottotieto = Option(vastaanotto))
    insertValintarekisteri(valinnanTila = Some(valinnanTila), julkaistavissa = Some(julkaistavissa))

    val result = get(valintarajaus = Some("HYVAKSYTYT")).andExpect(status.isOk)
    Option(odotettu) match {
      case Some(tila) => result.andExpect(jsonPath("$.hakijat[0].hakemukset[0].valinnanTila").value(tila))
      case None       => result.andExpect(content.json("""{"hakijat": []}"""))
    }
  }

  @Test
  def hyvaksytytExcludesHakutoivePeruuntunutByVastaanottoOnAnotherHakutoive(): Unit = {
    seedKaksiHakutoivetta(
      ylempiTila = "HYVAKSYTTY",
      ylempiVastaanotto = Some(VASTAANOTTOTIETO),
      alempiTila = "HYVAKSYTTY"
    )
    insertOhjausparametri("sijoittelu", alkuperainenArvo = Some("true"))
    insertOhjausparametri("jarjestetytHakutoiveet", alkuperainenArvo = Some("true"))

    // Molemmat hakutoiveet ovat organisaatiossa ja gen_valintarekisterissä hyväksyttyjä, mutta alempi peruuntuu.
    get(hakukohdeOid = None, organisaatioOid = Some(ORGANISAATIO_OID), valintarajaus = Some("HYVAKSYTYT"))
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohde").value(HAKUKOHDE_OID))
    get(hakukohdeOid = Some(HAKUKOHDE_OID_2), valintarajaus = Some("HYVAKSYTYT"))
      .andExpect(status.isOk)
      .andExpect(content.json("""{"hakijat": []}"""))
  }

  @ParameterizedTest
  @CsvSource(
    Array(
      "VASTAANOTTANUT_SITOVASTI,    true,  true",
      "EHDOLLISESTI_VASTAANOTTANUT, true,  true",
      "VASTAANOTTANUT_SITOVASTI,    false, false",
      "PERUNUT,                     true,  false"
    )
  )
  def vastaanottaneetFiltersOnComputedVastaanottotila(
    vastaanotto: String,
    julkaistavissa: Boolean,
    palautetaan: Boolean
  ): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(vastaanottotieto = Some(vastaanotto))
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"), julkaistavissa = Some(julkaistavissa))

    val result = get(valintarajaus = Some("VASTAANOTTANEET")).andExpect(status.isOk)
    if (palautetaan) result.andExpect(jsonPath("$.hakijat[0].hakemukset[0].vastaanottotieto").value(vastaanotto))
    else result.andExpect(content.json("""{"hakijat": []}"""))
  }

  // ---- Valintatapajono meta ----

  @Test
  def valintatapajonoTyyppiAndNimiPopulated(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"))
    insertValintaperusteValintatapajono(
      valintatapajonoTyyppi = Some("valintatapajono_yp"),
      valintatapajonoNimi = Some("Yhteispistejono")
    )
    insertValintatapajonoKoodi("valintatapajono_yp", Some("Yhteispisteet"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononTyyppi").value("Yhteispisteet"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononNimi").value("Yhteispistejono"))
  }

  @Test
  def valintatapajonoMetaNullWhenNoVrRow(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononTyyppi").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononNimi").doesNotExist())
  }

  @Test
  def valintatapajonoMetaNullWhenLookupRowMissing(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"))
    // NO insertValintaperusteValintatapajono call → LEFT JOIN misses → both fields null

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononTyyppi").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononNimi").doesNotExist())
  }

  @Test
  def valintatapajonoTyyppiNullWhenColumnNull(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"))
    insertValintaperusteValintatapajono(
      valintatapajonoTyyppi = None,
      valintatapajonoNimi = Some("Nimi")
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononTyyppi").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononNimi").value("Nimi"))
  }

  @Test
  def valintatapajonoFromSamePriorityRowAsPisteet(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-winner",
      valinnanTila = Some("HYVAKSYTTY"),
      pisteet = Some(BigDecimal("90"))
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-loser",
      valinnanTila = Some("VARALLA"),
      pisteet = Some(BigDecimal("99"))
    )
    insertValintaperusteValintatapajono(
      valintatapajonoId = "vtj-winner",
      valintatapajonoTyyppi = Some("WINNER-TYYPPI"),
      valintatapajonoNimi = Some("Winner")
    )
    insertValintaperusteValintatapajono(
      valintatapajonoId = "vtj-loser",
      valintatapajonoTyyppi = Some("LOSER-TYYPPI"),
      valintatapajonoNimi = Some("Loser")
    )

    get()
      .andExpect(status.isOk)
      // Tyypille ei ole koodia, joten Suren tapaan "Tuntematon".
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononTyyppi").value("Tuntematon"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononNimi").value("Winner"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").value(90))
  }

  /** Yksi valintatapajono annetulla tilalla, tyypillä valintatapajono_kp ja sen koodilla. */
  private def seedHakijaWithValintatapajono(valinnanTila: String): Unit = {
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valinnanTila = Some(valinnanTila))
    insertValintaperusteValintatapajono(
      valintatapajonoTyyppi = Some("valintatapajono_kp"),
      valintatapajonoNimi = Some("Koepistejono")
    )
    insertValintatapajonoKoodi("valintatapajono_kp", Some("Koepisteet"))
  }

  @ParameterizedTest
  @ValueSource(strings = Array("HYVAKSYTTY", "HARKINNANVARAISESTI_HYVAKSYTTY", "VARASIJALTA_HYVAKSYTTY"))
  def valintatapajonoTyyppiAsKoodiNimiWhenHyvaksytty(valinnanTila: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedHakijaWithValintatapajono(valinnanTila)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononTyyppi").value("Koepisteet"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononNimi").value("Koepistejono"))
  }

  @ParameterizedTest
  @ValueSource(strings = Array("VARALLA", "HYLATTY", "PERUUNTUNUT"))
  def valintatapajonoTyyppiOmittedWhenNotHyvaksytty(valinnanTila: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedHakijaWithValintatapajono(valinnanTila)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononTyyppi").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononNimi").value("Koepistejono"))
  }

  @Test
  def valintatapajonoTyyppiTuntematonWhenKoodiMissing(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"))
    insertValintaperusteValintatapajono(valintatapajonoTyyppi = Some("valintatapajono_kp"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononTyyppi").value("Tuntematon"))
  }

  @Test
  def valintatapajonoTyyppiTuntematonWhenKoodiHasNoNimiFi(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(valinnanTila = Some("HYVAKSYTTY"))
    insertValintaperusteValintatapajono(valintatapajonoTyyppi = Some("valintatapajono_kp"))
    insertValintatapajonoKoodi("valintatapajono_kp", None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononTyyppi").value("Tuntematon"))
  }

  @Test
  def valintatapajonoFromMerkitsevaJonoEvenWhenNotPublished(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(
      valintatapajonoId = "vtj-hyvaksytty",
      valinnanTila = Some("HYVAKSYTTY"),
      julkaistavissa = Some(false)
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-varalla",
      valinnanTila = Some("VARALLA"),
      julkaistavissa = Some(true),
      pisteet = Some(BigDecimal("50"))
    )
    insertValintaperusteValintatapajono(valintatapajonoId = "vtj-hyvaksytty", valintatapajonoNimi = Some("Hyväksytty"))
    insertValintaperusteValintatapajono(valintatapajonoId = "vtj-varalla", valintatapajonoNimi = Some("Varalla"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononNimi").value("Hyväksytty"))
      // Hakutoive ei ole julkaistu (merkitsevä jono julkaisematon, kaikki jonot eivät julkaistuja),
      // joten VTS:n tapaan pisteitä ei näytetä.
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").doesNotExist())
  }

  @Test
  def valintatapajonoTiebreakByPrioriteettiWhenSameTila(): Unit = {
    seedMinimalHakija()
    insertValintarekisteri(valintatapajonoId = "vtj-2", valinnanTila = Some("HYVAKSYTTY"))
    insertValintarekisteri(valintatapajonoId = "vtj-1", valinnanTila = Some("HYVAKSYTTY"))
    insertValintarekisteriValintatapajono(valintatapajonoId = "vtj-2", prioriteetti = Some(2))
    insertValintarekisteriValintatapajono(valintatapajonoId = "vtj-1", prioriteetti = Some(1))
    insertValintaperusteValintatapajono(valintatapajonoId = "vtj-2", valintatapajonoNimi = Some("Toinen"))
    insertValintaperusteValintatapajono(valintatapajonoId = "vtj-1", valintatapajonoNimi = Some("Ensimmäinen"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononNimi").value("Ensimmäinen"))
  }

  @Test
  def valintatapajonoTiebreakUsesJononPrioriteettiNotJonosijanPrioriteetti(): Unit = {
    seedMinimalHakija()
    // Jonosijan prioriteetti (hakutoiveen numero) suosisi vtj-a:ta, jonon prioriteetti vtj-b:tä.
    insertValintarekisteri(
      valintatapajonoId = "vtj-a",
      valinnanTila = Some("HYVAKSYTTY"),
      prioriteetti = Some(1),
      pisteet = Some(BigDecimal("10"))
    )
    insertValintarekisteri(
      valintatapajonoId = "vtj-b",
      valinnanTila = Some("HYVAKSYTTY"),
      prioriteetti = Some(2),
      pisteet = Some(BigDecimal("20"))
    )
    insertValintarekisteriValintatapajono(valintatapajonoId = "vtj-a", prioriteetti = Some(2))
    insertValintarekisteriValintatapajono(valintatapajonoId = "vtj-b", prioriteetti = Some(1))
    insertValintaperusteValintatapajono(valintatapajonoId = "vtj-a", valintatapajonoNimi = Some("A"))
    insertValintaperusteValintatapajono(valintatapajonoId = "vtj-b", valintatapajonoNimi = Some("B"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononNimi").value("B"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pisteet").value(20))
  }

  @Test
  def valintatapajonoVarallaTiebreakByVarasijanumeroNotPrioriteetti(): Unit = {
    seedHakijaIlmanVastaanottoa()
    insertValintarekisteri(valintatapajonoId = "vtj-a", valinnanTila = Some("VARALLA"), varasijanNumero = Some(3))
    insertValintarekisteri(valintatapajonoId = "vtj-b", valinnanTila = Some("VARALLA"), varasijanNumero = Some(1))
    insertValintarekisteriValintatapajono(valintatapajonoId = "vtj-a", prioriteetti = Some(1))
    insertValintarekisteriValintatapajono(valintatapajonoId = "vtj-b", prioriteetti = Some(2))
    insertValintaperusteValintatapajono(valintatapajonoId = "vtj-a", valintatapajonoNimi = Some("A"))
    insertValintaperusteValintatapajono(valintatapajonoId = "vtj-b", valintatapajonoNimi = Some("B"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].valintatapajononNimi").value("B"))
  }

  // ---- Pohjakoulutus ----

  @Test
  def pohjakoulutusPopulatedFromHakemus(): Unit = {
    initSchema()
    insertHakemus(pohjakoulutusKk = Some("""["pohjakoulutus_yo"]"""))
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pohjakoulutus", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pohjakoulutus[0]").value("pohjakoulutus_yo"))
  }

  @Test
  def pohjakoulutusMultipleValuesPreserveOrder(): Unit = {
    initSchema()
    insertHakemus(pohjakoulutusKk = Some("""["pohjakoulutus_yo", "pohjakoulutus_kk"]"""))
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pohjakoulutus", hasSize[Any](2)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pohjakoulutus[0]").value("pohjakoulutus_yo"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pohjakoulutus[1]").value("pohjakoulutus_kk"))
  }

  @Test
  def pohjakoulutusEmptyWhenColumnNull(): Unit = {
    seedMinimalHakija() // insertHakemus default pohjakoulutusKk = None

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].pohjakoulutus").isEmpty)
  }

  // ---- HakukohteenKoulutukset ----

  @Test
  def hakukohteenKoulutuksetSingleKoodi(): Unit = {
    seedMinimalHakija()
    insertToteutus(koulutuksenAlkamisvuosi = Some(2026), koulutuksenAlkamiskausiuri = Some("kausi_s#1"))
    insertKoulutus(
      ulkoinenTunniste = Some("672301"),
      koulutuksetKoodiuri = Some("""["koulutus_331101#12"]"""),
      johtaaTutkintoon = Some(true)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].komoOid").value(KOULUTUS_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].tkKoulutuskoodi").value("331101"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].kkKoulutusId").value("672301"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].koulutuksenAlkamisvuosi").value(2026))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].koulutuksenAlkamiskausi").value("S"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].johtaaTutkintoon").value(true))
  }

  @Test
  def hakukohteenKoulutuksetMultipleKoodit(): Unit = {
    seedMinimalHakija()
    insertToteutus(koulutuksenAlkamisvuosi = Some(2026), koulutuksenAlkamiskausiuri = Some("kausi_s#1"))
    insertKoulutus(
      ulkoinenTunniste = Some("672301"),
      koulutuksetKoodiuri = Some("""["koulutus_331101#12", "koulutus_671101#5"]"""),
      johtaaTutkintoon = Some(true)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset", hasSize[Any](2)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].tkKoulutuskoodi").value("331101"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[1].tkKoulutuskoodi").value("671101"))
      // Shared sub-fields
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].komoOid").value(KOULUTUS_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[1].komoOid").value(KOULUTUS_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].kkKoulutusId").value("672301"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[1].kkKoulutusId").value("672301"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].koulutuksenAlkamisvuosi").value(2026))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[1].koulutuksenAlkamisvuosi").value(2026))
  }

  @Test
  def hakukohteenKoulutuksetEmptyWhenNoKoulutusRow(): Unit = {
    seedMinimalHakija() // no toteutus / koulutus rows

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset").isEmpty)
  }

  @Test
  def hakukohteenKoulutuksetOneTupleWithoutKoodiWhenKoodiuriEmpty(): Unit = {
    seedMinimalHakija()
    insertToteutus(koulutuksenAlkamisvuosi = Some(2026), koulutuksenAlkamiskausiuri = Some("kausi_s#1"))
    insertKoulutus(
      ulkoinenTunniste = Some("672301"),
      koulutuksetKoodiuri = None,
      johtaaTutkintoon = Some(true)
    )

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].komoOid").value(KOULUTUS_OID))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].tkKoulutuskoodi").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].kkKoulutusId").value("672301"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].koulutuksenAlkamisvuosi").value(2026))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].koulutuksenAlkamiskausi").value("S"))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].johtaaTutkintoon").value(true))
  }

  @ParameterizedTest
  @CsvSource(
    Array(
      "kausi_s#1, S",
      "kausi_k#2, K"
    )
  )
  def hakukohteenKoulutuksetKausiParsesToUppercaseLetter(kausiUri: String, expected: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedMinimalHakija()
    insertToteutus(koulutuksenAlkamisvuosi = Some(2026), koulutuksenAlkamiskausiuri = Some(kausiUri))
    insertKoulutus(koulutuksetKoodiuri = Some("""["koulutus_331101#12"]"""))

    get()
      .andExpect(status.isOk)
      .andExpect(
        jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].koulutuksenAlkamiskausi").value(expected)
      )
  }

  // ---- Ilmoittautumiset ----

  /** expected: välilyönnein erotetut kausi:tila-parit, esim. "2026S:4 2027K:4". */
  @ParameterizedTest
  @CsvSource(
    Array(
      "kausi_s#1,                       , 2026S:4 2027K:4",
      "kausi_s#1, EI_TEHTY              , 2026S:4 2027K:4",
      "kausi_s#1, EI_ILMOITTAUTUNUT     , 2026S:4 2027K:4",
      "kausi_s#1, LASNA_KOKO_LUKUVUOSI  , 2026S:1 2027K:1",
      "kausi_s#1, POISSA_KOKO_LUKUVUOSI , 2026S:2 2027K:2",
      "kausi_s#1, LASNA_SYKSY           , 2026S:1 2027K:2",
      "kausi_s#1, POISSA_SYKSY          , 2026S:2 2027K:1",
      "kausi_s#1, LASNA                 , 2027K:1",
      "kausi_s#1, POISSA                , 2027K:2",
      "kausi_k#1,                       , 2026S:4 2026K:4",
      "kausi_k#1, LASNA_KOKO_LUKUVUOSI  , 2026S:1 2026K:1",
      "kausi_k#1, LASNA_SYKSY           , 2026S:1 2026K:2",
      "kausi_k#1, LASNA                 , 2026K:1"
    )
  )
  def ilmoittautumisetFromAlkamiskausiAndTila(kausiUri: String, tila: String, expected: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(ilmoittautumisenTila = Option(tila))
    insertToteutus(koulutuksenAlkamisvuosi = Some(2026), koulutuksenAlkamiskausiuri = Some(kausiUri))

    val odotetut = expected.split(" ").toSeq.map(_.split(":") match { case Array(k, t) => (k, t.toInt) })
    val result   = get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].ilmoittautumiset", hasSize[Any](odotetut.size)))
    odotetut.zipWithIndex.foreach { case ((kausi, tilaKoodi), i) =>
      result
        .andExpect(jsonPath(s"$$.hakijat[0].hakemukset[0].ilmoittautumiset[$i].kausi").value(kausi))
        .andExpect(jsonPath(s"$$.hakijat[0].hakemukset[0].ilmoittautumiset[$i].tila").value(tilaKoodi))
    }
  }

  @Test
  def ilmoittautumisetEmptyWithoutAlkamiskausi(): Unit = {
    seedMinimalHakija()
    insertToteutus(koulutuksenAlkamisvuosi = Some(2026), koulutuksenAlkamiskausiuri = None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].ilmoittautumiset").isEmpty)
  }

  @Test
  def ilmoittautumisetEmptyWithoutAlkamisvuosi(): Unit = {
    seedMinimalHakija()
    insertToteutus(koulutuksenAlkamisvuosi = None, koulutuksenAlkamiskausiuri = Some("kausi_s#1"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].ilmoittautumiset").isEmpty)
  }

  @Test
  def ilmoittautumisetEmptyForUnknownTila(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(ilmoittautumisenTila = Some("TUNTEMATON"))
    insertToteutus(koulutuksenAlkamisvuosi = Some(2026), koulutuksenAlkamiskausiuri = Some("kausi_s#1"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].ilmoittautumiset").isEmpty)
  }

  // ---- Koulutuksen päätelty alkamiskausi ----

  /** Hakija, jonka hakukohteella, haulla ja toteutuksella on annetut alkamiskaudet, ja hakukohteen koulutus. */
  private def seedHakijaWithAlkamiskaudet(
    hakukohdeKausi: Option[String] = None,
    hakukohdeVuosi: Option[Int] = None,
    hakuKausi: Option[String] = None,
    hakuVuosi: Option[Int] = None,
    toteutusKausi: Option[String] = None,
    toteutusVuosi: Option[Int] = None,
    toteutusAlkamisaika: Option[java.time.OffsetDateTime] = None
  ): Unit = {
    initSchema()
    insertHakemus(insertHaku = false)
    insertHaku(koulutuksenAlkamiskausiuri = hakuKausi, koulutuksenAlkamisvuosi = hakuVuosi)
    insertHakukohde(koulutuksenAlkamiskausiuri = hakukohdeKausi, koulutuksenAlkamisvuosi = hakukohdeVuosi)
    insertHakutoive()
    insertToteutus(
      koulutuksenAlkamiskausiuri = toteutusKausi,
      koulutuksenAlkamisvuosi = toteutusVuosi,
      koulutuksenTarkkaAlkamisaika = toteutusAlkamisaika
    )
    insertKoulutus(koulutuksetKoodiuri = Some("""["koulutus_331101#12"]"""))
  }

  /** ilmoittautumiset: välilyönnein erotetut kaudet. */
  private def expectAlkamiskausi(kausi: String, vuosi: Int, ilmoittautumiset: String): Unit = {
    val kaudet = ilmoittautumiset.split(" ").toSeq
    val result = get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].koulutuksenAlkamiskausi").value(kausi))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].koulutuksenAlkamisvuosi").value(vuosi))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].ilmoittautumiset", hasSize[Any](kaudet.size)))
    kaudet.zipWithIndex.foreach { case (k, i) =>
      result.andExpect(jsonPath(s"$$.hakijat[0].hakemukset[0].ilmoittautumiset[$i].kausi").value(k))
    }
  }

  /** Lähteet järjestyksessä hakukohde, haku, toteutus; tyhjä = ei arvoa. */
  @ParameterizedTest
  @CsvSource(
    Array(
      "kausi_k#1, 2027, kausi_s#1, 2028, kausi_s#1, 2029, K, 2027, 2027S 2027K",
      "         ,     , kausi_s#1, 2028, kausi_k#1, 2029, S, 2028, 2028S 2029K",
      "         ,     ,          ,     , kausi_k#1, 2029, K, 2029, 2029S 2029K",
      "kausi_k#1,     , kausi_s#1, 2028,          ,     , S, 2028, 2028S 2029K",
      "         , 2027,          ,     , kausi_k#1, 2029, K, 2029, 2029S 2029K"
    )
  )
  def alkamiskausiFromHakukohdeThenHakuThenToteutus(
    hakukohdeKausi: String,
    hakukohdeVuosi: Integer,
    hakuKausi: String,
    hakuVuosi: Integer,
    toteutusKausi: String,
    toteutusVuosi: Integer,
    kausi: String,
    vuosi: Int,
    ilmoittautumiset: String
  ): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedHakijaWithAlkamiskaudet(
      hakukohdeKausi = Option(hakukohdeKausi),
      hakukohdeVuosi = Option(hakukohdeVuosi).map(_.toInt),
      hakuKausi = Option(hakuKausi),
      hakuVuosi = Option(hakuVuosi).map(_.toInt),
      toteutusKausi = Option(toteutusKausi),
      toteutusVuosi = Option(toteutusVuosi).map(_.toInt)
    )

    expectAlkamiskausi(kausi, vuosi, ilmoittautumiset)
  }

  @ParameterizedTest
  @CsvSource(
    Array(
      "2026-09-01T00:00:00+03:00, S, 2026, 2026S 2027K",
      "2027-01-15T00:00:00+02:00, K, 2027, 2027S 2027K",
      // Suomen aikaa jo elokuu.
      "2026-07-31T23:30:00Z,      S, 2026, 2026S 2027K"
    )
  )
  def alkamiskausiFromTarkkaAlkamisaika(
    alkamisaika: String,
    kausi: String,
    vuosi: Int,
    ilmoittautumiset: String
  ): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedHakijaWithAlkamiskaudet(toteutusAlkamisaika = Some(java.time.OffsetDateTime.parse(alkamisaika)))

    expectAlkamiskausi(kausi, vuosi, ilmoittautumiset)
  }

  @Test
  def alkamiskausiOmittedWithoutKausiAndVuosiOrAlkamisaika(): Unit = {
    // Pelkkä vuosi ei riitä, kuten Suressa (päätelty alkamiskausi).
    seedHakijaWithAlkamiskaudet(toteutusVuosi = Some(2026))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset", hasSize[Any](1)))
      .andExpect(
        jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].koulutuksenAlkamiskausi").doesNotExist()
      )
      .andExpect(
        jsonPath("$.hakijat[0].hakemukset[0].hakukohteenKoulutukset[0].koulutuksenAlkamisvuosi").doesNotExist()
      )
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].ilmoittautumiset").isEmpty)
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
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
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
      .andExpect(jsonPath("$.hakijat[0].syntymaaika").doesNotExist())
  }

  // ---- Hetu ----

  @ParameterizedTest
  @CsvSource(
    Array(
      "2005-04-15, 150405A",
      "1984-09-14, 140984-"
    )
  )
  def hetuReplacedWithSyntymaaikaWhenMissing(syntymaaika: String, expected: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    initSchema()
    insertHenkilo(syntymaaika = Some(java.time.LocalDate.parse(syntymaaika)), hetu = None)
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hetu").value(expected))
  }

  @Test
  def hetuReplacedWithSyntymaaikaWhenEmpty(): Unit = {
    initSchema()
    insertHenkilo(syntymaaika = Some(SYNTYMAAIKA), hetu = Some(""))
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hetu").value("150405A"))
  }

  @Test
  def hetuPreferredOverSyntymaaika(): Unit = {
    initSchema()
    insertHenkilo(syntymaaika = Some(SYNTYMAAIKA))
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hetu").value(HETU))
  }

  @Test
  def hetuEmptyWhenHetuAndSyntymaaikaMissing(): Unit = {
    initSchema()
    insertHenkilo(hetu = None) // syntymaaika oletuksena None
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hetu").value(""))
  }

  // ---- Henkilötiedot ONR:n master-henkilöltä ----

  @Test
  def henkilotiedotFromMasterNotLinkedHenkiloOrHakemus(): Unit = {
    initSchema()
    insertHenkilo(
      hetu = Some("140984-123A"),
      etunimet = Some("Master Etunimi"),
      sukunimi = Some("Mastersukunimi"),
      sukupuoli = Some(2),
      aidinkieli = Some("sv"),
      syntymaaika = Some(java.time.LocalDate.parse("1984-09-14")),
      kaikkiKansalaisuudet = Some("""["643"]"""),
      turvakielto = Some(true)
    )
    insertHenkiloAlias(
      oppijanumero = OPPIJANUMERO,
      henkiloOid = ALIAS_HENKILO_OID,
      hetu = Some("010101-111A"),
      etunimet = Some("Alias"),
      sukunimi = Some("Aliaksinen"),
      sukupuoli = Some(1),
      aidinkieli = Some("en"),
      syntymaaika = Some(java.time.LocalDate.parse("2001-01-01")),
      kaikkiKansalaisuudet = Some("""["752"]"""),
      turvakielto = Some(false)
    )
    // Hakemuksella on omat hetu-, nimi- ja sukupuolitietonsa (oletukset), jotka eivät saa näkyä.
    insertHakemus(oppijanumero = ALIAS_HENKILO_OID, insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(OPPIJANUMERO))
      .andExpect(jsonPath("$.hakijat[0].hetu").value("140984-123A"))
      .andExpect(jsonPath("$.hakijat[0].etunimet").value("Master Etunimi"))
      .andExpect(jsonPath("$.hakijat[0].sukunimi").value("Mastersukunimi"))
      .andExpect(jsonPath("$.hakijat[0].kutsumanimi").value(KUTSUMANIMI))
      .andExpect(jsonPath("$.hakijat[0].sukupuoli").value("2"))
      .andExpect(jsonPath("$.hakijat[0].aidinkieli").value("SV"))
      .andExpect(jsonPath("$.hakijat[0].syntymaaika").value("14.09.1984"))
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet[0]").value("643"))
      .andExpect(jsonPath("$.hakijat[0].turvakielto").value(true))
  }

  @Test
  def pseudoHetuWhenMasterHasNoHetuEvenIfHakemusHasHetu(): Unit = {
    initSchema()
    insertHenkilo(hetu = None, syntymaaika = Some(java.time.LocalDate.parse("1984-09-14")))
    insertHakemus(insertHenkilo = false, hetu = Some(HETU))
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hetu").value("140984-"))
  }

  @Test
  def nimetEmptyWhenMasterHasNoNimet(): Unit = {
    initSchema()
    insertHenkilo(etunimet = None, sukunimi = None)
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].etunimet").value(""))
      .andExpect(jsonPath("$.hakijat[0].sukunimi").value(""))
  }

  @Test
  def henkilotiedotFromHakemusAndLinkedHenkiloWhenMasterRowMissing(): Unit = {
    val puuttuvaMaster = "1.2.246.562.24.00000000088"
    initSchema()
    insertHenkiloAlias(
      oppijanumero = puuttuvaMaster,
      henkiloOid = ALIAS_HENKILO_OID,
      aidinkieli = Some("sv"),
      syntymaaika = Some(java.time.LocalDate.parse("1984-09-14")),
      kaikkiKansalaisuudet = Some("""["643"]"""),
      turvakielto = Some(true),
      // Linkitetyn henkilön hetua, nimiä ja sukupuolta ei käytetä, vaan hakemuksen.
      hetu = Some("010101-111A"),
      etunimet = Some("Alias"),
      sukunimi = Some("Aliaksinen"),
      sukupuoli = Some(2)
    )
    insertHakemus(oppijanumero = ALIAS_HENKILO_OID, insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].oppijanumero").value(puuttuvaMaster))
      .andExpect(jsonPath("$.hakijat[0].hetu").value(HETU))
      .andExpect(jsonPath("$.hakijat[0].etunimet").value(ETUNIMET))
      .andExpect(jsonPath("$.hakijat[0].sukunimi").value(SUKUNIMI))
      .andExpect(jsonPath("$.hakijat[0].sukupuoli").value(SUKUPUOLI.toString))
      .andExpect(jsonPath("$.hakijat[0].aidinkieli").value("SV"))
      .andExpect(jsonPath("$.hakijat[0].syntymaaika").value("14.09.1984"))
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet[0]").value("643"))
      .andExpect(jsonPath("$.hakijat[0].turvakielto").value(true))
  }

  @Test
  def aidinkieliUnknownWhenMissing(): Unit = {
    initSchema()
    insertHenkilo(aidinkieli = None)
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].aidinkieli").value("99"))
  }

  @Test
  def kansalaisuudetEmptyListWhenMissing(): Unit = {
    initSchema()
    insertHenkilo(kaikkiKansalaisuudet = None)
    insertHakemus(insertHenkilo = false)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet").isArray)
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet", hasSize[Any](0)))
  }

  // ---- Kotikunta ----

  @Test
  def kotikuntaUnknownWhenMissing(): Unit = {
    initSchema()
    insertHakemus(kotikunta = None)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].kotikunta").value("999"))
  }

  @ParameterizedTest
  @CsvSource(Array("12", "1234", "abc"))
  def kotikuntaUnknownWhenNotThreeDigits(kotikunta: String): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    initSchema()
    insertHakemus(kotikunta = Some(kotikunta))
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].kotikunta").value("999"))
  }

  // ---- Osoite ----

  @Test
  def osoiteEmptyWhenColumnsNull(): Unit = {
    initSchema()
    insertHakemus(lahiosoite = None, postinumero = None, postitoimipaikka = None)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].lahiosoite").value(""))
      .andExpect(jsonPath("$.hakijat[0].postinumero").value(""))
      .andExpect(jsonPath("$.hakijat[0].postitoimipaikka").value(""))
  }

  @Test
  def postitoimipaikkaFromUlkKuntaWhenAsuinmaaAbroad(): Unit = {
    initSchema()
    insertHakemus(asuinmaa = Some("826"), postitoimipaikka = None, ulkKunta = Some("London"))
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].postitoimipaikka").value("London"))
      .andExpect(jsonPath("$.hakijat[0].maa").value("826"))
  }

  @Test
  def postitoimipaikkaFromUlkKuntaAndMaaFinlandWhenAsuinmaaMissing(): Unit = {
    initSchema()
    insertHakemus(asuinmaa = None, postitoimipaikka = Some(HELSINKI), ulkKunta = Some("London"))
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].postitoimipaikka").value("London"))
      .andExpect(jsonPath("$.hakijat[0].maa").value("246"))
  }

  @Test
  def postitoimipaikkaKeptWhenAsuinmaaFinland(): Unit = {
    initSchema()
    insertHakemus(asuinmaa = Some("246"), postitoimipaikka = Some(HELSINKI), ulkKunta = Some("London"))
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].postitoimipaikka").value(HELSINKI))
  }

  @Test
  def postitoimipaikkaEmptyWhenAbroadAndUlkKuntaMissing(): Unit = {
    initSchema()
    insertHakemus(asuinmaa = Some("826"), postitoimipaikka = Some(HELSINKI), ulkKunta = None)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].postitoimipaikka").value(""))
  }

  // ---- Viimeisin muokkaus ----

  @Test
  def hakemusViimeinenMuokkausAikaleimaFromLuotu(): Unit = {
    initSchema()
    insertHakemus(
      luotu = Some(java.time.OffsetDateTime.parse("2025-09-10T08:00:00+03:00")),
      muokattu = Some(java.time.OffsetDateTime.parse("2025-09-20T08:00:00+03:00"))
    )
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakemusViimeinenMuokkausAikaleima").value("2025-09-10T08:00:00"))
  }

  @Test
  def hakemusViimeinenMuokkausAikaleimaOmittedWhenLuotuMissing(): Unit = {
    initSchema()
    insertHakemus(luotu = None)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakemusViimeinenMuokkausAikaleima").doesNotExist())
  }

  // ---- Luvat ja koulusivistyskielet ----

  @Test
  def luvatFalseWhenColumnsNull(): Unit = {
    initSchema()
    insertHakemus(koulutusmarkkinointilupa = None, valintatuloksenJulkaisulupa = None)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].koulutusmarkkinointilupa").value(false))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].julkaisulupa").value(false))
  }

  @Test
  def koulusivistyskieletOmittedUntilAvailable(): Unit = {
    seedMinimalHakija()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].koulusivistyskielet").doesNotExist())
  }

  // ---- Kansalaisuudet / turvakielto (gen_henkilo-sourced) ----

  @Test
  def removedFieldsAreNotInResponse(): Unit = {
    seedMinimalHakija() // insertHenkilo default kansalaisuus = Some("246")

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].kansalaisuudet[0]").value(SUOMI_KOODI))
      .andExpect(jsonPath("$.hakijat[0].kansalaisuus").doesNotExist())
      .andExpect(jsonPath("$.hakijat[0].kaksoiskansalaisuus").doesNotExist())
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
      .andExpect(jsonPath("$.hakijat[0].yoSuoritusVuosi").doesNotExist())
  }

  @Test
  def yoSuoritusVuosiNullWhenValmistumisVuosiIsNull(): Unit = {
    seedMinimalHakija()
    insertYlioppilas(onYlioppilas = true, valmistumisVuosi = None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].yoSuoritusVuosi").doesNotExist())
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
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").doesNotExist())
  }

  @Test
  def ensikertalainenFalseWhenArvoIsFalse(): Unit = {
    seedMinimalHakija()
    insertSupaTieto(avain = "ensikertalainen", arvo = Some("false"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").value(false))
  }

  @Test
  def ensikertalainenFalseWhenArvoIsQuotedFalse(): Unit = {
    seedMinimalHakija()
    insertSupaTieto(avain = "ensikertalainen", arvo = Some("\"false\""))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").value(false))
  }

  @Test
  def ensikertalainenNullWhenArvoIsUnrecognized(): Unit = {
    seedMinimalHakija()
    insertSupaTieto(avain = "ensikertalainen", arvo = Some("maybe"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").doesNotExist())
  }

  @Test
  def ensikertalainenNullWhenArvoIsNull(): Unit = {
    seedMinimalHakija()
    insertSupaTieto(avain = "ensikertalainen", arvo = None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").doesNotExist())
  }

  @Test
  def ensikertalainenTrueWinsWhenBothTrueAndFalseRowsExist(): Unit = {
    seedMinimalHakija()
    insertSupaTieto(avain = "ensikertalainen", arvo = Some("false"))
    insertSupaTieto(avain = "ensikertalainen", arvo = Some("true"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").value(true))
  }

  @Test
  def excelLeavesEnsikertalainenCellEmptyWhenFalse(): Unit = {
    seedMinimalHakija()
    insertSupaTieto(avain = "ensikertalainen", arvo = Some("false"))

    val result   = getExcel().andExpect(status.isOk).andReturn()
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(result.getResponse.getContentAsByteArray))
    try {
      val sheet  = workbook.getSheetAt(0)
      val header = sheet.getRow(0).getCell(23).getStringCellValue
      val cell   = sheet.getRow(1).getCell(23).getStringCellValue
      assert(header == "On ensikertalainen", s"unexpected header at cell 23: $header")
      assert(cell == "", s"expected empty ensikertalainen cell, got: '$cell'")
    } finally workbook.close()
  }

  @Test
  def ensikertalainenIgnoresIrrelevantAvain(): Unit = {
    seedMinimalHakija()
    insertSupaTieto(avain = "some_other_key", arvo = Some("true"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").doesNotExist())
  }

  /** Hakija, jonka ainoalla hakutoiveella on annettu gen_hakutoive-ensikertalaisuus. */
  private def seedHakijaWithHakutoiveEnsikertalainen(ensikertalainen: Option[Boolean]): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(ensikertalainen = ensikertalainen)
  }

  @ParameterizedTest
  @ValueSource(booleans = Array(true, false))
  def ensikertalainenFromHakutoiveWhenNoSupaTieto(ensikertalainen: Boolean): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedHakijaWithHakutoiveEnsikertalainen(Some(ensikertalainen))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").value(ensikertalainen))
  }

  @ParameterizedTest
  @ValueSource(booleans = Array(true, false))
  def ensikertalainenSupaTietoWinsOverHakutoive(supaTieto: Boolean): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    seedHakijaWithHakutoiveEnsikertalainen(Some(!supaTieto))
    insertSupaTieto(avain = "ensikertalainen", arvo = Some(supaTieto.toString))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").value(supaTieto))
  }

  @Test
  def ensikertalainenFromHakutoiveWhenSupaTietoUnrecognized(): Unit = {
    seedHakijaWithHakutoiveEnsikertalainen(Some(true))
    insertSupaTieto(avain = "ensikertalainen", arvo = Some("maybe"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").value(true))
  }

  @Test
  def ensikertalainenFromHakutoiveIgnoresNullHakutoiveRows(): Unit = {
    seedHakijaWithHakutoiveEnsikertalainen(Some(true))
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2, ensikertalainen = None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].ensikertalainen").value(true))
  }

  @Test
  def excelShowsEnsikertalainenFromHakutoive(): Unit = {
    seedHakijaWithHakutoiveEnsikertalainen(Some(true))

    val result   = getExcel().andExpect(status.isOk).andReturn()
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(result.getResponse.getContentAsByteArray))
    try {
      val cell = workbook.getSheetAt(0).getRow(1).getCell(23).getStringCellValue
      assert(cell == "X", s"expected ensikertalainen cell X, got: '$cell'")
    } finally workbook.close()
  }

  // ---- Inaktivoidut hakemukset ----

  @Test
  def inactivatedHakemusExcludedEvenWithHakutoiveRow(): Unit = {
    initSchema()
    // dbt pudottaisi inaktivoidun hakemuksen hakutoiveet; tässä rivi jätetään, jotta rajaus näkyy.
    insertHakemus(poistettu = Some(true))
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](0)))
  }

  @Test
  def hakemusReturnedWhenPoistettuNull(): Unit = {
    initSchema()
    insertHakemus(poistettu = None)
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
  }

  @Test
  def inactivatedHakemusExcludedFromHakukohdeSearch(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertHakemus(hakemusOid = HAKEMUS_OID_2, insertHenkilo = false, insertHaku = false, poistettu = Some(true))
    insertHakutoive(hakemusOid = HAKEMUS_OID_2)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakemusnumero").value(HAKEMUS_OID))
  }

  @Test
  def inactivatedHakemusExcludedFromOppijanumeroSearch(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertHakemus(hakemusOid = HAKEMUS_OID_2, insertHenkilo = false, insertHaku = false, poistettu = Some(true))
    insertHakutoive(hakemusOid = HAKEMUS_OID_2)

    getByOppijanumero(OPPIJANUMERO)
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakemusnumero").value(HAKEMUS_OID))
  }

  // ---- Hakemusmaksu ----

  /** Hakemusmaksun tila tyhjänä (CsvSourcen tyhjä arvo) tarkoittaa, ettei maksutietoa ole. */
  @ParameterizedTest
  @CsvSource(
    Array(
      ", true",
      "not-required, true",
      "paid, true",
      "ok-by-proxy, true",
      "awaiting, false",
      "overdue, false",
      "foo, false"
    )
  )
  def hakemusIncludedOnlyWhenHakemusmaksuApprovedOrMissing(hakemusmaksunTila: String, palautetaan: Boolean): Unit = {
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "reset for parameterized case")
    initSchema()
    insertHakemus(hakemusmaksunTila = Option(hakemusmaksunTila))
    insertHakukohde()
    insertHakutoive()

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](if (palautetaan) 1 else 0)))
  }

  @Test
  def unpaidHakemusExcludedButPaidReturnedInSameHakukohde(): Unit = {
    initSchema()
    insertHakemus(hakemusmaksunTila = Some("paid"))
    insertHakukohde()
    insertHakutoive()
    insertHakemus(
      hakemusOid = HAKEMUS_OID_2,
      insertHenkilo = false,
      insertHaku = false,
      hakemusmaksunTila = Some("awaiting")
    )
    insertHakutoive(hakemusOid = HAKEMUS_OID_2)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](1)))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakemusnumero").value(HAKEMUS_OID))
  }

  @Test
  def unpaidHakemusExcludedFromOppijanumeroSearch(): Unit = {
    initSchema()
    insertHakemus(hakemusmaksunTila = Some("awaiting"))
    insertHakukohde()
    insertHakutoive()

    getByOppijanumero(OPPIJANUMERO)
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat", hasSize[Any](0)))
  }

  // ---- Hakuvuosi ja hakukausi ----

  /** Seedaa hakijan, jonka haun omat vuosi/kausi poikkeavat koulutuksen alkamisvuosista/-kausista. */
  private def seedHakijaWithHakuVuosiKausi(hakuVuosi: Option[Int], hakuKausi: Option[String]): Unit = {
    initSchema()
    insertHenkilo()
    insertHaku(
      koulutuksenAlkamisvuosi = Some(2028),
      koulutuksenAlkamiskausiuri = Some("kausi_s#1"),
      hakuVuosi = hakuVuosi,
      hakuKausi = hakuKausi
    )
    insertHakemus(insertHenkilo = false, insertHaku = false)
    insertHakukohde(koulutuksenAlkamisvuosi = Some(2029), koulutuksenAlkamiskausiuri = Some("kausi_s#1"))
    insertHakutoive()
    insertToteutus(koulutuksenAlkamisvuosi = Some(2031), koulutuksenAlkamiskausiuri = Some("kausi_s#1"))
  }

  @Test
  def hakuVuosiJaKausiTulevatHaunOmistaKentista(): Unit = {
    seedHakijaWithHakuVuosiKausi(hakuVuosi = Some(2030), hakuKausi = Some("kausi_k#1"))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakuVuosi").value(2030))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakuKausi").value("K"))
  }

  @Test
  def hakuVuosiJaKausiEivatPeriKoulutuksenAlkamisestaKunHaullaEiArvoja(): Unit = {
    seedHakijaWithHakuVuosiKausi(hakuVuosi = None, hakuKausi = None)

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakuVuosi").value(0))
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakuKausi").value(""))
  }

  @ParameterizedTest
  @CsvSource(
    value = Array(
      "kausi_s#1, S",
      "kausi_k#2, K"
    )
  )
  def hakuKausiParsesToUppercaseLetter(kausiUri: String, expected: String): Unit = {
    seedHakijaWithHakuVuosiKausi(hakuVuosi = Some(2030), hakuKausi = Some(kausiUri))

    get()
      .andExpect(status.isOk)
      .andExpect(jsonPath("$.hakijat[0].hakemukset[0].hakuKausi").value(expected))
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
  def excelAcceptsHakukohderyhmaAsTheOnlyRajaus(): Unit = {
    stubHakukohderyhma(List(HAKUKOHDE_OID))
    seedMinimalHakija()

    getExcel(hakukohdeOid = None, organisaatioOid = None, hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID))
      .andExpect(status.isOk)
      .andExpect(header.string("Content-Disposition", containsString("kkhakijat-")))
  }

  @Test
  def excelReturns400WhenNoRajaavaParametriProvided(): Unit = {
    initSchema()

    getExcel(hakukohdeOid = None, organisaatioOid = None)
      .andExpect(status.isBadRequest)
      .andExpect(
        content.json(
          """{"status": 400, "message": "virhe.validointi",
             "details": ["hakukohdeOid_or_organisaatioOid_or_hakukohderyhmaOid.required"] }"""
        )
      )
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
    insertHenkilo(syntymaaika = Some(SYNTYMAAIKA)) // → cell 1 = SYNTYMAAIKA_STR
    insertHakemus(
      insertHenkilo = false,
      asiointikieli = Some(1),
      pohjakoulutusKk = Some("""["pohjakoulutus_yo", "pohjakoulutus_kk"]""")
    ) // → cell 18 = "1", cell 43 = "pohjakoulutus_yo,pohjakoulutus_kk"
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
    insertToteutus(koulutuksenAlkamisvuosi = Some(2026), koulutuksenAlkamiskausiuri = Some("kausi_s#1"))
    insertKoulutus(
      ulkoinenTunniste = Some("TKID-1"),
      koulutuksetKoodiuri = Some("""["koulutus_331101#12"]"""),
      johtaaTutkintoon = Some(true)
    ) // → cell 32 = "TKID-1", cell 48 = Koulutus(...)
    insertValintaperusteValintatapajono(
      valintatapajonoTyyppi = Some("valintatapajono_yp"),
      valintatapajonoNimi = Some("Yhteispistejono")
    )
    insertValintatapajonoKoodi(
      "valintatapajono_yp",
      Some("Yhteispisteet")
    ) // → cell 39 = "Yhteispisteet", cell 40 = "Yhteispistejono"
    insertKasittelymerkinta(ELIGIBILITY_REQUIREMENT, Some("conditionally-eligible"))
    insertKasittelymerkinta(PAYMENT_REQUIREMENT, Some("obligated"))
    // → cell 45 = "CONDITIONALLY_ELIGIBLE", cell 47 = "REQUIRED"

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
        36 -> "2025-10-01T12:00:00",
        37 -> "82.5",
        38 -> "HyvaksymisenEhto(X,,Ehto FI,Ehto SV,Ehto EN)",
        39 -> "Yhteispisteet",
        40 -> "Yhteispistejono",
        41 -> VASTAANOTTOTIETO,
        42 -> "Lasna(Syksy(2026)),Lasna(Kevat(2027))",
        43 -> "pohjakoulutus_yo,pohjakoulutus_kk",
        44 -> "X",
        45 -> "CONDITIONALLY_ELIGIBLE",
        47 -> "REQUIRED",
        48 -> s"Koulutus($KOULUTUS_OID,331101,TKID-1,2026,S,true)"
      )
      expectedCells.foreach { case (idx, value) =>
        assert(
          dataRow.getCell(idx).getStringCellValue == value,
          s"cell $idx expected [$value] but was [${dataRow.getCell(idx).getStringCellValue}]"
        )
      }
      // deferred fields: cells 12, 19, 34, 46, 49-59 stay ""
      // (45 ja 47 tulevat käsittelymerkinnöistä, 48 koulutuksista)
      Seq(12, 19, 34, 46, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59)
        .foreach { idx =>
          assert(
            dataRow.getCell(idx).getStringCellValue == "",
            s"deferred cell $idx expected empty but was [${dataRow.getCell(idx).getStringCellValue}]"
          )
        }
    } finally workbook.close()
  }

  @Test
  def excelHasMultipleKoulutuksesInCells48And49(): Unit = {
    seedMinimalHakija()
    insertToteutus(koulutuksenAlkamisvuosi = Some(2026), koulutuksenAlkamiskausiuri = Some("kausi_s#1"))
    insertKoulutus(
      ulkoinenTunniste = Some("TKID-1"),
      koulutuksetKoodiuri = Some("""["koulutus_331101#12", "koulutus_671101#5"]"""),
      johtaaTutkintoon = Some(true)
    )

    val bytes    = getExcel().andExpect(status.isOk).andReturn().getResponse.getContentAsByteArray
    val workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))
    try {
      val dataRow = workbook.getSheetAt(0).getRow(1)
      assert(dataRow.getCell(48).getStringCellValue == s"Koulutus($KOULUTUS_OID,331101,TKID-1,2026,S,true)")
      assert(dataRow.getCell(49).getStringCellValue == s"Koulutus($KOULUTUS_OID,671101,TKID-1,2026,S,true)")
      Seq(50, 51, 52, 53).foreach { idx =>
        assert(
          dataRow.getCell(idx).getStringCellValue == "",
          s"cell $idx expected empty but was [${dataRow.getCell(idx).getStringCellValue}]"
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
