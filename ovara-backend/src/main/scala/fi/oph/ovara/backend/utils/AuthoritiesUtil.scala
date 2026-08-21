package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.utils.Constants.OPH_PAAKAYTTAJA_OID
import org.springframework.security.core.GrantedAuthority

import java.util
import scala.jdk.CollectionConverters.*
import scala.util.matching.Regex

object AuthoritiesUtil {
  def getOvaraAuthorities(allAuthorities: util.Collection[? <: GrantedAuthority]): List[String] = {
    val raportointiAuthoritiesRegex: Regex = """ROLE_APP_OVARA-VIRKAILIJA.*""".r
    allAuthorities.asScala.toList
      .flatMap(role => raportointiAuthoritiesRegex.findFirstIn(role.getAuthority))
  }

  def getKayttooikeusOids(authorities: List[String]): List[String] = {
    val raportointiOidRegex: Regex = """([0-9]\.?)+$""".r
    authorities.flatMap(role => raportointiOidRegex.findFirstIn(role)).distinct
  }

  def hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot: List[String]): Boolean = {
    kayttooikeusOrganisaatiot.contains(OPH_PAAKAYTTAJA_OID)
  }

  def filterHakukohderyhmaOids(kayttooikeusOids: List[String]): List[String] = {
    val regex = """^1\.2\.246\.562\.28.*""".r
    kayttooikeusOids.filter(oid => regex.matches(oid))
  }

  /**
   * Käyttöoikeus voidaan myöntää organisaatiolle tai hakukohderyhmälle. Organisaatio-oidit
   * poimitaan samalla patternilla jolla organisaatioOid-parametri validoidaan, jottei
   * rajapinnan kahdelle puolelle synny eri käsitystä siitä mikä on organisaatio-oid.
   *
   * Huomaa ettei tämä ja `filterHakukohderyhmaOids` yhdessä välttämättä kata koko listaa:
   * `getKayttooikeusOids` voi palauttaa myös muuta (esim. roolista `..._KK_HAKENEET_123`
   * irtoavan "123"), eikä sellainen ole kummankaan tyypin oikeus.
   */
  def filterOrganisaatioOids(kayttooikeusOids: List[String]): List[String] =
    kayttooikeusOids.filter(ParameterValidator.organisaatioOidPattern.matches)

}
