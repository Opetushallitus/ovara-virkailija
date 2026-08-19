package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.external.OrganisaatioHierarkiaStub
import fi.oph.ovara.backend.external.kkhakijat.ExternalKKHakijatTestData.*
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.service.CommonService
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.invocation.InvocationOnMock
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
    with OrganisaatioHierarkiaStub {

  @Autowired
  override val db: ReadOnlyDatabase = null

  @Autowired
  private val service: ExternalKKHakijatService = null

  // Organisaatiohierarkia luetaan pub-skeemasta, jota näiden testien H2-kanta ei sisällä
  // (initSchema luo vain gen-taulut). Ks. OrganisaatioHierarkiaStub.
  @MockitoBean
  override val commonService: CommonService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  override def beforeEach(): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "Drop everything")
    stubOrganisaatioHierarkiaAsIdentity()
  }

  "getKKHakijat" should "paakayttaja scope returns all hakutoiveet" in {
    seedMinimalHakija()

    val response = service.getKKHakijat(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScope.paakayttaja
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
      KayttooikeusScope.limited(Set(ORGANISAATIO_OID))
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
      KayttooikeusScope.limited(Set("1.2.246.562.10.99999"))
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
      KayttooikeusScope.limited(Set.empty)
    )

    assert(response.toOption.get.isEmpty)
  }

  it should "match organisaatioOid against the descendants of the selected organisaatio" in {
    // Hakukohteen järjestyspaikka on ORGANISAATIO_OID, joka on KOULUTUSTOIMIJA_OID:n lapsi.
    seedMinimalHakija()
    withOrganisaatioHierarkia(Map(KOULUTUSTOIMIJA_OID -> List(ORGANISAATIO_OID)))

    val response = service.getKKHakijat(HAKU_OID, None, Some(KOULUTUSTOIMIJA_OID))

    assert(getOnlyHakija(response).oppijanumero == OPPIJANUMERO)
  }

  it should "not widen the organisaatioOid rajaus to organisaatiot outside the hierarkia" in {
    seedMinimalHakija()
    withOrganisaatioHierarkia(Map(KOULUTUSTOIMIJA_OID -> List(ORGANISAATIO_OID_2)))

    val response = service.getKKHakijat(HAKU_OID, None, Some(KOULUTUSTOIMIJA_OID))

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
      KayttooikeusScope.limited(Set(KOULUTUSTOIMIJA_OID))
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
      KayttooikeusScope.limited(Set(KOULUTUSTOIMIJA_OID))
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

    val response   = service.getKKHakijat(HAKU_OID, None, Some(ORGANISAATIO_OID))
    val hakemukset = getOnlyHakija(response).hakemukset

    assert(hakemukset.size == 1, s"expected only org-matched hakutoive, got $hakemukset")
    assert(hakemukset.head.hakukohde == HAKUKOHDE_OID)
    assert(hakemukset.head.organisaatio == ORGANISAATIO_OID)
  }

  // ---- Hakukohderyhmä ----

  it should "expand a hakukohderyhma to all of its hakukohteet" in {
    seedHakijaWithTwoHakutoiveet()
    stubHakukohderyhma(List(HAKUKOHDE_OID, HAKUKOHDE_OID_2))

    val response = service.getKKHakijat(HAKU_OID, None, None, hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID))

    val hakemukset = getOnlyHakija(response).hakemukset
    assert(hakemukset.map(_.hakukohde).toSet == Set(HAKUKOHDE_OID, HAKUKOHDE_OID_2))
  }

  it should "return only the hakukohteet of the ryhma, not every hakutoive of the hakemus" in {
    seedHakijaWithTwoHakutoiveet()
    stubHakukohderyhma(List(HAKUKOHDE_OID_2))

    val response = service.getKKHakijat(HAKU_OID, None, None, hakukohderyhmaOid = Some(HAKUKOHDERYHMA_OID))

    val hakemukset = getOnlyHakija(response).hakemukset
    assert(hakemukset.size == 1, s"expected only the ryhmä's hakutoive, got $hakemukset")
    assert(hakemukset.head.hakukohde == HAKUKOHDE_OID_2)
  }

  it should "intersect hakukohde with hakukohderyhma" in {
    seedHakijaWithTwoHakutoiveet()
    stubHakukohderyhma(List(HAKUKOHDE_OID, HAKUKOHDE_OID_2))

    val response = service.getKKHakijat(
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

    val response = service.getKKHakijat(
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

    val response = service.getKKHakijat(
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

    val response = service.getKKHakijat(
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
      KayttooikeusScope.limited(Set(ORGANISAATIO_OID)),
      Some(HAKUKOHDERYHMA_OID)
    )

    val hakemukset = getOnlyHakija(response).hakemukset
    assert(hakemukset.size == 1, s"expected only the in-scope hakutoive, got $hakemukset")
    assert(hakemukset.head.hakukohde == HAKUKOHDE_OID)
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

  /**
   * Hakukohderyhmän laajennus tulee pub-skeemasta, jota näiden testien H2-kanta ei sisällä.
   * Stubataan doAnswer-muodossa samasta syystä kuin OrganisaatioHierarkiaStubissa.
   */
  private def stubHakukohderyhma(hakukohdeOids: List[String]): Unit =
    doAnswer((_: InvocationOnMock) => hakukohdeOids)
      .when(commonService)
      .getHakukohderyhmanHakukohdeOids(any[String](), any[String]())

  private def getOnlyHakija(response: Either[String, Seq[KKHakija]]): KKHakija = {
    assert(response.isRight, s"expected Right but got $response")
    assert(response.toOption.get.size == 1, s"expected single hakija but got ${response.toOption.get}")
    response.toOption.get.head
  }
}
