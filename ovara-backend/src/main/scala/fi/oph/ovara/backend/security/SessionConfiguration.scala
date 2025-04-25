package fi.oph.ovara.backend.security

import fi.oph.ovara.backend.repository.WriteDatabase
import org.apereo.cas.client.session.SessionMappingStorage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.{Bean, Configuration, Profile}
import org.springframework.session.{Session, SessionRepository}
import org.springframework.session.jdbc.JdbcIndexedSessionRepository

@Configuration
class SessionConfiguration {
  val LOG = LoggerFactory.getLogger(classOf[SessionConfiguration])

  @Autowired
  val db: WriteDatabase = null

  @Value("${session.schema.name}")
  val schema: String = null

  @Bean
  def sessionMappingStorage(sessionRepository: JdbcIndexedSessionRepository): SessionMappingStorage = {
    val jdbcSessionMappingStorage = new JdbcSessionMappingStorage(sessionRepository.asInstanceOf[SessionRepository[Session]], "ovara-virkailija", db, schema)
    jdbcSessionMappingStorage
  }
}
