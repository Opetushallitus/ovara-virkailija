package fi.oph.ovara.backend.raportointi.dto

import fi.oph.ovara.backend.domain.Kielistetty

case class KkPaatettavatOpiskeluoikeudetParams(
  oppilaitos: String,
  sukunimi: Option[String],
  etunimet: Option[String],
  hetu: Option[String],
  oppijanumero: Option[String],
  opiskeluoikeudenTila: Option[String]
)

def buildKkPaatettavatOpiskeluoikeudetAuditParams(
  params: KkPaatettavatOpiskeluoikeudetParams
): Map[String, Any] = {
  Map(
    "oppilaitos"           -> params.oppilaitos,
    "sukunimi"             -> params.sukunimi,
    "etunimet"             -> params.etunimet,
    "hetu"                 -> params.hetu,
    "oppijanumero"         -> params.oppijanumero,
    "opiskeluoikeuden-tila" -> params.opiskeluoikeudenTila
  ).collect { case (key, Some(value)) => key -> value }
}

def buildKkPaatettavatOpiskeluoikeudetParamsForExcel(
  params: KkPaatettavatOpiskeluoikeudetParams,
  paramNames: Map[String, Kielistetty]
): List[(String, String | Kielistetty)] = {
  List(
    "oppilaitos"           -> paramNames.getOrElse("oppilaitos", ""),
    "sukunimi"             -> params.sukunimi.getOrElse(""),
    "etunimet"             -> params.etunimet.getOrElse(""),
    "hetu"                 -> params.hetu.getOrElse(""),
    "oppijanumero"         -> params.oppijanumero.getOrElse(""),
    "opiskeluoikeudenTila" -> params.opiskeluoikeudenTila.getOrElse("")
  ).filterNot { case (_, value) =>
    value match {
      case str: String => str.isEmpty
      case _           => false
    }
  }
}
