package fi.oph.ovara.backend.raportointi.dto

import fi.oph.ovara.backend.domain.Kielistetty

case class RawKkPaatettavatOpiskeluoikeudetParams(
    oppilaitokset: List[String],
    sukunimi: Option[String],
    etunimet: Option[String],
    hetu: Option[String],
    oppijanumero: Option[String],
    opiskeluoikeudenTila: Option[String]
)

case class ValidatedKkPaatettavatOpiskeluoikeudetParams(
    oppilaitokset: List[String],
    sukunimi: Option[String],
    etunimet: Option[String],
    hetu: Option[String],
    oppijanumero: Option[String],
    opiskeluoikeudenTila: Option[String]
)

def buildKkPaatettavatOpiskeluoikeudetAuditParams(
                                                   params: ValidatedKkPaatettavatOpiskeluoikeudetParams
                                                 ): Map[String, Any] = {
  Map(
    "oppilaitokset" -> Option(params.oppilaitokset).filter(_.nonEmpty),
    "sukunimi" -> params.sukunimi,
    "etunimet" -> params.etunimet,
    "hetu" -> params.hetu,
    "oppijanumero" -> params.oppijanumero,
    "opiskeluoikeudenTila" -> params.opiskeluoikeudenTila
  ).collect { case (key, Some(value)) => key -> value }
}

def buildKkPaatettavatOpiskeluoikeudetParamsForExcel(params: ValidatedKkPaatettavatOpiskeluoikeudetParams, paramNames: Map[String, List[Kielistetty]]):
List[(String, Boolean | String | List[String] | Kielistetty | List[Kielistetty])] = {
  List(
    "oppilaitos" -> paramNames.getOrElse("oppilaitos", List.empty),
    "sukunimi" -> params.sukunimi.getOrElse(""),
    "etunimet" -> params.etunimet.getOrElse(""),
    "hetu" -> params.hetu.getOrElse(""),
    "oppijanumero" -> params.oppijanumero.getOrElse(""),
    "opiskeluoikeudenTila" -> params.opiskeluoikeudenTila.getOrElse("")
  ).filterNot { case (_, value) =>
    value match {
      case list: List[_] => list.isEmpty
      case str: String => str.isEmpty
      case _ => false
    }
  }
}
