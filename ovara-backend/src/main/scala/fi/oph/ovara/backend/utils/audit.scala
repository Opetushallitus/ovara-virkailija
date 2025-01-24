package fi.oph.ovara.backend.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.vm.sade.auditlog.{ApplicationType, Audit, Logger}
import org.ietf.jgss.Oid
import org.json4s.jackson.Serialization
import org.slf4j.LoggerFactory

import java.net.InetAddress

object AuditLogger extends Logger {
  private val logger = LoggerFactory.getLogger(classOf[Audit])

  override def log(msg: String): Unit = logger.info(msg)
}

object AuditLog extends AuditLog(AuditLogger)

class AuditLog(val logger: Logger) extends GsonSupport {

  val audit = new Audit(logger, "kouta-backend", ApplicationType.VIRKAILIJA)

  val mapper = {
    // luodaan objectmapper jonka pitäisi pystyä serialisoimaan "kaikki mahdollinen"
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.registerModule(new JavaTimeModule())
    mapper.registerModule(new Jdk8Module())
    mapper
  }
  
  def init(): Unit = {}

}


  class AuditUtil {}

object AuditUtil {
  private val logger = LoggerFactory.getLogger(classOf[Audit])

  implicit val formats = org.json4s.DefaultFormats

  def parseUser(request: HttpServletRequest, userOid: String): User = {
    try {
      val userAgent = Option(request.getHeader("User-Agent")).getOrElse("Unknown user agent")
      val session = request.getSession(false).getId
      val ip = InetAddress.getByName(HttpServletRequestUtils.getRemoteAddress(request))
      new User(new Oid(userOid), ip, session, userAgent)
    } catch {
      case e: Throwable =>
        logger.error("Error while parsing auditUser: " + e)
        throw AuditException(e.getMessage)
    }
  }

  def targetFromParams(params: Params): Target.Builder = {
    new Target.Builder().setField("params", Serialization.write(params))
  }
}

