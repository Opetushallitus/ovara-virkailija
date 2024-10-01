package fi.oph.ovara.backend.domain

import scala.beans.BeanProperty

case class UserResponseUser(@BeanProperty username: String)
case class UserResponse(@BeanProperty user: UserResponseUser)