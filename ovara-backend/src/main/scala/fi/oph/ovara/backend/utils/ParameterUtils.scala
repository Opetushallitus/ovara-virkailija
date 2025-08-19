package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.Kielistetty

case class ParametriNimet(
                           parametri: String,
                           nimet: List[Kielistetty]
                         )

object ParameterUtils {

  def collectBooleanParams(params: List[(String, Option[Boolean])]): List[(String, Boolean)] = {
    params.collect {
      case (key, Some(value)) => key -> value
    }
  }
}
