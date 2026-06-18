package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.utils.ParametriKaannos
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.{Component, Repository}
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
@Repository
class KkPaatettavatOpiskeluoikeudetRepository extends Extractors {

  val LOG: Logger = LoggerFactory.getLogger(classOf[KkPaatettavatOpiskeluoikeudetRepository])
  def organisaatioNameQuery(
    oppilaitos: String
  ): SqlStreamingAction[Vector[ParametriKaannos], ParametriKaannos, Effect] = {

    val query = sql"""
       SELECT 'oppilaitos' AS param, organisaatio_nimi AS nimi
       FROM pub.pub_dim_organisaatio
       WHERE organisaatio_oid = $oppilaitos
      """.as[ParametriKaannos]
    LOG.debug(s"hakuParamNamesQuery: ${query.statements.head}")
    query
  }
}
