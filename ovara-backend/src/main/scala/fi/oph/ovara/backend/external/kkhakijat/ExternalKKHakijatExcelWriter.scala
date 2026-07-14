package fi.oph.ovara.backend.external.kkhakijat

import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFRow, XSSFSheet, XSSFWorkbook}

object ExternalKKHakijatExcelWriter {

  private val Headers: Seq[String] = Seq(
    "Hetu",
    "Syntymäaika",
    "Oppijanumero",
    "Sukunimi",
    "Etunimet",
    "Kutsumanimi",
    "Lahiosoite",
    "Postinumero",
    "Postitoimipaikka",
    "Maa",
    "Kansalaisuudet",
    "Matkapuhelin",
    "Puhelin",
    "Sahkoposti",
    "Lukuvuosimaksu",
    "Kotikunta",
    "Sukupuoli",
    "Aidinkieli",
    "Asiointikieli",
    "Koulusivistyskielet",
    "Koulutusmarkkinointilupa",
    "On ylioppilas",
    "Suoritusvuosi",
    "On ensikertalainen",
    "Haku",
    "Hakuvuosi",
    "Hakukausi",
    "Hakemusnumero",
    "Hakemuksen jättämisen aikaleima",
    "Hakemuksen viimeinen muokkausaikaleima",
    "Organisaatio",
    "Hakukohde",
    "Hakukohteen kk-id",
    "Hakutoiveen prioriteetti",
    "Avoin vayla",
    "Valinnan tila",
    "Valinnan aikaleima",
    "Pisteet",
    "Hyväksymisen ehto (ehdolliisestiHyvaksyttavissa,ehtoKoodi,ehtoFI,ehtoSV,ehtoEN)",
    "Valintatapajonon tyyppi",
    "Valintatapajonon nimi",
    "Vastaanottotieto",
    "Ilmoittautumiset",
    "Pohjakoulutus",
    "Julkaisulupa",
    "Hakukelpoisuus",
    "Hakukelpoisuuden lahde",
    "Maksuvelvollisuus",
    "Hakukohteen koulutukset 1(komoOid,koulutusKoodi,kkKoulutusId,koulutuksenAlkamisvuosi,koulutuksenAlkamiskausi," +
      "johtaaTutkintoon)",
    "Koulutus 2",
    "Koulutus 3",
    "Koulutus 4",
    "Koulutus 5",
    "Koulutus 6",
    "Liite 1(hakuId,hakuRyhmäId,tila,saapumisenTila,nimi,vastaanottaja)",
    "Liite 2",
    "Liite 3",
    "Liite 4",
    "Liite 5",
    "Liite 6"
  )

  private val SheetName: String = "KK-Hakijat"

  def write(hakijat: Seq[KKHakija]): XSSFWorkbook = {
    val workbook = new XSSFWorkbook()
    val sheet    = workbook.createSheet(WorkbookUtil.createSafeSheetName(SheetName))

    writeHeaderRow(sheet)

    val rows = hakijat.flatMap { h =>
      if (h.hakemukset.isEmpty) Seq.empty
      else h.hakemukset.map(hm => rowValues(h, hm))
    }

    rows.zipWithIndex.foreach { case (cells, idx) =>
      writeRow(sheet, idx + 1, cells)
    }

    Headers.indices.foreach(sheet.autoSizeColumn)

    workbook
  }

  private def writeHeaderRow(sheet: XSSFSheet): Unit = {
    val row = sheet.createRow(0)
    Headers.zipWithIndex.foreach { case (header, idx) =>
      val cell: XSSFCell = row.createCell(idx)
      cell.setCellValue(header)
    }
  }

  private def writeRow(sheet: XSSFSheet, rowIndex: Int, values: Seq[String]): Unit = {
    val row: XSSFRow = sheet.createRow(rowIndex)
    values.zipWithIndex.foreach { case (value, idx) =>
      val cell: XSSFCell = row.createCell(idx)
      cell.setCellValue(value)
    }
  }

  private def rowValues(h: KKHakija, hm: KKHakemus): Seq[String] = Seq(
    h.hetu,                                                    // 0
    optStr(h.syntymaaika),                                     // 1
    h.oppijanumero,                                            // 2
    h.sukunimi,                                                // 3
    h.etunimet,                                                // 4
    h.kutsumanimi,                                             // 5
    h.lahiosoite,                                              // 6
    h.postinumero,                                             // 7
    h.postitoimipaikka,                                        // 8
    h.maa,                                                     // 9
    h.kansalaisuudet.map(_.mkString(", ")).getOrElse(""),      // 10
    optStr(h.matkapuhelin),                                    // 11
    optStr(h.puhelin),                                         // 12
    optStr(h.sahkoposti),                                      // 13
    optStr(hm.lukuvuosimaksu),                                 // 14
    h.kotikunta,                                               // 15
    h.sukupuoli,                                               // 16
    h.aidinkieli,                                              // 17
    h.asiointikieli,                                           // 18
    h.koulusivistyskielet.map(_.mkString(", ")).getOrElse(""), // 19
    onlyX(h.koulutusmarkkinointilupa),                         // 20
    onlyX(Some(h.onYlioppilas)),                               // 21
    optStr(h.yoSuoritusVuosi),                                 // 22
    h.ensikertalainen.map(onlyXBool).getOrElse(""),            // 23
    hm.haku,                                                   // 24
    hm.hakuVuosi.toString,                                     // 25
    hm.hakuKausi,                                              // 26
    hm.hakemusnumero,                                          // 27
    optStr(hm.hakemusJattoAikaleima),                          // 28
    optStr(hm.hakemusViimeinenMuokkausAikaleima),              // 29
    hm.organisaatio,                                           // 30
    hm.hakukohde,                                              // 31
    optStr(hm.hakukohdeKkId),                                  // 32
    hm.hakutoivePrioriteetti.map(_.toString).getOrElse(""),    // 33
    onlyX(hm.avoinVayla),                                      // 34
    hm.valinnanTila.map(_.name).getOrElse(""),                 // 35
    optStr(hm.valinnanAikaleima),                              // 36
    hm.pisteet.map(_.toString).getOrElse(""),                  // 37
    hm.hyvaksymisenEhto match {                                // 38
      case None    => ""
      case Some(e) =>
        s"HyvaksymisenEhto(${onlyXBool(e.ehdollisestiHyvaksyttavissa)},${e.ehtoKoodi.getOrElse("")}," +
          s"${e.ehtoFI.getOrElse("")},${e.ehtoSV.getOrElse("")},${e.ehtoEN.getOrElse("")})"
    },
    optStr(hm.valintatapajononTyyppi),             // 39
    optStr(hm.valintatapajononNimi),               // 40
    hm.vastaanottotieto.map(_.name).getOrElse(""), // 41
    hm.ilmoittautumiset.map(_.name).mkString(","), // 42
    hm.pohjakoulutus.mkString(","),                // 43
    onlyX(hm.julkaisulupa),                        // 44
    hm.hKelpoisuus,                                // 45
    optStr(hm.hKelpoisuusLahde),                   // 46
    optStr(hm.hKelpoisuusMaksuvelvollisuus),       // 47
    koulutusAt(hm.hakukohteenKoulutukset, 0),      // 48
    koulutusAt(hm.hakukohteenKoulutukset, 1),      // 49
    koulutusAt(hm.hakukohteenKoulutukset, 2),      // 50
    koulutusAt(hm.hakukohteenKoulutukset, 3),      // 51
    koulutusAt(hm.hakukohteenKoulutukset, 4),      // 52
    koulutusAt(hm.hakukohteenKoulutukset, 5),      // 53
    liiteAt(hm.liitteet, 0),                       // 54
    liiteAt(hm.liitteet, 1),                       // 55
    liiteAt(hm.liitteet, 2),                       // 56
    liiteAt(hm.liitteet, 3),                       // 57
    liiteAt(hm.liitteet, 4),                       // 58
    liiteAt(hm.liitteet, 5)                        // 59
  )

  private def optStr(opt: Option[String]): String = opt.getOrElse("")

  private def onlyX(b: Option[Boolean]): String =
    if (b.contains(true)) "X" else ""

  private def onlyXBool(b: Boolean): String =
    if (b) "X" else ""

  private def koulutusAt(ks: Seq[KkHakukohteenkoulutus], idx: Int): String =
    ks.lift(idx) match {
      case None    => ""
      case Some(k) =>
        s"(${k.komoOid},${k.koulutusKoodi.getOrElse("")},${k.kkKoulutusId.getOrElse("")}," +
          s"${k.koulutuksenAlkamisvuosi.map(_.toString).getOrElse("")}," +
          s"${k.koulutuksenAlkamiskausi.getOrElse("")}," +
          s"${k.johtaaTutkintoon.map(_.toString).getOrElse("")})"
    }

  private def liiteAt(ls: Option[Seq[Liite]], idx: Int): String =
    ls.flatMap(_.lift(idx)) match {
      case None    => ""
      case Some(l) =>
        s"(${l.hakuId.getOrElse("")},${l.hakuRyhmaId.getOrElse("")},${l.tila.getOrElse("")}," +
          s"${l.saapumisenTila.getOrElse("")},${l.nimi.getOrElse("")},${l.vastaanottaja.getOrElse("")})"
    }
}
