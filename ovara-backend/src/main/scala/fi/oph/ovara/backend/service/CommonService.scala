package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.Toteutus
import fi.oph.ovara.backend.repository.{CommonRepository, OvaraDatabase}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CommonService {
  @Autowired
    val db: OvaraDatabase = null

  def getToteutus(oid: String): Vector[Toteutus] = {
    db.run(CommonRepository().selectWithOid(oid))
  }
}

