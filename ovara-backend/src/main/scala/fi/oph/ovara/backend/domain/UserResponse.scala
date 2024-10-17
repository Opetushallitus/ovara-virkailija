package fi.oph.ovara.backend.domain

import org.springframework.security.core.GrantedAuthority

import java.util
import scala.beans.BeanProperty

case class User(@BeanProperty userOid: String,
                @BeanProperty authorities: List[String])

case class UserResponse(@BeanProperty user: User)