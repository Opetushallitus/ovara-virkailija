package fi.oph.ovara.backend.opiskelijavalintatieto

import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import slick.jdbc.H2Profile.api.*

@SpringBootTest
@ActiveProfiles(Array("test"))
class OpiskelijavalintatietoServiceTest extends AnyFlatSpec with Matchers with OpiskelijavalintatietoTestUtils {

  @Autowired
  override val db: ReadOnlyDatabase = null

  @Autowired
  private val service: OpiskelijavalintatietoService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "get" should "return error string when there's a db error" in {
    val response = service.get(List("1.2.246.562.24.9"))

    assert(response.isLeft)
    assert(response.left.toOption.get == "virhe.tietokanta")
  }

  it should "return only oppijat that really exist" in {
    initSchema()
    insertHenkilo()
    insertHenkilo("1.2.246.562.24.999")

    val response = service.get(List(OPPIJANUMERO, "1.2.246.562.24.8"))

    val head = getOnlyOppija(response)
    assert(head.oppijanumero == OPPIJANUMERO)
  }

  it should "return hakemus without haku names when haku not found" in {
    initAndInsert()
    db.run(sqlu"""DELETE FROM gen.gen_haku WHERE haku_oid = $HAKU_OID""", "Delete haku row")

    val response = service.get(List(OPPIJANUMERO))

    val hakemus = getOnlyHakemus(response)
    assert(hakemus.haku.oid == HAKU_OID)
    assert(hakemus.haku.nimi.isEmpty)
    assert(hakemus.haunKohdejoukko.isEmpty)
    assert(hakemus.hakutapa.isEmpty)
  }

  it should "return hakemus without hakukohde names when hakukohde not found" in {
    initAndInsert()
    db.run(sqlu"""DELETE FROM gen.gen_hakukohde WHERE hakukohde_oid = $HAKUKOHDE_OID""", "Delete hakukohde row")

    val response = service.get(List(OPPIJANUMERO))

    val hakutoive = getOnlyHakutoive(response)
    assert(hakutoive.hakukohde.oid == HAKUKOHDE_OID)
    assert(hakutoive.hakukohde.nimi.isEmpty)
  }

  it should "return hakemus without tarjoaja names when tarjoaja not found" in {
    initAndInsert()
    db.run(
      sqlu"""DELETE FROM gen.gen_organisaatio WHERE organisaatio_oid = $ORGANISAATIO_OID""",
      "Delete organisaatio row"
    )

    val response = service.get(List(OPPIJANUMERO))

    val hakutoive = getOnlyHakutoive(response)
    assert(hakutoive.tarjoaja.contains(Nimetty(ORGANISAATIO_OID, Map.empty)))
  }

  it should "read alkamiskausi and alkamisvuosi first from hakukohde and then from haku" in {
    initAndInsert()
    val otherHakukohdeOid = "1.2.246.562.20.901"
    db.run(
      sqlu"""INSERT INTO gen.gen_hakukohde VALUES ($otherHakukohdeOid, $HAKU_OID, 'Maisterihaku', 'Magisteransökan', 'Master''s Admission', $ORGANISAATIO_OID, null, null)""",
      "Insert hakukohde without alkamiskausi and vuosi"
    )
    db.run(
      sqlu"""INSERT INTO gen.gen_hakutoive VALUES ($HAKEMUS_OID, $otherHakukohdeOid, $HAKU_OID, $OPPIJANUMERO, null, null, null)""",
      "Insert hakutoive for hakukohde without alkamiskausi and vuosi"
    )

    val response = service.get(List(OPPIJANUMERO))

    val oppija = getOnlyOppija(response)
    assert(oppija.hakemukset.size == 1)
    assert(oppija.hakemukset.head.hakutoiveet.size == 2)
    val hakutoiveWithHakukohdeValues = oppija.hakemukset.head.hakutoiveet.find(_.hakukohde.oid == HAKUKOHDE_OID).get
    assert(hakutoiveWithHakukohdeValues.koulutuksenAlkamiskausiUri.contains("kausi_s#1"))
    assert(hakutoiveWithHakukohdeValues.koulutuksenAlkamisvuosi.contains(2022))
    val hakutoiveWithHakuValues = oppija.hakemukset.head.hakutoiveet.find(_.hakukohde.oid == otherHakukohdeOid).get
    assert(hakutoiveWithHakuValues.koulutuksenAlkamiskausiUri.contains("kausi_k#1"))
    assert(hakutoiveWithHakuValues.koulutuksenAlkamisvuosi.contains(2023))
  }

  it should "returns alkamiskausi and alkamisvuosi as nulls when both hakukohde and haku don't have them" in {
    initAndInsert()
    val otherHakukohdeOid = "1.2.246.562.20.901"
    db.run(
      sqlu"""UPDATE gen.gen_haku SET koulutuksen_alkamiskausiuri = null, koulutuksen_alkamisvuosi = null""",
      "Remove alkamiskausi and vuosi from haku"
    )
    db.run(
      sqlu"""UPDATE gen.gen_hakukohde SET koulutuksen_alkamiskausiuri = null, koulutuksen_alkamisvuosi = null""",
      "Remove alkamiskausi and vuosi from hakukohde"
    )

    val response = service.get(List(OPPIJANUMERO))

    val hakutoive = getOnlyHakutoive(response)
    assert(hakutoive.koulutuksenAlkamiskausiUri.isEmpty)
    assert(hakutoive.koulutuksenAlkamisvuosi.isEmpty)
  }

  it should "return tila information when set on the hakutoive" in {
    db.run(
      sqlu"""UPDATE gen.gen_hakutoive SET
             valintatieto = 'HYVAKSYTTY',
             vastaanottotieto = 'VASTAANOTTANUT_SITOVASTI',
             ilmoittautumisen_tila = 'LASNA_KOKO_LUKUVUOSI'""",
      "Set tila information for hakutoive"
    )

    val response = service.get(List(OPPIJANUMERO))

    val hakutoive = getOnlyHakutoive(response)
    assert(hakutoive.valinnanTila.contains("HYVAKSYTTY"))
    assert(hakutoive.vastaanotonTila.contains("VASTAANOTTANUT_SITOVASTI"))
    assert(hakutoive.ilmoittautumisenTila.contains("LASNA_KOKO_LUKUVUOSI"))
  }

  private def getOnlyOppija(response: Either[String, Seq[Opiskelijavalintatieto]]): Opiskelijavalintatieto = {
    assert(response.isRight)
    assert(response.toOption.get.size == 1)
    response.toOption.get.head
  }

  private def getOnlyHakemus(response: Either[String, Seq[Opiskelijavalintatieto]]): Hakemus = {
    val oppija = getOnlyOppija(response)
    assert(oppija.hakemukset.size == 1)
    oppija.hakemukset.head
  }

  private def getOnlyHakutoive(response: Either[String, Seq[Opiskelijavalintatieto]]): Hakutoive = {
    val hakemus = getOnlyHakemus(response)
    assert(hakemus.hakutoiveet.size == 1)
    hakemus.hakutoiveet.head
  }

}
