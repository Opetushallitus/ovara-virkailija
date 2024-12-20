package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.*
import org.slf4j.{Logger, LoggerFactory}

object ExcelWriter {

  val LOG: Logger = LoggerFactory.getLogger("ExcelWriter")

  def countAloituspaikat(
      organisaationKoulutuksetToteutuksetHakukohteet: OrganisaationKoulutuksetToteutuksetHakukohteet
  ): Int = {
    val koulutuksetToteutuksetHakukohteet =
      organisaationKoulutuksetToteutuksetHakukohteet.koulutuksetToteutuksetHakukohteet
    koulutuksetToteutuksetHakukohteet.flatMap(kth => kth.aloituspaikat).sum
  }

  def createOrganisaatioHeadingRow(
      row: XSSFRow,
      headingCellStyle: XSSFCellStyle,
      asiointikieli: String,
      organisaatio: Option[Organisaatio],
      organisaationKoulutuksetToteutuksetHakukohteet: OrganisaationKoulutuksetToteutuksetHakukohteet,
      raporttiColumnTitlesWithIndex: List[(String, Int)]
  ): XSSFRow = {
    val orgNameCell = row.createCell(0)
    orgNameCell.setCellStyle(headingCellStyle)
    val kielistettyNimi = organisaatio match {
      case Some(org: Organisaatio) => org.organisaatio_nimi(Kieli.withName(asiointikieli))
      case None                    => "-"
    }

    orgNameCell.setCellValue(kielistettyNimi)

    raporttiColumnTitlesWithIndex.find((title, i) => title.startsWith("Aloituspaikat")) match {
      case Some((t, index)) =>
        val aloituspaikatCell = row.createCell(index)
        val aloituspaikat     = countAloituspaikat(organisaationKoulutuksetToteutuksetHakukohteet)
        aloituspaikatCell.setCellValue(aloituspaikat)
      case _ =>
    }

    row
  }

  // TODO: userin sijaan asiointikieli parametrina?
  def writeRaportti(
      queryResult: List[(Organisaatio, OrganisaationKoulutuksetToteutuksetHakukohteet)],
      raporttiColumnTitles: Map[String, List[String]],
      user: User
  ): XSSFWorkbook = {
    val asiointikieli          = user.asiointikieli.getOrElse("fi")
    val workbook: XSSFWorkbook = new XSSFWorkbook()
    try {
      LOG.info("Creating new excel from db results")
      val sheet: XSSFSheet                = workbook.createSheet()
      val headingCellstyle: XSSFCellStyle = workbook.createCellStyle()
      val cellstyle2: XSSFCellStyle       = workbook.createCellStyle()
      val dataformat: XSSFDataFormat      = workbook.createDataFormat()
      val font1                           = workbook.createFont()
      val font2                           = workbook.createFont()
      font1.setFontHeightInPoints(12)
      font1.setBold(true)
      font2.setFontHeightInPoints(10)
      headingCellstyle.setFont(font1)
      headingCellstyle.setDataFormat(dataformat.getFormat("text"))
      cellstyle2.setFont(font2)
      workbook.setSheetName(0, WorkbookUtil.createSafeSheetName("Yhteenveto")) //TODO: käännös
      val row                           = sheet.createRow(0)
      val titles                        = raporttiColumnTitles.getOrElse(asiointikieli, raporttiColumnTitles.getOrElse("fi", List()))
      val raporttiColumnTitlesWithIndex = titles.zipWithIndex
      raporttiColumnTitlesWithIndex.foreach { case (title, index) =>
        val cell = row.createCell(index)
        cell.setCellValue(title)
        cell.setCellStyle(headingCellstyle)
      }

      queryResult.zipWithIndex.foreach {
        case (result: (Organisaatio, OrganisaationKoulutuksetToteutuksetHakukohteet), index) =>
          val parentOrgRow          = sheet.createRow(1 + index)
          val parentOrg             = result._1
          val organisaationTulokset = result._2
          createOrganisaatioHeadingRow(
            parentOrgRow,
            headingCellstyle,
            asiointikieli,
            Some(parentOrg),
            organisaationTulokset,
            raporttiColumnTitlesWithIndex
          )

          val orgRow = sheet.createRow(2 + index)
          createOrganisaatioHeadingRow(
            orgRow,
            headingCellstyle,
            asiointikieli,
            organisaationTulokset._1,
            organisaationTulokset,
            raporttiColumnTitlesWithIndex
          )

          val kths                 = organisaationTulokset.koulutuksetToteutuksetHakukohteet
          val firstKth             = kths.head
          val hakukohteenTiedotRow = sheet.createRow(3 + index)

          for (i <- 0 until firstKth.productArity) yield {
            val cell = hakukohteenTiedotRow.createCell(i)
            cell.setCellStyle(cellstyle2)
            firstKth.productElement(i) match {
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
