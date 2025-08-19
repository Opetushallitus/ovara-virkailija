package fi.oph.ovara.backend

import fi.oph.ovara.backend.domain.Kielistetty

package object domain {
  type Kielistetty = Map[Kieli, String]

  val KOULUTUSTOIMIJAORGANISAATIOTYYPPI = "01"
  val OPPILAITOSORGANISAATIOTYYPPI = "02"
  val TOIMIPISTEORGANISAATIOTYYPPI = "03"
}

case class ParametriNimet(
                           parametri: String,
                           nimet: List[Kielistetty]
                         )
