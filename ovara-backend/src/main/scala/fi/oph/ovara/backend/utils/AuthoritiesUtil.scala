package fi.oph.ovara.backend.utils
import org.springframework.security.core.GrantedAuthority
import scala.jdk.CollectionConverters._

import java.util
import scala.util.matching.Regex

object AuthoritiesUtil {
  def getRaportointiAuthorities(allAuthorities: util.Collection[? <: GrantedAuthority]): List[? <: GrantedAuthority] = {

    val raportointiAuthoritiesRegex: Regex = """ROLE_APP_RAPORTOINTI""".r
    allAuthorities.asScala.toList.filter(role => raportointiAuthoritiesRegex.findFirstIn(role.getAuthority).isDefined)
  }
}
