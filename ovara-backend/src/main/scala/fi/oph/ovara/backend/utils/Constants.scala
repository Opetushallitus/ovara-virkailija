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

  val HAKUTOIVEET_TITLES: List[String] =
    List(
      "toive1",
      "toive2",
      "toive3",
      "toive4",
      "toive5",
      "toive6",
      "toive7"
    )

  val HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_COMMON_TITLES: List[String] =
    List(
      "hakijat-yht",
      "ensisijaisia",
      "varasija",
      "hyvaksytyt",
      "vastaanottaneet",
      "lasna",
      "poissa",
      "ilm-yht",
      "aloituspaikat"
    )

  val OPH_PAAKAYTTAJA_OID = "1.2.246.562.10.00000000001"

  val KOULUTUSTOIMIJARAPORTTI = "koulutustoimijaraportti"
  val OPPILAITOSRAPORTTI      = "oppilaitosraportti"
  val TOIMIPISTERAPORTTI      = "toimipisteraportti"

  val KORKEAKOULURAPORTTI    = "korkeakoulu"
  val TOISEN_ASTEEN_RAPORTTI = "toinen aste"

  val POSTIOSOITEFIELDS: List[String] = List("lahiosoite", "postinumero", "postitoimipaikka")
}
