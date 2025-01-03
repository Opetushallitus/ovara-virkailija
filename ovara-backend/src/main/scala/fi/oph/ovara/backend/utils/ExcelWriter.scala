package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.*
import org.slf4j.{Logger, LoggerFactory}

object ExcelWriter {

  val LOG: Logger = LoggerFactory.getLogger("ExcelWriter")

  def countAloituspaikat(
      organisaationKoulutuksetToteutuksetHakukohteet: List[KoulutuksetToteutuksetHakukohteetResult]
  ): Int = {
    val koulutuksetToteutuksetHakukohteet = organisaationKoulutuksetToteutuksetHakukohteet
    koulutuksetToteutuksetHakukohteet.flatMap(kth => kth.aloituspaikat).sum
  }

  def flattenHierarkiaHakukohteet(
      organisaatioHierarkiaWithHakukohteet: OrganisaatioHierarkiaWithHakukohteet
  ): List[KoulutuksetToteutuksetHakukohteetResult] = {
    if (organisaatioHierarkiaWithHakukohteet.children.isEmpty) {
      organisaatioHierarkiaWithHakukohteet.hakukohteet
    } else {
      val childHakukohteet =
        organisaatioHierarkiaWithHakukohteet.children.flatMap(child => flattenHierarkiaHakukohteet(child))
      organisaatioHierarkiaWithHakukohteet.hakukohteet concat childHakukohteet
    }
  }

  def flattenHierarkiat(
      organisaatioHierarkiaWithHakukohteet: OrganisaatioHierarkiaWithHakukohteet
  ): List[OrganisaatioHierarkiaWithHakukohteet] = {
    if (organisaatioHierarkiaWithHakukohteet.children.isEmpty) {
      List(organisaatioHierarkiaWithHakukohteet)
    } else {
      val childHierarkiat = organisaatioHierarkiaWithHakukohteet.children.flatMap(child => flattenHierarkiat(child))
      organisaatioHierarkiaWithHakukohteet :: childHierarkiat
    }
  }

  def createOrganisaatioHeadingRow(
      sheet: XSSFSheet,
      initialRowIndex: Int,
      headingCellStyle: XSSFCellStyle,
      asiointikieli: String,
      hierarkiaWithHakukohteet: OrganisaatioHierarkiaWithHakukohteet,
      raporttiColumnTitlesWithIndex: List[(String, Int)]
  ): Int = {
    val hakukohteet = flattenHierarkiaHakukohteet(hierarkiaWithHakukohteet)

    if (hakukohteet.nonEmpty) {
      val row             = sheet.createRow(initialRowIndex)
      val updatedRowIndex = initialRowIndex + 1

      val orgNameCell = row.createCell(0)
      orgNameCell.setCellStyle(headingCellStyle)
      val kielistettyNimi = hierarkiaWithHakukohteet.organisaatio_nimi(Kieli.withName(asiointikieli))

      orgNameCell.setCellValue(kielistettyNimi)

      raporttiColumnTitlesWithIndex.find((title, i) => title.startsWith("Aloituspaikat")) match {
        case Some((t, index)) =>
          val aloituspaikatCell = row.createCell(index)
          aloituspaikatCell.setCellStyle(headingCellStyle)
          val aloituspaikat = countAloituspaikat(hakukohteet)
          aloituspaikatCell.setCellValue(aloituspaikat)
        case _ =>
      }

      updatedRowIndex
    } else {
      initialRowIndex
    }
  }

  def writeRaportti(
      hierarkiatWithResults: List[OrganisaatioHierarkiaWithHakukohteet],
      raporttiColumnTitles: Map[String, List[String]],
      userLng: String
  ): XSSFWorkbook = {
    val workbook: XSSFWorkbook = new XSSFWorkbook()
    try {
      LOG.info("Creating new excel from db results")
      var sheet: XSSFSheet                = workbook.createSheet()
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
      var currentRowIndex = 0
      val row             = sheet.createRow(currentRowIndex)
      currentRowIndex = currentRowIndex + 1
      val titles                        = raporttiColumnTitles.getOrElse(userLng, raporttiColumnTitles.getOrElse("fi", List()))
      val raporttiColumnTitlesWithIndex = titles.zipWithIndex
      raporttiColumnTitlesWithIndex.foreach { case (title, index) =>
        val cell = row.createCell(index)
        cell.setCellStyle(headingCellstyle)
        cell.setCellValue(title)
      }

      def createHakukohdeRow(kth: KoulutuksetToteutuksetHakukohteetResult) = {
        val hakukohteenTiedotRow = sheet.createRow(currentRowIndex)
        currentRowIndex = currentRowIndex + 1

        for (i <- 0 until kth.productArity) yield {
          val cell = hakukohteenTiedotRow.createCell(i)
          cell.setCellStyle(cellstyle2)
          kth.productElement(i) match {
            case kielistetty: Kielistetty =>
              val kielistettyValue = kielistetty(Kieli.withName(userLng))
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

      val allHierarkiat         = hierarkiatWithResults.flatMap(child => flattenHierarkiat(child))
      val hakukohteet           = allHierarkiat.flatMap(_.hakukohteet)
      val allAloituspaikatCount = countAloituspaikat(hakukohteet)

      hierarkiatWithResults.foreach(orgHierarkiaWithResults => {
        val updatedRowIndex = createOrganisaatioHeadingRow(
          sheet = sheet,
          initialRowIndex = currentRowIndex,
          headingCellStyle = headingCellstyle,
          asiointikieli = userLng,
          hierarkiaWithHakukohteet = orgHierarkiaWithResults,
          raporttiColumnTitlesWithIndex = raporttiColumnTitlesWithIndex
        )
        currentRowIndex = updatedRowIndex

        orgHierarkiaWithResults.hakukohteet.foreach(hakukohde => {
          createHakukohdeRow(hakukohde)
        })

        val h           = orgHierarkiaWithResults.children.flatMap(child => flattenHierarkiat(child))
        val hakukohteet = h.flatMap(_.hakukohteet)

        h.foreach(orgHierarkiaWithResults => {
          val updatedRowIndex = createOrganisaatioHeadingRow(
            sheet = sheet,
            initialRowIndex = currentRowIndex,
            headingCellStyle = headingCellstyle,
            asiointikieli = userLng,
            hierarkiaWithHakukohteet = orgHierarkiaWithResults,
            raporttiColumnTitlesWithIndex = raporttiColumnTitlesWithIndex
          )
          currentRowIndex = updatedRowIndex

          val kths = orgHierarkiaWithResults.hakukohteet

          if (kths.nonEmpty) {
            kths.zipWithIndex.foreach((kth: KoulutuksetToteutuksetHakukohteetResult, resultRowIndex: Int) => {
              createHakukohdeRow(kth)
            })
          }
        })
      })

      // Asetetaan lopuksi kolumnien leveys automaattisesti leveimmän arvon mukaan
      raporttiColumnTitlesWithIndex.foreach { case (title, index) =>
        sheet.autoSizeColumn(index)
      }
    } catch {
      case e: Exception =>
        LOG.error(s"Error creating excel: ${e.getMessage}")
        throw e
    }

    workbook
  }
}
