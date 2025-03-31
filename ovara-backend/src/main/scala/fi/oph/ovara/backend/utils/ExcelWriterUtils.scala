package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.{Kieli, Kielistetty}
import fi.oph.ovara.backend.utils.Constants.DATE_FORMATTER_FOR_EXCEL
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFCellStyle, XSSFRow}

import java.time.LocalDate
import scala.util.matching.Regex

object ExcelWriterUtils {
  def createCell(row: XSSFRow, cellStyle: XSSFCellStyle, cellIndex: Int): XSSFCell = {
    val cell = row.createCell(cellIndex)
    cell.setCellStyle(cellStyle)
    cell
  }

  def writeStrToCell(row: XSSFRow, cellStyle: XSSFCellStyle, cellIndex: Int, str: String): Int = {
    val cell = createCell(row, cellStyle, cellIndex)
    cell.setCellValue(str)
    cellIndex + 1
  }

  def writeOptionStrToCell(row: XSSFRow, cellStyle: XSSFCellStyle, cellIndex: Int, maybeStr: Option[String]): Int = {
    val cell = createCell(row, cellStyle, cellIndex)
    val value = maybeStr match {
      case Some(str) => str
      case None      => "-"
    }
    cell.setCellValue(value)
    cellIndex + 1
  }

  def writeOptionTranslationToCell(
      row: XSSFRow,
      cellStyle: XSSFCellStyle,
      cellIndex: Int,
      maybeStr: Option[String],
      translations: Map[String, String]
  ): Int = {
    val cell = createCell(row, cellStyle, cellIndex)
    val value = maybeStr match {
      case Some(str) =>
        val lowerCaseStr = str.toLowerCase
        translations.getOrElse(s"raportti.$lowerCaseStr", s"raportti.$lowerCaseStr")
      case None => "-"
    }
    cell.setCellValue(value)
    cellIndex + 1
  }

  def writeOptionLocalDateToCell(
      row: XSSFRow,
      cellStyle: XSSFCellStyle,
      cellIndex: Int,
      localDate: Option[LocalDate]
  ): Int = {
    val cell = createCell(row, cellStyle, cellIndex)
    val value = localDate match {
      case Some(date) => date.format(DATE_FORMATTER_FOR_EXCEL)
      case None       => "-"
    }
    cell.setCellValue(value)
    cellIndex + 1
  }

  def writeKielistettyToCell(
      row: XSSFRow,
      cellStyle: XSSFCellStyle,
      cellIndex: Int,
      kielistetty: Kielistetty,
      asiointikieli: String
  ): Int = {
    val cell = createCell(row, cellStyle, cellIndex)
    val kielistettyValue = kielistetty.get(Kieli.withName(asiointikieli)) match {
      case Some(value) =>
        if (value == null) {
          "-"
        } else {
          value
        }
      case None => "-"
    }
    cell.setCellValue(kielistettyValue)
    cellIndex + 1
  }

  def writeIntToCell(row: XSSFRow, cellStyle: XSSFCellStyle, cellIndex: Int, int: Int): Int = {
    val cell = createCell(row, cellStyle, cellIndex)
    cell.setCellValue(int)
    cellIndex + 1
  }

  def writeOptionBooleanToCell(
      row: XSSFRow,
      cellStyle: XSSFCellStyle,
      cellIndex: Int,
      maybeBoolean: Option[Boolean],
      translations: Map[String, String]
  ): Int = {
    val cell = createCell(row, cellStyle, cellIndex)
    val value = maybeBoolean match {
      case Some(b) =>
        if (b) translations.getOrElse("raportti.kylla", "raportti.kylla")
        else translations.getOrElse("raportti.ei", "raportti.ei")
      case None => "-"
    }
    cell.setCellValue(value)
    cellIndex + 1
  }

  def writeOptionHarkinnanvaraisuusToCell(
      row: XSSFRow,
      cellStyle: XSSFCellStyle,
      cellIndex: Int,
      maybeStr: Option[String],
      translations: Map[String, String]
  ): Int = {
    val cell = createCell(row, cellStyle, cellIndex)
    val valueToWrite = maybeStr match {
      case Some(s) =>
        if (s.startsWith("EI_HARKINNANVARAINEN")) {
          "-"
        } else {
          val r: Regex = "(ATARU|SURE)_(\\w*)".r
          val group    = for (m <- r.findFirstMatchIn(s)) yield m.group(2)
          group match {
            case Some(m) =>
              val value = if (m == "ULKOMAILLA_OPISKELTU") {
                "KOULUTODISTUSTEN_VERTAILUVAIKEUDET"
              } else {
                m
              }
              val lowerCaseStr = value.toLowerCase
              translations.getOrElse(s"raportti.$lowerCaseStr", s"raportti.$lowerCaseStr")
            case None => "-"
          }
        }
      case None => "-"
    }
    cell.setCellValue(valueToWrite)
    cellIndex + 1
  }
}
