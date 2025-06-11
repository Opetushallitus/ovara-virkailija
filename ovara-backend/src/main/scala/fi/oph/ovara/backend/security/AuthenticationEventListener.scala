package fi.oph.ovara.backend.security

import fi.oph.ovara.backend.utils.AuditLog
import fi.oph.ovara.backend.utils.AuditLogObj.{audit, getUser}
import fi.oph.ovara.backend.utils.AuditOperation.Login
import fi.vm.sade.auditlog.{Changes, Target}
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.stereotype.Component
import org.springframework.web.context.request.{RequestContextHolder, ServletRequestAttributes}



@Component class AuthenticationEventListener(auditLog: AuditLog) {
  @EventListener def onAuthenticationSuccess(event: AuthenticationSuccessEvent): Unit = {
    val target = new Target.Builder().setField("userOid", event.getAuthentication.getName).build()
    val request = getCurrentHttpRequest
    audit.log(getUser(request), Login, target, Changes.EMPTY)
    val username = event.getAuthentication.getName
  }

  private def getCurrentHttpRequest: HttpServletRequest = {
    val requestAttributes = RequestContextHolder.getRequestAttributes.asInstanceOf[ServletRequestAttributes]
    requestAttributes.getRequest
  }
}
