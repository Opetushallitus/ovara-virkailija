package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.{Fi, *}
import fi.oph.ovara.backend.utils.Constants.*
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.xssf.usermodel.{XSSFCellStyle, XSSFSheet, XSSFWorkbook}
import org.scalatest.flatspec.AnyFlatSpec

import java.time.LocalDate

class ExcelWriterSpec extends AnyFlatSpec {
  val userLng: String = "sv"

  val translations: Map[String, String] = Map(
    "raportti.yhteenveto"                     -> "Yhteenveto SV",
    "raportti.hakija"                         -> "Hakija SV",
    "raportti.turvakielto"                    -> "Turvakielto SV",
    "raportti.kansalaisuus"                   -> "Kansalaisuus SV",
    "raportti.hakukohteenNimi"                -> "Hakukohde SV",
    "raportti.kaksoistutkintoKiinnostaa"      -> "Kaksoistutkinto kiinnostaa SV",
    "raportti.markkinointilupa"               -> "LupaMark SV",
    "raportti.kylla"                          -> "Ja",
    "raportti.ei"                             -> "Nej",
    "raportti.hylatty"                        -> "Hylatty SV",
    "raportti.hyvaksytty"                     -> "Hyvaksytty SV",
    "raportti.peruuntunut"                    -> "Peruuntunut SV",
    "raportti.ei_vastaanotettu_maara_aikana"  -> "Ei vastaanotettu SV",
    "raportti.kokonaispisteet"                -> "Kokonaispisteet SV",
    "raportti.hylkaamisenTaiPeruuntumisenSyy" -> "Hylkäämisen tai peruuntumisen syy SV",
    "raportti.toimipiste"                     -> "Toimipiste SV",
    "raportti.hakukohde"                      -> "Hakukohde SV",
    "raportti.hakukelpoisuus"                 -> "Hakukelpoisuus SV",
    "raportti.eligible"                       -> "hakukelpoinen SV",
    "raportti.oppilaitos"                     -> "Oppilaitos SV",
    "raportti.hakijat-yht"                    -> "Hakijat SV",
    "raportti.ensisijaisia"                   -> "Ensisijaisia SV",
    "raportti.ensikertalaisia"                -> "Ensikertalaisia SV",
    "raportti.maksuvelvollisia"               -> "Maksuvelvollisia SV",
    "raportti.hakukohderyhma"                 -> "Hakukohderyhma SV",
    "raportti.varasija"                       -> "Varasija SV",
    "raportti.hyvaksytyt"                     -> "Hyväksytyt SV",
    "raportti.pohjakoulutus"                  -> "Pohjakoulutus SV",
    "raportti.puhelinnumero"                  -> "Puhelinnumero SV",
    "raportti.not-obligated"                  -> "Ei velvollinen",
    "raportti.obligated"                      -> "Velvollinen",
    "raportti.unreviewed"                     -> "Tarkastamatta SV",
    "raportti.vastaanottaneet"                -> "Vastaanottaneet SV",
    "raportti.valintatiedonPvm"               -> "Valintatiedon päivämäärä SV",
    "raportti.lasna"                          -> "Läsnä SV",
    "raportti.poissa"                         -> "Poissa SV",
    "raportti.ilm-yht"                        -> "IlmYht SV",
    "raportti.aloituspaikat"                  -> "Aloituspaikat SV",
    "raportti.valinnan-aloituspaikat"         -> "Valinnan aloituspaikat SV",
    "raportti.toive1"                         -> "Toive1 SV",
    "raportti.toive2"                         -> "Toive2 SV",
    "raportti.toive3"                         -> "Toive3 SV",
    "raportti.toive4"                         -> "Toive4 SV",
    "raportti.toive5"                         -> "Toive5 SV",
    "raportti.toive6"                         -> "Toive6 SV",
    "raportti.toive7"                         -> "Toive7 SV",
    "raportti.yhteensa"                       -> "Yhteensä SV",
    "raportti.yksittaiset-hakijat"            -> "Yksittäiset hakijat SV"
  )

  def checkAloituspaikatRowValidity(sheet: XSSFSheet, rowNumber: Int, expected: Int): Unit = {
    for (i <- 1 until 5) {
      assert(sheet.getRow(rowNumber).getCell(i) == null)
    }
    assert(sheet.getRow(rowNumber).getCell(5).getNumericCellValue == expected)
    for (i <- 6 until 8) {
      assert(sheet.getRow(rowNumber).getCell(i) == null)
    }
  }

  "countAloituspaikat" should "return 5 for one hakukohde with 5 aloituspaikkaa" in {
    val organisaationKoulutuksetToteutuksetHakukohteet = List(
      OrganisaationKoulutusToteutusHakukohde(
        organisaatio_oid = Some("1.2.246.562.10.278170642010"),
        koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
          hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
          hakukohdeOid = "1.2.246.562.20.00000000000000021565",
          koulutuksenTila = Some("julkaistu"),
          toteutuksenTila = Some("julkaistu"),
          hakukohteenTila = Some("julkaistu"),
          aloituspaikat = Some(5),
          onValintakoe = Some(false)
        )
      )
    )

    assert(ExcelWriter.countAloituspaikat(organisaationKoulutuksetToteutuksetHakukohteet) == 5)
  }

  it should "return 35 for three hakukohde aloituspaikat summed up" in {
    val kth = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.278170642010"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(5),
        onValintakoe = Some(false)
      )
    )

    val kth2 = kth.copy(koulutusToteutusHakukohde =
      kth._2.copy(
        hakukohdeNimi =
          Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000012345",
        aloituspaikat = Some(20)
      )
    )

    val kth3 = kth.copy(koulutusToteutusHakukohde =
      kth._2.copy(
        hakukohdeNimi =
          Map(En -> "Hakukohteen 3 nimi en", Fi -> "Hakukohteen 3 nimi fi", Sv -> "Hakukohteen 3 nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000025467",
        aloituspaikat = Some(10)
      )
    )

    val organisaationKoulutuksetToteutuksetHakukohteet = List(kth, kth2, kth3)

    assert(ExcelWriter.countAloituspaikat(organisaationKoulutuksetToteutuksetHakukohteet) == 35)
  }

  it should "return 0 if there are no results" in {
    assert(ExcelWriter.countAloituspaikat(List()) == 0)
  }

  "flattenHierarkiaHakukohteet" should "return list with one hakukohde for one org in hierarkia" in {
    val kth = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(8),
        onValintakoe = Some(false)
      )
    )

    val hierarkiaWithHakukohteet =
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(),
        List(kth)
      )

    assert(ExcelWriter.flattenHierarkiaHakukohteet(hierarkiaWithHakukohteet) == List(kth))
  }

  it should "return list with two hakukohde for koulutustoimija and oppilaitos in hierarkia" in {
    val kth = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(3),
        onValintakoe = Some(false)
      )
    )

    val oppilaitoksenKth = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.2781706420111"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohdeNimi = Map(
          En -> "Oppilaitoksen hakukohteen nimi en",
          Fi -> "Oppilaitoksen hakukohteen nimi fi",
          Sv -> "Oppilaitoksen hakukohteen nimi sv"
        ),
        hakukohdeOid = "1.2.246.562.20.000000000000000215666",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(8),
        onValintakoe = Some(false)
      )
    )

    val hierarkiaWithHakukohteet =
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.2781706420111",
            Map(
              En -> "Oppilaitoksen nimi en",
              Fi -> "Oppilaitoksen nimi fi",
              Sv -> "Oppilaitoksen nimi sv"
            ),
            List("02"),
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.2781706420111"),
            None,
            List(),
            List(oppilaitoksenKth)
          )
        ),
        List(kth)
      )

    assert(ExcelWriter.flattenHierarkiaHakukohteet(hierarkiaWithHakukohteet) == List(kth, oppilaitoksenKth))
  }

  it should "return list with two oppilaitos hakukohde in hierarkia" in {
    val kth1 = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.278170642010"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(8),
        onValintakoe = Some(false)
      )
    )

    val kth2 = kth1.copy(koulutusToteutusHakukohde =
      kth1._2.copy(
        hakukohdeNimi =
          Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
        hakukohdeOid = "1.2.246.562.20.0000000000000002156667"
      )
    )

    val hierarkiaWithHakukohteet =
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.278170642010",
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
            List("02"),
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
            None,
            List(),
            List(kth1, kth2)
          )
        ),
        List()
      )

    assert(ExcelWriter.flattenHierarkiaHakukohteet(hierarkiaWithHakukohteet) == List(kth1, kth2))
  }

  it should "return list with one toimipiste hakukohde and two oppilaitos hakukohde in hierarkia" in {
    val oppilaitoksenKth1 = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.278170642010"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(8),
        onValintakoe = Some(false)
      )
    )

    val oppilaitoksenKth2 = oppilaitoksenKth1.copy(
      koulutusToteutusHakukohde = oppilaitoksenKth1._2.copy(
        hakukohdeNimi =
          Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
        hakukohdeOid = "1.2.246.562.20.0000000000000002156667"
      )
    )

    val toimipisteenKth = oppilaitoksenKth1.copy(
      koulutusToteutusHakukohde = oppilaitoksenKth1._2.copy(
        hakukohdeNimi = Map(
          En -> "Toimipiste hakukohteen nimi en",
          Fi -> "Toimipiste hakukohteen nimi fi",
          Sv -> "Toimipiste hakukohteen nimi sv"
        ),
        hakukohdeOid = "1.2.246.562.20.000000000000000215521"
      ),
      organisaatio_oid = Some("1.2.246.562.10.2781706420101111")
    )

    val hierarkiaWithHakukohteet =
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.278170642010",
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
            List("02"),
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
            None,
            List(
              OrganisaatioHierarkiaWithHakukohteet(
                "1.2.246.562.10.2781706420101111",
                Map(En -> "Toimipisteen nimi en", Fi -> "Toimipisteen nimi fi", Sv -> "Toimipisteen nimi sv"),
                List("03"),
                List(
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.2781706420000",
                  "1.2.246.562.10.278170642010",
                  "1.2.246.562.10.2781706420101111"
                ),
                None,
                List(),
                List(toimipisteenKth)
              )
            ),
            List(oppilaitoksenKth1, oppilaitoksenKth2)
          )
        ),
        List()
      )

    assert(
      ExcelWriter.flattenHierarkiaHakukohteet(hierarkiaWithHakukohteet) == List(
        oppilaitoksenKth1,
        oppilaitoksenKth2,
        toimipisteenKth
      )
    )
  }

  "createOrganisaatioHeadingRow" should "create row with org name and total count of aloituspaikat" in {
    val oppilaitoksenKth1 = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(8),
        onValintakoe = Some(false)
      )
    )

    val oppilaitoksenKth2 = oppilaitoksenKth1.copy(
      koulutusToteutusHakukohde = oppilaitoksenKth1._2.copy(
        hakukohdeNimi =
          Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
        hakukohdeOid = "1.2.246.562.20.0000000000000002156667"
      )
    )

    val hierarkiaWithHakukohteet =
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(),
        List(oppilaitoksenKth1, oppilaitoksenKth2)
      )

    val workbook: XSSFWorkbook          = new XSSFWorkbook()
    val sheet: XSSFSheet                = workbook.createSheet()
    val headingCellstyle: XSSFCellStyle = workbook.createCellStyle()
    val titles = KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES.getOrElse(
      userLng,
      KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES.getOrElse("fi", List())
    )
    val raporttiColumnTitlesWithIndex = titles.zipWithIndex

    val currentRowIndex = ExcelWriter.createOrganisaatioHeadingRow(
      sheet = sheet,
      initialRowIndex = 0,
      indentedHeadingCellStyle = headingCellstyle,
      headingCellStyle = headingCellstyle,
      asiointikieli = userLng,
      hierarkiaWithHakukohteet = hierarkiaWithHakukohteet,
      raporttiColumnTitlesWithIndex = raporttiColumnTitlesWithIndex
    )
    assert(sheet.getRow(0).getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")
    assert(sheet.getRow(0).getCell(5).getNumericCellValue == 16)
    assert(sheet.getRow(1) == null)
    assert(sheet.getPhysicalNumberOfRows == 1)
    assert(currentRowIndex == 1)
  }

  it should "not create row with org name and total count of aloituspaikat if there are no hakukohteet" in {
    val hierarkiaWithHakukohteet =
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(),
        List()
      )

    val workbook: XSSFWorkbook          = new XSSFWorkbook()
    val sheet: XSSFSheet                = workbook.createSheet()
    val headingCellstyle: XSSFCellStyle = workbook.createCellStyle()
    val titles = KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES.getOrElse(
      userLng,
      KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES.getOrElse("fi", List())
    )
    val raporttiColumnTitlesWithIndex = titles.zipWithIndex

    val currentRowIndex = ExcelWriter.createOrganisaatioHeadingRow(
      sheet = sheet,
      initialRowIndex = 0,
      indentedHeadingCellStyle = headingCellstyle,
      headingCellStyle = headingCellstyle,
      asiointikieli = userLng,
      hierarkiaWithHakukohteet = hierarkiaWithHakukohteet,
      raporttiColumnTitlesWithIndex = raporttiColumnTitlesWithIndex
    )
    assert(sheet.getRow(0) == null)
    assert(sheet.getPhysicalNumberOfRows == 0)
    assert(currentRowIndex == 0)
  }

  "writeKoulutuksetToteutuksetHakukohteetRaportti" should "create one sheet and set 'Yhteenveto' as the name of the sheet" in {
    val hierarkiatWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
        List(),
        List()
      )
    )

    val wb =
      ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
        hierarkiatWithHakukohteet,
        KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES,
        userLng,
        KOULUTUSTOIMIJARAPORTTI,
        translations
      )
    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetName(0) == "Yhteenveto SV")
  }

  it should "create one sheet with the column title row and no results" in {
    val hierarkiatWithHakukohteet = List()
    val wb =
      ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
        hierarkiatWithHakukohteet,
        KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES,
        userLng,
        KOULUTUSTOIMIJARAPORTTI,
        translations
      )
    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Hakukohteen nimi SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "Hakukohteen oid SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "Kou.tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "Tot.tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "Hak.tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "Aloituspaikat SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "Koe SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "Voi suorittaa kaksoistutkinnon? SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "Voi suorittaa tutkinnon urheilijana? SV")
    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 1)
    assert(wb.getSheetAt(0).getRow(1) == null)
  }

  it should "create a sheet with the column title row and one result row for koulutustoimija" in {
    val hierarkiatWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(),
        List()
      )
    )

    val wb =
      ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
        hierarkiatWithHakukohteet,
        KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES,
        userLng,
        KOULUTUSTOIMIJARAPORTTI,
        translations
      )
    val sheet = wb.getSheetAt(0)
    // Heading row
    assert(sheet.getRow(0).getCell(0).getStringCellValue == "Hakukohteen nimi SV")
    assert(sheet.getRow(0).getCell(4).getStringCellValue == "Hak.tila SV")
    assert(sheet.getRow(0).getCell(5).getStringCellValue == "Aloituspaikat SV")
    assert(sheet.getRow(0).getCell(2).getStringCellValue == "Kou.tila SV")
    assert(sheet.getRow(0).getCell(3).getStringCellValue == "Tot.tila SV")
    assert(sheet.getRow(0).getCell(1).getStringCellValue == "Hakukohteen oid SV")
    assert(sheet.getRow(0).getCell(6).getStringCellValue == "Koe SV")
    assert(sheet.getRow(0).getCell(7).getStringCellValue == "Voi suorittaa kaksoistutkinnon? SV")
    assert(sheet.getRow(0).getCell(8).getStringCellValue == "Voi suorittaa tutkinnon urheilijana? SV")
    // Parent organisaatio row with aloituspaikat sum
    assert(sheet.getRow(1) == null)
    assert(sheet.getPhysicalNumberOfRows == 1)
  }

  it should "create a sheet with the column title row and two result rows for oppilaitos under koulutustoimija" in {
    val hierarkiatWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.278170642010",
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
            List("02"),
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
            None,
            List(),
            List(
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.278170642010"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohdeNimi =
                    Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
                  hakukohdeOid = "1.2.246.562.20.00000000000000021565",
                  koulutuksenTila = Some("julkaistu"),
                  toteutuksenTila = Some("julkaistu"),
                  hakukohteenTila = Some("julkaistu"),
                  aloituspaikat = Some(8),
                  onValintakoe = Some(false)
                )
              ),
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.278170642010"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohdeNimi =
                    Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
                  hakukohdeOid = "1.2.246.562.20.00000000000000031232",
                  koulutuksenTila = Some("julkaistu"),
                  toteutuksenTila = Some("tallennettu"),
                  hakukohteenTila = Some("tallennettu"),
                  aloituspaikat = Some(5),
                  onValintakoe = Some(true)
                )
              )
            )
          )
        ),
        List()
      )
    )

    val wb =
      ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
        hierarkiatWithHakukohteet,
        KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES,
        userLng,
        KOULUTUSTOIMIJARAPORTTI,
        translations
      )
    val sheet = wb.getSheetAt(0)
    // Heading row
    assert(sheet.getRow(0).getCell(0).getStringCellValue == "Hakukohteen nimi SV")
    assert(sheet.getRow(0).getCell(4).getStringCellValue == "Hak.tila SV")
    assert(sheet.getRow(0).getCell(5).getStringCellValue == "Aloituspaikat SV")
    assert(sheet.getRow(0).getCell(2).getStringCellValue == "Kou.tila SV")
    assert(sheet.getRow(0).getCell(3).getStringCellValue == "Tot.tila SV")
    assert(sheet.getRow(0).getCell(1).getStringCellValue == "Hakukohteen oid SV")
    assert(sheet.getRow(0).getCell(6).getStringCellValue == "Koe SV")
    assert(sheet.getRow(0).getCell(7).getStringCellValue == "Voi suorittaa kaksoistutkinnon? SV")
    assert(sheet.getRow(0).getCell(8).getStringCellValue == "Voi suorittaa tutkinnon urheilijana? SV")

    // Parent organisaatio row with aloituspaikat sum
    assert(sheet.getRow(1).getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")
    checkAloituspaikatRowValidity(sheet, 1, 13)

    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Oppilaitoksen nimi sv")
    // Hakukohde result row
    assert(sheet.getRow(3).getCell(0).getStringCellValue == "Hakukohteen nimi sv")
    assert(sheet.getRow(3).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000021565")
    assert(sheet.getRow(3).getCell(2).getStringCellValue == "julkaistu")
    assert(sheet.getRow(3).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(3).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(3).getCell(5).getNumericCellValue == 8)
    assert(sheet.getRow(3).getCell(6).getStringCellValue == "-")

    // Hakukohde 2 result row
    assert(sheet.getRow(4).getCell(0).getStringCellValue == "Hakukohteen 2 nimi sv")
    assert(sheet.getRow(4).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000031232")
    assert(sheet.getRow(4).getCell(2).getStringCellValue == "julkaistu")
    assert(sheet.getRow(4).getCell(3).getStringCellValue == "tallennettu")
    assert(sheet.getRow(4).getCell(4).getStringCellValue == "tallennettu")
    assert(sheet.getRow(4).getCell(5).getNumericCellValue == 5)
    assert(sheet.getRow(4).getCell(6).getStringCellValue == "X")

    assert(sheet.getPhysicalNumberOfRows == 5)
  }

  it should "create a sheet with two result rows for koulutustoimija" in {
    val koulutustoimijaWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(),
        List(
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohdeNimi =
                Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
              hakukohdeOid = "1.2.246.562.20.00000000000000021565",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("julkaistu"),
              hakukohteenTila = Some("julkaistu"),
              aloituspaikat = Some(8),
              onValintakoe = Some(false)
            )
          ),
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohdeNimi =
                Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
              hakukohdeOid = "1.2.246.562.20.00000000000000031232",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("tallennettu"),
              hakukohteenTila = Some("tallennettu"),
              aloituspaikat = Some(5),
              onValintakoe = Some(true)
            )
          )
        )
      )
    )

    val wb = ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
      koulutustoimijaWithHakukohteet,
      KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES,
      userLng,
      KOULUTUSTOIMIJARAPORTTI,
      translations
    )
    val sheet = wb.getSheetAt(0)
    // Parent organisaatio row with aloituspaikat sum
    assert(sheet.getRow(1).getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")
    checkAloituspaikatRowValidity(sheet, 1, 13)

    // Hakukohde result row
    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Hakukohteen nimi sv")
    assert(sheet.getRow(2).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000021565")
    assert(sheet.getRow(2).getCell(2).getStringCellValue == "julkaistu")
    assert(sheet.getRow(2).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(2).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(2).getCell(5).getNumericCellValue == 8)
    assert(sheet.getRow(2).getCell(6).getStringCellValue == "-")

    // Hakukohde 2 result row
    assert(sheet.getRow(3).getCell(0).getStringCellValue == "Hakukohteen 2 nimi sv")
    assert(sheet.getRow(3).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000031232")
    assert(sheet.getRow(3).getCell(2).getStringCellValue == "julkaistu")
    assert(sheet.getRow(3).getCell(3).getStringCellValue == "tallennettu")
    assert(sheet.getRow(3).getCell(4).getStringCellValue == "tallennettu")
    assert(sheet.getRow(3).getCell(5).getNumericCellValue == 5)
    assert(sheet.getRow(3).getCell(6).getStringCellValue == "X")

    assert(sheet.getPhysicalNumberOfRows == 4)
  }

  it should "create an oppilaitosraportti with two oppilaitos and subheadings with koulutustoimija name" in {
    val hierarkiatWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.278170642010",
        Map(En -> "Oppilaitoksen 1 nimi en", Fi -> "Oppilaitoksen 1 nimi fi", Sv -> "Oppilaitoksen 1 nimi sv"),
        List("02"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
        Some(
          Organisaatio(
            "1.2.246.562.10.2781706420000",
            Map(
              En -> "Koulutustoimijan nimi en",
              Fi -> "Koulutustoimijan nimi fi",
              Sv -> "Koulutustoimijan nimi sv"
            ),
            List("01")
          )
        ),
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.2781706420111",
            Map(En -> "Toimipisteen nimi en", Fi -> "Toimipisteen nimi fi", Sv -> "Toimipisteen nimi sv"),
            List("03"),
            List(
              "1.2.246.562.10.00000000001",
              "1.2.246.562.10.2781706420000",
              "1.2.246.562.10.278170642010",
              "1.2.246.562.10.2781706420111"
            ),
            Some(
              Organisaatio(
                "1.2.246.562.10.2781706420000",
                Map(
                  En -> "Koulutustoimijan nimi en",
                  Fi -> "Koulutustoimijan nimi fi",
                  Sv -> "Koulutustoimijan nimi sv"
                ),
                List("01")
              )
            ),
            List(),
            List(
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.2781706420111"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohdeNimi = Map(
                    En -> "Toimipisteen hakukohteen nimi en",
                    Fi -> "Toimipisteen hakukohteen nimi fi",
                    Sv -> "Toimipisteen hakukohteen nimi sv"
                  ),
                  hakukohdeOid = "1.2.246.562.20.0000000000000004567",
                  koulutuksenTila = Some("arkistoitu"),
                  toteutuksenTila = Some("julkaistu"),
                  hakukohteenTila = Some("julkaistu"),
                  aloituspaikat = Some(3),
                  onValintakoe = Some(false)
                )
              )
            )
          ),
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.2781706420132",
            Map(En -> "Toimipisteen 2 nimi en", Fi -> "Toimipisteen 2 nimi fi", Sv -> "Toimipisteen 2 nimi sv"),
            List("03"),
            List(
              "1.2.246.562.10.00000000001",
              "1.2.246.562.10.2781706420000",
              "1.2.246.562.10.278170642010",
              "1.2.246.562.10.2781706420132"
            ),
            Some(
              Organisaatio(
                "1.2.246.562.10.2781706420000",
                Map(
                  En -> "Koulutustoimijan nimi en",
                  Fi -> "Koulutustoimijan nimi fi",
                  Sv -> "Koulutustoimijan nimi sv"
                ),
                List("01")
              )
            ),
            List(
              OrganisaatioHierarkiaWithHakukohteet(
                "1.2.246.562.10.278170642013211",
                Map(
                  En -> "Toimipisteen 2 alitoimipisteen nimi en",
                  Fi -> "Toimipisteen 2 alitoimipisteen nimi fi",
                  Sv -> "Toimipisteen 2 alitoimipisteen nimi sv"
                ),
                List("03"),
                List(
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.2781706420000",
                  "1.2.246.562.10.278170642010",
                  "1.2.246.562.10.2781706420132",
                  "1.2.246.562.10.278170642013211"
                ),
                Some(
                  Organisaatio(
                    "1.2.246.562.10.2781706420000",
                    Map(
                      En -> "Koulutustoimijan nimi en",
                      Fi -> "Koulutustoimijan nimi fi",
                      Sv -> "Koulutustoimijan nimi sv"
                    ),
                    List("01")
                  )
                ),
                List(),
                List(
                  OrganisaationKoulutusToteutusHakukohde(
                    organisaatio_oid = Some("1.2.246.562.10.278170642013211"),
                    koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                      hakukohdeNimi = Map(
                        En -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi en",
                        Fi -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi fi",
                        Sv -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi sv"
                      ),
                      hakukohdeOid = "1.2.246.562.20.000000000000000456811",
                      koulutuksenTila = Some("arkistoitu"),
                      toteutuksenTila = Some("julkaistu"),
                      hakukohteenTila = Some("julkaistu"),
                      aloituspaikat = Some(2),
                      onValintakoe = Some(false)
                    )
                  )
                )
              )
            ),
            List(
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.2781706420132"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohdeNimi = Map(
                    En -> "Toimipisteen 2 hakukohteen nimi en",
                    Fi -> "Toimipisteen 2 hakukohteen nimi fi",
                    Sv -> "Toimipisteen 2 hakukohteen nimi sv"
                  ),
                  hakukohdeOid = "1.2.246.562.20.0000000000000004568",
                  koulutuksenTila = Some("arkistoitu"),
                  toteutuksenTila = Some("julkaistu"),
                  hakukohteenTila = Some("julkaistu"),
                  aloituspaikat = Some(4),
                  onValintakoe = Some(true)
                )
              )
            )
          )
        ),
        List(
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.278170642010"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohdeNimi =
                Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
              hakukohdeOid = "1.2.246.562.20.00000000000000021565",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("julkaistu"),
              hakukohteenTila = Some("julkaistu"),
              aloituspaikat = Some(8),
              onValintakoe = Some(false)
            )
          ),
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.278170642010"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohdeNimi =
                Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
              hakukohdeOid = "1.2.246.562.20.00000000000000031232",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("tallennettu"),
              hakukohteenTila = Some("tallennettu"),
              aloituspaikat = Some(5),
              onValintakoe = Some(true)
            )
          )
        )
      ),
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.278170642012",
        Map(En -> "Oppilaitoksen 2 nimi en", Fi -> "Oppilaitoksen 2 nimi fi", Sv -> "Oppilaitoksen 2 nimi sv"),
        List("02"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642012"),
        Some(
          Organisaatio(
            "1.2.246.562.10.2781706420002",
            Map(
              En -> "Koulutustoimijan 2 nimi en",
              Fi -> "Koulutustoimijan 2 nimi fi",
              Sv -> "Koulutustoimijan 2 nimi sv"
            ),
            List("01")
          )
        ),
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.27817064201113",
            Map(En -> "Toimipisteen 3 nimi en", Fi -> "Toimipisteen 3 nimi fi", Sv -> "Toimipisteen 3 nimi sv"),
            List("03"),
            List(
              "1.2.246.562.10.00000000001",
              "1.2.246.562.10.2781706420002",
              "1.2.246.562.10.278170642012",
              "1.2.246.562.10.27817064201113"
            ),
            None,
            List(),
            List(
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.27817064201113"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohdeNimi = Map(
                    En -> "Toimipisteen 3 hakukohteen nimi en",
                    Fi -> "Toimipisteen 3 hakukohteen nimi fi",
                    Sv -> "Toimipisteen 3 hakukohteen nimi sv"
                  ),
                  hakukohdeOid = "1.2.246.562.20.00000000000000045673",
                  koulutuksenTila = Some("arkistoitu"),
                  toteutuksenTila = Some("julkaistu"),
                  hakukohteenTila = Some("julkaistu"),
                  aloituspaikat = Some(3),
                  onValintakoe = Some(false)
                )
              )
            )
          )
        ),
        List(
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.278170642012"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohdeNimi = Map(
                En -> "Oppilaitoksen 2 hakukohteen nimi en",
                Fi -> "Oppilaitoksen 2 hakukohteen nimi fi",
                Sv -> "Oppilaitoksen 2 hakukohteen nimi sv"
              ),
              hakukohdeOid = "1.2.246.562.20.000000000000000215651",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("julkaistu"),
              hakukohteenTila = Some("julkaistu"),
              aloituspaikat = Some(8),
              onValintakoe = Some(false)
            )
          )
        )
      )
    )

    val wb = ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
      hierarkiatWithHakukohteet,
      KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES,
      userLng,
      OPPILAITOSRAPORTTI,
      translations
    )
    val sheet = wb.getSheetAt(0)
    // Parent organisaatio row with aloituspaikat sum
    assert(sheet.getRow(1).getCell(0).getStringCellValue == "Oppilaitoksen 1 nimi sv")
    checkAloituspaikatRowValidity(sheet, 1, 22)

    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")

    // Oppilaitos 1 hakukohteet
    assert(sheet.getRow(3).getCell(0).getStringCellValue == "Hakukohteen nimi sv")
    assert(sheet.getRow(3).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000021565")
    assert(sheet.getRow(3).getCell(2).getStringCellValue == "julkaistu")
    assert(sheet.getRow(3).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(3).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(3).getCell(5).getNumericCellValue == 8)
    assert(sheet.getRow(3).getCell(6).getStringCellValue == "-")

    // Hakukohde 2 result row
    assert(sheet.getRow(4).getCell(0).getStringCellValue == "Hakukohteen 2 nimi sv")
    assert(sheet.getRow(4).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000031232")
    assert(sheet.getRow(4).getCell(2).getStringCellValue == "julkaistu")
    assert(sheet.getRow(4).getCell(3).getStringCellValue == "tallennettu")
    assert(sheet.getRow(4).getCell(4).getStringCellValue == "tallennettu")
    assert(sheet.getRow(4).getCell(5).getNumericCellValue == 5)
    assert(sheet.getRow(4).getCell(6).getStringCellValue == "X")

    // Oppilaitos 1:n toimipiste 1
    assert(sheet.getRow(5).getCell(0).getStringCellValue == "Toimipisteen nimi sv")
    checkAloituspaikatRowValidity(sheet, 5, 3)

    // Toimipisteen hakukohde
    assert(sheet.getRow(6).getCell(0).getStringCellValue == "Toimipisteen hakukohteen nimi sv")
    assert(sheet.getRow(6).getCell(1).getStringCellValue == "1.2.246.562.20.0000000000000004567")
    assert(sheet.getRow(6).getCell(2).getStringCellValue == "arkistoitu")
    assert(sheet.getRow(6).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(6).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(6).getCell(5).getNumericCellValue == 3)
    assert(sheet.getRow(6).getCell(6).getStringCellValue == "-")

    // Oppilaitos 1:n toimipiste 2
    assert(sheet.getRow(7).getCell(0).getStringCellValue == "Toimipisteen 2 nimi sv")
    checkAloituspaikatRowValidity(sheet, 7, 6)

    // Toimipisteen 2 hakukohde
    assert(sheet.getRow(8).getCell(0).getStringCellValue == "Toimipisteen 2 hakukohteen nimi sv")
    assert(sheet.getRow(8).getCell(1).getStringCellValue == "1.2.246.562.20.0000000000000004568")
    assert(sheet.getRow(8).getCell(2).getStringCellValue == "arkistoitu")
    assert(sheet.getRow(8).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(8).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(8).getCell(5).getNumericCellValue == 4)
    assert(sheet.getRow(8).getCell(6).getStringCellValue == "X")

    // Oppilaitos 1:n toimipisteen 2 alitoimipiste
    assert(sheet.getRow(9).getCell(0).getStringCellValue == "Toimipisteen 2 alitoimipisteen nimi sv")
    checkAloituspaikatRowValidity(sheet, 9, 2)

    // Toimipisteen 2 alitoimipiste hakukohde
    assert(sheet.getRow(10).getCell(0).getStringCellValue == "Toimipisteen 2 alitoimipisteen hakukohteen nimi sv")
    assert(sheet.getRow(10).getCell(1).getStringCellValue == "1.2.246.562.20.000000000000000456811")
    assert(sheet.getRow(10).getCell(2).getStringCellValue == "arkistoitu")
    assert(sheet.getRow(10).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(10).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(10).getCell(5).getNumericCellValue == 2)
    assert(sheet.getRow(10).getCell(6).getStringCellValue == "-")

    // Oppilaitos 2
    assert(sheet.getRow(11).getCell(0).getStringCellValue == "Oppilaitoksen 2 nimi sv")
    checkAloituspaikatRowValidity(sheet, 11, 11)

    assert(sheet.getRow(12).getCell(0).getStringCellValue == "Koulutustoimijan 2 nimi sv")

    // Oppilaitos 2 hakukohde
    assert(sheet.getRow(13).getCell(0).getStringCellValue == "Oppilaitoksen 2 hakukohteen nimi sv")
    assert(sheet.getRow(13).getCell(1).getStringCellValue == "1.2.246.562.20.000000000000000215651")
    assert(sheet.getRow(13).getCell(2).getStringCellValue == "julkaistu")
    assert(sheet.getRow(13).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(13).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(13).getCell(5).getNumericCellValue == 8)
    assert(sheet.getRow(13).getCell(6).getStringCellValue == "-")

    // Oppilaitos 2 toimipiste 3
    assert(sheet.getRow(14).getCell(0).getStringCellValue == "Toimipisteen 3 nimi sv")
    checkAloituspaikatRowValidity(sheet, 14, 3)

    // Oppilaitos 2 hakukohde
    assert(sheet.getRow(15).getCell(0).getStringCellValue == "Toimipisteen 3 hakukohteen nimi sv")
    assert(sheet.getRow(15).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000045673")
    assert(sheet.getRow(15).getCell(2).getStringCellValue == "arkistoitu")
    assert(sheet.getRow(15).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(15).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(15).getCell(5).getNumericCellValue == 3)
    assert(sheet.getRow(15).getCell(6).getStringCellValue == "-")

    assert(sheet.getPhysicalNumberOfRows == 16)
  }

  it should "create a toimipisteraportti with three toimipiste and koulutustoimija name as a subheading" in {
    val hierarkiatWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420111",
        Map(En -> "Toimipisteen nimi en", Fi -> "Toimipisteen nimi fi", Sv -> "Toimipisteen nimi sv"),
        List("03"),
        List(
          "1.2.246.562.10.00000000001",
          "1.2.246.562.10.2781706420000",
          "1.2.246.562.10.278170642010",
          "1.2.246.562.10.2781706420111"
        ),
        Some(
          Organisaatio(
            "1.2.246.562.10.2781706420000",
            Map(
              En -> "Koulutustoimijan nimi en",
              Fi -> "Koulutustoimijan nimi fi",
              Sv -> "Koulutustoimijan nimi sv"
            ),
            List("01")
          )
        ),
        List(),
        List(
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.2781706420111"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohdeNimi = Map(
                En -> "Toimipisteen hakukohteen nimi en",
                Fi -> "Toimipisteen hakukohteen nimi fi",
                Sv -> "Toimipisteen hakukohteen nimi sv"
              ),
              hakukohdeOid = "1.2.246.562.20.0000000000000004567",
              koulutuksenTila = Some("arkistoitu"),
              toteutuksenTila = Some("julkaistu"),
              hakukohteenTila = Some("julkaistu"),
              aloituspaikat = Some(3),
              onValintakoe = Some(false)
            )
          )
        )
      ),
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420132",
        Map(En -> "Toimipisteen 2 nimi en", Fi -> "Toimipisteen 2 nimi fi", Sv -> "Toimipisteen 2 nimi sv"),
        List("03"),
        List(
          "1.2.246.562.10.00000000001",
          "1.2.246.562.10.2781706420000",
          "1.2.246.562.10.278170642010",
          "1.2.246.562.10.2781706420132"
        ),
        Some(
          Organisaatio(
            "1.2.246.562.10.2781706420000",
            Map(
              En -> "Koulutustoimijan 2 nimi en",
              Fi -> "Koulutustoimijan 2 nimi fi",
              Sv -> "Koulutustoimijan 2 nimi sv"
            ),
            List("01")
          )
        ),
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.278170642013211",
            Map(
              En -> "Toimipisteen 2 alitoimipisteen nimi en",
              Fi -> "Toimipisteen 2 alitoimipisteen nimi fi",
              Sv -> "Toimipisteen 2 alitoimipisteen nimi sv"
            ),
            List("03"),
            List(
              "1.2.246.562.10.00000000001",
              "1.2.246.562.10.2781706420000",
              "1.2.246.562.10.278170642010",
              "1.2.246.562.10.2781706420132",
              "1.2.246.562.10.278170642013211"
            ),
            Some(
              Organisaatio(
                "1.2.246.562.10.2781706420000",
                Map(
                  En -> "Koulutustoimijan 2 nimi en",
                  Fi -> "Koulutustoimijan 2 nimi fi",
                  Sv -> "Koulutustoimijan 2 nimi sv"
                ),
                List("01")
              )
            ),
            List(),
            List(
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.278170642013211"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohdeNimi = Map(
                    En -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi en",
                    Fi -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi fi",
                    Sv -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi sv"
                  ),
                  hakukohdeOid = "1.2.246.562.20.000000000000000456811",
                  koulutuksenTila = Some("arkistoitu"),
                  toteutuksenTila = Some("julkaistu"),
                  hakukohteenTila = Some("julkaistu"),
                  aloituspaikat = Some(2),
                  onValintakoe = Some(false)
                )
              )
            )
          )
        ),
        List(
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.2781706420132"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohdeNimi = Map(
                En -> "Toimipisteen 2 hakukohteen nimi en",
                Fi -> "Toimipisteen 2 hakukohteen nimi fi",
                Sv -> "Toimipisteen 2 hakukohteen nimi sv"
              ),
              hakukohdeOid = "1.2.246.562.20.0000000000000004568",
              koulutuksenTila = Some("arkistoitu"),
              toteutuksenTila = Some("julkaistu"),
              hakukohteenTila = Some("julkaistu"),
              aloituspaikat = Some(4),
              onValintakoe = Some(true)
            )
          )
        )
      )
    )

    val wb = ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
      hierarkiatWithHakukohteet,
      KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES,
      userLng,
      TOIMIPISTERAPORTTI,
      translations
    )
    val sheet = wb.getSheetAt(0)
    // Parent organisaatio row with aloituspaikat sum
    assert(sheet.getRow(1).getCell(0).getStringCellValue == "Toimipisteen nimi sv")
    checkAloituspaikatRowValidity(sheet, 1, 3)
    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")

    assert(sheet.getRow(4).getCell(0).getStringCellValue == "Toimipisteen 2 nimi sv")
    checkAloituspaikatRowValidity(sheet, 4, 6)
    assert(sheet.getRow(5).getCell(0).getStringCellValue == "Koulutustoimijan 2 nimi sv")

    assert(sheet.getRow(7).getCell(0).getStringCellValue == "Toimipisteen 2 alitoimipisteen nimi sv")
    checkAloituspaikatRowValidity(sheet, 7, 2)
    assert(sheet.getRow(8).getCell(0).getStringCellValue == "Koulutustoimijan 2 nimi sv")

    assert(sheet.getPhysicalNumberOfRows == 10)
  }

  "createKoulutuksetToteutuksetHakukohteetResultRows" should "create a sheet with the column title row and two result rows for oppilaitos under koulutustoimija" in {
    val hierarkiatWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.278170642010",
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
            List("02"),
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
            None,
            List(
              OrganisaatioHierarkiaWithHakukohteet(
                "1.2.246.562.10.2781706420111",
                Map(En -> "Toimipisteen nimi en", Fi -> "Toimipisteen nimi fi", Sv -> "Toimipisteen nimi sv"),
                List("03"),
                List(
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.2781706420000",
                  "1.2.246.562.10.278170642010",
                  "1.2.246.562.10.2781706420111"
                ),
                None,
                List(),
                List(
                  OrganisaationKoulutusToteutusHakukohde(
                    organisaatio_oid = Some("1.2.246.562.10.2781706420111"),
                    koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                      hakukohdeNimi = Map(
                        En -> "Toimipisteen hakukohteen nimi en",
                        Fi -> "Toimipisteen hakukohteen nimi fi",
                        Sv -> "Toimipisteen hakukohteen nimi sv"
                      ),
                      hakukohdeOid = "1.2.246.562.20.0000000000000004567",
                      koulutuksenTila = Some("arkistoitu"),
                      toteutuksenTila = Some("julkaistu"),
                      hakukohteenTila = Some("julkaistu"),
                      aloituspaikat = Some(3),
                      onValintakoe = Some(false)
                    )
                  )
                )
              ),
              OrganisaatioHierarkiaWithHakukohteet(
                "1.2.246.562.10.2781706420132",
                Map(En -> "Toimipisteen 2 nimi en", Fi -> "Toimipisteen 2 nimi fi", Sv -> "Toimipisteen 2 nimi sv"),
                List("03"),
                List(
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.2781706420000",
                  "1.2.246.562.10.278170642010",
                  "1.2.246.562.10.2781706420132"
                ),
                None,
                List(
                  OrganisaatioHierarkiaWithHakukohteet(
                    "1.2.246.562.10.278170642013211",
                    Map(
                      En -> "Toimipisteen 2 alitoimipisteen nimi en",
                      Fi -> "Toimipisteen 2 alitoimipisteen nimi fi",
                      Sv -> "Toimipisteen 2 alitoimipisteen nimi sv"
                    ),
                    List("03"),
                    List(
                      "1.2.246.562.10.00000000001",
                      "1.2.246.562.10.2781706420000",
                      "1.2.246.562.10.278170642010",
                      "1.2.246.562.10.2781706420132",
                      "1.2.246.562.10.278170642013211"
                    ),
                    None,
                    List(),
                    List(
                      OrganisaationKoulutusToteutusHakukohde(
                        organisaatio_oid = Some("1.2.246.562.10.278170642013211"),
                        koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                          hakukohdeNimi = Map(
                            En -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi en",
                            Fi -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi fi",
                            Sv -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi sv"
                          ),
                          hakukohdeOid = "1.2.246.562.20.000000000000000456811",
                          koulutuksenTila = Some("arkistoitu"),
                          toteutuksenTila = Some("julkaistu"),
                          hakukohteenTila = Some("julkaistu"),
                          aloituspaikat = Some(2),
                          onValintakoe = Some(false)
                        )
                      )
                    )
                  )
                ),
                List(
                  OrganisaationKoulutusToteutusHakukohde(
                    organisaatio_oid = Some("1.2.246.562.10.2781706420132"),
                    koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                      hakukohdeNimi = Map(
                        En -> "Toimipisteen 2 hakukohteen nimi en",
                        Fi -> "Toimipisteen 2 hakukohteen nimi fi",
                        Sv -> "Toimipisteen 2 hakukohteen nimi sv"
                      ),
                      hakukohdeOid = "1.2.246.562.20.0000000000000004568",
                      koulutuksenTila = Some("arkistoitu"),
                      toteutuksenTila = Some("julkaistu"),
                      hakukohteenTila = Some("julkaistu"),
                      aloituspaikat = Some(4),
                      onValintakoe = Some(true)
                    )
                  )
                )
              )
            ),
            List(
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.278170642010"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohdeNimi =
                    Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
                  hakukohdeOid = "1.2.246.562.20.00000000000000021565",
                  koulutuksenTila = Some("julkaistu"),
                  toteutuksenTila = Some("julkaistu"),
                  hakukohteenTila = Some("julkaistu"),
                  aloituspaikat = Some(8),
                  onValintakoe = Some(false),
                  voiSuorittaaKaksoistutkinnon = Some(true),
                  jarjestaaUrheilijanAmmKoulutusta = Some(true)
                )
              ),
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.278170642010"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohdeNimi =
                    Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
                  hakukohdeOid = "1.2.246.562.20.00000000000000031232",
                  koulutuksenTila = Some("julkaistu"),
                  toteutuksenTila = Some("tallennettu"),
                  hakukohteenTila = Some("tallennettu"),
                  aloituspaikat = Some(5),
                  onValintakoe = Some(true)
                )
              )
            )
          )
        ),
        List(
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohdeNimi = Map(
                En -> "Koulutustoimijan hakukohteen nimi en",
                Fi -> "Koulutustoimijan hakukohteen nimi fi",
                Sv -> "Koulutustoimijan hakukohteen nimi sv"
              ),
              hakukohdeOid = "1.2.246.562.20.00000000000000021542013",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("julkaistu"),
              hakukohteenTila = Some("julkaistu"),
              aloituspaikat = Some(4),
              onValintakoe = Some(false)
            )
          )
        )
      )
    )

    val workbook: XSSFWorkbook          = new XSSFWorkbook()
    val sheet: XSSFSheet                = workbook.createSheet()
    val headingCellStyle: XSSFCellStyle = workbook.createCellStyle()
    val cellStyle: XSSFCellStyle        = workbook.createCellStyle()
    val font1                           = workbook.createFont()
    val titles = KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES.getOrElse(
      userLng,
      KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES.getOrElse("fi", List())
    )
    val raporttiColumnTitlesWithIndex = titles.zipWithIndex

    val wb: Unit =
      ExcelWriter.createKoulutuksetToteutuksetHakukohteetResultRows(
        workbook,
        sheet,
        hierarkiatWithHakukohteet,
        0,
        headingCellStyle,
        cellStyle,
        cellStyle,
        font1,
        font1,
        userLng,
        raporttiColumnTitlesWithIndex,
        KOULUTUSTOIMIJARAPORTTI
      )

    // Parent organisaatio row with aloituspaikat sum
    assert(sheet.getRow(0).getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")
    assert(sheet.getRow(0).getCell(0).getCellStyle.getAlignment == HorizontalAlignment.LEFT)
    assert(sheet.getRow(0).getCell(0).getCellStyle.getIndention == 0.toShort)
    checkAloituspaikatRowValidity(sheet, 0, 26)

    assert(sheet.getRow(1).getCell(0).getStringCellValue == "Koulutustoimijan hakukohteen nimi sv")
    assert(sheet.getRow(1).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000021542013")
    assert(sheet.getRow(1).getCell(2).getStringCellValue == "julkaistu")
    assert(sheet.getRow(1).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(1).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(1).getCell(5).getNumericCellValue == 4)
    assert(sheet.getRow(1).getCell(6).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(9) == null)

    // Oppilaitos row
    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Oppilaitoksen nimi sv")
    assert(sheet.getRow(2).getCell(0).getCellStyle.getAlignment == HorizontalAlignment.LEFT)
    assert(sheet.getRow(2).getCell(0).getCellStyle.getIndention == 1.toShort)
    checkAloituspaikatRowValidity(sheet, 2, 22)

    // Hakukohde result row
    assert(sheet.getRow(3).getCell(0).getStringCellValue == "Hakukohteen nimi sv")
    assert(sheet.getRow(3).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000021565")
    assert(sheet.getRow(3).getCell(2).getStringCellValue == "julkaistu")
    assert(sheet.getRow(3).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(3).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(3).getCell(5).getNumericCellValue == 8)
    assert(sheet.getRow(3).getCell(6).getStringCellValue == "-")
    assert(sheet.getRow(3).getCell(7).getStringCellValue == "X")
    assert(sheet.getRow(3).getCell(8).getStringCellValue == "X")
    assert(sheet.getRow(3).getCell(9) == null)

    // Hakukohde 2 result row
    assert(sheet.getRow(4).getCell(0).getStringCellValue == "Hakukohteen 2 nimi sv")
    assert(sheet.getRow(4).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000031232")
    assert(sheet.getRow(4).getCell(2).getStringCellValue == "julkaistu")
    assert(sheet.getRow(4).getCell(3).getStringCellValue == "tallennettu")
    assert(sheet.getRow(4).getCell(4).getStringCellValue == "tallennettu")
    assert(sheet.getRow(4).getCell(5).getNumericCellValue == 5)
    assert(sheet.getRow(4).getCell(6).getStringCellValue == "X")
    assert(sheet.getRow(4).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(4).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(4).getCell(9) == null)

    // Toimipisteen row
    assert(sheet.getRow(5).getCell(0).getStringCellValue == "Toimipisteen nimi sv")
    assert(sheet.getRow(5).getCell(0).getCellStyle.getAlignment == HorizontalAlignment.LEFT)
    assert(sheet.getRow(5).getCell(0).getCellStyle.getIndention == 2.toShort)
    checkAloituspaikatRowValidity(sheet, 5, 3)

    // Toimipisteen result row
    assert(sheet.getRow(6).getCell(0).getStringCellValue == "Toimipisteen hakukohteen nimi sv")
    assert(sheet.getRow(6).getCell(1).getStringCellValue == "1.2.246.562.20.0000000000000004567")
    assert(sheet.getRow(6).getCell(2).getStringCellValue == "arkistoitu")
    assert(sheet.getRow(6).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(6).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(6).getCell(5).getNumericCellValue == 3)
    assert(sheet.getRow(6).getCell(6).getStringCellValue == "-")
    assert(sheet.getRow(6).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(6).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(6).getCell(9) == null)

    // Toimipiste 2 row
    assert(sheet.getRow(7).getCell(0).getStringCellValue == "Toimipisteen 2 nimi sv")
    assert(sheet.getRow(7).getCell(0).getCellStyle.getAlignment == HorizontalAlignment.LEFT)
    assert(sheet.getRow(7).getCell(0).getCellStyle.getIndention == 2.toShort)
    checkAloituspaikatRowValidity(sheet, 7, 6)

    // Toimipisteen 2 result row
    assert(sheet.getRow(8).getCell(0).getStringCellValue == "Toimipisteen 2 hakukohteen nimi sv")
    assert(sheet.getRow(8).getCell(1).getStringCellValue == "1.2.246.562.20.0000000000000004568")
    assert(sheet.getRow(8).getCell(2).getStringCellValue == "arkistoitu")
    assert(sheet.getRow(8).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(8).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(8).getCell(5).getNumericCellValue == 4)
    assert(sheet.getRow(8).getCell(6).getStringCellValue == "X")
    assert(sheet.getRow(8).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(8).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(8).getCell(9) == null)

    // Toimipisteen 2 alitoimipiste row
    assert(sheet.getRow(9).getCell(0).getStringCellValue == "Toimipisteen 2 alitoimipisteen nimi sv")
    assert(sheet.getRow(9).getCell(0).getCellStyle.getAlignment == HorizontalAlignment.LEFT)
    assert(sheet.getRow(9).getCell(0).getCellStyle.getIndention == 2.toShort)
    checkAloituspaikatRowValidity(sheet, 9, 2)

    // Toimipisteen 2 alitoimipisteen result row
    assert(sheet.getRow(10).getCell(0).getStringCellValue == "Toimipisteen 2 alitoimipisteen hakukohteen nimi sv")
    assert(sheet.getRow(10).getCell(1).getStringCellValue == "1.2.246.562.20.000000000000000456811")
    assert(sheet.getRow(10).getCell(2).getStringCellValue == "arkistoitu")
    assert(sheet.getRow(10).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(10).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(10).getCell(5).getNumericCellValue == 2)
    assert(sheet.getRow(10).getCell(6).getStringCellValue == "-")
    assert(sheet.getRow(10).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(10).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(10).getCell(9) == null)

    assert(sheet.getPhysicalNumberOfRows == 11)
  }

  "createHeadingRow" should "create heading row with translated column names or translation keys for hakijat raportti" in {
    val wb: XSSFWorkbook                = new XSSFWorkbook()
    val sheet: XSSFSheet                = wb.createSheet()
    val headingCellStyle: XSSFCellStyle = wb.createCellStyle()

    val fieldNames = classOf[ToisenAsteenHakijaWithCombinedNimi].getDeclaredFields.map(_.getName).toList
    ExcelWriter.createHeadingRow(
      sheet,
      userLng,
      translations,
      0,
      fieldNames,
      headingCellStyle
    )

    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Hakija SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "Turvakielto SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "Kansalaisuus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "Oppilaitos SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(0).getCell(9).getStringCellValue == "Kaksoistutkinto kiinnostaa SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(10).getStringCellValue == "raportti.urheilijatutkintoKiinnostaa")
    assert(wb.getSheetAt(0).getRow(0).getCell(11).getStringCellValue == "raportti.valintatieto")
    assert(wb.getSheetAt(0).getRow(0).getCell(12).getStringCellValue == "Varasija SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(13).getStringCellValue == "Kokonaispisteet SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(14).getStringCellValue == "Hylkäämisen tai peruuntumisen syy SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(15).getStringCellValue == "raportti.vastaanottotieto")
    assert(wb.getSheetAt(0).getRow(0).getCell(16).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(0).getCell(17).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(0).getCell(18).getStringCellValue == "raportti.harkinnanvaraisuus")
    assert(wb.getSheetAt(0).getRow(0).getCell(19).getStringCellValue == "raportti.soraAiempi")
    assert(wb.getSheetAt(0).getRow(0).getCell(20).getStringCellValue == "raportti.soraTerveys")
    assert(wb.getSheetAt(0).getRow(0).getCell(21).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(22).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(23).getStringCellValue == "raportti.julkaisulupa")
    assert(wb.getSheetAt(0).getRow(0).getCell(24).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(0).getCell(25).getStringCellValue == "raportti.lahiosoite")
    assert(wb.getSheetAt(0).getRow(0).getCell(26).getStringCellValue == "raportti.postinumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(27).getStringCellValue == "raportti.postitoimipaikka")
    assert(wb.getSheetAt(0).getRow(0).getCell(28) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 1)
    assert(wb.getSheetAt(0).getRow(1) == null)
  }

  "createHeadingRowWithValintatapajonotAndYoArvosanat" should "create heading row with translated column names or translation keys for hakijat raportti" in {
    val wb: XSSFWorkbook                = new XSSFWorkbook()
    val sheet: XSSFSheet                = wb.createSheet()
    val headingCellStyle: XSSFCellStyle = wb.createCellStyle()

    val valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1709304116443-4815957697640331820",
        valintatapajononNimi = "Lukiokoulutus",
        valinnanTila = "PERUUNTUNUT",
        valinnanTilanKuvaus = Map(
          En -> "Cancelled, accepted for a study place with higher priority",
          Fi -> "Peruuntunut, hyväksytty ylemmälle hakutoiveelle",
          Sv -> "Annullerad, godkänt till ansökningsmål med högre prioritet"
        )
      )
    )
    val origFieldNames    = classOf[KkHakijaWithCombinedNimi].getDeclaredFields.map(_.getName).toList
    val headingFieldNames = ExcelWriter.getHeadingFieldNames(true, true, true, valintatapajonot, List("A", "FF", "TE"))
    ExcelWriter.createHeadingRowWithValintatapajonotAndYoArvosanat(
      sheet,
      translations,
      0,
      headingFieldNames,
      origFieldNames,
      headingCellStyle
    )

    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Hakija SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "raportti.hetu")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "raportti.syntymaAika")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "Kansalaisuus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "Hakukelpoisuus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(9).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(0).getCell(10).getStringCellValue == "raportti.valintatieto")
    assert(wb.getSheetAt(0).getRow(0).getCell(11).getStringCellValue == "raportti.ehdollisestiHyvaksytty")
    assert(wb.getSheetAt(0).getRow(0).getCell(12).getStringCellValue == "Valintatiedon päivämäärä SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(13).getStringCellValue == "Lukiokoulutus")
    assert(wb.getSheetAt(0).getRow(0).getCell(14).getStringCellValue == "raportti.vastaanottotieto")
    assert(wb.getSheetAt(0).getRow(0).getCell(15).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(0).getCell(16).getStringCellValue == "raportti.ensikertalainen")
    assert(wb.getSheetAt(0).getRow(0).getCell(17).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(0).getCell(18).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(19).getStringCellValue == "raportti.maksuvelvollisuus")
    assert(wb.getSheetAt(0).getRow(0).getCell(20).getStringCellValue == "raportti.hakemusmaksunTila")
    assert(wb.getSheetAt(0).getRow(0).getCell(21).getStringCellValue == "raportti.julkaisulupa")
    assert(wb.getSheetAt(0).getRow(0).getCell(22).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(23).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(0).getCell(24).getStringCellValue == "raportti.lahiosoite")
    assert(wb.getSheetAt(0).getRow(0).getCell(25).getStringCellValue == "raportti.postinumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(26).getStringCellValue == "raportti.postitoimipaikka")
    assert(wb.getSheetAt(0).getRow(0).getCell(27).getStringCellValue == "raportti.kotikunta")
    assert(wb.getSheetAt(0).getRow(0).getCell(28).getStringCellValue == "raportti.asuinmaa")
    assert(wb.getSheetAt(0).getRow(0).getCell(29).getStringCellValue == "Puhelinnumero SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(30).getStringCellValue == "raportti.sahkoposti")
    assert(wb.getSheetAt(0).getRow(0).getCell(31).getStringCellValue == "A")
    assert(wb.getSheetAt(0).getRow(0).getCell(32).getStringCellValue == "FF")
    assert(wb.getSheetAt(0).getRow(0).getCell(33).getStringCellValue == "TE")
    assert(wb.getSheetAt(0).getRow(0).getCell(34) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 1)
    assert(wb.getSheetAt(0).getRow(1) == null)
  }

  "writeToisenAsteenHakijatRaportti" should "create one sheet with a sheet name, heading row and no results" in {
    val hakijatQueryResult = Vector()
    val wb =
      ExcelWriter.writeToisenAsteenHakijatRaportti(
        hakijatQueryResult,
        userLng,
        translations
      )

    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetName(0) == "Yhteenveto SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Hakija SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "Turvakielto SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "Kansalaisuus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "Oppilaitos SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(0).getCell(9).getStringCellValue == "Kaksoistutkinto kiinnostaa SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(10).getStringCellValue == "raportti.urheilijatutkintoKiinnostaa")
    assert(wb.getSheetAt(0).getRow(0).getCell(11).getStringCellValue == "raportti.valintatieto")
    assert(wb.getSheetAt(0).getRow(0).getCell(12).getStringCellValue == "Varasija SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(13).getStringCellValue == "Kokonaispisteet SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(14).getStringCellValue == "Hylkäämisen tai peruuntumisen syy SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(15).getStringCellValue == "raportti.vastaanottotieto")
    assert(wb.getSheetAt(0).getRow(0).getCell(16).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(0).getCell(17).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(0).getCell(18).getStringCellValue == "raportti.harkinnanvaraisuus")
    assert(wb.getSheetAt(0).getRow(0).getCell(19).getStringCellValue == "raportti.soraAiempi")
    assert(wb.getSheetAt(0).getRow(0).getCell(20).getStringCellValue == "raportti.soraTerveys")
    assert(wb.getSheetAt(0).getRow(0).getCell(21).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(22).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(23).getStringCellValue == "raportti.julkaisulupa")
    assert(wb.getSheetAt(0).getRow(0).getCell(24).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(0).getCell(25).getStringCellValue == "raportti.lahiosoite")
    assert(wb.getSheetAt(0).getRow(0).getCell(26).getStringCellValue == "raportti.postinumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(27).getStringCellValue == "raportti.postitoimipaikka")
    assert(wb.getSheetAt(0).getRow(0).getCell(28) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 1)
    assert(wb.getSheetAt(0).getRow(1) == null)
  }

  it should "return excel with one result row in addition to heading row" in {
    val hakijatResult = Vector(
      ToisenAsteenHakijaWithCombinedNimi(
        "Rautiainen-Testi, Dina Testi",
        Some(false),
        Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland"),
        "1.2.246.562.24.30646006111",
        "1.2.246.562.11.00000000000002179045",
        Map(En -> "Oppilaitos en", Fi  -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
        Map(En -> "Toimipiste en", Fi  -> "Toimipiste fi", Sv -> "Toimipiste sv"),
        Map(En -> "Hakukohde 1 EN", Fi -> "Hakukohde 1", Sv   -> "Hakukohde 1 SV"),
        2,
        Some(true),
        None,
        Some("HYLATTY"),
        None,
        None,
        Map(En -> "", Fi -> "Hylätty harkinnanvaraisessa valinnassa", Sv -> ""),
        Some("EI_VASTAANOTETTU_MAARA_AIKANA"),
        None,
        None,
        Some("ATARU_OPPIMISVAIKEUDET"),
        Some(false),
        Some(false),
        Map(
          En -> "Perusopetuksen oppimäärä",
          Fi -> "Perusopetuksen oppimäärä",
          Sv -> "Den grundläggande utbildningens lärokurs"
        ),
        Some(false),
        Some(true),
        None,
        Some("Rämsöönranta 368"),
        Some("00100"),
        Some("HELSINKI")
      )
    )

    val wb =
      ExcelWriter.writeToisenAsteenHakijatRaportti(
        hakijatResult,
        userLng,
        translations
      )

    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Rautiainen-Testi, Dina Testi")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "1.2.246.562.11.00000000000002179045")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "Oppilaitos sv")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "Toimipiste sv")
    assert(wb.getSheetAt(0).getRow(1).getCell(7).getStringCellValue == "Hakukohde 1 SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(8).getNumericCellValue == 2)
    assert(wb.getSheetAt(0).getRow(1).getCell(9).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(10).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(11).getStringCellValue == "Hylatty SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(12).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(13).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(14).getStringCellValue == "")
    assert(wb.getSheetAt(0).getRow(1).getCell(15).getStringCellValue == "Ei vastaanotettu SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(16).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(17).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(18).getStringCellValue == "raportti.oppimisvaikeudet")
    assert(wb.getSheetAt(0).getRow(1).getCell(19).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(20).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(21).getStringCellValue == "Den grundläggande utbildningens lärokurs")
    assert(wb.getSheetAt(0).getRow(1).getCell(22).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(23).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(24).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(25).getStringCellValue == "Rämsöönranta 368")
    assert(wb.getSheetAt(0).getRow(1).getCell(26).getStringCellValue == "00100")
    assert(wb.getSheetAt(0).getRow(1).getCell(27).getStringCellValue == "HELSINKI")
    assert(wb.getSheetAt(0).getRow(1).getCell(28) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 2)
    assert(wb.getSheetAt(0).getRow(2) == null)
  }

  it should "return excel with three result rows: two for one hakija and one for another hakija" in {
    val hakijatResult =
      Vector(
        ToisenAsteenHakijaWithCombinedNimi(
          "Rautiainen-Testi, Dina Testi",
          Some(false),
          Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland"),
          "1.2.246.562.24.30646006111",
          "1.2.246.562.11.00000000000002179045",
          Map(En -> "Oppilaitos 1 en", Fi -> "Oppilaitos 1 fi", Sv -> "Oppilaitos 1 sv"),
          Map(En -> "Toimipiste 1 en", Fi -> "Toimipiste 1 fi", Sv -> "Toimipiste 1 sv"),
          Map(En -> "Hakukohde 1 EN", Fi  -> "Hakukohde 1", Sv     -> "Hakukohde 1 SV"),
          2,
          Some(true),
          Some(false),
          Some("HYLATTY"),
          Some("3"),
          Some("8.5"),
          Map(En -> "", Fi -> "Hylätty harkinnanvaraisessa valinnassa", Sv -> ""),
          None,
          None,
          Some("LASNA_KOKO_LUKUVUOSI"),
          None,
          Some(false),
          Some(false),
          Map(
            En -> "Perusopetuksen oppimäärä",
            Fi -> "Perusopetuksen oppimäärä",
            Sv -> "Den grundläggande utbildningens lärokurs"
          ),
          Some(false),
          Some(true),
          None,
          Some("Rämsöönranta 368"),
          Some("00100"),
          Some("HELSINKI")
        ),
        ToisenAsteenHakijaWithCombinedNimi(
          "Rautiainen-Testi, Dina Testi",
          Some(false),
          Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland"),
          "1.2.246.562.24.30646006111",
          "1.2.246.562.11.00000000000002112891",
          Map(En -> "Oppilaitos 2 en", Fi -> "Oppilaitos 2 fi", Sv -> "Oppilaitos 2 sv"),
          Map(En -> "Toimipiste 2 en", Fi -> "Toimipiste 2 fi", Sv -> "Toimipiste 2 sv"),
          Map(En -> "Hakukohde 2 EN", Fi  -> "Hakukohde 2", Sv     -> "Hakukohde 2 SV"),
          1,
          Some(true),
          Some(true),
          Some("HYVAKSYTTY"),
          Some("8"),
          None,
          Map(),
          Some("PERUNUT"),
          None,
          Some("EI_TEHTY"),
          Some("ATARU_ULKOMAILLA_OPISKELTU"),
          Some(false),
          Some(false),
          Map(
            En -> "Perusopetuksen oppimäärä",
            Fi -> "Perusopetuksen oppimäärä",
            Sv -> "Den grundläggande utbildningens lärokurs"
          ),
          Some(true),
          Some(true),
          None,
          Some("Rämsöönranta 368"),
          Some("00100"),
          Some("HELSINKI")
        ),
        ToisenAsteenHakijaWithCombinedNimi(
          "Lehto-Testi, Vikke Testi",
          Some(false),
          Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland"),
          "1.2.246.562.24.18441866015",
          "1.2.246.562.11.00000000000002126102",
          Map(En -> "Oppilaitos 3 en", Fi -> "Oppilaitos 3 fi", Sv -> "Oppilaitos 3 sv"),
          Map(En -> "Toimipiste 3 en", Fi -> "Toimipiste 3 fi", Sv -> "Toimipiste 3 sv"),
          Map(En -> "Hakukohde 3 EN", Fi  -> "Hakukohde 3", Sv     -> "Hakukohde 3 SV"),
          1,
          Some(true),
          Some(false),
          Some("PERUUNTUNUT"),
          None,
          Some("5.6"),
          Map(
            En -> "Cancelled, waiting list selection has ended",
            Fi -> "Peruuntunut, varasijatäyttö päättynyt",
            Sv -> "Annullerad, besättning av reservplatser har upphört"
          ),
          Some("PERUNUT"),
          Some(LocalDate.parse("2024-06-26")),
          Some("LASNA_KOKO_LUKUVUOSI"),
          Some("SURE_YKS_MAT_AI"),
          Some(false),
          Some(false),
          Map(
            En -> "Ulkomailla suoritettu koulutus",
            Fi -> "Ulkomailla suoritettu koulutus",
            Sv -> "Utbildning utomlands"
          ),
          Some(false),
          Some(true),
          Some(true),
          Some("Laholanaukio 834"),
          Some("00100"),
          Some("HELSINKI")
        )
      )

    val wb =
      ExcelWriter.writeToisenAsteenHakijatRaportti(
        hakijatResult,
        userLng,
        translations
      )

    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Rautiainen-Testi, Dina Testi")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "1.2.246.562.11.00000000000002179045")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "Oppilaitos 1 sv")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "Toimipiste 1 sv")
    assert(wb.getSheetAt(0).getRow(1).getCell(7).getStringCellValue == "Hakukohde 1 SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(8).getNumericCellValue == 2)
    assert(wb.getSheetAt(0).getRow(1).getCell(9).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(10).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(11).getStringCellValue == "Hylatty SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(12).getStringCellValue == "3")
    assert(wb.getSheetAt(0).getRow(1).getCell(13).getStringCellValue == "8.5")
    assert(wb.getSheetAt(0).getRow(1).getCell(14).getStringCellValue == "")
    assert(wb.getSheetAt(0).getRow(1).getCell(15).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(16).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(17).getStringCellValue == "raportti.lasna_koko_lukuvuosi")
    assert(wb.getSheetAt(0).getRow(1).getCell(18).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(19).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(20).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(21).getStringCellValue == "Den grundläggande utbildningens lärokurs")
    assert(wb.getSheetAt(0).getRow(1).getCell(22).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(23).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(24).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(25).getStringCellValue == "Rämsöönranta 368")
    assert(wb.getSheetAt(0).getRow(1).getCell(26).getStringCellValue == "00100")
    assert(wb.getSheetAt(0).getRow(1).getCell(27).getStringCellValue == "HELSINKI")
    assert(wb.getSheetAt(0).getRow(1).getCell(28) == null)

    assert(wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue == "Rautiainen-Testi, Dina Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(1).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(2).getCell(2).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(3).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(2).getCell(4).getStringCellValue == "1.2.246.562.11.00000000000002112891")
    assert(wb.getSheetAt(0).getRow(2).getCell(5).getStringCellValue == "Oppilaitos 2 sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(6).getStringCellValue == "Toimipiste 2 sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(7).getStringCellValue == "Hakukohde 2 SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(8).getNumericCellValue == 1)
    assert(wb.getSheetAt(0).getRow(2).getCell(9).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(10).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(11).getStringCellValue == "Hyvaksytty SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(12).getStringCellValue == "8")
    assert(wb.getSheetAt(0).getRow(2).getCell(13).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(14).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(15).getStringCellValue == "raportti.perunut")
    assert(wb.getSheetAt(0).getRow(2).getCell(16).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(17).getStringCellValue == "raportti.ei_tehty")
    assert(wb.getSheetAt(0).getRow(2).getCell(18).getStringCellValue == "raportti.koulutodistusten_vertailuvaikeudet")
    assert(wb.getSheetAt(0).getRow(2).getCell(19).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(2).getCell(20).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(2).getCell(21).getStringCellValue == "Den grundläggande utbildningens lärokurs")
    assert(wb.getSheetAt(0).getRow(2).getCell(22).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(23).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(24).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(25).getStringCellValue == "Rämsöönranta 368")
    assert(wb.getSheetAt(0).getRow(2).getCell(26).getStringCellValue == "00100")
    assert(wb.getSheetAt(0).getRow(2).getCell(27).getStringCellValue == "HELSINKI")
    assert(wb.getSheetAt(0).getRow(2).getCell(28) == null)

    assert(wb.getSheetAt(0).getRow(3).getCell(0).getStringCellValue == "Lehto-Testi, Vikke Testi")
    assert(wb.getSheetAt(0).getRow(3).getCell(1).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(2).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(3).getCell(3).getStringCellValue == "1.2.246.562.24.18441866015")
    assert(wb.getSheetAt(0).getRow(3).getCell(4).getStringCellValue == "1.2.246.562.11.00000000000002126102")
    assert(wb.getSheetAt(0).getRow(3).getCell(5).getStringCellValue == "Oppilaitos 3 sv")
    assert(wb.getSheetAt(0).getRow(3).getCell(6).getStringCellValue == "Toimipiste 3 sv")
    assert(wb.getSheetAt(0).getRow(3).getCell(7).getStringCellValue == "Hakukohde 3 SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(8).getNumericCellValue == 1)
    assert(wb.getSheetAt(0).getRow(3).getCell(9).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(10).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(11).getStringCellValue == "Peruuntunut SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(12).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(13).getStringCellValue == "5.6")
    assert(
      wb.getSheetAt(0).getRow(3).getCell(14).getStringCellValue == "Annullerad, besättning av reservplatser har upphört"
    )
    assert(wb.getSheetAt(0).getRow(3).getCell(15).getStringCellValue == "raportti.perunut")
    assert(wb.getSheetAt(0).getRow(3).getCell(16).getStringCellValue == "26.06.2024")
    assert(wb.getSheetAt(0).getRow(3).getCell(17).getStringCellValue == "raportti.lasna_koko_lukuvuosi")
    assert(wb.getSheetAt(0).getRow(3).getCell(18).getStringCellValue == "raportti.yks_mat_ai")
    assert(wb.getSheetAt(0).getRow(3).getCell(19).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(20).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(21).getStringCellValue == "Utbildning utomlands")
    assert(wb.getSheetAt(0).getRow(3).getCell(22).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(23).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(24).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(25).getStringCellValue == "Laholanaukio 834")
    assert(wb.getSheetAt(0).getRow(3).getCell(26).getStringCellValue == "00100")
    assert(wb.getSheetAt(0).getRow(3).getCell(27).getStringCellValue == "HELSINKI")
    assert(wb.getSheetAt(0).getRow(3).getCell(28) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 4)
    assert(wb.getSheetAt(0).getRow(5) == null)
  }

  "createHylkaamisenTaiPeruuntumisenSyyHeadingRow" should "create merged hylkäämisen tai peruuntumisen syy heading cell in the right index on the row" in {
    val wb: XSSFWorkbook                = new XSSFWorkbook()
    val sheet: XSSFSheet                = wb.createSheet()
    val headingCellStyle: XSSFCellStyle = wb.createCellStyle()
    val valintatapajonot = List(
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
    val fieldNames = classOf[KkHakijaWithCombinedNimi].getDeclaredFields.map(_.getName).toList
    ExcelWriter.createHylkaamisenTaiPeruuntumisenSyyHeadingRow(
      wb,
      sheet,
      translations,
      0,
      fieldNames,
      valintatapajonot,
      true
    )

    for (i <- 0 until 12) {
      assert(wb.getSheetAt(0).getRow(0).getCell(i) == null)
    }

    assert(wb.getSheetAt(0).getRow(0).getCell(13).getStringCellValue == "Hylkäämisen tai peruuntumisen syy SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(13).getColumnIndex == 13)

    for (i <- 14 until 27) {
      assert(wb.getSheetAt(0).getRow(0).getCell(i) == null)
    }
  }

  it should "create merged hylkäämisen tai peruuntumisen syy heading cell in the right index on the row when hetu is not shown" in {
    val wb: XSSFWorkbook                = new XSSFWorkbook()
    val sheet: XSSFSheet                = wb.createSheet()
    val headingCellStyle: XSSFCellStyle = wb.createCellStyle()
    val valintatapajonot = List(
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
    val fieldNames = classOf[KkHakijaWithCombinedNimi].getDeclaredFields.map(_.getName).toList
    ExcelWriter.createHylkaamisenTaiPeruuntumisenSyyHeadingRow(
      wb,
      sheet,
      translations,
      0,
      fieldNames,
      valintatapajonot,
      false
    )

    for (i <- 0 until 12) {
      assert(wb.getSheetAt(0).getRow(0).getCell(i) == null)
    }

    assert(wb.getSheetAt(0).getRow(0).getCell(12).getStringCellValue == "Hylkäämisen tai peruuntumisen syy SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(12).getColumnIndex == 12)

    for (i <- 13 until 26) {
      assert(wb.getSheetAt(0).getRow(0).getCell(i) == null)
    }
  }

  val kkHakijaRautiainen: KkHakijaWithCombinedNimi = KkHakijaWithCombinedNimi(
    hakija = "Rautiainen-Testi, Dina Testi",
    hetu = Some("120393-129E"),
    syntymaAika = Some(LocalDate.parse("1993-03-12")),
    kansalaisuus = Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland"),
    oppijanumero = "1.2.246.562.24.30646006111",
    hakemusOid = "1.2.246.562.11.00000000000002179045",
    toimipiste = Map(En -> "Toimipiste 1 en", Fi -> "Toimipiste 1 fi", Sv -> "Toimipiste 1 sv"),
    hakukohteenNimi = Map(En -> "Hakukohde 1 EN", Fi -> "Hakukohde 1", Sv -> "Hakukohde 1 SV"),
    hakukelpoisuus = None,
    prioriteetti = 2,
    valintatieto = Some("HYVAKSYTTY"),
    ehdollisestiHyvaksytty = Some(true),
    valintatiedonPvm = Some(LocalDate.parse("2024-06-13")),
    valintatapajonot = List(),
    vastaanottotieto = Some("PERUNUT"),
    viimVastaanottopaiva = Some(LocalDate.parse("2024-06-26")),
    ensikertalainen = Some(true),
    ilmoittautuminen = Some("LASNA_KOKO_LUKUVUOSI"),
    pohjakoulutus = Some(s"""["pohjakoulutus_yo", "pohjakoulutus_kk"]"""),
    maksuvelvollisuus = Some("not-obligated"),
    hakemusmaksunTila = Some("overdue"),
    julkaisulupa = Some(true),
    markkinointilupa = Some(true),
    sahkoinenViestintalupa = Some(true),
    lahiosoite = Some("Rämsöönranta 368"),
    postinumero = Some("00100"),
    postitoimipaikka = Some("HELSINKI"),
    kotikunta = Map(En -> "Helsinki", Fi -> "Helsinki", Sv -> "Helsingfors"),
    asuinmaa = Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland"),
    puhelinnumero = Some("050 64292261"),
    sahkoposti = Some("hakija-33919666@oph.fi"),
    arvosanat = Map("A" -> "E", "M" -> "E", "BB" -> "M", "EA" -> "C", "FY" -> "E", "KE" -> "L")
  )

  val kkHakijatRautiainenWithValintatapajonot: KkHakijaWithCombinedNimi = kkHakijaRautiainen.copy(
    valintatiedonPvm = None,
    pohjakoulutus = None,
    maksuvelvollisuus = None,
    valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1707384694164-3621431542682802084",
        valintatapajononNimi = "Todistusvalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Fi -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Sv -> "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 1."
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "17096467457545361875995955973551",
        valintatapajononNimi = "Todistusvalinta (YO)",
        valinnanTila = "HYVAKSYTTY",
        valinnanTilanKuvaus = Map(
          En -> null,
          Fi -> null,
          Sv -> null
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1707384681516-2842344525807969324",
        valintatapajononNimi = "Todistusvalintajono ensikertalaisille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Fi -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Sv -> "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 2."
        )
      )
    )
  )

  val kkHakijaLehto: KkHakijaWithCombinedNimi = KkHakijaWithCombinedNimi(
    hakija = "Lehto-Testi, Vikke Testi",
    hetu = Some("04041990-345K"),
    syntymaAika = Some(LocalDate.parse("1990-04-04")),
    kansalaisuus = Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland"),
    oppijanumero = "1.2.246.562.24.18441866015",
    hakemusOid = "1.2.246.562.11.00000000000002126102",
    toimipiste = Map(En -> "Toimipiste 2 en", Fi -> "Toimipiste 2 fi", Sv -> "Toimipiste 2 sv"),
    hakukohteenNimi = Map(En -> "Hakukohde 2 EN", Fi -> "Hakukohde 2", Sv -> "Hakukohde 2 SV"),
    hakukelpoisuus = Some("uneligible"),
    prioriteetti = 1,
    valintatieto = Some("HYLATTY"),
    ehdollisestiHyvaksytty = None,
    valintatiedonPvm = Some(LocalDate.parse("2024-06-11")),
    valintatapajonot = List(),
    vastaanottotieto = None,
    viimVastaanottopaiva = Some(LocalDate.parse("2024-06-26")),
    ensikertalainen = Some(false),
    ilmoittautuminen = None,
    pohjakoulutus = None,
    maksuvelvollisuus = Some("unreviewed"),
    hakemusmaksunTila = None,
    julkaisulupa = Some(true),
    markkinointilupa = Some(false),
    sahkoinenViestintalupa = Some(true),
    lahiosoite = Some("Laholanaukio 834"),
    postinumero = Some("15700"),
    postitoimipaikka = Some("LAHTI"),
    kotikunta = Map(En -> "Lahti", Fi -> "Lahti", Sv -> "Lahtis"),
    asuinmaa = Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland"),
    puhelinnumero = Some("050 64293345"),
    sahkoposti = Some("hakija-33919611@oph.fi"),
    arvosanat = Map("M" -> "M", "O" -> "L", "CA" -> "M", "EA" -> "M", "PS" -> "E", "YH" -> "E")
  )

  val kkHakijaLehtoWithValintatapajonot: KkHakijaWithCombinedNimi = kkHakijaLehto.copy(
    julkaisulupa = Some(false),
    markkinointilupa = Some(true),
    valintatapajonot = List(
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
  )

  "writeKkHakijatRaportti" should "return excel with two result rows for kk-hakijat with hetu, postiosoite and arvosanat" in {
    val kkHakijatResult =
      Vector(
        kkHakijaRautiainen.copy(ehdollisestiHyvaksytty = Some(false)),
        kkHakijaLehto.copy(
          hakukelpoisuus = Some("eligible"),
          valintatieto = None,
          valintatiedonPvm = None,
          maksuvelvollisuus = None,
          puhelinnumero = None
        )
      )

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(true),
        maybeNaytaHetu = Some(true),
        maybeNaytaPostiosoite = Some(true)
      )

    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Hakija SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "raportti.hetu")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "raportti.syntymaAika")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "Kansalaisuus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "Hakukelpoisuus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(9).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(0).getCell(10).getStringCellValue == "raportti.valintatieto")
    assert(wb.getSheetAt(0).getRow(0).getCell(11).getStringCellValue == "raportti.ehdollisestiHyvaksytty")
    assert(wb.getSheetAt(0).getRow(0).getCell(12).getStringCellValue == "Valintatiedon päivämäärä SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(13).getStringCellValue == "raportti.vastaanottotieto")
    assert(wb.getSheetAt(0).getRow(0).getCell(14).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(0).getCell(15).getStringCellValue == "raportti.ensikertalainen")
    assert(wb.getSheetAt(0).getRow(0).getCell(16).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(0).getCell(17).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(18).getStringCellValue == "raportti.maksuvelvollisuus")
    assert(wb.getSheetAt(0).getRow(0).getCell(19).getStringCellValue == "raportti.hakemusmaksunTila")
    assert(wb.getSheetAt(0).getRow(0).getCell(20).getStringCellValue == "raportti.julkaisulupa")
    assert(wb.getSheetAt(0).getRow(0).getCell(21).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(22).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(0).getCell(23).getStringCellValue == "raportti.lahiosoite")
    assert(wb.getSheetAt(0).getRow(0).getCell(24).getStringCellValue == "raportti.postinumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(25).getStringCellValue == "raportti.postitoimipaikka")
    assert(wb.getSheetAt(0).getRow(0).getCell(26).getStringCellValue == "raportti.kotikunta")
    assert(wb.getSheetAt(0).getRow(0).getCell(27).getStringCellValue == "raportti.asuinmaa")
    assert(wb.getSheetAt(0).getRow(0).getCell(28).getStringCellValue == "Puhelinnumero SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(29).getStringCellValue == "raportti.sahkoposti")
    assert(wb.getSheetAt(0).getRow(0).getCell(30).getStringCellValue == "A")
    assert(wb.getSheetAt(0).getRow(0).getCell(31).getStringCellValue == "BB")
    assert(wb.getSheetAt(0).getRow(0).getCell(32).getStringCellValue == "CA")
    assert(wb.getSheetAt(0).getRow(0).getCell(33).getStringCellValue == "EA")
    assert(wb.getSheetAt(0).getRow(0).getCell(34).getStringCellValue == "FY")
    assert(wb.getSheetAt(0).getRow(0).getCell(35).getStringCellValue == "KE")
    assert(wb.getSheetAt(0).getRow(0).getCell(36).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(0).getCell(37).getStringCellValue == "O")
    assert(wb.getSheetAt(0).getRow(0).getCell(38).getStringCellValue == "PS")
    assert(wb.getSheetAt(0).getRow(0).getCell(39).getStringCellValue == "YH")
    assert(wb.getSheetAt(0).getRow(0).getCell(40) == null)

    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Rautiainen-Testi, Dina Testi")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "120393-129E")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "12.03.1993")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "1.2.246.562.11.00000000000002179045")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "Toimipiste 1 sv")
    assert(wb.getSheetAt(0).getRow(1).getCell(7).getStringCellValue == "Hakukohde 1 SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(8).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(9).getNumericCellValue == 2)
    assert(wb.getSheetAt(0).getRow(1).getCell(10).getStringCellValue == "Hyvaksytty SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(11).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(12).getStringCellValue == "13.06.2024")
    assert(wb.getSheetAt(0).getRow(1).getCell(13).getStringCellValue == "raportti.perunut")
    assert(wb.getSheetAt(0).getRow(1).getCell(14).getStringCellValue == "26.06.2024")
    assert(wb.getSheetAt(0).getRow(1).getCell(15).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(16).getStringCellValue == "raportti.lasna_koko_lukuvuosi")
    assert(wb.getSheetAt(0).getRow(1).getCell(17).getStringCellValue == s"""["pohjakoulutus_yo", "pohjakoulutus_kk"]""")
    assert(wb.getSheetAt(0).getRow(1).getCell(18).getStringCellValue == "Ei velvollinen")
    assert(wb.getSheetAt(0).getRow(1).getCell(19).getStringCellValue == "raportti.overdue")
    assert(wb.getSheetAt(0).getRow(1).getCell(20).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(21).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(22).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(23).getStringCellValue == "Rämsöönranta 368")
    assert(wb.getSheetAt(0).getRow(1).getCell(24).getStringCellValue == "00100")
    assert(wb.getSheetAt(0).getRow(1).getCell(25).getStringCellValue == "HELSINKI")
    assert(wb.getSheetAt(0).getRow(1).getCell(26).getStringCellValue == "Helsingfors")
    assert(wb.getSheetAt(0).getRow(1).getCell(27).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(1).getCell(28).getStringCellValue == "050 64292261")
    assert(wb.getSheetAt(0).getRow(1).getCell(29).getStringCellValue == "hakija-33919666@oph.fi")
    assert(wb.getSheetAt(0).getRow(1).getCell(30).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(1).getCell(31).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(1).getCell(32).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(33).getStringCellValue == "C")
    assert(wb.getSheetAt(0).getRow(1).getCell(34).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(1).getCell(35).getStringCellValue == "L")
    assert(wb.getSheetAt(0).getRow(1).getCell(36).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(1).getCell(37).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(38).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(39).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(40) == null)

    assert(wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue == "Lehto-Testi, Vikke Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(1).getStringCellValue == "04041990-345K")
    assert(wb.getSheetAt(0).getRow(2).getCell(2).getStringCellValue == "04.04.1990")
    assert(wb.getSheetAt(0).getRow(2).getCell(3).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(4).getStringCellValue == "1.2.246.562.24.18441866015")
    assert(wb.getSheetAt(0).getRow(2).getCell(5).getStringCellValue == "1.2.246.562.11.00000000000002126102")
    assert(wb.getSheetAt(0).getRow(2).getCell(6).getStringCellValue == "Toimipiste 2 sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(7).getStringCellValue == "Hakukohde 2 SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(8).getStringCellValue == "hakukelpoinen SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(9).getNumericCellValue == 1)
    assert(wb.getSheetAt(0).getRow(2).getCell(10).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(11).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(12).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(13).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(14).getStringCellValue == "26.06.2024")
    assert(wb.getSheetAt(0).getRow(2).getCell(15).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(2).getCell(16).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(17).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(18).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(19).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(20).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(21).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(2).getCell(22).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(23).getStringCellValue == "Laholanaukio 834")
    assert(wb.getSheetAt(0).getRow(2).getCell(24).getStringCellValue == "15700")
    assert(wb.getSheetAt(0).getRow(2).getCell(25).getStringCellValue == "LAHTI")
    assert(wb.getSheetAt(0).getRow(2).getCell(26).getStringCellValue == "Lahtis")
    assert(wb.getSheetAt(0).getRow(2).getCell(27).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(28).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(29).getStringCellValue == "hakija-33919611@oph.fi")
    assert(wb.getSheetAt(0).getRow(2).getCell(30).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(31).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(32).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(2).getCell(33).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(2).getCell(34).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(35).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(36).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(2).getCell(37).getStringCellValue == "L")
    assert(wb.getSheetAt(0).getRow(2).getCell(38).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(2).getCell(39).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(2).getCell(40) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 3)
    assert(wb.getSheetAt(0).getRow(3) == null)
  }

  it should "return excel with two result rows for kk-hakijat without hetu and postiosoite and with arvosanat" in {
    val kkHakijatResult =
      Vector(
        kkHakijaRautiainen.copy(
          hakukelpoisuus = Some("conditionally-eligible"),
          valintatiedonPvm = Some(LocalDate.parse("2024-06-05")),
          maksuvelvollisuus = Some("obligated")
        ),
        kkHakijaLehto.copy(
          markkinointilupa = Some(true),
          puhelinnumero = Some("050 64293345"),
          sahkoposti = None
        )
      )

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(true),
        maybeNaytaHetu = Some(false),
        maybeNaytaPostiosoite = Some(false)
      )

    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Hakija SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "raportti.syntymaAika")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "Kansalaisuus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "Hakukelpoisuus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(0).getCell(9).getStringCellValue == "raportti.valintatieto")
    assert(wb.getSheetAt(0).getRow(0).getCell(10).getStringCellValue == "raportti.ehdollisestiHyvaksytty")
    assert(wb.getSheetAt(0).getRow(0).getCell(11).getStringCellValue == "Valintatiedon päivämäärä SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(12).getStringCellValue == "raportti.vastaanottotieto")
    assert(wb.getSheetAt(0).getRow(0).getCell(13).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(0).getCell(14).getStringCellValue == "raportti.ensikertalainen")
    assert(wb.getSheetAt(0).getRow(0).getCell(15).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(0).getCell(16).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(17).getStringCellValue == "raportti.maksuvelvollisuus")
    assert(wb.getSheetAt(0).getRow(0).getCell(18).getStringCellValue == "raportti.hakemusmaksunTila")
    assert(wb.getSheetAt(0).getRow(0).getCell(19).getStringCellValue == "raportti.julkaisulupa")
    assert(wb.getSheetAt(0).getRow(0).getCell(20).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(21).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(0).getCell(22).getStringCellValue == "raportti.kotikunta")
    assert(wb.getSheetAt(0).getRow(0).getCell(23).getStringCellValue == "raportti.asuinmaa")
    assert(wb.getSheetAt(0).getRow(0).getCell(24).getStringCellValue == "Puhelinnumero SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(25).getStringCellValue == "raportti.sahkoposti")
    assert(wb.getSheetAt(0).getRow(0).getCell(26).getStringCellValue == "A")
    assert(wb.getSheetAt(0).getRow(0).getCell(27).getStringCellValue == "BB")
    assert(wb.getSheetAt(0).getRow(0).getCell(28).getStringCellValue == "CA")
    assert(wb.getSheetAt(0).getRow(0).getCell(29).getStringCellValue == "EA")
    assert(wb.getSheetAt(0).getRow(0).getCell(30).getStringCellValue == "FY")
    assert(wb.getSheetAt(0).getRow(0).getCell(31).getStringCellValue == "KE")
    assert(wb.getSheetAt(0).getRow(0).getCell(32).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(0).getCell(33).getStringCellValue == "O")
    assert(wb.getSheetAt(0).getRow(0).getCell(34).getStringCellValue == "PS")
    assert(wb.getSheetAt(0).getRow(0).getCell(35).getStringCellValue == "YH")
    assert(wb.getSheetAt(0).getRow(0).getCell(36) == null)

    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Rautiainen-Testi, Dina Testi")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "12.03.1993")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "1.2.246.562.11.00000000000002179045")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "Toimipiste 1 sv")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "Hakukohde 1 SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(7).getStringCellValue == "raportti.conditionally-eligible")
    assert(wb.getSheetAt(0).getRow(1).getCell(8).getNumericCellValue == 2)
    assert(wb.getSheetAt(0).getRow(1).getCell(9).getStringCellValue == "Hyvaksytty SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(10).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(11).getStringCellValue == "05.06.2024")
    assert(wb.getSheetAt(0).getRow(1).getCell(12).getStringCellValue == "raportti.perunut")
    assert(wb.getSheetAt(0).getRow(1).getCell(13).getStringCellValue == "26.06.2024")
    assert(wb.getSheetAt(0).getRow(1).getCell(14).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(15).getStringCellValue == "raportti.lasna_koko_lukuvuosi")
    assert(wb.getSheetAt(0).getRow(1).getCell(16).getStringCellValue == s"""["pohjakoulutus_yo", "pohjakoulutus_kk"]""")
    assert(wb.getSheetAt(0).getRow(1).getCell(17).getStringCellValue == "Velvollinen")
    assert(wb.getSheetAt(0).getRow(1).getCell(18).getStringCellValue == "raportti.overdue")
    assert(wb.getSheetAt(0).getRow(1).getCell(19).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(20).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(21).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(22).getStringCellValue == "Helsingfors")
    assert(wb.getSheetAt(0).getRow(1).getCell(23).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(1).getCell(24).getStringCellValue == "050 64292261")
    assert(wb.getSheetAt(0).getRow(1).getCell(25).getStringCellValue == "hakija-33919666@oph.fi")
    assert(wb.getSheetAt(0).getRow(1).getCell(26).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(1).getCell(27).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(1).getCell(28).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(29).getStringCellValue == "C")
    assert(wb.getSheetAt(0).getRow(1).getCell(30).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(1).getCell(31).getStringCellValue == "L")
    assert(wb.getSheetAt(0).getRow(1).getCell(32).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(1).getCell(33).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(34).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(35).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(36) == null)

    assert(wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue == "Lehto-Testi, Vikke Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(1).getStringCellValue == "04.04.1990")
    assert(wb.getSheetAt(0).getRow(2).getCell(2).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(3).getStringCellValue == "1.2.246.562.24.18441866015")
    assert(wb.getSheetAt(0).getRow(2).getCell(4).getStringCellValue == "1.2.246.562.11.00000000000002126102")
    assert(wb.getSheetAt(0).getRow(2).getCell(5).getStringCellValue == "Toimipiste 2 sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(6).getStringCellValue == "Hakukohde 2 SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(7).getStringCellValue == "raportti.uneligible")
    assert(wb.getSheetAt(0).getRow(2).getCell(8).getNumericCellValue == 1)
    assert(wb.getSheetAt(0).getRow(2).getCell(9).getStringCellValue == "Hylatty SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(10).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(11).getStringCellValue == "11.06.2024")
    assert(wb.getSheetAt(0).getRow(2).getCell(12).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(13).getStringCellValue == "26.06.2024")
    assert(wb.getSheetAt(0).getRow(2).getCell(14).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(2).getCell(15).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(16).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(17).getStringCellValue == "Tarkastamatta SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(18).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(19).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(20).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(21).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(22).getStringCellValue == "Lahtis")
    assert(wb.getSheetAt(0).getRow(2).getCell(23).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(24).getStringCellValue == "050 64293345")
    assert(wb.getSheetAt(0).getRow(2).getCell(25).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(26).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(27).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(28).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(2).getCell(29).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(2).getCell(30).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(31).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(32).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(2).getCell(33).getStringCellValue == "L")
    assert(wb.getSheetAt(0).getRow(2).getCell(34).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(2).getCell(35).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(2).getCell(36) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 3)
    assert(wb.getSheetAt(0).getRow(4) == null)
  }

  it should "return excel with one result row for kk-hakijat with hetu and arvosanat but without postiosoite" in {
    val kkHakijatResult =
      Vector(
        kkHakijaRautiainen.copy(
          valintatiedonPvm = None,
          pohjakoulutus = None,
          maksuvelvollisuus = None,
          valintatapajonot = List(
            Valintatapajono(
              valintatapajonoOid = "1709304116443-4815957697640331820",
              valintatapajononNimi = "Lukiokoulutus",
              valinnanTila = "PERUUNTUNUT",
              valinnanTilanKuvaus = Map(
                En -> "Cancelled, accepted for a study place with higher priority",
                Fi -> "Peruuntunut, hyväksytty ylemmälle hakutoiveelle",
                Sv -> "Annullerad, godkänt till ansökningsmål med högre prioritet"
              )
            )
          )
        )
      )

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(true),
        maybeNaytaHetu = Some(true),
        maybeNaytaPostiosoite = Some(false)
      )

    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(0).getCell(0) == null)
    assert(wb.getSheetAt(0).getRow(0).getCell(13).getStringCellValue == "Hylkäämisen tai peruuntumisen syy SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Hakija SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "raportti.hetu")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "raportti.syntymaAika")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "Kansalaisuus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(7).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(8).getStringCellValue == "Hakukelpoisuus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(9).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(1).getCell(10).getStringCellValue == "raportti.valintatieto")
    assert(wb.getSheetAt(0).getRow(1).getCell(11).getStringCellValue == "raportti.ehdollisestiHyvaksytty")
    assert(wb.getSheetAt(0).getRow(1).getCell(12).getStringCellValue == "Valintatiedon päivämäärä SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(13).getStringCellValue == "Lukiokoulutus")
    assert(wb.getSheetAt(0).getRow(1).getCell(14).getStringCellValue == "raportti.vastaanottotieto")
    assert(wb.getSheetAt(0).getRow(1).getCell(15).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(1).getCell(16).getStringCellValue == "raportti.ensikertalainen")
    assert(wb.getSheetAt(0).getRow(1).getCell(17).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(1).getCell(18).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(19).getStringCellValue == "raportti.maksuvelvollisuus")
    assert(wb.getSheetAt(0).getRow(1).getCell(20).getStringCellValue == "raportti.hakemusmaksunTila")
    assert(wb.getSheetAt(0).getRow(1).getCell(21).getStringCellValue == "raportti.julkaisulupa")
    assert(wb.getSheetAt(0).getRow(1).getCell(22).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(23).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(1).getCell(24).getStringCellValue == "raportti.kotikunta")
    assert(wb.getSheetAt(0).getRow(1).getCell(25).getStringCellValue == "raportti.asuinmaa")
    assert(wb.getSheetAt(0).getRow(1).getCell(26).getStringCellValue == "Puhelinnumero SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(27).getStringCellValue == "raportti.sahkoposti")
    assert(wb.getSheetAt(0).getRow(1).getCell(28).getStringCellValue == "A")
    assert(wb.getSheetAt(0).getRow(1).getCell(29).getStringCellValue == "BB")
    assert(wb.getSheetAt(0).getRow(1).getCell(30).getStringCellValue == "EA")
    assert(wb.getSheetAt(0).getRow(1).getCell(31).getStringCellValue == "FY")
    assert(wb.getSheetAt(0).getRow(1).getCell(32).getStringCellValue == "KE")
    assert(wb.getSheetAt(0).getRow(1).getCell(33).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(1).getCell(34) == null)

    assert(wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue == "Rautiainen-Testi, Dina Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(1).getStringCellValue == "120393-129E")
    assert(wb.getSheetAt(0).getRow(2).getCell(2).getStringCellValue == "12.03.1993")
    assert(wb.getSheetAt(0).getRow(2).getCell(3).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(4).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(2).getCell(5).getStringCellValue == "1.2.246.562.11.00000000000002179045")
    assert(wb.getSheetAt(0).getRow(2).getCell(6).getStringCellValue == "Toimipiste 1 sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(7).getStringCellValue == "Hakukohde 1 SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(8).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(9).getNumericCellValue == 2)
    assert(wb.getSheetAt(0).getRow(2).getCell(10).getStringCellValue == "Hyvaksytty SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(11).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(12).getStringCellValue == "-")
    assert(
      wb.getSheetAt(0)
        .getRow(2)
        .getCell(13)
        .getStringCellValue == "Annullerad, godkänt till ansökningsmål med högre prioritet"
    )
    assert(wb.getSheetAt(0).getRow(2).getCell(14).getStringCellValue == "raportti.perunut")
    assert(wb.getSheetAt(0).getRow(2).getCell(15).getStringCellValue == "26.06.2024")
    assert(wb.getSheetAt(0).getRow(2).getCell(16).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(17).getStringCellValue == "raportti.lasna_koko_lukuvuosi")
    assert(wb.getSheetAt(0).getRow(2).getCell(18).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(19).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(20).getStringCellValue == "raportti.overdue")
    assert(wb.getSheetAt(0).getRow(2).getCell(21).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(22).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(23).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(24).getStringCellValue == "Helsingfors")
    assert(wb.getSheetAt(0).getRow(2).getCell(25).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(26).getStringCellValue == "050 64292261")
    assert(wb.getSheetAt(0).getRow(2).getCell(27).getStringCellValue == "hakija-33919666@oph.fi")
    assert(wb.getSheetAt(0).getRow(2).getCell(28).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(2).getCell(29).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(2).getCell(30).getStringCellValue == "C")
    assert(wb.getSheetAt(0).getRow(2).getCell(31).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(2).getCell(32).getStringCellValue == "L")
    assert(wb.getSheetAt(0).getRow(2).getCell(33).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(2).getCell(34) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 3)
    assert(wb.getSheetAt(0).getRow(4) == null)
  }

  it should "return excel with two result rows without hetu and with postiosoite and arvosanat for kk-hakijat that have several valintatapajono" in {
    val kkHakijatResult =
      Vector(
        kkHakijatRautiainenWithValintatapajonot,
        kkHakijaLehtoWithValintatapajonot
      )

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(true),
        maybeNaytaHetu = Some(false),
        maybeNaytaPostiosoite = Some(true)
      )

    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(0).getCell(0) == null)
    assert(wb.getSheetAt(0).getRow(0).getCell(12).getStringCellValue == "Hylkäämisen tai peruuntumisen syy SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Hakija SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "raportti.syntymaAika")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "Kansalaisuus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(7).getStringCellValue == "Hakukelpoisuus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(8).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(1).getCell(9).getStringCellValue == "raportti.valintatieto")
    assert(wb.getSheetAt(0).getRow(1).getCell(10).getStringCellValue == "raportti.ehdollisestiHyvaksytty")
    assert(wb.getSheetAt(0).getRow(1).getCell(11).getStringCellValue == "Valintatiedon päivämäärä SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(12).getStringCellValue == "Koevalintajono kaikille hakijoille")
    assert(wb.getSheetAt(0).getRow(1).getCell(13).getStringCellValue == "Todistusvalinta (YO)")
    assert(
      wb.getSheetAt(0).getRow(1).getCell(14).getStringCellValue == "Todistusvalintajono ensikertalaisille hakijoille"
    )
    assert(
      wb.getSheetAt(0).getRow(1).getCell(15).getStringCellValue == "Todistusvalintajono ensikertalaisille hakijoille"
    )
    assert(wb.getSheetAt(0).getRow(1).getCell(16).getStringCellValue == "Todistusvalintajono kaikille hakijoille")
    assert(wb.getSheetAt(0).getRow(1).getCell(17).getStringCellValue == "raportti.vastaanottotieto")
    assert(wb.getSheetAt(0).getRow(1).getCell(18).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(1).getCell(19).getStringCellValue == "raportti.ensikertalainen")
    assert(wb.getSheetAt(0).getRow(1).getCell(20).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(1).getCell(21).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(22).getStringCellValue == "raportti.maksuvelvollisuus")
    assert(wb.getSheetAt(0).getRow(1).getCell(23).getStringCellValue == "raportti.hakemusmaksunTila")
    assert(wb.getSheetAt(0).getRow(1).getCell(24).getStringCellValue == "raportti.julkaisulupa")
    assert(wb.getSheetAt(0).getRow(1).getCell(25).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(26).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(1).getCell(27).getStringCellValue == "raportti.lahiosoite")
    assert(wb.getSheetAt(0).getRow(1).getCell(28).getStringCellValue == "raportti.postinumero")
    assert(wb.getSheetAt(0).getRow(1).getCell(29).getStringCellValue == "raportti.postitoimipaikka")
    assert(wb.getSheetAt(0).getRow(1).getCell(30).getStringCellValue == "raportti.kotikunta")
    assert(wb.getSheetAt(0).getRow(1).getCell(31).getStringCellValue == "raportti.asuinmaa")
    assert(wb.getSheetAt(0).getRow(1).getCell(32).getStringCellValue == "Puhelinnumero SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(33).getStringCellValue == "raportti.sahkoposti")
    assert(wb.getSheetAt(0).getRow(1).getCell(34).getStringCellValue == "A")
    assert(wb.getSheetAt(0).getRow(1).getCell(35).getStringCellValue == "BB")
    assert(wb.getSheetAt(0).getRow(1).getCell(36).getStringCellValue == "CA")
    assert(wb.getSheetAt(0).getRow(1).getCell(37).getStringCellValue == "EA")
    assert(wb.getSheetAt(0).getRow(1).getCell(38).getStringCellValue == "FY")
    assert(wb.getSheetAt(0).getRow(1).getCell(39).getStringCellValue == "KE")
    assert(wb.getSheetAt(0).getRow(1).getCell(40).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(1).getCell(41).getStringCellValue == "O")
    assert(wb.getSheetAt(0).getRow(1).getCell(42).getStringCellValue == "PS")
    assert(wb.getSheetAt(0).getRow(1).getCell(43).getStringCellValue == "YH")
    assert(wb.getSheetAt(0).getRow(1).getCell(44) == null)

    assert(wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue == "Rautiainen-Testi, Dina Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(1).getStringCellValue == "12.03.1993")
    assert(wb.getSheetAt(0).getRow(2).getCell(2).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(3).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(2).getCell(4).getStringCellValue == "1.2.246.562.11.00000000000002179045")
    assert(wb.getSheetAt(0).getRow(2).getCell(5).getStringCellValue == "Toimipiste 1 sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(6).getStringCellValue == "Hakukohde 1 SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(7).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(8).getNumericCellValue == 2)
    assert(wb.getSheetAt(0).getRow(2).getCell(9).getStringCellValue == "Hyvaksytty SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(10).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(11).getStringCellValue == "-")
    // Valintatapajonosarakkeet alkaa
    // Koevalintajono kaikille hakijoille
    assert(wb.getSheetAt(0).getRow(2).getCell(12).getStringCellValue == "-")
    // Todistusvalinta (YO)
    assert(wb.getSheetAt(0).getRow(2).getCell(13).getStringCellValue == "-")
    //Todistusvalintajono ensikertalaisille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(2)
        .getCell(14)
        .getStringCellValue == "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 2."
    )
    // Todistusvalintajono ensikertalaisille hakijoille
    assert(
      wb.getSheetAt(0).getRow(2).getCell(15).getStringCellValue == "-"
    )
    // Todistusvalintajono kaikille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(2)
        .getCell(16)
        .getStringCellValue == "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 1."
    )
    // Valintatapajonosarakkeet loppuu
    assert(wb.getSheetAt(0).getRow(2).getCell(17).getStringCellValue == "raportti.perunut")
    assert(wb.getSheetAt(0).getRow(2).getCell(18).getStringCellValue == "26.06.2024")
    assert(wb.getSheetAt(0).getRow(2).getCell(19).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(20).getStringCellValue == "raportti.lasna_koko_lukuvuosi")
    assert(wb.getSheetAt(0).getRow(2).getCell(21).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(22).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(23).getStringCellValue == "raportti.overdue")
    assert(wb.getSheetAt(0).getRow(2).getCell(24).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(25).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(26).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(27).getStringCellValue == "Rämsöönranta 368")
    assert(wb.getSheetAt(0).getRow(2).getCell(28).getStringCellValue == "00100")
    assert(wb.getSheetAt(0).getRow(2).getCell(29).getStringCellValue == "HELSINKI")
    assert(wb.getSheetAt(0).getRow(2).getCell(30).getStringCellValue == "Helsingfors")
    assert(wb.getSheetAt(0).getRow(2).getCell(31).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(32).getStringCellValue == "050 64292261")
    assert(wb.getSheetAt(0).getRow(2).getCell(33).getStringCellValue == "hakija-33919666@oph.fi")
    assert(wb.getSheetAt(0).getRow(2).getCell(34).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(2).getCell(35).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(2).getCell(36).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(37).getStringCellValue == "C")
    assert(wb.getSheetAt(0).getRow(2).getCell(38).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(2).getCell(39).getStringCellValue == "L")
    assert(wb.getSheetAt(0).getRow(2).getCell(40).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(2).getCell(41).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(42).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(43).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(44) == null)

    assert(wb.getSheetAt(0).getRow(3).getCell(0).getStringCellValue == "Lehto-Testi, Vikke Testi")
    assert(wb.getSheetAt(0).getRow(3).getCell(1).getStringCellValue == "04.04.1990")
    assert(wb.getSheetAt(0).getRow(3).getCell(2).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(3).getCell(3).getStringCellValue == "1.2.246.562.24.18441866015")
    assert(wb.getSheetAt(0).getRow(3).getCell(4).getStringCellValue == "1.2.246.562.11.00000000000002126102")
    assert(wb.getSheetAt(0).getRow(3).getCell(5).getStringCellValue == "Toimipiste 2 sv")
    assert(wb.getSheetAt(0).getRow(3).getCell(6).getStringCellValue == "Hakukohde 2 SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(7).getStringCellValue == "raportti.uneligible")
    assert(wb.getSheetAt(0).getRow(3).getCell(8).getNumericCellValue == 1)
    assert(wb.getSheetAt(0).getRow(3).getCell(9).getStringCellValue == "Hylatty SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(10).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(11).getStringCellValue == "11.06.2024")
    // Valintatapajonosarakkeet alkaa
    // Koevalintajono kaikille hakijoille
    assert(wb.getSheetAt(0).getRow(3).getCell(12).getStringCellValue == "Et osallistunut valintakokeeseen SV")
    // Todistusvalinta (YO)
    assert(wb.getSheetAt(0).getRow(3).getCell(13).getStringCellValue == "-")
    //Todistusvalintajono ensikertalaisille hakijoille - 1707384681516-2842344525807969324
    assert(wb.getSheetAt(0).getRow(3).getCell(14).getStringCellValue == "-")
    // Todistusvalintajono ensikertalaisille hakijoille - 1704199256878262657431481297336
    assert(wb.getSheetAt(0).getRow(3).getCell(15).getStringCellValue == "Du är inte en förstagångssökande.")
    // Todistusvalintajono kaikille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(3)
        .getCell(16)
        .getStringCellValue == "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt."
    )
    // Valintatapajonosarakkeet loppuu
    assert(wb.getSheetAt(0).getRow(3).getCell(17).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(18).getStringCellValue == "26.06.2024")
    assert(wb.getSheetAt(0).getRow(3).getCell(19).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(20).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(21).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(22).getStringCellValue == "Tarkastamatta SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(23).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(24).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(25).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(26).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(27).getStringCellValue == "Laholanaukio 834")
    assert(wb.getSheetAt(0).getRow(3).getCell(28).getStringCellValue == "15700")
    assert(wb.getSheetAt(0).getRow(3).getCell(29).getStringCellValue == "LAHTI")
    assert(wb.getSheetAt(0).getRow(3).getCell(30).getStringCellValue == "Lahtis")
    assert(wb.getSheetAt(0).getRow(3).getCell(31).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(3).getCell(32).getStringCellValue == "050 64293345")
    assert(wb.getSheetAt(0).getRow(3).getCell(33).getStringCellValue == "hakija-33919611@oph.fi")
    assert(wb.getSheetAt(0).getRow(3).getCell(34).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(35).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(36).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(3).getCell(37).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(3).getCell(38).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(39).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(40).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(3).getCell(41).getStringCellValue == "L")
    assert(wb.getSheetAt(0).getRow(3).getCell(42).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(3).getCell(43).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(3).getCell(44) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 4)
    assert(wb.getSheetAt(0).getRow(4) == null)
  }

  it should "return excel with arvosanat in their right place without hetu and postiosoite and several valintatapajono" in {
    val kkHakijatResult = Vector(kkHakijatRautiainenWithValintatapajonot, kkHakijaLehtoWithValintatapajonot)

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(true),
        maybeNaytaHetu = Some(false),
        maybeNaytaPostiosoite = Some(false)
      )

    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(0).getCell(0) == null)
    assert(wb.getSheetAt(0).getRow(0).getCell(12).getStringCellValue == "Hylkäämisen tai peruuntumisen syy SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Hakija SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "raportti.syntymaAika")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "Kansalaisuus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(7).getStringCellValue == "Hakukelpoisuus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(8).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(1).getCell(9).getStringCellValue == "raportti.valintatieto")
    assert(wb.getSheetAt(0).getRow(1).getCell(10).getStringCellValue == "raportti.ehdollisestiHyvaksytty")
    assert(wb.getSheetAt(0).getRow(1).getCell(11).getStringCellValue == "Valintatiedon päivämäärä SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(12).getStringCellValue == "Koevalintajono kaikille hakijoille")
    assert(wb.getSheetAt(0).getRow(1).getCell(13).getStringCellValue == "Todistusvalinta (YO)")
    assert(
      wb.getSheetAt(0).getRow(1).getCell(14).getStringCellValue == "Todistusvalintajono ensikertalaisille hakijoille"
    )
    assert(
      wb.getSheetAt(0).getRow(1).getCell(15).getStringCellValue == "Todistusvalintajono ensikertalaisille hakijoille"
    )
    assert(wb.getSheetAt(0).getRow(1).getCell(16).getStringCellValue == "Todistusvalintajono kaikille hakijoille")
    assert(wb.getSheetAt(0).getRow(1).getCell(17).getStringCellValue == "raportti.vastaanottotieto")
    assert(wb.getSheetAt(0).getRow(1).getCell(18).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(1).getCell(19).getStringCellValue == "raportti.ensikertalainen")
    assert(wb.getSheetAt(0).getRow(1).getCell(20).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(1).getCell(21).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(22).getStringCellValue == "raportti.maksuvelvollisuus")
    assert(wb.getSheetAt(0).getRow(1).getCell(23).getStringCellValue == "raportti.hakemusmaksunTila")
    assert(wb.getSheetAt(0).getRow(1).getCell(24).getStringCellValue == "raportti.julkaisulupa")
    assert(wb.getSheetAt(0).getRow(1).getCell(25).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(26).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(1).getCell(27).getStringCellValue == "raportti.kotikunta")
    assert(wb.getSheetAt(0).getRow(1).getCell(28).getStringCellValue == "raportti.asuinmaa")
    assert(wb.getSheetAt(0).getRow(1).getCell(29).getStringCellValue == "Puhelinnumero SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(30).getStringCellValue == "raportti.sahkoposti")
    assert(wb.getSheetAt(0).getRow(1).getCell(31).getStringCellValue == "A")
    assert(wb.getSheetAt(0).getRow(1).getCell(32).getStringCellValue == "BB")
    assert(wb.getSheetAt(0).getRow(1).getCell(33).getStringCellValue == "CA")
    assert(wb.getSheetAt(0).getRow(1).getCell(34).getStringCellValue == "EA")
    assert(wb.getSheetAt(0).getRow(1).getCell(35).getStringCellValue == "FY")
    assert(wb.getSheetAt(0).getRow(1).getCell(36).getStringCellValue == "KE")
    assert(wb.getSheetAt(0).getRow(1).getCell(37).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(1).getCell(38).getStringCellValue == "O")
    assert(wb.getSheetAt(0).getRow(1).getCell(39).getStringCellValue == "PS")
    assert(wb.getSheetAt(0).getRow(1).getCell(40).getStringCellValue == "YH")
    assert(wb.getSheetAt(0).getRow(1).getCell(41) == null)

    assert(wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue == "Rautiainen-Testi, Dina Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(1).getStringCellValue == "12.03.1993")
    assert(wb.getSheetAt(0).getRow(2).getCell(2).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(3).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(2).getCell(4).getStringCellValue == "1.2.246.562.11.00000000000002179045")
    assert(wb.getSheetAt(0).getRow(2).getCell(5).getStringCellValue == "Toimipiste 1 sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(6).getStringCellValue == "Hakukohde 1 SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(7).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(8).getNumericCellValue == 2)
    assert(wb.getSheetAt(0).getRow(2).getCell(9).getStringCellValue == "Hyvaksytty SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(10).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(11).getStringCellValue == "-")
    // Valintatapajonosarakkeet alkaa
    // Koevalintajono kaikille hakijoille
    assert(wb.getSheetAt(0).getRow(2).getCell(12).getStringCellValue == "-")
    // Todistusvalinta (YO)
    assert(wb.getSheetAt(0).getRow(2).getCell(13).getStringCellValue == "-")
    //Todistusvalintajono ensikertalaisille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(2)
        .getCell(14)
        .getStringCellValue == "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 2."
    )
    // Todistusvalintajono ensikertalaisille hakijoille
    assert(
      wb.getSheetAt(0).getRow(2).getCell(15).getStringCellValue == "-"
    )
    // Todistusvalintajono kaikille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(2)
        .getCell(16)
        .getStringCellValue == "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 1."
    )
    // Valintatapajonosarakkeet loppuu
    assert(wb.getSheetAt(0).getRow(2).getCell(17).getStringCellValue == "raportti.perunut")
    assert(wb.getSheetAt(0).getRow(2).getCell(18).getStringCellValue == "26.06.2024")
    assert(wb.getSheetAt(0).getRow(2).getCell(19).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(20).getStringCellValue == "raportti.lasna_koko_lukuvuosi")
    assert(wb.getSheetAt(0).getRow(2).getCell(21).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(22).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(23).getStringCellValue == "raportti.overdue")
    assert(wb.getSheetAt(0).getRow(2).getCell(24).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(25).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(26).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(27).getStringCellValue == "Helsingfors")
    assert(wb.getSheetAt(0).getRow(2).getCell(28).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(29).getStringCellValue == "050 64292261")
    assert(wb.getSheetAt(0).getRow(2).getCell(30).getStringCellValue == "hakija-33919666@oph.fi")
    assert(wb.getSheetAt(0).getRow(2).getCell(31).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(2).getCell(32).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(2).getCell(33).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(34).getStringCellValue == "C")
    assert(wb.getSheetAt(0).getRow(2).getCell(35).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(2).getCell(36).getStringCellValue == "L")
    assert(wb.getSheetAt(0).getRow(2).getCell(37).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(2).getCell(38).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(39).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(40).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(41) == null)

    assert(wb.getSheetAt(0).getRow(3).getCell(0).getStringCellValue == "Lehto-Testi, Vikke Testi")
    assert(wb.getSheetAt(0).getRow(3).getCell(1).getStringCellValue == "04.04.1990")
    assert(wb.getSheetAt(0).getRow(3).getCell(2).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(3).getCell(3).getStringCellValue == "1.2.246.562.24.18441866015")
    assert(wb.getSheetAt(0).getRow(3).getCell(4).getStringCellValue == "1.2.246.562.11.00000000000002126102")
    assert(wb.getSheetAt(0).getRow(3).getCell(5).getStringCellValue == "Toimipiste 2 sv")
    assert(wb.getSheetAt(0).getRow(3).getCell(6).getStringCellValue == "Hakukohde 2 SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(7).getStringCellValue == "raportti.uneligible")
    assert(wb.getSheetAt(0).getRow(3).getCell(8).getNumericCellValue == 1)
    assert(wb.getSheetAt(0).getRow(3).getCell(9).getStringCellValue == "Hylatty SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(10).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(11).getStringCellValue == "11.06.2024")
    // Valintatapajonosarakkeet alkaa
    // Koevalintajono kaikille hakijoille
    assert(wb.getSheetAt(0).getRow(3).getCell(12).getStringCellValue == "Et osallistunut valintakokeeseen SV")
    // Todistusvalinta (YO)
    assert(wb.getSheetAt(0).getRow(3).getCell(13).getStringCellValue == "-")
    //Todistusvalintajono ensikertalaisille hakijoille - 1707384681516-2842344525807969324
    assert(wb.getSheetAt(0).getRow(3).getCell(14).getStringCellValue == "-")
    // Todistusvalintajono ensikertalaisille hakijoille - 1704199256878262657431481297336
    assert(wb.getSheetAt(0).getRow(3).getCell(15).getStringCellValue == "Du är inte en förstagångssökande.")
    // Todistusvalintajono kaikille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(3)
        .getCell(16)
        .getStringCellValue == "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt."
    )
    // Valintatapajonosarakkeet loppuu
    assert(wb.getSheetAt(0).getRow(3).getCell(17).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(18).getStringCellValue == "26.06.2024")
    assert(wb.getSheetAt(0).getRow(3).getCell(19).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(20).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(21).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(22).getStringCellValue == "Tarkastamatta SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(23).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(24).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(25).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(26).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(27).getStringCellValue == "Lahtis")
    assert(wb.getSheetAt(0).getRow(3).getCell(28).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(3).getCell(29).getStringCellValue == "050 64293345")
    assert(wb.getSheetAt(0).getRow(3).getCell(30).getStringCellValue == "hakija-33919611@oph.fi")
    assert(wb.getSheetAt(0).getRow(3).getCell(31).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(32).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(33).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(3).getCell(34).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(3).getCell(35).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(36).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(37).getStringCellValue == "M")
    assert(wb.getSheetAt(0).getRow(3).getCell(38).getStringCellValue == "L")
    assert(wb.getSheetAt(0).getRow(3).getCell(39).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(3).getCell(40).getStringCellValue == "E")
    assert(wb.getSheetAt(0).getRow(3).getCell(41) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 4)
    assert(wb.getSheetAt(0).getRow(4) == null)
  }

  it should "return excel without arvosanat when nayta arvosanat is false" in {
    val kkHakijatResult = Vector(
      kkHakijatRautiainenWithValintatapajonot,
      kkHakijaLehtoWithValintatapajonot
    )

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(false),
        maybeNaytaHetu = Some(true),
        maybeNaytaPostiosoite = Some(true)
      )

    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(0).getCell(0) == null)
    assert(wb.getSheetAt(0).getRow(0).getCell(13).getStringCellValue == "Hylkäämisen tai peruuntumisen syy SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Hakija SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "raportti.hetu")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "raportti.syntymaAika")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "Kansalaisuus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(7).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(8).getStringCellValue == "Hakukelpoisuus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(9).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(1).getCell(10).getStringCellValue == "raportti.valintatieto")
    assert(wb.getSheetAt(0).getRow(1).getCell(11).getStringCellValue == "raportti.ehdollisestiHyvaksytty")
    assert(wb.getSheetAt(0).getRow(1).getCell(12).getStringCellValue == "Valintatiedon päivämäärä SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(13).getStringCellValue == "Koevalintajono kaikille hakijoille")
    assert(wb.getSheetAt(0).getRow(1).getCell(14).getStringCellValue == "Todistusvalinta (YO)")
    assert(
      wb.getSheetAt(0).getRow(1).getCell(15).getStringCellValue == "Todistusvalintajono ensikertalaisille hakijoille"
    )
    assert(
      wb.getSheetAt(0).getRow(1).getCell(16).getStringCellValue == "Todistusvalintajono ensikertalaisille hakijoille"
    )
    assert(wb.getSheetAt(0).getRow(1).getCell(17).getStringCellValue == "Todistusvalintajono kaikille hakijoille")
    assert(wb.getSheetAt(0).getRow(1).getCell(18).getStringCellValue == "raportti.vastaanottotieto")
    assert(wb.getSheetAt(0).getRow(1).getCell(19).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(1).getCell(20).getStringCellValue == "raportti.ensikertalainen")
    assert(wb.getSheetAt(0).getRow(1).getCell(21).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(1).getCell(22).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(23).getStringCellValue == "raportti.maksuvelvollisuus")
    assert(wb.getSheetAt(0).getRow(1).getCell(24).getStringCellValue == "raportti.hakemusmaksunTila")
    assert(wb.getSheetAt(0).getRow(1).getCell(25).getStringCellValue == "raportti.julkaisulupa")
    assert(wb.getSheetAt(0).getRow(1).getCell(26).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(27).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(1).getCell(28).getStringCellValue == "raportti.lahiosoite")
    assert(wb.getSheetAt(0).getRow(1).getCell(29).getStringCellValue == "raportti.postinumero")
    assert(wb.getSheetAt(0).getRow(1).getCell(30).getStringCellValue == "raportti.postitoimipaikka")
    assert(wb.getSheetAt(0).getRow(1).getCell(31).getStringCellValue == "raportti.kotikunta")
    assert(wb.getSheetAt(0).getRow(1).getCell(32).getStringCellValue == "raportti.asuinmaa")
    assert(wb.getSheetAt(0).getRow(1).getCell(33).getStringCellValue == "Puhelinnumero SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(34).getStringCellValue == "raportti.sahkoposti")
    assert(wb.getSheetAt(0).getRow(1).getCell(35) == null)

    assert(wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue == "Rautiainen-Testi, Dina Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(1).getStringCellValue == "120393-129E")
    assert(wb.getSheetAt(0).getRow(2).getCell(2).getStringCellValue == "12.03.1993")
    assert(wb.getSheetAt(0).getRow(2).getCell(3).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(4).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(2).getCell(5).getStringCellValue == "1.2.246.562.11.00000000000002179045")
    assert(wb.getSheetAt(0).getRow(2).getCell(6).getStringCellValue == "Toimipiste 1 sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(7).getStringCellValue == "Hakukohde 1 SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(8).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(9).getNumericCellValue == 2)
    assert(wb.getSheetAt(0).getRow(2).getCell(10).getStringCellValue == "Hyvaksytty SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(11).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(12).getStringCellValue == "-")
    // Valintatapajonosarakkeet alkaa
    // Koevalintajono kaikille hakijoille
    assert(wb.getSheetAt(0).getRow(2).getCell(13).getStringCellValue == "-")
    // Todistusvalinta (YO)
    assert(wb.getSheetAt(0).getRow(2).getCell(14).getStringCellValue == "-")
    //Todistusvalintajono ensikertalaisille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(2)
        .getCell(15)
        .getStringCellValue == "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 2."
    )
    // Todistusvalintajono ensikertalaisille hakijoille
    assert(
      wb.getSheetAt(0).getRow(2).getCell(16).getStringCellValue == "-"
    )
    // Todistusvalintajono kaikille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(2)
        .getCell(17)
        .getStringCellValue == "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 1."
    )
    // Valintatapajonosarakkeet loppuu
    assert(wb.getSheetAt(0).getRow(2).getCell(18).getStringCellValue == "raportti.perunut")
    assert(wb.getSheetAt(0).getRow(2).getCell(19).getStringCellValue == "26.06.2024")
    assert(wb.getSheetAt(0).getRow(2).getCell(20).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(21).getStringCellValue == "raportti.lasna_koko_lukuvuosi")
    assert(wb.getSheetAt(0).getRow(2).getCell(22).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(23).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(24).getStringCellValue == "raportti.overdue")
    assert(wb.getSheetAt(0).getRow(2).getCell(25).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(26).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(27).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(28).getStringCellValue == "Rämsöönranta 368")
    assert(wb.getSheetAt(0).getRow(2).getCell(29).getStringCellValue == "00100")
    assert(wb.getSheetAt(0).getRow(2).getCell(30).getStringCellValue == "HELSINKI")
    assert(wb.getSheetAt(0).getRow(2).getCell(31).getStringCellValue == "Helsingfors")
    assert(wb.getSheetAt(0).getRow(2).getCell(32).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(33).getStringCellValue == "050 64292261")
    assert(wb.getSheetAt(0).getRow(2).getCell(34).getStringCellValue == "hakija-33919666@oph.fi")
    assert(wb.getSheetAt(0).getRow(2).getCell(35) == null)

    assert(wb.getSheetAt(0).getRow(3).getCell(0).getStringCellValue == "Lehto-Testi, Vikke Testi")
    assert(wb.getSheetAt(0).getRow(3).getCell(1).getStringCellValue == "04041990-345K")
    assert(wb.getSheetAt(0).getRow(3).getCell(2).getStringCellValue == "04.04.1990")
    assert(wb.getSheetAt(0).getRow(3).getCell(3).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(3).getCell(4).getStringCellValue == "1.2.246.562.24.18441866015")
    assert(wb.getSheetAt(0).getRow(3).getCell(5).getStringCellValue == "1.2.246.562.11.00000000000002126102")
    assert(wb.getSheetAt(0).getRow(3).getCell(6).getStringCellValue == "Toimipiste 2 sv")
    assert(wb.getSheetAt(0).getRow(3).getCell(7).getStringCellValue == "Hakukohde 2 SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(8).getStringCellValue == "raportti.uneligible")
    assert(wb.getSheetAt(0).getRow(3).getCell(9).getNumericCellValue == 1)
    assert(wb.getSheetAt(0).getRow(3).getCell(10).getStringCellValue == "Hylatty SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(11).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(12).getStringCellValue == "11.06.2024")
    // Valintatapajonosarakkeet alkaa
    // Koevalintajono kaikille hakijoille
    assert(wb.getSheetAt(0).getRow(3).getCell(13).getStringCellValue == "Et osallistunut valintakokeeseen SV")
    // Todistusvalinta (YO)
    assert(wb.getSheetAt(0).getRow(3).getCell(14).getStringCellValue == "-")
    //Todistusvalintajono ensikertalaisille hakijoille - 1707384681516-2842344525807969324
    assert(wb.getSheetAt(0).getRow(3).getCell(15).getStringCellValue == "-")
    // Todistusvalintajono ensikertalaisille hakijoille - 1704199256878262657431481297336
    assert(wb.getSheetAt(0).getRow(3).getCell(16).getStringCellValue == "Du är inte en förstagångssökande.")
    // Todistusvalintajono kaikille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(3)
        .getCell(17)
        .getStringCellValue == "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt."
    )
    // Valintatapajonosarakkeet loppuu
    assert(wb.getSheetAt(0).getRow(3).getCell(18).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(19).getStringCellValue == "26.06.2024")
    assert(wb.getSheetAt(0).getRow(3).getCell(20).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(21).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(22).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(23).getStringCellValue == "Tarkastamatta SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(24).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(25).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(26).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(27).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(28).getStringCellValue == "Laholanaukio 834")
    assert(wb.getSheetAt(0).getRow(3).getCell(29).getStringCellValue == "15700")
    assert(wb.getSheetAt(0).getRow(3).getCell(30).getStringCellValue == "LAHTI")
    assert(wb.getSheetAt(0).getRow(3).getCell(31).getStringCellValue == "Lahtis")
    assert(wb.getSheetAt(0).getRow(3).getCell(32).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(3).getCell(33).getStringCellValue == "050 64293345")
    assert(wb.getSheetAt(0).getRow(3).getCell(34).getStringCellValue == "hakija-33919611@oph.fi")
    assert(wb.getSheetAt(0).getRow(3).getCell(35) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 4)
    assert(wb.getSheetAt(0).getRow(4) == null)
  }

  it should "return excel without arvosanat in the heading when they don't exist" in {
    val kkHakijatResult = Vector(kkHakijatRautiainenWithValintatapajonot)

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = None,
        maybeNaytaHetu = Some(false),
        maybeNaytaPostiosoite = Some(false)
      )

    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(0).getCell(0) == null)
    assert(wb.getSheetAt(0).getRow(0).getCell(12).getStringCellValue == "Hylkäämisen tai peruuntumisen syy SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Hakija SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "raportti.syntymaAika")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "Kansalaisuus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(7).getStringCellValue == "Hakukelpoisuus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(8).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(1).getCell(9).getStringCellValue == "raportti.valintatieto")
    assert(wb.getSheetAt(0).getRow(1).getCell(10).getStringCellValue == "raportti.ehdollisestiHyvaksytty")
    assert(wb.getSheetAt(0).getRow(1).getCell(11).getStringCellValue == "Valintatiedon päivämäärä SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(12).getStringCellValue == "Todistusvalinta (YO)")
    assert(
      wb.getSheetAt(0).getRow(1).getCell(13).getStringCellValue == "Todistusvalintajono ensikertalaisille hakijoille"
    )
    assert(wb.getSheetAt(0).getRow(1).getCell(14).getStringCellValue == "Todistusvalintajono kaikille hakijoille")
    assert(wb.getSheetAt(0).getRow(1).getCell(15).getStringCellValue == "raportti.vastaanottotieto")
    assert(wb.getSheetAt(0).getRow(1).getCell(16).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(1).getCell(17).getStringCellValue == "raportti.ensikertalainen")
    assert(wb.getSheetAt(0).getRow(1).getCell(18).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(1).getCell(19).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(20).getStringCellValue == "raportti.maksuvelvollisuus")
    assert(wb.getSheetAt(0).getRow(1).getCell(21).getStringCellValue == "raportti.hakemusmaksunTila")
    assert(wb.getSheetAt(0).getRow(1).getCell(22).getStringCellValue == "raportti.julkaisulupa")
    assert(wb.getSheetAt(0).getRow(1).getCell(23).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(24).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(1).getCell(25).getStringCellValue == "raportti.kotikunta")
    assert(wb.getSheetAt(0).getRow(1).getCell(26).getStringCellValue == "raportti.asuinmaa")
    assert(wb.getSheetAt(0).getRow(1).getCell(27).getStringCellValue == "Puhelinnumero SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(28).getStringCellValue == "raportti.sahkoposti")
    assert(wb.getSheetAt(0).getRow(1).getCell(29) == null)

    assert(wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue == "Rautiainen-Testi, Dina Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(1).getStringCellValue == "12.03.1993")
    assert(wb.getSheetAt(0).getRow(2).getCell(2).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(3).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(2).getCell(4).getStringCellValue == "1.2.246.562.11.00000000000002179045")
    assert(wb.getSheetAt(0).getRow(2).getCell(5).getStringCellValue == "Toimipiste 1 sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(6).getStringCellValue == "Hakukohde 1 SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(7).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(8).getNumericCellValue == 2)
    assert(wb.getSheetAt(0).getRow(2).getCell(9).getStringCellValue == "Hyvaksytty SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(10).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(11).getStringCellValue == "-")
    // Valintatapajonosarakkeet alkaa
    // Todistusvalinta (YO)
    assert(wb.getSheetAt(0).getRow(2).getCell(12).getStringCellValue == "-")
    //Todistusvalintajono ensikertalaisille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(2)
        .getCell(13)
        .getStringCellValue == "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 2."
    )
    // Todistusvalintajono kaikille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(2)
        .getCell(14)
        .getStringCellValue == "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 1."
    )
    // Valintatapajonosarakkeet loppuu
    assert(wb.getSheetAt(0).getRow(2).getCell(15).getStringCellValue == "raportti.perunut")
    assert(wb.getSheetAt(0).getRow(2).getCell(16).getStringCellValue == "26.06.2024")
    assert(wb.getSheetAt(0).getRow(2).getCell(17).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(18).getStringCellValue == "raportti.lasna_koko_lukuvuosi")
    assert(wb.getSheetAt(0).getRow(2).getCell(19).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(20).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(21).getStringCellValue == "raportti.overdue")
    assert(wb.getSheetAt(0).getRow(2).getCell(22).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(23).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(24).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(25).getStringCellValue == "Helsingfors")
    assert(wb.getSheetAt(0).getRow(2).getCell(26).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(27).getStringCellValue == "050 64292261")
    assert(wb.getSheetAt(0).getRow(2).getCell(28).getStringCellValue == "hakija-33919666@oph.fi")
    assert(wb.getSheetAt(0).getRow(2).getCell(29) == null)
  }

  "writeHakeneetHyvaksytytVastaanottaneetRaportti" should "return excel with heading row, three result rows and two summary rows for tilastoraportti" in {
    val data = List(
      HakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "Ajoneuvoalan perustutkinto\nOSAO, Haukiputaan yksikkö",
          Fi -> "Ajoneuvoalan perustutkinto\nOSAO, Haukiputaan yksikkö",
          Sv -> "Grundexamen inom fordonsbranschen\nOSAO, Haukiputaan yksikkö"
        ),
        hakijat = 354,
        ensisijaisia = 95,
        varasija = 354,
        hyvaksytyt = 100,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        aloituspaikat = 100,
        toive1 = 95,
        toive2 = 91,
        toive3 = 76,
        toive4 = 45,
        toive5 = 25,
        toive6 = 11,
        toive7 = 11
      ),
      HakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "Ajoneuvoalan perustutkinto\nOSAO, Muhoksen yksikkö",
          Fi -> "Ajoneuvoalan perustutkinto\nOSAO, Muhoksen yksikkö",
          Sv -> "Grundexamen inom fordonsbranschen\nOSAO, Muhoksen yksikkö"
        ),
        hakijat = 148,
        ensisijaisia = 39,
        varasija = 148,
        hyvaksytyt = 16,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        aloituspaikat = 24,
        toive1 = 39,
        toive2 = 29,
        toive3 = 33,
        toive4 = 14,
        toive5 = 12,
        toive6 = 14,
        toive7 = 7
      ),
      HakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "Elintarvikealan perustutkinto\nOSAO, Kaukovainion yksikkö, palvelut",
          Fi -> "Elintarvikealan perustutkinto\nOSAO, Kaukovainion yksikkö, palvelut",
          Sv -> "Grundexamen inom livsmedelsbranschen\nOSAO, Kaukovainion yksikkö, palvelut"
        ),
        hakijat = 112,
        ensisijaisia = 25,
        varasija = 112,
        hyvaksytyt = 17,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        aloituspaikat = 40,
        toive1 = 25,
        toive2 = 29,
        toive3 = 21,
        toive4 = 15,
        toive5 = 11,
        toive6 = 6,
        toive7 = 5
      )
    )

    val workbook = ExcelWriter.writeHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 450,
      naytaHakutoiveet = true,
      tulostustapa = "hakukohteittain"
    )

    assert(workbook.getNumberOfSheets == 1)
    val sheet = workbook.getSheetAt(0)
    // Otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Hakukohde SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Varasija SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getCell(10).getStringCellValue == "Toive1 SV")
    assert(headingRow.getCell(11).getStringCellValue == "Toive2 SV")
    assert(headingRow.getCell(12).getStringCellValue == "Toive3 SV")
    assert(headingRow.getCell(13).getStringCellValue == "Toive4 SV")
    assert(headingRow.getCell(14).getStringCellValue == "Toive5 SV")
    assert(headingRow.getCell(15).getStringCellValue == "Toive6 SV")
    assert(headingRow.getCell(16).getStringCellValue == "Toive7 SV")
    assert(headingRow.getCell(17) == null)

    // Datarivit
    assert(
      sheet.getRow(1).getCell(0).getStringCellValue == "Grundexamen inom fordonsbranschen\nOSAO, Haukiputaan yksikkö"
    )
    assert(sheet.getRow(1).getCell(1).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(2).getNumericCellValue == 95)
    assert(sheet.getRow(1).getCell(3).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(4).getNumericCellValue == 100)
    assert(sheet.getRow(1).getCell(5).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(6).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(8).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(9).getNumericCellValue == 100)
    assert(sheet.getRow(1).getCell(10).getNumericCellValue == 95)
    assert(sheet.getRow(1).getCell(11).getNumericCellValue == 91)
    assert(sheet.getRow(1).getCell(12).getNumericCellValue == 76)
    assert(sheet.getRow(1).getCell(13).getNumericCellValue == 45)
    assert(sheet.getRow(1).getCell(14).getNumericCellValue == 25)
    assert(sheet.getRow(1).getCell(15).getNumericCellValue == 11)
    assert(sheet.getRow(1).getCell(16).getNumericCellValue == 11)
    assert(sheet.getRow(1).getCell(17) == null)

    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Grundexamen inom fordonsbranschen\nOSAO, Muhoksen yksikkö")
    assert(sheet.getRow(2).getCell(1).getNumericCellValue == 148)
    assert(sheet.getRow(2).getCell(2).getNumericCellValue == 39)
    assert(sheet.getRow(2).getCell(3).getNumericCellValue == 148)
    assert(sheet.getRow(2).getCell(4).getNumericCellValue == 16)
    assert(sheet.getRow(2).getCell(5).getNumericCellValue == 0)
    assert(sheet.getRow(2).getCell(6).getNumericCellValue == 0)
    assert(sheet.getRow(2).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(2).getCell(8).getNumericCellValue == 0)
    assert(sheet.getRow(2).getCell(9).getNumericCellValue == 24)
    assert(sheet.getRow(2).getCell(10).getNumericCellValue == 39)
    assert(sheet.getRow(2).getCell(11).getNumericCellValue == 29)
    assert(sheet.getRow(2).getCell(12).getNumericCellValue == 33)
    assert(sheet.getRow(2).getCell(13).getNumericCellValue == 14)
    assert(sheet.getRow(2).getCell(14).getNumericCellValue == 12)
    assert(sheet.getRow(2).getCell(15).getNumericCellValue == 14)
    assert(sheet.getRow(2).getCell(16).getNumericCellValue == 7)
    assert(sheet.getRow(2).getCell(17) == null)

    assert(
      sheet
        .getRow(3)
        .getCell(0)
        .getStringCellValue == "Grundexamen inom livsmedelsbranschen\nOSAO, Kaukovainion yksikkö, palvelut"
    )
    assert(sheet.getRow(3).getCell(1).getNumericCellValue == 112)
    assert(sheet.getRow(3).getCell(2).getNumericCellValue == 25)
    assert(sheet.getRow(3).getCell(3).getNumericCellValue == 112)
    assert(sheet.getRow(3).getCell(4).getNumericCellValue == 17)
    assert(sheet.getRow(3).getCell(5).getNumericCellValue == 0)
    assert(sheet.getRow(3).getCell(6).getNumericCellValue == 0)
    assert(sheet.getRow(3).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(3).getCell(8).getNumericCellValue == 0)
    assert(sheet.getRow(3).getCell(9).getNumericCellValue == 40)
    assert(sheet.getRow(3).getCell(10).getNumericCellValue == 25)
    assert(sheet.getRow(3).getCell(11).getNumericCellValue == 29)
    assert(sheet.getRow(3).getCell(12).getNumericCellValue == 21)
    assert(sheet.getRow(3).getCell(13).getNumericCellValue == 15)
    assert(sheet.getRow(3).getCell(14).getNumericCellValue == 11)
    assert(sheet.getRow(3).getCell(15).getNumericCellValue == 6)
    assert(sheet.getRow(3).getCell(16).getNumericCellValue == 5)
    assert(sheet.getRow(3).getCell(17) == null)

    // summarivit
    assert(sheet.getRow(4).getCell(0).getStringCellValue == "Yhteensä SV")
    assert(sheet.getRow(4).getCell(1).getNumericCellValue == 614)
    assert(sheet.getRow(5).getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(sheet.getRow(5).getCell(1).getNumericCellValue == 450)
    assert(sheet.getPhysicalNumberOfRows == 6)

  }

  it should "return excel with correct tulostustapa heading" in {
    val data = List(
      HakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "OSAO, Haukiputaan yksikkö",
          Fi -> "OSAO, Haukiputaan yksikkö",
          Sv -> "OSAO, Haukiputaan yksikkö"
        ),
        hakijat = 354,
        ensisijaisia = 95,
        varasija = 354,
        hyvaksytyt = 100,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        aloituspaikat = 100,
        toive1 = 95,
        toive2 = 91,
        toive3 = 76,
        toive4 = 45,
        toive5 = 25,
        toive6 = 11,
        toive7 = 11
      )
    )

    val workbook = ExcelWriter.writeHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 300,
      naytaHakutoiveet = false,
      tulostustapa = "oppilaitoksittain"
    )

    assert(workbook.getNumberOfSheets == 1)
    val sheet = workbook.getSheetAt(0)
    // Otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Oppilaitos SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Varasija SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getLastCellNum == 10)
    assert(headingRow.getCell(10) == null)

    // Datarivit
    assert(sheet.getRow(1).getCell(0).getStringCellValue == "OSAO, Haukiputaan yksikkö")
    assert(sheet.getRow(1).getCell(1).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(2).getNumericCellValue == 95)
    assert(sheet.getRow(1).getCell(3).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(4).getNumericCellValue == 100)
    assert(sheet.getRow(1).getCell(5).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(6).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(8).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(9).getNumericCellValue == 100)

    assert(sheet.getRow(1).getCell(10) == null)
    // summarivit
    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Yhteensä SV")
    assert(sheet.getRow(2).getCell(1).getNumericCellValue == 354)
    assert(sheet.getRow(3).getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(sheet.getRow(3).getCell(1).getNumericCellValue == 300)
    assert(sheet.getPhysicalNumberOfRows == 4)

  }

  it should "return excel without Toive sarakkeet if naytaHakutoiveet is false" in {
    val data = List(
      HakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "Ajoneuvoalan perustutkinto\nOSAO, Haukiputaan yksikkö",
          Fi -> "Ajoneuvoalan perustutkinto\nOSAO, Haukiputaan yksikkö",
          Sv -> "Grundexamen inom fordonsbranschen\nOSAO, Haukiputaan yksikkö"
        ),
        hakijat = 354,
        ensisijaisia = 95,
        varasija = 354,
        hyvaksytyt = 100,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        aloituspaikat = 100,
        toive1 = 95,
        toive2 = 91,
        toive3 = 76,
        toive4 = 45,
        toive5 = 25,
        toive6 = 11,
        toive7 = 11
      )
    )

    val workbook = ExcelWriter.writeHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 300,
      naytaHakutoiveet = false,
      tulostustapa = "hakukohteittain"
    )

    assert(workbook.getNumberOfSheets == 1)
    val sheet = workbook.getSheetAt(0)
    // Otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Hakukohde SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Varasija SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getLastCellNum == 10)
    assert(headingRow.getCell(10) == null)

    // Datarivit
    assert(
      sheet.getRow(1).getCell(0).getStringCellValue == "Grundexamen inom fordonsbranschen\nOSAO, Haukiputaan yksikkö"
    )
    assert(sheet.getRow(1).getCell(1).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(2).getNumericCellValue == 95)
    assert(sheet.getRow(1).getCell(3).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(4).getNumericCellValue == 100)
    assert(sheet.getRow(1).getCell(5).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(6).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(8).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(9).getNumericCellValue == 100)
    assert(sheet.getRow(1).getCell(10) == null)
    // summarivit
    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Yhteensä SV")
    assert(sheet.getRow(2).getCell(1).getNumericCellValue == 354)
    assert(sheet.getRow(3).getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(sheet.getRow(3).getCell(1).getNumericCellValue == 300)
    assert(sheet.getPhysicalNumberOfRows == 4)

  }

  "shouldSkipCreatingCell" should "return false for hetu if naytaHetu is true" in {
    assert(
      !ExcelWriter.shouldSkipCreatingCell(
        fieldName = "hetu",
        naytaArvosanat = true,
        naytaHetu = true,
        naytaPostiosoite = false
      )
    )
  }

  it should "return true for hetu if naytaHetu is false" in {
    assert(
      ExcelWriter.shouldSkipCreatingCell(
        fieldName = "hetu",
        naytaArvosanat = true,
        naytaHetu = false,
        naytaPostiosoite = false
      )
    )
  }

  it should "return false for lahiosoite if naytaPostiosoite is true" in {
    assert(
      !ExcelWriter.shouldSkipCreatingCell(
        fieldName = "lahiosoite",
        naytaArvosanat = true,
        naytaHetu = false,
        naytaPostiosoite = true
      )
    )
  }

  it should "return true for lahiosoite if naytaPostiosoite is false" in {
    assert(
      ExcelWriter.shouldSkipCreatingCell(
        fieldName = "lahiosoite",
        naytaArvosanat = true,
        naytaHetu = false,
        naytaPostiosoite = false
      )
    )
  }

  it should "return false for postinumero if naytaPostiosoite is true" in {
    assert(
      !ExcelWriter.shouldSkipCreatingCell(
        fieldName = "postinumero",
        naytaArvosanat = true,
        naytaHetu = false,
        naytaPostiosoite = true
      )
    )
  }

  it should "return true for postinumero if naytaPostiosoite is false" in {
    assert(
      ExcelWriter.shouldSkipCreatingCell(
        fieldName = "postinumero",
        naytaArvosanat = true,
        naytaHetu = false,
        naytaPostiosoite = false
      )
    )
  }

  it should "return false for postitoimipaikka if naytaPostiosoite is true" in {
    assert(
      !ExcelWriter.shouldSkipCreatingCell(
        fieldName = "postitoimipaikka",
        naytaArvosanat = true,
        naytaHetu = false,
        naytaPostiosoite = true
      )
    )
  }

  it should "return true for postitoimipaikka if naytaPostiosoite is false" in {
    assert(
      ExcelWriter.shouldSkipCreatingCell(
        fieldName = "postitoimipaikka",
        naytaArvosanat = true,
        naytaHetu = false,
        naytaPostiosoite = false
      )
    )
  }

  "getTranslationForCellValue" should "return translation key if translation is not found" in {
    assert(
      ExcelWriter.getTranslationForCellValue("puuttuva_kaannos", translations) == "raportti.puuttuva_kaannos"
    )
  }

  it should "return translation when translation if found with translation key" in {
    assert(
      ExcelWriter.getTranslationForCellValue("eligible", translations) == "hakukelpoinen SV"
    )
  }

  "getHeadingFieldNames" should "return all field names except valintatapajonot and arvosanat for kk-hakija because valintatapajonot and arvosanat are empty" in {
    assert(
      ExcelWriter.getHeadingFieldNames(true, true, true, List(), List()) ==
        List(
          "hakija",
          "hetu",
          "syntymaAika",
          "kansalaisuus",
          "oppijanumero",
          "hakemusOid",
          "toimipiste",
          "hakukohteenNimi",
          "hakukelpoisuus",
          "prioriteetti",
          "valintatieto",
          "ehdollisestiHyvaksytty",
          "valintatiedonPvm",
          "vastaanottotieto",
          "viimVastaanottopaiva",
          "ensikertalainen",
          "ilmoittautuminen",
          "pohjakoulutus",
          "maksuvelvollisuus",
          "hakemusmaksunTila",
          "julkaisulupa",
          "markkinointilupa",
          "sahkoinenViestintalupa",
          "lahiosoite",
          "postinumero",
          "postitoimipaikka",
          "kotikunta",
          "asuinmaa",
          "puhelinnumero",
          "sahkoposti"
        )
    )
  }

  it should "return Lukiokoulutus field name, arvosanat and all class properties for kk-hakija" in {
    val valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1709304116443-4815957697640331820",
        valintatapajononNimi = "Lukiokoulutus",
        valinnanTila = "PERUUNTUNUT",
        valinnanTilanKuvaus = Map(
          En -> "Cancelled, accepted for a study place with higher priority",
          Fi -> "Peruuntunut, hyväksytty ylemmälle hakutoiveelle",
          Sv -> "Annullerad, godkänt till ansökningsmål med högre prioritet"
        )
      )
    )

    assert(
      ExcelWriter.getHeadingFieldNames(true, true, true, valintatapajonot, List("A", "EA", "PS")) ==
        List(
          "hakija",
          "hetu",
          "syntymaAika",
          "kansalaisuus",
          "oppijanumero",
          "hakemusOid",
          "toimipiste",
          "hakukohteenNimi",
          "hakukelpoisuus",
          "prioriteetti",
          "valintatieto",
          "ehdollisestiHyvaksytty",
          "valintatiedonPvm",
          "Lukiokoulutus",
          "vastaanottotieto",
          "viimVastaanottopaiva",
          "ensikertalainen",
          "ilmoittautuminen",
          "pohjakoulutus",
          "maksuvelvollisuus",
          "hakemusmaksunTila",
          "julkaisulupa",
          "markkinointilupa",
          "sahkoinenViestintalupa",
          "lahiosoite",
          "postinumero",
          "postitoimipaikka",
          "kotikunta",
          "asuinmaa",
          "puhelinnumero",
          "sahkoposti",
          "A",
          "EA",
          "PS"
        )
    )
  }

  it should "return Lukiokoulutus field name, arvosanat and all other properties except hetu for kk-hakija" in {
    val valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1709304116443-4815957697640331820",
        valintatapajononNimi = "Lukiokoulutus",
        valinnanTila = "PERUUNTUNUT",
        valinnanTilanKuvaus = Map(
          En -> "Cancelled, accepted for a study place with higher priority",
          Fi -> "Peruuntunut, hyväksytty ylemmälle hakutoiveelle",
          Sv -> "Annullerad, godkänt till ansökningsmål med högre prioritet"
        )
      )
    )

    assert(
      ExcelWriter.getHeadingFieldNames(true, false, true, valintatapajonot, List("A", "EA", "PS")) ==
        List(
          "hakija",
          "syntymaAika",
          "kansalaisuus",
          "oppijanumero",
          "hakemusOid",
          "toimipiste",
          "hakukohteenNimi",
          "hakukelpoisuus",
          "prioriteetti",
          "valintatieto",
          "ehdollisestiHyvaksytty",
          "valintatiedonPvm",
          "Lukiokoulutus",
          "vastaanottotieto",
          "viimVastaanottopaiva",
          "ensikertalainen",
          "ilmoittautuminen",
          "pohjakoulutus",
          "maksuvelvollisuus",
          "hakemusmaksunTila",
          "julkaisulupa",
          "markkinointilupa",
          "sahkoinenViestintalupa",
          "lahiosoite",
          "postinumero",
          "postitoimipaikka",
          "kotikunta",
          "asuinmaa",
          "puhelinnumero",
          "sahkoposti",
          "A",
          "EA",
          "PS"
        )
    )
  }

  it should "return three valintatapajono field names, arvosanat and all other properties except postiosoite for kk-hakija" in {
    val valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1712237413120-5813883472174467653",
        valintatapajononNimi = "Ammatillinen koulutus",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "",
          Fi -> "Hylätty harkinnanvaraisessa valinnassa",
          Sv -> "Underkänd i antagning enligt prövning"
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1699941505144-4366169695634781556",
        valintatapajononNimi = "Kurssivalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "",
          Fi -> "Et suorittanut kurssia.",
          Sv -> "Du har inte genomfört kursen."
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1709304116443-4815957697640331820",
        valintatapajononNimi = "Lukiokoulutus",
        valinnanTila = "PERUUNTUNUT",
        valinnanTilanKuvaus = Map(
          En -> "Cancelled, accepted for a study place with higher priority",
          Fi -> "Peruuntunut, hyväksytty ylemmälle hakutoiveelle",
          Sv -> "Annullerad, godkänt till ansökningsmål med högre prioritet"
        )
      )
    )

    assert(
      ExcelWriter.getHeadingFieldNames(true, true, false, valintatapajonot, List("A", "EA", "PS")) ==
        List(
          "hakija",
          "hetu",
          "syntymaAika",
          "kansalaisuus",
          "oppijanumero",
          "hakemusOid",
          "toimipiste",
          "hakukohteenNimi",
          "hakukelpoisuus",
          "prioriteetti",
          "valintatieto",
          "ehdollisestiHyvaksytty",
          "valintatiedonPvm",
          "Ammatillinen koulutus",
          "Kurssivalintajono kaikille hakijoille",
          "Lukiokoulutus",
          "vastaanottotieto",
          "viimVastaanottopaiva",
          "ensikertalainen",
          "ilmoittautuminen",
          "pohjakoulutus",
          "maksuvelvollisuus",
          "hakemusmaksunTila",
          "julkaisulupa",
          "markkinointilupa",
          "sahkoinenViestintalupa",
          "kotikunta",
          "asuinmaa",
          "puhelinnumero",
          "sahkoposti",
          "A",
          "EA",
          "PS"
        )
    )
  }

  it should "not return arvosanat in the heading when naytaArvosanat is false" in {
    val valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1712237413120-5813883472174467653",
        valintatapajononNimi = "Ammatillinen koulutus",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "",
          Fi -> "Hylätty harkinnanvaraisessa valinnassa",
          Sv -> "Underkänd i antagning enligt prövning"
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1699941505144-4366169695634781556",
        valintatapajononNimi = "Kurssivalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "",
          Fi -> "Et suorittanut kurssia.",
          Sv -> "Du har inte genomfört kursen."
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1709304116443-4815957697640331820",
        valintatapajononNimi = "Lukiokoulutus",
        valinnanTila = "PERUUNTUNUT",
        valinnanTilanKuvaus = Map(
          En -> "Cancelled, accepted for a study place with higher priority",
          Fi -> "Peruuntunut, hyväksytty ylemmälle hakutoiveelle",
          Sv -> "Annullerad, godkänt till ansökningsmål med högre prioritet"
        )
      )
    )

    assert(
      ExcelWriter.getHeadingFieldNames(false, true, false, valintatapajonot, List("A", "EA", "PS")) ==
        List(
          "hakija",
          "hetu",
          "syntymaAika",
          "kansalaisuus",
          "oppijanumero",
          "hakemusOid",
          "toimipiste",
          "hakukohteenNimi",
          "hakukelpoisuus",
          "prioriteetti",
          "valintatieto",
          "ehdollisestiHyvaksytty",
          "valintatiedonPvm",
          "Ammatillinen koulutus",
          "Kurssivalintajono kaikille hakijoille",
          "Lukiokoulutus",
          "vastaanottotieto",
          "viimVastaanottopaiva",
          "ensikertalainen",
          "ilmoittautuminen",
          "pohjakoulutus",
          "maksuvelvollisuus",
          "hakemusmaksunTila",
          "julkaisulupa",
          "markkinointilupa",
          "sahkoinenViestintalupa",
          "kotikunta",
          "asuinmaa",
          "puhelinnumero",
          "sahkoposti"
        )
    )
  }

  "writeKkHakeneetHyvaksytytVastaanottaneetRaportti" should "return excel with heading row, two data rows and summary rows" in {
    val data = List(
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          Fi -> "Arkkitehtuuri, tekniikan kandidaatti ja arkkitehti (3 v + 2 v) - DIA-yhteisvalinta\nAalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          En -> "Arkkitehtuuri, tekniikan kandidaatti ja arkkitehti (3 v + 2 v) - DIA-yhteisvalinta\nAalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          Sv -> "Arkitektur, teknologie kandidat och arkitekt (3 år + 2 år) - DIA gemensamma antagning\nAalto-universitetet, Högskolan för konst design och arkitektur"),
        hakijat = 815,
        ensisijaisia = 261,
        ensikertalaisia = 120,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 1,
        valinnanAloituspaikat = 55,
        aloituspaikat = 55,
        toive1 = 261,
        toive2 = 0,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          Fi -> "Dokumentaarinen elokuva, taiteen kandidaatti ja maisteri (3 v + 2 v)\nAalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          En -> "Dokumentaarinen elokuva, taiteen kandidaatti ja maisteri (3 v + 2 v)\nAalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          Sv -> "Dokumentärfilm, konstkandidat och -magister (3 år + 2 år)\nAalto-universitetet, Högskolan för konst design och arkitektur"),
        hakijat = 41,
        ensisijaisia = 26,
        ensikertalaisia = 5,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 1,
        valinnanAloituspaikat = 2,
        aloituspaikat = 2,
        toive1 = 26,
        toive2 = 0,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      )
    )

    val workbook: XSSFWorkbook = ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 820,
      ensikertalaisetYksittaisetHakijat = 126,
      maksuvelvollisetYksittaisetHakijat = 2,
      naytaHakutoiveet = true,
      tulostustapa = "hakukohteittain"
    )

    assert(workbook.getNumberOfSheets == 1)
    val sheet = workbook.getSheetAt(0)

    // otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Hakukohde SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Ensikertalaisia SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Maksuvelvollisia SV")
    assert(headingRow.getCell(10).getStringCellValue == "Valinnan aloituspaikat SV")
    assert(headingRow.getCell(11).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getCell(12).getStringCellValue == "Toive1 SV")
    assert(headingRow.getCell(13).getStringCellValue == "Toive2 SV")
    assert(headingRow.getCell(14).getStringCellValue == "Toive3 SV")
    assert(headingRow.getCell(15).getStringCellValue == "Toive4 SV")
    assert(headingRow.getCell(16).getStringCellValue == "Toive5 SV")
    assert(headingRow.getCell(17).getStringCellValue == "Toive6 SV")
    assert(headingRow.getCell(18) == null)

    val dataRow1 = sheet.getRow(1)
    assert(dataRow1.getCell(0).getStringCellValue == "Arkitektur, teknologie kandidat och arkitekt (3 år + 2 år) - DIA gemensamma antagning\nAalto-universitetet, Högskolan för konst design och arkitektur")
    assert(dataRow1.getCell(1).getNumericCellValue == 815)
    assert(dataRow1.getCell(2).getNumericCellValue == 261)
    assert(dataRow1.getCell(3).getNumericCellValue == 120)
    assert(dataRow1.getCell(4).getNumericCellValue == 0)
    assert(dataRow1.getCell(5).getNumericCellValue == 0)
    assert(dataRow1.getCell(6).getNumericCellValue == 0)
    assert(dataRow1.getCell(7).getNumericCellValue == 0)
    assert(dataRow1.getCell(8).getNumericCellValue == 0)
    assert(dataRow1.getCell(9).getNumericCellValue == 1)
    assert(dataRow1.getCell(10).getNumericCellValue == 55)
    assert(dataRow1.getCell(11).getNumericCellValue == 55)
    assert(dataRow1.getCell(12).getNumericCellValue == 261)
    assert(dataRow1.getCell(13).getNumericCellValue == 0)
    assert(dataRow1.getCell(14).getNumericCellValue == 0)
    assert(dataRow1.getCell(15).getNumericCellValue == 0)
    assert(dataRow1.getCell(16).getNumericCellValue == 0)
    assert(dataRow1.getCell(17).getNumericCellValue == 0)
    assert(dataRow1.getCell(18) == null)

    val dataRow2 = sheet.getRow(2)
    assert(dataRow2.getCell(0).getStringCellValue == "Dokumentärfilm, konstkandidat och -magister (3 år + 2 år)\nAalto-universitetet, Högskolan för konst design och arkitektur")
    assert(dataRow2.getCell(1).getNumericCellValue == 41)
    assert(dataRow2.getCell(2).getNumericCellValue == 26)
    assert(dataRow2.getCell(3).getNumericCellValue == 5)
    assert(dataRow2.getCell(4).getNumericCellValue == 0)
    assert(dataRow2.getCell(5).getNumericCellValue == 0)
    assert(dataRow2.getCell(6).getNumericCellValue == 0)
    assert(dataRow2.getCell(7).getNumericCellValue == 0)
    assert(dataRow2.getCell(8).getNumericCellValue == 0)
    assert(dataRow2.getCell(9).getNumericCellValue == 1)
    assert(dataRow2.getCell(10).getNumericCellValue == 2)
    assert(dataRow2.getCell(11).getNumericCellValue == 2)
    assert(dataRow2.getCell(12).getNumericCellValue == 26)
    assert(dataRow2.getCell(13).getNumericCellValue == 0)
    assert(dataRow2.getCell(14).getNumericCellValue == 0)
    assert(dataRow2.getCell(15).getNumericCellValue == 0)
    assert(dataRow2.getCell(16).getNumericCellValue == 0)
    assert(dataRow2.getCell(17).getNumericCellValue == 0)
    assert(dataRow2.getCell(18) == null)

    // yhteensä
    val summaryRow = sheet.getRow(3)
    assert(summaryRow.getCell(0).getStringCellValue == "Yhteensä SV")
    assert(summaryRow.getCell(1).getNumericCellValue == 856)
    assert(summaryRow.getCell(2).getNumericCellValue == 287)
    assert(summaryRow.getCell(3).getNumericCellValue == 125)
    assert(summaryRow.getCell(4).getNumericCellValue == 0)
    assert(summaryRow.getCell(5).getNumericCellValue == 0)
    assert(summaryRow.getCell(6).getNumericCellValue == 0)
    assert(summaryRow.getCell(7).getNumericCellValue == 0)
    assert(summaryRow.getCell(8).getNumericCellValue == 0)
    assert(summaryRow.getCell(9).getNumericCellValue == 2)
    assert(summaryRow.getCell(10).getNumericCellValue == 57)
    assert(summaryRow.getCell(11).getNumericCellValue == 57)
    assert(summaryRow.getCell(12).getNumericCellValue == 287)
    assert(summaryRow.getCell(13).getNumericCellValue == 0)
    assert(summaryRow.getCell(14).getNumericCellValue == 0)
    assert(summaryRow.getCell(15).getNumericCellValue == 0)
    assert(summaryRow.getCell(16).getNumericCellValue == 0)
    assert(summaryRow.getCell(17).getNumericCellValue == 0)
    assert(summaryRow.getCell(18) == null)

    // yksittäiset hakijat lasketaan vaan tiettyihin sarakkeisiin
    val hakijatSummaryRow = sheet.getRow(4)
    assert(hakijatSummaryRow.getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(hakijatSummaryRow.getCell(1).getNumericCellValue == 820)
    assert(hakijatSummaryRow.getCell(2).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(3).getNumericCellValue == 126)
    assert(hakijatSummaryRow.getCell(4).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(5).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(6).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(7).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(8).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(9).getNumericCellValue == 2)
  }

  "writeKkHakeneetHyvaksytytVastaanottaneetRaportti" should "return excel without hautoiveet columns if naytaHakutoiveet is false" in {
    val data = List(
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          Fi -> "Arkkitehtuuri, tekniikan kandidaatti ja arkkitehti (3 v + 2 v) - DIA-yhteisvalinta\nAalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          En -> "Arkkitehtuuri, tekniikan kandidaatti ja arkkitehti (3 v + 2 v) - DIA-yhteisvalinta\nAalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          Sv -> "Arkitektur, teknologie kandidat och arkitekt (3 år + 2 år) - DIA gemensamma antagning\nAalto-universitetet, Högskolan för konst design och arkitektur"),
        hakijat = 815,
        ensisijaisia = 261,
        ensikertalaisia = 120,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 1,
        valinnanAloituspaikat = 55,
        aloituspaikat = 55,
        toive1 = 261,
        toive2 = 0,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          Fi -> "Dokumentaarinen elokuva, taiteen kandidaatti ja maisteri (3 v + 2 v)\nAalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          En -> "Dokumentaarinen elokuva, taiteen kandidaatti ja maisteri (3 v + 2 v)\nAalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          Sv -> "Dokumentärfilm, konstkandidat och -magister (3 år + 2 år)\nAalto-universitetet, Högskolan för konst design och arkitektur"),
        hakijat = 41,
        ensisijaisia = 26,
        ensikertalaisia = 5,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 1,
        valinnanAloituspaikat = 2,
        aloituspaikat = 2,
        toive1 = 26,
        toive2 = 0,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      )
    )

    val workbook: XSSFWorkbook = ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 820,
      ensikertalaisetYksittaisetHakijat = 126,
      maksuvelvollisetYksittaisetHakijat = 2,
      naytaHakutoiveet = false,
      tulostustapa = "hakukohteittain"
    )

    assert(workbook.getNumberOfSheets == 1)
    val sheet = workbook.getSheetAt(0)

    // otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Hakukohde SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Ensikertalaisia SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Maksuvelvollisia SV")
    assert(headingRow.getCell(10).getStringCellValue == "Valinnan aloituspaikat SV")
    assert(headingRow.getCell(11).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getCell(12) == null)

    val dataRow1 = sheet.getRow(1)
    assert(dataRow1.getCell(0).getStringCellValue == "Arkitektur, teknologie kandidat och arkitekt (3 år + 2 år) - DIA gemensamma antagning\nAalto-universitetet, Högskolan för konst design och arkitektur")
    assert(dataRow1.getCell(1).getNumericCellValue == 815)
    assert(dataRow1.getCell(2).getNumericCellValue == 261)
    assert(dataRow1.getCell(3).getNumericCellValue == 120)
    assert(dataRow1.getCell(4).getNumericCellValue == 0)
    assert(dataRow1.getCell(5).getNumericCellValue == 0)
    assert(dataRow1.getCell(6).getNumericCellValue == 0)
    assert(dataRow1.getCell(7).getNumericCellValue == 0)
    assert(dataRow1.getCell(8).getNumericCellValue == 0)
    assert(dataRow1.getCell(9).getNumericCellValue == 1)
    assert(dataRow1.getCell(10).getNumericCellValue == 55)
    assert(dataRow1.getCell(11).getNumericCellValue == 55)
    assert(dataRow1.getCell(12) == null)

    val dataRow2 = sheet.getRow(2)
    assert(dataRow2.getCell(0).getStringCellValue == "Dokumentärfilm, konstkandidat och -magister (3 år + 2 år)\nAalto-universitetet, Högskolan för konst design och arkitektur")
    assert(dataRow2.getCell(1).getNumericCellValue == 41)
    assert(dataRow2.getCell(2).getNumericCellValue == 26)
    assert(dataRow2.getCell(3).getNumericCellValue == 5)
    assert(dataRow2.getCell(4).getNumericCellValue == 0)
    assert(dataRow2.getCell(5).getNumericCellValue == 0)
    assert(dataRow2.getCell(6).getNumericCellValue == 0)
    assert(dataRow2.getCell(7).getNumericCellValue == 0)
    assert(dataRow2.getCell(8).getNumericCellValue == 0)
    assert(dataRow2.getCell(9).getNumericCellValue == 1)
    assert(dataRow2.getCell(10).getNumericCellValue == 2)
    assert(dataRow2.getCell(11).getNumericCellValue == 2)
    assert(dataRow2.getCell(12) == null)

    // yhteensä
    val summaryRow = sheet.getRow(3)
    assert(summaryRow.getCell(0).getStringCellValue == "Yhteensä SV")
    assert(summaryRow.getCell(1).getNumericCellValue == 856)
    assert(summaryRow.getCell(2).getNumericCellValue == 287)
    assert(summaryRow.getCell(3).getNumericCellValue == 125)
    assert(summaryRow.getCell(4).getNumericCellValue == 0)
    assert(summaryRow.getCell(5).getNumericCellValue == 0)
    assert(summaryRow.getCell(6).getNumericCellValue == 0)
    assert(summaryRow.getCell(7).getNumericCellValue == 0)
    assert(summaryRow.getCell(8).getNumericCellValue == 0)
    assert(summaryRow.getCell(9).getNumericCellValue == 2)
    assert(summaryRow.getCell(10).getNumericCellValue == 57)
    assert(summaryRow.getCell(11).getNumericCellValue == 57)
    assert(summaryRow.getCell(12) == null)

    // yksittäiset hakijat lasketaan vaan tiettyihin sarakkeisiin
    val hakijatSummaryRow = sheet.getRow(4)
    assert(hakijatSummaryRow.getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(hakijatSummaryRow.getCell(1).getNumericCellValue == 820)
    assert(hakijatSummaryRow.getCell(2).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(3).getNumericCellValue == 126)
    assert(hakijatSummaryRow.getCell(4).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(5).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(6).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(7).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(8).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(9).getNumericCellValue == 2)
  }

  "writeKkHakeneetHyvaksytytVastaanottaneetRaportti" should "return excel with correct heading and first column and provided data rows" in {
    val data = List(
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(En -> "Afghanistan", Fi -> "Afganistan", Sv -> "Afghanistan"),
        hakijat = 2,
        ensisijaisia = 0,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 58,
        aloituspaikat = 58,
        toive1 = 0,
        toive2 = 0,
        toive3 = 2,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(En -> "Bulgaria", Fi -> "Bulgaria", Sv -> "Bulgarien"),
        hakijat = 2,
        ensisijaisia = 0,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 42,
        aloituspaikat = 42,
        toive1 = 0,
        toive2 = 1,
        toive3 = 1,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(En -> "China", Fi -> "Kiina", Sv -> "Kina"),
        hakijat = 4,
        ensisijaisia = 2,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 79,
        aloituspaikat = 79,
        toive1 = 2,
        toive2 = 1,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 1
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(Fi -> "Finland", Fi -> "Suomi", Sv -> "Finland"),
        hakijat = 2751,
        ensisijaisia = 1287,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 252,
        aloituspaikat = 252,
        toive1 = 1287,
        toive2 = 694,
        toive3 = 347,
        toive4 = 209,
        toive5 = 122,
        toive6 = 92
      )
    )

    val workbook: XSSFWorkbook = ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
      "sv",
      translations,
      data,
      yksittaisetHakijat = 2759,
      ensikertalaisetYksittaisetHakijat = 1289,
      maksuvelvollisetYksittaisetHakijat = 0,
      naytaHakutoiveet = true,
      tulostustapa = "kansalaisuuksittain"
    )

    assert(workbook.getNumberOfSheets == 1)
    val sheet = workbook.getSheetAt(0)

    // otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Kansalaisuus SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Ensikertalaisia SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Maksuvelvollisia SV")
    assert(headingRow.getCell(10).getStringCellValue == "Valinnan aloituspaikat SV")
    assert(headingRow.getCell(11).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getCell(12).getStringCellValue == "Toive1 SV")
    assert(headingRow.getCell(13).getStringCellValue == "Toive2 SV")
    assert(headingRow.getCell(14).getStringCellValue == "Toive3 SV")
    assert(headingRow.getCell(15).getStringCellValue == "Toive4 SV")
    assert(headingRow.getCell(16).getStringCellValue == "Toive5 SV")
    assert(headingRow.getCell(17).getStringCellValue == "Toive6 SV")
    assert(headingRow.getCell(18) == null)

    // data rows
    val dataRow1 = sheet.getRow(1)
    assert(dataRow1.getCell(0).getStringCellValue == "Afghanistan")
    assert(dataRow1.getCell(1).getNumericCellValue == 2)
    assert(dataRow1.getCell(2).getNumericCellValue == 0)
    assert(dataRow1.getCell(3).getNumericCellValue == 0)
    assert(dataRow1.getCell(4).getNumericCellValue == 0)
    assert(dataRow1.getCell(5).getNumericCellValue == 0)
    assert(dataRow1.getCell(6).getNumericCellValue == 0)
    assert(dataRow1.getCell(7).getNumericCellValue == 0)
    assert(dataRow1.getCell(8).getNumericCellValue == 0)
    assert(dataRow1.getCell(9).getNumericCellValue == 0)
    assert(dataRow1.getCell(10).getNumericCellValue == 58)
    assert(dataRow1.getCell(11).getNumericCellValue == 58)
    assert(dataRow1.getCell(12).getNumericCellValue == 0)
    assert(dataRow1.getCell(13).getNumericCellValue == 0)
    assert(dataRow1.getCell(14).getNumericCellValue == 2)
    assert(dataRow1.getCell(15).getNumericCellValue == 0)
    assert(dataRow1.getCell(16).getNumericCellValue == 0)
    assert(dataRow1.getCell(17).getNumericCellValue == 0)
    assert(dataRow1.getCell(18) == null)

    val dataRow2 = sheet.getRow(2)
    assert(dataRow2.getCell(0).getStringCellValue == "Bulgarien")
    assert(dataRow2.getCell(1).getNumericCellValue == 2)
    assert(dataRow2.getCell(2).getNumericCellValue == 0)
    assert(dataRow2.getCell(3).getNumericCellValue == 0)
    assert(dataRow2.getCell(4).getNumericCellValue == 0)
    assert(dataRow2.getCell(5).getNumericCellValue == 0)
    assert(dataRow2.getCell(6).getNumericCellValue == 0)
    assert(dataRow2.getCell(7).getNumericCellValue == 0)
    assert(dataRow2.getCell(8).getNumericCellValue == 0)
    assert(dataRow2.getCell(9).getNumericCellValue == 0)
    assert(dataRow2.getCell(10).getNumericCellValue == 42)
    assert(dataRow2.getCell(11).getNumericCellValue == 42)
    assert(dataRow2.getCell(12).getNumericCellValue == 0)
    assert(dataRow2.getCell(13).getNumericCellValue == 1)
    assert(dataRow2.getCell(14).getNumericCellValue == 1)
    assert(dataRow2.getCell(15).getNumericCellValue == 0)
    assert(dataRow2.getCell(16).getNumericCellValue == 0)
    assert(dataRow2.getCell(17).getNumericCellValue == 0)
    assert(dataRow2.getCell(18) == null)

    val dataRow3 = sheet.getRow(3)
    assert(dataRow3.getCell(0).getStringCellValue == "Kina")
    assert(dataRow3.getCell(1).getNumericCellValue == 4)
    assert(dataRow3.getCell(2).getNumericCellValue == 2)
    assert(dataRow3.getCell(3).getNumericCellValue == 0)
    assert(dataRow3.getCell(4).getNumericCellValue == 0)
    assert(dataRow3.getCell(5).getNumericCellValue == 0)
    assert(dataRow3.getCell(6).getNumericCellValue == 0)
    assert(dataRow3.getCell(7).getNumericCellValue == 0)
    assert(dataRow3.getCell(8).getNumericCellValue == 0)
    assert(dataRow3.getCell(9).getNumericCellValue == 0)
    assert(dataRow3.getCell(10).getNumericCellValue == 79)
    assert(dataRow3.getCell(11).getNumericCellValue == 79)
    assert(dataRow3.getCell(12).getNumericCellValue == 2)
    assert(dataRow3.getCell(13).getNumericCellValue == 1)
    assert(dataRow3.getCell(14).getNumericCellValue == 0)
    assert(dataRow3.getCell(15).getNumericCellValue == 0)
    assert(dataRow3.getCell(16).getNumericCellValue == 0)
    assert(dataRow3.getCell(17).getNumericCellValue == 1)
    assert(dataRow3.getCell(18) == null)

    val dataRow4 = sheet.getRow(4)
    assert(dataRow4.getCell(0).getStringCellValue == "Finland")
    assert(dataRow4.getCell(1).getNumericCellValue == 2751)
    assert(dataRow4.getCell(2).getNumericCellValue == 1287)
    assert(dataRow4.getCell(3).getNumericCellValue == 0)
    assert(dataRow4.getCell(4).getNumericCellValue == 0)
    assert(dataRow4.getCell(5).getNumericCellValue == 0)
    assert(dataRow4.getCell(6).getNumericCellValue == 0)
    assert(dataRow4.getCell(7).getNumericCellValue == 0)
    assert(dataRow4.getCell(8).getNumericCellValue == 0)
    assert(dataRow4.getCell(9).getNumericCellValue == 0)
    assert(dataRow4.getCell(10).getNumericCellValue == 252)
    assert(dataRow4.getCell(11).getNumericCellValue == 252)
    assert(dataRow4.getCell(12).getNumericCellValue == 1287)
    assert(dataRow4.getCell(13).getNumericCellValue == 694)
    assert(dataRow4.getCell(14).getNumericCellValue == 347)
    assert(dataRow4.getCell(15).getNumericCellValue == 209)
    assert(dataRow4.getCell(16).getNumericCellValue == 122)
    assert(dataRow4.getCell(17).getNumericCellValue == 92)
    assert(dataRow4.getCell(18) == null)

    // summary rows
    val summaryRow = sheet.getRow(5)
    assert(summaryRow.getCell(0).getStringCellValue == "Yhteensä SV")
    assert(summaryRow.getCell(1).getNumericCellValue == 2759)
    assert(summaryRow.getCell(2).getNumericCellValue == 1289)
    assert(summaryRow.getCell(3).getNumericCellValue == 0)
    assert(summaryRow.getCell(4).getNumericCellValue == 0)
    assert(summaryRow.getCell(5).getNumericCellValue == 0)
    assert(summaryRow.getCell(6).getNumericCellValue == 0)
    assert(summaryRow.getCell(7).getNumericCellValue == 0)
    assert(summaryRow.getCell(8).getNumericCellValue == 0)
    assert(summaryRow.getCell(9).getNumericCellValue == 0)
    assert(summaryRow.getCell(10).getNumericCellValue == 431)
    assert(summaryRow.getCell(11).getNumericCellValue == 431)
    assert(summaryRow.getCell(12).getNumericCellValue == 1289)
    assert(summaryRow.getCell(13).getNumericCellValue == 696)
    assert(summaryRow.getCell(14).getNumericCellValue == 350)
    assert(summaryRow.getCell(15).getNumericCellValue == 209)
    assert(summaryRow.getCell(16).getNumericCellValue == 122)
    assert(summaryRow.getCell(17).getNumericCellValue == 93)
    assert(summaryRow.getCell(18) == null)

    val hakijatSummaryRow = sheet.getRow(6)
    assert(hakijatSummaryRow.getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(hakijatSummaryRow.getCell(1).getNumericCellValue == 2759)
    assert(hakijatSummaryRow.getCell(2).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(3).getNumericCellValue == 1289)
    assert(hakijatSummaryRow.getCell(4).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(5).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(6).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(7).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(8).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(9).getNumericCellValue == 0)
    assert(hakijatSummaryRow.getCell(10) == null)
  }

  it should "return excel with only hakijat yhteensä summary row heading row and provided data rows for hakukohderyhmittain" in {
    val data = List(
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "Automaattinen hakukelpoisuus kevät II 2024",
          Fi -> "Automaattinen hakukelpoisuus kevät II 2024",
          Sv -> "Automaattinen hakukelpoisuus kevät II 2024"),
        hakijat = 5661,
        ensisijaisia = 1309,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 242,
        aloituspaikat = 242,
        toive1 = 1309,
        toive2 = 1163,
        toive3 = 993,
        toive4 = 1003,
        toive5 = 892,
        toive6 = 301
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "Hammaslääketieteen yhteisvalinta 2024",
          Fi -> "Hammaslääketieteen yhteisvalinta 2024",
          Sv -> "Hammaslääketieteen yhteisvalinta 2024"),
        hakijat = 1078,
        ensisijaisia = 269,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 52,
        aloituspaikat = 52,
        toive1 = 269,
        toive2 = 241,
        toive3 = 220,
        toive4 = 219,
        toive5 = 79,
        toive6 = 50
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "Hoitotieteen/terveystieteiden valintakoeyhteistyö 2024",
          Fi -> "Hoitotieteen/terveystieteiden valintakoeyhteistyö 2024",
          Sv -> "Hoitotieteen/terveystieteiden valintakoeyhteistyö 2024"),
        hakijat = 0,
        ensisijaisia = 0,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 0,
        aloituspaikat = 0,
        toive1 = 0,
        toive2 = 0,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      )
    )

    val workbook: XSSFWorkbook = ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
      "sv",
      translations,
      data,
      yksittaisetHakijat = 6739,
      ensikertalaisetYksittaisetHakijat = 1578,
      maksuvelvollisetYksittaisetHakijat = 0,
      naytaHakutoiveet = true,
      tulostustapa = "hakukohderyhmittain"
    )

    assert(workbook.getNumberOfSheets == 1)
    val sheet = workbook.getSheetAt(0)

    // otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Hakukohderyhma SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Ensikertalaisia SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Maksuvelvollisia SV")
    assert(headingRow.getCell(10).getStringCellValue == "Valinnan aloituspaikat SV")
    assert(headingRow.getCell(11).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getCell(12).getStringCellValue == "Toive1 SV")
    assert(headingRow.getCell(13).getStringCellValue == "Toive2 SV")
    assert(headingRow.getCell(14).getStringCellValue == "Toive3 SV")
    assert(headingRow.getCell(15).getStringCellValue == "Toive4 SV")
    assert(headingRow.getCell(16).getStringCellValue == "Toive5 SV")
    assert(headingRow.getCell(17).getStringCellValue == "Toive6 SV")
    assert(headingRow.getCell(18) == null)

    // data rows
    val dataRow1 = sheet.getRow(1)
    assert(dataRow1.getCell(0).getStringCellValue == "Automaattinen hakukelpoisuus kevät II 2024")
    assert(dataRow1.getCell(1).getNumericCellValue == 5661)
    assert(dataRow1.getCell(2).getNumericCellValue == 1309)
    assert(dataRow1.getCell(3).getNumericCellValue == 0)
    assert(dataRow1.getCell(4).getNumericCellValue == 0)
    assert(dataRow1.getCell(5).getNumericCellValue == 0)
    assert(dataRow1.getCell(6).getNumericCellValue == 0)
    assert(dataRow1.getCell(7).getNumericCellValue == 0)
    assert(dataRow1.getCell(8).getNumericCellValue == 0)
    assert(dataRow1.getCell(9).getNumericCellValue == 0)
    assert(dataRow1.getCell(10).getNumericCellValue == 242)
    assert(dataRow1.getCell(11).getNumericCellValue == 242)
    assert(dataRow1.getCell(12).getNumericCellValue == 1309)
    assert(dataRow1.getCell(13).getNumericCellValue == 1163)
    assert(dataRow1.getCell(14).getNumericCellValue == 993)
    assert(dataRow1.getCell(15).getNumericCellValue == 1003)
    assert(dataRow1.getCell(16).getNumericCellValue == 892)
    assert(dataRow1.getCell(17).getNumericCellValue == 301)
    assert(dataRow1.getCell(18) == null)

    val dataRow2 = sheet.getRow(2)
    assert(dataRow2.getCell(0).getStringCellValue == "Hammaslääketieteen yhteisvalinta 2024")
    assert(dataRow2.getCell(1).getNumericCellValue == 1078)
    assert(dataRow2.getCell(2).getNumericCellValue == 269)
    assert(dataRow2.getCell(3).getNumericCellValue == 0)
    assert(dataRow2.getCell(4).getNumericCellValue == 0)
    assert(dataRow2.getCell(5).getNumericCellValue == 0)
    assert(dataRow2.getCell(6).getNumericCellValue == 0)
    assert(dataRow2.getCell(7).getNumericCellValue == 0)
    assert(dataRow2.getCell(8).getNumericCellValue == 0)
    assert(dataRow2.getCell(9).getNumericCellValue == 0)
    assert(dataRow2.getCell(10).getNumericCellValue == 52)
    assert(dataRow2.getCell(11).getNumericCellValue == 52)
    assert(dataRow2.getCell(12).getNumericCellValue == 269)
    assert(dataRow2.getCell(13).getNumericCellValue == 241)
    assert(dataRow2.getCell(14).getNumericCellValue == 220)
    assert(dataRow2.getCell(15).getNumericCellValue == 219)
    assert(dataRow2.getCell(16).getNumericCellValue == 79)
    assert(dataRow2.getCell(17).getNumericCellValue == 50)
    assert(dataRow2.getCell(18) == null)

    val dataRow3 = sheet.getRow(3)
    assert(dataRow3.getCell(0).getStringCellValue == "Hoitotieteen/terveystieteiden valintakoeyhteistyö 2024")
    assert(dataRow3.getCell(1).getNumericCellValue == 0)
    assert(dataRow3.getCell(2).getNumericCellValue == 0)
    assert(dataRow3.getCell(3).getNumericCellValue == 0)
    assert(dataRow3.getCell(4).getNumericCellValue == 0)
    assert(dataRow3.getCell(5).getNumericCellValue == 0)
    assert(dataRow3.getCell(6).getNumericCellValue == 0)
    assert(dataRow3.getCell(7).getNumericCellValue == 0)
    assert(dataRow3.getCell(8).getNumericCellValue == 0)
    assert(dataRow3.getCell(9).getNumericCellValue == 0)
    assert(dataRow3.getCell(10).getNumericCellValue == 0)
    assert(dataRow3.getCell(11).getNumericCellValue == 0)
    assert(dataRow3.getCell(12).getNumericCellValue == 0)
    assert(dataRow3.getCell(13).getNumericCellValue == 0)
    assert(dataRow3.getCell(14).getNumericCellValue == 0)
    assert(dataRow3.getCell(15).getNumericCellValue == 0)
    assert(dataRow3.getCell(16).getNumericCellValue == 0)
    assert(dataRow3.getCell(17).getNumericCellValue == 0)
    assert(dataRow3.getCell(18) == null)

    // vain hakijat yhteensä summarivi
    val hakijatSummaryRow = sheet.getRow(4)
    assert(hakijatSummaryRow.getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(hakijatSummaryRow.getCell(1).getNumericCellValue == 6739)
    assert(hakijatSummaryRow.getCell(2).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(3).getNumericCellValue == 1578)
    assert(hakijatSummaryRow.getCell(4).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(5).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(6).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(7).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(8).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(9).getNumericCellValue == 0)
    assert(hakijatSummaryRow.getCell(10) == null)

    assert(sheet.getPhysicalNumberOfRows == 5)
  }
}
