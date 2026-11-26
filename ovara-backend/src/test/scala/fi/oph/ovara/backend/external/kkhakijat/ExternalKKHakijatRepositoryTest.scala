package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.external.kkhakijat.ExternalKKHakijatTestData.*
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import slick.jdbc.PostgresProfile.api.*

import java.time.{OffsetDateTime, ZoneOffset}

@SpringBootTest
@ActiveProfiles(Array("test"))
class ExternalKKHakijatRepositoryTest
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterEach
    with ExternalKKHakijatTestUtils {

  @Autowired
  override val db: ReadOnlyDatabase = null

  @Autowired
  private val repository: ExternalKKHakijatRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  override def beforeEach(): Unit =
    db.run(sqlu"""DROP SCHEMA IF EXISTS gen, pub CASCADE""", "Drop everything")

  private val hakukohteenAlkamisaika = OffsetDateTime.parse("2027-01-10T00:00:00+02:00")
  private val haunAlkamisaika        = OffsetDateTime.parse("2028-09-01T00:00:00+03:00")
  private val toteutuksenAlkamisaika = OffsetDateTime.parse("2029-02-01T00:00:00+02:00")

  // Kanta palauttaa aikaleimat istunnon aikavyöhykkeellä, joten ne verrataan UTC:ssä samana hetkenä.
  private def utc(rivi: YpsHakukohdeRow): YpsHakukohdeRow = {
    def aika(a: AlkamiskausiRow) = a.copy(alkamisaika = a.alkamisaika.map(_.withOffsetSameInstant(ZoneOffset.UTC)))
    rivi.copy(
      hakukohteenAlkamiskausi = aika(rivi.hakukohteenAlkamiskausi),
      haunAlkamiskausi = aika(rivi.haunAlkamiskausi),
      toteutuksenAlkamiskausi = aika(rivi.toteutuksenAlkamiskausi)
    )
  }

  // Jokaisella YPS-sarakkeella on oma arvonsa, joten kahden sarakkeen järjestyksen vaihtuminen kummassa tahansa
  // kyselyssä tai getYpsHakukohdessa muuttaa tulosta. Toteutuksen kausi puuttuu, koska kausia on vain kaksi.
  "selectHakemukset and selectYpsVastaanotot" should "read the YPS columns in the same order" in {
    initSchema()
    insertHakemus(insertHaku = false)
    insertHaku(
      tila = Some("arkistoitu"),
      kohdejoukonTarkenneKoodiuri = Some("haunkohdejoukontarkenne_3#1"),
      koulutuksenAlkamiskausiuri = Some("kausi_s#1"),
      koulutuksenAlkamisvuosi = Some(2028),
      koulutuksenTarkkaAlkamisaika = Some(haunAlkamisaika)
    )
    insertHakukohde(
      tila = Some("julkaistu"),
      koulutuksenAlkamiskausiuri = Some("kausi_k#1"),
      koulutuksenAlkamisvuosi = Some(2027),
      koulutuksenAlkamispaivamaara = Some(hakukohteenAlkamisaika)
    )
    insertToteutus(koulutuksenAlkamisvuosi = Some(2029), koulutuksenTarkkaAlkamisaika = Some(toteutuksenAlkamisaika))
    insertKoulutus(johtaaTutkintoon = Some(true), koulutustyyppi = Some("yo"))
    insertHakutoive(vastaanottotieto = Some("VASTAANOTTANUT_SITOVASTI"))

    val odotettu = YpsHakukohdeRow(
      hakuTila = Some("arkistoitu"),
      hakukohdeTila = Some("julkaistu"),
      kohdejoukonTarkenne = Some("haunkohdejoukontarkenne_3#1"),
      koulutustyyppi = Some("yo"),
      johtaaTutkintoon = Some(true),
      hakukohteenAlkamiskausi = AlkamiskausiRow(Some("K"), Some(2027), Some(hakukohteenAlkamisaika)),
      haunAlkamiskausi = AlkamiskausiRow(Some("S"), Some(2028), Some(haunAlkamisaika)),
      toteutuksenAlkamiskausi = AlkamiskausiRow(None, Some(2029), Some(toteutuksenAlkamisaika))
    )

    repository.selectHakemukset(Set(HAKEMUS_OID)).map(r => utc(r.yps)) shouldBe Seq(utc(odotettu))
    repository.selectYpsVastaanotot(Set(HAKEMUS_OID)).map(r => utc(r.yps)) shouldBe Seq(utc(odotettu))
  }
}
