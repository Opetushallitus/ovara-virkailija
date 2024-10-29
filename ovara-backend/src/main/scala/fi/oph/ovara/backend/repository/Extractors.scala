package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.{Haku, Kieli, Kielistetty, Toteutus}
import fi.oph.ovara.backend.utils.GenericOvaraJsonFormats
import org.json4s.jackson.Serialization.read
import slick.jdbc.GetResult

trait Extractors extends GenericOvaraJsonFormats {
  def extractKielistetty(json: Option[String]): Kielistetty = json.map(read[Map[Kieli, String]]).getOrElse(Map())

  implicit val getToteutusResult: GetResult[Toteutus] = GetResult(r => Toteutus(
    oid = r.nextString()
  ))

  implicit val getHakuResult: GetResult[Haku] = GetResult(r => Haku(
    haku_oid = r.nextString(),
    haku_nimi = extractKielistetty(r.nextStringOption())
  ))
}
