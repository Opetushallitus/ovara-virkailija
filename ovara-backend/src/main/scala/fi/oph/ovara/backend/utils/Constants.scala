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

  val HAKUTOIVEET_TITLES: Map[String, List[String]] = Map(
    "fi" -> List(
      "Toive 1",
      "Toive 2",
      "Toive 3",
      "Toive 4",
      "Toive 5",
    ),
    "sv" -> List(
      "Toive 1",
      "Toive 2",
      "Toive 3",
      "Toive 4",
      "Toive 5",
    )
  )

  val HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_TITLES: Map[String, List[String]] = Map(
    "fi" -> List(
      "Hakukohde",
      "Hakijat yht.",
      "Hakijoista ensisijaisia",
      "Varasijalla",
      "Hyväksytyt",
      "Vastaanottaneet",
      "Läsnä",
      "Poissa",
      "Ilmoittautunet yhteensä",
      "Aloituspaikat",
    ),
    "sv" -> List(
      "Hakukohde",
      "Hakijat yht.",
      "Hakijoista ensisijaisia",
      "Varasijalla",
      "Hyväksytyt",
      "Vastaanottaneet",
      "Läsnä",
      "Poissa",
      "Ilmoittautunet yhteensä",
      "Aloituspaikat",
    )
  )

  val OPH_PAAKAYTTAJA_OID = "1.2.246.562.10.00000000001"

  val KOULUTUSTOIMIJARAPORTTI = "koulutustoimijaraportti"
  val OPPILAITOSRAPORTTI = "oppilaitosraportti"
  val TOIMIPISTERAPORTTI = "toimipisteraportti"
}
