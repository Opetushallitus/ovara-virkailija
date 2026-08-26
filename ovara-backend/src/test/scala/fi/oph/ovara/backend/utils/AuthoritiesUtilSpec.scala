package fi.oph.ovara.backend.utils

import org.scalatest.*
import org.scalatest.flatspec.*
import org.springframework.security.core
import org.springframework.security.core.authority.SimpleGrantedAuthority

import scala.jdk.CollectionConverters.*

class AuthoritiesUtilSpec extends AnyFlatSpec {
  "getRaportointiAuthorities" should "return empty list if no role in all authorities" in {
    val allAuthorities: java.util.Collection[SimpleGrantedAuthority] = List().asJava
    assert(AuthoritiesUtil.getOvaraAuthorities(allAuthorities).isEmpty)
  }

  it should "return empty list when there is one role without the ROLE_APP_OVARA-VIRKAILIJA prefix" in {
    val allAuthorities: java.util.Collection[SimpleGrantedAuthority] =
      List(SimpleGrantedAuthority("ROLE_APP_SUORITUSREKISTERI_CRUD_1.2.246.562.10.00000000001")).asJava
    assert(AuthoritiesUtil.getOvaraAuthorities(allAuthorities).isEmpty)
  }

  it should "return only authorities with ROLE_APP_OVARA-VIRKAILIJA prefix" in {
    val allAuthorities: java.util.Collection[SimpleGrantedAuthority] = List(
      SimpleGrantedAuthority("ROLE_APP_SUORITUSREKISTERI_CRUD_1.2.246.562.10.00000000001"),
      SimpleGrantedAuthority("ROLE_APP_OVARA-VIRKAILIJA_KK_1.2.246.562.10.00000000001"),
      SimpleGrantedAuthority("ROLE_APP_OVARA-VIRKAILIJA"),
      SimpleGrantedAuthority("ROLE_APP_KOUTA"),
      SimpleGrantedAuthority("ROLE_APP_OVARA-VIRKAILIJA_KK")
    ).asJava
    assert(
      AuthoritiesUtil.getOvaraAuthorities(allAuthorities) == List(
        "ROLE_APP_OVARA-VIRKAILIJA_KK_1.2.246.562.10.00000000001",
        "ROLE_APP_OVARA-VIRKAILIJA",
        "ROLE_APP_OVARA-VIRKAILIJA_KK"
      )
    )
  }

  "getRaportointiOrganisaatiot" should "return one organisaatio for user" in {
    val allAuthorities = List(
      "ROLE_APP_SUORITUSREKISTERI_CRUD_1.2.246.562.10.654321",
      "ROLE_APP_RAPORTOINTI_1.2.246.562.10.654321",
      "ROLE_APP_OVARA-VIRKAILIJA_KK_1.2.246.562.10.654321",
      "ROLE_APP_RAPORTOINTI_OPO",
      "ROLE_APP_KOUTA",
      "ROLE_APP_RAPORTOINTI_KK"
    )
    assert(AuthoritiesUtil.getKayttooikeusOids(allAuthorities) == List("1.2.246.562.10.654321"))
  }

  it should "return all organisaatiot the user has rights for" in {
    val allAuthorities = List(
      "ROLE_APP_SUORITUSREKISTERI_CRUD_1.2.246.562.10.654321",
      "ROLE_APP_OVARA-VIRKAILIJA_KK_1.2.246.562.10.789101112",
      "ROLE_APP_RAPORTOINTI_OPO",
      "ROLE_APP_KOUTA",
      "ROLE_APP_RAPORTOINTI_1.2.246.562.10.654321",
      "ROLE_APP_RAPORTOINTI_KK",
      "ROLE_APP_RAPORTOINTI_1.2.246.562.10.333334445"
    )
    assert(
      AuthoritiesUtil.getKayttooikeusOids(allAuthorities) == List(
        "1.2.246.562.10.654321",
        "1.2.246.562.10.789101112",
        "1.2.246.562.10.333334445"
      )
    )
  }

  it should "return a hakukohderyhma oid as is" in {
    val allAuthorities = List("ROLE_APP_OVARA-VIRKAILIJA_KK_HAKENEET_1.2.246.562.28.00000000000000000012")
    assert(
      AuthoritiesUtil.getKayttooikeusOids(allAuthorities) == List("1.2.246.562.28.00000000000000000012")
    )
  }

  // Organisaatio, hakukohderyhmä, ja lopuksi joukko oideja jotka eivät ole kumpaakaan:
  // hakukohde, henkilö ja roolista "..._KK_HAKENEET_123" irronnut rykelmä numeroita.
  // Huom: metodi saa pelkän oid-listan eikä näe millä roolilla oid on myönnetty. Siksi
  // kutsuja päättää mitkä oidit se syöttää -- external-rajapinnat syöttävät vain oman
  // oikeusperheensä oidit, sisäiset raportit kaikki.
  "hasOPHPaakayttajaRights" should "return true when the OPH organisaatio oid is present" in {
    assert(
      AuthoritiesUtil.hasOPHPaakayttajaRights(
        List("1.2.246.562.10.00000000000000000586", "1.2.246.562.10.00000000001")
      )
    )
  }

  it should "return false for other organisaatio oids" in {
    assert(!AuthoritiesUtil.hasOPHPaakayttajaRights(List("1.2.246.562.10.00000000000000000586")))
  }

  it should "return false for an empty list" in {
    assert(!AuthoritiesUtil.hasOPHPaakayttajaRights(List()))
  }

  private val mixedOids = List(
    "1.2.246.562.10.00000000001",
    "1.2.246.562.28.00000000000000000012",
    "1.2.246.562.10.00000000000000000586",
    "1.2.246.562.28.00000000000000000013",
    "1.2.246.562.20.00000000000000000112",
    "1.2.246.562.24.00000000019",
    "123"
  )

  "filterHakukohderyhmaOids" should "return only hakukohderyhma oids" in {
    assert(
      AuthoritiesUtil.filterHakukohderyhmaOids(mixedOids) == List(
        "1.2.246.562.28.00000000000000000012",
        "1.2.246.562.28.00000000000000000013"
      )
    )
  }

  "filterOrganisaatioOids" should "return only organisaatio oids" in {
    assert(
      AuthoritiesUtil.filterOrganisaatioOids(mixedOids) == List(
        "1.2.246.562.10.00000000001",
        "1.2.246.562.10.00000000000000000586"
      )
    )
  }

  // Pattern on sama jolla organisaatioOid-parametri validoidaan. Jos sitä kiristetään,
  // tämä testi kertoo että myös käyttöoikeuksien tulkinta muuttuu.
  it should "accept the same organisaatio oid namespaces as validateOrganisaatioOid" in {
    val kaikkiAvaruudet = List(
      "1.2.246.562.10.00000000001",
      "1.2.246.562.99.00000000000000000001",
      "1.2.246.562.199.00000000000000000001",
      "1.2.246.562.299.00000000000000000001"
    )
    assert(AuthoritiesUtil.filterOrganisaatioOids(kaikkiAvaruudet) == kaikkiAvaruudet)
    assert(kaikkiAvaruudet.forall(oid => ParameterValidator.validateOrganisaatioOid(Some(oid), "f").isEmpty))
  }

  it should "be disjoint from filterHakukohderyhmaOids, without necessarily covering the input" in {
    val organisaatiot   = AuthoritiesUtil.filterOrganisaatioOids(mixedOids)
    val hakukohderyhmat = AuthoritiesUtil.filterHakukohderyhmaOids(mixedOids)
    assert(organisaatiot.intersect(hakukohderyhmat).isEmpty)
    // Tunnistamattomat jäävät kokonaan ulkopuolelle -- eivät siis päädy organisaatio-oikeuksiksi.
    assert(
      mixedOids.diff(organisaatiot).diff(hakukohderyhmat) == List(
        "1.2.246.562.20.00000000000000000112",
        "1.2.246.562.24.00000000019",
        "123"
      )
    )
  }

  it should "return an empty list for an empty input" in {
    assert(AuthoritiesUtil.filterOrganisaatioOids(List()).isEmpty)
    assert(AuthoritiesUtil.filterHakukohderyhmaOids(List()).isEmpty)
  }
}
