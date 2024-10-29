package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.{Haku, Kieli, Kielistetty, Toteutus}
import org.springframework.stereotype.Component
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
class CommonRepository extends Extractors {
  def selectWithOid(oid: String): DBIO[Vector[Toteutus]] = {
    sql"""select * from pub.pub_dim_toteutus where toteutus_oid = $oid""".as[Toteutus]
  }

  def selectDistinctAlkamisvuodet(): SqlStreamingAction[Vector[String], String, Effect] = {
    sql"""select distinct koulutuksen_alkamisvuosi
          from pub.pub_dim_toteutus pdt
          where koulutuksen_alkamisvuosi is not null""".as[String]
  }

  def selectDistinctHaut(): SqlStreamingAction[Vector[Haku], Haku, Effect] = {
    sql"""select distinct haku_oid, haku_nimi
          from pub.pub_dim_haku
          where kohdejoukko_koodi in ('10', '11', '20', '23', '24')""".as[Haku]
  }
}
