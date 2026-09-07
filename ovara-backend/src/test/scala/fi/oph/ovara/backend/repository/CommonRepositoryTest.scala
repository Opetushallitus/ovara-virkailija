package fi.oph.ovara.backend.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.{BeforeEach, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import slick.jdbc.H2Profile.api.*

/**
 * selectHakukohderyhmat ajetaan oikeaa kantaa vasten, jotta testi kertoo mitä ryhmiä käyttäjä
 * tosiasiassa saa. Oikeudet tulevat metodin parametreina, joten niiden vaihteluun
 * ei tarvita käyttäjä- tai authority-koneistoa.
 *
 * Huom: tämä ei kata oikeuslistojen johtamista käyttäjän authorityistä.
 */
@SpringBootTest
@ActiveProfiles(Array("test"))
class CommonRepositoryTest {

  @Autowired
  private val db: ReadOnlyDatabase = null

  @Autowired
  private val commonRepository: CommonRepository = null

  private val HAKU_OID       = "1.2.246.562.29.00000000000000021303"
  private val TOINEN_HAKU    = "1.2.246.562.29.00000000000000021304"
  private val RYHMA_A        = "1.2.246.562.28.00000000000000000001"
  private val RYHMA_B        = "1.2.246.562.28.00000000000000000002"
  private val RYHMA_TOISESSA = "1.2.246.562.28.00000000000000000003"
  private val ORG_A          = "1.2.246.562.10.00000000000000000486"
  private val ORG_B          = "1.2.246.562.10.00000000000000000487"

  @BeforeEach
  def initSchema(): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "Drop everything")
    db.run(
      sqlu"""
          CREATE SCHEMA pub;

          CREATE TABLE pub.pub_dim_hakukohderyhma (
              hakukohderyhma_oid  text NOT NULL PRIMARY KEY,
              hakukohderyhma_nimi text
          );

          CREATE TABLE pub.pub_dim_hakukohderyhma_ja_hakukohteet (
              hakukohderyhma_oid text NOT NULL,
              hakukohde_oid      text NOT NULL,
              haku_oid           text
          );

          CREATE TABLE pub.pub_dim_hakukohde (
              hakukohde_oid       text NOT NULL PRIMARY KEY,
              tila                text,
              jarjestyspaikka_oid text
          );
          """,
      "Create pub schema"
    )

    // RyhmäA:n hakukohde on orgA:ssa, ryhmäB:n orgB:ssä -- eri järjestyspaikat, jotta
    // organisaatio-oikeus erottelee ne. RYHMA_TOISESSA on eri haussa.
    insertRyhma(RYHMA_A, "hk-a", ORG_A, HAKU_OID)
    insertRyhma(RYHMA_B, "hk-b", ORG_B, HAKU_OID)
    insertRyhma(RYHMA_TOISESSA, "hk-c", ORG_A, TOINEN_HAKU)
  }

  private def insertRyhma(ryhmaOid: String, hakukohdeOid: String, jarjestyspaikka: String, hakuOid: String): Unit = {
    db.run(
      sqlu"""INSERT INTO pub.pub_dim_hakukohderyhma VALUES ($ryhmaOid, '{"fi":"Ryhmä"}')""",
      "Insert hakukohderyhma"
    )
    db.run(
      sqlu"""INSERT INTO pub.pub_dim_hakukohde VALUES ($hakukohdeOid, 'julkaistu', $jarjestyspaikka)""",
      "Insert hakukohde"
    )
    db.run(
      sqlu"""INSERT INTO pub.pub_dim_hakukohderyhma_ja_hakukohteet VALUES ($ryhmaOid, $hakukohdeOid, $hakuOid)""",
      "Insert hakukohderyhma_ja_hakukohteet"
    )
  }

  private def hakukohderyhmaOids(
    kayttooikeusOrgOids: List[String],
    kayttooikeusHakukohderyhmaOids: List[String],
    isOphPaakayttaja: Boolean,
    haut: List[String] = List(HAKU_OID)
  ): Set[String] =
    db.run(
      commonRepository.selectHakukohderyhmat(
        kayttooikeusOrgOids,
        kayttooikeusHakukohderyhmaOids,
        haut,
        isOphPaakayttaja
      ),
      "selectHakukohderyhmat"
    ).map(_.hakukohderyhma_oid)
      .toSet

  @Test
  def paakayttajaSeesEveryHakukohderyhma(): Unit =
    assertEquals(
      Set(RYHMA_A, RYHMA_B),
      hakukohderyhmaOids(List.empty, List.empty, isOphPaakayttaja = true)
    )

  @Test
  def organisaatioOikeusLimitsToRyhmatOfOwnJarjestyspaikka(): Unit =
    assertEquals(
      Set(RYHMA_A),
      hakukohderyhmaOids(List(ORG_A), List.empty, isOphPaakayttaja = false)
    )

  @Test
  def hakukohderyhmaOikeusLimitsToThatRyhma(): Unit =
    assertEquals(
      Set(RYHMA_B),
      hakukohderyhmaOids(List.empty, List(RYHMA_B), isOphPaakayttaja = false)
    )

  // Oikeuslajit yhdistetään OR:lla, ei AND:lla.
  @Test
  def organisaatioAndHakukohderyhmaOikeudetAreUnioned(): Unit =
    assertEquals(
      Set(RYHMA_A, RYHMA_B),
      hakukohderyhmaOids(List(ORG_A), List(RYHMA_B), isOphPaakayttaja = false)
    )

  // Regressiovahti: ilman rajausta kysely palauttaisi kaikki ryhmät.
  @Test
  def userWithoutAnyOikeudetSeesNoRyhmat(): Unit =
    assertEquals(
      Set.empty,
      hakukohderyhmaOids(List.empty, List.empty, isOphPaakayttaja = false)
    )

  @Test
  def hautParameterLimitsToRyhmatInGivenHaku(): Unit =
    assertEquals(
      Set(RYHMA_TOISESSA),
      hakukohderyhmaOids(List.empty, List.empty, isOphPaakayttaja = true, haut = List(TOINEN_HAKU))
    )
}
