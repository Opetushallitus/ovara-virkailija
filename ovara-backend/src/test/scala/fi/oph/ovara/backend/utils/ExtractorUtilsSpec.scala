package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.{En, Fi, Sv, Valintatapajono}
import org.scalatest.flatspec.AnyFlatSpec

class ExtractorUtilsSpec extends AnyFlatSpec {
  "extractValintatapajonot" should "return empty list when there are no valintatapajonot" in {
    assert(ExtractorUtils.extractValintatapajonot(None) == List())
  }

  it should "return list with one valintatapajono" in {
    val valintatapajonot =
      Some(
        s"""[{"pisteet": null, "jonosija": 15, "prioriteetti": 6, "valinnan_tila": "HYLATTY",
           |"julkaistavissa": false, "valintatiedon_pvm": "2024-07-10", "hyvaksyperuuntunut": false,
           |"valintatapajono_oid": "17029035856444980475243405151068", "valintatapajono_nimi": "Certificate-based selection (Matriculation Examination)",
           |"ehdollisesti_hyvaksytty": false, "ehdollisen_hyvaksymisen_ehto": {"en": null, "fi": null, "sv": null}, "hyvaksytty_harkinnanvaraisesti": false,
           |"valinnantilan_kuvauksen_teksti": {"en": "This selection method is valid only for the applicant with Finnish matriculation examination or IB/EB/RP/DIA diploma.",
           |"fi": "Tämä jono on käytössä vain ylioppilastutkinnon suorittaneilla.", "sv": ""}, "onko_muuttunut_viime_sijoittelussa": false, "siirtynyt_toisesta_valintatapajonosta": false}]""".stripMargin
      )

    assert(
      ExtractorUtils.extractValintatapajonot(valintatapajonot) == List(
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
  "extractCommaSeparatedString" should "return None for an empty list and a comma-separated string for a non-empty list" in {
    assert(ExtractorUtils.extractCommaSeparatedString(None) == None)
    assert(ExtractorUtils.extractCommaSeparatedString(Some("[]")) == None)
    assert(ExtractorUtils.extractCommaSeparatedString(Some("""["amv", "yo"]""")) == Some("amv,yo"))
    assert(ExtractorUtils.extractCommaSeparatedString(Some("""["amv", "muu", "yo"]""")) == Some("amv,muu,yo"))
  }
}
