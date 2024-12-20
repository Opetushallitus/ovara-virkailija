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

  def createOrganisaatioHeadingRow(
      row: XSSFRow,
      headingCellStyle: XSSFCellStyle,
      asiointikieli: String,
      organisaatio: Organisaatio,
      organisaationKoulutuksetToteutuksetHakukohteet: List[KoulutuksetToteutuksetHakukohteetResult],
      raporttiColumnTitlesWithIndex: List[(String, Int)]
  ): XSSFRow = {
    val orgNameCell = row.createCell(0)
    orgNameCell.setCellStyle(headingCellStyle)
    val kielistettyNimi = organisaatio.organisaatio_nimi(Kieli.withName(asiointikieli))

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
      queryResult: Map[String, List[(Organisaatio, OrganisaationKoulutuksetToteutuksetHakukohteet)]],
      raporttiColumnTitles: Map[String, List[String]],
      userLng: String
  ): XSSFWorkbook = {
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
      var currentRowIndex = 0
      val row             = sheet.createRow(currentRowIndex)
      currentRowIndex = currentRowIndex + 1
      val titles                        = raporttiColumnTitles.getOrElse(userLng, raporttiColumnTitles.getOrElse("fi", List()))
      val raporttiColumnTitlesWithIndex = titles.zipWithIndex
      raporttiColumnTitlesWithIndex.foreach { case (title, index) =>
        val cell = row.createCell(index)
        cell.setCellValue(title)
        cell.setCellStyle(headingCellstyle)
      }

      queryResult.foreach(orgResults => {
        val parentOrgOid = orgResults._1
        val res          = orgResults._2

        val orgKths = orgResults._2
        if (orgKths.nonEmpty) {
          val parentOrgRow = sheet.createRow(currentRowIndex)
          currentRowIndex = currentRowIndex + 1
          val parentOrg = orgKths.head._1
          val organisaationTulokset =
            orgResults._2
              .flatMap((r: (Organisaatio, OrganisaationKoulutuksetToteutuksetHakukohteet)) =>
                r._2.koulutuksetToteutuksetHakukohteet
              )

          createOrganisaatioHeadingRow(
            parentOrgRow,
            headingCellstyle,
            userLng,
            parentOrg,
            organisaationTulokset,
            raporttiColumnTitlesWithIndex
          )

          res.zipWithIndex.foreach { case ((org, koulutuksetToteutuksetHakukohteet), index) =>
            val childOrg = koulutuksetToteutuksetHakukohteet.organisaatio
            val childOrgOid = childOrg match {
              case Some(org) => org.organisaatio_oid
              case _         => ""
            }

            val koulTotHak = koulutuksetToteutuksetHakukohteet.koulutuksetToteutuksetHakukohteet.toList

            if (parentOrgOid != childOrgOid && childOrgOid != "") {
              val orgRow = sheet.createRow(currentRowIndex)
              currentRowIndex = currentRowIndex + 1

              createOrganisaatioHeadingRow(
                orgRow,
                headingCellstyle,
                userLng,
                childOrg.get, //TODO: Muuta param optioniksi
                koulTotHak,
                raporttiColumnTitlesWithIndex
              )
            }

            val kths = koulutuksetToteutuksetHakukohteet._2
            var kthsLen = kths.length
            kths.zipWithIndex.foreach(
              (kth: KoulutuksetToteutuksetHakukohteetResult, resultRowIndex: Int) => {
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
            )
          }
        }
      })
    } catch {
      case e: Exception =>
        LOG.error(s"Error creating excel: ${e.getMessage}")
        throw e
    }

    workbook
  }
}
