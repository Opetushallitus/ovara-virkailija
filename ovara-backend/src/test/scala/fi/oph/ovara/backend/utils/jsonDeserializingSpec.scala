package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*
import fi.oph.ovara.backend.utils.Constants.ISO_LOCAL_DATE_TIME_FORMATTER
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jvalue2extractable
import org.scalatest.flatspec.AnyFlatSpec

import java.time.LocalDate

class jsonDeserializingSpec extends AnyFlatSpec with GenericOvaraJsonFormats {
  "valintatapajonoSerializer" should "return empty list when valintatapajonot json is empty" in {
    assert(parse("[]").extract[List[Valintatapajono]] == List())
  }

  it should "return list with one valintatapajono" in {
    val valintatapajonot = s"""[{"pisteet": null, "jonosija": 15, "prioriteetti": 6, "valinnan_tila": "HYLATTY",
                              |"julkaistavissa": false, "valintatiedon_pvm": "2024-07-10", "hyvaksyperuuntunut": false,
                              |"valintatapajono_oid": "17029035856444980475243405151068", "valintatapajono_nimi": "Certificate-based selection (Matriculation Examination)",
                              |"ehdollisesti_hyvaksytty": false, "ehdollisen_hyvaksymisen_ehto": {"en": null, "fi": null, "sv": null}, "hyvaksytty_harkinnanvaraisesti": false,
                              |"valinnantilan_kuvauksen_teksti": {"en": "This selection method is valid only for the applicant with Finnish matriculation examination or IB/EB/RP/DIA diploma.",
                              |"fi": "Tämä jono on käytössä vain ylioppilastutkinnon suorittaneilla.", "sv": ""}, "onko_muuttunut_viime_sijoittelussa": false, "siirtynyt_toisesta_valintatapajonosta": false}]""".stripMargin

    assert(
      parse(valintatapajonot).extract[List[Valintatapajono]] == List(
        Valintatapajono(
          valintatapajonoOid = "17029035856444980475243405151068",
          valintatapajononNimi = "Certificate-based selection (Matriculation Examination)",
          valinnanTila = "HYLATTY",
          valinnanTilanKuvaus = Map(
            En -> "This selection method is valid only for the applicant with Finnish matriculation examination or IB/EB/RP/DIA diploma.",
            Fi -> "Tämä jono on käytössä vain ylioppilastutkinnon suorittaneilla.",
            Sv -> ""
          )
        )
      )
    )
  }

  "hakuaikaSerializer" should "return hakuaika json extracted as Hakuaika with alkaa and paattyy times defined" in {
    val hakuaikaJson = s"""{"alkaa": "2021-09-01T08:00", "paattyy": "2021-09-15T15:00"}"""
    assert(
      parse(hakuaikaJson).extract[Hakuaika] == Hakuaika(
        alkaa = Some(LocalDate.parse("2021-09-01")),
        paattyy = Some(LocalDate.parse("2021-09-15"))
      )
    )
  }

  it should "return hakuaika json extracted as Hakuaika with only alkaa time defined" in {
    val hakuaikaJson = s"""{"alkaa": "2021-09-01T08:00"}"""
    assert(
      parse(hakuaikaJson).extract[Hakuaika] == Hakuaika(
        alkaa = Some(LocalDate.parse("2021-09-01")),
        paattyy = None
      )
    )
  }

  "koulutuksenAlkamiskausiSerializer" should "return json extracted as Alkamiskausi with alkamiskausityyppi, koulutuksenAlkamisvuosi and koulutuksenAlkamiskausiKoodiUri" in {
    val alkamiskausiJson =
      s"""{"alkamiskausityyppi": "alkamiskausi ja -vuosi", "koulutuksenAlkamisvuosi": "2023", "koulutuksenAlkamiskausiKoodiUri": "kausi_s#1", "henkilokohtaisenSuunnitelmanLisatiedot": {}}"""
    assert(
      parse(alkamiskausiJson).extract[Alkamiskausi] == Alkamiskausi(
        alkamiskausityyppi = "alkamiskausi ja -vuosi",
        koulutuksenAlkamisvuosi = Some("2023"),
        koulutuksenAlkamiskausiKoodiUri = Some("kausi_s#1")
      )
    )
  }

  it should "return json extracted as Alkamiskausi with alkamiskausityyppi, koulutuksenAlkamispaivamaara and koulutuksenPaattymispaivamaara" in {
    val alkamiskausiJson = s"""{
                              |  "alkamiskausityyppi": "tarkka alkamisajankohta",
                              |  "koulutuksenAlkamispaivamaara": "2023-01-16T09:00",
                              |  "koulutuksenPaattymispaivamaara": "2024-05-31T15:00",
                              |  "henkilokohtaisenSuunnitelmanLisatiedot": {}
                              |}""".stripMargin
    assert(
      parse(alkamiskausiJson).extract[Alkamiskausi] == Alkamiskausi(
        alkamiskausityyppi = "tarkka alkamisajankohta",
        koulutuksenAlkamispaivamaara = Some(LocalDate.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse("2023-01-16T09:00"))),
        koulutuksenPaattymispaivamaara = Some(LocalDate.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse("2024-05-31T15:00")))
      )
    )
  }

  it should "return json extracted as Alkamiskausi when alkamiskausityyppi is henkilokohtainen suunnitelma" in {
    val alkamiskausiJson = s"""{"alkamiskausityyppi": "henkilokohtainen suunnitelma","henkilokohtaisenSuunnitelmanLisatiedot": {}}"""
    assert(
      parse(alkamiskausiJson).extract[Alkamiskausi] == Alkamiskausi(
        alkamiskausityyppi = "henkilokohtainen suunnitelma"
      )
    )
  }
}
