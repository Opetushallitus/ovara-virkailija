package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*
import org.apache.poi.xssf.usermodel.{XSSFCellStyle, XSSFSheet, XSSFWorkbook}
import org.scalatest.flatspec.AnyFlatSpec

class ExcelWriterSpec extends AnyFlatSpec {
  val userLng: String = "sv"

  "countAloituspaikat" should "return 5 for one hakukohde with 5 aloituspaikkaa" in {
    val organisaationKoulutuksetToteutuksetHakukohteet = List(
      KoulutuksetToteutuksetHakukohteetResult(
        hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(5),
        onValintakoe = Some(false),
        organisaatio_oid = Some("1.2.246.562.10.278170642010"),
        organisaatio_nimi =
          Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
        organisaatiotyypit = List("02")
      )
    )

    assert(ExcelWriter.countAloituspaikat(organisaationKoulutuksetToteutuksetHakukohteet) == 5)
  }

  it should "return 35 for three hakukohde aloituspaikat summed up" in {
    val kth = KoulutuksetToteutuksetHakukohteetResult(
      hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000021565",
      koulutuksenTila = Some("julkaistu"),
      toteutuksenTila = Some("julkaistu"),
      hakukohteenTila = Some("julkaistu"),
      aloituspaikat = Some(5),
      onValintakoe = Some(false),
      organisaatio_oid = Some("1.2.246.562.10.278170642010"),
      organisaatio_nimi =
        Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
      organisaatiotyypit = List("02")
    )

    val kth2 = kth.copy(
      hakukohdeNimi = Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000012345",
      aloituspaikat = Some(20)
    )

    val kth3 = kth.copy(
      hakukohdeNimi = Map(En -> "Hakukohteen 3 nimi en", Fi -> "Hakukohteen 3 nimi fi", Sv -> "Hakukohteen 3 nimi sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000025467",
      aloituspaikat = Some(10)
    )

    val organisaationKoulutuksetToteutuksetHakukohteet = List(kth, kth2, kth3)

    assert(ExcelWriter.countAloituspaikat(organisaationKoulutuksetToteutuksetHakukohteet) == 35)
  }

  it should "return 0 if there are no results" in {
    assert(ExcelWriter.countAloituspaikat(List()) == 0)
  }

  "flattenHierarkiaHakukohteet" should "return list with one hakukohde for one org in hierarkia" in {
    val kth = KoulutuksetToteutuksetHakukohteetResult(
      hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000021565",
      koulutuksenTila = Some("julkaistu"),
      toteutuksenTila = Some("julkaistu"),
      hakukohteenTila = Some("julkaistu"),
      aloituspaikat = Some(8),
      onValintakoe = Some(false),
      organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
      organisaatio_nimi = Map(
        En -> "Koulutustoimijan nimi en",
        Fi -> "Koulutustoimijan nimi fi",
        Sv -> "Koulutustoimijan nimi sv"
      ),
      organisaatiotyypit = List("01")
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
        List(),
        List(kth)
      )

    assert(ExcelWriter.flattenHierarkiaHakukohteet(hierarkiaWithHakukohteet) == List(kth))
  }

  it should "return list with two hakukohde for koulutustoimija and oppilaitos in hierarkia" in {
    val kth = KoulutuksetToteutuksetHakukohteetResult(
      hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000021565",
      koulutuksenTila = Some("julkaistu"),
      toteutuksenTila = Some("julkaistu"),
      hakukohteenTila = Some("julkaistu"),
      aloituspaikat = Some(3),
      onValintakoe = Some(false),
      organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
      organisaatio_nimi = Map(
        En -> "Koulutustoimijan nimi en",
        Fi -> "Koulutustoimijan nimi fi",
        Sv -> "Koulutustoimijan nimi sv"
      ),
      organisaatiotyypit = List("01")
    )

    val oppilaitoksenKth = KoulutuksetToteutuksetHakukohteetResult(
      hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000021565",
      koulutuksenTila = Some("julkaistu"),
      toteutuksenTila = Some("julkaistu"),
      hakukohteenTila = Some("julkaistu"),
      aloituspaikat = Some(8),
      onValintakoe = Some(false),
      organisaatio_oid = Some("1.2.246.562.10.2781706420111"),
      organisaatio_nimi = Map(
        En -> "Oppilaitoksen nimi en",
        Fi -> "Oppilaitoksen nimi fi",
        Sv -> "Oppilaitoksen nimi sv"
      ),
      organisaatiotyypit = List("02")
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
            List(),
            List(oppilaitoksenKth)
          )
        ),
        List(kth)
      )

    assert(ExcelWriter.flattenHierarkiaHakukohteet(hierarkiaWithHakukohteet) == List(kth, oppilaitoksenKth))
  }

  it should "return list with two oppilaitos hakukohde in hierarkia" in {
    val kth1 = KoulutuksetToteutuksetHakukohteetResult(
      hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000021565",
      koulutuksenTila = Some("julkaistu"),
      toteutuksenTila = Some("julkaistu"),
      hakukohteenTila = Some("julkaistu"),
      aloituspaikat = Some(8),
      onValintakoe = Some(false),
      organisaatio_oid = Some("1.2.246.562.10.278170642010"),
      organisaatio_nimi =
        Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
      organisaatiotyypit = List("02")
    )

    val kth2 = kth1.copy(
      hakukohdeNimi = Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
      hakukohdeOid = "1.2.246.562.20.0000000000000002156667"
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
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.278170642010",
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
            List("02"),
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
            List(),
            List(kth1, kth2)
          )
        ),
        List()
      )

    assert(ExcelWriter.flattenHierarkiaHakukohteet(hierarkiaWithHakukohteet) == List(kth1, kth2))
  }

  it should "return list with one toimipiste hakukohde and two oppilaitos hakukohde in hierarkia" in {
    val oppilaitoksenKth1 = KoulutuksetToteutuksetHakukohteetResult(
      hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000021565",
      koulutuksenTila = Some("julkaistu"),
      toteutuksenTila = Some("julkaistu"),
      hakukohteenTila = Some("julkaistu"),
      aloituspaikat = Some(8),
      onValintakoe = Some(false),
      organisaatio_oid = Some("1.2.246.562.10.278170642010"),
      organisaatio_nimi =
        Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
      organisaatiotyypit = List("02")
    )

    val oppilaitoksenKth2 = oppilaitoksenKth1.copy(
      hakukohdeNimi = Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
      hakukohdeOid = "1.2.246.562.20.0000000000000002156667"
    )

    val toimipisteenKth = oppilaitoksenKth1.copy(
      hakukohdeNimi = Map(
        En -> "Toimipiste hakukohteen nimi en",
        Fi -> "Toimipiste hakukohteen nimi fi",
        Sv -> "Toimipiste hakukohteen nimi sv"
      ),
      hakukohdeOid = "1.2.246.562.20.000000000000000215521",
      organisaatio_oid = Some("1.2.246.562.10.2781706420101111"),
      organisaatio_nimi = Map(En -> "Toimipisteen nimi en", Fi -> "Toimipisteen nimi fi", Sv -> "Toimipisteen nimi sv"),
      organisaatiotyypit = List("03")
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
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.278170642010",
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
            List("02"),
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
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

  "flattenHierarkiat" should "return list with one toimipiste hakukohde and two oppilaitos hakukohde in hierarkia" in {
    val oppilaitoksenKth1 = KoulutuksetToteutuksetHakukohteetResult(
      hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000021565",
      koulutuksenTila = Some("julkaistu"),
      toteutuksenTila = Some("julkaistu"),
      hakukohteenTila = Some("julkaistu"),
      aloituspaikat = Some(8),
      onValintakoe = Some(false),
      organisaatio_oid = Some("1.2.246.562.10.278170642010"),
      organisaatio_nimi =
        Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
      organisaatiotyypit = List("02")
    )

    val oppilaitoksenKth2 = oppilaitoksenKth1.copy(
      hakukohdeNimi = Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
      hakukohdeOid = "1.2.246.562.20.0000000000000002156667"
    )

    val toimipisteenKth = oppilaitoksenKth1.copy(
      hakukohdeNimi = Map(
        En -> "Toimipiste hakukohteen nimi en",
        Fi -> "Toimipiste hakukohteen nimi fi",
        Sv -> "Toimipiste hakukohteen nimi sv"
      ),
      hakukohdeOid = "1.2.246.562.20.000000000000000215521",
      organisaatio_oid = Some("1.2.246.562.10.2781706420101111"),
      organisaatio_nimi = Map(En -> "Toimipisteen nimi en", Fi -> "Toimipisteen nimi fi", Sv -> "Toimipisteen nimi sv"),
      organisaatiotyypit = List("03")
    )

    val hierarkiaWithHakukohteet =
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.278170642010",
        Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
        List("02"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
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
            List(),
            List(toimipisteenKth)
          ),
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.2781706420101112",
            Map(En -> "Toimipisteen 2 nimi en", Fi -> "Toimipisteen 2 nimi fi", Sv -> "Toimipisteen 2 nimi sv"),
            List("03"),
            List(
              "1.2.246.562.10.00000000001",
              "1.2.246.562.10.2781706420000",
              "1.2.246.562.10.278170642010",
              "1.2.246.562.10.2781706420101112"
            ),
            List(),
            List()
          )
        ),
        List(oppilaitoksenKth1, oppilaitoksenKth2)
      )

    assert(
      ExcelWriter.flattenHierarkiat(hierarkiaWithHakukohteet) == List(
        OrganisaatioHierarkiaWithHakukohteet(
          "1.2.246.562.10.278170642010",
          Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
          List("02"),
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
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
              List(),
              List(toimipisteenKth)
            ),
            OrganisaatioHierarkiaWithHakukohteet(
              "1.2.246.562.10.2781706420101112",
              Map(En -> "Toimipisteen 2 nimi en", Fi -> "Toimipisteen 2 nimi fi", Sv -> "Toimipisteen 2 nimi sv"),
              List("03"),
              List(
                "1.2.246.562.10.00000000001",
                "1.2.246.562.10.2781706420000",
                "1.2.246.562.10.278170642010",
                "1.2.246.562.10.2781706420101112"
              ),
              List(),
              List()
            )
          ),
          List(oppilaitoksenKth1, oppilaitoksenKth2)
        ),
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
          List(),
          List(toimipisteenKth)
        ),
        OrganisaatioHierarkiaWithHakukohteet(
          "1.2.246.562.10.2781706420101112",
          Map(En -> "Toimipisteen 2 nimi en", Fi -> "Toimipisteen 2 nimi fi", Sv -> "Toimipisteen 2 nimi sv"),
          List("03"),
          List(
            "1.2.246.562.10.00000000001",
            "1.2.246.562.10.2781706420000",
            "1.2.246.562.10.278170642010",
            "1.2.246.562.10.2781706420101112"
          ),
          List(),
          List()
        )
      )
    )
  }

  "createOrganisaatioHeadingRow" should "create row with org name and total count of aloituspaikat" in {
    val organisaatio = Organisaatio(
      organisaatio_oid = "1.2.246.562.10.2781706420000",
      organisaatio_nimi =
        Map(En -> "Koulutustoimijan nimi en", Fi -> "Koulutustoimijan nimi fi", Sv -> "Koulutustoimijan nimi sv"),
      organisaatiotyypit = List("01")
    )

    val organisaationKoulutuksetToteutuksetHakukohteet = List(
      KoulutuksetToteutuksetHakukohteetResult(
        hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(8),
        onValintakoe = Some(false),
        organisaatio_oid = Some("1.2.246.562.10.278170642010"),
        organisaatio_nimi =
          Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
        organisaatiotyypit = List("02")
      )
    )

    val workbook: XSSFWorkbook          = new XSSFWorkbook()
    val sheet: XSSFSheet                = workbook.createSheet()
    val row                             = sheet.createRow(0)
    val headingCellstyle: XSSFCellStyle = workbook.createCellStyle()
    val titles = KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES.getOrElse(
      userLng,
      KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES.getOrElse("fi", List())
    )
    val raporttiColumnTitlesWithIndex = titles.zipWithIndex

    val organisaatioRow = ExcelWriter.createOrganisaatioHeadingRow(
      row = row,
      headingCellStyle = headingCellstyle,
      asiointikieli = userLng,
      organisaatio = organisaatio,
      organisaationKoulutuksetToteutuksetHakukohteet = organisaationKoulutuksetToteutuksetHakukohteet,
      raporttiColumnTitlesWithIndex = raporttiColumnTitlesWithIndex
    )
    assert(organisaatioRow.getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")
    assert(sheet.getRow(0).getCell(5).getNumericCellValue == 8)
  }

  "writeExcel" should "create one sheet and set 'Yhteenveto' as the name of the sheet" in {
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
        List(),
        List()
      )
    )

    val wb =
      ExcelWriter.writeRaportti(hierarkiatWithHakukohteet, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES, userLng)
    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetName(0) == "Yhteenveto")
  }

  it should "create one sheet with the column title row and no results" in {
    val hierarkiatWithHakukohteet = List()
    val wb =
      ExcelWriter.writeRaportti(hierarkiatWithHakukohteet, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES, userLng)
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
        List(),
        List(
          KoulutuksetToteutuksetHakukohteetResult(
            hakukohdeNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
            hakukohdeOid = "1.2.246.562.20.00000000000000021565",
            koulutuksenTila = Some("julkaistu"),
            toteutuksenTila = Some("julkaistu"),
            hakukohteenTila = Some("julkaistu"),
            aloituspaikat = Some(8),
            onValintakoe = Some(false),
            organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
            organisaatio_nimi = Map(
              En -> "Koulutustoimijan nimi en",
              Fi -> "Koulutustoimijan nimi fi",
              Sv -> "Koulutustoimijan nimi sv"
            ),
            organisaatiotyypit = List("01")
          )
        )
      )
    )

    val wb =
      ExcelWriter.writeRaportti(hierarkiatWithHakukohteet, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES, userLng)
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
    for (i <- 1 until 5) {
      assert(sheet.getRow(1).getCell(i) == null)
    }
    assert(sheet.getRow(1).getCell(5).getNumericCellValue == 8)
    for (i <- 6 until 8) {
      assert(sheet.getRow(1).getCell(i) == null)
    }
    assert(sheet.getPhysicalNumberOfRows == 2)
  }

  //TODO: JATKA TÄSTÄ
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
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.278170642010",
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
            List("02"),
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
            List(),
            List(
              KoulutuksetToteutuksetHakukohteetResult(
                hakukohdeNimi =
                  Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
                hakukohdeOid = "1.2.246.562.20.00000000000000021565",
                koulutuksenTila = Some("julkaistu"),
                toteutuksenTila = Some("julkaistu"),
                hakukohteenTila = Some("julkaistu"),
                aloituspaikat = Some(8),
                onValintakoe = Some(false),
                organisaatio_oid = Some("1.2.246.562.10.278170642010"),
                organisaatio_nimi =
                  Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
                organisaatiotyypit = List("02")
              ),
              KoulutuksetToteutuksetHakukohteetResult(
                hakukohdeNimi =
                  Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
                hakukohdeOid = "1.2.246.562.20.00000000000000031232",
                koulutuksenTila = Some("julkaistu"),
                toteutuksenTila = Some("tallennettu"),
                hakukohteenTila = Some("tallennettu"),
                aloituspaikat = Some(5),
                onValintakoe = Some(true),
                organisaatio_oid = Some("1.2.246.562.10.278170642010"),
                organisaatio_nimi =
                  Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
                organisaatiotyypit = List("02")
              )
            )
          )
        ),
        List()
      )
    )
    val results = Map(
      "1.2.246.562.10.2781706420000" -> List(
        (
          Organisaatio(
            organisaatio_oid = "1.2.246.562.10.2781706420000",
            organisaatio_nimi =
              Map(En -> "Koulutustoimijan nimi en", Fi -> "Koulutustoimijan nimi fi", Sv -> "Koulutustoimijan nimi sv"),
            organisaatiotyypit = List("01")
          ),
          OrganisaationKoulutuksetToteutuksetHakukohteet(
            Some(
              Organisaatio(
                organisaatio_oid = "1.2.246.562.10.278170642010",
                organisaatio_nimi =
                  Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
                organisaatiotyypit = List("02")
              )
            ),
            Vector(
              KoulutuksetToteutuksetHakukohteetResult(
                hakukohdeNimi =
                  Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
                hakukohdeOid = "1.2.246.562.20.00000000000000021565",
                koulutuksenTila = Some("julkaistu"),
                toteutuksenTila = Some("julkaistu"),
                hakukohteenTila = Some("julkaistu"),
                aloituspaikat = Some(8),
                onValintakoe = Some(false),
                organisaatio_oid = Some("1.2.246.562.10.278170642010"),
                organisaatio_nimi =
                  Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
                organisaatiotyypit = List("02")
              ),
              KoulutuksetToteutuksetHakukohteetResult(
                hakukohdeNimi =
                  Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
                hakukohdeOid = "1.2.246.562.20.00000000000000031232",
                koulutuksenTila = Some("julkaistu"),
                toteutuksenTila = Some("tallennettu"),
                hakukohteenTila = Some("tallennettu"),
                aloituspaikat = Some(5),
                onValintakoe = Some(true),
                organisaatio_oid = Some("1.2.246.562.10.278170642010"),
                organisaatio_nimi =
                  Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
                organisaatiotyypit = List("02")
              )
            )
          )
        )
      )
    )
    val wb =
      ExcelWriter.writeRaportti(hierarkiatWithHakukohteet, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES, userLng)
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
    for (i <- 1 until 5) {
      assert(sheet.getRow(1).getCell(i) == null)
    }
    assert(sheet.getRow(1).getCell(5).getNumericCellValue == 13)
    for (i <- 6 until 8) {
      assert(sheet.getRow(1).getCell(i) == null)
    }
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

  //TODO: Testi jossa ylätason organisaatiolla hakukohteita kts. kälikuva

  /*it should "create a sheet with two result rows for koulutustoimija and one result row for its oppilaitos" in {
    val results = Map(
      "1.2.246.562.10.2781706420000" -> List(
        (
          Organisaatio(
            organisaatio_oid = "1.2.246.562.10.2781706420000",
            organisaatio_nimi =
              Map(En -> "Koulutustoimijan nimi en", Fi -> "Koulutustoimijan nimi fi", Sv -> "Koulutustoimijan nimi sv"),
            organisaatiotyypit = List("01")
          ),
          OrganisaationKoulutuksetToteutuksetHakukohteet(
            Some(
              Organisaatio(
                organisaatio_oid = "1.2.246.562.10.2781706420000",
                organisaatio_nimi = Map(
                  En -> "Koulutustoimijan nimi en",
                  Fi -> "Koulutustoimijan nimi fi",
                  Sv -> "Koulutustoimijan nimi sv"
                ),
                organisaatiotyypit = List("01")
              )
            ),
            Vector(
              KoulutuksetToteutuksetHakukohteetResult(
                hakukohdeNimi =
                  Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
                hakukohdeOid = "1.2.246.562.20.00000000000000021565",
                koulutuksenTila = Some("julkaistu"),
                toteutuksenTila = Some("julkaistu"),
                hakukohteenTila = Some("julkaistu"),
                aloituspaikat = Some(8),
                onValintakoe = Some(false),
                organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
                organisaatio_nimi = Map(
                  En -> "Koulutustoimijan nimi en",
                  Fi -> "Koulutustoimijan nimi fi",
                  Sv -> "Koulutustoimijan nimi sv"
                ),
                organisaatiotyypit = List("01")
              ),
              KoulutuksetToteutuksetHakukohteetResult(
                hakukohdeNimi =
                  Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
                hakukohdeOid = "1.2.246.562.20.00000000000000031232",
                koulutuksenTila = Some("julkaistu"),
                toteutuksenTila = Some("tallennettu"),
                hakukohteenTila = Some("tallennettu"),
                aloituspaikat = Some(5),
                onValintakoe = Some(true),
                organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
                organisaatio_nimi = Map(
                  En -> "Koulutustoimijan nimi en",
                  Fi -> "Koulutustoimijan nimi fi",
                  Sv -> "Koulutustoimijan nimi sv"
                ),
                organisaatiotyypit = List("01")
              )
            )
          )
        ),
        (
          Organisaatio(
            organisaatio_oid = "1.2.246.562.10.2781706420000",
            organisaatio_nimi =
              Map(En -> "Koulutustoimijan nimi en", Fi -> "Koulutustoimijan nimi fi", Sv -> "Koulutustoimijan nimi sv"),
            organisaatiotyypit = List("01")
          ),
          OrganisaationKoulutuksetToteutuksetHakukohteet(
            Some(
              Organisaatio(
                organisaatio_oid = "1.2.246.562.10.278170642010",
                organisaatio_nimi =
                  Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
                organisaatiotyypit = List("02")
              )
            ),
            Vector(
              KoulutuksetToteutuksetHakukohteetResult(
                hakukohdeNimi = Map(
                  En -> "Oppilaitoksen hakukohteen nimi en",
                  Fi -> "Oppilaitoksen hakukohteen nimi fi",
                  Sv -> "Oppilaitoksen hakukohteen nimi sv"
                ),
                hakukohdeOid = "1.2.246.562.20.00000000000000089796",
                koulutuksenTila = Some("julkaistu"),
                toteutuksenTila = Some("julkaistu"),
                hakukohteenTila = Some("julkaistu"),
                aloituspaikat = Some(10),
                onValintakoe = Some(false),
                organisaatio_oid = Some("1.2.246.562.10.278170642010"),
                organisaatio_nimi =
                  Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
                organisaatiotyypit = List("02")
              )
            )
          )
        )
      )
    )
    val wb    = ExcelWriter.writeRaportti(results, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES, userLng)
    val sheet = wb.getSheetAt(0)
    // Parent organisaatio row with aloituspaikat sum
    assert(sheet.getRow(1).getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")
    for (i <- 1 until 5) {
      assert(sheet.getRow(1).getCell(i) == null)
    }
//    assert(sheet.getRow(1).getCell(5).getNumericCellValue == 23)
    for (i <- 6 until 8) {
      assert(sheet.getRow(1).getCell(i) == null)
    }
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

    assert(sheet.getRow(4).getCell(0).getStringCellValue == "Oppilaitoksen nimi sv")
    for (i <- 1 until 5) {
      assert(sheet.getRow(1).getCell(i) == null)
    }
    assert(sheet.getRow(4).getCell(5).getNumericCellValue == 10)

    // Oppilaitoksen hakukohteen nimi result row
    assert(sheet.getRow(5).getCell(0).getStringCellValue == "Oppilaitoksen hakukohteen nimi sv")
    assert(sheet.getRow(5).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000089796")
    assert(sheet.getRow(5).getCell(2).getStringCellValue == "julkaistu")
    assert(sheet.getRow(5).getCell(3).getStringCellValue == "julkaistu")
    assert(sheet.getRow(5).getCell(4).getStringCellValue == "julkaistu")
    assert(sheet.getRow(5).getCell(5).getNumericCellValue == 10)
    assert(sheet.getRow(5).getCell(6).getStringCellValue == "-")

    assert(sheet.getPhysicalNumberOfRows == 6)
  }*/
}
