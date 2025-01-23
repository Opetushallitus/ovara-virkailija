package fi.oph.ovara.backend.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

trait IAuthenticationFacade:
  def getAuthentication: Authentication

@Component
class AuthenticationFacade extends IAuthenticationFacade {

  @Override
  def getAuthentication: Authentication = {
    SecurityContextHolder.getContext.getAuthentication
  }
}
