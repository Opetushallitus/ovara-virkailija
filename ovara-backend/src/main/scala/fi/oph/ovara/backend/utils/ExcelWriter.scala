package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*
import org.apache.poi.ss.usermodel.HorizontalAlignment
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
      indentedHeadingCellStyle: XSSFCellStyle,
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
      orgNameCell.setCellStyle(indentedHeadingCellStyle)

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

  private def createHakukohdeRow(
      sheet: XSSFSheet,
      initialRowIndex: Int,
      resultRowIndex: Int,
      cellStyle: XSSFCellStyle,
      kth: KoulutuksetToteutuksetHakukohteetResult,
      asiointikieli: String
  ) = {
    val hakukohteenTiedotRow = sheet.createRow(initialRowIndex)

    for (i <- 0 until kth.productArity) yield {
      val cell = hakukohteenTiedotRow.createCell(i)
      cell.setCellStyle(cellStyle)
      kth.productElement(i) match {
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

    initialRowIndex + 1
  }

  def createResultRows(
      workbook: XSSFWorkbook,
      sheet: XSSFSheet,
      hierarkiatWithHakukohteet: List[OrganisaatioHierarkiaWithHakukohteet],
      initialRowIndex: Int,
      headingCellStyle: XSSFCellStyle,
      cellStyle: XSSFCellStyle,
      headingFont: XSSFFont,
      asiointikieli: String,
      raporttiColumnTitlesWithIndex: List[(String, Int)]
  ): Unit = {
    val allHierarkiat   = hierarkiatWithHakukohteet.flatMap(child => flattenHierarkiat(child))
    val hakukohteet     = allHierarkiat.flatMap(_.hakukohteet)
    var currentRowIndex = initialRowIndex

    if (hierarkiatWithHakukohteet.nonEmpty) {
      hierarkiatWithHakukohteet.foreach(orgHierarkiaWithResults => {

        var indent = 0
        if (orgHierarkiaWithResults.organisaatiotyypit.contains(OPPILAITOSORGANISAATIOTYYPPI)) {
          indent = 1
        } else if (orgHierarkiaWithResults.organisaatiotyypit.contains(TOIMIPISTEORGANISAATIOTYYPPI)) {
          indent = 2
        }

        val indentedHeadingCellStyle: XSSFCellStyle = workbook.createCellStyle()
        indentedHeadingCellStyle.setFont(headingFont)
        indentedHeadingCellStyle.setAlignment(HorizontalAlignment.LEFT)
        indentedHeadingCellStyle.setIndention(indent.toShort)

        val updatedRowIndex = createOrganisaatioHeadingRow(
          sheet = sheet,
          initialRowIndex = currentRowIndex,
          indentedHeadingCellStyle = indentedHeadingCellStyle,
          headingCellStyle = headingCellStyle,
          asiointikieli = asiointikieli,
          hierarkiaWithHakukohteet = orgHierarkiaWithResults,
          raporttiColumnTitlesWithIndex = raporttiColumnTitlesWithIndex
        )
        currentRowIndex = updatedRowIndex

        orgHierarkiaWithResults.hakukohteet.zipWithIndex.foreach((hakukohde, resultRowIndex) => {
          currentRowIndex =
            createHakukohdeRow(sheet, currentRowIndex, resultRowIndex, cellStyle, hakukohde, asiointikieli)
        })

        if (orgHierarkiaWithResults.children.nonEmpty) {
          createResultRows(
            workbook,
            sheet,
            orgHierarkiaWithResults.children,
            currentRowIndex,
            headingCellStyle,
            cellStyle,
            headingFont,
            asiointikieli,
            raporttiColumnTitlesWithIndex
          )
        }
      })
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
      val sheet: XSSFSheet                 = workbook.createSheet()
      val headingCellStyle: XSSFCellStyle  = workbook.createCellStyle()
      val bodyTextCellStyle: XSSFCellStyle = workbook.createCellStyle()
      val dataformat: XSSFDataFormat       = workbook.createDataFormat()
      val headingFont                      = workbook.createFont()
      val bodyTextFont                     = workbook.createFont()

      headingFont.setFontHeightInPoints(12)
      headingFont.setBold(true)
      headingCellStyle.setFont(headingFont)
      headingCellStyle.setAlignment(HorizontalAlignment.LEFT)
      headingCellStyle.setDataFormat(dataformat.getFormat("text"))

      bodyTextFont.setFontHeightInPoints(10)
      bodyTextCellStyle.setFont(bodyTextFont)
      bodyTextCellStyle.setAlignment(HorizontalAlignment.LEFT)

      workbook.setSheetName(0, WorkbookUtil.createSafeSheetName("Yhteenveto")) //TODO: käännös

      var currentRowIndex = 0
      val row             = sheet.createRow(currentRowIndex)
      currentRowIndex = currentRowIndex + 1

      val titles                        = raporttiColumnTitles.getOrElse(userLng, raporttiColumnTitles.getOrElse("fi", List()))
      val raporttiColumnTitlesWithIndex = titles.zipWithIndex
      raporttiColumnTitlesWithIndex.foreach { case (title, index) =>
        val cell = row.createCell(index)
        cell.setCellStyle(headingCellStyle)
        cell.setCellValue(title)
      }

      createResultRows(
        workbook = workbook,
        sheet = sheet,
        hierarkiatWithHakukohteet = hierarkiatWithResults,
        initialRowIndex = currentRowIndex,
        headingCellStyle = headingCellStyle,
        cellStyle = bodyTextCellStyle,
        headingFont = headingFont,
        asiointikieli = userLng,
        raporttiColumnTitlesWithIndex = raporttiColumnTitlesWithIndex
      )

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
