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
    insertOpetuskieli()
    insertOrganisaatio()
    insertHakemusToinenAsteYhteishaku()

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
    assert(hakija.aidinkieli.contains(AIDINKIELI))
    assert(hakija.opetuskieli.contains(OPETUSKIELI))
    assert(hakija.koulutusmarkkinointilupa.contains(KOULUTUSMARKKINOINTILUPA))
    assert(hakija.kiinnostunutoppisopimuksesta.contains(KIINNOSTUNUT_OPPISOPIMUKSESTA))
    assert(hakija.sahkoisenAsioinninLupa.contains(SAHKOINENVIESTINTALUPA))
    assert(hakija.huoltaja1.contains(HUOLTAJA1))
    assert(hakija.huoltaja2.contains(HUOLTAJA2))
    assert(hakija.hakemus.hakemusnumero == HAKEMUS_OID)
    assert(hakija.hakemus.vuosi.contains(VUOSI))
    assert(hakija.hakemus.kausi.contains(KAUSI))
    assert(hakija.hakemus.julkaisulupa.contains(VALINTATULOKSEN_JULKAISULUPA))
    assert(hakija.hakemus.hakemuksenJattopaiva.contains(JATETTY))
    assert(hakija.hakemus.hakemuksenMuokkauspaiva.contains(MUOKATTU))
    assert(hakija.hakemus.hakutoiveet.size == 1)
    val hakutoive = hakija.hakemus.hakutoiveet.head
    assert(hakutoive.hakukohdeOid == HAKUKOHDE_OID)
    assert(hakutoive.hakujno == 1)
    assert(hakutoive.opetuspiste.contains(ORGANISAATIO_OID))
    assert(hakutoive.oppilaitos.contains(OPPILAITOS))
    assert(hakutoive.koulutus.exists(_.versioituUri == KOULUTUS_KOODIURI))
    assert(hakutoive.terveys.contains(TERVEYS))
    assert(hakutoive.aiempiperuminen.contains(AIEMPI_PERUMINEN))
    assert(hakutoive.kaksoistutkinto.contains(KAKSOISTUTKINTO))
  }

  it should "leave terveys/aiempiperuminen/kaksoistutkinto None when hakukohdeOid is not in hakukohteet array" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertHakemusToinenAsteYhteishaku(
      hakukohteetJson = Some(
        s"""[{"oid":"$HAKUKOHDE_OID_2","terveys":true,"aiempiPeruminen":true,"kiinnostunutKaksoistutkinnosta":true}]"""
      )
    )

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(hakutoive.terveys.isEmpty)
    assert(hakutoive.aiempiperuminen.isEmpty)
    assert(hakutoive.kaksoistutkinto.isEmpty)
  }

  it should "leave terveys/aiempiperuminen/kaksoistutkinto None when hakukohteet column is null" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
    insertHakemusToinenAsteYhteishaku(hakukohteetJson = None)

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(hakutoive.terveys.isEmpty)
    assert(hakutoive.aiempiperuminen.isEmpty)
    assert(hakutoive.kaksoistutkinto.isEmpty)
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
    assert(hakija.oikeusMaksuttomaanKoulutukseenVoimassaAsti.isEmpty)
    assert(hakija.oppivelvollisuusVoimassaAsti.isEmpty)
    assert(hakija.lisakysymykset.isEmpty)
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

  it should "normalize kausi koodisto uri 'kausi_k#1' to 'K'" in {
    initSchema()
    insertHakemus()
    insertHakukohde(koulutuksenAlkamiskausiuri = Some("kausi_k#1"))
    insertHakutoive()

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    val hakija = getOnlyHakija(response)
    assert(hakija.hakemus.kausi.contains("K"))
  }

  it should "return None for kausi when koulutuksen_alkamiskausiuri is missing" in {
    initSchema()
    insertHakemus()
    insertHakukohde(koulutuksenAlkamiskausiuri = None)
    insertHakutoive()

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    val hakija = getOnlyHakija(response)
    assert(hakija.hakemus.kausi.isEmpty)
  }

  it should "fall back to haku for vuosi/kausi when hakukohde values are null" in {
    initSchema()
    insertHenkilo()
    insertHaku(koulutuksenAlkamisvuosi = Some(2027), koulutuksenAlkamiskausiuri = Some("kausi_k#1"))
    insertHakemus(insertHenkilo = false, insertHaku = false)
    insertHakukohde(koulutuksenAlkamisvuosi = None, koulutuksenAlkamiskausiuri = None)
    insertHakutoive()

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    val hakija = getOnlyHakija(response)
    assert(hakija.hakemus.vuosi.contains("2027"))
    assert(hakija.hakemus.kausi.contains("K"))
  }

  it should "fall back to toteutus for vuosi/kausi when hakukohde and haku are null" in {
    initSchema()
    insertHakemus()
    insertHakukohde(koulutuksenAlkamisvuosi = None, koulutuksenAlkamiskausiuri = None)
    insertHakutoive()
    insertToteutusJaKoulutus(
      koulutuksenAlkamisvuosi = Some(2028),
      koulutuksenAlkamiskausiuri = Some("kausi_s#1")
    )

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    val hakija = getOnlyHakija(response)
    assert(hakija.hakemus.vuosi.contains("2028"))
    assert(hakija.hakemus.kausi.contains("S"))
  }

  it should "take both vuosi and kausi from haku when hakukohde has only vuosi (atomic rule)" in {
    initSchema()
    insertHenkilo()
    insertHaku(koulutuksenAlkamisvuosi = Some(2029), koulutuksenAlkamiskausiuri = Some("kausi_k#1"))
    insertHakemus(insertHenkilo = false, insertHaku = false)
    insertHakukohde(
      koulutuksenAlkamisvuosi = Some(2030),
      koulutuksenAlkamiskausiuri = None
    )
    insertHakutoive()

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    val hakija = getOnlyHakija(response)
    assert(
      hakija.hakemus.vuosi.contains("2029"),
      s"vuosi must come from haku (both non-null), not from hakukohde; got ${hakija.hakemus.vuosi}"
    )
    assert(
      hakija.hakemus.kausi.contains("K"),
      s"kausi must come from haku, got ${hakija.hakemus.kausi}"
    )
  }

  it should "return None for both vuosi and kausi when all three source tables have null values" in {
    initSchema()
    insertHakemus()
    insertHakukohde(koulutuksenAlkamisvuosi = None, koulutuksenAlkamiskausiuri = None)
    insertHakutoive()
    insertToteutusJaKoulutus()

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    val hakija = getOnlyHakija(response)
    assert(hakija.hakemus.vuosi.isEmpty)
    assert(hakija.hakemus.kausi.isEmpty)
  }

  it should "return None for kausi when koulutuksen_alkamiskausiuri is not a recognized value" in {
    initSchema()
    insertHakemus()
    insertHakukohde(koulutuksenAlkamiskausiuri = Some("kausi_v#1"))
    insertHakutoive()

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)

    val hakija = getOnlyHakija(response)
    assert(hakija.hakemus.kausi.isEmpty)
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

  it should "populate urheilijanLisakysymykset from urli for urheilijaLukio hakukohde" in {
    initSchema()
    insertHakemus()
    insertHakukohde(hakukohteenLinjaJson = Some(LINJA_URHEILIJA_LUKIO_JSON))
    insertHakutoive()
    insertHakemusToinenAsteYhteishaku()

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(
      hakutoive.urheilijanLisakysymykset.contains(lukioKysymykset),
      s"expected lukioKysymykset, got ${hakutoive.urheilijanLisakysymykset}"
    )
  }

  it should "populate urheilijanLisakysymykset from urli when linja has a #version suffix" in {
    initSchema()
    insertHakemus()
    insertHakukohde(hakukohteenLinjaJson = Some("""{"linja":"lukiolinjaterityinenkoulutustehtava_0105#1"}"""))
    insertHakutoive()
    insertHakemusToinenAsteYhteishaku()

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(
      hakutoive.urheilijanLisakysymykset.contains(lukioKysymykset),
      s"version-suffixed linja should still match; got ${hakutoive.urheilijanLisakysymykset}"
    )
  }

  it should "populate urheilijanLisakysymykset from uram when amm hakukohde and applicant opted in" in {
    initSchema()
    insertHakemus()
    insertHakukohde(jarjestaaUrheilijanAmmkoulutusta = Some(true))
    insertHakutoive()
    insertHakemusToinenAsteYhteishaku(kiinnostunutUrheilijanAmmatillisestaKoulutuksesta = Some(true))

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(
      hakutoive.urheilijanLisakysymykset.contains(ammatillisetKysymykset),
      s"expected ammatillisetKysymykset, got ${hakutoive.urheilijanLisakysymykset}"
    )
  }

  it should "leave urheilijanLisakysymykset None for amm hakukohde when applicant did not opt in" in {
    initSchema()
    insertHakemus()
    insertHakukohde(jarjestaaUrheilijanAmmkoulutusta = Some(true))
    insertHakutoive()
    insertHakemusToinenAsteYhteishaku(kiinnostunutUrheilijanAmmatillisestaKoulutuksesta = Some(false))

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(hakutoive.urheilijanLisakysymykset.isEmpty)
  }

  it should "pick urli when hakukohde is BOTH urheilijaLukio AND amm urheilija (lukio takes precedence)" in {
    initSchema()
    insertHakemus()
    insertHakukohde(
      hakukohteenLinjaJson = Some(LINJA_URHEILIJA_LUKIO_JSON),
      jarjestaaUrheilijanAmmkoulutusta = Some(true)
    )
    insertHakutoive()
    insertHakemusToinenAsteYhteishaku(kiinnostunutUrheilijanAmmatillisestaKoulutuksesta = Some(true))

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(
      hakutoive.urheilijanLisakysymykset.contains(lukioKysymykset),
      s"lukio should win precedence; got ${hakutoive.urheilijanLisakysymykset}"
    )
  }

  it should "leave urheilijanLisakysymykset None for a non-urheilija hakukohde" in {
    initSchema()
    insertHakemus()
    insertHakukohde() // defaults: linja=None, jarjestaa=false
    insertHakutoive()
    insertHakemusToinenAsteYhteishaku()

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(hakutoive.urheilijanLisakysymykset.isEmpty)
  }

  it should "set urheilijanammatillinenkoulutus true only when both flags are true" in {
    initSchema()
    insertHakemus()
    insertHakukohde(jarjestaaUrheilijanAmmkoulutusta = Some(true))
    insertHakutoive()
    insertHakemusToinenAsteYhteishaku(kiinnostunutUrheilijanAmmatillisestaKoulutuksesta = Some(true))

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(hakutoive.urheilijanammatillinenkoulutus.contains(true))
  }

  it should "set urheilijanammatillinenkoulutus false when hakukohde supports but applicant did not opt in" in {
    initSchema()
    insertHakemus()
    insertHakukohde(jarjestaaUrheilijanAmmkoulutusta = Some(true))
    insertHakutoive()
    insertHakemusToinenAsteYhteishaku(kiinnostunutUrheilijanAmmatillisestaKoulutuksesta = Some(false))

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(hakutoive.urheilijanammatillinenkoulutus.contains(false))
  }

  it should "leave urheilijanammatillinenkoulutus None when hakukohde flag is null" in {
    initSchema()
    insertHakemus()
    insertHakukohde(jarjestaaUrheilijanAmmkoulutusta = None)
    insertHakutoive()
    insertHakemusToinenAsteYhteishaku(kiinnostunutUrheilijanAmmatillisestaKoulutuksesta = Some(true))

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(hakutoive.urheilijanammatillinenkoulutus.isEmpty)
  }

  it should "leave urheilijan fields off when applicant opted in but hakukohde does not offer amm" in {
    initSchema()
    insertHakemus()
    insertHakukohde(jarjestaaUrheilijanAmmkoulutusta = Some(false))
    insertHakutoive()
    insertHakemusToinenAsteYhteishaku(kiinnostunutUrheilijanAmmatillisestaKoulutuksesta = Some(true))

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(
      hakutoive.urheilijanammatillinenkoulutus.contains(false),
      s"applicant interest alone must not flip the per-hakutoive flag; got ${hakutoive.urheilijanammatillinenkoulutus}"
    )
    assert(
      hakutoive.urheilijanLisakysymykset.isEmpty,
      s"applicant interest alone must not surface ammatilliset lisäkysymykset; got ${hakutoive.urheilijanLisakysymykset}"
    )
  }

  private def getOnlyHakija(response: Either[String, Seq[ToisenAsteenHakija]]): ToisenAsteenHakija = {
    assert(response.isRight, s"expected Right but got $response")
    assert(response.toOption.get.size == 1, s"expected single hakija but got ${response.toOption.get}")
    response.toOption.get.head
  }
}
