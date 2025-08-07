package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.{En, Fi, Sv, Valintatapajono}
import org.scalatest.flatspec.AnyFlatSpec

class ExcelWriterUtilsSpec extends AnyFlatSpec {
  val translations: Map[String, String] = Map(
    "raportti.eligible" -> "hakukelpoinen SV"
  )

  "extractKoodi" should "return numeric koodi when there is no version" in {
    assert(ExcelWriterUtils.extractKoodi("koulutus_309902") == "309902")
  }

  it should "return numeric koodi when the koodiarvo ends in a version" in {
    assert(ExcelWriterUtils.extractKoodi("koulutus_309902") == "309902")
  }

  "getTranslationForCellValue" should "return translation key if translation is not found" in {
    assert(
      ExcelWriterUtils.getTranslationForCellValue("puuttuva_kaannos", translations) == "raportti.puuttuva_kaannos"
    )
  }

  it should "return translation when translation if found with translation key" in {
    assert(
      ExcelWriterUtils.getTranslationForCellValue("eligible", translations) == "hakukelpoinen SV"
    )
  }

  "createHakutoiveenArvosanatWritableValues" should "return empty list when there are no arvosanat in the result" in {
    val arvosanat: Map[String, String] = Map()
    val sortedArvosanaNames            = List()

    assert(ExcelWriterUtils.createHakutoiveenArvosanatWritableValues(arvosanat, sortedArvosanaNames) == List())
  }

  it should s"""return a list of "-" values when hakutoiveen arvosanat Map is empty""" in {
    val arvosanat: Map[String, String] = Map()
    val sortedArvosanaNames            = List("A", "BB", "EA", "KE", "M")

    assert(
      ExcelWriterUtils
        .createHakutoiveenArvosanatWritableValues(arvosanat, sortedArvosanaNames) == List("-", "-", "-", "-", "-")
    )
  }

  it should "return a list of arvosana values in the sorted orer of arvosana names list" in {
    val arvosanat           = Map("A" -> "E", "M" -> "E", "BB" -> "M", "EA" -> "C", "KE" -> "L")
    val sortedArvosanaNames = List("A", "BB", "EA", "KE", "M")

    assert(
      ExcelWriterUtils
        .createHakutoiveenArvosanatWritableValues(arvosanat, sortedArvosanaNames) == List("E", "M", "C", "L", "E")
    )
  }

  it should s"""add "-" when there is no corresponding arvosana value in the hakutoiveen arvosanat Map""" in {
    val arvosanat           = Map("A" -> "E", "M" -> "E", "KE" -> "L")
    val sortedArvosanaNames = List("A", "BB", "EA", "KE", "M")

    assert(
      ExcelWriterUtils
        .createHakutoiveenArvosanatWritableValues(arvosanat, sortedArvosanaNames) == List("E", "-", "-", "L", "E")
    )
  }

  "createHakutoiveenValintapajonoWritableValues" should "return empty list when there are no valintatapajonot in the result" in {
    val hakutoiveenValintapajonot: Map[String, Seq[Valintatapajono]] = Map()
    val sortedValintatapajonot                                       = List()
    val translations = Map[String, String]()
    assert(
      ExcelWriterUtils.createHakutoiveenValintatapajonoWritableValues(
        hakutoiveenValintapajonot,
        sortedValintatapajonot,
        translations
      ) == List()
    )
  }

  it should s"return a list with empty Map values because hakukohteen valintatapajonot list is empty" in {
    val hakutoiveenValintapajonot: Map[String, Seq[Valintatapajono]] = Map()
    val sortedValintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "17000468320548583779630214204232",
        valintatapajononNimi = "Koevalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Et osallistunut valintakokeeseen",
          Fi -> "Et osallistunut valintakokeeseen",
          Sv -> "Et osallistunut valintakokeeseen SV"
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1704199256878262657431481297336",
        valintatapajononNimi = "Todistusvalintajono ensikertalaisille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Et ole ensikertalainen hakija.",
          Fi -> "Et ole ensikertalainen hakija.",
          Sv -> "Du är inte en förstagångssökande."
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1707384694164-3621431542682802084",
        valintatapajononNimi = "Todistusvalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Fi -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Sv -> "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt."
        )
      )
    )
    val translations = Map[String, String]("raportti.hylatty" -> "Hylätty")

    assert(
      ExcelWriterUtils.createHakutoiveenValintatapajonoWritableValues(
        hakutoiveenValintapajonot,
        sortedValintatapajonot,
        translations
      ) == List(Map(), Map(), Map())
    )
  }

  it should "return a list with two valinnanTilanKuvaus values and one empty Map because two valintatapajono are found in hakukohteen valintatapajonot" in {
    val hakutoiveenValintapajonot: Map[String, Seq[Valintatapajono]] = Map(
      "1707384694164-3621431542682802084" ->
        List(
          Valintatapajono(
            valintatapajonoOid = "1707384694164-3621431542682802084",
            valintatapajononNimi = "Todistusvalintajono kaikille hakijoille",
            valinnanTila = "HYLATTY",
            valinnanTilanKuvaus = Map(
              En -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
              Fi -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
              Sv -> "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt."
            )
          )
        ),
      "17000468320548583779630214204232" -> List(
        Valintatapajono(
          valintatapajonoOid = "17000468320548583779630214204232",
          valintatapajononNimi = "Koevalintajono kaikille hakijoille",
          valinnanTila = "HYLATTY",
          valinnanTilanKuvaus = Map(
            En -> "Et osallistunut valintakokeeseen",
            Fi -> "Et osallistunut valintakokeeseen",
            Sv -> "Et osallistunut valintakokeeseen SV"
          )
        )
      )
    )
    val sortedValintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "17000468320548583779630214204232",
        valintatapajononNimi = "Koevalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Et osallistunut valintakokeeseen",
          Fi -> "Et osallistunut valintakokeeseen",
          Sv -> "Et osallistunut valintakokeeseen SV"
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1704199256878262657431481297336",
        valintatapajononNimi = "Todistusvalintajono ensikertalaisille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Et ole ensikertalainen hakija.",
          Fi -> "Et ole ensikertalainen hakija.",
          Sv -> "Du är inte en förstagångssökande."
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1707384694164-3621431542682802084",
        valintatapajononNimi = "Todistusvalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Fi -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Sv -> "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt."
        )
      )
    )
    val translations = Map[String, String]("raportti.hylatty" -> "Hylätty")

    assert(
      ExcelWriterUtils.createHakutoiveenValintatapajonoWritableValues(
        hakutoiveenValintapajonot,
        sortedValintatapajonot,
        translations
      ) == List(
        Map(
          En -> "Hylätty, Et osallistunut valintakokeeseen",
          Fi -> "Hylätty, Et osallistunut valintakokeeseen",
          Sv -> "Hylätty, Et osallistunut valintakokeeseen SV"
        ),
        Map(),
        Map(
          En -> "Hylätty, Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Fi -> "Hylätty, Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Sv -> "Hylätty, Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt."
        )
      )
    )
  }

  it should "return only valinnanTilanKuvaus for peruuntunut and only tila for hyvaksytty" in {
    val hakutoiveenValintapajonot: Map[String, Seq[Valintatapajono]] = Map(
      "1704199256878262657431481297336" ->
        List(
          Valintatapajono(
            valintatapajonoOid = "1704199256878262657431481297336",
            valintatapajononNimi = "Todistusvalintajono ensikertalaisille hakijoille",
            valinnanTila = "HYVAKSYTTY",
            valinnanTilanKuvaus = Map()
          )),
      "1707384694164-3621431542682802084" ->
        List(
          Valintatapajono(
            valintatapajonoOid = "1707384694164-3621431542682802084",
            valintatapajononNimi = "Todistusvalintajono kaikille hakijoille",
            valinnanTila = "PERUUNTUNUT",
            valinnanTilanKuvaus = Map(
              En -> "Peruuntunut, hyväksytty toisessa valintatapajonossa",
              Fi -> "Peruuntunut, hyväksytty toisessa valintatapajonossa",
              Sv -> "Peruuntunut, hyväksytty toisessa valintatapajonossa"
            )
          )
        )
    )
    val sortedValintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1704199256878262657431481297336",
        valintatapajononNimi = "Todistusvalintajono ensikertalaisille hakijoille",
        valinnanTila = "HYVAKSYTTY",
        valinnanTilanKuvaus = Map()
      ),
      Valintatapajono(
        valintatapajonoOid = "1707384694164-3621431542682802084",
        valintatapajononNimi = "Todistusvalintajono kaikille hakijoille",
        valinnanTila = "PERUUNTUNUT",
        valinnanTilanKuvaus = Map(
          En -> "Peruuntunut, hyväksytty toisessa valintatapajonossa",
          Fi -> "Peruuntunut, hyväksytty toisessa valintatapajonossa",
          Sv -> "Peruuntunut, hyväksytty toisessa valintatapajonossa"
        )
      )
    )
    val translations = Map[String, String]("raportti.hylatty" -> "Hylätty", "raportti.hyvaksytty" -> "Hyväksytty")

    assert(
      ExcelWriterUtils.createHakutoiveenValintatapajonoWritableValues(
        hakutoiveenValintapajonot,
        sortedValintatapajonot,
        translations
      ) == List(
        Map(
          En -> "Hyväksytty",
          Fi -> "Hyväksytty",
          Sv -> "Hyväksytty"
        ),
        Map(
          En -> "Peruuntunut, hyväksytty toisessa valintatapajonossa",
          Fi -> "Peruuntunut, hyväksytty toisessa valintatapajonossa",
          Sv -> "Peruuntunut, hyväksytty toisessa valintatapajonossa"
        )
      )
    )
  }
}
