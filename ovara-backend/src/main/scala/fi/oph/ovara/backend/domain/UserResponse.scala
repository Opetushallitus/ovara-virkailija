package fi.oph.ovara.backend.domain

import scala.beans.BeanProperty

case class UserResponseUser(@BeanProperty userOid: String)
case class UserResponse(@BeanProperty user: UserResponseUser)