package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.utils.{ParametriNimet, RepositoryUtils}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.{Component, Repository}
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
@Repository
class KkPaatettavatOpiskeluoikeudetRepository extends Extractors {

  val LOG: Logger = LoggerFactory.getLogger(classOf[KkPaatettavatOpiskeluoikeudetRepository])
  def hakuParamNamesQuery(
    oppilaitos: String
  ): SqlStreamingAction[Vector[ParametriNimet], ParametriNimet, Effect] = {

    val query = sql"""
       SELECT 'oppilaitos' AS param, organisaatio_nimi AS nimi 
       FROM pub.pub_dim_organisaatio 
       WHERE organisaatio_oid = $oppilaitos
      """.as[ParametriNimet]
    LOG.debug(s"hakuParamNamesQuery: ${query.statements.head}")
    query
  }
}
