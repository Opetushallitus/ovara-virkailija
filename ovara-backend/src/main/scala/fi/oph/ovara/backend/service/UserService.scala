package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.User
import fi.oph.ovara.backend.security.AuthenticationFacade
import fi.oph.ovara.backend.utils.AuthoritiesUtil
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.{Component, Service}

@Component
@Service
class UserService(onrService: OnrService, authenticationFacade: AuthenticationFacade) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[UserService])
  def getEnrichedUserDetails: User = {
    val principal = authenticationFacade.getAuthentication.getPrincipal.asInstanceOf[UserDetails]
    if (principal == null) {
      null
    } else {
      val username = principal.getUsername
      val asiointikieli = onrService.getAsiointikieli(username) match {
        case Left(e) => None
        case Right(v) => Some(v)
      }

      User(
        userOid = username,
        authorities = AuthoritiesUtil.getOvaraAuthorities(principal.getAuthorities),
        asiointikieli = asiointikieli
      )
    }
  }
}
