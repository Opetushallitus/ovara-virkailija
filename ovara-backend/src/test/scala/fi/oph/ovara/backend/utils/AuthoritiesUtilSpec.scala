package fi.oph.ovara.backend.utils

import org.scalatest.*
import org.scalatest.flatspec.*
import org.springframework.security.core
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

import scala.jdk.CollectionConverters.*

class AuthoritiesUtilSpec extends AnyFlatSpec {
  "getRaportointiAuthorities" should "return empty list if no role in all authorities" in {
    val allAuthorities: java.util.Collection[SimpleGrantedAuthority] = List().asJava
    assert(AuthoritiesUtil.getRaportointiAuthorities(allAuthorities).isEmpty)
  }

  it should "return empty list when there is one role without the ROLE_APP_RAPORTOINTI prefix" in {
    val allAuthorities: java.util.Collection[SimpleGrantedAuthority] = List(
      SimpleGrantedAuthority("ROLE_APP_SUORITUSREKISTERI_CRUD_1.2.246.562.10.00000000001")).asJava
    assert(AuthoritiesUtil.getRaportointiAuthorities(allAuthorities).isEmpty)
  }

  it should "return only authorities with ROLE_APP_RAPORTOINTI prefix" in {
    val allAuthorities: java.util.Collection[SimpleGrantedAuthority] = List(
      SimpleGrantedAuthority("ROLE_APP_SUORITUSREKISTERI_CRUD_1.2.246.562.10.00000000001"),
      SimpleGrantedAuthority("ROLE_APP_RAPORTOINTI_1.2.246.562.10.00000000001"),
      SimpleGrantedAuthority("ROLE_APP_RAPORTOINTI_OPO"),
      SimpleGrantedAuthority("ROLE_APP_KOUTA"),
      SimpleGrantedAuthority("ROLE_APP_RAPORTOINTI_KK")
    ).asJava
    assert(AuthoritiesUtil.getRaportointiAuthorities(allAuthorities) == List(
      SimpleGrantedAuthority("ROLE_APP_RAPORTOINTI_1.2.246.562.10.00000000001"),
      SimpleGrantedAuthority("ROLE_APP_RAPORTOINTI_OPO"),
      SimpleGrantedAuthority("ROLE_APP_RAPORTOINTI_KK"),
    ))
  }
}