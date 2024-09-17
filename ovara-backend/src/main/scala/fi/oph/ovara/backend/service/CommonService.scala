package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.repository.{OvaraDatabase, ToteutusDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CommonService {
  @Autowired
    val db: OvaraDatabase = null

  def getToteutus(oid: String) = {
     db.run(ToteutusDAO().selectWithOid(oid))
  }
}

