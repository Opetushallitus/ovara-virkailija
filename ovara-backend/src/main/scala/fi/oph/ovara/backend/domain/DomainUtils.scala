package fi.oph.ovara.backend.domain

object DomainUtils {
  def mergeKielistetty(a: Kielistetty, b: Kielistetty): Kielistetty = {
    (a.keys ++ b.keys).map { key =>
      key -> List(a.getOrElse(key, ""), b.getOrElse(key, "")).filter(_.nonEmpty).mkString("\n")
    }.toMap
  }
}
