package fi.oph.ovara.backend.utils

object Constants {
  val KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES: Map[String, List[String]] = Map(
    "fi" -> List(
      "Hakukohteen nimi",
      "Hakukohteen oid",
      "Kou.tila",
      "Tot.tila",
      "Hak.tila",
      "Aloituspaikat",
      "Koe",
      "Voi suorittaa kaksoistutkinnon?",
      "Voi suorittaa tutkinnon urheilijana?"
    ),
    "sv" -> List(
      "Hakukohteen nimi SV",
      "Hakukohteen oid SV",
      "Kou.tila SV",
      "Tot.tila SV",
      "Hak.tila SV",
      "Aloituspaikat SV",
      "Koe SV",
      "Voi suorittaa kaksoistutkinnon? SV",
      "Voi suorittaa tutkinnon urheilijana? SV"
    )
  )

  val OPH_PAAKAYTTAJA_OID = "1.2.246.562.10.00000000001"
}

export Constants.*
