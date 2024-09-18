package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.Toteutus
import org.springframework.stereotype.Component
import slick.jdbc.PostgresProfile.api.*

@Component
class CommonRepository extends Extractors {
  def selectWithOid(oid: String): DBIO[Vector[Toteutus]] = {
    sql"""select * from pub.pub_dim_toteutus where toteutus_oid = $oid""".as[Toteutus]
  }
}
