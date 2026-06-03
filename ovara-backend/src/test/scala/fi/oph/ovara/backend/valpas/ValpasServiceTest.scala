package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.domain.{En, Fi, Sv}
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.valpas.ValpasFactory.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import slick.jdbc.H2Profile.api.*

import java.time.{LocalDateTime, OffsetDateTime}

@SpringBootTest
@ActiveProfiles(Array("test"))
class ValpasServiceTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach with ValpasTestUtils {

  @Autowired
  override val db: ReadOnlyDatabase = null

  @Autowired
  private val service: ValpasService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  override def beforeEach(): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "Drop everything")
  }

  "getValpasTiedot" should "return error string when there's a db error" in {
    val response = service.getValpasTiedot(List("1.2.246.562.24.9"), false)

    assert(response.isLeft)
    assert(response.left.toOption.get == "virhe.tietokanta")
  }

  it should "return empty list when oppija has no hakemus" in {
    initSchema()
    insertHenkilo()

    val response = service.getValpasTiedot(List(OPPIJANUMERO), false)

    assert(response.isRight)
    assert(response.toOption.get.isEmpty)
  }

  it should "return hakemus with empty hakutoive list when oppija has hakemus but no hakutoive" in {
    initSchema()
    insertHakemus()

    val response = service.getValpasTiedot(List(OPPIJANUMERO), false)

    val hakemus = getOnlyHakemus(response)
    assert(hakemus.hakutoiveet.isEmpty)
    assert(hakemus.hakemusOid == HAKEMUS_OID)
    assert(hakemus.hakemuksenMuokkauksenAikaleima.contains(OffsetDateTime.parse(HAKEMUKSEN_MUOKKAUSAIKA)))
    assert(hakemus.email == EMAIL)
    assert(hakemus.matkapuhelin == MATKAPUHELIN)
    assert(hakemus.lahiosoite == LAHIOSOITE)
    assert(hakemus.postinumero == POSTINUMERO)
    assert(hakemus.postitoimipaikka == HELSINKI)
    assert(hakemus.maa.koodiarvo == SUOMI_KOODI)
    assert(hakemus.hakuOid == HAKU_OID)
    assert(hakemus.hakuNimi.get(Fi).contains("Yhteishaku"))
    assert(hakemus.hakuNimi.get(Sv).contains("Gemensamma"))
    assert(hakemus.hakuNimi.get(En).contains("Joint application"))
    assert(hakemus.hakutapa.versioituUri == "hakutapa_03#1")
    assert(hakemus.aktiivinenHaku.contains(true))
    assert(hakemus.haunAlkamispaivamaara.contains(LocalDateTime.parse("2022-08-31T23:59")))
    assert(hakemus.oppijaOid == OPPIJANUMERO)
  }

  it should "return hakemus only for the requested oppijanumerot" in {
    initSchema()
    insertHakemus()
    insertHakemus(idOffset = 1)

    val response = service.getValpasTiedot(List(OPPIJANUMERO, "1.2.246.562.24.331"), false)

    val hakemus = getOnlyHakemus(response)
    assert(hakemus.oppijaOid == OPPIJANUMERO)
  }

  it should "return hakemus for active haku when vainAktiiviset is true" in {
    initSchema()
    insertHakemus()

    val response = service.getValpasTiedot(List(OPPIJANUMERO, "1.2.246.562.24.331"), true)

    val hakemus = getOnlyHakemus(response)
    assert(hakemus.aktiivinenHaku.contains(true))
  }

  it should "not return hakemus for inactive haku when vainAktiiviset is true" in {
    initSchema()
    insertHakemus(aktiivinen = false)

    val response = service.getValpasTiedot(List(OPPIJANUMERO, "1.2.246.562.24.331"), true)

    assert(response.isRight)
    assert(response.toOption.get.isEmpty)
  }

  it should "return hakutoive when oppija has one" in {
    initSchema()
    insertHakemus()
    insertHakutoive()

    val response = service.getValpasTiedot(List(OPPIJANUMERO, "1.2.246.562.24.331"), true)

    val hakutoive = getOnlyHakutoive(response)
    assert(hakutoive.hakukohdeOid == HAKUKOHDE_OID)
    assert(hakutoive.hakukohdeNimi == createKielistetty())
    assert(hakutoive.hakutoivenumero == 3)
    assert(hakutoive.hakukohdeOrganisaatio == ORGANISAATIO_OID)
    assert(hakutoive.organisaatioNimi == ORGANISAATIO_NIMI)
    assert(hakutoive.koulutusOid == KOULUTUS_OID)
    assert(hakutoive.koulutusNimi == KOULUTUS_NIMI)
    assert(hakutoive.hakukohdeKoulutuskoodi.size == 1)
    assert(hakutoive.hakukohdeKoulutuskoodi.head.versioituUri == KOULUTUS_KOODIURI)
    assert(hakutoive.vastaanottotieto == "VASTAANOTTANUT_SITOVASTI")
    assert(hakutoive.valintatila == "HYVAKSYTTY")
    assert(hakutoive.ilmoittautumistila == "LASNA")
    assert(hakutoive.harkinnanvaraisuus == "EI_HARKINNANVARAINEN")
    assert(hakutoive.alinHyvaksyttyPistemaara.contains(21.1))
    assert(hakutoive.pisteet.contains(23.7))
    assert(hakutoive.varasijanumero.contains(4))
  }

  it should "return an error when there are results from more than one valintatapajono for a hakutoive" in {
    initSchema()
    insertHakemus()
    insertHakutoive()
    insertValinnanTulos(valintatapajonoId = "Toinen")

    val response = service.getValpasTiedot(List(OPPIJANUMERO, "1.2.246.562.24.331"), true)

    assert(response.isLeft)
    assert(response.left.toOption.get == "virhe.tietokanta")
  }

  private def getOnlyHakemus(response: Either[String, Seq[Hakemus]]): Hakemus = {
    assert(response.isRight)
    assert(response.toOption.get.size == 1)
    response.toOption.get.head
  }

  private def getOnlyHakutoive(response: Either[String, Seq[Hakemus]]): Hakutoive = {
    val hakemus = getOnlyHakemus(response)
    assert(hakemus.hakutoiveet.size == 1)
    hakemus.hakutoiveet.head
  }

}
