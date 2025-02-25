package fi.oph.ovara.backend

package object domain {
  type Kielistetty = Map[Kieli, String]

  val KOULUTUSTOIMIJAORGANISAATIOTYYPPI = "01"
  val OPPILAITOSORGANISAATIOTYYPPI = "02"
  val TOIMIPISTEORGANISAATIOTYYPPI = "03"
}
