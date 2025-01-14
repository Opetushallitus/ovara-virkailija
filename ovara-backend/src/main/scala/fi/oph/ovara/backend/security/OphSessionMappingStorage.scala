package fi.oph.ovara.backend.security

import org.apereo.cas.client.session.SessionMappingStorage

trait OphSessionMappingStorage extends SessionMappingStorage {
  def clean(): Unit;

}
