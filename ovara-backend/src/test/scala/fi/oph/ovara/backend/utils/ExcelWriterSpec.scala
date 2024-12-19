package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*
import org.apache.poi.xssf.usermodel.{XSSFCellStyle, XSSFSheet, XSSFWorkbook}
import org.scalatest.flatspec.AnyFlatSpec

class ExcelWriterSpec extends AnyFlatSpec {
  val user: User = User(userOid = "1.2.246.562.24.60357717666", authorities = List(), asiointikieli = Some("sv"))

  "countAloituspaikat" should "return 5 for one hakukohde with 5 aloituspaikkaa" in {
    val organisaationKoulutuksetToteutuksetHakukohteet = OrganisaationKoulutuksetToteutuksetHakukohteet(
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
          aloituspaikat = Some(5),
          onValintakoe = Some(false),
          organisaatio_oid = Some("1.2.246.562.10.278170642010"),
          organisaatio_nimi =
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
          organisaatiotyypit = List("02")
        )
      )
    )

    assert(ExcelWriter.countAloituspaikat(organisaationKoulutuksetToteutuksetHakukohteet) == 5)
  }

  it should "return 35 for three hakukohde aloituspaikat summed up" in {
    val kth = KoulutuksetToteutuksetHakukohteetResult(
      hakukohdeNimi =
        Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
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
      hakukohdeNimi =
        Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000012345",
      aloituspaikat = Some(20),
    )

    val kth3 = kth.copy(
      hakukohdeNimi =
        Map(En -> "Hakukohteen 3 nimi en", Fi -> "Hakukohteen 3 nimi fi", Sv -> "Hakukohteen 3 nimi sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000025467",
      aloituspaikat = Some(10),
    )

    val organisaationKoulutuksetToteutuksetHakukohteet = OrganisaationKoulutuksetToteutuksetHakukohteet(
      Some(
        Organisaatio(
          organisaatio_oid = "1.2.246.562.10.278170642010",
          organisaatio_nimi =
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
          organisaatiotyypit = List("02")
        )
      ),
      Vector(kth, kth2, kth3)
    )

    assert(ExcelWriter.countAloituspaikat(organisaationKoulutuksetToteutuksetHakukohteet) == 35)
  }

  it should "return 0 if there are no results" in {
    val organisaationKoulutuksetToteutuksetHakukohteet = OrganisaationKoulutuksetToteutuksetHakukohteet(
      Some(
        Organisaatio(
          organisaatio_oid = "1.2.246.562.10.278170642010",
          organisaatio_nimi =
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
          organisaatiotyypit = List("02")
        )
      ),
      Vector()
    )

    assert(ExcelWriter.countAloituspaikat(organisaationKoulutuksetToteutuksetHakukohteet) == 0)
  }

  "createOrganisaatioHeadingRow" should "create row with org name and total count of aloituspaikat" in {
    val organisaatio = Organisaatio(
      organisaatio_oid = "1.2.246.562.10.2781706420000",
      organisaatio_nimi =
        Map(En -> "Koulutustoimijan nimi en", Fi -> "Koulutustoimijan nimi fi", Sv -> "Koulutustoimijan nimi sv"),
      organisaatiotyypit = List("01"))

    val organisaationKoulutuksetToteutuksetHakukohteet = OrganisaationKoulutuksetToteutuksetHakukohteet(
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
        )
      )
    )

    val workbook: XSSFWorkbook = new XSSFWorkbook()
    val sheet: XSSFSheet = workbook.createSheet()
    val row = sheet.createRow(0)
    val headingCellstyle: XSSFCellStyle = workbook.createCellStyle()
    val titles = KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES.getOrElse(
      user.asiointikieli.get,
      KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES.getOrElse("fi", List()))
    val raporttiColumnTitlesWithIndex = titles.zipWithIndex

    val organisaatioRow = ExcelWriter.createOrganisaatioHeadingRow(
      row = row,
      headingCellStyle = headingCellstyle,
      asiointikieli = user.asiointikieli.get,
      organisaatio = Some(organisaatio),
      organisaationKoulutuksetToteutuksetHakukohteet = organisaationKoulutuksetToteutuksetHakukohteet,
      raporttiColumnTitlesWithIndex = raporttiColumnTitlesWithIndex)
    assert(organisaatioRow.getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")
  }

  "writeExcel" should "create one sheet and set 'Yhteenveto' as the name of the sheet" in {
    val results = Vector()
    val wb      = ExcelWriter.writeRaportti(results, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES, user)
    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetName(0) == "Yhteenveto")
  }

  it should "create one sheet with the column title row and no results" in {
    val results = Vector()
    val wb      = ExcelWriter.writeRaportti(results, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES, user)
    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 1)
    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Hakukohteen nimi SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "Hakukohteen oid SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "Kou.tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "Tot.tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "Hak.tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "Aloituspaikat SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "Koe SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "Voi suorittaa kaksoistutkinnon? SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "Voi suorittaa tutkinnon urheilijana? SV")
    assert(wb.getSheetAt(0).getRow(1) == null)
  }

  it should "create a sheet with the column title row and one result row with all values defined" in {
    val results = Vector(
      KoulutuksetToteutuksetHakukohteetResult(
        Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        "1.2.246.562.20.00000000000000021565",
        Some("julkaistu"),
        Some("julkaistu"),
        Some("julkaistu"),
        Some(8),
        Some(false)
      )
    )
    val wb = ExcelWriter.writeRaportti(results, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES, user)
    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 2)
    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Hakukohteen nimi SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "Hakukohteen oid SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "Kou.tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "Tot.tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "Hak.tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "Aloituspaikat SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "Koe SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "Voi suorittaa kaksoistutkinnon? SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "Voi suorittaa tutkinnon urheilijana? SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Hakukohteen nimi sv")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000021565")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "julkaistu")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "julkaistu")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "julkaistu")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getNumericCellValue == 8)
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "-")
  }

  it should "create a sheet with one result row with 'Koe', 'Voi suorittaa kaksoistutkinnon' and 'Voi suorittaa tutkinnon urheilijana' as not defined" in {
    val results = Vector(
      KoulutuksetToteutuksetHakukohteetResult(
        Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        "1.2.246.562.20.00000000000000021565",
        Some("julkaistu"),
        Some("julkaistu"),
        Some("julkaistu")
      )
    )
    val wb = ExcelWriter.writeRaportti(results, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES, user)
    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 2)
    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Hakukohteen nimi sv")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000021565")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "julkaistu")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "julkaistu")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "julkaistu")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "-")
  }

  it should "create a sheet with 'X' as the value for Koe-column when it is set as true" in {
    val results = Vector(
      KoulutuksetToteutuksetHakukohteetResult(
        Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        "1.2.246.562.20.00000000000000021565",
        Some("julkaistu"),
        Some("julkaistu"),
        Some("julkaistu"),
        Some(10),
        Some(true)
      )
    )
    val wb = ExcelWriter.writeRaportti(results, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES, user)
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "X")
  }

  it should "create a sheet with two rows from two query results" in {
    val results = Vector(
      KoulutuksetToteutuksetHakukohteetResult(
        Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        "1.2.246.562.20.00000000000000021565",
        Some("julkaistu"),
        Some("julkaistu"),
        Some("julkaistu")
      ),
      KoulutuksetToteutuksetHakukohteetResult(
        Map(En -> "Hakukohde 2 en", Fi -> "Hakukohde 2 fi", Sv -> "Hakukohde 2 sv"),
        "1.2.246.562.20.00000000000000021666",
        Some("julkaistu"),
        Some("arkistoitu"),
        Some("arkistoitu"),
        Some(10),
        Some(true)
      )
    )
    val wb = ExcelWriter.writeRaportti(results, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES, user)
    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 3)
    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Hakukohteen nimi sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue == "Hakukohde 2 sv")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "julkaistu")
    assert(wb.getSheetAt(0).getRow(2).getCell(3).getStringCellValue == "arkistoitu")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(5).getNumericCellValue == 10)
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(6).getStringCellValue == "X")
  }
}
