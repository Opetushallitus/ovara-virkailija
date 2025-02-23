package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*
import fi.oph.ovara.backend.utils.Constants.*
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.*
import org.slf4j.{Logger, LoggerFactory}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.matching.Regex

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

  private def createHakukohdeRow(
      sheet: XSSFSheet,
      initialRowIndex: Int,
      resultRowIndex: Int,
      cellStyle: XSSFCellStyle,
      hakukohteenNimiCellStyle: XSSFCellStyle,
      kth: KoulutusToteutusHakukohdeResult,
      asiointikieli: String
  ) = {
    val hakukohteenTiedotRow = sheet.createRow(initialRowIndex)

    for (i <- 0 until kth.productArity) yield {
      val cell = hakukohteenTiedotRow.createCell(i)
      cell.setCellStyle(cellStyle)
      kth.productElement(i) match {
        case kielistetty: Kielistetty =>
          val kielistettyValue = kielistetty(Kieli.withName(asiointikieli))

          if (i == 0) {
            cell.setCellStyle(hakukohteenNimiCellStyle)
          }

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

  def createKoulutuksetToteutuksetHakukohteetResultRows(
      workbook: XSSFWorkbook,
      sheet: XSSFSheet,
      hierarkiatWithHakukohteet: List[OrganisaatioHierarkiaWithHakukohteet],
      initialRowIndex: Int,
      headingCellStyle: XSSFCellStyle,
      cellStyle: XSSFCellStyle,
      hakukohteenNimiTextCellStyle: XSSFCellStyle,
      headingFont: XSSFFont,
      subHeadingFont: XSSFFont,
      asiointikieli: String,
      raporttiColumnTitlesWithIndex: List[(String, Int)],
      raporttityyppi: String
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
          currentRowIndex = createHakukohdeRow(
            sheet,
            currentRowIndex,
            resultRowIndex,
            cellStyle,
            hakukohteenNimiTextCellStyle,
            hakukohde._2,
            asiointikieli
          )
        })

        if (orgHierarkiaWithResults.children.nonEmpty) {
          val updatedRowIndex = createKoulutuksetToteutuksetHakukohteetResultRows(
            workbook,
            sheet,
            orgHierarkiaWithResults.children,
            currentRowIndex,
            headingCellStyle,
            cellStyle,
            hakukohteenNimiTextCellStyle,
            headingFont,
            subHeadingFont,
            asiointikieli,
            raporttiColumnTitlesWithIndex,
            raporttityyppi
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

  private def createBodyTextFont(workbook: XSSFWorkbook, bodyTextCellStyle: XSSFCellStyle): XSSFFont = {
    val bodyTextFont = workbook.createFont()
    bodyTextFont.setFontHeightInPoints(10)

    bodyTextCellStyle.setFont(bodyTextFont)
    bodyTextCellStyle.setAlignment(HorizontalAlignment.LEFT)
    bodyTextFont
  }

  def writeKoulutuksetToteutuksetHakukohteetRaportti(
      hierarkiatWithResults: List[OrganisaatioHierarkiaWithHakukohteet],
      raporttiColumnTitles: Map[String, List[String]],
      userLng: String,
      raporttityyppi: String,
      translations: Map[String, String]
  ): XSSFWorkbook = {
    val workbook: XSSFWorkbook = new XSSFWorkbook()
    try {
      LOG.info("Creating new excel from db results")
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
      val row             = sheet.createRow(currentRowIndex)
      currentRowIndex = currentRowIndex + 1

      val titles                        = raporttiColumnTitles.getOrElse(userLng, raporttiColumnTitles.getOrElse("fi", List()))
      val raporttiColumnTitlesWithIndex = titles.zipWithIndex
      raporttiColumnTitlesWithIndex.foreach { case (title, index) =>
        val cell = row.createCell(index)
        cell.setCellStyle(headingCellStyle)
        cell.setCellValue(title)
      }

      createKoulutuksetToteutuksetHakukohteetResultRows(
        workbook = workbook,
        sheet = sheet,
        hierarkiatWithHakukohteet = hierarkiatWithResults,
        initialRowIndex = currentRowIndex,
        headingCellStyle = headingCellStyle,
        cellStyle = bodyTextCellStyle,
        hakukohteenNimiTextCellStyle = hakukohteenNimiTextCellStyle,
        headingFont = headingFont,
        subHeadingFont = subHeadingFont,
        asiointikieli = userLng,
        raporttiColumnTitlesWithIndex = raporttiColumnTitlesWithIndex,
        raporttityyppi = raporttityyppi
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

  def createHakijaHeadingRow(
      sheet: XSSFSheet,
      asiontikieli: String,
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

  def writeHakijatRaportti(
      hakijat: Seq[HakijaWithCombinedNimi | KkHakijaWithCombinedNimi],
      asiointikieli: String,
      translations: Map[String, String],
      id: String,
      naytaYoArvosanat: Option[Boolean] = None
  ): XSSFWorkbook = {
    val workbook: XSSFWorkbook = new XSSFWorkbook()
    LOG.info("Creating new excel from db results")
    val sheet: XSSFSheet = workbook.createSheet()
    workbook.setSheetName(
      0,
      WorkbookUtil.createSafeSheetName(translations.getOrElse("raportti.yhteenveto", "raportti.yhteenveto"))
    )

    val headingCellStyle: XSSFCellStyle  = workbook.createCellStyle()
    val bodyTextCellStyle: XSSFCellStyle = workbook.createCellStyle()

    val headingFont  = createHeadingFont(workbook, headingCellStyle)
    val bodyTextFont = createBodyTextFont(workbook, bodyTextCellStyle)

    var currentRowIndex = 0

    val c = if (id == "korkeakoulu") classOf[KkHakijaWithCombinedNimi] else classOf[HakijaWithCombinedNimi]

    val fieldNames          = c.getDeclaredFields.map(_.getName).toList
    val fieldNamesWithIndex = fieldNames.zipWithIndex

    currentRowIndex =
      createHakijaHeadingRow(sheet, asiointikieli, translations, currentRowIndex, fieldNames, headingCellStyle)

    // TODO: päätellään täällä näytetäänkö yo, hetu, osoite raporttityypin ja
    hakijat.foreach(hakutoive => {
      val hakijanHakutoiveRow = sheet.createRow(currentRowIndex)
      currentRowIndex = currentRowIndex + 1

      for (i <- 0 until hakutoive.productArity) yield {
        val fieldName = fieldNamesWithIndex.find((name, index) => i == index) match {
          case Some((name, i)) => name
          case None            => ""
        }

        val cell = hakijanHakutoiveRow.createCell(i)
        cell.setCellStyle(bodyTextCellStyle)
        hakutoive.productElement(i) match {
          case kielistetty: Kielistetty =>
            val kielistettyValue = kielistetty.get(Kieli.withName(asiointikieli)) match {
              case Some(value) => value
              case None        => "-"
            }
            cell.setCellValue(kielistettyValue)
          case Some(d: LocalDate) =>
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            cell.setCellValue(d.format(formatter))
          case s: String if List("valintatieto").contains(fieldName) =>
            val lowerCaseStr = s.toLowerCase
            val translation  = translations.getOrElse(s"raportti.$lowerCaseStr", s"raportti.$lowerCaseStr")
            cell.setCellValue(translation)
          case s: String =>
            cell.setCellValue(s)
          case Some(s: String) if List("vastaanottotieto", "ilmoittautuminen").contains(fieldName) =>
            val lowerCaseStr = s.toLowerCase
            val translation  = translations.getOrElse(s"raportti.$lowerCaseStr", s"raportti.$lowerCaseStr")
            cell.setCellValue(translation)
          case Some(s: String) if List("harkinnanvaraisuus").contains(fieldName) =>
            val value = if (s.startsWith("EI_HARKINNANVARAINEN")) {
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
            cell.setCellValue(value)
          case Some(s: String) =>
            cell.setCellValue(s)
          case Some(int: Int) =>
            cell.setCellValue(int)
          case int: Int =>
            cell.setCellValue(int)
          case Some(b: Boolean)
              if List(
                "turvakielto",
                "kaksoistutkintoKiinnostaa",
                "urheilijatutkintoKiinnostaa",
                "soraAiempi",
                "soraTerveys",
                "markkinointilupa",
                "julkaisulupa",
                "sahkoinenViestintaLupa",
                "ensikertalainen"
              ).contains(fieldName) =>
            val translation =
              if (b) translations.getOrElse("raportti.kylla", "raportti.kylla")
              else translations.getOrElse("raportti.ei", "raportti.ei")
            cell.setCellValue(translation)
          case Some(b: Boolean) =>
            val value = if (b) "X" else "-"
            cell.setCellValue(value)
          case _ =>
            cell.setCellValue("-")
        }
      }
    })

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
}
