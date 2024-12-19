package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.{Kieli, Kielistetty, Organisaatio, OrganisaationKoulutuksetToteutuksetHakukohteet, User}
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.*
import org.slf4j.{Logger, LoggerFactory}

val KOULUTUKSET_TOTEUTUKSET_HAKUKOHTEET_COLUMN_TITLES = Map(
  "fi" -> List(
    "Hakukohteen nimi",
    "Hakukohteen oid",
    "Kou.tila",
    "Tot.tila",
    "Hak.tila",
    "Aloituspaikat",
    "Koe",
    "Voi suorittaa kaksoistutkinnon?",
    "Voi suorittaa tutkinnon urheilijana?"
  ),
  "sv" -> List(
    "Hakukohteen nimi SV",
    "Hakukohteen oid SV",
    "Kou.tila SV",
    "Tot.tila SV",
    "Hak.tila SV",
    "Aloituspaikat SV",
    "Koe SV",
    "Voi suorittaa kaksoistutkinnon? SV",
    "Voi suorittaa tutkinnon urheilijana? SV"
  )
)

object ExcelWriter {
  val LOG: Logger = LoggerFactory.getLogger("ExcelWriter")

  def countAloituspaikat(organisaationKoulutuksetToteutuksetHakukohteet: OrganisaationKoulutuksetToteutuksetHakukohteet): Int = {
    val koulutuksetToteutuksetHakukohteet = organisaationKoulutuksetToteutuksetHakukohteet.koulutuksetToteutuksetHakukohteet
    koulutuksetToteutuksetHakukohteet.flatMap(kth => kth.aloituspaikat).sum
  }

  // TODO: userin sijaan asiointikieli parametrina?
  def writeRaportti(queryResult: List[(Organisaatio, OrganisaationKoulutuksetToteutuksetHakukohteet)],
                    raporttiColumnTitles: Map[String, List[String]],
                    user: User): XSSFWorkbook = {
    val asiointikieli = user.asiointikieli.getOrElse("fi")
    val workbook: XSSFWorkbook = new XSSFWorkbook()
    try {
      LOG.info("Creating new excel from db results")
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
      workbook.setSheetName(0, WorkbookUtil.createSafeSheetName("Yhteenveto")) //TODO: käännös
      val row = sheet.createRow(0)
      val titles = raporttiColumnTitles.getOrElse(
        asiointikieli, 
        raporttiColumnTitles.getOrElse("fi", List()))
      val raporttiColumnTitlesWithIndex = titles.zipWithIndex
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
                val kielistettyValue = kielistetty(Kieli.withName(asiointikieli))
                cell.setCellValue(kielistettyValue)
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
    } catch {
      case e: Exception =>
        LOG.error(s"Error creating excel: ${e.getMessage}")
        throw e
    }

    workbook
  }
}
