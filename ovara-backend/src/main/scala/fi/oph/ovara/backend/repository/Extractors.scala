package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.Toteutus
import slick.jdbc.GetResult

class Extractors {
  implicit val getToteutusResult: GetResult[Toteutus] = GetResult(r => Toteutus(
    oid = r.nextString()
  ))
}
