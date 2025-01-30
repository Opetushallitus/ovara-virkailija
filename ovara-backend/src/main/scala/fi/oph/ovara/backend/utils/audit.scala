package fi.oph.ovara.backend.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.vm.sade.auditlog.{ApplicationType, Audit, Changes, Logger, Operation, Target, User}
import org.ietf.jgss.Oid
import org.slf4j.LoggerFactory
import fi.vm.sade.javautils.http.HttpServletRequestUtils
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

import java.net.InetAddress

object AuditLogger extends Logger {
  private val logger = LoggerFactory.getLogger("fi.oph.ovara.backend.utils.AuditLog")

  override def log(msg: String): Unit = logger.info(msg)
}

object AuditLog extends AuditLog(AuditLogger)

class AuditLog(val logger: Logger) {

  val audit = new Audit(logger, "ovara-virkailija", ApplicationType.VIRKAILIJA)
  private val errorLogger = LoggerFactory.getLogger(classOf[AuditLog])

  def logWithParams(request: HttpServletRequest, operation: Operation, raporttiParams: Map[String, Any]): Unit = {
    try {
      val paramsJson = toJson(raporttiParams)
      val target = new Target.Builder().setField("parametrit", paramsJson).build()
      audit.log(getUser(request), operation, target, Changes.EMPTY)
    } catch {
      case e: Exception =>
        errorLogger.error(s"Auditlokitus epäonnistui: ${e.getMessage}")
        throw AuditException(e.getMessage)
    }
  }

  val mapper = {
    // luodaan objectmapper jonka pitäisi pystyä serialisoimaan "kaikki mahdollinen"
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.registerModule(new JavaTimeModule())
    mapper.registerModule(new Jdk8Module())
    mapper
  }

  def toJson(value: Any): String = {
    try {
      mapper.writeValueAsString(value)
    } catch {
      case e: Exception =>
        errorLogger.error("JSON-konversio epäonnistui: " + e.getMessage)
        throw AuditException(e.getMessage)
    }
  }

  def getUser(request: HttpServletRequest): User = {
    val userOid = getCurrentPersonOid()
    val ip = getInetAddress(request)
    new User(userOid, ip, request.getSession(false).getId(), Option(request.getHeader("User-Agent")).getOrElse("Tuntematon user agent"))
  }

  def getCurrentPersonOid(): Oid = {
    val authentication: Authentication = SecurityContextHolder.getContext().getAuthentication()
    if (authentication != null) {
      try {
        new Oid(authentication.getName())
      } catch {
        case e: Exception =>
          errorLogger.error(s"Käyttäjän oidin luonti epäonnistui: ${authentication.getName}")
          throw AuditException(e.getMessage)
      }
    } else {
      null
    }
  }

  def getInetAddress(request: HttpServletRequest): InetAddress = {
    InetAddress.getByName(HttpServletRequestUtils.getRemoteAddress(request))
  }
}

trait AuditOperation extends Operation {
  val name: String
}

object AuditOperation {
  case object Login extends AuditOperation {
    val name = "KIRJAUTUMINEN"
  }

  case object KoulutuksetToteutuksetHakukohteet extends AuditOperation {
    val name = "KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET"
  }
}

case class AuditException(message: String) extends Exception(message)