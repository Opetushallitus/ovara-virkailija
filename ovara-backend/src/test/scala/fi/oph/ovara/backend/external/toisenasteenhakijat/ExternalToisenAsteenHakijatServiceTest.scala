package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.external.OrganisaatioHierarkiaStub
import fi.oph.ovara.backend.external.toisenasteenhakijat.ExternalToisenAsteenHakijatTestData.*
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.service.CommonService
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
class ExternalToisenAsteenHakijatServiceTest
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterEach
    with ExternalToisenAsteenHakijatTestUtils
    with OrganisaatioHierarkiaStub {

  @Autowired
  override val db: ReadOnlyDatabase = null

  @Autowired
  private val service: ExternalToisenAsteenHakijatService = null

  // Organisaatiohierarkia luetaan pub-skeemasta, jota näiden testien H2-kanta ei sisällä
  // (initSchema luo vain gen-taulut). Oletuksena laajennus palauttaa syötteen sellaisenaan,
  // jolloin testit kohdistuvat suodatuslogiikkaan; hierarkian laajentuminen testataan
  // erikseen `withOrganisaatioHierarkia`-apurilla.
  @MockitoBean
  override val commonService: CommonService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  override def beforeEach(): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "Drop everything")
    stubOrganisaatioHierarkiaAsIdentity()
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
    seedMinimalHakija()
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
    assert(hakutoive.valinta.contains(VALINTA_CODE))
    assert(hakutoive.vastaanotto.contains(VASTAANOTTO_CODE))
    assert(hakutoive.lasnaolo.contains(LASNAOLO_CODE))
  }

  it should "default vastaanotto to 1 (kesken) when null; valinta / lasnaolo stay empty" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(valintatieto = None, vastaanottotieto = None, ilmoittautumisenTila = None)

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(hakutoive.valinta.isEmpty)
    assert(hakutoive.vastaanotto.contains(VASTAANOTTO_KESKEN_CODE))
    assert(hakutoive.lasnaolo.isEmpty)
  }

  it should "leave terveys/aiempiperuminen/kaksoistutkinto None when hakukohdeOid is not in hakukohteet array" in {
    seedMinimalHakija()
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
    seedMinimalHakija()
    insertHakemusToinenAsteYhteishaku(hakukohteetJson = None)

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(hakutoive.terveys.isEmpty)
    assert(hakutoive.aiempiperuminen.isEmpty)
    assert(hakutoive.kaksoistutkinto.isEmpty)
  }

  it should "return hakija matched by organisaatioOid" in {
    seedMinimalHakija()

    val response = service.getHakijat(HAKU_OID, None, Some(ORGANISAATIO_OID))

    val hakija = getOnlyHakija(response)
    assert(hakija.oppijanumero == OPPIJANUMERO)
  }

  it should "return None / empty for fields that have no source yet" in {
    seedMinimalHakija()

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
    seedMinimalHakija()

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

  it should "HYVAKSYTYT includes hakija with HYVAKSYTTY" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(valintatieto = Some("HYVAKSYTTY"))

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None, Valintarajaus.HYVAKSYTYT)

    assert(response.toOption.get.size == 1)
  }

  it should "HYVAKSYTYT includes hakija with HARKINNANVARAISESTI_HYVAKSYTTY" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(valintatieto = Some("HARKINNANVARAISESTI_HYVAKSYTTY"))

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None, Valintarajaus.HYVAKSYTYT)

    assert(response.toOption.get.size == 1)
  }

  it should "HYVAKSYTYT includes hakija with VARASIJALTA_HYVAKSYTTY" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(valintatieto = Some("VARASIJALTA_HYVAKSYTTY"))

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None, Valintarajaus.HYVAKSYTYT)

    assert(response.toOption.get.size == 1)
  }

  it should "HYVAKSYTYT filters out hakija whose hakutoive is HYLATTY" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(valintatieto = Some("HYLATTY"))

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None, Valintarajaus.HYVAKSYTYT)

    assert(response.toOption.get.isEmpty)
  }

  it should "VASTAANOTTANEET includes hakija with VASTAANOTTANUT_SITOVASTI" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(vastaanottotieto = Some("VASTAANOTTANUT_SITOVASTI"))

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None, Valintarajaus.VASTAANOTTANEET)

    assert(response.toOption.get.size == 1)
  }

  it should "VASTAANOTTANEET filters out hakija with EHDOLLISESTI_VASTAANOTTANUT (strict binding only)" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(vastaanottotieto = Some("EHDOLLISESTI_VASTAANOTTANUT"))

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None, Valintarajaus.VASTAANOTTANEET)

    assert(response.toOption.get.isEmpty)
  }

  it should "HAKENEET returns hakija regardless of state" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(valintatieto = Some("HYLATTY"), vastaanottotieto = None)

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None, Valintarajaus.HAKENEET)

    assert(response.toOption.get.size == 1)
  }

  it should "return only the matching hakutoive when hakija has hakutoiveet to multiple hakukohteet" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, hakutoivenumero = 1)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2)

    val response    = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None, Valintarajaus.HAKENEET)
    val hakutoiveet = getOnlyHakija(response).hakemus.hakutoiveet

    assert(hakutoiveet.size == 1, s"expected only the matched hakutoive, got $hakutoiveet")
    assert(hakutoiveet.head.hakukohdeOid == HAKUKOHDE_OID)
  }

  it should "HYVAKSYTYT + hakukohdeOid returns only that hakukohde's hakutoive when accepted, drops others" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, hakutoivenumero = 1, valintatieto = Some("HYVAKSYTTY"))
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2, valintatieto = Some("HYVAKSYTTY"))

    val response    = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None, Valintarajaus.HYVAKSYTYT)
    val hakutoiveet = getOnlyHakija(response).hakemus.hakutoiveet

    assert(hakutoiveet.size == 1)
    assert(hakutoiveet.head.hakukohdeOid == HAKUKOHDE_OID)
  }

  it should "HYVAKSYTYT + hakukohdeOid drops hakija entirely when their matching hakutoive is HYLATTY" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, valintatieto = Some("HYLATTY"))

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None, Valintarajaus.HYVAKSYTYT)

    assert(response.toOption.get.isEmpty)
  }

  it should "paakayttaja scope returns all hakutoiveet" in {
    seedMinimalHakija()

    val response = service.getHakijat(
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

    val response = service.getHakijat(
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

    val response = service.getHakijat(
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

    val response = service.getHakijat(
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

    val response = service.getHakijat(HAKU_OID, None, Some(KOULUTUSTOIMIJA_OID))

    val hakija = getOnlyHakija(response)
    assert(hakija.oppijanumero == OPPIJANUMERO)
  }

  it should "not widen the organisaatioOid rajaus to organisaatiot outside the hierarkia" in {
    seedMinimalHakija()
    withOrganisaatioHierarkia(Map(KOULUTUSTOIMIJA_OID -> List(ORGANISAATIO_OID_2)))

    val response = service.getHakijat(HAKU_OID, None, Some(KOULUTUSTOIMIJA_OID))

    assert(response.toOption.get.isEmpty)
  }

  it should "limited scope on koulutustoimija level keeps a hakutoive järjestetty by a lapsiorganisaatio" in {
    seedMinimalHakija()
    withOrganisaatioHierarkia(Map(KOULUTUSTOIMIJA_OID -> List(ORGANISAATIO_OID)))

    val response = service.getHakijat(
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

    val response = service.getHakijat(
      HAKU_OID,
      Some(HAKUKOHDE_OID),
      None,
      Valintarajaus.HAKENEET,
      KayttooikeusScope.limited(Set(KOULUTUSTOIMIJA_OID))
    )

    assert(response.toOption.get.isEmpty)
  }

  it should "collapse HARKINNANVARAISESTI_HYVAKSYTTY and VARASIJALTA_HYVAKSYTTY to valinta code 1" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2)
    insertHakutoive(
      hakukohdeOid = HAKUKOHDE_OID,
      hakutoivenumero = 1,
      valintatieto = Some("HARKINNANVARAISESTI_HYVAKSYTTY")
    )
    insertHakutoive(
      hakukohdeOid = HAKUKOHDE_OID_2,
      hakutoivenumero = 2,
      valintatieto = Some("VARASIJALTA_HYVAKSYTTY")
    )

    val response = service.getHakijat(HAKU_OID, None, Some(ORGANISAATIO_OID))
    val hakija   = getOnlyHakija(response)

    assert(hakija.hakemus.hakutoiveet.forall(_.valinta.contains("1")))
  }

  it should "map PERUUTETTU on valintatieto to code 5" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(valintatieto = Some("PERUUTETTU"))

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(hakutoive.valinta.contains("5"))
  }

  it should "map unrecognised valintatieto value to None" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(valintatieto = Some("SOMETHING_ELSE"))

    val response  = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)
    val hakutoive = getOnlyHakija(response).hakemus.hakutoiveet.head

    assert(hakutoive.valinta.isEmpty)
  }

  it should "still exclude EHDOLLISESTI_VASTAANOTTANUT from VASTAANOTTANEET despite both mapping to code 3" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(vastaanottotieto = Some("EHDOLLISESTI_VASTAANOTTANUT"))

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None, Valintarajaus.VASTAANOTTANEET)

    assert(response.toOption.get.isEmpty)
  }

  it should "map every valintatieto DB enum value to its numeric code" in {
    val cases = Seq(
      "HYVAKSYTTY"                     -> Some("1"),
      "HARKINNANVARAISESTI_HYVAKSYTTY" -> Some("1"),
      "VARASIJALTA_HYVAKSYTTY"         -> Some("1"),
      "VARALLA"                        -> Some("2"),
      "HYLATTY"                        -> Some("3"),
      "PERUNUT"                        -> Some("4"),
      "PERUUNTUNUT"                    -> Some("4"),
      "PERUUTETTU"                     -> Some("5"),
      "TOTALLY_UNKNOWN"                -> None
    )
    cases.foreach { case (raw, expected) =>
      resetDb()
      insertHakemus()
      insertHakukohde()
      insertHakutoive(valintatieto = Some(raw))
      val hakutoive = getOnlyHakija(service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)).hakemus.hakutoiveet.head
      assert(hakutoive.valinta == expected, s"$raw should map to $expected but got ${hakutoive.valinta}")
    }
  }

  it should "map every vastaanottotieto DB enum value to its numeric code" in {
    val cases = Seq(
      "VASTAANOTTANUT_SITOVASTI"      -> Some("3"),
      "EHDOLLISESTI_VASTAANOTTANUT"   -> Some("3"),
      "PERUNUT"                       -> Some("4"),
      "EI_VASTAANOTETTU_MAARA_AIKANA" -> Some("5"),
      "PERUUTETTU"                    -> Some("6"),
      "TOTALLY_UNKNOWN"               -> None
    )
    cases.foreach { case (raw, expected) =>
      resetDb()
      insertHakemus()
      insertHakukohde()
      insertHakutoive(vastaanottotieto = Some(raw))
      val hakutoive = getOnlyHakija(service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)).hakemus.hakutoiveet.head
      assert(hakutoive.vastaanotto == expected, s"$raw should map to $expected but got ${hakutoive.vastaanotto}")
    }
  }

  it should "map every ilmoittautumisen_tila DB enum value to its numeric code" in {
    val cases = Seq(
      "EI_TEHTY"              -> Some("1"),
      "LASNA_KOKO_LUKUVUOSI"  -> Some("2"),
      "POISSA_KOKO_LUKUVUOSI" -> Some("3"),
      "EI_ILMOITTAUTUNUT"     -> Some("4"),
      "LASNA_SYKSY"           -> Some("5"),
      "POISSA_SYKSY"          -> Some("6"),
      "LASNA"                 -> Some("7"),
      "POISSA"                -> Some("8"),
      "TOTALLY_UNKNOWN"       -> None
    )
    cases.foreach { case (raw, expected) =>
      resetDb()
      insertHakemus()
      insertHakukohde()
      insertHakutoive(ilmoittautumisenTila = Some(raw))
      val hakutoive = getOnlyHakija(service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None)).hakemus.hakutoiveet.head
      assert(hakutoive.lasnaolo == expected, s"$raw should map to $expected but got ${hakutoive.lasnaolo}")
    }
  }

  it should "HYVAKSYTYT excludes hakija when valintatieto is NULL" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(valintatieto = None)

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None, Valintarajaus.HYVAKSYTYT)

    assert(response.toOption.get.isEmpty)
  }

  it should "VASTAANOTTANEET excludes hakija when vastaanottotieto is NULL" in {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive(vastaanottotieto = None)

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None, Valintarajaus.VASTAANOTTANEET)

    assert(response.toOption.get.isEmpty)
  }

  it should "return only the org-matching hakutoive when hakija has hakutoiveet at multiple orgs" in {
    initSchema()
    insertHakemus()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID, jarjestyspaikkaOid = ORGANISAATIO_OID)
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2, jarjestyspaikkaOid = ORGANISAATIO_OID_2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, hakutoivenumero = 1)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2)

    val response    = service.getHakijat(HAKU_OID, None, Some(ORGANISAATIO_OID))
    val hakutoiveet = getOnlyHakija(response).hakemus.hakutoiveet

    assert(hakutoiveet.size == 1, s"expected only org-matched hakutoive, got $hakutoiveet")
    assert(hakutoiveet.head.opetuspiste.contains(ORGANISAATIO_OID))
  }

  it should "HYVAKSYTYT + organisaatioOid returns only the org+state matching hakutoive" in {
    initSchema()
    insertHakemus()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID, jarjestyspaikkaOid = ORGANISAATIO_OID)
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2, jarjestyspaikkaOid = ORGANISAATIO_OID_2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, hakutoivenumero = 1, valintatieto = Some("HYVAKSYTTY"))
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2, valintatieto = Some("HYVAKSYTTY"))

    val response    = service.getHakijat(HAKU_OID, None, Some(ORGANISAATIO_OID), Valintarajaus.HYVAKSYTYT)
    val hakutoiveet = getOnlyHakija(response).hakemus.hakutoiveet

    assert(hakutoiveet.size == 1)
    assert(hakutoiveet.head.opetuspiste.contains(ORGANISAATIO_OID))
  }

  it should "HYVAKSYTYT + organisaatioOid drops hakija when only the other-org hakutoive is HYVAKSYTTY" in {
    initSchema()
    insertHakemus()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID, jarjestyspaikkaOid = ORGANISAATIO_OID)
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2, jarjestyspaikkaOid = ORGANISAATIO_OID_2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, hakutoivenumero = 1, valintatieto = Some("HYLATTY"))
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2, valintatieto = Some("HYVAKSYTTY"))

    val response = service.getHakijat(HAKU_OID, None, Some(ORGANISAATIO_OID), Valintarajaus.HYVAKSYTYT)

    assert(response.toOption.get.isEmpty)
  }

  // ---- vuosi/kausi across MULTIPLE hakutoiveet ----
  // Every other vuosi/kausi test above uses a single hakutoive, so none of them pin down which
  // hakutoive the value is taken from. These do.

  it should "take vuosi/kausi from the hakutoive with the lowest hakutoivenumero" in {
    initSchema()
    insertHakemus()
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID,
      koulutuksenAlkamisvuosi = Some(2030),
      koulutuksenAlkamiskausiuri = Some("kausi_k#1")
    )
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID_2,
      koulutuksenAlkamisvuosi = Some(2031),
      koulutuksenAlkamiskausiuri = Some("kausi_s#1")
    )
    // Insert the higher hakutoivenumero first so insertion order cannot be what makes this pass.
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, hakutoivenumero = 1)

    val hakija = getOnlyHakija(service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None))

    assert(hakija.hakemus.vuosi.contains("2030"))
    assert(hakija.hakemus.kausi.contains("K"))
  }

  it should "skip a hakutoive whose hakukohde row is missing when picking vuosi/kausi" in {
    initSchema()
    insertHakemus()
    // hakutoive 1 points at a hakukohde that has no gen_hakukohde row at all.
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 1)
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID,
      koulutuksenAlkamisvuosi = Some(2032),
      koulutuksenAlkamiskausiuri = Some("kausi_s#1")
    )
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, hakutoivenumero = 2)

    val hakija = getOnlyHakija(service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None))

    assert(hakija.hakemus.vuosi.contains("2032"), "should fall through to hakutoive 2's hakukohde")
    assert(hakija.hakemus.kausi.contains("S"))
  }

  it should "take the toteutus fallback from a different hakutoive than the hakukohde candidate" in {
    initSchema()
    insertHakemus()
    // hakutoive 1: hakukohde exists but has no vuosi/kausi, and its toteutus row is missing.
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID,
      toteutusOid = "1.2.246.562.17.00000000000000000999",
      koulutuksenAlkamisvuosi = None,
      koulutuksenAlkamiskausiuri = None
    )
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, hakutoivenumero = 1)
    // hakutoive 2: hakukohde also has no vuosi/kausi, but its toteutus does.
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID_2,
      toteutusOid = TOTEUTUS_OID,
      koulutuksenAlkamisvuosi = None,
      koulutuksenAlkamiskausiuri = None
    )
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 2)
    insertToteutusJaKoulutus(
      koulutuksenAlkamisvuosi = Some(2033),
      koulutuksenAlkamiskausiuri = Some("kausi_k#1")
    )

    val hakija = getOnlyHakija(service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None))

    assert(hakija.hakemus.vuosi.contains("2033"), "toteutus candidate comes from hakutoive 2")
    assert(hakija.hakemus.kausi.contains("K"))
  }

  it should "take vuosi/kausi from hakutoive 1 even when the queried hakukohde is hakutoive 2" in {
    initSchema()
    insertHakemus()
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID_2,
      koulutuksenAlkamisvuosi = Some(2034),
      koulutuksenAlkamiskausiuri = Some("kausi_k#1")
    )
    insertHakukohde(
      hakukohdeOid = HAKUKOHDE_OID,
      koulutuksenAlkamisvuosi = Some(2035),
      koulutuksenAlkamiskausiuri = Some("kausi_s#1")
    )
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID_2, hakutoivenumero = 1)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID, hakutoivenumero = 2)

    // Querying hakukohde 2 must not change which hakutoive vuosi/kausi is derived from:
    // the value belongs to the hakemus, not to the filtered hakutoive.
    val hakija = getOnlyHakija(service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None))

    assert(hakija.hakemus.hakutoiveet.size == 1, "only the queried hakutoive is returned")
    assert(hakija.hakemus.hakutoiveet.head.hakukohdeOid == HAKUKOHDE_OID)
    assert(hakija.hakemus.vuosi.contains("2034"), "vuosi still comes from hakutoive 1")
    assert(hakija.hakemus.kausi.contains("K"))
  }

  // ---- structural guards for the narrowing query ----

  it should "return one hakija per matching hakemus when a henkilo has two hakemus in the same haku" in {
    initSchema()
    insertHakemus(hakemusOid = HAKEMUS_OID)
    insertHakemus(hakemusOid = HAKEMUS_OID_2, insertHenkilo = false, insertHaku = false)
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID)
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID_2)
    // Only HAKEMUS_OID has a hakutoive to the queried hakukohde.
    insertHakutoive(hakemusOid = HAKEMUS_OID, hakukohdeOid = HAKUKOHDE_OID)
    insertHakutoive(hakemusOid = HAKEMUS_OID_2, hakukohdeOid = HAKUKOHDE_OID_2)
    insertPohjakoulutus(hakemusOid = HAKEMUS_OID, arvo = Some("\"1\""))
    insertPohjakoulutus(hakemusOid = HAKEMUS_OID_2, arvo = Some("\"9\""))

    val hakija = getOnlyHakija(service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None))

    assert(hakija.hakemus.hakemusnumero == HAKEMUS_OID)
    assert(hakija.hakemus.pohjakoulutus.contains("1"), "must not pick up the other hakemus's supa row")
  }

  // Positional-integrity guard. Both GetResult extractors read strictly by position, so a mismatch
  // between a SELECT list and its extractor is a silent column shift rather than a compile error --
  // and NULLs are what make it silent, since nextStringOption() on the wrong column just yields None.
  // Every column below is non-NULL and distinctly typed so a shift surfaces as a type error instead.
  // The tail columns of selectHakutoiveet (harkinnanvaraisuus, pisteet, keskiarvo) matter most:
  // they are what an off-by-one in that query's SELECT list corrupts first.
  it should "populate every field of a fully-populated hakija (column-order guard)" in {
    initSchema()
    insertHenkilo()
    insertHaku(koulutuksenAlkamisvuosi = Some(2040), koulutuksenAlkamiskausiuri = Some("kausi_k#1"))
    insertHakemus(insertHenkilo = false, insertHaku = false)
    insertHakukohde()
    insertHakutoive(harkinnanvaraisuudenSyy = Some("ATARU_OPPIMISVAIKEUDET"))
    insertToteutusJaKoulutus(
      koulutuksenAlkamisvuosi = Some(2041),
      koulutuksenAlkamiskausiuri = Some("kausi_s#1")
    )
    insertOrganisaatio()
    insertHakemusToinenAsteYhteishaku()
    insertOpetuskieli()
    insertPohjakoulutus(arvo = Some("\"1\""))
    insertTodistusvuosi(arvo = Some("\"2025\""))
    insertLisakoulutus(avain = "LISAKOULUTUS_TUVA")
    insertValintarekisteri(pisteet = Some(BigDecimal("7.75")))
    insertValintalaskentaFunktiotulos(tunniste = "keskiarvo_pk", arvo = Some("8.25"))

    val hakija = getOnlyHakija(service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), None))

    // gen_hakemus / gen_henkilo block
    assert(hakija.oppijanumero == OPPIJANUMERO)
    assert(hakija.aidinkieli.contains(AIDINKIELI))
    assert(hakija.huoltaja1.contains(HUOLTAJA1))
    assert(hakija.huoltaja2.contains(HUOLTAJA2))
    // gen_supa_tieto block
    assert(hakija.opetuskieli.contains(OPETUSKIELI))
    assert(hakija.hakemus.pohjakoulutus.contains("1"))
    assert(hakija.hakemus.todistusvuosi.contains("2025"))
    assert(hakija.hakemus.lisapistekoulutus.contains("LISAKOULUTUS_TUVA"))
    // vuosi/kausi: hakukohde wins over both haku (2040) and toteutus (2041)
    assert(hakija.hakemus.vuosi.contains(VUOSI))
    assert(hakija.hakemus.kausi.contains(KAUSI))
    // tail of selectHakutoiveet -- the columns an off-by-one corrupts first
    val hakutoive = hakija.hakemus.hakutoiveet.head
    assert(hakutoive.harkinnanvaraisuusperuste.contains("1"))
    assert(hakutoive.yhteispisteet.contains(BigDecimal("7.75")))
    assert(hakutoive.keskiarvo.contains("8.25"))
    assert(hakutoive.lasnaolo.contains(LASNAOLO_CODE))
    assert(hakutoive.valinta.contains(VALINTA_CODE))
    assert(hakutoive.vastaanotto.contains(VASTAANOTTO_CODE))
  }

  it should "return empty when hakukohdeOid and organisaatioOid point at different organisaatiot" in {
    initSchema()
    insertHakemus()
    insertHakukohde(hakukohdeOid = HAKUKOHDE_OID, jarjestyspaikkaOid = ORGANISAATIO_OID)
    insertHakutoive(hakukohdeOid = HAKUKOHDE_OID)

    val response = service.getHakijat(HAKU_OID, Some(HAKUKOHDE_OID), Some(ORGANISAATIO_OID_2))

    assert(response.toOption.get.isEmpty)
  }

  private def resetDb(): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "Drop everything")
    initSchema()
  }

  private def getOnlyHakija(response: Either[String, Seq[ToisenAsteenHakija]]): ToisenAsteenHakija = {
    assert(response.isRight, s"expected Right but got $response")
    assert(response.toOption.get.size == 1, s"expected single hakija but got ${response.toOption.get}")
    response.toOption.get.head
  }
}
