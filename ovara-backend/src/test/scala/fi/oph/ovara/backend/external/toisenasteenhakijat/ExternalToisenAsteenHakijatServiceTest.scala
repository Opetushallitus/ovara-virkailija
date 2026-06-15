package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.external.toisenasteenhakijat.ExternalToisenAsteenHakijatTestData.*
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
class ExternalToisenAsteenHakijatServiceTest
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterEach
    with ExternalToisenAsteenHakijatTestUtils {

  @Autowired
  override val db: ReadOnlyDatabase = null

  @Autowired
  private val service: ExternalToisenAsteenHakijatService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  override def beforeEach(): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "Drop everything")
  }

  "getHakijat" should "return error string when there's a db error" in {
    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    assert(response.isLeft)
    assert(response.left.toOption.get == "virhe.tietokanta")
  }

  it should "return empty list when no hakijat match" in {
    initSchema()

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    assert(response.isRight)
    assert(response.toOption.get.isEmpty)
  }

  it should "return hakija matched by hakukohdeOid" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertToteutusJaKoulutus()

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    val hakija = getOnlyHakija(response)
    assert(hakija.oppijanumero == OPPIJANUMERO)
    assert(hakija.sahkoposti == EMAIL)
    assert(hakija.matkapuhelin == MATKAPUHELIN)
    assert(hakija.lahiosoite == LAHIOSOITE)
    assert(hakija.postinumero == POSTINUMERO)
    assert(hakija.postitoimipaikka == HELSINKI)
    assert(hakija.hetu.contains(HETU))
    assert(hakija.sukunimi.contains(SUKUNIMI))
    assert(hakija.etunimet.contains(ETUNIMET))
    assert(hakija.kutsumanimi.contains(KUTSUMANIMI))
    assert(hakija.kotikunta.contains(KOTIKUNTA))
    assert(hakija.maa.contains(SUOMI_KOODI))
    assert(hakija.kansalaisuudet == Seq("246"))
    assert(hakija.sukupuoli.contains(SUKUPUOLI.toString))
    assert(hakija.koulutusmarkkinointilupa.contains(KOULUTUSMARKKINOINTILUPA))
    assert(hakija.kiinnostunutoppisopimuksesta.contains(KIINNOSTUNUT_OPPISOPIMUKSESTA))
    assert(hakija.sahkoisenAsioinninLupa.contains(SAHKOINENVIESTINTALUPA))
    assert(hakija.hakemus.hakemusnumero == HAKEMUS_OID)
    assert(hakija.hakemus.julkaisulupa.contains(VALINTATULOKSEN_JULKAISULUPA))
    assert(hakija.hakemus.hakemuksenJattopaiva.contains(JATETTY))
    assert(hakija.hakemus.hakemuksenMuokkauspaiva.contains(MUOKATTU))
    assert(hakija.hakemus.hakutoiveet.size == 1)
    val hakutoive = hakija.hakemus.hakutoiveet.head
    assert(hakutoive.hakukohdeOid == HAKUKOHDE_OID)
    assert(hakutoive.hakujno == 1)
    assert(hakutoive.opetuspiste.contains(ORGANISAATIO_OID))
    assert(hakutoive.koulutus.exists(_.versioituUri == KOULUTUS_KOODIURI))
  }

  it should "return hakija matched by organisaatioOid" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()

    val response = service.getHakijat(HAKU_OID, None, Some(ORGANISAATIO_OID))

    val hakija = getOnlyHakija(response)
    assert(hakija.oppijanumero == OPPIJANUMERO)
  }

  it should "return None / empty for fields that have no source yet" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    val hakija = getOnlyHakija(response)
    assert(hakija.muupuhelin.isEmpty)
    assert(hakija.aidinkieli.isEmpty)
    assert(hakija.opetuskieli.isEmpty)
    assert(hakija.huoltaja1.isEmpty)
    assert(hakija.huoltaja2.isEmpty)
    assert(hakija.oikeusMaksuttomaanKoulutukseenVoimassaAsti.isEmpty)
    assert(hakija.oppivelvollisuusVoimassaAsti.isEmpty)
    assert(hakija.lisakysymykset.isEmpty)
    assert(hakija.hakemus.vuosi.isEmpty)
    assert(hakija.hakemus.kausi.isEmpty)
    assert(hakija.hakemus.lahtokoulu.isEmpty)
    assert(hakija.hakemus.luokka.isEmpty)
    assert(hakija.hakemus.pohjakoulutus.isEmpty)
    assert(hakija.hakemus.osaaminen.yleinen_kielitutkinto_fi.isEmpty)
    assert(hakija.hakemus.osaaminen.valtionhallinnon_kielitutkinto_en.isEmpty)
  }

  it should "return None for fields whose gen_hakemus columns are NULL" in {
    initSchema()
    insertHakemus(
      etunimet = None,
      kutsumanimi = None,
      sukunimi = None,
      hetu = None,
      kotikunta = None,
      sukupuoli = None,
      kansalaisuusJson = None,
      koulutusmarkkinointilupa = None,
      kiinnostunutOppisopimuksesta = None,
      sahkoinenviestintalupa = None,
      valintatuloksenJulkaisulupa = None,
      jatetty = None,
      muokattu = None
    )
    insertHakukohde()
    insertHakutoive()

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    val hakija = getOnlyHakija(response)
    assert(hakija.hetu.isEmpty)
    assert(hakija.sukunimi.isEmpty)
    assert(hakija.etunimet.isEmpty)
    assert(hakija.kansalaisuudet.isEmpty)
    assert(hakija.sukupuoli.isEmpty)
    assert(hakija.sahkoisenAsioinninLupa.isEmpty)
    assert(hakija.hakemus.julkaisulupa.isEmpty)
    assert(hakija.hakemus.hakemuksenJattopaiva.isEmpty)
    assert(hakija.hakemus.hakemuksenMuokkauspaiva.isEmpty)
  }

  it should "not return hakemus when hakemus_oid is not 35 characters" in {
    initSchema()
    insertHakemus(hakemusOid = "1.2.246.562.11.3511892")
    insertHakukohde()
    insertHakutoive(hakemusOid = "1.2.246.562.11.3511892")

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    assert(response.isRight)
    assert(response.toOption.get.isEmpty)
  }

  it should "not return hakemus when haku has wrong kohdejoukko" in {
    initSchema()
    insertHenkilo()
    insertHaku(kohdejoukkoKoodiuri = "haunkohdejoukko_12#1")
    insertHakemus(insertHenkilo = false, insertHaku = false)
    insertHakukohde()
    insertHakutoive()

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    assert(response.isRight)
    assert(response.toOption.get.isEmpty)
  }

  it should "not return hakija whose hakutoive points to a different hakukohde" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2)

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    assert(response.isRight)
    assert(response.toOption.get.isEmpty)
  }

  it should "filter by hakuOid" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()

    val response = service.getHakijat(HAKU_OID_2, Some(HAKUKOHDE_OID), None)

    assert(response.isRight)
    assert(response.toOption.get.isEmpty)
  }

  private def getOnlyHakija(response: Either[String, Seq[ToisenAsteenHakija]]): ToisenAsteenHakija = {
    assert(response.isRight, s"expected Right but got $response")
    assert(response.toOption.get.size == 1, s"expected single hakija but got ${response.toOption.get}")
    response.toOption.get.head
  }
}
