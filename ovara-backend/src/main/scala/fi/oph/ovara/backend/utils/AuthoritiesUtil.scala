package fi.oph.ovara.backend.utils

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
}
