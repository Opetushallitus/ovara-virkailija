package fi.oph.ovara.backend.utils

import java.time.format.DateTimeFormatter

object Constants {
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
  val KK_HAKUTOIVEET_TITLES: List[String] =
    List(
      "toive1",
      "toive2",
      "toive3",
      "toive4",
      "toive5",
      "toive6"
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

  val KK_HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_COMMON_TITLES: List[String] =
    List(
      "hakijat-yht",
      "ensisijaisia",
      "ensikertalaisia",
      "hyvaksytyt",
      "vastaanottaneet",
      "lasna",
      "poissa",
      "ilm-yht",
      "maksuvelvollisia",
    )

  val KK_HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_ALOITUSPAIKAT_TITLES: List[String] =
    List(
      "valinnan-aloituspaikat",
      "aloituspaikat",
    )
    
  val OPH_PAAKAYTTAJA_OID = "1.2.246.562.10.00000000001"

  val KOULUTUSTOIMIJARAPORTTI = "koulutustoimijaraportti"
  val OPPILAITOSRAPORTTI      = "oppilaitosraportti"
  val TOIMIPISTERAPORTTI      = "toimipisteraportti"

  val KORKEAKOULURAPORTTI    = "korkeakoulu"
  val TOISEN_ASTEEN_RAPORTTI = "toinen aste"

  val POSTIOSOITEFIELDS: List[String] = List("lahiosoite", "postinumero", "postitoimipaikka")

  val ISO_LOCAL_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
}
