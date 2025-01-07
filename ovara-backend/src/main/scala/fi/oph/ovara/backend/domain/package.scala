package fi.oph.ovara.backend

package object domain {
  type Kielistetty = Map[Kieli, String]

  val ammatillisetHakukohdekoodit = List(
    "10", // Aikuiskoulutus
    "19", // Aikuisten perusopetus
    "15", // Ammatillinen peruskoulutus erityisopetuksena
    "11", // Perusopetuksen jälkeisen koulutuksen yhteishaku
    "20", // Erityisopetuksena järjestettävä ammatillinen koulutus
    "23", // Ammatillinen koulutus
    "24", // Lukiokoulutus
    "21", // Yhteishaun ulkopuolinen lukiokoulutus
  )

  val KOULUTUSTOIMIJAORGANISAATIOTYYPPI = "01"
  val OPPILAITOSORGANISAATIOTYYPPI = "02"
  val TOIMIPISTEORGANISAATIOTYYPPI = "03"
}
