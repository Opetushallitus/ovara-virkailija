package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.{Fi, Kielistetty, KoulutuksetToteutuksetHakukohteetResult}
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.*

import java.io.FileOutputStream

val KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES = List(
  "Hakukohteen nimi",
  "Hakukohteen oid",
  "Kou.tila",
  "Tot.tila",
  "Hak.tila",
  "Aloituspaikat",
  "Koe",
  "Voi suorittaa kaksoistutkinnon?",
  "Voi suorittaa tutkinnon urheilijana?",
)

object ExcelWriter {
  def writeRaportti(queryResult: Vector[KoulutuksetToteutuksetHakukohteetResult], raporttiColumnTitles: List[String]): XSSFWorkbook = {
    val workbook: XSSFWorkbook = new XSSFWorkbook()
    val sheet: XSSFSheet = workbook.createSheet()
    val cellstyle: XSSFCellStyle = workbook.createCellStyle()
    val cellstyle2: XSSFCellStyle = workbook.createCellStyle()
    val dataformat: XSSFDataFormat = workbook.createDataFormat()
    val font1 = workbook.createFont()
    val font2 = workbook.createFont()
    font1.setFontHeightInPoints(12)
    font1.setBold(true)
    font2.setFontHeightInPoints(10)
    cellstyle.setFont(font1)
    cellstyle.setDataFormat(dataformat.getFormat("text"))
    cellstyle2.setFont(font2)
    workbook.setSheetName(0, WorkbookUtil.createSafeSheetName("Yhteenveto"))
    val row = sheet.createRow(0)
    val raporttiColumnTitlesWithIndex = raporttiColumnTitles.zipWithIndex
    raporttiColumnTitlesWithIndex.foreach {
      case (title, index) =>
        val cell = row.createCell(index)
        cell.setCellValue(title)
        cell.setCellStyle(cellstyle)
    }

    queryResult.zipWithIndex.foreach {
      case (result: KoulutuksetToteutuksetHakukohteetResult, index) =>
        val resultRow = sheet.createRow(1 + index)
        for (i <- 0 until result.productArity) yield {
          val cell = resultRow.createCell(i)
          cell.setCellStyle(cellstyle2)
          result.productElement(i) match {
            case kielistetty: Kielistetty =>
              // TODO: Käyttäjän kieli ja käännökset
              cell.setCellValue(kielistetty.getOrElse(Fi, ""))
            case string: String =>
              cell.setCellValue(string)
            case Some(s: String) =>
              cell.setCellValue(s)
            case Some(int: Int) =>
              cell.setCellValue(int)
            case Some(b: Boolean) =>
              val value = if (b) "X" else "-"
              cell.setCellValue(value)
            case _ =>
              cell.setCellValue("-")
          }
        }
    }

    workbook
  }
}
