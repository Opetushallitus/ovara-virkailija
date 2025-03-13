package fi.oph.ovara.backend.security

import fi.oph.ovara.backend.repository.OvaraDatabase
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.session.{Session, SessionRepository}
import slick.jdbc.PostgresProfile.api.*

class JdbcSessionMappingStorage(sessionRepository: SessionRepository[Session], serviceName: String, ovaraDatabase: OvaraDatabase, @Value("${session.schema.name}") schema: String) extends OphSessionMappingStorage {

  val LOG = LoggerFactory.getLogger(classOf[JdbcSessionMappingStorage])

  val mappingTableName = s"${schema}.virkailija_cas_client_session"
  val sessionTableName = s"${schema}.virkailija_session"
  @Override
  def removeSessionByMappingId(mappingId: String): HttpSession = {
    LOG.debug(s"Poistetaan sessiomappaus cas tiketillä $mappingId")
    val query = sql"""SELECT virkailija_session_id FROM #$mappingTableName WHERE mapped_ticket_id = $mappingId""".as[String].headOption
    val sessionIdOpt = ovaraDatabase.run(query, "selectSessionIdByMappingId")

    sessionIdOpt
      .flatMap(sessionId => Option(sessionRepository.findById(sessionId)))
      .map(session => new HttpSessionAdapter(sessionRepository, session))
      .orNull
  }

  @Override
  def removeBySessionById(sessionId: String): Unit = {
    LOG.debug(s"Poistetaan sessiomappaus session id:llä $sessionId")
    val sql = sqlu"""DELETE FROM #$mappingTableName WHERE virkailija_session_id = $sessionId"""
    ovaraDatabase.run(sql, "removeBySessionById")
  }

  @Override
  def addSessionById(mappingId: String, session: HttpSession): Unit = {
    LOG.debug(s"Lisätään sessiomappaus, mappingId: $mappingId, sessionId: ${session.getId}")
    val sql = sqlu"""INSERT INTO #$mappingTableName (mapped_ticket_id, virkailija_session_id) VALUES ($mappingId, ${session.getId}) ON CONFLICT (mapped_ticket_id) DO NOTHING"""
    ovaraDatabase.run(sql, "addSessionById")
  }

  @Override
  def clean(): Unit = {
    LOG.debug("Siivotaan sessiomappaukset joille ei löydy sessiota")
    val sql = sqlu"""DELETE FROM #$mappingTableName WHERE virkailija_session_id NOT IN (SELECT session_id FROM #$sessionTableName)"""
    ovaraDatabase.run(sql, "clean")
  }

}
