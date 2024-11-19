package fi.oph.ovara.backend.domain

import scala.beans.BeanProperty

case class User(@BeanProperty userOid: String,
                @BeanProperty authorities: List[String],
                @BeanProperty asiointikieli: Option[String] = None)

case class UserResponse(@BeanProperty user: User)