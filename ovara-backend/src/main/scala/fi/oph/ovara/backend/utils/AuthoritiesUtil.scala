package fi.oph.ovara.backend.utils

import org.springframework.security.core.GrantedAuthority

import java.util
import scala.jdk.CollectionConverters.*
import scala.util.matching.Regex

object AuthoritiesUtil {
  def getRaportointiAuthorities(allAuthorities: util.Collection[? <: GrantedAuthority]): List[String] = {
    val raportointiAuthoritiesRegex: Regex = """ROLE_APP_RAPORTOINTI.*""".r
    allAuthorities.asScala.toList
      .flatMap(role => raportointiAuthoritiesRegex.findFirstIn(role.getAuthority))
  }

  def getRaportointiOrganisaatiot(authorities: util.Collection[? <: GrantedAuthority]): List[String] = {
    val raportointiAuthorities = getRaportointiAuthorities(authorities)
    val raportointiOrganisaatiotRegex: Regex = """([0-9]\.?)+""".r
    raportointiAuthorities.flatMap(role => raportointiOrganisaatiotRegex.findFirstIn(role))
  }
}
