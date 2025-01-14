package fi.oph.ovara.backend.security

import fi.oph.ovara.backend.repository.OvaraDatabase
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.session.{Session, SessionRepository}
import slick.jdbc.PostgresProfile.api.*

class JdbcSessionMappingStorage(sessionRepository: SessionRepository[Session], serviceName: String, ovaraDatabase: OvaraDatabase, @Value("${session.schema.name}") schema: String) extends OphSessionMappingStorage {

  val LOG = LoggerFactory.getLogger(classOf[JdbcSessionMappingStorage])

  @Override
  def removeSessionByMappingId(mappingId: String): HttpSession = {
    val query = sql"SELECT session_id FROM #$schema.cas_client_session WHERE mapping_id = $mappingId".as[String].headOption
    val sessionIdOpt = ovaraDatabase.run(query)

    sessionIdOpt
      .flatMap(sessionId => Option(sessionRepository.findById(sessionId)))
      .map(session => new HttpSessionAdapter(sessionRepository, session))
      .orNull
  }

  @Override
  def removeBySessionById(sessionId: String): Unit = {
    val sql = sqlu"DELETE FROM #$schema.cas_client_session WHERE session_id = $sessionId"
    ovaraDatabase.run(sql)
  }

  @Override
  def addSessionById(mappingId: String, session: HttpSession): Unit = {
    val sql = sqlu"INSERT INTO #$schema.cas_client_session (mapping_id, session_id) VALUES ($mappingId, ${session.getId}) ON CONFLICT (mapping_id) DO NOTHING"
    ovaraDatabase.run(sql)
  }

  @Override
  def clean(): Unit = {
    val sql = sqlu"DELETE FROM #$schema.cas_client_session WHERE session_id NOT IN (SELECT session_id FROM spring_session)"
    ovaraDatabase.run(sql)
  }

}
