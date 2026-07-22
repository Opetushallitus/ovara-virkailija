package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.external.kkhakijat.ExternalKKHakijatTestData.*
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import slick.jdbc.H2Profile.api.*

@SpringBootTest
@ActiveProfiles(Array("test"))
class ExternalKKHakijatServiceTest
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterEach
    with ExternalKKHakijatTestUtils {

  @Autowired
  override val db: ReadOnlyDatabase = null

  @Autowired
  private val service: ExternalKKHakijatService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  override def beforeEach(): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "Drop everything")
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

  private def getOnlyHakija(response: Either[String, Seq[KKHakija]]): KKHakija = {
    assert(response.isRight, s"expected Right but got $response")
    assert(response.toOption.get.size == 1, s"expected single hakija but got ${response.toOption.get}")
    response.toOption.get.head
  }
}
