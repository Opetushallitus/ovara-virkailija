package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.Toteutus
import org.springframework.stereotype.Component
import slick.jdbc.PostgresProfile.api.*

@Component
class ToteutusDAO extends Extractors {
  def selectWithOid(oid: String): DBIO[Vector[Toteutus]] = {
    sql"""select * from pub.pub_dim_toteutus where toteutus_oid = '1.2.246.562.17.00000000000000003709'""".as[Toteutus]
  }
}
