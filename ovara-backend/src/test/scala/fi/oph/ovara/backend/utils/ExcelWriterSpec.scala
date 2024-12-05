package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.{En, Fi, KoulutuksetToteutuksetHakukohteetResult, Sv}
import org.scalatest.flatspec.AnyFlatSpec

class ExcelWriterSpec extends AnyFlatSpec {
  "writeExcel" should "create one sheet and set 'Yhteenveto' as the name of the sheet" in {
    val results = Vector()
    val wb      = ExcelWriter.writeRaportti(results, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES)
    assert(wb.getNumberOfSheets == 1)
    assert(wb.getSheetName(0) == "Yhteenveto")
  }

  it should "create one sheet with the column title row and no results" in {
    val results = Vector()
    val wb      = ExcelWriter.writeRaportti(results, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES)
    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 1)
    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Hakukohteen nimi")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "Hakukohteen oid")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "Kou.tila")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "Tot.tila")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "Hak.tila")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "Aloituspaikat")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "Koe")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "Voi suorittaa kaksoistutkinnon?")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "Voi suorittaa tutkinnon urheilijana?")
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
    val wb = ExcelWriter.writeRaportti(results, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES)
    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 2)
    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Hakukohteen nimi")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "Hakukohteen oid")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "Kou.tila")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "Tot.tila")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "Hak.tila")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "Aloituspaikat")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "Koe")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "Voi suorittaa kaksoistutkinnon?")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "Voi suorittaa tutkinnon urheilijana?")
    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Hakukohteen nimi fi")
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
    val wb = ExcelWriter.writeRaportti(results, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES)
    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 2)
    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Hakukohteen nimi fi")
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
    val wb = ExcelWriter.writeRaportti(results, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES)
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
    val wb = ExcelWriter.writeRaportti(results, KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES)
    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 3)
    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Hakukohteen nimi fi")
    assert(wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue == "Hakukohde 2 fi")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "julkaistu")
    assert(wb.getSheetAt(0).getRow(2).getCell(3).getStringCellValue == "arkistoitu")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(5).getNumericCellValue == 10)
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(6).getStringCellValue == "X")
  }

}
