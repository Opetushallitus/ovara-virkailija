package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.external.toisenasteenhakijat.ExternalToisenAsteenHakijatTestData.*
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Käyttöoikeusrajaus viedään tällä rajapinnalla myös kyselyyn, ei pelkästään
 * `matchesFilters`-suodatukseen palvelukerroksessa. Koska kerrokset pudottavat samat rivit,
 * tulosjoukkoa tarkastava testi ei huomaisi jos kyselyn rajaus katoaisi -- siksi tässä
 * varmistetaan suoraan emitoitu SQL.
 *
 * `db` ei ole käytössä kyselyn rakentamisessa, joten null riittää: `selectHakijatQuery` ei aja
 * kyselyä vaan palauttaa sen.
 */
class ExternalToisenAsteenHakijatRepositorySpec extends AnyFlatSpec {

  private val repository = new ExternalToisenAsteenHakijatRepository(null)

  private val KAYTTOOIKEUS_SENTINEL = "1.2.246.562.10.11111111111111111111"
  private val PARAMETRI_SENTINEL    = "1.2.246.562.10.22222222222222222222"

  private def sqlFor(scope: KayttooikeusScope, organisaatioOids: Seq[String] = Seq.empty): String =
    repository
      .selectHakijatQuery(
        HAKU_OID,
        Some(HAKUKOHDE_OID),
        organisaatioOids,
        Valintarajaus.HAKENEET,
        scope
      )
      .statements
      .head

  "sallitutOrganisaatiotKayttooikeusFragment" should "not restrict jarjestyspaikka for paakayttaja" in {
    assert(repository.sallitutOrganisaatiotKayttooikeusFragment(KayttooikeusScope.paakayttaja) == "")
  }

  // Tyhjä oikeusjoukko: tyhjä IN () olisi syntaksivirhe, joten rajaus on ehto joka ei täsmää.
  it should "emit a never-matching condition when the scope has no organisaatiot" in {
    assert(
      repository.sallitutOrganisaatiotKayttooikeusFragment(KayttooikeusScope.limited(Set.empty)) == " AND FALSE"
    )
  }

  it should "restrict jarjestyspaikka to the scope's organisaatio" in {
    assert(
      repository.sallitutOrganisaatiotKayttooikeusFragment(KayttooikeusScope.limited(Set(ORGANISAATIO_OID)))
        == s" AND hk.jarjestyspaikka_oid IN ('$ORGANISAATIO_OID')"
    )
  }

  it should "restrict jarjestyspaikka to every organisaatio in the scope" in {
    val fragment =
      repository.sallitutOrganisaatiotKayttooikeusFragment(
        KayttooikeusScope.limited(Set(ORGANISAATIO_OID, ORGANISAATIO_OID_2))
      )

    // Set ei takaa järjestystä, joten kumpi tahansa järjestys kelpaa.
    assert(
      fragment == s" AND hk.jarjestyspaikka_oid IN ('$ORGANISAATIO_OID', '$ORGANISAATIO_OID_2')"
        || fragment == s" AND hk.jarjestyspaikka_oid IN ('$ORGANISAATIO_OID_2', '$ORGANISAATIO_OID')"
    )
  }

  "selectHakijatQuery" should "include the kayttooikeus rajaus as emitted by the fragment" in {
    val scope = KayttooikeusScope.limited(Set(KAYTTOOIKEUS_SENTINEL))

    // Verrataan fragmentin omaan tulokseen, jottei odotusarvo pääse eriytymään sen muodosta.
    assert(sqlFor(scope).contains(repository.sallitutOrganisaatiotKayttooikeusFragment(scope)))
  }

  it should "include the never-matching rajaus when the scope has no organisaatiot" in {
    val scope = KayttooikeusScope.limited(Set.empty)

    assert(sqlFor(scope).contains(repository.sallitutOrganisaatiotKayttooikeusFragment(scope)))
  }

  it should "restrict by the scope's organisaatio alongside the request's own organisaatio rajaus" in {
    val sql = sqlFor(KayttooikeusScope.limited(Set(KAYTTOOIKEUS_SENTINEL)), Seq(PARAMETRI_SENTINEL))

    assert(sql.contains(s"'$KAYTTOOIKEUS_SENTINEL'"), s"kayttooikeus rajaus missing from:\n$sql")
    assert(sql.contains(s"'$PARAMETRI_SENTINEL'"), s"parametrirajaus missing from:\n$sql")
  }

  // isPaakayttaja ohittaa oikeusjoukon: rajausta ei emitoida vaikka joukko olisi ei-tyhjä.
  it should "not restrict jarjestyspaikka for paakayttaja" in {
    val sql = sqlFor(KayttooikeusScope(isPaakayttaja = true, allowedOrgOids = Set(KAYTTOOIKEUS_SENTINEL)))

    assert(!sql.contains(KAYTTOOIKEUS_SENTINEL), s"expected no kayttooikeus rajaus, got:\n$sql")
  }

  /**
   * `db` on null, joten kannan koskettaminen näkyy NullPointerExceptionina: tyhjän oikeusjoukon
   * kohdalla tyhjä tulos todistaa ettei kyselyä ajettu, ja muissa tapauksissa NPE todistaa että
   * ajettiin.
   */
  private def hakijat(scope: KayttooikeusScope): Seq[HakijaRow] =
    repository.selectHakijat(HAKU_OID, Some(HAKUKOHDE_OID), Seq.empty, Valintarajaus.HAKENEET, scope)

  "selectHakijat" should "not run the query at all when the scope has no organisaatiot" in {
    assert(hakijat(KayttooikeusScope.limited(Set.empty)).isEmpty)
  }

  it should "run the query for a scope with organisaatioita" in {
    assertThrows[NullPointerException](hakijat(KayttooikeusScope.limited(Set(ORGANISAATIO_OID))))
  }

  it should "run the query for paakayttaja even though the oikeusjoukko is empty" in {
    assertThrows[NullPointerException](hakijat(KayttooikeusScope.paakayttaja))
  }
}
