package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*
import fi.oph.ovara.backend.utils.Constants.*
import fi.oph.ovara.backend.utils.ExcelWriterUtils.*
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.util.{CellRangeAddress, WorkbookUtil}
import org.apache.poi.xssf.usermodel.*
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.immutable.List as data

object ExcelWriter {

  val LOG: Logger = LoggerFactory.getLogger("ExcelWriter")

  def countAloituspaikat(
      organisaationKoulutuksetToteutuksetHakukohteet: List[OrganisaationKoulutusToteutusHakukohde]
  ): Int = {
    val koulutuksetToteutuksetHakukohteet = organisaationKoulutuksetToteutuksetHakukohteet
    koulutuksetToteutuksetHakukohteet.flatMap(kth => kth.koulutusToteutusHakukohde.aloituspaikat).sum
  }

  def flattenHierarkiaHakukohteet(
      organisaatioHierarkiaWithHakukohteet: OrganisaatioHierarkiaWithHakukohteet
  ): List[OrganisaationKoulutusToteutusHakukohde] = {
    if (organisaatioHierarkiaWithHakukohteet.children.isEmpty) {
      organisaatioHierarkiaWithHakukohteet.hakukohteet
    } else {
      val childHakukohteet =
        organisaatioHierarkiaWithHakukohteet.children.flatMap(child => flattenHierarkiaHakukohteet(child))
      organisaatioHierarkiaWithHakukohteet.hakukohteet concat childHakukohteet
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

      raporttiColumnTitlesWithIndex.find((title, i) => title.startsWith("aloituspaikat")) match {
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

  private def addOrgSubHeading(
      sheet: XSSFSheet,
      initialRowIndex: Int,
      orgSubHeadingCellStyle: XSSFCellStyle,
      organisaatioHierarkiaWithHakukohteet: OrganisaatioHierarkiaWithHakukohteet,
      asiointikieli: String
  ) = {
    val hakukohteet = flattenHierarkiaHakukohteet(organisaatioHierarkiaWithHakukohteet)

    if (hakukohteet.nonEmpty) {
      organisaatioHierarkiaWithHakukohteet.koulutustoimijaParent match {
        case Some(koulutustoimijaParent) =>
          val orgSubHeadingRow = sheet.createRow(initialRowIndex)
          val cell             = orgSubHeadingRow.createCell(0)
          cell.setCellStyle(orgSubHeadingCellStyle)
          val kielistettyNimi  = koulutustoimijaParent.organisaatio_nimi
          val kielistettyValue = kielistettyNimi(Kieli.withName(asiointikieli))
          cell.setCellValue(kielistettyValue)

          initialRowIndex + 1
        case _ => initialRowIndex
      }
    } else {
      initialRowIndex
    }
  }

  private def createKoulutuksetToteutuksetHakukohteetRow(
      sheet: XSSFSheet,
      initialRowIndex: Int,
      cellStyle: XSSFCellStyle,
      hakukohteenNimiCellStyle: XSSFCellStyle,
      kth: KoulutusToteutusHakukohdeResult,
      asiointikieli: String,
      translations: Map[String, String]
  ) = {
    val hakukohteenTiedotRow = sheet.createRow(initialRowIndex)
    var cellIndex            = 0

    cellIndex = writeKielistettyToCell(
      row = hakukohteenTiedotRow,
      cellStyle = hakukohteenNimiCellStyle,
      cellIndex = cellIndex,
      kielistetty = kth.hakukohteenNimi,
      asiointikieli = asiointikieli
    )
    cellIndex =
      writeStrToCell(row = hakukohteenTiedotRow, cellStyle = cellStyle, cellIndex = cellIndex, str = kth.hakukohdeOid)

    cellIndex = writeOptionTilaToCell(
      row = hakukohteenTiedotRow,
      cellStyle = cellStyle,
      cellIndex = cellIndex,
      maybeTila = kth.koulutuksenTila,
      translations = translations
    )
    cellIndex = writeOptionTilaToCell(
      row = hakukohteenTiedotRow,
      cellStyle = cellStyle,
      cellIndex = cellIndex,
      maybeTila = kth.toteutuksenTila,
      translations = translations
    )
    cellIndex = writeOptionTilaToCell(
      row = hakukohteenTiedotRow,
      cellStyle = cellStyle,
      cellIndex = cellIndex,
      maybeTila = kth.hakukohteenTila,
      translations = translations
    )
    cellIndex = writeOptionIntToCell(
      row = hakukohteenTiedotRow,
      cellStyle = cellStyle,
      cellIndex = cellIndex,
      maybeInt = kth.aloituspaikat
    )
    cellIndex = writeOptionBooleanToCell(
      row = hakukohteenTiedotRow,
      cellStyle = cellStyle,
      cellIndex = cellIndex,
      maybeBoolean = kth.onValintakoe,
      translations = translations
    )
    cellIndex = writeOptionBooleanToCell(
      row = hakukohteenTiedotRow,
      cellStyle = cellStyle,
      cellIndex = cellIndex,
      maybeBoolean = kth.voiSuorittaaKaksoistutkinnon,
      translations = translations
    )
    cellIndex = writeOptionBooleanToCell(
      row = hakukohteenTiedotRow,
      cellStyle = cellStyle,
      cellIndex = cellIndex,
      maybeBoolean = kth.jarjestaaUrheilijanAmmKoulutusta,
      translations = translations
    )

    initialRowIndex + 1
  }

  def createKoulutuksetToteutuksetHakukohteetResultRows(
      workbook: XSSFWorkbook,
      sheet: XSSFSheet,
      hierarkiatWithHakukohteet: List[OrganisaatioHierarkiaWithHakukohteet],
      initialRowIndex: Int,
      headingCellStyle: XSSFCellStyle,
      bodyTextCellStyle: XSSFCellStyle,
      hakukohteenNimiTextCellStyle: XSSFCellStyle,
      headingFont: XSSFFont,
      subHeadingFont: XSSFFont,
      asiointikieli: String,
      raporttiColumnTitlesWithIndex: List[(String, Int)],
      raporttityyppi: String,
      translations: Map[String, String]
  ): Int = {
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

        val subHeadingCellStyle: XSSFCellStyle = workbook.createCellStyle()
        subHeadingCellStyle.setFont(subHeadingFont)
        subHeadingCellStyle.setAlignment(HorizontalAlignment.LEFT)
        subHeadingCellStyle.setIndention(indent.toShort)

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

        if (
          raporttityyppi == OPPILAITOSRAPORTTI && orgHierarkiaWithResults.organisaatiotyypit
            .contains(OPPILAITOSORGANISAATIOTYYPPI)
        ) {
          val updatedRowIndex =
            addOrgSubHeading(sheet, currentRowIndex, subHeadingCellStyle, orgHierarkiaWithResults, asiointikieli)
          currentRowIndex = updatedRowIndex
        }

        if (
          raporttityyppi == TOIMIPISTERAPORTTI && orgHierarkiaWithResults.organisaatiotyypit
            .contains(TOIMIPISTEORGANISAATIOTYYPPI)
        ) {
          val updatedRowIndex =
            addOrgSubHeading(sheet, currentRowIndex, subHeadingCellStyle, orgHierarkiaWithResults, asiointikieli)
          currentRowIndex = updatedRowIndex
        }

        orgHierarkiaWithResults.hakukohteet.zipWithIndex.foreach((hakukohde, resultRowIndex) => {
          currentRowIndex = createKoulutuksetToteutuksetHakukohteetRow(
            sheet,
            currentRowIndex,
            bodyTextCellStyle,
            hakukohteenNimiTextCellStyle,
            hakukohde._2,
            asiointikieli,
            translations
          )
        })

        if (orgHierarkiaWithResults.children.nonEmpty) {
          val updatedRowIndex = createKoulutuksetToteutuksetHakukohteetResultRows(
            workbook,
            sheet,
            orgHierarkiaWithResults.children,
            currentRowIndex,
            headingCellStyle,
            bodyTextCellStyle,
            hakukohteenNimiTextCellStyle,
            headingFont,
            subHeadingFont,
            asiointikieli,
            raporttiColumnTitlesWithIndex,
            raporttityyppi,
            translations
          )
          currentRowIndex = updatedRowIndex
        }
      })
    }

    currentRowIndex
  }

  private def createHeadingFont(workbook: XSSFWorkbook, headingCellStyle: XSSFCellStyle): XSSFFont = {
    val headingFont                = workbook.createFont()
    val dataformat: XSSFDataFormat = workbook.createDataFormat()
    headingFont.setFontHeightInPoints(12)
    headingFont.setBold(true)
    headingCellStyle.setFont(headingFont)
    headingCellStyle.setAlignment(HorizontalAlignment.LEFT)
    headingCellStyle.setDataFormat(dataformat.getFormat("text"))
    headingFont
  }

  private def createSubHeadingFont(workbook: XSSFWorkbook): XSSFFont = {
    val subHeadingFont = workbook.createFont()
    subHeadingFont.setFontHeightInPoints(9)
    subHeadingFont.setItalic(true)
    subHeadingFont
  }

  private def createSummaryRowFont(workbook: XSSFWorkbook): XSSFFont = {
    val subHeadingFont = workbook.createFont()
    subHeadingFont.setFontHeightInPoints(10)
    subHeadingFont.setBold(true)
    subHeadingFont
  }

  private def createBodyTextFont(workbook: XSSFWorkbook, bodyTextCellStyle: XSSFCellStyle): XSSFFont = {
    val bodyTextFont = workbook.createFont()
    bodyTextFont.setFontHeightInPoints(10)

    bodyTextCellStyle.setFont(bodyTextFont)
    bodyTextCellStyle.setAlignment(HorizontalAlignment.LEFT)
    bodyTextFont
  }

  private def createNumericCellStyle(workbook: XSSFWorkbook): XSSFCellStyle = {
    val numericCellStyle = workbook.createCellStyle()
    numericCellStyle.setDataFormat(workbook.createDataFormat().getFormat("0"))
    numericCellStyle
  }

  private def createBodyTextCellStyle(workbook: XSSFWorkbook): XSSFCellStyle = {
    val bodyTextCellStyle = workbook.createCellStyle()
    val bodyTextFont      = createBodyTextFont(workbook, bodyTextCellStyle)
    bodyTextCellStyle.setWrapText(true)
    bodyTextCellStyle
  }

  private def createSummaryCellStyle(workbook: XSSFWorkbook): XSSFCellStyle = {
    val summaryCellStyle = workbook.createCellStyle()
    val summaryFont      = createSummaryRowFont(workbook)
    summaryCellStyle.setFont(summaryFont)
    summaryCellStyle.setAlignment(HorizontalAlignment.RIGHT)
    summaryCellStyle
  }

  def writeKoulutuksetToteutuksetHakukohteetRaportti(
      hierarkiatWithResults: List[OrganisaatioHierarkiaWithHakukohteet],
      asiointikieli: String,
      raporttityyppi: String,
      translations: Map[String, String]
  ): XSSFWorkbook = {
    val workbook: XSSFWorkbook = new XSSFWorkbook()
    try {
      LOG.info("Creating new KoulutuksetToteutuksetHakukohteetExcel from db results")
      val sheet: XSSFSheet                            = workbook.createSheet()
      val headingCellStyle: XSSFCellStyle             = workbook.createCellStyle()
      val bodyTextCellStyle: XSSFCellStyle            = workbook.createCellStyle()
      val hakukohteenNimiTextCellStyle: XSSFCellStyle = workbook.createCellStyle()

      val headingFont    = createHeadingFont(workbook, headingCellStyle)
      val subHeadingFont = createSubHeadingFont(workbook)
      val bodyTextFont   = createBodyTextFont(workbook, bodyTextCellStyle)

      hakukohteenNimiTextCellStyle.setFont(bodyTextFont)
      hakukohteenNimiTextCellStyle.setAlignment(HorizontalAlignment.LEFT)
      hakukohteenNimiTextCellStyle.setIndention(2.toShort)

      workbook.setSheetName(
        0,
        WorkbookUtil.createSafeSheetName(translations.getOrElse("raportti.yhteenveto", "raportti.yhteenveto"))
      )

      var currentRowIndex = 0
      val titles = classOf[KoulutusToteutusHakukohdeResult].getDeclaredFields
        .map(_.getName)
        .toList

      val raporttiColumnTitlesWithIndex = titles.zipWithIndex

      currentRowIndex = createHeadingRow(
        sheet = sheet,
        translations = translations,
        currentRowIndex = currentRowIndex,
        fieldNames = titles,
        headingCellStyle = headingCellStyle
      )

      createKoulutuksetToteutuksetHakukohteetResultRows(
        workbook = workbook,
        sheet = sheet,
        hierarkiatWithHakukohteet = hierarkiatWithResults,
        initialRowIndex = currentRowIndex,
        headingCellStyle = headingCellStyle,
        bodyTextCellStyle = bodyTextCellStyle,
        hakukohteenNimiTextCellStyle = hakukohteenNimiTextCellStyle,
        headingFont = headingFont,
        subHeadingFont = subHeadingFont,
        asiointikieli = asiointikieli,
        raporttiColumnTitlesWithIndex = raporttiColumnTitlesWithIndex,
        raporttityyppi = raporttityyppi,
        translations = translations
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

  private def writeKorkeakouluKoulutuksetToteutuksetHakukohteetKoulutuksittainRows(
      sheet: XSSFSheet,
      bodyTextCellStyle: XSSFCellStyle,
      currentRowIndex: Int,
      korkeakouluKoulutuksetToteutuksetHakukohteetResults: Seq[KorkeakouluKoulutusToteutusHakukohdeResult],
      asiointikieli: String,
      translations: Map[String, String]
  ): Unit = {
    var rowIndex = currentRowIndex
    korkeakouluKoulutuksetToteutuksetHakukohteetResults.foreach(result => {
      val resultRow = sheet.createRow(rowIndex)
      rowIndex += 1
      var cellIndex = 0

      cellIndex =
        writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.oppilaitosJaToimipiste, asiointikieli)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.koulutuksenNimi, asiointikieli)
      cellIndex = writeStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.koulutusOid)
      cellIndex = writeOptionTilaToCell(resultRow, bodyTextCellStyle, cellIndex, result.koulutuksenTila, translations)
      cellIndex = writeOptionKoodiToCell(resultRow, bodyTextCellStyle, cellIndex, result.koulutuskoodi)
      cellIndex = writeOptionStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.koulutuksenUlkoinenTunniste)
      cellIndex = writeTutkinnonTasoToCell(resultRow, bodyTextCellStyle, cellIndex, result.tutkinnonTaso, translations)
      cellIndex =
        writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.opintojenLaajuus, asiointikieli)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.toteutuksenNimi, asiointikieli)
      cellIndex = writeStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.toteutusOid)
      cellIndex = writeOptionTilaToCell(resultRow, bodyTextCellStyle, cellIndex, result.toteutuksenTila, translations)
      cellIndex = writeOptionStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.toteutuksenUlkoinenTunniste)
      cellIndex = writeKoulutuksenAlkamisaikaToCell(
        resultRow,
        bodyTextCellStyle,
        cellIndex,
        result.koulutuksenAlkamisaika,
        asiointikieli
      )
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohteenNimi, asiointikieli)
      cellIndex = writeStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohdeOid)
      cellIndex = writeOptionTilaToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohteenTila, translations)
      cellIndex = writeOptionStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohteenUlkoinenTunniste)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.haunNimi, asiointikieli)
      cellIndex = writeStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakuOid)
      cellIndex = writeOptionHakuaikaToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakuaika)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakutapa, asiointikieli)
      cellIndex = writeOptionIntToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohteenAloituspaikat)
      cellIndex = writeOptionIntToCell(resultRow, bodyTextCellStyle, cellIndex, result.ensikertalaistenAloituspaikat)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.valintaperuste, asiointikieli)
    })
  }

  private def writeKorkeakouluKoulutuksetToteutuksetHakukohteetToteutuksittainRows(
      sheet: XSSFSheet,
      bodyTextCellStyle: XSSFCellStyle,
      currentRowIndex: Int,
      korkeakouluKoulutuksetToteutuksetHakukohteetResults: Seq[KorkeakouluKoulutusToteutusHakukohdeResult],
      asiointikieli: String,
      translations: Map[String, String]
  ): Unit = {
    var rowIndex = currentRowIndex
    korkeakouluKoulutuksetToteutuksetHakukohteetResults.foreach(result => {
      val resultRow = sheet.createRow(rowIndex)
      rowIndex += 1
      var cellIndex = 0

      cellIndex =
        writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.oppilaitosJaToimipiste, asiointikieli)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.toteutuksenNimi, asiointikieli)
      cellIndex = writeStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.toteutusOid)
      cellIndex = writeOptionTilaToCell(resultRow, bodyTextCellStyle, cellIndex, result.toteutuksenTila, translations)
      cellIndex = writeOptionStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.toteutuksenUlkoinenTunniste)
      cellIndex = writeKoulutuksenAlkamisaikaToCell(
        resultRow,
        bodyTextCellStyle,
        cellIndex,
        result.koulutuksenAlkamisaika,
        asiointikieli
      )
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohteenNimi, asiointikieli)
      cellIndex = writeStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohdeOid)
      cellIndex = writeOptionTilaToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohteenTila, translations)
      cellIndex = writeOptionStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohteenUlkoinenTunniste)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.haunNimi, asiointikieli)
      cellIndex = writeStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakuOid)
      cellIndex = writeOptionHakuaikaToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakuaika)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakutapa, asiointikieli)
      cellIndex = writeOptionIntToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohteenAloituspaikat)
      cellIndex = writeOptionIntToCell(resultRow, bodyTextCellStyle, cellIndex, result.ensikertalaistenAloituspaikat)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.valintaperuste, asiointikieli)
    })
  }

  private def writeKorkeakouluKoulutuksetToteutuksetHakukohteetHakukohteittainRows(
      sheet: XSSFSheet,
      bodyTextCellStyle: XSSFCellStyle,
      currentRowIndex: Int,
      korkeakouluKoulutuksetToteutuksetHakukohteetResults: Seq[KorkeakouluKoulutusToteutusHakukohdeResult],
      asiointikieli: String,
      translations: Map[String, String]
  ): Unit = {
    var rowIndex = currentRowIndex
    korkeakouluKoulutuksetToteutuksetHakukohteetResults.foreach(result => {
      val resultRow = sheet.createRow(rowIndex)
      rowIndex += 1
      var cellIndex = 0

      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.oppilaitosJaToimipiste, asiointikieli)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohteenNimi, asiointikieli)
      cellIndex = writeStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohdeOid)
      cellIndex = writeOptionTilaToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohteenTila, translations)
      cellIndex = writeOptionStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohteenUlkoinenTunniste)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.haunNimi, asiointikieli)
      cellIndex = writeStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakuOid)
      cellIndex = writeOptionHakuaikaToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakuaika)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakutapa, asiointikieli)
      cellIndex = writeOptionIntToCell(resultRow, bodyTextCellStyle, cellIndex, result.hakukohteenAloituspaikat)
      cellIndex = writeOptionIntToCell(resultRow, bodyTextCellStyle, cellIndex, result.ensikertalaistenAloituspaikat)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.valintaperuste, asiointikieli)
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.toteutuksenNimi, asiointikieli)
      cellIndex = writeStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.toteutusOid)
      cellIndex = writeOptionTilaToCell(resultRow, bodyTextCellStyle, cellIndex, result.toteutuksenTila, translations)
      cellIndex = writeOptionStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.toteutuksenUlkoinenTunniste)
      cellIndex = writeKoulutuksenAlkamisaikaToCell(
        resultRow,
        bodyTextCellStyle,
        cellIndex,
        result.koulutuksenAlkamisaika,
        asiointikieli
      )
      cellIndex = writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.koulutuksenNimi, asiointikieli)
      cellIndex = writeStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.koulutusOid)
      cellIndex = writeOptionTilaToCell(resultRow, bodyTextCellStyle, cellIndex, result.koulutuksenTila, translations)
      cellIndex = writeOptionKoodiToCell(resultRow, bodyTextCellStyle, cellIndex, result.koulutuskoodi)
      cellIndex = writeOptionStrToCell(resultRow, bodyTextCellStyle, cellIndex, result.koulutuksenUlkoinenTunniste)
      cellIndex = writeTutkinnonTasoToCell(resultRow, bodyTextCellStyle, cellIndex, result.tutkinnonTaso, translations)
      cellIndex =
        writeKielistettyToCell(resultRow, bodyTextCellStyle, cellIndex, result.opintojenLaajuus, asiointikieli)

    })
  }

  def writeKorkeakouluKoulutuksetToteutuksetHakukohteetRaportti(
      korkeakouluKoulutuksetToteutuksetHakukohteetResults: Seq[KorkeakouluKoulutusToteutusHakukohdeResult],
      asiointikieli: String,
      translations: Map[String, String],
      tulostustapa: String
  ): XSSFWorkbook = {
    val workbook: XSSFWorkbook = new XSSFWorkbook()
    try {
      LOG.info("Creating new KkKoulutuksetToteutuksetHakukohteetExcel from db results")
      val sheet: XSSFSheet                 = workbook.createSheet()
      val headingCellStyle: XSSFCellStyle  = workbook.createCellStyle()
      val bodyTextCellStyle: XSSFCellStyle = workbook.createCellStyle()

      createHeadingFont(workbook, headingCellStyle)
      createSubHeadingFont(workbook)
      createBodyTextFont(workbook, bodyTextCellStyle)

      workbook.setSheetName(
        0,
        WorkbookUtil.createSafeSheetName(translations.getOrElse("raportti.yhteenveto", "raportti.yhteenveto"))
      )

      var currentRowIndex = 0

      val raporttiColumnTitlesWithIndex = if (tulostustapa == "toteutuksittain") {
        val sorted =
          korkeakouluKoulutuksetToteutuksetHakukohteetResults.sortBy(resultRow =>
            (
              resultRow.oppilaitosJaToimipiste.get(Kieli.withName(asiointikieli)),
              resultRow.toteutuksenNimi.get(Kieli.withName(asiointikieli))
            )
          )

        val titles = classOf[KorkeakouluKoulutusToteutusHakukohdeResult].getDeclaredFields
          .map(_.getName)
          .toList
          .filter(title =>
            !List(
              "koulutuksenNimi",
              "koulutusOid",
              "koulutuksenTila",
              "koulutuskoodi",
              "koulutuksenUlkoinenTunniste",
              "opintojenLaajuus",
              "tutkinnonTaso"
            ).contains(title)
          )

        currentRowIndex = createHeadingRow(
          sheet = sheet,
          translations = translations,
          currentRowIndex = currentRowIndex,
          fieldNames = titles,
          headingCellStyle = headingCellStyle
        )

        writeKorkeakouluKoulutuksetToteutuksetHakukohteetToteutuksittainRows(
          sheet = sheet,
          bodyTextCellStyle = bodyTextCellStyle,
          currentRowIndex = currentRowIndex,
          korkeakouluKoulutuksetToteutuksetHakukohteetResults = sorted,
          asiointikieli = asiointikieli,
          translations = translations
        )

        titles.zipWithIndex
      } else if (tulostustapa == "hakukohteittain") {
        val sorted =
          korkeakouluKoulutuksetToteutuksetHakukohteetResults.sortBy(resultRow =>
            (
              resultRow.oppilaitosJaToimipiste.get(Kieli.withName(asiointikieli)),
              resultRow.hakukohteenNimi.get(Kieli.withName(asiointikieli))
            )
          )

        val titles = List(
          "oppilaitosJaToimipiste",
          "hakukohteenNimi",
          "hakukohdeOid",
          "hakukohteenTila",
          "hakukohteenUlkoinenTunniste",
          "haunNimi",
          "hakuOid",
          "hakuaika",
          "hakutapa",
          "hakukohteenAloituspaikat",
          "ensikertalaistenAloituspaikat",
          "valintaperuste",
          "toteutuksenNimi",
          "toteutusOid",
          "toteutuksenTila",
          "toteutuksenUlkoinenTunniste",
          "koulutuksenAlkamisaika",
          "koulutuksenNimi",
          "koulutusOid",
          "koulutuksenTila",
          "koulutuskoodi",
          "koulutuksenUlkoinenTunniste",
          "tutkinnonTaso",
          "opintojenLaajuus"
        )

        currentRowIndex = createHeadingRow(
          sheet = sheet,
          translations = translations,
          currentRowIndex = currentRowIndex,
          fieldNames = titles,
          headingCellStyle = headingCellStyle
        )

        writeKorkeakouluKoulutuksetToteutuksetHakukohteetHakukohteittainRows(
          sheet = sheet,
          bodyTextCellStyle = bodyTextCellStyle,
          currentRowIndex = currentRowIndex,
          korkeakouluKoulutuksetToteutuksetHakukohteetResults = sorted,
          asiointikieli = asiointikieli,
          translations = translations
        )

        titles.zipWithIndex
      } else {
        val sorted =
          korkeakouluKoulutuksetToteutuksetHakukohteetResults.sortBy(resultRow =>
            (
              resultRow.oppilaitosJaToimipiste.get(Kieli.withName(asiointikieli)),
              resultRow.koulutuksenNimi.get(Kieli.withName(asiointikieli))
            )
          )

        val titles = classOf[KorkeakouluKoulutusToteutusHakukohdeResult].getDeclaredFields
          .map(_.getName)
          .toList

        currentRowIndex = createHeadingRow(
          sheet = sheet,
          translations = translations,
          currentRowIndex = currentRowIndex,
          fieldNames = titles,
          headingCellStyle = headingCellStyle
        )

        writeKorkeakouluKoulutuksetToteutuksetHakukohteetKoulutuksittainRows(
          sheet = sheet,
          bodyTextCellStyle = bodyTextCellStyle,
          currentRowIndex = currentRowIndex,
          korkeakouluKoulutuksetToteutuksetHakukohteetResults = sorted,
          asiointikieli = asiointikieli,
          translations = translations
        )

        titles.zipWithIndex
      }
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

  def createHeadingRow(
      sheet: XSSFSheet,
      translations: Map[String, String],
      currentRowIndex: Int,
      fieldNames: List[String],
      headingCellStyle: XSSFCellStyle
  ): Int = {
    val headingRow = sheet.createRow(currentRowIndex)
    fieldNames.zipWithIndex.foreach((fieldName, index) => {
      val headingCell = headingRow.createCell(index)
      headingCell.setCellStyle(headingCellStyle)
      val translationKey = s"raportti.$fieldName"
      val translation    = translations.getOrElse(translationKey, translationKey)
      headingCell.setCellValue(translation)
    })

    currentRowIndex + 1
  }

  def createHeadingRowWithValintatapajonotAndYoArvosanat(
      sheet: XSSFSheet,
      translations: Map[String, String],
      currentRowIndex: Int,
      fieldNames: List[String],
      origFieldNames: List[String],
      headingCellStyle: XSSFCellStyle
  ): Int = {
    val headingRow = sheet.createRow(currentRowIndex)
    fieldNames.zipWithIndex.foreach((fieldName, index) => {
      val headingCell = headingRow.createCell(index)
      headingCell.setCellStyle(headingCellStyle)
      val value = if (origFieldNames.contains(fieldName)) {
        val translationKey = s"raportti.$fieldName"
        translations.getOrElse(translationKey, translationKey)
      } else {
        fieldName
      }

      headingCell.setCellValue(value)
    })

    currentRowIndex + 1
  }

  private def writeToisenAsteenHakijatRows(
      sheet: XSSFSheet,
      bodyTextCellStyle: XSSFCellStyle,
      currentRowIndex: Int,
      hakijoidenHakutoiveet: Seq[ToisenAsteenHakija],
      asiointikieli: String,
      translations: Map[String, String]
  ): Unit = {
    var rowIndex = currentRowIndex
    hakijoidenHakutoiveet.foreach(hakutoive => {
      val hakijanHakutoiveRow = sheet.createRow(rowIndex)
      rowIndex += 1
      var cellIndex = 0

      cellIndex = writeStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.hakijanSukunimi)
      cellIndex = writeStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.hakijanEtunimi)
      cellIndex =
        writeOptionBooleanToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.turvakielto, translations)

      cellIndex =
        writeKielistettyListToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.kansalaisuudet, asiointikieli)
      cellIndex = writeStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.oppijanumero)
      cellIndex = writeStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.hakemusOid)
      cellIndex =
        writeKielistettyToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.oppilaitos, asiointikieli)
      cellIndex =
        writeKielistettyToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.toimipiste, asiointikieli)
      cellIndex = writeKielistettyToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.hakukohteenNimi,
        asiointikieli
      )
      cellIndex = writeIntToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.prioriteetti)
      cellIndex = writeOptionBooleanToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.kaksoistutkintoKiinnostaa,
        translations
      )
      cellIndex = writeOptionBooleanToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.urheilijatutkintoKiinnostaa,
        translations
      )
      cellIndex = writeOptionTranslationToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.valintatieto,
        translations
      )
      cellIndex = writeOptionStrToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.varasija
      )
      cellIndex = writeOptionStrToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.kokonaispisteet
      )
      cellIndex = writeKielistettyToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.valintatapajonokohtainenTila,
        asiointikieli
      )
      cellIndex = writeOptionTranslationToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.vastaanottotieto,
        translations
      )
      cellIndex =
        writeOptionLocalDateToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.viimVastaanottopaiva)
      cellIndex = writeOptionTranslationToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.ilmoittautuminen,
        translations
      )
      cellIndex = writeOptionHarkinnanvaraisuusToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.harkinnanvaraisuus,
        translations
      )
      cellIndex = writeOptionBooleanToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.soraAiempi,
        translations
      )
      cellIndex = writeOptionBooleanToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.soraTerveys,
        translations
      )
      cellIndex = writeKielistettyToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.pohjakoulutus,
        asiointikieli
      )
      cellIndex = writeOptionBooleanToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.markkinointilupa,
        translations
      )
      cellIndex = writeOptionBooleanToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.julkaisulupa,
        translations
      )
      cellIndex = writeOptionBooleanToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.sahkoinenViestintalupa,
        translations
      )

      cellIndex = writeOptionStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.lahiosoite)
      cellIndex = writeOptionStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.postinumero)
      cellIndex = writeOptionStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.postitoimipaikka)
      cellIndex = writeOptionStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.sahkoposti)
    })
  }

  def writeToisenAsteenHakijatRaportti(
      hakijat: Seq[ToisenAsteenHakija],
      asiointikieli: String,
      translations: Map[String, String]
  ): XSSFWorkbook = {
    val workbook: XSSFWorkbook = new XSSFWorkbook()
    LOG.info("Creating new ToisenAsteenHakijatExcel from db results")
    val sheet: XSSFSheet = workbook.createSheet()
    workbook.setSheetName(
      0,
      WorkbookUtil.createSafeSheetName(translations.getOrElse("raportti.yhteenveto", "raportti.yhteenveto"))
    )

    val headingCellStyle: XSSFCellStyle  = workbook.createCellStyle()
    val bodyTextCellStyle: XSSFCellStyle = workbook.createCellStyle()

    createHeadingFont(workbook, headingCellStyle)
    createBodyTextFont(workbook, bodyTextCellStyle)

    var currentRowIndex = 0

    val fieldNames: List[String] = classOf[ToisenAsteenHakija].getDeclaredFields.map(_.getName).toList
    val fieldNamesWithIndex      = fieldNames.zipWithIndex

    currentRowIndex = createHeadingRow(sheet, translations, currentRowIndex, fieldNames, headingCellStyle)

    writeToisenAsteenHakijatRows(
      sheet = sheet,
      bodyTextCellStyle = bodyTextCellStyle,
      currentRowIndex = currentRowIndex,
      hakijoidenHakutoiveet = hakijat,
      asiointikieli = asiointikieli,
      translations = translations
    )

    // Asetetaan lopuksi kolumnien leveys automaattisesti leveimmän arvon mukaan
    fieldNamesWithIndex.foreach { case (title, index) =>
      sheet.autoSizeColumn(index)
    }

    try {
      workbook
    } catch {
      case e: Exception =>
        LOG.error(s"Error creating excel: ${e.getMessage}")
        throw e
    }
  }

  def getHeadingFieldNames(
      naytaArvosanat: Boolean,
      naytaHetu: Boolean,
      naytaPostiosoite: Boolean,
      valintatapajonot: Seq[Valintatapajono],
      arvosanat: Seq[String]
  ): List[String] = {
    val optionallyShowableFields = List("hetu", "arvosanat") ::: POSTIOSOITEFIELDS

    val fieldNames =
      classOf[KkHakija].getDeclaredFields
        .map(_.getName)
        .toList

    val showableFieldNames = fieldNames.filter(fieldName => {
      !optionallyShowableFields.contains(fieldName) ||
        fieldName == "hetu" && naytaHetu ||
        POSTIOSOITEFIELDS.contains(
          fieldName
        ) && naytaPostiosoite ||
        fieldName == "arvosanat" && naytaArvosanat
    })

    (for (field <- showableFieldNames) yield {
      if (field == "valintatapajonot") {
        if (valintatapajonot.isEmpty) {
          List()
        } else {
          valintatapajonot.flatMap(valintatapajono => List(valintatapajono.valintatapajononNimi))
        }
      } else if (field == "arvosanat") {
        if (arvosanat.isEmpty) {
          List()
        } else {
          arvosanat
        }
      } else {
        List(field)
      }
    }).flatten
  }

  def createValintatapajonokohtainenTilaHeadingRow(
      workbook: XSSFWorkbook,
      sheet: XSSFSheet,
      translations: Map[String, String],
      currentRowIndex: Int,
      fieldNames: List[String],
      valintatapajonot: Seq[Valintatapajono],
      naytaHetu: Boolean
  ): Int = {
    val headingRow                      = sheet.createRow(currentRowIndex)
    val headingCellStyle: XSSFCellStyle = workbook.createCellStyle()
    createHeadingFont(workbook, headingCellStyle)
    headingCellStyle.setAlignment(HorizontalAlignment.CENTER)

    var headingColumnStartIndex = 0
    for (fieldName <- fieldNames.takeWhile(_ != "valintatapajonot")) {
      headingColumnStartIndex += 1
    }

    if (!naytaHetu) {
      headingColumnStartIndex -= 1
    }

    if (valintatapajonot.length > 1) {
      val firstRow              = currentRowIndex
      val lastRow               = currentRowIndex
      val headingColumnEndIndex = headingColumnStartIndex + valintatapajonot.length - 1

      sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, headingColumnStartIndex, headingColumnEndIndex))
    }

    val headingCell = headingRow.createCell(headingColumnStartIndex)
    headingCell.setCellStyle(headingCellStyle)
    val translationKey = s"raportti.valintatapajonokohtainenTila"
    val value          = translations.getOrElse(translationKey, translationKey)
    headingCell.setCellValue(value)

    currentRowIndex + 1
  }

  private def writeKKHakijatRows(
      sheet: XSSFSheet,
      bodyTextCellStyle: XSSFCellStyle,
      currentRowIndex: Int,
      hakijoidenHakutoiveet: Seq[KkHakija],
      asiointikieli: String,
      translations: Map[String, String],
      naytaArvosanat: Boolean,
      naytaHetu: Boolean,
      naytaPostiosoite: Boolean,
      distinctSortedValintatapajonotInQueryResult: Seq[Valintatapajono],
      allSortedYoKokeet: Seq[String]
  ): Unit = {
    var rowIndex = currentRowIndex
    hakijoidenHakutoiveet.foreach(hakutoive => {
      val hakijanHakutoiveRow = sheet.createRow(rowIndex)
      rowIndex += 1
      var cellIndex = 0

      cellIndex = writeStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.hakijanSukunimi)
      cellIndex = writeStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.hakijanEtunimi)
      cellIndex = writeOptionBooleanToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.turvakielto, translations)

      if (naytaHetu) {
        cellIndex = writeOptionStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.hetu)
      }

      cellIndex = writeOptionLocalDateToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.syntymaAika)
      cellIndex =
        writeKielistettyListToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.kansalaisuudet, asiointikieli)
      cellIndex = writeStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.oppijanumero)
      cellIndex = writeStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.hakemusOid)
      cellIndex =
        writeKielistettyToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.toimipiste, asiointikieli)
      cellIndex = writeKielistettyToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.hakukohteenNimi,
        asiointikieli
      )
      cellIndex = writeOptionTranslationToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.hakukelpoisuus,
        translations
      )
      if (hakutoive.prioriteetti < 0) {
        cellIndex = writeStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, "-")
      } else {
        cellIndex = writeIntToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.prioriteetti)
      }
      cellIndex = writeOptionTranslationToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.valintatieto,
        translations
      )
      cellIndex = writeOptionBooleanToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.ehdollisestiHyvaksytty,
        translations
      )
      cellIndex =
        writeOptionLocalDateToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.valintatiedonPvm)

      val hakutoiveValintatapajonot = hakutoive.valintatapajonot.groupBy(_.valintatapajonoOid)
      val hakukohteenValinnanTilanKuvaukset = createHakutoiveenValintatapajonoWritableValues(
        hakutoiveValintatapajonot,
        distinctSortedValintatapajonotInQueryResult,
        translations
      )
      hakukohteenValinnanTilanKuvaukset.foreach(kielistettyValinnanTilanKuvaus => {
        cellIndex = writeKielistettyToCell(
          hakijanHakutoiveRow,
          bodyTextCellStyle,
          cellIndex,
          kielistettyValinnanTilanKuvaus,
          asiointikieli
        )
      })

      cellIndex = writeOptionTranslationToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.vastaanottotieto,
        translations
      )
      cellIndex =
        writeOptionLocalDateToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.viimVastaanottopaiva)
      cellIndex = writeOptionBooleanToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.ensikertalainen,
        translations
      )
      cellIndex = writeOptionTranslationToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.ilmoittautuminen,
        translations
      )
      cellIndex = writeOptionStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.pohjakoulutus)
      cellIndex = writeOptionTranslationToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.maksuvelvollisuus,
        translations
      )
      cellIndex = writeOptionTranslationToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.hakemusmaksunTila,
        translations
      )
      cellIndex = writeOptionBooleanToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.markkinointilupa,
        translations
      )
      cellIndex = writeOptionBooleanToCell(
        hakijanHakutoiveRow,
        bodyTextCellStyle,
        cellIndex,
        hakutoive.sahkoinenViestintalupa,
        translations
      )

      if (naytaPostiosoite) {
        cellIndex = writeOptionStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.lahiosoite)
        cellIndex = writeOptionStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.postinumero)
        cellIndex = writeOptionStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.postitoimipaikka)
      }

      cellIndex =
        writeKielistettyToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.kotikunta, asiointikieli)
      cellIndex =
        writeKielistettyToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.asuinmaa, asiointikieli)
      cellIndex = writeOptionStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.puhelinnumero)
      cellIndex = writeOptionStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, hakutoive.sahkoposti)

      if (naytaArvosanat) {
        val hakutoiveArvosanat = hakutoive.arvosanat
        val hakutoiveenArvosanatCellValues =
          createHakutoiveenArvosanatWritableValues(hakutoiveArvosanat, allSortedYoKokeet)
        hakutoiveenArvosanatCellValues.foreach(arvosanaValue => {
          cellIndex = writeStrToCell(hakijanHakutoiveRow, bodyTextCellStyle, cellIndex, arvosanaValue)
        })
      }
    })
  }

  def writeKkHakijatRaportti(
      hakijoidenHakutoiveet: Seq[KkHakija],
      asiointikieli: String,
      translations: Map[String, String],
      maybeNaytaYoArvosanat: Option[Boolean] = None,
      maybeNaytaHetu: Option[Boolean] = None,
      maybeNaytaPostiosoite: Option[Boolean] = None,
      yokokeet: Vector[Koodi]
  ): XSSFWorkbook = {
    val workbook: XSSFWorkbook = new XSSFWorkbook()
    LOG.info("Creating new KkHakijatExcel from db results")
    val sheet: XSSFSheet = workbook.createSheet()
    workbook.setSheetName(
      0,
      WorkbookUtil.createSafeSheetName(translations.getOrElse("raportti.yhteenveto", "raportti.yhteenveto"))
    )

    val headingCellStyle: XSSFCellStyle  = workbook.createCellStyle()
    val bodyTextCellStyle: XSSFCellStyle = workbook.createCellStyle()

    createHeadingFont(workbook, headingCellStyle)
    createBodyTextFont(workbook, bodyTextCellStyle)

    var currentRowIndex = 0

    val naytaArvosanat   = maybeNaytaYoArvosanat.getOrElse(false)
    val naytaHetu        = maybeNaytaHetu.getOrElse(false)
    val naytaPostiosoite = maybeNaytaPostiosoite.getOrElse(false)

    val distinctSortedValintatapajonotInQueryResult =
      hakijoidenHakutoiveet.flatMap(_.valintatapajonot).distinctBy(_.valintatapajonoOid).sortBy(_.valintatapajononNimi)

    val allYoKokeet = yokokeet.map(k => k.koodiarvo)

    val headingFieldNames =
      getHeadingFieldNames(
        naytaArvosanat,
        naytaHetu,
        naytaPostiosoite,
        distinctSortedValintatapajonotInQueryResult,
        allYoKokeet
      )

    val fieldNames: List[String] = classOf[KkHakija].getDeclaredFields.map(_.getName).toList

    if (distinctSortedValintatapajonotInQueryResult.nonEmpty) {
      currentRowIndex = createValintatapajonokohtainenTilaHeadingRow(
        workbook,
        sheet,
        translations,
        currentRowIndex,
        fieldNames,
        distinctSortedValintatapajonotInQueryResult,
        naytaHetu
      )
    }

    currentRowIndex = createHeadingRowWithValintatapajonotAndYoArvosanat(
      sheet,
      translations,
      currentRowIndex,
      headingFieldNames,
      fieldNames,
      headingCellStyle
    )

    writeKKHakijatRows(
      sheet = sheet,
      bodyTextCellStyle = bodyTextCellStyle,
      currentRowIndex = currentRowIndex,
      hakijoidenHakutoiveet = hakijoidenHakutoiveet,
      asiointikieli = asiointikieli,
      translations = translations,
      naytaArvosanat = naytaArvosanat,
      naytaHetu = naytaHetu,
      naytaPostiosoite = naytaPostiosoite,
      distinctSortedValintatapajonotInQueryResult = distinctSortedValintatapajonotInQueryResult,
      allSortedYoKokeet = allYoKokeet
    )

    // Asetetaan lopuksi kolumnien leveys automaattisesti leveimmän arvon mukaan
    headingFieldNames.zipWithIndex.foreach { case (title, index) =>
      sheet.autoSizeColumn(index)
    }

    // yo-kokeiden selitteet jos yo-arvosanat näkyvissä
    if (naytaArvosanat) {
      val yoKokeetSheet: XSSFSheet = workbook.createSheet()
      workbook.setSheetName(
        1,
        WorkbookUtil.createSafeSheetName(translations.getOrElse("raportti.yo-kokeet", "raportti.yo-kokeet"))
      )
      var yokokeetRowIndex = 0
      // otsikkorivi
      val yoKoeTitles = List("yokoelyhenne", "yokoeselite")
      yokokeetRowIndex = createHeadingRow(yoKokeetSheet, translations, yokokeetRowIndex, yoKoeTitles, headingCellStyle)
      // yo-kokeiden selitteet koodistosta
      yokokeet.foreach { yokoe =>
        val row = yoKokeetSheet.createRow(yokokeetRowIndex)
        val yoRowData = List(
          yokoe.koodiarvo,
          yokoe.koodinimi(Kieli.withName(asiointikieli))
        )
        createRowCells(yoRowData, row, workbook, createBodyTextCellStyle(workbook))
        yokokeetRowIndex += 1
      }
      // Asetetaan lopuksi kolumnien leveys automaattisesti leveimmän arvon mukaan
      yoKoeTitles.zipWithIndex.foreach { case (title, index) =>
        yoKokeetSheet.autoSizeColumn(index)
      }
    }

    try {
      workbook
    } catch {
      case e: Exception =>
        LOG.error(s"Error creating excel: ${e.getMessage}")
        throw e
    }
  }

  def writeHakeneetHyvaksytytVastaanottaneetRaportti[T <: HakeneetHyvaksytytVastaanottaneetResult | HakeneetHyvaksytytVastaanottaneetHakukohteittain](
      asiointikieli: String,
      translations: Map[String, String],
      data: List[T],
      yksittaisetHakijat: Int,
      naytaHakutoiveet: Boolean,
      tulostustapa: String
  ): XSSFWorkbook = {
    val workbook: XSSFWorkbook = new XSSFWorkbook()
    LOG.info("Creating new HakeneetHyvaksytytVastaanottaneet excel from db results")
    val sheet: XSSFSheet = workbook.createSheet()
    workbook.setSheetName(
      0,
      WorkbookUtil.createSafeSheetName(translations.getOrElse("raportti.yhteenveto", "raportti.yhteenveto"))
    )

    val headingCellStyle: XSSFCellStyle = workbook.createCellStyle()
    val headingFont                     = createHeadingFont(workbook, headingCellStyle)

    var currentRowIndex = 0

    val otsikko = tulostustapa match {
      case "hakukohteittain"       => List("hakukohde", "organisaatio", "haku")
      case "oppilaitoksittain"     => List("oppilaitos")
      case "toimipisteittain"      => List("toimipiste")
      case "koulutustoimijoittain" => List("koulutustoimija")
      case "koulutusaloittain"     => List("koulutusala")
    }

    val fieldNames =
      if (naytaHakutoiveet)
        otsikko ++ HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_COMMON_TITLES ++ HAKUTOIVEET_TITLES
      else
        otsikko ++ HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_COMMON_TITLES
    val fieldNamesWithIndex = fieldNames.zipWithIndex

    currentRowIndex = createHeadingRow(sheet, translations, currentRowIndex, fieldNames, headingCellStyle)

    data.foreach { item =>
      val dataRow = sheet.createRow(currentRowIndex)
      val rowData = item match {
        case result: HakeneetHyvaksytytVastaanottaneetResult =>
          List(
            result.otsikko(Kieli.withName(asiointikieli)),
            result.hakijat,
            result.ensisijaisia,
            result.varasija,
            result.hyvaksytyt,
            result.vastaanottaneet,
            result.lasna,
            result.poissa,
            result.ilmYht,
            result.aloituspaikat
          ) ++ generateHakutoiveet(naytaHakutoiveet, item)
        case hakukohteittain: HakeneetHyvaksytytVastaanottaneetHakukohteittain =>
          List(
            hakukohteittain.hakukohdeNimi(Kieli.withName(asiointikieli)),
            hakukohteittain.organisaatioNimi(Kieli.withName(asiointikieli)),
            hakukohteittain.hakuNimi(Kieli.withName(asiointikieli)),
            hakukohteittain.hakijat,
            hakukohteittain.ensisijaisia,
            hakukohteittain.varasija,
            hakukohteittain.hyvaksytyt,
            hakukohteittain.vastaanottaneet,
            hakukohteittain.lasna,
            hakukohteittain.poissa,
            hakukohteittain.ilmYht,
            hakukohteittain.aloituspaikat
          ) ++ generateHakutoiveet(naytaHakutoiveet, hakukohteittain)
      }
      if(tulostustapa == "hakukohteittain") {
        createHakukohteittainRowCells(rowData, dataRow, workbook, createBodyTextCellStyle(workbook))
      } else {
        createRowCells(rowData, dataRow, workbook, createBodyTextCellStyle(workbook))
      }
      currentRowIndex += 1
    }

    // yhteensä-rivi
    val summaryRow = sheet.createRow(currentRowIndex)
    if (tulostustapa == "hakukohteittain") {
      val summaryData = List("", "") ++ List(
        translations.getOrElse("raportti.yhteensa", "raportti.yhteensa"),
        data.map(_.hakijat).sum,
        data.map(_.ensisijaisia).sum,
        data.map(_.varasija).sum,
        data.map(_.hyvaksytyt).sum,
        data.map(_.vastaanottaneet).sum,
        data.map(_.lasna).sum,
        data.map(_.poissa).sum,
        data.map(_.ilmYht).sum,
        data.map(_.aloituspaikat).sum
      ) ++ generateHakutoiveetSum(data, naytaHakutoiveet)
      createHakukohteittainSummaryRowCells(summaryData, summaryRow, workbook, createSummaryCellStyle(workbook))
      currentRowIndex += 1
      // yksittäiset hakijat -rivi
      val hakijatSummaryRow = sheet.createRow(currentRowIndex)
      val hakijatSummaryData = List(
        "", "", // 2 tyhjää solua rivin alkuun jotta summa tulee kohdalleen
        translations.getOrElse("raportti.yksittaiset-hakijat", "raportti.yksittaiset-hakijat"),
        yksittaisetHakijat)
      createHakukohteittainSummaryRowCells(hakijatSummaryData, hakijatSummaryRow, workbook, createSummaryCellStyle(workbook))
    } else {
      val summaryData = List(
        translations.getOrElse("raportti.yhteensa", "raportti.yhteensa"),
        data.map(_.hakijat).sum,
        data.map(_.ensisijaisia).sum,
        data.map(_.varasija).sum,
        data.map(_.hyvaksytyt).sum,
        data.map(_.vastaanottaneet).sum,
        data.map(_.lasna).sum,
        data.map(_.poissa).sum,
        data.map(_.ilmYht).sum,
        data.map(_.aloituspaikat).sum
      ) ++ generateHakutoiveetSum(data, naytaHakutoiveet)

      createRowCells(summaryData, summaryRow, workbook, createSummaryCellStyle(workbook))
      currentRowIndex += 1
      // yksittäiset hakijat -rivi
      val hakijatSummaryRow = sheet.createRow(currentRowIndex)
      val hakijatSummaryData = List(translations.getOrElse("raportti.yksittaiset-hakijat", "raportti.yksittaiset-hakijat"),
        yksittaisetHakijat)
      createRowCells(hakijatSummaryData, hakijatSummaryRow, workbook, createSummaryCellStyle(workbook))
    }

    // Asetetaan lopuksi kolumnien leveys automaattisesti leveimmän arvon mukaan
    fieldNamesWithIndex.foreach { case (title, index) =>
      sheet.autoSizeColumn(index)
    }

    try {
      workbook
    } catch {
      case e: Exception =>
        LOG.error(s"Error creating excel: ${e.getMessage}")
        throw e
    }
  }

  private def generateHakutoiveet[T <: HakeneetHyvaksytytVastaanottaneetResult | HakeneetHyvaksytytVastaanottaneetHakukohteittain](naytaHakutoiveet: Boolean, item: T) = {
    (if (naytaHakutoiveet) {
      List(
        item.toive1,
        item.toive2,
        item.toive3,
        item.toive4,
        item.toive5,
        item.toive6,
        item.toive7
      )
    } else {
      List()
    })
  }

  private def generateHakutoiveetSum[T <: HakeneetHyvaksytytVastaanottaneetResult | HakeneetHyvaksytytVastaanottaneetHakukohteittain](data: List[T], naytaHakutoiveet: Boolean) = {
    if (naytaHakutoiveet) {
      List(
        data.map(_.toive1).sum,
        data.map(_.toive2).sum,
        data.map(_.toive3).sum,
        data.map(_.toive4).sum,
        data.map(_.toive5).sum,
        data.map(_.toive6).sum,
        data.map(_.toive7).sum
      )
    } else {
      List()
    }
  }

  def writeKkHakeneetHyvaksytytVastaanottaneetRaportti[T <: KkHakeneetHyvaksytytVastaanottaneetResult | KkHakeneetHyvaksytytVastaanottaneetHakukohteittain](
      asiointikieli: String,
      translations: Map[String, String],
      data: List[T],
      yksittaisetHakijat: Int,
      ensikertalaisetYksittaisetHakijat: Int,
      maksuvelvollisetYksittaisetHakijat: Int,
      naytaHakutoiveet: Boolean,
      tulostustapa: String
  ): XSSFWorkbook = {
    val workbook: XSSFWorkbook = new XSSFWorkbook()
    LOG.info("Creating new KkHakeneetHyvaksytytVastaanottaneet excel from db results")
    val sheet: XSSFSheet = workbook.createSheet()
    workbook.setSheetName(
      0,
      WorkbookUtil.createSafeSheetName(translations.getOrElse("raportti.yhteenveto", "raportti.yhteenveto"))
    )

    val headingCellStyle: XSSFCellStyle = workbook.createCellStyle()
    val headingFont                     = createHeadingFont(workbook, headingCellStyle)

    var currentRowIndex = 0

    if (tulostustapa == "hakukohteittain") {
      val otsikkorivi = List("hakukohde", "organisaatio", "haku")
        ++ KK_HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_COMMON_TITLES
        ++ KK_HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_ALOITUSPAIKAT_TITLES
        ++ (
        if (naytaHakutoiveet)
          KK_HAKUTOIVEET_TITLES
        else
          List()
        )
      currentRowIndex = createHeadingRow(sheet, translations, currentRowIndex, otsikkorivi, headingCellStyle)
      val otsikotWithIndex = otsikkorivi.zipWithIndex
      data.foreach {
        // hakukohteittain tulostaessa tiedetään että data on tätä tyyppiä
        case item: KkHakeneetHyvaksytytVastaanottaneetHakukohteittain =>
          val dataRow = sheet.createRow(currentRowIndex)
          val rowData = List(
            item.hakukohdeNimi(Kieli.withName(asiointikieli)),
            item.organisaatioNimi(Kieli.withName(asiointikieli)),
            item.hakuNimi(Kieli.withName(asiointikieli)),
            item.hakijat,
            item.ensisijaisia,
            item.ensikertalaisia,
            item.hyvaksytyt,
            item.vastaanottaneet,
            item.lasna,
            item.poissa,
            item.ilmYht,
            item.maksuvelvollisia,
            item.valinnanAloituspaikat,
            item.aloituspaikat
          ) ++ (if (naytaHakutoiveet) {
            List(
              item.toive1,
              item.toive2,
              item.toive3,
              item.toive4,
              item.toive5,
              item.toive6
            )
          } else {
            List()
          })
          createHakukohteittainRowCells(rowData, dataRow, workbook, createBodyTextCellStyle(workbook))
          currentRowIndex += 1
      }
      val summaryRow = sheet.createRow(currentRowIndex)
      val summaryData = {
        // 2 tyhjää solua rivin alkuun jotta summa tulee kohdalleen
        List("", "") ++
          List(
            translations.getOrElse("raportti.yhteensa", "raportti.yhteensa"),
            data.map(_.hakijat).sum,
            data.map(_.ensisijaisia).sum,
            data.map(_.ensikertalaisia).sum,
            data.map(_.hyvaksytyt).sum,
            data.map(_.vastaanottaneet).sum,
            data.map(_.lasna).sum,
            data.map(_.poissa).sum,
            data.map(_.ilmYht).sum,
            data.map(_.maksuvelvollisia).sum,
            data.map(_.valinnanAloituspaikat).sum,
            data.map(_.aloituspaikat).sum
          ) ++ (if (naytaHakutoiveet) {
          List(
            data.map(_.toive1).sum,
            data.map(_.toive2).sum,
            data.map(_.toive3).sum,
            data.map(_.toive4).sum,
            data.map(_.toive5).sum,
            data.map(_.toive6).sum
          )
        } else {
          List()
        })
      }
      createHakukohteittainSummaryRowCells(summaryData, summaryRow, workbook, createSummaryCellStyle(workbook))
      currentRowIndex += 1
      // yksittäiset hakijat -rivi
      val hakijatSummaryRow = sheet.createRow(currentRowIndex)
      val hakijatSummaryData =
        List(
          "",
          "",
          translations.getOrElse("raportti.yksittaiset-hakijat", "raportti.yksittaiset-hakijat"),
          yksittaisetHakijat,
          "",
          ensikertalaisetYksittaisetHakijat,
          "",
          "",
          "",
          "",
          "",
          maksuvelvollisetYksittaisetHakijat
        )

      createHakukohteittainSummaryRowCells(hakijatSummaryData, hakijatSummaryRow, workbook, createSummaryCellStyle(workbook))
      // Asetetaan lopuksi kolumnien leveys automaattisesti leveimmän arvon mukaan
      otsikotWithIndex.foreach { case (title, index) =>
        sheet.autoSizeColumn(index)
      }
    }
    else {
      // muut tulostustavat
      val otsikkoList = tulostustapa match {
        case "oppilaitoksittain" => List("oppilaitos")
        case "toimipisteittain" => List("toimipiste")
        case "koulutustoimijoittain" => List("koulutustoimija")
        case "okm-ohjauksen-aloittain" => List("okm-ohjauksen-ala")
        case "hauittain" => List("haku")
        case "hakukohderyhmittain" => List("hakukohderyhma")
        case "kansalaisuuksittain" => List("kansalaisuus")
      }

      val fieldNames =
        if (naytaHakutoiveet) {
          otsikkoList ++ KK_HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_COMMON_TITLES ++ (
            if (tulostustapa != "kansalaisuuksittain") KK_HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_ALOITUSPAIKAT_TITLES
            else List()
            )
            ++ KK_HAKUTOIVEET_TITLES
        } else {
          otsikkoList ++ KK_HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_COMMON_TITLES ++ (
            if (tulostustapa != "kansalaisuuksittain") KK_HAKENEET_HYVAKSYTYT_VASTAANOTTANEET_ALOITUSPAIKAT_TITLES
            else List()
            )
        }
      val fieldNamesWithIndex = fieldNames.zipWithIndex

      currentRowIndex = createHeadingRow(sheet, translations, currentRowIndex, fieldNames, headingCellStyle)

      data.foreach {
        case item: KkHakeneetHyvaksytytVastaanottaneetResult =>
          val dataRow = sheet.createRow(currentRowIndex)
          val rowData =
            List(
              item.otsikko(Kieli.withName(asiointikieli)),
              item.hakijat,
              item.ensisijaisia,
              item.ensikertalaisia,
              item.hyvaksytyt,
              item.vastaanottaneet,
              item.lasna,
              item.poissa,
              item.ilmYht,
              item.maksuvelvollisia,
            ) ++ (if (tulostustapa != "kansalaisuuksittain") {
              List(item.valinnanAloituspaikat, item.aloituspaikat)
            } else {
              List()
            }) ++ (if (naytaHakutoiveet) {
              List(
                item.toive1,
                item.toive2,
                item.toive3,
                item.toive4,
                item.toive5,
                item.toive6
              )
            } else {
              List()
            })
          createRowCells(rowData, dataRow, workbook, createBodyTextCellStyle(workbook))
          currentRowIndex += 1
      }

      if (!tulostustapa.equals("hakukohderyhmittain")) {
        // hakukohderyhmittäin tulostaessa ei lasketa rivien summaa
        createSummaryRow(translations, data, naytaHakutoiveet, sheet, currentRowIndex, workbook, tulostustapa)
        currentRowIndex += 1
      }

      // yksittäiset hakijat -rivi
      val hakijatSummaryRow = sheet.createRow(currentRowIndex)
      val hakijatSummaryData =
        List(
          translations.getOrElse("raportti.yksittaiset-hakijat", "raportti.yksittaiset-hakijat"),
          yksittaisetHakijat,
          "",
          ensikertalaisetYksittaisetHakijat,
          "",
          "",
          "",
          "",
          "",
          maksuvelvollisetYksittaisetHakijat
        )

      createRowCells(hakijatSummaryData, hakijatSummaryRow, workbook, createSummaryCellStyle(workbook))
      // Asetetaan lopuksi kolumnien leveys automaattisesti leveimmän arvon mukaan
      fieldNamesWithIndex.foreach { case (title, index) =>
        sheet.autoSizeColumn(index)
      }
    }

    try {
      workbook
    } catch {
      case e: Exception =>
        LOG.error(s"Error creating excel: ${e.getMessage}")
        throw e
    }
  }

  private def createSummaryRow(
      translations: Map[String, String],
      data: List[KkHakeneetHyvaksytytVastaanottaneetResult | KkHakeneetHyvaksytytVastaanottaneetHakukohteittain],
      naytaHakutoiveet: Boolean,
      sheet: XSSFSheet,
      currentRowIndex: Int,
      workbook: XSSFWorkbook,
      tulostustapa: String
  ): Unit = {
    // yhteensä-rivi
    val summaryRow = sheet.createRow(currentRowIndex)
    val emptyCells = if (tulostustapa == "hakukohteittain") {
      List("", "") // 2 tyhjää solua rivin alkuun jotta summa tulee kohdalleen
    } else {
      List()
    }
    val summaryData = {
      emptyCells ++
        List(
          translations.getOrElse("raportti.yhteensa", "raportti.yhteensa"),
          data.map(_.hakijat).sum,
          data.map(_.ensisijaisia).sum,
          data.map(_.ensikertalaisia).sum,
          data.map(_.hyvaksytyt).sum,
          data.map(_.vastaanottaneet).sum,
          data.map(_.lasna).sum,
          data.map(_.poissa).sum,
          data.map(_.ilmYht).sum,
          data.map(_.maksuvelvollisia).sum
        ) ++ (if (tulostustapa != "kansalaisuuksittain") {
        List(data.map(_.valinnanAloituspaikat).sum, data.map(_.aloituspaikat).sum)
      } else {
        List()
      }) ++ (if (naytaHakutoiveet) {
        List(
          data.map(_.toive1).sum,
          data.map(_.toive2).sum,
          data.map(_.toive3).sum,
          data.map(_.toive4).sum,
          data.map(_.toive5).sum,
          data.map(_.toive6).sum
        )
      } else {
        List()
      })
    }
    if (tulostustapa == "hakukohteittain") {
      createHakukohteittainSummaryRowCells(summaryData, summaryRow, workbook, createSummaryCellStyle(workbook))
    } else {
      createRowCells(summaryData, summaryRow, workbook, createSummaryCellStyle(workbook))
    }
  }

  private def createRowCells(
      rowData: List[String | Int],
      row: XSSFRow,
      workbook: XSSFWorkbook,
      firstCellStyle: XSSFCellStyle
  ): Unit = {
    val numericCellStyle = createNumericCellStyle(workbook)
    rowData.zipWithIndex.foreach { case (value, index) =>
      val cell = row.createCell(index)
      if (index == 0) {
        cell.setCellStyle(firstCellStyle)
      } else {
        cell.setCellStyle(numericCellStyle)
      }
      value match {
        case intValue: Int => cell.setCellValue(intValue)
        case _             => cell.setCellValue(value.toString)
      }
    }
  }

  private def createHakukohteittainRowCells(
                              rowData: List[String | Int],
                              row: XSSFRow,
                              workbook: XSSFWorkbook,
                              firstCellStyle: XSSFCellStyle
                            ): Unit = {
    val numericCellStyle = createNumericCellStyle(workbook)
    rowData.zipWithIndex.foreach { case (value, index) =>
      val cell = row.createCell(index)
      if (index < 3) {
        cell.setCellStyle(firstCellStyle)
      } else {
        cell.setCellStyle(numericCellStyle)
      }
      value match {
        case intValue: Int => cell.setCellValue(intValue)
        case _ => cell.setCellValue(value.toString)
      }
    }
  }

  private def createHakukohteittainSummaryRowCells(
                              rowData: List[String | Int],
                              row: XSSFRow,
                              workbook: XSSFWorkbook,
                              headingCellStyle: XSSFCellStyle
                            ): Unit = {
    val numericCellStyle = createNumericCellStyle(workbook)
    rowData.zipWithIndex.foreach { case (value, index) =>
      val cell = row.createCell(index)
      if (index == 2) {
        cell.setCellStyle(headingCellStyle)
      } else {
        cell.setCellStyle(numericCellStyle)
      }
      value match {
        case intValue: Int => cell.setCellValue(intValue)
        case _ => cell.setCellValue(value.toString)
      }
    }
  }
}
