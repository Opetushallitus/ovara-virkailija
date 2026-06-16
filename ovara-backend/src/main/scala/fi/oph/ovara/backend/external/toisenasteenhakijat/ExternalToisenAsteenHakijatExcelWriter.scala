package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.domain.{En, Fi, Kielistetty, Sv}
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFRow, XSSFSheet, XSSFWorkbook}

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object ExternalToisenAsteenHakijatExcelWriter {

  private val Headers: Seq[String] = Seq(
    "Hetu",
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
    "Muupuhelin",
    "Sahkoposti",
    "Kotikunta",
    "Sukupuoli",
    "Aidinkieli",
    "Opetuskieli",
    "Huoltaja 1 etunimi",
    "Huoltaja 1 sukunimi",
    "Huoltaja 1 puh",
    "Huoltaja 1 email",
    "Huoltaja 2 etunimi",
    "Huoltaja 2 sukunimi",
    "Huoltaja 2 puh",
    "Huoltaja 2 email",
    "Koulutusmarkkinointilupa",
    "Kiinnostunut oppisopimuskoulutuksesta",
    "Oppivelvollisuus voimassa asti",
    "Oikeus maksuttomaan koulutukseen voimassa asti",
    "Vuosi",
    "Kausi",
    "Hakemusnumero",
    "Hakemus jätetty",
    "Hakemusta viimeksi muokattu",
    "Lahtokoulu",
    "Lahtokoulunnimi",
    "Luokka",
    "Luokkataso",
    "Pohjakoulutus",
    "Todistusvuosi",
    "Julkaisulupa",
    "Yhteisetaineet",
    "Lukiontasapisteet",
    "Yleinenkoulumenestys",
    "Lisapistekoulutus",
    "Painotettavataineet",
    "Keskiarvo valintalaskennasta",
    "Hakujno",
    "Oppilaitos",
    "Opetuspiste",
    "Opetuspisteennimi",
    "Koulutus",
    "HakukohdeOid",
    "Harkinnanvaraisuuden peruste",
    "Urheilijan ammatillinen koulutus",
    "Yhteispisteet",
    "Valinta",
    "Vastaanotto",
    "Lasnaolo",
    "Terveys",
    "Aiempiperuminen",
    "Kaksoistutkinto",
    "Urheilija-peruskoulu",
    "Urheilija-keskiarvo",
    "Urheilija-tamakausi",
    "Urheilija.viimekausi",
    "Urheilija-toissakausi",
    "Urheilija-sivulaji",
    "Urheilija-valmennusryhma-seurajoukkue",
    "Urheilija-valmennusryhma-piirijoukkue",
    "Urheilija-valmennusryhma-maajoukkue",
    "Urheilija-valmentaja-nimi",
    "Urheilija-valmentaja-email",
    "Urheilija-valmentaja-puh",
    "Urheilija-laji",
    "Urheilija-liitto",
    "Urheilija-seura",
    "Sähköisen asioinnin lupa"
  )

  private val DateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

  private val SheetName: String = "Hakijat"

  def write(hakijat: Seq[ToisenAsteenHakija]): XSSFWorkbook = {
    val workbook = new XSSFWorkbook()
    val sheet    = workbook.createSheet(WorkbookUtil.createSafeSheetName(SheetName))

    writeHeaderRow(sheet)

    val rows = hakijat.flatMap { h =>
      if (h.hakemus.hakutoiveet.isEmpty) Seq.empty
      else h.hakemus.hakutoiveet.map(ht => rowValues(h, ht))
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

  private def rowValues(h: ToisenAsteenHakija, ht: HakijaHakutoive): Seq[String] = Seq(
    optStr(h.hetu),
    h.oppijanumero,
    optStr(h.sukunimi),
    optStr(h.etunimet),
    optStr(h.kutsumanimi),
    h.lahiosoite,
    h.postinumero,
    h.postitoimipaikka,
    optStr(h.maa),
    h.kansalaisuudet.mkString(", "),
    h.matkapuhelin,
    optStr(h.muupuhelin),
    h.sahkoposti,
    optStr(h.kotikunta),
    optStr(h.sukupuoli),
    optStr(h.aidinkieli),
    optStr(h.opetuskieli),
    optStr(h.huoltaja1.map(_.etunimi)),
    optStr(h.huoltaja1.map(_.sukunimi)),
    optStr(h.huoltaja1.map(_.puhelinnumero)),
    optStr(h.huoltaja1.map(_.sahkoposti)),
    optStr(h.huoltaja2.map(_.etunimi)),
    optStr(h.huoltaja2.map(_.sukunimi)),
    optStr(h.huoltaja2.map(_.puhelinnumero)),
    optStr(h.huoltaja2.map(_.sahkoposti)),
    optBooleanKielistys(h.koulutusmarkkinointilupa),
    optBooleanKielistys(h.kiinnostunutoppisopimuksesta),
    optStr(h.oppivelvollisuusVoimassaAsti),
    optStr(h.oikeusMaksuttomaanKoulutukseenVoimassaAsti),
    optStr(h.hakemus.vuosi),
    optStr(h.hakemus.kausi),
    h.hakemus.hakemusnumero,
    formatDate(h.hakemus.hakemuksenJattopaiva),
    formatDate(h.hakemus.hakemuksenMuokkauspaiva),
    optStr(h.hakemus.lahtokoulu),
    optStr(h.hakemus.lahtokoulunnimi),
    optStr(h.hakemus.luokka),
    optStr(h.hakemus.luokkataso),
    optStr(h.hakemus.pohjakoulutus),
    optStr(h.hakemus.todistusvuosi),
    optBooleanKielistys(h.hakemus.julkaisulupa),
    numericOrZero(h.hakemus.yhteisetaineet),
    numericOrZero(h.hakemus.lukiontasapisteet),
    numericOrZero(h.hakemus.yleinenkoulumenestys),
    optStr(h.hakemus.lisapistekoulutus),
    numericOrZero(h.hakemus.painotettavataineet),
    optStr(ht.keskiarvo),
    ht.hakujno.toString,
    optStr(ht.oppilaitos),
    optStr(ht.opetuspiste),
    kielistettyToString(ht.opetuspisteennimi),
    koulutusToString(ht.koulutus),
    ht.hakukohdeOid,
    optStr(ht.harkinnanvaraisuusperuste),
    onlyKylla(ht.urheilijanammatillinenkoulutus),
    numericOrZero(ht.yhteispisteet),
    optStr(ht.valinta),
    optStr(ht.vastaanotto),
    optStr(ht.lasnaolo),
    optBooleanKielistys(ht.terveys),
    optBooleanKielistys(ht.aiempiperuminen),
    optBooleanKielistys(ht.kaksoistutkinto),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.peruskoulu)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.keskiarvo)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.tamakausi)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.viimekausi)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.toissakausi)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.sivulaji)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.valmennusryhma_seurajoukkue)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.valmennusryhma_piirijoukkue)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.valmennusryhma_maajoukkue)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.valmentaja_nimi)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.valmentaja_email)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.valmentaja_puh)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.laji)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.liitto)),
    optStr(ht.urheilijanLisakysymykset.flatMap(_.seura)),
    optBooleanKielistys(h.sahkoisenAsioinninLupa)
  )

  private def optStr(opt: Option[String]): String = opt.getOrElse("")

  private def optBooleanKielistys(b: Option[Boolean]): String = b match {
    case Some(true)  => "Kyllä"
    case Some(false) => "Ei"
    case None        => ""
  }

  private def onlyKylla(b: Option[Boolean]): String =
    if (b.contains(true)) "Kyllä" else ""

  private def numericOrZero(opt: Option[BigDecimal]): String =
    opt.getOrElse(BigDecimal(0)).toString

  private def formatDate(opt: Option[OffsetDateTime]): String =
    opt.map(_.format(DateFormatter)).getOrElse("")

  private def kielistettyToString(opt: Option[Kielistetty]): String =
    opt.flatMap(k => k.get(Fi).orElse(k.get(Sv)).orElse(k.get(En))).getOrElse("")

  private def koulutusToString(opt: Option[KoodistoArvo]): String =
    opt
      .flatMap(k => k.nimi.get(Fi).orElse(k.nimi.get(Sv)).orElse(k.nimi.get(En)))
      .getOrElse(opt.map(_.koodiarvo).getOrElse(""))
}
