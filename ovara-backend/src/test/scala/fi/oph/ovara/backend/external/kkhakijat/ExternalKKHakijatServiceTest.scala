package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.external.{HakukohderyhmaStub, OrganisaatioHierarkiaStub}
import fi.oph.ovara.backend.external.kkhakijat.ExternalKKHakijatTestData.*
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.service.CommonService
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.`override`.mockito.MockitoBean
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import slick.jdbc.H2Profile.api.*

@SpringBootTest
@ActiveProfiles(Array("test"))
class ExternalKKHakijatServiceTest
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterEach
    with ExternalKKHakijatTestUtils
    with OrganisaatioHierarkiaStub
    with HakukohderyhmaStub {

  @Autowired
  override val db: ReadOnlyDatabase = null

  @Autowired
  private val service: ExternalKKHakijatService = null

  // Organisaatiohierarkia luetaan pub-skeemasta, jota näiden testien H2-kanta ei sisällä
  // (initSchema luo vain gen-taulut). Ks. OrganisaatioHierarkiaStub.
  @MockitoBean
  override val commonService: CommonService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  // ScalaTest ei aja Springin test-metodikohtaista elinkaarta, joten mockia ei nollata
  // automaattisesti testien välillä. Nollataan itse, jotta verify(..., never()) mittaa vain
  // käsillä olevaa testiä.
  override def beforeEach(): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "Drop everything")
    reset(commonService)
    stubOrganisaatioHierarkiaAsIdentity()
  }

  "getKKHakijat" should "paakayttaja scope returns all hakutoiveet" in {
    seedMinimalHakija()

    val response = service.getKKHakijat(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.paakayttaja
    )

    assert(response.toOption.get.size == 1)
  }

  it should "limited scope with matching org keeps the hakutoive" in {
    seedMinimalHakija()

    val response = service.getKKHakijat(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set(ORGANISAATIO_OID))
    )

    assert(response.toOption.get.size == 1)
  }

  it should "limited scope with unrelated org drops the hakija" in {
    seedMinimalHakija()

    val response = service.getKKHakijat(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set("1.2.246.562.10.99999"))
    )

    assert(response.toOption.get.isEmpty)
  }

  it should "limited scope with empty org set drops all hakijas" in {
    seedMinimalHakija()

    val response = service.getKKHakijat(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set.empty)
    )

    assert(response.toOption.get.isEmpty)
  }

  it should "match organisaatioOid against the descendants of the selected organisaatio" in {
    // Hakukohteen järjestyspaikka on ORGANISAATIO_OID, joka on KOULUTUSTOIMIJA_OID:n lapsi.
    seedMinimalHakija()
    withOrganisaatioHierarkia(Map(KOULUTUSTOIMIJA_OID -> List(ORGANISAATIO_OID)))

    val response = getKKHakijatAsPaakayttaja(HAKU_OID, None, Some(KOULUTUSTOIMIJA_OID))

    assert(getOnlyHakija(response).oppijanumero == OPPIJANUMERO)
  }

  it should "not widen the organisaatioOid rajaus to organisaatiot outside the hierarkia" in {
    seedMinimalHakija()
    withOrganisaatioHierarkia(Map(KOULUTUSTOIMIJA_OID -> List(ORGANISAATIO_OID_2)))

    val response = getKKHakijatAsPaakayttaja(HAKU_OID, None, Some(KOULUTUSTOIMIJA_OID))

    assert(response.toOption.get.isEmpty)
  }

  it should "limited scope on koulutustoimija level keeps a hakutoive järjestetty by a lapsiorganisaatio" in {
    seedMinimalHakija()
    withOrganisaatioHierarkia(Map(KOULUTUSTOIMIJA_OID -> List(ORGANISAATIO_OID)))

    val response = service.getKKHakijat(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set(KOULUTUSTOIMIJA_OID))
    )

    assert(response.toOption.get.size == 1)
  }

  it should "limited scope on koulutustoimija level still drops hakutoiveet outside its hierarkia" in {
    seedMinimalHakija()
    withOrganisaatioHierarkia(Map(KOULUTUSTOIMIJA_OID -> List(ORGANISAATIO_OID_2)))

    val response = service.getKKHakijat(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set(KOULUTUSTOIMIJA_OID))
    )

    assert(response.toOption.get.isEmpty)
  }

  it should "return only the org-matching hakutoive when hakija has hakutoiveet at multiple orgs" in {
    initSchema()
    insertHakemus()
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID,
      jarjestyspaikkaOid = ORGANISAATIO_OID,
      organisaatioOid = Some(ORGANISAATIO_OID)
    )
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID_2,
      jarjestyspaikkaOid = ORGANISAATIO_OID_2,
      organisaatioOid = Some(ORGANISAATIO_OID_2)
    )
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, hakutoivenumero = 1)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2)

    val response   = getKKHakijatAsPaakayttaja(HAKU_OID, None, Some(ORGANISAATIO_OID))
    val hakemukset = getOnlyHakija(response).hakemukset

    assert(hakemukset.size == 1, s"expected only org-matched hakutoive, got $hakemukset")
    assert(hakemukset.head.hakukohde == HAKUKOHDE_OID)
    assert(hakemukset.head.organisaatio == ORGANISAATIO_OID)
  }

  // ---- Hakukohderyhmä ----

  it should "expand a hakukohderyhma to all of its hakukohteet" in {
    seedHakijaWithTwoHakutoiveet()
    stubHakukohderyhma(List(HAKUKOHDE_OID, HAKUKOHDE_OID_2))

    val response = getKKHakijatAsPaakayttaja(HAKU_OID, None, None, hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID))

    val hakemukset = getOnlyHakija(response).hakemukset
    assert(hakemukset.map(_.hakukohde).toSet == Set(HAKUKOHDE_OID, HAKUKOHDE_OID_2))
  }

  it should "return only the hakukohteet of the ryhma, not every hakutoive of the hakemus" in {
    seedHakijaWithTwoHakutoiveet()
    stubHakukohderyhma(List(HAKUKOHDE_OID_2))

    val response = getKKHakijatAsPaakayttaja(HAKU_OID, None, None, hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID))

    val hakemukset = getOnlyHakija(response).hakemukset
    assert(hakemukset.size == 1, s"expected only the ryhmä's hakutoive, got $hakemukset")
    assert(hakemukset.head.hakukohde == HAKUKOHDE_OID_2)
  }

  it should "intersect hakukohde with hakukohderyhma" in {
    seedHakijaWithTwoHakutoiveet()
    stubHakukohderyhma(List(HAKUKOHDE_OID, HAKUKOHDE_OID_2))

    val response = getKKHakijatAsPaakayttaja(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID)
    )

    val hakemukset = getOnlyHakija(response).hakemukset
    assert(hakemukset.size == 1, s"expected only the intersected hakutoive, got $hakemukset")
    assert(hakemukset.head.hakukohde == HAKUKOHDE_OID)
  }

  it should "return nothing when the selected hakukohde is outside the hakukohderyhma" in {
    seedHakijaWithTwoHakutoiveet()
    stubHakukohderyhma(List(HAKUKOHDE_OID_2))

    val response = getKKHakijatAsPaakayttaja(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID)
    )

    assert(response.toOption.get.isEmpty)
  }

  it should "intersect hakukohderyhma with organisaatioOid" in {
    seedHakijaWithTwoHakutoiveet()
    stubHakukohderyhma(List(HAKUKOHDE_OID, HAKUKOHDE_OID_2))

    val response = getKKHakijatAsPaakayttaja(
      HAKU_OID,
      None,
      Some(ORGANISAATIO_OID),
      hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID)
    )

    val hakemukset = getOnlyHakija(response).hakemukset
    assert(hakemukset.size == 1, s"expected only the org-matched hakutoive, got $hakemukset")
    assert(hakemukset.head.hakukohde == HAKUKOHDE_OID)
  }

  // Regressiovahti: tyhjä ryhmälaajennus ei saa pudottaa hakukohderajausta pois, jolloin
  // kyselyyn jäisi vain organisaatiorajaus ja tulos laajenisi koko organisaatioon.
  it should "return nothing when an empty hakukohderyhma is combined with organisaatioOid" in {
    seedHakijaWithTwoHakutoiveet()
    stubHakukohderyhma(List.empty)

    val response = getKKHakijatAsPaakayttaja(
      HAKU_OID,
      None,
      Some(ORGANISAATIO_OID),
      hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID)
    )

    assert(response.toOption.get.isEmpty)
  }

  it should "still drop hakutoiveet outside the kayttooikeus scope when a hakukohderyhma is given" in {
    seedHakijaWithTwoHakutoiveet()
    stubHakukohderyhma(List(HAKUKOHDE_OID, HAKUKOHDE_OID_2))

    val response = service.getKKHakijat(
      HAKU_OID,
      None,
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set(ORGANISAATIO_OID)),
      Some(HAKUKOHDERYHMA_OID)
    )

    val hakemukset = getOnlyHakija(response).hakemukset
    assert(hakemukset.size == 1, s"expected only the in-scope hakutoive, got $hakemukset")
    assert(hakemukset.head.hakukohde == HAKUKOHDE_OID)
  }

  // ---- Hakukohderyhmäoikeudet ----
  // Käyttöoikeus voi olla myönnetty hakukohderyhmälle organisaation sijaan. Fixtuureissa
  // HAKUKOHDE_OID on ORGANISAATIO_OID:n ja HAKUKOHDE_OID_2 on ORGANISAATIO_OID_2:n järjestämä,
  // eikä ryhmäoikeuden käyttäjällä ole kumpaankaan organisaatio-oikeutta.

  it should "let a hakukohderyhma right alone grant access to the ryhmä's hakukohteet" in {
    seedHakijaWithTwoHakutoiveet()
    withHakukohderyhmat(Map(HAKUKOHDERYHMA_OID -> List(HAKUKOHDE_OID)))

    val response = service.getKKHakijat(
      HAKU_OID,
      None,
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set.empty, Set(HAKUKOHDERYHMA_OID)),
      Some(HAKUKOHDERYHMA_OID)
    )

    val hakemukset = getOnlyHakija(response).hakemukset
    assert(hakemukset.size == 1, s"expected only the ryhmä's hakutoive, got $hakemukset")
    assert(hakemukset.head.hakukohde == HAKUKOHDE_OID)
  }

  it should "let a hakukohderyhma right grant access via a hakukohdeOid inside the ryhmä" in {
    seedHakijaWithTwoHakutoiveet()
    withHakukohderyhmat(Map(HAKUKOHDERYHMA_OID -> List(HAKUKOHDE_OID)))

    val response = service.getKKHakijat(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set.empty, Set(HAKUKOHDERYHMA_OID))
    )

    assert(getOnlyHakija(response).hakemukset.map(_.hakukohde) == Seq(HAKUKOHDE_OID))
  }

  it should "drop a hakukohdeOid outside the user's hakukohderyhma" in {
    seedHakijaWithTwoHakutoiveet()
    withHakukohderyhmat(Map(HAKUKOHDERYHMA_OID -> List(HAKUKOHDE_OID_2)))

    val response = service.getKKHakijat(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set.empty, Set(HAKUKOHDERYHMA_OID))
    )

    assert(response.toOption.get.isEmpty)
  }

  it should "return nothing when the requested hakukohderyhma is disjoint from the user's" in {
    seedHakijaWithTwoHakutoiveet()
    withHakukohderyhmat(
      Map(
        HAKUKOHDERYHMA_OID   -> List(HAKUKOHDE_OID),
        HAKUKOHDERYHMA_OID_2 -> List(HAKUKOHDE_OID_2)
      )
    )

    val response = service.getKKHakijat(
      HAKU_OID,
      None,
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set.empty, Set(HAKUKOHDERYHMA_OID)),
      Some(HAKUKOHDERYHMA_OID_2)
    )

    assert(response.toOption.get.isEmpty)
  }

  it should "intersect the requested hakukohderyhma with the user's hakukohderyhma right" in {
    seedHakijaWithTwoHakutoiveet()
    withHakukohderyhmat(
      Map(
        HAKUKOHDERYHMA_OID   -> List(HAKUKOHDE_OID, HAKUKOHDE_OID_2),
        HAKUKOHDERYHMA_OID_2 -> List(HAKUKOHDE_OID_2)
      )
    )

    val response = service.getKKHakijat(
      HAKU_OID,
      None,
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set.empty, Set(HAKUKOHDERYHMA_OID)),
      Some(HAKUKOHDERYHMA_OID_2)
    )

    val hakemukset = getOnlyHakija(response).hakemukset
    assert(hakemukset.size == 1, s"expected only the shared hakutoive, got $hakemukset")
    assert(hakemukset.head.hakukohde == HAKUKOHDE_OID_2)
  }

  it should "union organisaatio and hakukohderyhma rights" in {
    seedHakijaWithTwoHakutoiveet()
    withHakukohderyhmat(
      Map(
        // Käyttäjän ryhmäoikeus kattaa vain toisen organisaation järjestämän hakukohteen,
        // organisaatio-oikeus toisen -- yhdessä molemmat hakutoiveet.
        HAKUKOHDERYHMA_OID   -> List(HAKUKOHDE_OID_2),
        HAKUKOHDERYHMA_OID_2 -> List(HAKUKOHDE_OID, HAKUKOHDE_OID_2)
      )
    )

    val response = service.getKKHakijat(
      HAKU_OID,
      None,
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set(ORGANISAATIO_OID), Set(HAKUKOHDERYHMA_OID)),
      Some(HAKUKOHDERYHMA_OID_2)
    )

    val hakemukset = getOnlyHakija(response).hakemukset
    assert(hakemukset.map(_.hakukohde).toSet == Set(HAKUKOHDE_OID, HAKUKOHDE_OID_2))
  }

  it should "union two hakukohderyhma rights" in {
    seedHakijaWithTwoHakutoiveet()
    withHakukohderyhmat(
      Map(
        HAKUKOHDERYHMA_OID   -> List(HAKUKOHDE_OID),
        HAKUKOHDERYHMA_OID_2 -> List(HAKUKOHDE_OID_2)
      )
    )

    // Rajaimena ei anneta ryhmää, joten hakukohderajaus tulee suoraan hakukohde-oidista;
    // molempien oikeusryhmien on silti näyttävä.
    val hakemukset = List(HAKUKOHDE_OID, HAKUKOHDE_OID_2).flatMap { hakukohdeOid =>
      val response = service.getKKHakijat(
        HAKU_OID,
        Some(hakukohdeOid),
        None,
        Valintarajaus.HAKENEET,
        KayttooikeusScopeKK.limited(Set.empty, Set(HAKUKOHDERYHMA_OID, HAKUKOHDERYHMA_OID_2))
      )
      getOnlyHakija(response).hakemukset
    }

    assert(hakemukset.map(_.hakukohde).toSet == Set(HAKUKOHDE_OID, HAKUKOHDE_OID_2))
  }

  it should "not widen past the request rajaus when the ryhmä right covers more hakukohteet" in {
    seedHakijaWithTwoHakutoiveet()
    withHakukohderyhmat(Map(HAKUKOHDERYHMA_OID -> List(HAKUKOHDE_OID, HAKUKOHDE_OID_2)))

    val response = service.getKKHakijat(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set.empty, Set(HAKUKOHDERYHMA_OID))
    )

    assert(getOnlyHakija(response).hakemukset.map(_.hakukohde) == Seq(HAKUKOHDE_OID))
  }

  // Organisaatiorajaus on katettava organisaatio-oikeuksilla: ryhmäoikeus ei riitä siihen.
  it should "ignore hakukohderyhma rights when organisaatioOid is given" in {
    seedHakijaWithTwoHakutoiveet()
    withHakukohderyhmat(Map(HAKUKOHDERYHMA_OID -> List(HAKUKOHDE_OID)))

    val response = service.getKKHakijat(
      HAKU_OID,
      None,
      Some(ORGANISAATIO_OID),
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set.empty, Set(HAKUKOHDERYHMA_OID))
    )

    assert(response.toOption.get.isEmpty)
    verify(commonService, never()).getHakukohderyhmanHakukohdeOids(any[String](), any[String]())
  }

  it should "keep only org-right rows when organisaatioOid is given and the user also has ryhmä rights" in {
    seedHakijaWithTwoHakutoiveet()
    withHakukohderyhmat(Map(HAKUKOHDERYHMA_OID -> List(HAKUKOHDE_OID_2)))

    val response = service.getKKHakijat(
      HAKU_OID,
      None,
      Some(ORGANISAATIO_OID),
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set(ORGANISAATIO_OID), Set(HAKUKOHDERYHMA_OID))
    )

    val hakemukset = getOnlyHakija(response).hakemukset
    assert(hakemukset.size == 1, s"expected only the org-matched hakutoive, got $hakemukset")
    assert(hakemukset.head.hakukohde == HAKUKOHDE_OID)
  }

  // Ryhmän jäsenyys on hakukohtaista: toisen haun laajennus ei saa kelpuuttaa tätä hakua.
  it should "expand the ryhmä rights with the requested hakuOid" in {
    seedHakijaWithTwoHakutoiveet()
    withHakukohderyhmatPerHaku(Map((HAKUKOHDERYHMA_OID, "1.2.246.562.29.00000000000000000999") -> List(HAKUKOHDE_OID)))

    val response = service.getKKHakijat(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.limited(Set.empty, Set(HAKUKOHDERYHMA_OID))
    )

    assert(response.toOption.get.isEmpty)
  }

  it should "not expand ryhmä rights for paakayttaja" in {
    seedHakijaWithTwoHakutoiveet()

    val response = service.getKKHakijat(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScopeKK.paakayttaja.copy(allowedHakukohderyhmaOids = Set(HAKUKOHDERYHMA_OID))
    )

    assert(getOnlyHakija(response).hakemukset.map(_.hakukohde) == Seq(HAKUKOHDE_OID))
    verify(commonService, never()).getHakukohderyhmanHakukohdeOids(any[String](), any[String]())
  }

  /** Yksi hakija, kaksi hakutoivetta eri organisaatioiden järjestämiin hakukohteisiin. */
  private def seedHakijaWithTwoHakutoiveet(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID,
      jarjestyspaikkaOid = ORGANISAATIO_OID,
      organisaatioOid = Some(ORGANISAATIO_OID)
    )
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID_2,
      jarjestyspaikkaOid = ORGANISAATIO_OID_2,
      organisaatioOid = Some(ORGANISAATIO_OID_2)
    )
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, hakutoivenumero = 1)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2)
  }

  private def getKKHakijatAsPaakayttaja(
    hakuOid: String,
    hakukohdeOid: Option[String],
    organisaatioOid: Option[String],
    valintarajaus: Valintarajaus = Valintarajaus.HAKENEET,
    hakukohderyhmaOid: Option[String] = None
  ): Either[String, Seq[KKHakija]] =
    service.getKKHakijat(
      hakuOid,
      hakukohdeOid,
      organisaatioOid,
      valintarajaus,
      KayttooikeusScopeKK.paakayttaja,
      hakukohderyhmaOid
    )

  private def getOnlyHakija(response: Either[String, Seq[KKHakija]]): KKHakija = {
    assert(response.isRight, s"expected Right but got $response")
    assert(response.toOption.get.size == 1, s"expected single hakija but got ${response.toOption.get}")
    response.toOption.get.head
  }
}
