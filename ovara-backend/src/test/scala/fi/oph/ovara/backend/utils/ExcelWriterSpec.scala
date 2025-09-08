package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.{Koodi, *}
import fi.oph.ovara.backend.utils.Constants.*
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.xssf.usermodel.{XSSFCellStyle, XSSFSheet, XSSFWorkbook}
import org.scalatest.flatspec.AnyFlatSpec

import java.time.LocalDate

class ExcelWriterSpec extends AnyFlatSpec {
  val userLng: String = "sv"

  val translations: Map[String, String] = Map(
    "raportti.yhteenveto"                       -> "Yhteenveto SV",
    "raportti.hakijanSukunimi"                  -> "Sukunimi SV",
    "raportti.hakijanEtunimi"                   -> "Etunimi SV",
    "raportti.turvakielto"                      -> "Turvakielto SV",
    "raportti.kansalaisuus"                     -> "Kansalaisuus SV",
    "raportti.kansalaisuudet"                   -> "Kansalaisuudet SV",
    "raportti.hakukohteenNimi"                  -> "Hakukohde SV",
    "raportti.haku"                             -> "Haku SV",
    "raportti.kaksoistutkintoKiinnostaa"        -> "Kaksoistutkinto kiinnostaa SV",
    "raportti.urheilijatutkintoKiinnostaa"      -> "Urheilijatutkinto kiinnostaa SV",
    "raportti.markkinointilupa"                 -> "LupaMark SV",
    "raportti.kylla"                            -> "Ja",
    "raportti.ei"                               -> "Nej",
    "raportti.hylatty"                          -> "Hylatty SV",
    "raportti.hyvaksytty"                       -> "Hyvaksytty SV",
    "raportti.peruuntunut"                      -> "Peruuntunut SV",
    "raportti.peruutettu"                       -> "Peruutettu SV",
    "raportti.ei_vastaanotettu_maara_aikana"    -> "Ei vastaanotettu SV",
    "raportti.kokonaispisteet"                  -> "Kokonaispisteet SV",
    "raportti.valintatapajonokohtainenTila"     -> "Valintatapajonokohtainen tila SV",
    "raportti.toimipiste"                       -> "Toimipiste SV",
    "raportti.hakukohde"                        -> "Hakukohde SV",
    "raportti.hakukelpoisuus"                   -> "Hakukelpoisuus SV",
    "raportti.eligible"                         -> "hakukelpoinen SV",
    "raportti.oppilaitos"                       -> "Oppilaitos SV",
    "raportti.hakijat-yht"                      -> "Hakijat SV",
    "raportti.ensisijaisia"                     -> "Ensisijaisia SV",
    "raportti.ensikertalaisia"                  -> "Ensikertalaisia SV",
    "raportti.maksuvelvollisia"                 -> "Maksuvelvollisia SV",
    "raportti.hakukohderyhma"                   -> "Hakukohderyhma SV",
    "raportti.varasija"                         -> "Varasija SV",
    "raportti.hyvaksytyt"                       -> "Hyväksytyt SV",
    "raportti.pohjakoulutus"                    -> "Pohjakoulutus SV",
    "raportti.puhelinnumero"                    -> "Puhelinnumero SV",
    "raportti.not-obligated"                    -> "Ei velvollinen",
    "raportti.obligated"                        -> "Velvollinen",
    "raportti.organisaatio"                     -> "Organisaatio SV",
    "raportti.unreviewed"                       -> "Tarkastamatta SV",
    "raportti.vastaanottaneet"                  -> "Vastaanottaneet SV",
    "raportti.valintatiedonPvm"                 -> "Valintatiedon päivämäärä SV",
    "raportti.lasna"                            -> "Läsnä SV",
    "raportti.poissa"                           -> "Poissa SV",
    "raportti.ilm-yht"                          -> "IlmYht SV",
    "raportti.aloituspaikat"                    -> "Aloituspaikat SV",
    "raportti.valinnan-aloituspaikat"           -> "Valinnan aloituspaikat SV",
    "raportti.toive1"                           -> "Toive1 SV",
    "raportti.toive2"                           -> "Toive2 SV",
    "raportti.toive3"                           -> "Toive3 SV",
    "raportti.toive4"                           -> "Toive4 SV",
    "raportti.toive5"                           -> "Toive5 SV",
    "raportti.toive6"                           -> "Toive6 SV",
    "raportti.toive7"                           -> "Toive7 SV",
    "raportti.yhteensa"                         -> "Yhteensä SV",
    "raportti.yksittaiset-hakijat"              -> "Yksittäiset hakijat SV",
    "raportti.oppilaitosJaToimipiste"           -> "Oppilaitos ja toimipiste SV",
    "raportti.julkaistu"                        -> "Julkaistu SV",
    "raportti.arkistoitu"                       -> "Arkistoitu SV",
    "raportti.luonnos"                          -> "Luonnos SV",
    "raportti.hakukohdeOid"                     -> "Hakukohteen oid SV",
    "raportti.koulutuksenTila"                  -> "Kou.tila SV",
    "raportti.toteutuksenTila"                  -> "Tot.tila SV",
    "raportti.hakukohteenTila"                  -> "Hak.tila SV",
    "raportti.onValintakoe"                     -> "Koe SV",
    "raportti.voiSuorittaaKaksoistutkinnon"     -> "Voi suorittaa kaksoistutkinnon? SV",
    "raportti.jarjestaaUrheilijanAmmKoulutusta" -> "Voi suorittaa tutkinnon urheilijana? SV",
    "raportti.hakuparametrit"                   -> "Hakuparametrit SV",
    "raportti.koulutustoimija"                  -> "Koulutustoimija SV",
    "raportti.oppilaitos"                       -> "Oppilaitos SV",
    "raportti.valintakoe"                       -> "Valintakoe SV",
    "raportti.tulostustapa"                     -> "Tulostustapa SV",
    "raportti.tulostustapa.hakukohteittain"     -> "Hakukohteittain SV",
    "raportti.kk-tutkinnon-taso"                -> "Tutkinnon taso SV",
    "raportti.ylempi"                           -> "ylempi SV",
    "raportti.valintatieto"                     -> "Valintatieto SV",
    "raportti.vastaanottotieto"                 -> "Vastaanottotieto SV",
    "raportti.harkinnanvaraisuus"               -> "Harkinnanvaraisuus SV",
    "raportti.oppimisvaikeudet"                 -> "Oppimisvaikeudet SV",
    "raportti.soraTerveys"                      -> "Sora terveydentila SV",
    "raportti.soraAiempi"                       -> "Sora aiempi SV",
    "raportti.julkaisulupa"                     -> "Julkaisulupa SV",
    "raportti.opetuskieli"                      -> "Opetuskieli SV",
    "raportti.koulutusala1"                     -> "Koulutusala 1 SV",
    "raportti.koulutusala2"                     -> "Koulutusala 2 SV",
    "raportti.koulutusala3"                     -> "Koulutusala 3 SV",
  )

  def checkAloituspaikatRowValidity(sheet: XSSFSheet, rowNumber: Int, expected: Int): Unit = {
    for (i <- 1 until 5) {
      assert(sheet.getRow(rowNumber).getCell(i) == null)
    }
    assert(sheet.getRow(rowNumber).getCell(5).getNumericCellValue == expected)
    for (i <- 6 until 8) {
      assert(sheet.getRow(rowNumber).getCell(i) == null)
    }
  }

  "countAloituspaikat" should "return 5 for one hakukohde with 5 aloituspaikkaa" in {
    val organisaationKoulutuksetToteutuksetHakukohteet = List(
      OrganisaationKoulutusToteutusHakukohde(
        organisaatio_oid = Some("1.2.246.562.10.278170642010"),
        koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
          hakukohteenNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
          hakukohdeOid = "1.2.246.562.20.00000000000000021565",
          koulutuksenTila = Some("julkaistu"),
          toteutuksenTila = Some("julkaistu"),
          hakukohteenTila = Some("julkaistu"),
          aloituspaikat = Some(5),
          onValintakoe = Some(false)
        )
      )
    )

    assert(ExcelWriter.countAloituspaikat(organisaationKoulutuksetToteutuksetHakukohteet) == 5)
  }

  it should "return 35 for three hakukohde aloituspaikat summed up" in {
    val kth = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.278170642010"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohteenNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(5),
        onValintakoe = Some(false)
      )
    )

    val kth2 = kth.copy(koulutusToteutusHakukohde =
      kth._2.copy(
        hakukohteenNimi =
          Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000012345",
        aloituspaikat = Some(20)
      )
    )

    val kth3 = kth.copy(koulutusToteutusHakukohde =
      kth._2.copy(
        hakukohteenNimi =
          Map(En -> "Hakukohteen 3 nimi en", Fi -> "Hakukohteen 3 nimi fi", Sv -> "Hakukohteen 3 nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000025467",
        aloituspaikat = Some(10)
      )
    )

    val organisaationKoulutuksetToteutuksetHakukohteet = List(kth, kth2, kth3)

    assert(ExcelWriter.countAloituspaikat(organisaationKoulutuksetToteutuksetHakukohteet) == 35)
  }

  it should "return 0 if there are no results" in {
    assert(ExcelWriter.countAloituspaikat(List()) == 0)
  }

  "flattenHierarkiaHakukohteet" should "return list with one hakukohde for one org in hierarkia" in {
    val kth = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohteenNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(8),
        onValintakoe = Some(false)
      )
    )

    val hierarkiaWithHakukohteet =
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(),
        List(kth)
      )

    assert(ExcelWriter.flattenHierarkiaHakukohteet(hierarkiaWithHakukohteet) == List(kth))
  }

  it should "return list with two hakukohde for koulutustoimija and oppilaitos in hierarkia" in {
    val kth = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohteenNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(3),
        onValintakoe = Some(false)
      )
    )

    val oppilaitoksenKth = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.2781706420111"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohteenNimi = Map(
          En -> "Oppilaitoksen hakukohteen nimi en",
          Fi -> "Oppilaitoksen hakukohteen nimi fi",
          Sv -> "Oppilaitoksen hakukohteen nimi sv"
        ),
        hakukohdeOid = "1.2.246.562.20.000000000000000215666",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(8),
        onValintakoe = Some(false)
      )
    )

    val hierarkiaWithHakukohteet =
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.2781706420111",
            Map(
              En -> "Oppilaitoksen nimi en",
              Fi -> "Oppilaitoksen nimi fi",
              Sv -> "Oppilaitoksen nimi sv"
            ),
            List("02"),
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.2781706420111"),
            None,
            List(),
            List(oppilaitoksenKth)
          )
        ),
        List(kth)
      )

    assert(ExcelWriter.flattenHierarkiaHakukohteet(hierarkiaWithHakukohteet) == List(kth, oppilaitoksenKth))
  }

  it should "return list with two oppilaitos hakukohde in hierarkia" in {
    val kth1 = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.278170642010"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohteenNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(8),
        onValintakoe = Some(false)
      )
    )

    val kth2 = kth1.copy(koulutusToteutusHakukohde =
      kth1._2.copy(
        hakukohteenNimi =
          Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
        hakukohdeOid = "1.2.246.562.20.0000000000000002156667"
      )
    )

    val hierarkiaWithHakukohteet =
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.278170642010",
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
            List("02"),
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
            None,
            List(),
            List(kth1, kth2)
          )
        ),
        List()
      )

    assert(ExcelWriter.flattenHierarkiaHakukohteet(hierarkiaWithHakukohteet) == List(kth1, kth2))
  }

  it should "return list with one toimipiste hakukohde and two oppilaitos hakukohde in hierarkia" in {
    val oppilaitoksenKth1 = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.278170642010"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohteenNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(8),
        onValintakoe = Some(false)
      )
    )

    val oppilaitoksenKth2 = oppilaitoksenKth1.copy(
      koulutusToteutusHakukohde = oppilaitoksenKth1._2.copy(
        hakukohteenNimi =
          Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
        hakukohdeOid = "1.2.246.562.20.0000000000000002156667"
      )
    )

    val toimipisteenKth = oppilaitoksenKth1.copy(
      koulutusToteutusHakukohde = oppilaitoksenKth1._2.copy(
        hakukohteenNimi = Map(
          En -> "Toimipiste hakukohteen nimi en",
          Fi -> "Toimipiste hakukohteen nimi fi",
          Sv -> "Toimipiste hakukohteen nimi sv"
        ),
        hakukohdeOid = "1.2.246.562.20.000000000000000215521"
      ),
      organisaatio_oid = Some("1.2.246.562.10.2781706420101111")
    )

    val hierarkiaWithHakukohteet =
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.278170642010",
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
            List("02"),
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
            None,
            List(
              OrganisaatioHierarkiaWithHakukohteet(
                "1.2.246.562.10.2781706420101111",
                Map(En -> "Toimipisteen nimi en", Fi -> "Toimipisteen nimi fi", Sv -> "Toimipisteen nimi sv"),
                List("03"),
                List(
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.2781706420000",
                  "1.2.246.562.10.278170642010",
                  "1.2.246.562.10.2781706420101111"
                ),
                None,
                List(),
                List(toimipisteenKth)
              )
            ),
            List(oppilaitoksenKth1, oppilaitoksenKth2)
          )
        ),
        List()
      )

    assert(
      ExcelWriter.flattenHierarkiaHakukohteet(hierarkiaWithHakukohteet) == List(
        oppilaitoksenKth1,
        oppilaitoksenKth2,
        toimipisteenKth
      )
    )
  }

  "createOrganisaatioHeadingRow" should "create row with org name and total count of aloituspaikat" in {
    val oppilaitoksenKth1 = OrganisaationKoulutusToteutusHakukohde(
      organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
      koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
        hakukohteenNimi = Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000021565",
        koulutuksenTila = Some("julkaistu"),
        toteutuksenTila = Some("julkaistu"),
        hakukohteenTila = Some("julkaistu"),
        aloituspaikat = Some(8),
        onValintakoe = Some(false)
      )
    )

    val oppilaitoksenKth2 = oppilaitoksenKth1.copy(
      koulutusToteutusHakukohde = oppilaitoksenKth1._2.copy(
        hakukohteenNimi =
          Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
        hakukohdeOid = "1.2.246.562.20.0000000000000002156667"
      )
    )

    val hierarkiaWithHakukohteet =
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(),
        List(oppilaitoksenKth1, oppilaitoksenKth2)
      )

    val workbook: XSSFWorkbook          = new XSSFWorkbook()
    val sheet: XSSFSheet                = workbook.createSheet()
    val headingCellstyle: XSSFCellStyle = workbook.createCellStyle()
    val titles = classOf[KoulutusToteutusHakukohdeResult].getDeclaredFields
      .map(_.getName)
      .toList

    val raporttiColumnTitlesWithIndex = titles.zipWithIndex

    val currentRowIndex = ExcelWriter.createOrganisaatioHeadingRow(
      sheet = sheet,
      initialRowIndex = 0,
      indentedHeadingCellStyle = headingCellstyle,
      headingCellStyle = headingCellstyle,
      asiointikieli = userLng,
      hierarkiaWithHakukohteet = hierarkiaWithHakukohteet,
      raporttiColumnTitlesWithIndex = raporttiColumnTitlesWithIndex
    )
    assert(sheet.getRow(0).getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")
    assert(sheet.getRow(0).getCell(5).getNumericCellValue == 16)
    assert(sheet.getRow(1) == null)
    assert(sheet.getPhysicalNumberOfRows == 1)
    assert(currentRowIndex == 1)
  }

  it should "not create row with org name and total count of aloituspaikat if there are no hakukohteet" in {
    val hierarkiaWithHakukohteet =
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(),
        List()
      )

    val workbook: XSSFWorkbook          = new XSSFWorkbook()
    val sheet: XSSFSheet                = workbook.createSheet()
    val headingCellstyle: XSSFCellStyle = workbook.createCellStyle()
    val titles = classOf[KoulutusToteutusHakukohdeResult].getDeclaredFields
      .map(_.getName)
      .toList

    val raporttiColumnTitlesWithIndex = titles.zipWithIndex

    val currentRowIndex = ExcelWriter.createOrganisaatioHeadingRow(
      sheet = sheet,
      initialRowIndex = 0,
      indentedHeadingCellStyle = headingCellstyle,
      headingCellStyle = headingCellstyle,
      asiointikieli = userLng,
      hierarkiaWithHakukohteet = hierarkiaWithHakukohteet,
      raporttiColumnTitlesWithIndex = raporttiColumnTitlesWithIndex
    )
    assert(sheet.getRow(0) == null)
    assert(sheet.getPhysicalNumberOfRows == 0)
    assert(currentRowIndex == 0)
  }

  val koulutuksetToteutuksetHakukohteetParams: List[(String, String | Boolean | List[String])] =
    List(
      "haku" -> List("1.2.246.562.29.00000000000000015722"),
      "oppilaitos" -> List.empty,
      "toimipiste" -> List("1.2.246.562.10.00000000001"),
    )

  "writeKoulutuksetToteutuksetHakukohteetRaportti" should "create two sheets and set 'Yhteenveto' as the name of the first sheet" in {
    val hierarkiatWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
        List(),
        List()
      )
    )

    val wb =
      ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
        hierarkiatWithHakukohteet,
        userLng,
        KOULUTUSTOIMIJARAPORTTI,
        translations,
        koulutuksetToteutuksetHakukohteetParams
      )
    assert(wb.getNumberOfSheets == 2)
    assert(wb.getSheetName(0) == "Yhteenveto SV")
    assert(wb.getSheetName(1) == "Hakuparametrit SV")
  }

  it should "create a sheet with the column title row and no results" in {
    val hierarkiatWithHakukohteet = List()
    val wb =
      ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
        hierarkiatWithHakukohteet,
        userLng,
        KOULUTUSTOIMIJARAPORTTI,
        translations,
        koulutuksetToteutuksetHakukohteetParams
      )
    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "Hakukohteen oid SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "Kou.tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "Tot.tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "Hak.tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "Aloituspaikat SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "Koe SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "Voi suorittaa kaksoistutkinnon? SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "Voi suorittaa tutkinnon urheilijana? SV")
    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 1)
    assert(wb.getSheetAt(0).getRow(1) == null)
  }

  it should "create a second sheet with all search terms listed" in {
    val hakuParams: List[(String, String | Boolean | List[String])] =
      List(
        "haku" -> List("1.2.246.562.29.00000000000000015722"),
        "koulutustoimija" -> "1.2.246.562.10.2781706420000",
        "oppilaitos" -> List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        "toimipiste" -> List("1.2.246.562.10.2781706420001"),
        "koulutuksenTila" -> "julkaistu",
        "toteutuksenTila" -> "arkistoitu",
        "hakukohteenTila" -> "julkaistu",
        "valintakoe" -> false
      )
    val hierarkiatWithHakukohteet = List()
    val wb =
      ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
        hierarkiatWithHakukohteet,
        userLng,
        KOULUTUSTOIMIJARAPORTTI,
        translations,
        hakuParams
      )

    val expectedHeaders = List("raportti.hakuehto", "raportti.hakuarvo")
    val expectedRows = List(
      List("Haku SV","1.2.246.562.29.00000000000000015722"),
      List("Koulutustoimija SV", "1.2.246.562.10.2781706420000"),
      List("Oppilaitos SV", "1.2.246.562.10.00000000001, 1.2.246.562.10.2781706420000"),
      List("Toimipiste SV", "1.2.246.562.10.2781706420001"),
      List("Kou.tila SV", "Julkaistu SV"),
      List("Tot.tila SV", "Arkistoitu SV"),
      List("Hak.tila SV", "Julkaistu SV"),
      List("Valintakoe SV", "Nej")
    )
    assert(wb.getNumberOfSheets == 2)
    assert(wb.getSheetAt(1).getRow(1) != null)
    val sheet = wb.getSheetAt(1)

    validateHeaders(sheet = sheet, expectedHeaders = expectedHeaders)
    expectedRows.zipWithIndex.foreach { case (expectedRow, rowIndex) =>
      validateRow(sheet, rowIndex + 1, expectedRow)
    }
  }

  it should "create a sheet with the column title row and one result row for koulutustoimija" in {
    val hierarkiatWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(),
        List()
      )
    )

    val wb =
      ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
        hierarkiatWithHakukohteet,
        userLng,
        KOULUTUSTOIMIJARAPORTTI,
        translations,
        koulutuksetToteutuksetHakukohteetParams
      )
    val sheet = wb.getSheetAt(0)
    // Heading row
    assert(sheet.getRow(0).getCell(0).getStringCellValue == "Hakukohde SV")
    assert(sheet.getRow(0).getCell(4).getStringCellValue == "Hak.tila SV")
    assert(sheet.getRow(0).getCell(5).getStringCellValue == "Aloituspaikat SV")
    assert(sheet.getRow(0).getCell(2).getStringCellValue == "Kou.tila SV")
    assert(sheet.getRow(0).getCell(3).getStringCellValue == "Tot.tila SV")
    assert(sheet.getRow(0).getCell(1).getStringCellValue == "Hakukohteen oid SV")
    assert(sheet.getRow(0).getCell(6).getStringCellValue == "Koe SV")
    assert(sheet.getRow(0).getCell(7).getStringCellValue == "Voi suorittaa kaksoistutkinnon? SV")
    assert(sheet.getRow(0).getCell(8).getStringCellValue == "Voi suorittaa tutkinnon urheilijana? SV")
    // Parent organisaatio row with aloituspaikat sum
    assert(sheet.getRow(1) == null)
    assert(sheet.getPhysicalNumberOfRows == 1)
  }

  it should "create a sheet with the column title row and two result rows for oppilaitos under koulutustoimija" in {
    val hierarkiatWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.278170642010",
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
            List("02"),
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
            None,
            List(),
            List(
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.278170642010"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohteenNimi =
                    Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
                  hakukohdeOid = "1.2.246.562.20.00000000000000021565",
                  koulutuksenTila = Some("julkaistu"),
                  toteutuksenTila = Some("julkaistu"),
                  hakukohteenTila = Some("julkaistu"),
                  aloituspaikat = Some(8),
                  onValintakoe = Some(false)
                )
              ),
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.278170642010"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohteenNimi =
                    Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
                  hakukohdeOid = "1.2.246.562.20.00000000000000031232",
                  koulutuksenTila = Some("julkaistu"),
                  toteutuksenTila = Some("tallennettu"),
                  hakukohteenTila = Some("tallennettu"),
                  aloituspaikat = Some(5),
                  onValintakoe = Some(true),
                  voiSuorittaaKaksoistutkinnon = Some(true),
                  jarjestaaUrheilijanAmmKoulutusta = Some(false)
                )
              )
            )
          )
        ),
        List()
      )
    )

    val wb =
      ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
        hierarkiatWithHakukohteet,
        userLng,
        KOULUTUSTOIMIJARAPORTTI,
        translations,
        koulutuksetToteutuksetHakukohteetParams
      )
    val sheet = wb.getSheetAt(0)
    // Heading row
    assert(sheet.getRow(0).getCell(0).getStringCellValue == "Hakukohde SV")
    assert(sheet.getRow(0).getCell(1).getStringCellValue == "Hakukohteen oid SV")
    assert(sheet.getRow(0).getCell(2).getStringCellValue == "Kou.tila SV")
    assert(sheet.getRow(0).getCell(3).getStringCellValue == "Tot.tila SV")
    assert(sheet.getRow(0).getCell(4).getStringCellValue == "Hak.tila SV")
    assert(sheet.getRow(0).getCell(5).getStringCellValue == "Aloituspaikat SV")
    assert(sheet.getRow(0).getCell(6).getStringCellValue == "Koe SV")
    assert(sheet.getRow(0).getCell(7).getStringCellValue == "Voi suorittaa kaksoistutkinnon? SV")
    assert(sheet.getRow(0).getCell(8).getStringCellValue == "Voi suorittaa tutkinnon urheilijana? SV")

    // Parent organisaatio row with aloituspaikat sum
    assert(sheet.getRow(1).getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")
    checkAloituspaikatRowValidity(sheet, 1, 13)

    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Oppilaitoksen nimi sv")
    // Hakukohde result row
    assert(sheet.getRow(3).getCell(0).getStringCellValue == "Hakukohteen nimi sv")
    assert(sheet.getRow(3).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000021565")
    assert(sheet.getRow(3).getCell(2).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(3).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(3).getCell(4).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(3).getCell(5).getNumericCellValue == 8)
    assert(sheet.getRow(3).getCell(6).getStringCellValue == "Nej")
    assert(sheet.getRow(3).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(3).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(3).getCell(9) == null)

    // Hakukohde 2 result row
    assert(sheet.getRow(4).getCell(0).getStringCellValue == "Hakukohteen 2 nimi sv")
    assert(sheet.getRow(4).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000031232")
    assert(sheet.getRow(4).getCell(2).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(4).getCell(3).getStringCellValue == "Luonnos SV")
    assert(sheet.getRow(4).getCell(4).getStringCellValue == "Luonnos SV")
    assert(sheet.getRow(4).getCell(5).getNumericCellValue == 5)
    assert(sheet.getRow(4).getCell(6).getStringCellValue == "Ja")
    assert(sheet.getRow(4).getCell(7).getStringCellValue == "Ja")
    assert(sheet.getRow(4).getCell(8).getStringCellValue == "Nej")
    assert(sheet.getRow(4).getCell(9) == null)

    assert(sheet.getPhysicalNumberOfRows == 5)
  }

  it should "create a sheet with two result rows for koulutustoimija" in {
    val koulutustoimijaWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(),
        List(
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohteenNimi =
                Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
              hakukohdeOid = "1.2.246.562.20.00000000000000021565",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("julkaistu"),
              hakukohteenTila = Some("julkaistu"),
              aloituspaikat = Some(8),
              onValintakoe = Some(false),
              voiSuorittaaKaksoistutkinnon = None,
              jarjestaaUrheilijanAmmKoulutusta = Some(false)
            )
          ),
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohteenNimi =
                Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
              hakukohdeOid = "1.2.246.562.20.00000000000000031232",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("tallennettu"),
              hakukohteenTila = Some("tallennettu"),
              aloituspaikat = Some(5),
              onValintakoe = Some(true)
            )
          )
        )
      )
    )

    val wb = ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
      koulutustoimijaWithHakukohteet,
      userLng,
      KOULUTUSTOIMIJARAPORTTI,
      translations,
      koulutuksetToteutuksetHakukohteetParams
    )
    val sheet = wb.getSheetAt(0)
    // Parent organisaatio row with aloituspaikat sum
    assert(sheet.getRow(1).getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")
    checkAloituspaikatRowValidity(sheet, 1, 13)

    // Hakukohde result row
    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Hakukohteen nimi sv")
    assert(sheet.getRow(2).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000021565")
    assert(sheet.getRow(2).getCell(2).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(2).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(2).getCell(4).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(2).getCell(5).getNumericCellValue == 8)
    assert(sheet.getRow(2).getCell(6).getStringCellValue == "Nej")
    assert(sheet.getRow(2).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(2).getCell(8).getStringCellValue == "Nej")
    assert(sheet.getRow(2).getCell(9) == null)

    // Hakukohde 2 result row
    assert(sheet.getRow(3).getCell(0).getStringCellValue == "Hakukohteen 2 nimi sv")
    assert(sheet.getRow(3).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000031232")
    assert(sheet.getRow(3).getCell(2).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(3).getCell(3).getStringCellValue == "Luonnos SV")
    assert(sheet.getRow(3).getCell(4).getStringCellValue == "Luonnos SV")
    assert(sheet.getRow(3).getCell(5).getNumericCellValue == 5)
    assert(sheet.getRow(3).getCell(6).getStringCellValue == "Ja")
    assert(sheet.getRow(3).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(3).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(3).getCell(9) == null)

    assert(sheet.getPhysicalNumberOfRows == 4)
  }

  it should "create an oppilaitosraportti with two oppilaitos and subheadings with koulutustoimija name" in {
    val hierarkiatWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.278170642010",
        Map(En -> "Oppilaitoksen 1 nimi en", Fi -> "Oppilaitoksen 1 nimi fi", Sv -> "Oppilaitoksen 1 nimi sv"),
        List("02"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
        Some(
          Organisaatio(
            "1.2.246.562.10.2781706420000",
            Map(
              En -> "Koulutustoimijan nimi en",
              Fi -> "Koulutustoimijan nimi fi",
              Sv -> "Koulutustoimijan nimi sv"
            ),
            List("01")
          )
        ),
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.2781706420111",
            Map(En -> "Toimipisteen nimi en", Fi -> "Toimipisteen nimi fi", Sv -> "Toimipisteen nimi sv"),
            List("03"),
            List(
              "1.2.246.562.10.00000000001",
              "1.2.246.562.10.2781706420000",
              "1.2.246.562.10.278170642010",
              "1.2.246.562.10.2781706420111"
            ),
            Some(
              Organisaatio(
                "1.2.246.562.10.2781706420000",
                Map(
                  En -> "Koulutustoimijan nimi en",
                  Fi -> "Koulutustoimijan nimi fi",
                  Sv -> "Koulutustoimijan nimi sv"
                ),
                List("01")
              )
            ),
            List(),
            List(
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.2781706420111"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohteenNimi = Map(
                    En -> "Toimipisteen hakukohteen nimi en",
                    Fi -> "Toimipisteen hakukohteen nimi fi",
                    Sv -> "Toimipisteen hakukohteen nimi sv"
                  ),
                  hakukohdeOid = "1.2.246.562.20.0000000000000004567",
                  koulutuksenTila = Some("arkistoitu"),
                  toteutuksenTila = Some("julkaistu"),
                  hakukohteenTila = Some("julkaistu"),
                  aloituspaikat = Some(3),
                  onValintakoe = Some(false),
                  jarjestaaUrheilijanAmmKoulutusta = Some(false)
                )
              )
            )
          ),
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.2781706420132",
            Map(En -> "Toimipisteen 2 nimi en", Fi -> "Toimipisteen 2 nimi fi", Sv -> "Toimipisteen 2 nimi sv"),
            List("03"),
            List(
              "1.2.246.562.10.00000000001",
              "1.2.246.562.10.2781706420000",
              "1.2.246.562.10.278170642010",
              "1.2.246.562.10.2781706420132"
            ),
            Some(
              Organisaatio(
                "1.2.246.562.10.2781706420000",
                Map(
                  En -> "Koulutustoimijan nimi en",
                  Fi -> "Koulutustoimijan nimi fi",
                  Sv -> "Koulutustoimijan nimi sv"
                ),
                List("01")
              )
            ),
            List(
              OrganisaatioHierarkiaWithHakukohteet(
                "1.2.246.562.10.278170642013211",
                Map(
                  En -> "Toimipisteen 2 alitoimipisteen nimi en",
                  Fi -> "Toimipisteen 2 alitoimipisteen nimi fi",
                  Sv -> "Toimipisteen 2 alitoimipisteen nimi sv"
                ),
                List("03"),
                List(
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.2781706420000",
                  "1.2.246.562.10.278170642010",
                  "1.2.246.562.10.2781706420132",
                  "1.2.246.562.10.278170642013211"
                ),
                Some(
                  Organisaatio(
                    "1.2.246.562.10.2781706420000",
                    Map(
                      En -> "Koulutustoimijan nimi en",
                      Fi -> "Koulutustoimijan nimi fi",
                      Sv -> "Koulutustoimijan nimi sv"
                    ),
                    List("01")
                  )
                ),
                List(),
                List(
                  OrganisaationKoulutusToteutusHakukohde(
                    organisaatio_oid = Some("1.2.246.562.10.278170642013211"),
                    koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                      hakukohteenNimi = Map(
                        En -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi en",
                        Fi -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi fi",
                        Sv -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi sv"
                      ),
                      hakukohdeOid = "1.2.246.562.20.000000000000000456811",
                      koulutuksenTila = Some("arkistoitu"),
                      toteutuksenTila = Some("julkaistu"),
                      hakukohteenTila = Some("julkaistu"),
                      aloituspaikat = Some(2),
                      onValintakoe = Some(false),
                      voiSuorittaaKaksoistutkinnon = Some(true)
                    )
                  )
                )
              )
            ),
            List(
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.2781706420132"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohteenNimi = Map(
                    En -> "Toimipisteen 2 hakukohteen nimi en",
                    Fi -> "Toimipisteen 2 hakukohteen nimi fi",
                    Sv -> "Toimipisteen 2 hakukohteen nimi sv"
                  ),
                  hakukohdeOid = "1.2.246.562.20.0000000000000004568",
                  koulutuksenTila = Some("arkistoitu"),
                  toteutuksenTila = Some("julkaistu"),
                  hakukohteenTila = Some("julkaistu"),
                  aloituspaikat = Some(4),
                  onValintakoe = Some(true)
                )
              )
            )
          )
        ),
        List(
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.278170642010"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohteenNimi =
                Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
              hakukohdeOid = "1.2.246.562.20.00000000000000021565",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("julkaistu"),
              hakukohteenTila = Some("julkaistu"),
              aloituspaikat = Some(8),
              onValintakoe = Some(false)
            )
          ),
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.278170642010"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohteenNimi =
                Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
              hakukohdeOid = "1.2.246.562.20.00000000000000031232",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("tallennettu"),
              hakukohteenTila = Some("tallennettu"),
              aloituspaikat = Some(5),
              onValintakoe = Some(true),
              voiSuorittaaKaksoistutkinnon = Some(true),
              jarjestaaUrheilijanAmmKoulutusta = Some(false)
            )
          )
        )
      ),
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.278170642012",
        Map(En -> "Oppilaitoksen 2 nimi en", Fi -> "Oppilaitoksen 2 nimi fi", Sv -> "Oppilaitoksen 2 nimi sv"),
        List("02"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642012"),
        Some(
          Organisaatio(
            "1.2.246.562.10.2781706420002",
            Map(
              En -> "Koulutustoimijan 2 nimi en",
              Fi -> "Koulutustoimijan 2 nimi fi",
              Sv -> "Koulutustoimijan 2 nimi sv"
            ),
            List("01")
          )
        ),
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.27817064201113",
            Map(En -> "Toimipisteen 3 nimi en", Fi -> "Toimipisteen 3 nimi fi", Sv -> "Toimipisteen 3 nimi sv"),
            List("03"),
            List(
              "1.2.246.562.10.00000000001",
              "1.2.246.562.10.2781706420002",
              "1.2.246.562.10.278170642012",
              "1.2.246.562.10.27817064201113"
            ),
            None,
            List(),
            List(
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.27817064201113"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohteenNimi = Map(
                    En -> "Toimipisteen 3 hakukohteen nimi en",
                    Fi -> "Toimipisteen 3 hakukohteen nimi fi",
                    Sv -> "Toimipisteen 3 hakukohteen nimi sv"
                  ),
                  hakukohdeOid = "1.2.246.562.20.00000000000000045673",
                  koulutuksenTila = Some("arkistoitu"),
                  toteutuksenTila = Some("julkaistu"),
                  hakukohteenTila = Some("julkaistu"),
                  aloituspaikat = Some(3),
                  onValintakoe = Some(false)
                )
              )
            )
          )
        ),
        List(
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.278170642012"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohteenNimi = Map(
                En -> "Oppilaitoksen 2 hakukohteen nimi en",
                Fi -> "Oppilaitoksen 2 hakukohteen nimi fi",
                Sv -> "Oppilaitoksen 2 hakukohteen nimi sv"
              ),
              hakukohdeOid = "1.2.246.562.20.000000000000000215651",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("julkaistu"),
              hakukohteenTila = Some("julkaistu"),
              aloituspaikat = Some(8),
              onValintakoe = Some(false),
              jarjestaaUrheilijanAmmKoulutusta = Some(true)
            )
          )
        )
      )
    )

    val wb = ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
      hierarkiatWithHakukohteet,
      userLng,
      OPPILAITOSRAPORTTI,
      translations,
      koulutuksetToteutuksetHakukohteetParams
    )
    val sheet = wb.getSheetAt(0)
    // Parent organisaatio row with aloituspaikat sum
    assert(sheet.getRow(1).getCell(0).getStringCellValue == "Oppilaitoksen 1 nimi sv")
    checkAloituspaikatRowValidity(sheet, 1, 22)

    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")

    // Oppilaitos 1 hakukohteet
    assert(sheet.getRow(3).getCell(0).getStringCellValue == "Hakukohteen nimi sv")
    assert(sheet.getRow(3).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000021565")
    assert(sheet.getRow(3).getCell(2).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(3).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(3).getCell(4).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(3).getCell(5).getNumericCellValue == 8)
    assert(sheet.getRow(3).getCell(6).getStringCellValue == "Nej")
    assert(sheet.getRow(3).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(3).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(3).getCell(9) == null)

    // Hakukohde 2 result row
    assert(sheet.getRow(4).getCell(0).getStringCellValue == "Hakukohteen 2 nimi sv")
    assert(sheet.getRow(4).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000031232")
    assert(sheet.getRow(4).getCell(2).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(4).getCell(3).getStringCellValue == "Luonnos SV")
    assert(sheet.getRow(4).getCell(4).getStringCellValue == "Luonnos SV")
    assert(sheet.getRow(4).getCell(5).getNumericCellValue == 5)
    assert(sheet.getRow(4).getCell(6).getStringCellValue == "Ja")
    assert(sheet.getRow(4).getCell(7).getStringCellValue == "Ja")
    assert(sheet.getRow(4).getCell(8).getStringCellValue == "Nej")
    assert(sheet.getRow(4).getCell(9) == null)

    // Oppilaitos 1:n toimipiste 1
    assert(sheet.getRow(5).getCell(0).getStringCellValue == "Toimipisteen nimi sv")
    checkAloituspaikatRowValidity(sheet, 5, 3)

    // Toimipisteen hakukohde
    assert(sheet.getRow(6).getCell(0).getStringCellValue == "Toimipisteen hakukohteen nimi sv")
    assert(sheet.getRow(6).getCell(1).getStringCellValue == "1.2.246.562.20.0000000000000004567")
    assert(sheet.getRow(6).getCell(2).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(6).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(6).getCell(4).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(6).getCell(5).getNumericCellValue == 3)
    assert(sheet.getRow(6).getCell(6).getStringCellValue == "Nej")
    assert(sheet.getRow(6).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(6).getCell(8).getStringCellValue == "Nej")
    assert(sheet.getRow(6).getCell(9) == null)

    // Oppilaitos 1:n toimipiste 2
    assert(sheet.getRow(7).getCell(0).getStringCellValue == "Toimipisteen 2 nimi sv")
    checkAloituspaikatRowValidity(sheet, 7, 6)

    // Toimipisteen 2 hakukohde
    assert(sheet.getRow(8).getCell(0).getStringCellValue == "Toimipisteen 2 hakukohteen nimi sv")
    assert(sheet.getRow(8).getCell(1).getStringCellValue == "1.2.246.562.20.0000000000000004568")
    assert(sheet.getRow(8).getCell(2).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(8).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(8).getCell(4).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(8).getCell(5).getNumericCellValue == 4)
    assert(sheet.getRow(8).getCell(6).getStringCellValue == "Ja")
    assert(sheet.getRow(8).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(8).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(8).getCell(9) == null)

    // Oppilaitos 1:n toimipisteen 2 alitoimipiste
    assert(sheet.getRow(9).getCell(0).getStringCellValue == "Toimipisteen 2 alitoimipisteen nimi sv")
    checkAloituspaikatRowValidity(sheet, 9, 2)

    // Toimipisteen 2 alitoimipiste hakukohde
    assert(sheet.getRow(10).getCell(0).getStringCellValue == "Toimipisteen 2 alitoimipisteen hakukohteen nimi sv")
    assert(sheet.getRow(10).getCell(1).getStringCellValue == "1.2.246.562.20.000000000000000456811")
    assert(sheet.getRow(10).getCell(2).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(10).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(10).getCell(4).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(10).getCell(5).getNumericCellValue == 2)
    assert(sheet.getRow(10).getCell(6).getStringCellValue == "Nej")
    assert(sheet.getRow(10).getCell(7).getStringCellValue == "Ja")
    assert(sheet.getRow(10).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(10).getCell(9) == null)

    // Oppilaitos 2
    assert(sheet.getRow(11).getCell(0).getStringCellValue == "Oppilaitoksen 2 nimi sv")
    checkAloituspaikatRowValidity(sheet, 11, 11)

    assert(sheet.getRow(12).getCell(0).getStringCellValue == "Koulutustoimijan 2 nimi sv")

    // Oppilaitos 2 hakukohde
    assert(sheet.getRow(13).getCell(0).getStringCellValue == "Oppilaitoksen 2 hakukohteen nimi sv")
    assert(sheet.getRow(13).getCell(1).getStringCellValue == "1.2.246.562.20.000000000000000215651")
    assert(sheet.getRow(13).getCell(2).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(13).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(13).getCell(4).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(13).getCell(5).getNumericCellValue == 8)
    assert(sheet.getRow(13).getCell(6).getStringCellValue == "Nej")
    assert(sheet.getRow(13).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(13).getCell(8).getStringCellValue == "Ja")
    assert(sheet.getRow(13).getCell(9) == null)

    // Oppilaitos 2 toimipiste 3
    assert(sheet.getRow(14).getCell(0).getStringCellValue == "Toimipisteen 3 nimi sv")
    checkAloituspaikatRowValidity(sheet, 14, 3)

    // Oppilaitos 2 hakukohde
    assert(sheet.getRow(15).getCell(0).getStringCellValue == "Toimipisteen 3 hakukohteen nimi sv")
    assert(sheet.getRow(15).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000045673")
    assert(sheet.getRow(15).getCell(2).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(15).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(15).getCell(4).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(15).getCell(5).getNumericCellValue == 3)
    assert(sheet.getRow(15).getCell(6).getStringCellValue == "Nej")
    assert(sheet.getRow(15).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(15).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(15).getCell(9) == null)

    assert(sheet.getPhysicalNumberOfRows == 16)
  }

  it should "create a toimipisteraportti with three toimipiste and koulutustoimija name as a subheading" in {
    val hierarkiatWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420111",
        Map(En -> "Toimipisteen nimi en", Fi -> "Toimipisteen nimi fi", Sv -> "Toimipisteen nimi sv"),
        List("03"),
        List(
          "1.2.246.562.10.00000000001",
          "1.2.246.562.10.2781706420000",
          "1.2.246.562.10.278170642010",
          "1.2.246.562.10.2781706420111"
        ),
        Some(
          Organisaatio(
            "1.2.246.562.10.2781706420000",
            Map(
              En -> "Koulutustoimijan nimi en",
              Fi -> "Koulutustoimijan nimi fi",
              Sv -> "Koulutustoimijan nimi sv"
            ),
            List("01")
          )
        ),
        List(),
        List(
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.2781706420111"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohteenNimi = Map(
                En -> "Toimipisteen hakukohteen nimi en",
                Fi -> "Toimipisteen hakukohteen nimi fi",
                Sv -> "Toimipisteen hakukohteen nimi sv"
              ),
              hakukohdeOid = "1.2.246.562.20.0000000000000004567",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("julkaistu"),
              hakukohteenTila = Some("arkistoitu"),
              aloituspaikat = Some(3),
              onValintakoe = Some(false)
            )
          )
        )
      ),
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420132",
        Map(En -> "Toimipisteen 2 nimi en", Fi -> "Toimipisteen 2 nimi fi", Sv -> "Toimipisteen 2 nimi sv"),
        List("03"),
        List(
          "1.2.246.562.10.00000000001",
          "1.2.246.562.10.2781706420000",
          "1.2.246.562.10.278170642010",
          "1.2.246.562.10.2781706420132"
        ),
        Some(
          Organisaatio(
            "1.2.246.562.10.2781706420000",
            Map(
              En -> "Koulutustoimijan 2 nimi en",
              Fi -> "Koulutustoimijan 2 nimi fi",
              Sv -> "Koulutustoimijan 2 nimi sv"
            ),
            List("01")
          )
        ),
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.278170642013211",
            Map(
              En -> "Toimipisteen 2 alitoimipisteen nimi en",
              Fi -> "Toimipisteen 2 alitoimipisteen nimi fi",
              Sv -> "Toimipisteen 2 alitoimipisteen nimi sv"
            ),
            List("03"),
            List(
              "1.2.246.562.10.00000000001",
              "1.2.246.562.10.2781706420000",
              "1.2.246.562.10.278170642010",
              "1.2.246.562.10.2781706420132",
              "1.2.246.562.10.278170642013211"
            ),
            Some(
              Organisaatio(
                "1.2.246.562.10.2781706420000",
                Map(
                  En -> "Koulutustoimijan 2 nimi en",
                  Fi -> "Koulutustoimijan 2 nimi fi",
                  Sv -> "Koulutustoimijan 2 nimi sv"
                ),
                List("01")
              )
            ),
            List(),
            List(
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.278170642013211"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohteenNimi = Map(
                    En -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi en",
                    Fi -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi fi",
                    Sv -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi sv"
                  ),
                  hakukohdeOid = "1.2.246.562.20.000000000000000456811",
                  koulutuksenTila = Some("julkaistu"),
                  toteutuksenTila = Some("julkaistu"),
                  hakukohteenTila = Some("arkistoitu"),
                  aloituspaikat = Some(2),
                  onValintakoe = Some(false)
                )
              )
            )
          )
        ),
        List(
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.2781706420132"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohteenNimi = Map(
                En -> "Toimipisteen 2 hakukohteen nimi en",
                Fi -> "Toimipisteen 2 hakukohteen nimi fi",
                Sv -> "Toimipisteen 2 hakukohteen nimi sv"
              ),
              hakukohdeOid = "1.2.246.562.20.0000000000000004568",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("julkaistu"),
              hakukohteenTila = Some("arkistoitu"),
              aloituspaikat = Some(4),
              onValintakoe = Some(true)
            )
          )
        )
      )
    )

    val wb = ExcelWriter.writeKoulutuksetToteutuksetHakukohteetRaportti(
      hierarkiatWithHakukohteet,
      userLng,
      TOIMIPISTERAPORTTI,
      translations,
      koulutuksetToteutuksetHakukohteetParams
    )
    val sheet = wb.getSheetAt(0)
    // Parent organisaatio row with aloituspaikat sum
    assert(sheet.getRow(1).getCell(0).getStringCellValue == "Toimipisteen nimi sv")
    checkAloituspaikatRowValidity(sheet, 1, 3)
    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")

    assert(sheet.getRow(4).getCell(0).getStringCellValue == "Toimipisteen 2 nimi sv")
    checkAloituspaikatRowValidity(sheet, 4, 6)
    assert(sheet.getRow(5).getCell(0).getStringCellValue == "Koulutustoimijan 2 nimi sv")

    assert(sheet.getRow(7).getCell(0).getStringCellValue == "Toimipisteen 2 alitoimipisteen nimi sv")
    checkAloituspaikatRowValidity(sheet, 7, 2)
    assert(sheet.getRow(8).getCell(0).getStringCellValue == "Koulutustoimijan 2 nimi sv")

    assert(sheet.getPhysicalNumberOfRows == 10)
  }

  "createKoulutuksetToteutuksetHakukohteetResultRows" should "create a sheet with the column title row and two result rows for oppilaitos under koulutustoimija" in {
    val hierarkiatWithHakukohteet = List(
      OrganisaatioHierarkiaWithHakukohteet(
        "1.2.246.562.10.2781706420000",
        Map(
          En -> "Koulutustoimijan nimi en",
          Fi -> "Koulutustoimijan nimi fi",
          Sv -> "Koulutustoimijan nimi sv"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        None,
        List(
          OrganisaatioHierarkiaWithHakukohteet(
            "1.2.246.562.10.278170642010",
            Map(En -> "Oppilaitoksen nimi en", Fi -> "Oppilaitoksen nimi fi", Sv -> "Oppilaitoksen nimi sv"),
            List("02"),
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000", "1.2.246.562.10.278170642010"),
            None,
            List(
              OrganisaatioHierarkiaWithHakukohteet(
                "1.2.246.562.10.2781706420111",
                Map(En -> "Toimipisteen nimi en", Fi -> "Toimipisteen nimi fi", Sv -> "Toimipisteen nimi sv"),
                List("03"),
                List(
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.2781706420000",
                  "1.2.246.562.10.278170642010",
                  "1.2.246.562.10.2781706420111"
                ),
                None,
                List(),
                List(
                  OrganisaationKoulutusToteutusHakukohde(
                    organisaatio_oid = Some("1.2.246.562.10.2781706420111"),
                    koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                      hakukohteenNimi = Map(
                        En -> "Toimipisteen hakukohteen nimi en",
                        Fi -> "Toimipisteen hakukohteen nimi fi",
                        Sv -> "Toimipisteen hakukohteen nimi sv"
                      ),
                      hakukohdeOid = "1.2.246.562.20.0000000000000004567",
                      koulutuksenTila = Some("julkaistu"),
                      toteutuksenTila = Some("julkaistu"),
                      hakukohteenTila = Some("arkistoitu"),
                      aloituspaikat = Some(3),
                      onValintakoe = Some(false)
                    )
                  )
                )
              ),
              OrganisaatioHierarkiaWithHakukohteet(
                "1.2.246.562.10.2781706420132",
                Map(En -> "Toimipisteen 2 nimi en", Fi -> "Toimipisteen 2 nimi fi", Sv -> "Toimipisteen 2 nimi sv"),
                List("03"),
                List(
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.2781706420000",
                  "1.2.246.562.10.278170642010",
                  "1.2.246.562.10.2781706420132"
                ),
                None,
                List(
                  OrganisaatioHierarkiaWithHakukohteet(
                    "1.2.246.562.10.278170642013211",
                    Map(
                      En -> "Toimipisteen 2 alitoimipisteen nimi en",
                      Fi -> "Toimipisteen 2 alitoimipisteen nimi fi",
                      Sv -> "Toimipisteen 2 alitoimipisteen nimi sv"
                    ),
                    List("03"),
                    List(
                      "1.2.246.562.10.00000000001",
                      "1.2.246.562.10.2781706420000",
                      "1.2.246.562.10.278170642010",
                      "1.2.246.562.10.2781706420132",
                      "1.2.246.562.10.278170642013211"
                    ),
                    None,
                    List(),
                    List(
                      OrganisaationKoulutusToteutusHakukohde(
                        organisaatio_oid = Some("1.2.246.562.10.278170642013211"),
                        koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                          hakukohteenNimi = Map(
                            En -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi en",
                            Fi -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi fi",
                            Sv -> "Toimipisteen 2 alitoimipisteen hakukohteen nimi sv"
                          ),
                          hakukohdeOid = "1.2.246.562.20.000000000000000456811",
                          koulutuksenTila = Some("julkaistu"),
                          toteutuksenTila = Some("julkaistu"),
                          hakukohteenTila = Some("arkistoitu"),
                          aloituspaikat = Some(2),
                          onValintakoe = Some(false)
                        )
                      )
                    )
                  )
                ),
                List(
                  OrganisaationKoulutusToteutusHakukohde(
                    organisaatio_oid = Some("1.2.246.562.10.2781706420132"),
                    koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                      hakukohteenNimi = Map(
                        En -> "Toimipisteen 2 hakukohteen nimi en",
                        Fi -> "Toimipisteen 2 hakukohteen nimi fi",
                        Sv -> "Toimipisteen 2 hakukohteen nimi sv"
                      ),
                      hakukohdeOid = "1.2.246.562.20.0000000000000004568",
                      koulutuksenTila = Some("julkaistu"),
                      toteutuksenTila = Some("julkaistu"),
                      hakukohteenTila = Some("arkistoitu"),
                      aloituspaikat = Some(4),
                      onValintakoe = Some(true)
                    )
                  )
                )
              )
            ),
            List(
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.278170642010"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohteenNimi =
                    Map(En -> "Hakukohteen nimi en", Fi -> "Hakukohteen nimi fi", Sv -> "Hakukohteen nimi sv"),
                  hakukohdeOid = "1.2.246.562.20.00000000000000021565",
                  koulutuksenTila = Some("julkaistu"),
                  toteutuksenTila = Some("julkaistu"),
                  hakukohteenTila = Some("tallennettu"),
                  aloituspaikat = Some(8),
                  onValintakoe = Some(false),
                  voiSuorittaaKaksoistutkinnon = Some(true),
                  jarjestaaUrheilijanAmmKoulutusta = Some(true)
                )
              ),
              OrganisaationKoulutusToteutusHakukohde(
                organisaatio_oid = Some("1.2.246.562.10.278170642010"),
                koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
                  hakukohteenNimi =
                    Map(En -> "Hakukohteen 2 nimi en", Fi -> "Hakukohteen 2 nimi fi", Sv -> "Hakukohteen 2 nimi sv"),
                  hakukohdeOid = "1.2.246.562.20.00000000000000031232",
                  koulutuksenTila = Some("julkaistu"),
                  toteutuksenTila = Some("tallennettu"),
                  hakukohteenTila = Some("tallennettu"),
                  aloituspaikat = Some(5),
                  onValintakoe = Some(true)
                )
              )
            )
          )
        ),
        List(
          OrganisaationKoulutusToteutusHakukohde(
            organisaatio_oid = Some("1.2.246.562.10.2781706420000"),
            koulutusToteutusHakukohde = KoulutusToteutusHakukohdeResult(
              hakukohteenNimi = Map(
                En -> "Koulutustoimijan hakukohteen nimi en",
                Fi -> "Koulutustoimijan hakukohteen nimi fi",
                Sv -> "Koulutustoimijan hakukohteen nimi sv"
              ),
              hakukohdeOid = "1.2.246.562.20.00000000000000021542013",
              koulutuksenTila = Some("julkaistu"),
              toteutuksenTila = Some("julkaistu"),
              hakukohteenTila = Some("julkaistu"),
              aloituspaikat = Some(4),
              onValintakoe = Some(false)
            )
          )
        )
      )
    )

    val workbook: XSSFWorkbook          = new XSSFWorkbook()
    val sheet: XSSFSheet                = workbook.createSheet()
    val headingCellStyle: XSSFCellStyle = workbook.createCellStyle()
    val cellStyle: XSSFCellStyle        = workbook.createCellStyle()
    val font1                           = workbook.createFont()

    val titles = classOf[KoulutusToteutusHakukohdeResult].getDeclaredFields
      .map(_.getName)
      .toList

    val raporttiColumnTitlesWithIndex = titles.zipWithIndex

    val wb: Unit =
      ExcelWriter.createKoulutuksetToteutuksetHakukohteetResultRows(
        workbook,
        sheet,
        hierarkiatWithHakukohteet,
        0,
        headingCellStyle,
        cellStyle,
        cellStyle,
        font1,
        font1,
        userLng,
        raporttiColumnTitlesWithIndex,
        KOULUTUSTOIMIJARAPORTTI,
        translations
      )

    // Parent organisaatio row with aloituspaikat sum
    assert(sheet.getRow(0).getCell(0).getStringCellValue == "Koulutustoimijan nimi sv")
    assert(sheet.getRow(0).getCell(0).getCellStyle.getAlignment == HorizontalAlignment.LEFT)
    assert(sheet.getRow(0).getCell(0).getCellStyle.getIndention == 0.toShort)
    checkAloituspaikatRowValidity(sheet, 0, 26)

    assert(sheet.getRow(1).getCell(0).getStringCellValue == "Koulutustoimijan hakukohteen nimi sv")
    assert(sheet.getRow(1).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000021542013")
    assert(sheet.getRow(1).getCell(2).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(1).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(1).getCell(4).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(1).getCell(5).getNumericCellValue == 4)
    assert(sheet.getRow(1).getCell(6).getStringCellValue == "Nej")
    assert(sheet.getRow(1).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(9) == null)

    // Oppilaitos row
    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Oppilaitoksen nimi sv")
    assert(sheet.getRow(2).getCell(0).getCellStyle.getAlignment == HorizontalAlignment.LEFT)
    assert(sheet.getRow(2).getCell(0).getCellStyle.getIndention == 1.toShort)
    checkAloituspaikatRowValidity(sheet, 2, 22)

    // Hakukohde result row
    assert(sheet.getRow(3).getCell(0).getStringCellValue == "Hakukohteen nimi sv")
    assert(sheet.getRow(3).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000021565")
    assert(sheet.getRow(3).getCell(2).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(3).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(3).getCell(4).getStringCellValue == "Luonnos SV")
    assert(sheet.getRow(3).getCell(5).getNumericCellValue == 8)
    assert(sheet.getRow(3).getCell(6).getStringCellValue == "Nej")
    assert(sheet.getRow(3).getCell(7).getStringCellValue == "Ja")
    assert(sheet.getRow(3).getCell(8).getStringCellValue == "Ja")
    assert(sheet.getRow(3).getCell(9) == null)

    // Hakukohde 2 result row
    assert(sheet.getRow(4).getCell(0).getStringCellValue == "Hakukohteen 2 nimi sv")
    assert(sheet.getRow(4).getCell(1).getStringCellValue == "1.2.246.562.20.00000000000000031232")
    assert(sheet.getRow(4).getCell(2).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(4).getCell(3).getStringCellValue == "Luonnos SV")
    assert(sheet.getRow(4).getCell(4).getStringCellValue == "Luonnos SV")
    assert(sheet.getRow(4).getCell(5).getNumericCellValue == 5)
    assert(sheet.getRow(4).getCell(6).getStringCellValue == "Ja")
    assert(sheet.getRow(4).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(4).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(4).getCell(9) == null)

    // Toimipisteen row
    assert(sheet.getRow(5).getCell(0).getStringCellValue == "Toimipisteen nimi sv")
    assert(sheet.getRow(5).getCell(0).getCellStyle.getAlignment == HorizontalAlignment.LEFT)
    assert(sheet.getRow(5).getCell(0).getCellStyle.getIndention == 2.toShort)
    checkAloituspaikatRowValidity(sheet, 5, 3)

    // Toimipisteen result row
    assert(sheet.getRow(6).getCell(0).getStringCellValue == "Toimipisteen hakukohteen nimi sv")
    assert(sheet.getRow(6).getCell(1).getStringCellValue == "1.2.246.562.20.0000000000000004567")
    assert(sheet.getRow(6).getCell(2).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(6).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(6).getCell(4).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(6).getCell(5).getNumericCellValue == 3)
    assert(sheet.getRow(6).getCell(6).getStringCellValue == "Nej")
    assert(sheet.getRow(6).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(6).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(6).getCell(9) == null)

    // Toimipiste 2 row
    assert(sheet.getRow(7).getCell(0).getStringCellValue == "Toimipisteen 2 nimi sv")
    assert(sheet.getRow(7).getCell(0).getCellStyle.getAlignment == HorizontalAlignment.LEFT)
    assert(sheet.getRow(7).getCell(0).getCellStyle.getIndention == 2.toShort)
    checkAloituspaikatRowValidity(sheet, 7, 6)

    // Toimipisteen 2 result row
    assert(sheet.getRow(8).getCell(0).getStringCellValue == "Toimipisteen 2 hakukohteen nimi sv")
    assert(sheet.getRow(8).getCell(1).getStringCellValue == "1.2.246.562.20.0000000000000004568")
    assert(sheet.getRow(8).getCell(2).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(8).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(8).getCell(4).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(8).getCell(5).getNumericCellValue == 4)
    assert(sheet.getRow(8).getCell(6).getStringCellValue == "Ja")
    assert(sheet.getRow(8).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(8).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(8).getCell(9) == null)

    // Toimipisteen 2 alitoimipiste row
    assert(sheet.getRow(9).getCell(0).getStringCellValue == "Toimipisteen 2 alitoimipisteen nimi sv")
    assert(sheet.getRow(9).getCell(0).getCellStyle.getAlignment == HorizontalAlignment.LEFT)
    assert(sheet.getRow(9).getCell(0).getCellStyle.getIndention == 2.toShort)
    checkAloituspaikatRowValidity(sheet, 9, 2)

    // Toimipisteen 2 alitoimipisteen result row
    assert(sheet.getRow(10).getCell(0).getStringCellValue == "Toimipisteen 2 alitoimipisteen hakukohteen nimi sv")
    assert(sheet.getRow(10).getCell(1).getStringCellValue == "1.2.246.562.20.000000000000000456811")
    assert(sheet.getRow(10).getCell(2).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(10).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(10).getCell(4).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(10).getCell(5).getNumericCellValue == 2)
    assert(sheet.getRow(10).getCell(6).getStringCellValue == "Nej")
    assert(sheet.getRow(10).getCell(7).getStringCellValue == "-")
    assert(sheet.getRow(10).getCell(8).getStringCellValue == "-")
    assert(sheet.getRow(10).getCell(9) == null)

    assert(sheet.getPhysicalNumberOfRows == 11)
  }

  val korkeakouluKoulutuksetToteutuksetHakukohteet: Vector[KorkeakouluKoulutusToteutusHakukohdeResult] = Vector(
    KorkeakouluKoulutusToteutusHakukohdeResult(
      Map(
        En -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
        Fi -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
        Sv -> "Aalto-universitetet, Högskolan för konst design och arkitektur"
      ),
      Map(
        En -> "Screenwriting - Film and Television, Master of Arts (Art and Design) (2 yrs)",
        Fi -> "Elokuva- ja tv-käsikirjoitus - Elokuvataide, taiteen maisteri (2 v)",
        Sv -> "Film- och tv-manuskript - Filmkonst, konstmagister (2 år)"
      ),
      "1.2.246.562.13.00000000000000002677",
      Some("julkaistu"),
      Some("koulutus_309902#7"),
      Some("ARTS20502"),
      Some(1),
      Map(En -> "120 ECTS credits", Fi -> "120 opintopistettä", Sv -> "120 studiepoäng"),
      Map(
        En -> "Screenwriting - Film and Television, Master of Arts (Art and Design) (2 yrs) -toteutus",
        Fi -> "Elokuva- ja tv-käsikirjoitus - Elokuvataide, taiteen maisteri (2 v) -toteutus",
        Sv -> "Film- och tv-manuskript - Filmkonst, konstmagister (2 år) -toteutus"
      ),
      "1.2.246.562.17.00000000000000007967",
      Some("arkistoitu"),
      None,
      Map(En -> "Autumn 2023", Fi -> "Syksy 2023", Sv -> "Höst 2023"),
      Map(
        En -> "Screenwriting, Master of Arts (2 yrs)",
        Fi -> "Elokuva- ja tv-käsikirjoitus, taiteen maisteri (2 v) ",
        Sv -> "Film- och tv-manuskript, konstmagister (2 år)"
      ),
      "1.2.246.562.20.00000000000000017822",
      Some("tallennettu"),
      Some("ARTS20503"),
      Map(
        En -> "Aalto University's Master's Admission 2023",
        Fi -> "Aalto-yliopiston maisterihaku 2023",
        Sv -> "Aalto-universitetets magisteransökan 2023"
      ),
      "1.2.246.562.29.00000000000000015722",
      Some(
        Hakuaika(
          alkaa = Some(LocalDate.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse("2022-12-01T09:00"))),
          paattyy = Some(LocalDate.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse("2023-01-02T15:00")))
        )
      ),
      Map(
        En -> "Separate application",
        Fi -> "Erillishaku",
        Sv -> "Separata antagningar"
      ),
      Some(15),
      Some(10),
      Map(
        En -> "Master's Admissions (Aalto University, field of Art and Design) 2023",
        Fi -> "Maisterivalinta (Aalto-yliopisto, ARTS) 2023",
        Sv -> "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
      )
    ),
    KorkeakouluKoulutusToteutusHakukohdeResult(
      Map(
        En -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
        Fi -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
        Sv -> "Aalto-universitetet, Högskolan för konst design och arkitektur"
      ),
      Map(
        En -> "Animation, Master of Arts (Art and Design) (2 yrs) EN",
        Fi -> "Animation, Master of Arts (Art and Design) (2 yrs)",
        Sv -> "Animation, Master of Arts (Art and Design) (2 yrs) SV"
      ),
      "1.2.246.562.13.00000000000000002880",
      Some("julkaistu"),
      Some("koulutus_722101#12"),
      None,
      Some(2),
      Map(En -> "120 ECTS credits", Fi -> "120 opintopistettä", Sv -> "120 studiepoäng"),
      Map(
        En -> "Animation, Master of Arts (Art and Design) (2 yrs) -toteutus EN",
        Fi -> "Animation, Master of Arts (Art and Design) (2 yrs) -toteutus",
        Sv -> "Animation, Master of Arts (Art and Design) (2 yrs) -toteutus SV"
      ),
      "1.2.246.562.17.00000000000000008396",
      Some("julkaistu"),
      None,
      Map(),
      Map(
        En -> "Animation, Master of Arts (2 yrs) EN",
        Fi -> "Animation, Master of Arts (2 yrs)",
        Sv -> "Animation, Master of Arts (2 yrs) SV"
      ),
      "1.2.246.562.20.00000000000000017880",
      Some("arkistoitu"),
      None,
      Map(
        En -> "Aalto University's Master's Admission 2023",
        Fi -> "Aalto-yliopiston maisterihaku 2023",
        Sv -> "Aalto-universitetets magisteransökan 2023"
      ),
      "1.2.246.562.29.00000000000000015722",
      Some(
        Hakuaika(
          Some(LocalDate.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse("2022-12-01T09:00"))),
          Some(LocalDate.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse("2023-01-02T15:00")))
        )
      ),
      Map(En -> "Separate application", Fi -> "Erillishaku", Sv -> "Separata antagningar"),
      Some(8),
      None,
      Map(
        En -> "Master's Admissions (Aalto University, field of Art and Design) 2023",
        Fi -> "Maisterivalinta (Aalto-yliopisto, ARTS) 2023",
        Sv -> "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
      )
    ),
    KorkeakouluKoulutusToteutusHakukohdeResult(
      Map(
        En -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
        Fi -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
        Sv -> "Aalto-universitetet, Högskolan för konst design och arkitektur"
      ),
      Map(
        En -> "Architecture - Architecture, Landscape architecture and Interior Architecture, Architect, Master of Science (2 yrs)",
        Fi -> "Arkkitehtuuri - Arkkitehtuuri, maisema-arkkitehtuuri ja sisustusarkkitehtuuri, Arkkitehti (2 v)",
        Sv -> "Arkitektur - Arkitektur, landskapsarkitektur och inredningsarkitektur, Arkitekt (2 år)"
      ),
      "1.2.246.562.13.00000000000000002675",
      Some("julkaistu"),
      Some("koulutus_754101#12"),
      Some("ARTS20100"),
      Some(3),
      Map(En -> "120 ECTS credits", Fi -> "120 opintopistettä", Sv -> "120 studiepoäng"),
      Map(
        En ->
          "Arkkitehtuuri - Arkkitehtuuri, maisema-arkkitehtuuri ja sisustusarkkitehtuuri, Arkkitehti (2 v)",
        Fi -> "Architecture - Architecture, Landscape architecture and Interior Architecture, Architect, Master of Science (2 yrs)",
        Sv -> "Arkitektur - Arkitektur, landskapsarkitektur och inredningsarkitektur, Arkitekt (2 år)"
      ),
      "1.2.246.562.17.00000000000000007965",
      Some("julkaistu"),
      None,
      None,
      Map(
        En -> "Architecture, Architect, Master of Science (2 yrs)",
        Fi -> "Arkkitehtuuri, Arkkitehti (2 v)",
        Sv -> "Arkitektur, Arkitekt (2 år)"
      ),
      "1.2.246.562.20.00000000000000017819",
      Some("arkistoitu"),
      Some("ARTS20100"),
      Map(
        En -> "Aalto University's Master's Admission 2023",
        Fi -> "Aalto-yliopiston maisterihaku 2023",
        Sv -> "Aalto-universitetets magisteransökan 2023"
      ),
      "1.2.246.562.29.00000000000000015722",
      Some(
        Hakuaika(
          Some(LocalDate.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse("2022-12-01T09:00"))),
          Some(LocalDate.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse("2023-01-02T15:00")))
        )
      ),
      Map(En -> "Separate application", Fi -> "Erillishaku", Sv -> "Separata antagningar"),
      Some(12),
      None,
      Map(
        En -> "Master's Admissions (Aalto University, field of Art and Design) 2023",
        Fi -> "Maisterivalinta (Aalto-yliopisto, ARTS) 2023",
        Sv -> "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
      )
    ),
    KorkeakouluKoulutusToteutusHakukohdeResult(
      Map(
        En -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
        Fi -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
        Sv -> "Aalto-universitetet, Högskolan för konst design och arkitektur"
      ),
      Map(
        En -> "Collaborative and Industrial Design - Design, Master of Arts (Art and Design) (2 yrs) EN",
        Fi -> "Collaborative and Industrial Design - Design, Master of Arts (Art and Design) (2 yrs)",
        Sv -> "Collaborative and Industrial Design - Design, Master of Arts (Art and Design) (2 yrs) SV"
      ),
      "1.2.246.562.13.00000000000000002687",
      Some("julkaistu"),
      Some("koulutus_722101#12"),
      Some("ARTS20200"),
      Some(4),
      Map(En -> "120 ECTS credits", Fi -> "120 opintopistettä", Sv -> "120 studiepoäng"),
      Map(
        En -> "Collaborative and Industrial Design - Design, Master of Arts (Art and Design) (2 yrs) -toteutus EN",
        Fi -> "Collaborative and Industrial Design - Design, Master of Arts (Art and Design) (2 yrs) -toteutus",
        Sv ->
          "Collaborative and Industrial Design - Design, Master of Arts (Art and Design) (2 yrs) -toteutus SV"
      ),
      "1.2.246.562.17.00000000000000007977",
      Some("julkaistu"),
      None,
      Some(LocalDate.parse("2023-09-30")),
      Map(
        En -> "Hakukohde Collaborative and Industrial Design, Master of Arts (2 yrs) EN",
        Fi -> "Hakukohde Collaborative and Industrial Design, Master of Arts (2 yrs)",
        Sv -> "Hakukohde Collaborative and Industrial Design, Master of Arts (2 yrs) SV"
      ),
      "1.2.246.562.20.00000000000000017881",
      Some("arkistoitu"),
      Some("ARTS20200"),
      Map(
        En -> "Aalto University's Master's Admission 2023",
        Fi -> "Aalto-yliopiston maisterihaku 2023",
        Sv -> "Aalto-universitetets magisteransökan 2023"
      ),
      "1.2.246.562.29.00000000000000015722",
      Some(
        Hakuaika(
          Some(LocalDate.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse("2022-12-01T09:00"))),
          Some(LocalDate.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse("2023-01-02T15:00")))
        )
      ),
      Map(En -> "Separate application", Fi -> "Erillishaku", Sv -> "Separata antagningar"),
      Some(14),
      Some(10),
      Map(
        En -> "Master's Admissions (Aalto University, field of Art and Design) 2023",
        Fi -> "Maisterivalinta (Aalto-yliopisto, ARTS) 2023",
        Sv -> "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
      )
    )
  )

  val kkKoulutuksetToteutuksetHakukohteetParams: List[(String, String | Boolean | List[String])] =
    List(
      "haku" -> List("1.2.246.562.29.00000000000000015722"),
      "oppilaitos" -> List.empty,
      "toimipiste" -> List("1.2.246.562.10.00000000001"),
    )

  "writeKorkeakouluKoulutuksetToteutuksetHakukohteetRaportti" should "create Korkeakoulujen koulutukset toteutukset ja hakukohteet -raportti koulutuksittain with four result rows" in {
    val wb = ExcelWriter.writeKorkeakouluKoulutuksetToteutuksetHakukohteetRaportti(
      korkeakouluKoulutuksetToteutuksetHakukohteetResults = korkeakouluKoulutuksetToteutuksetHakukohteet,
      asiointikieli = userLng,
      translations = translations,
      tulostustapa = "koulutuksittain",
      parametrit = kkKoulutuksetToteutuksetHakukohteetParams
    )

    val sheet = wb.getSheetAt(0)
    assert(sheet.getRow(0).getCell(0).getStringCellValue == "Oppilaitos ja toimipiste SV")
    assert(sheet.getRow(0).getCell(1).getStringCellValue == "raportti.koulutuksenNimi")
    assert(sheet.getRow(0).getCell(2).getStringCellValue == "raportti.koulutusOid")
    assert(sheet.getRow(0).getCell(3).getStringCellValue == "Kou.tila SV")
    assert(sheet.getRow(0).getCell(4).getStringCellValue == "raportti.koulutuskoodi")
    assert(sheet.getRow(0).getCell(5).getStringCellValue == "raportti.koulutuksenUlkoinenTunniste")
    assert(sheet.getRow(0).getCell(6).getStringCellValue == "raportti.tutkinnonTaso")
    assert(sheet.getRow(0).getCell(7).getStringCellValue == "raportti.opintojenLaajuus")
    assert(sheet.getRow(0).getCell(8).getStringCellValue == "raportti.toteutuksenNimi")
    assert(sheet.getRow(0).getCell(9).getStringCellValue == "raportti.toteutusOid")
    assert(sheet.getRow(0).getCell(10).getStringCellValue == "Tot.tila SV")
    assert(sheet.getRow(0).getCell(11).getStringCellValue == "raportti.toteutuksenUlkoinenTunniste")
    assert(sheet.getRow(0).getCell(12).getStringCellValue == "raportti.koulutuksenAlkamisaika")
    assert(sheet.getRow(0).getCell(13).getStringCellValue == "Hakukohde SV")
    assert(sheet.getRow(0).getCell(14).getStringCellValue == "Hakukohteen oid SV")
    assert(sheet.getRow(0).getCell(15).getStringCellValue == "Hak.tila SV")
    assert(sheet.getRow(0).getCell(16).getStringCellValue == "raportti.hakukohteenUlkoinenTunniste")
    assert(sheet.getRow(0).getCell(17).getStringCellValue == "raportti.haunNimi")
    assert(sheet.getRow(0).getCell(18).getStringCellValue == "raportti.hakuOid")
    assert(sheet.getRow(0).getCell(19).getStringCellValue == "raportti.hakuaika")
    assert(sheet.getRow(0).getCell(20).getStringCellValue == "raportti.hakutapa")
    assert(sheet.getRow(0).getCell(21).getStringCellValue == "raportti.hakukohteenAloituspaikat")
    assert(sheet.getRow(0).getCell(22).getStringCellValue == "raportti.ensikertalaistenAloituspaikat")
    assert(sheet.getRow(0).getCell(23).getStringCellValue == "raportti.valintaperuste")
    assert(sheet.getRow(0).getCell(24) == null)

    assert(
      sheet.getRow(1).getCell(0).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(sheet.getRow(1).getCell(1).getStringCellValue == "Animation, Master of Arts (Art and Design) (2 yrs) SV")
    assert(sheet.getRow(1).getCell(2).getStringCellValue == "1.2.246.562.13.00000000000000002880")
    assert(sheet.getRow(1).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(1).getCell(4).getStringCellValue == "722101")
    assert(sheet.getRow(1).getCell(5).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(6).getStringCellValue == "raportti.tutkinnontaso.ylempi")
    assert(sheet.getRow(1).getCell(7).getStringCellValue == "120 studiepoäng")
    assert(
      sheet
        .getRow(1)
        .getCell(8)
        .getStringCellValue == "Animation, Master of Arts (Art and Design) (2 yrs) -toteutus SV"
    )
    assert(sheet.getRow(1).getCell(9).getStringCellValue == "1.2.246.562.17.00000000000000008396")
    assert(sheet.getRow(1).getCell(10).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(1).getCell(11).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(12).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(13).getStringCellValue == "Animation, Master of Arts (2 yrs) SV")
    assert(sheet.getRow(1).getCell(14).getStringCellValue == "1.2.246.562.20.00000000000000017880")
    assert(sheet.getRow(1).getCell(15).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(1).getCell(16).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(17).getStringCellValue == "Aalto-universitetets magisteransökan 2023")
    assert(sheet.getRow(1).getCell(18).getStringCellValue == "1.2.246.562.29.00000000000000015722")
    assert(sheet.getRow(1).getCell(19).getStringCellValue == "1.12.2022 - 2.1.2023")
    assert(sheet.getRow(1).getCell(20).getStringCellValue == "Separata antagningar")
    assert(sheet.getRow(1).getCell(21).getNumericCellValue == 8)
    assert(sheet.getRow(1).getCell(22).getStringCellValue == "-")
    assert(
      sheet
        .getRow(1)
        .getCell(23)
        .getStringCellValue == "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
    )
    assert(sheet.getRow(1).getCell(24) == null)

    assert(
      sheet.getRow(2).getCell(0).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(
      sheet
        .getRow(2)
        .getCell(1)
        .getStringCellValue == "Arkitektur - Arkitektur, landskapsarkitektur och inredningsarkitektur, Arkitekt (2 år)"
    )
    assert(sheet.getRow(2).getCell(2).getStringCellValue == "1.2.246.562.13.00000000000000002675")
    assert(sheet.getRow(2).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(2).getCell(4).getStringCellValue == "754101")
    assert(sheet.getRow(2).getCell(5).getStringCellValue == "ARTS20100")
    assert(sheet.getRow(2).getCell(6).getStringCellValue == "raportti.tutkinnontaso.alempijaylempi")
    assert(sheet.getRow(2).getCell(7).getStringCellValue == "120 studiepoäng")
    assert(
      sheet
        .getRow(2)
        .getCell(8)
        .getStringCellValue == "Arkitektur - Arkitektur, landskapsarkitektur och inredningsarkitektur, Arkitekt (2 år)"
    )
    assert(sheet.getRow(2).getCell(9).getStringCellValue == "1.2.246.562.17.00000000000000007965")
    assert(sheet.getRow(2).getCell(10).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(2).getCell(11).getStringCellValue == "-")
    assert(sheet.getRow(2).getCell(12).getStringCellValue == "-")
    assert(sheet.getRow(2).getCell(13).getStringCellValue == "Arkitektur, Arkitekt (2 år)")
    assert(sheet.getRow(2).getCell(14).getStringCellValue == "1.2.246.562.20.00000000000000017819")
    assert(sheet.getRow(2).getCell(15).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(2).getCell(16).getStringCellValue == "ARTS20100")
    assert(sheet.getRow(2).getCell(17).getStringCellValue == "Aalto-universitetets magisteransökan 2023")
    assert(sheet.getRow(2).getCell(18).getStringCellValue == "1.2.246.562.29.00000000000000015722")
    assert(sheet.getRow(2).getCell(19).getStringCellValue == "1.12.2022 - 2.1.2023")
    assert(sheet.getRow(2).getCell(20).getStringCellValue == "Separata antagningar")
    assert(sheet.getRow(2).getCell(21).getNumericCellValue == 12)
    assert(sheet.getRow(2).getCell(22).getStringCellValue == "-")
    assert(
      sheet
        .getRow(2)
        .getCell(23)
        .getStringCellValue == "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
    )
    assert(sheet.getRow(2).getCell(24) == null)

    assert(
      sheet.getRow(3).getCell(0).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(
      sheet
        .getRow(3)
        .getCell(1)
        .getStringCellValue == "Collaborative and Industrial Design - Design, Master of Arts (Art and Design) (2 yrs) SV"
    )
    assert(sheet.getRow(3).getCell(2).getStringCellValue == "1.2.246.562.13.00000000000000002687")
    assert(sheet.getRow(3).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(3).getCell(4).getStringCellValue == "722101")
    assert(sheet.getRow(3).getCell(5).getStringCellValue == "ARTS20200")
    assert(sheet.getRow(3).getCell(6).getStringCellValue == "raportti.tutkinnontaso.jatkotutkinto")
    assert(sheet.getRow(3).getCell(7).getStringCellValue == "120 studiepoäng")
    assert(
      sheet
        .getRow(3)
        .getCell(8)
        .getStringCellValue == "Collaborative and Industrial Design - Design, Master of Arts (Art and Design) (2 yrs) -toteutus SV"
    )
    assert(sheet.getRow(3).getCell(9).getStringCellValue == "1.2.246.562.17.00000000000000007977")
    assert(sheet.getRow(3).getCell(10).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(3).getCell(11).getStringCellValue == "-")
    assert(sheet.getRow(3).getCell(12).getStringCellValue == "30.9.2023")
    assert(
      sheet.getRow(3).getCell(13).getStringCellValue == "Hakukohde Collaborative and Industrial Design, Master of Arts (2 yrs) SV"
    )
    assert(sheet.getRow(3).getCell(14).getStringCellValue == "1.2.246.562.20.00000000000000017881")
    assert(sheet.getRow(3).getCell(15).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(3).getCell(16).getStringCellValue == "ARTS20200")
    assert(sheet.getRow(3).getCell(17).getStringCellValue == "Aalto-universitetets magisteransökan 2023")
    assert(sheet.getRow(3).getCell(18).getStringCellValue == "1.2.246.562.29.00000000000000015722")
    assert(sheet.getRow(3).getCell(19).getStringCellValue == "1.12.2022 - 2.1.2023")
    assert(sheet.getRow(3).getCell(20).getStringCellValue == "Separata antagningar")
    assert(sheet.getRow(3).getCell(21).getNumericCellValue == 14)
    assert(sheet.getRow(3).getCell(22).getNumericCellValue == 10)
    assert(
      sheet
        .getRow(3)
        .getCell(23)
        .getStringCellValue == "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
    )
    assert(sheet.getRow(3).getCell(24) == null)

    assert(
      sheet.getRow(4).getCell(0).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(sheet.getRow(4).getCell(1).getStringCellValue == "Film- och tv-manuskript - Filmkonst, konstmagister (2 år)")
    assert(sheet.getRow(4).getCell(2).getStringCellValue == "1.2.246.562.13.00000000000000002677")
    assert(sheet.getRow(4).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(4).getCell(4).getStringCellValue == "309902")
    assert(sheet.getRow(4).getCell(5).getStringCellValue == "ARTS20502")
    assert(sheet.getRow(4).getCell(6).getStringCellValue == "raportti.tutkinnontaso.alempi")
    assert(sheet.getRow(4).getCell(7).getStringCellValue == "120 studiepoäng")
    assert(
      sheet
        .getRow(4)
        .getCell(8)
        .getStringCellValue == "Film- och tv-manuskript - Filmkonst, konstmagister (2 år) -toteutus"
    )
    assert(sheet.getRow(4).getCell(9).getStringCellValue == "1.2.246.562.17.00000000000000007967")
    assert(sheet.getRow(4).getCell(10).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(4).getCell(11).getStringCellValue == "-")
    assert(sheet.getRow(4).getCell(12).getStringCellValue == "Höst 2023")
    assert(sheet.getRow(4).getCell(13).getStringCellValue == "Film- och tv-manuskript, konstmagister (2 år)")
    assert(sheet.getRow(4).getCell(14).getStringCellValue == "1.2.246.562.20.00000000000000017822")
    assert(sheet.getRow(4).getCell(15).getStringCellValue == "Luonnos SV")
    assert(sheet.getRow(4).getCell(16).getStringCellValue == "ARTS20503")
    assert(sheet.getRow(4).getCell(17).getStringCellValue == "Aalto-universitetets magisteransökan 2023")
    assert(sheet.getRow(4).getCell(18).getStringCellValue == "1.2.246.562.29.00000000000000015722")
    assert(sheet.getRow(4).getCell(19).getStringCellValue == "1.12.2022 - 2.1.2023")
    assert(sheet.getRow(4).getCell(20).getStringCellValue == "Separata antagningar")
    assert(sheet.getRow(4).getCell(21).getNumericCellValue == 15)
    assert(sheet.getRow(4).getCell(22).getNumericCellValue == 10)
    assert(
      sheet
        .getRow(4)
        .getCell(23)
        .getStringCellValue == "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
    )
    assert(sheet.getRow(4).getCell(24) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 5)
    assert(wb.getSheetAt(0).getRow(6) == null)
  }

  it should "create Korkeakoulujen koulutukset toteutukset ja hakukohteet -raportti toteutuksittain with four result rows" in {
    val wb = ExcelWriter.writeKorkeakouluKoulutuksetToteutuksetHakukohteetRaportti(
      korkeakouluKoulutuksetToteutuksetHakukohteetResults = korkeakouluKoulutuksetToteutuksetHakukohteet,
      asiointikieli = userLng,
      translations = translations,
      tulostustapa = "toteutuksittain",
      parametrit = kkKoulutuksetToteutuksetHakukohteetParams
    )

    val sheet = wb.getSheetAt(0)
    assert(sheet.getRow(0).getCell(0).getStringCellValue == "Oppilaitos ja toimipiste SV")
    assert(sheet.getRow(0).getCell(1).getStringCellValue == "raportti.toteutuksenNimi")
    assert(sheet.getRow(0).getCell(2).getStringCellValue == "raportti.toteutusOid")
    assert(sheet.getRow(0).getCell(3).getStringCellValue == "Tot.tila SV")
    assert(sheet.getRow(0).getCell(4).getStringCellValue == "raportti.toteutuksenUlkoinenTunniste")
    assert(sheet.getRow(0).getCell(5).getStringCellValue == "raportti.koulutuksenAlkamisaika")
    assert(sheet.getRow(0).getCell(6).getStringCellValue == "Hakukohde SV")
    assert(sheet.getRow(0).getCell(7).getStringCellValue == "Hakukohteen oid SV")
    assert(sheet.getRow(0).getCell(8).getStringCellValue == "Hak.tila SV")
    assert(sheet.getRow(0).getCell(9).getStringCellValue == "raportti.hakukohteenUlkoinenTunniste")
    assert(sheet.getRow(0).getCell(10).getStringCellValue == "raportti.haunNimi")
    assert(sheet.getRow(0).getCell(11).getStringCellValue == "raportti.hakuOid")
    assert(sheet.getRow(0).getCell(12).getStringCellValue == "raportti.hakuaika")
    assert(sheet.getRow(0).getCell(13).getStringCellValue == "raportti.hakutapa")
    assert(sheet.getRow(0).getCell(14).getStringCellValue == "raportti.hakukohteenAloituspaikat")
    assert(sheet.getRow(0).getCell(15).getStringCellValue == "raportti.ensikertalaistenAloituspaikat")
    assert(sheet.getRow(0).getCell(16).getStringCellValue == "raportti.valintaperuste")
    assert(sheet.getRow(0).getCell(17) == null)

    assert(
      sheet.getRow(1).getCell(0).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(
      sheet
        .getRow(1)
        .getCell(1)
        .getStringCellValue == "Animation, Master of Arts (Art and Design) (2 yrs) -toteutus SV"
    )
    assert(sheet.getRow(1).getCell(2).getStringCellValue == "1.2.246.562.17.00000000000000008396")
    assert(sheet.getRow(1).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(1).getCell(4).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(5).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(6).getStringCellValue == "Animation, Master of Arts (2 yrs) SV")
    assert(sheet.getRow(1).getCell(7).getStringCellValue == "1.2.246.562.20.00000000000000017880")
    assert(sheet.getRow(1).getCell(8).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(1).getCell(9).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(10).getStringCellValue == "Aalto-universitetets magisteransökan 2023")
    assert(sheet.getRow(1).getCell(11).getStringCellValue == "1.2.246.562.29.00000000000000015722")
    assert(sheet.getRow(1).getCell(12).getStringCellValue == "1.12.2022 - 2.1.2023")
    assert(sheet.getRow(1).getCell(13).getStringCellValue == "Separata antagningar")
    assert(sheet.getRow(1).getCell(14).getNumericCellValue == 8)
    assert(sheet.getRow(1).getCell(15).getStringCellValue == "-")
    assert(
      sheet
        .getRow(1)
        .getCell(16)
        .getStringCellValue == "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
    )
    assert(sheet.getRow(1).getCell(17) == null)

    assert(
      sheet.getRow(2).getCell(0).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(
      sheet
        .getRow(2)
        .getCell(1)
        .getStringCellValue == "Arkitektur - Arkitektur, landskapsarkitektur och inredningsarkitektur, Arkitekt (2 år)"
    )
    assert(sheet.getRow(2).getCell(2).getStringCellValue == "1.2.246.562.17.00000000000000007965")
    assert(sheet.getRow(2).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(2).getCell(4).getStringCellValue == "-")
    assert(sheet.getRow(2).getCell(5).getStringCellValue == "-")
    assert(sheet.getRow(2).getCell(6).getStringCellValue == "Arkitektur, Arkitekt (2 år)")
    assert(sheet.getRow(2).getCell(7).getStringCellValue == "1.2.246.562.20.00000000000000017819")
    assert(sheet.getRow(2).getCell(8).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(2).getCell(9).getStringCellValue == "ARTS20100")
    assert(sheet.getRow(2).getCell(10).getStringCellValue == "Aalto-universitetets magisteransökan 2023")
    assert(sheet.getRow(1).getCell(11).getStringCellValue == "1.2.246.562.29.00000000000000015722")
    assert(sheet.getRow(2).getCell(12).getStringCellValue == "1.12.2022 - 2.1.2023")
    assert(sheet.getRow(2).getCell(13).getStringCellValue == "Separata antagningar")
    assert(sheet.getRow(2).getCell(14).getNumericCellValue == 12)
    assert(sheet.getRow(2).getCell(15).getStringCellValue == "-")
    assert(
      sheet
        .getRow(2)
        .getCell(16)
        .getStringCellValue == "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
    )
    assert(sheet.getRow(2).getCell(17) == null)

    assert(
      sheet.getRow(3).getCell(0).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(
      sheet
        .getRow(3)
        .getCell(1)
        .getStringCellValue == "Collaborative and Industrial Design - Design, Master of Arts (Art and Design) (2 yrs) -toteutus SV"
    )
    assert(sheet.getRow(3).getCell(2).getStringCellValue == "1.2.246.562.17.00000000000000007977")
    assert(sheet.getRow(3).getCell(3).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(3).getCell(4).getStringCellValue == "-")
    assert(sheet.getRow(3).getCell(5).getStringCellValue == "30.9.2023")
    assert(
      sheet.getRow(3).getCell(6).getStringCellValue == "Hakukohde Collaborative and Industrial Design, Master of Arts (2 yrs) SV"
    )
    assert(sheet.getRow(3).getCell(7).getStringCellValue == "1.2.246.562.20.00000000000000017881")
    assert(sheet.getRow(3).getCell(8).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(3).getCell(9).getStringCellValue == "ARTS20200")
    assert(sheet.getRow(3).getCell(10).getStringCellValue == "Aalto-universitetets magisteransökan 2023")
    assert(sheet.getRow(1).getCell(11).getStringCellValue == "1.2.246.562.29.00000000000000015722")
    assert(sheet.getRow(3).getCell(12).getStringCellValue == "1.12.2022 - 2.1.2023")
    assert(sheet.getRow(3).getCell(13).getStringCellValue == "Separata antagningar")
    assert(sheet.getRow(3).getCell(14).getNumericCellValue == 14)
    assert(sheet.getRow(3).getCell(15).getNumericCellValue == 10)
    assert(
      sheet
        .getRow(3)
        .getCell(16)
        .getStringCellValue == "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
    )
    assert(sheet.getRow(3).getCell(17) == null)

    assert(
      sheet.getRow(4).getCell(0).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(
      sheet
        .getRow(4)
        .getCell(1)
        .getStringCellValue == "Film- och tv-manuskript - Filmkonst, konstmagister (2 år) -toteutus"
    )
    assert(sheet.getRow(4).getCell(2).getStringCellValue == "1.2.246.562.17.00000000000000007967")
    assert(sheet.getRow(4).getCell(3).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(4).getCell(4).getStringCellValue == "-")
    assert(sheet.getRow(4).getCell(5).getStringCellValue == "Höst 2023")
    assert(sheet.getRow(4).getCell(6).getStringCellValue == "Film- och tv-manuskript, konstmagister (2 år)")
    assert(sheet.getRow(4).getCell(7).getStringCellValue == "1.2.246.562.20.00000000000000017822")
    assert(sheet.getRow(4).getCell(8).getStringCellValue == "Luonnos SV")
    assert(sheet.getRow(4).getCell(9).getStringCellValue == "ARTS20503")
    assert(sheet.getRow(4).getCell(10).getStringCellValue == "Aalto-universitetets magisteransökan 2023")
    assert(sheet.getRow(1).getCell(11).getStringCellValue == "1.2.246.562.29.00000000000000015722")
    assert(sheet.getRow(4).getCell(12).getStringCellValue == "1.12.2022 - 2.1.2023")
    assert(sheet.getRow(4).getCell(13).getStringCellValue == "Separata antagningar")
    assert(sheet.getRow(4).getCell(14).getNumericCellValue == 15)
    assert(sheet.getRow(4).getCell(15).getNumericCellValue == 10)
    assert(
      sheet
        .getRow(4)
        .getCell(16)
        .getStringCellValue == "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
    )
    assert(sheet.getRow(4).getCell(17) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 5)
    assert(wb.getSheetAt(0).getRow(6) == null)
  }

  it should "create Korkeakoulujen koulutukset toteutukset ja hakukohteet -raportti hakukohteittain with four result rows" in {
    val wb = ExcelWriter.writeKorkeakouluKoulutuksetToteutuksetHakukohteetRaportti(
      korkeakouluKoulutuksetToteutuksetHakukohteetResults = korkeakouluKoulutuksetToteutuksetHakukohteet,
      asiointikieli = userLng,
      translations = translations,
      tulostustapa = "hakukohteittain",
      parametrit = kkKoulutuksetToteutuksetHakukohteetParams
    )

    val sheet = wb.getSheetAt(0)
    assert(sheet.getRow(0).getCell(0).getStringCellValue == "Oppilaitos ja toimipiste SV")
    assert(sheet.getRow(0).getCell(1).getStringCellValue == "Hakukohde SV")
    assert(sheet.getRow(0).getCell(2).getStringCellValue == "Hakukohteen oid SV")
    assert(sheet.getRow(0).getCell(3).getStringCellValue == "Hak.tila SV")
    assert(sheet.getRow(0).getCell(4).getStringCellValue == "raportti.hakukohteenUlkoinenTunniste")
    assert(sheet.getRow(0).getCell(5).getStringCellValue == "raportti.haunNimi")
    assert(sheet.getRow(0).getCell(6).getStringCellValue == "raportti.hakuOid")
    assert(sheet.getRow(0).getCell(7).getStringCellValue == "raportti.hakuaika")
    assert(sheet.getRow(0).getCell(8).getStringCellValue == "raportti.hakutapa")
    assert(sheet.getRow(0).getCell(9).getStringCellValue == "raportti.hakukohteenAloituspaikat")
    assert(sheet.getRow(0).getCell(10).getStringCellValue == "raportti.ensikertalaistenAloituspaikat")
    assert(sheet.getRow(0).getCell(11).getStringCellValue == "raportti.valintaperuste")
    assert(sheet.getRow(0).getCell(12).getStringCellValue == "raportti.toteutuksenNimi")
    assert(sheet.getRow(0).getCell(13).getStringCellValue == "raportti.toteutusOid")
    assert(sheet.getRow(0).getCell(14).getStringCellValue == "Tot.tila SV")
    assert(sheet.getRow(0).getCell(15).getStringCellValue == "raportti.toteutuksenUlkoinenTunniste")
    assert(sheet.getRow(0).getCell(16).getStringCellValue == "raportti.koulutuksenAlkamisaika")
    assert(sheet.getRow(0).getCell(17).getStringCellValue == "raportti.koulutuksenNimi")
    assert(sheet.getRow(0).getCell(18).getStringCellValue == "raportti.koulutusOid")
    assert(sheet.getRow(0).getCell(19).getStringCellValue == "Kou.tila SV")
    assert(sheet.getRow(0).getCell(20).getStringCellValue == "raportti.koulutuskoodi")
    assert(sheet.getRow(0).getCell(21).getStringCellValue == "raportti.koulutuksenUlkoinenTunniste")
    assert(sheet.getRow(0).getCell(22).getStringCellValue == "raportti.tutkinnonTaso")
    assert(sheet.getRow(0).getCell(23).getStringCellValue == "raportti.opintojenLaajuus")
    assert(sheet.getRow(0).getCell(24) == null)

    assert(
      sheet.getRow(1).getCell(0).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(sheet.getRow(1).getCell(1).getStringCellValue == "Animation, Master of Arts (2 yrs) SV")
    assert(sheet.getRow(1).getCell(2).getStringCellValue == "1.2.246.562.20.00000000000000017880")
    assert(sheet.getRow(1).getCell(3).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(1).getCell(4).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(5).getStringCellValue == "Aalto-universitetets magisteransökan 2023")
    assert(sheet.getRow(1).getCell(6).getStringCellValue == "1.2.246.562.29.00000000000000015722")
    assert(sheet.getRow(1).getCell(7).getStringCellValue == "1.12.2022 - 2.1.2023")
    assert(sheet.getRow(1).getCell(8).getStringCellValue == "Separata antagningar")
    assert(sheet.getRow(1).getCell(9).getNumericCellValue == 8)
    assert(sheet.getRow(1).getCell(10).getStringCellValue == "-")
    assert(
      sheet
        .getRow(1)
        .getCell(11)
        .getStringCellValue == "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
    )
    assert(
      sheet
        .getRow(1)
        .getCell(12)
        .getStringCellValue == "Animation, Master of Arts (Art and Design) (2 yrs) -toteutus SV"
    )
    assert(sheet.getRow(1).getCell(13).getStringCellValue == "1.2.246.562.17.00000000000000008396")
    assert(sheet.getRow(1).getCell(14).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(1).getCell(15).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(16).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(17).getStringCellValue == "Animation, Master of Arts (Art and Design) (2 yrs) SV")
    assert(sheet.getRow(1).getCell(18).getStringCellValue == "1.2.246.562.13.00000000000000002880")
    assert(sheet.getRow(1).getCell(19).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(1).getCell(20).getStringCellValue == "722101")
    assert(sheet.getRow(1).getCell(21).getStringCellValue == "-")
    assert(sheet.getRow(1).getCell(22).getStringCellValue == "raportti.tutkinnontaso.ylempi")
    assert(sheet.getRow(1).getCell(23).getStringCellValue == "120 studiepoäng")
    assert(sheet.getRow(1).getCell(24) == null)

    assert(
      sheet.getRow(2).getCell(0).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(sheet.getRow(2).getCell(1).getStringCellValue == "Arkitektur, Arkitekt (2 år)")
    assert(sheet.getRow(2).getCell(2).getStringCellValue == "1.2.246.562.20.00000000000000017819")
    assert(sheet.getRow(2).getCell(3).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(2).getCell(4).getStringCellValue == "ARTS20100")
    assert(sheet.getRow(2).getCell(5).getStringCellValue == "Aalto-universitetets magisteransökan 2023")
    assert(sheet.getRow(2).getCell(6).getStringCellValue == "1.2.246.562.29.00000000000000015722")
    assert(sheet.getRow(2).getCell(7).getStringCellValue == "1.12.2022 - 2.1.2023")
    assert(sheet.getRow(2).getCell(8).getStringCellValue == "Separata antagningar")
    assert(sheet.getRow(2).getCell(9).getNumericCellValue == 12)
    assert(sheet.getRow(2).getCell(10).getStringCellValue == "-")
    assert(
      sheet
        .getRow(2)
        .getCell(11)
        .getStringCellValue == "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
    )
    assert(
      sheet
        .getRow(2)
        .getCell(12)
        .getStringCellValue == "Arkitektur - Arkitektur, landskapsarkitektur och inredningsarkitektur, Arkitekt (2 år)"
    )
    assert(sheet.getRow(2).getCell(13).getStringCellValue == "1.2.246.562.17.00000000000000007965")
    assert(sheet.getRow(2).getCell(14).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(2).getCell(15).getStringCellValue == "-")
    assert(sheet.getRow(2).getCell(16).getStringCellValue == "-")
    assert(
      sheet
        .getRow(2)
        .getCell(17)
        .getStringCellValue == "Arkitektur - Arkitektur, landskapsarkitektur och inredningsarkitektur, Arkitekt (2 år)"
    )
    assert(sheet.getRow(2).getCell(18).getStringCellValue == "1.2.246.562.13.00000000000000002675")
    assert(sheet.getRow(2).getCell(19).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(2).getCell(20).getStringCellValue == "754101")
    assert(sheet.getRow(2).getCell(21).getStringCellValue == "ARTS20100")
    assert(sheet.getRow(2).getCell(22).getStringCellValue == "raportti.tutkinnontaso.alempijaylempi")
    assert(sheet.getRow(2).getCell(23).getStringCellValue == "120 studiepoäng")
    assert(sheet.getRow(2).getCell(24) == null)

    assert(
      sheet.getRow(3).getCell(0).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(sheet.getRow(3).getCell(1).getStringCellValue == "Film- och tv-manuskript, konstmagister (2 år)")
    assert(sheet.getRow(3).getCell(2).getStringCellValue == "1.2.246.562.20.00000000000000017822")
    assert(sheet.getRow(3).getCell(3).getStringCellValue == "Luonnos SV")
    assert(sheet.getRow(3).getCell(4).getStringCellValue == "ARTS20503")
    assert(sheet.getRow(3).getCell(5).getStringCellValue == "Aalto-universitetets magisteransökan 2023")
    assert(sheet.getRow(3).getCell(6).getStringCellValue == "1.2.246.562.29.00000000000000015722")
    assert(sheet.getRow(3).getCell(7).getStringCellValue == "1.12.2022 - 2.1.2023")
    assert(sheet.getRow(3).getCell(8).getStringCellValue == "Separata antagningar")
    assert(sheet.getRow(3).getCell(9).getNumericCellValue == 15)
    assert(sheet.getRow(3).getCell(10).getNumericCellValue == 10)
    assert(
      sheet
        .getRow(3)
        .getCell(11)
        .getStringCellValue == "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
    )
    assert(
      sheet
        .getRow(3)
        .getCell(12)
        .getStringCellValue == "Film- och tv-manuskript - Filmkonst, konstmagister (2 år) -toteutus"
    )
    assert(sheet.getRow(3).getCell(13).getStringCellValue == "1.2.246.562.17.00000000000000007967")
    assert(sheet.getRow(3).getCell(14).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(3).getCell(15).getStringCellValue == "-")
    assert(sheet.getRow(3).getCell(16).getStringCellValue == "Höst 2023")
    assert(sheet.getRow(3).getCell(17).getStringCellValue == "Film- och tv-manuskript - Filmkonst, konstmagister (2 år)")
    assert(sheet.getRow(3).getCell(18).getStringCellValue == "1.2.246.562.13.00000000000000002677")
    assert(sheet.getRow(3).getCell(19).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(3).getCell(20).getStringCellValue == "309902")
    assert(sheet.getRow(3).getCell(21).getStringCellValue == "ARTS20502")
    assert(sheet.getRow(3).getCell(22).getStringCellValue == "raportti.tutkinnontaso.alempi")
    assert(sheet.getRow(3).getCell(23).getStringCellValue == "120 studiepoäng")
    assert(sheet.getRow(3).getCell(24) == null)

    assert(
      sheet.getRow(4).getCell(0).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(
      sheet.getRow(4).getCell(1).getStringCellValue == "Hakukohde Collaborative and Industrial Design, Master of Arts (2 yrs) SV"
    )
    assert(sheet.getRow(4).getCell(2).getStringCellValue == "1.2.246.562.20.00000000000000017881")
    assert(sheet.getRow(4).getCell(3).getStringCellValue == "Arkistoitu SV")
    assert(sheet.getRow(4).getCell(4).getStringCellValue == "ARTS20200")
    assert(sheet.getRow(4).getCell(5).getStringCellValue == "Aalto-universitetets magisteransökan 2023")
    assert(sheet.getRow(4).getCell(6).getStringCellValue == "1.2.246.562.29.00000000000000015722")
    assert(sheet.getRow(4).getCell(7).getStringCellValue == "1.12.2022 - 2.1.2023")
    assert(sheet.getRow(4).getCell(8).getStringCellValue == "Separata antagningar")
    assert(sheet.getRow(4).getCell(9).getNumericCellValue == 14)
    assert(sheet.getRow(4).getCell(10).getNumericCellValue == 10)
    assert(
      sheet
        .getRow(4)
        .getCell(11)
        .getStringCellValue == "Magisterurvalet (Aalto-universitetet, konstnärligt utbildningsområde) 2023"
    )
    assert(
      sheet
        .getRow(4)
        .getCell(12)
        .getStringCellValue == "Collaborative and Industrial Design - Design, Master of Arts (Art and Design) (2 yrs) -toteutus SV"
    )
    assert(sheet.getRow(4).getCell(13).getStringCellValue == "1.2.246.562.17.00000000000000007977")
    assert(sheet.getRow(4).getCell(14).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(4).getCell(15).getStringCellValue == "-")
    assert(sheet.getRow(4).getCell(16).getStringCellValue == "30.9.2023")
    assert(
      sheet
        .getRow(4)
        .getCell(17)
        .getStringCellValue == "Collaborative and Industrial Design - Design, Master of Arts (Art and Design) (2 yrs) SV"
    )
    assert(sheet.getRow(4).getCell(18).getStringCellValue == "1.2.246.562.13.00000000000000002687")
    assert(sheet.getRow(4).getCell(19).getStringCellValue == "Julkaistu SV")
    assert(sheet.getRow(4).getCell(20).getStringCellValue == "722101")
    assert(sheet.getRow(4).getCell(21).getStringCellValue == "ARTS20200")
    assert(sheet.getRow(4).getCell(22).getStringCellValue == "raportti.tutkinnontaso.jatkotutkinto")
    assert(sheet.getRow(4).getCell(23).getStringCellValue == "120 studiepoäng")
    assert(sheet.getRow(4).getCell(24) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 5)
    assert(wb.getSheetAt(0).getRow(6) == null)
  }

  it should "create hakuparametrit sheet in Korkeakoulujen koulutukset toteutukset ja hakukohteet" in {
    val hakuParams: List[(String, String | Boolean | List[String])] =
      List(
        "haku" -> List("1.2.246.562.29.00000000000000015722"),
        "tulostustapa" -> "hakukohteittain",
        "oppilaitos" -> List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        "toimipiste" -> List("1.2.246.562.10.2781706420001"),
        "hakukohderyhma" -> List("1.2.246.562.28.28396122930"),
        "koulutuksenTila" -> "julkaistu",
        "toteutuksenTila" -> "arkistoitu",
        "hakukohteenTila" -> "julkaistu",
        "kk-tutkinnon-taso" -> List("ylempi")
      )
    val wb = ExcelWriter.writeKorkeakouluKoulutuksetToteutuksetHakukohteetRaportti(
      korkeakouluKoulutuksetToteutuksetHakukohteetResults = korkeakouluKoulutuksetToteutuksetHakukohteet,
      asiointikieli = userLng,
      translations = translations,
      tulostustapa = "hakukohteittain",
      parametrit = hakuParams
    )

    val expectedHeaders = List("raportti.hakuehto", "raportti.hakuarvo")
    val expectedRows = List(
      List("Haku SV", "1.2.246.562.29.00000000000000015722"),
      List("Tulostustapa SV", "Hakukohteittain SV"),
      List("Oppilaitos SV", "1.2.246.562.10.00000000001, 1.2.246.562.10.2781706420000"),
      List("Toimipiste SV", "1.2.246.562.10.2781706420001"),
      List("Hakukohderyhma SV", "1.2.246.562.28.28396122930"),
      List("Kou.tila SV", "Julkaistu SV"),
      List("Tot.tila SV", "Arkistoitu SV"),
      List("Hak.tila SV", "Julkaistu SV"),
      List("Tutkinnon taso SV", "ylempi SV")
    )
    assert(wb.getNumberOfSheets == 2)
    assert(wb.getSheetAt(1).getRow(1) != null)
    val sheet = wb.getSheetAt(1)

    validateHeaders(sheet = sheet, expectedHeaders = expectedHeaders)
    expectedRows.zipWithIndex.foreach { case (expectedRow, rowIndex) =>
      validateRow(sheet, rowIndex + 1, expectedRow)
    }
  }


  "createHeadingRow" should "create heading row with translated column names or translation keys for hakijat raportti" in {
    val wb: XSSFWorkbook                = new XSSFWorkbook()
    val sheet: XSSFSheet                = wb.createSheet()
    val headingCellStyle: XSSFCellStyle = wb.createCellStyle()

    val fieldNames = classOf[ToisenAsteenHakija].getDeclaredFields.map(_.getName).toList
    ExcelWriter.createHeadingRow(
      sheet,
      translations,
      0,
      fieldNames,
      headingCellStyle
    )

    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Sukunimi SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "Etunimi SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "Turvakielto SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "Kansalaisuudet SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "Oppilaitos SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(9).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(0).getCell(10).getStringCellValue == "Kaksoistutkinto kiinnostaa SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(11).getStringCellValue == "Urheilijatutkinto kiinnostaa SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(12).getStringCellValue == "Valintatieto SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(13).getStringCellValue == "Varasija SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(14).getStringCellValue == "Kokonaispisteet SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(15).getStringCellValue == "Valintatapajonokohtainen tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(16).getStringCellValue == "Vastaanottotieto SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(17).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(0).getCell(18).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(0).getCell(19).getStringCellValue == "Harkinnanvaraisuus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(20).getStringCellValue == "Sora aiempi SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(21).getStringCellValue == "Sora terveydentila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(22).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(23).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(24).getStringCellValue == "Julkaisulupa SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(25).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(0).getCell(26).getStringCellValue == "raportti.lahiosoite")
    assert(wb.getSheetAt(0).getRow(0).getCell(27).getStringCellValue == "raportti.postinumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(28).getStringCellValue == "raportti.postitoimipaikka")
    assert(wb.getSheetAt(0).getRow(0).getCell(29).getStringCellValue == "raportti.sahkoposti")
    assert(wb.getSheetAt(0).getRow(0).getCell(30) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 1)
    assert(wb.getSheetAt(0).getRow(1) == null)
  }

  "createHeadingRowWithValintatapajonotAndYoArvosanat" should "create heading row with translated column names or translation keys for hakijat raportti" in {
    val wb: XSSFWorkbook                = new XSSFWorkbook()
    val sheet: XSSFSheet                = wb.createSheet()
    val headingCellStyle: XSSFCellStyle = wb.createCellStyle()

    val valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1709304116443-4815957697640331820",
        valintatapajononNimi = "Lukiokoulutus",
        valinnanTila = "PERUUNTUNUT",
        valinnanTilanKuvaus = Map(
          En -> "Cancelled, accepted for a study place with higher priority",
          Fi -> "Peruuntunut, hyväksytty ylemmälle hakutoiveelle",
          Sv -> "Annullerad, godkänt till ansökningsmål med högre prioritet"
        )
      )
    )
    val origFieldNames    = classOf[KkHakija].getDeclaredFields.map(_.getName).toList
    val headingFieldNames = ExcelWriter.getHeadingFieldNames(true, true, true, valintatapajonot, List("A", "FF", "TE"))
    ExcelWriter.createHeadingRowWithValintatapajonotAndYoArvosanat(
      sheet,
      translations,
      0,
      headingFieldNames,
      origFieldNames,
      headingCellStyle
    )

    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Sukunimi SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "Etunimi SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "Turvakielto SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "raportti.hetu")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "raportti.syntymaAika")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "Kansalaisuudet SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(9).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(10).getStringCellValue == "Hakukelpoisuus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(11).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(0).getCell(12).getStringCellValue == "Valintatieto SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(13).getStringCellValue == "raportti.ehdollisestiHyvaksytty")
    assert(wb.getSheetAt(0).getRow(0).getCell(14).getStringCellValue == "Valintatiedon päivämäärä SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(15).getStringCellValue == "Lukiokoulutus")
    assert(wb.getSheetAt(0).getRow(0).getCell(16).getStringCellValue == "Vastaanottotieto SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(17).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(0).getCell(18).getStringCellValue == "raportti.ensikertalainen")
    assert(wb.getSheetAt(0).getRow(0).getCell(19).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(0).getCell(20).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(21).getStringCellValue == "raportti.maksuvelvollisuus")
    assert(wb.getSheetAt(0).getRow(0).getCell(22).getStringCellValue == "raportti.hakemusmaksunTila")
    assert(wb.getSheetAt(0).getRow(0).getCell(23).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(24).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(0).getCell(25).getStringCellValue == "raportti.lahiosoite")
    assert(wb.getSheetAt(0).getRow(0).getCell(26).getStringCellValue == "raportti.postinumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(27).getStringCellValue == "raportti.postitoimipaikka")
    assert(wb.getSheetAt(0).getRow(0).getCell(28).getStringCellValue == "raportti.kotikunta")
    assert(wb.getSheetAt(0).getRow(0).getCell(29).getStringCellValue == "raportti.asuinmaa")
    assert(wb.getSheetAt(0).getRow(0).getCell(30).getStringCellValue == "Puhelinnumero SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(31).getStringCellValue == "raportti.sahkoposti")
    assert(wb.getSheetAt(0).getRow(0).getCell(32).getStringCellValue == "A")
    assert(wb.getSheetAt(0).getRow(0).getCell(33).getStringCellValue == "FF")
    assert(wb.getSheetAt(0).getRow(0).getCell(34).getStringCellValue == "TE")
    assert(wb.getSheetAt(0).getRow(0).getCell(35) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 1)
    assert(wb.getSheetAt(0).getRow(1) == null)
  }

  val hakijatParams: List[(String, String | Boolean | List[String])] =
    List(
      "haku" -> List("1.2.246.562.29.00000000000000015722"),
      "oppilaitos" -> List("1.2.246.562.10.00000000001"),
      "valintatieto" -> List("HYVAKSYTTY"),
    )

  "writeToisenAsteenHakijatRaportti" should "create one sheet with a sheet name, heading row and no results and parameter sheet" in {
    val hakijatQueryResult = Vector()
    val wb =
      ExcelWriter.writeToisenAsteenHakijatRaportti(
        hakijatQueryResult,
        userLng,
        translations,
        hakijatParams,
      )

    assert(wb.getNumberOfSheets == 2)
    assert(wb.getSheetName(0) == "Yhteenveto SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue == "Sukunimi SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue == "Etunimi SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(2).getStringCellValue == "Turvakielto SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(3).getStringCellValue == "Kansalaisuudet SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(4).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(5).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(0).getCell(6).getStringCellValue == "Oppilaitos SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(7).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(8).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(9).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(0).getCell(10).getStringCellValue == "Kaksoistutkinto kiinnostaa SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(11).getStringCellValue == "Urheilijatutkinto kiinnostaa SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(12).getStringCellValue == "Valintatieto SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(13).getStringCellValue == "Varasija SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(14).getStringCellValue == "Kokonaispisteet SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(15).getStringCellValue == "Valintatapajonokohtainen tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(16).getStringCellValue == "Vastaanottotieto SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(17).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(0).getCell(18).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(0).getCell(19).getStringCellValue == "Harkinnanvaraisuus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(20).getStringCellValue == "Sora aiempi SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(21).getStringCellValue == "Sora terveydentila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(22).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(23).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(24).getStringCellValue == "Julkaisulupa SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(25).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(0).getCell(26).getStringCellValue == "raportti.lahiosoite")
    assert(wb.getSheetAt(0).getRow(0).getCell(27).getStringCellValue == "raportti.postinumero")
    assert(wb.getSheetAt(0).getRow(0).getCell(28).getStringCellValue == "raportti.postitoimipaikka")
    assert(wb.getSheetAt(0).getRow(0).getCell(29).getStringCellValue == "raportti.sahkoposti")
    assert(wb.getSheetAt(0).getRow(0).getCell(30) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 1)
    assert(wb.getSheetAt(0).getRow(1) == null)
  }

  it should "create hakuparametrit sheet" in {
    val hakuParams: List[(String, String | Boolean | List[String])] =
      List(
        "haku" -> List("1.2.246.562.29.00000000000000015722"),
        "oppilaitos" -> List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        "toimipiste" -> List("1.2.246.562.10.2781706420001"),
        "hakukohde" -> List("1.2.246.562.20.00000000000000059957"),
        "pohjakoulutus" -> List("Perusopetuksen oppimäärä"),
        "valintatieto" -> List("HYVAKSYTTY"),
        "vastaanottotieto" -> List("PERUUTETTU"),
        "harkinnanvaraisuus" -> List("OPPIMISVAIKEUDET"),
        "kaksoistutkintoKiinnostaa" -> false,
        "urheilijatutkintoKiinnostaa" -> false,
        "soraTerveys" -> true,
        "soraAiempi" -> true,
        "markkinointilupa" -> true,
        "julkaisulupa" -> true
      )
    val hakijatQueryResult = Vector()
    val wb =
      ExcelWriter.writeToisenAsteenHakijatRaportti(
        hakijatQueryResult,
        userLng,
        translations,
        hakuParams,
      )

    val expectedHeaders = List("raportti.hakuehto", "raportti.hakuarvo")
    val expectedRows = List(
      List("Haku SV", "1.2.246.562.29.00000000000000015722"),
      List("Oppilaitos SV", "1.2.246.562.10.00000000001, 1.2.246.562.10.2781706420000"),
      List("Toimipiste SV", "1.2.246.562.10.2781706420001"),
      List("Hakukohde SV", "1.2.246.562.20.00000000000000059957"),
      List("Pohjakoulutus SV", "Perusopetuksen oppimäärä"),
      List("Valintatieto SV", "Hyvaksytty SV"),
      List("Vastaanottotieto SV", "Peruutettu SV"),
      List("Harkinnanvaraisuus SV", "Oppimisvaikeudet SV"),
      List("Kaksoistutkinto kiinnostaa SV", "Nej"),
      List("Urheilijatutkinto kiinnostaa SV", "Nej"),
      List("Sora terveydentila SV", "Ja"),
      List("Sora aiempi SV", "Ja"),
      List("LupaMark SV", "Ja"),
      List("Julkaisulupa SV", "Ja")
    )
    assert(wb.getNumberOfSheets == 2)
    assert(wb.getSheetAt(1).getRow(1) != null)
    val sheet = wb.getSheetAt(1)

    validateHeaders(sheet = sheet, expectedHeaders = expectedHeaders)
    expectedRows.zipWithIndex.foreach { case (expectedRow, rowIndex) =>
      validateRow(sheet, rowIndex + 1, expectedRow)
    }
  }

  it should "return excel with one result row in addition to heading row" in {
    val hakijatResult = Vector(
      ToisenAsteenHakija(
        "Rautiainen-Testi",
        "Dina Testi",
        Some(false),
        List(Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland"), Map(En -> "Sweden", Fi -> "Ruotsi", Sv -> "Sverige")),
        "1.2.246.562.24.30646006111",
        "1.2.246.562.11.00000000000002179045",
        Map(En -> "Oppilaitos en", Fi  -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
        Map(En -> "Toimipiste en", Fi  -> "Toimipiste fi", Sv -> "Toimipiste sv"),
        Map(En -> "Hakukohde 1 EN", Fi -> "Hakukohde 1", Sv   -> "Hakukohde 1 SV"),
        2,
        Some(true),
        None,
        Some("HYLATTY"),
        None,
        None,
        Map(En -> "", Fi -> "Hylätty harkinnanvaraisessa valinnassa", Sv -> ""),
        Some("EI_VASTAANOTETTU_MAARA_AIKANA"),
        None,
        None,
        Some("ATARU_OPPIMISVAIKEUDET"),
        Some(false),
        Some(false),
        Map(
          En -> "Perusopetuksen oppimäärä",
          Fi -> "Perusopetuksen oppimäärä",
          Sv -> "Den grundläggande utbildningens lärokurs"
        ),
        Some(false),
        Some(true),
        None,
        Some("Rämsöönranta 368"),
        Some("00100"),
        Some("HELSINKI"),
        Some("dina.rautiainen@example.org")
      )
    )

    val wb =
      ExcelWriter.writeToisenAsteenHakijatRaportti(
        hakijatResult,
        userLng,
        translations,
        hakijatParams,
      )

    assert(wb.getNumberOfSheets == 2)
    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Rautiainen-Testi")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "Dina Testi")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "Finland, Sverige")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "1.2.246.562.11.00000000000002179045")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "Oppilaitos sv")
    assert(wb.getSheetAt(0).getRow(1).getCell(7).getStringCellValue == "Toimipiste sv")
    assert(wb.getSheetAt(0).getRow(1).getCell(8).getStringCellValue == "Hakukohde 1 SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(9).getNumericCellValue == 2)
    assert(wb.getSheetAt(0).getRow(1).getCell(10).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(11).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(12).getStringCellValue == "Hylatty SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(13).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(14).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(15).getStringCellValue == "")
    assert(wb.getSheetAt(0).getRow(1).getCell(16).getStringCellValue == "Ei vastaanotettu SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(17).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(18).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(19).getStringCellValue == "Oppimisvaikeudet SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(20).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(21).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(22).getStringCellValue == "Den grundläggande utbildningens lärokurs")
    assert(wb.getSheetAt(0).getRow(1).getCell(23).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(24).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(25).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(26).getStringCellValue == "Rämsöönranta 368")
    assert(wb.getSheetAt(0).getRow(1).getCell(27).getStringCellValue == "00100")
    assert(wb.getSheetAt(0).getRow(1).getCell(28).getStringCellValue == "HELSINKI")
    assert(wb.getSheetAt(0).getRow(1).getCell(29).getStringCellValue == "dina.rautiainen@example.org")
    assert(wb.getSheetAt(0).getRow(1).getCell(30) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 2)
    assert(wb.getSheetAt(0).getRow(2) == null)
  }

  it should "return excel with three result rows: two for one hakija and one for another hakija" in {
    val hakijatResult =
      Vector(
        ToisenAsteenHakija(
          "Rautiainen-Testi",
          "Dina Testi",
          Some(false),
          List(Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland")),
          "1.2.246.562.24.30646006111",
          "1.2.246.562.11.00000000000002179045",
          Map(En -> "Oppilaitos 1 en", Fi -> "Oppilaitos 1 fi", Sv -> "Oppilaitos 1 sv"),
          Map(En -> "Toimipiste 1 en", Fi -> "Toimipiste 1 fi", Sv -> "Toimipiste 1 sv"),
          Map(En -> "Hakukohde 1 EN", Fi  -> "Hakukohde 1", Sv     -> "Hakukohde 1 SV"),
          2,
          Some(true),
          Some(false),
          Some("HYLATTY"),
          Some("3"),
          Some("8.5"),
          Map(En -> "", Fi -> "Hylätty harkinnanvaraisessa valinnassa", Sv -> ""),
          None,
          None,
          Some("LASNA_KOKO_LUKUVUOSI"),
          None,
          Some(false),
          Some(false),
          Map(
            En -> "Perusopetuksen oppimäärä",
            Fi -> "Perusopetuksen oppimäärä",
            Sv -> "Den grundläggande utbildningens lärokurs"
          ),
          Some(false),
          Some(true),
          None,
          Some("Rämsöönranta 368"),
          Some("00100"),
          Some("HELSINKI"),
          Some("dina.rautiainen@example.org")
        ),
        ToisenAsteenHakija(
          "Rautiainen-Testi",
          "Dina Testi",
          Some(false),
          List(Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland")),
          "1.2.246.562.24.30646006111",
          "1.2.246.562.11.00000000000002112891",
          Map(En -> "Oppilaitos 2 en", Fi -> "Oppilaitos 2 fi", Sv -> "Oppilaitos 2 sv"),
          Map(En -> "Toimipiste 2 en", Fi -> "Toimipiste 2 fi", Sv -> "Toimipiste 2 sv"),
          Map(En -> "Hakukohde 2 EN", Fi  -> "Hakukohde 2", Sv     -> "Hakukohde 2 SV"),
          1,
          Some(true),
          Some(true),
          Some("HYVAKSYTTY"),
          Some("8"),
          None,
          Map(),
          Some("PERUNUT"),
          None,
          Some("EI_TEHTY"),
          Some("ATARU_ULKOMAILLA_OPISKELTU"),
          Some(false),
          Some(false),
          Map(
            En -> "Perusopetuksen oppimäärä",
            Fi -> "Perusopetuksen oppimäärä",
            Sv -> "Den grundläggande utbildningens lärokurs"
          ),
          Some(true),
          Some(true),
          None,
          Some("Rämsöönranta 368"),
          Some("00100"),
          Some("HELSINKI"),
          Some("dina.rautiainen@example.org")
        ),
        ToisenAsteenHakija(
          "Lehto-Testi",
          "Vikke Testi",
          Some(false),
          List(Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland")),
          "1.2.246.562.24.18441866015",
          "1.2.246.562.11.00000000000002126102",
          Map(En -> "Oppilaitos 3 en", Fi -> "Oppilaitos 3 fi", Sv -> "Oppilaitos 3 sv"),
          Map(En -> "Toimipiste 3 en", Fi -> "Toimipiste 3 fi", Sv -> "Toimipiste 3 sv"),
          Map(En -> "Hakukohde 3 EN", Fi  -> "Hakukohde 3", Sv     -> "Hakukohde 3 SV"),
          1,
          Some(true),
          Some(false),
          Some("PERUUNTUNUT"),
          None,
          Some("5.6"),
          Map(
            En -> "Cancelled, waiting list selection has ended",
            Fi -> "Peruuntunut, varasijatäyttö päättynyt",
            Sv -> "Annullerad, besättning av reservplatser har upphört"
          ),
          Some("PERUNUT"),
          Some(LocalDate.parse("2024-06-26")),
          Some("LASNA_KOKO_LUKUVUOSI"),
          Some("SURE_YKS_MAT_AI"),
          Some(false),
          Some(false),
          Map(
            En -> "Ulkomailla suoritettu koulutus",
            Fi -> "Ulkomailla suoritettu koulutus",
            Sv -> "Utbildning utomlands"
          ),
          Some(false),
          Some(true),
          Some(true),
          Some("Laholanaukio 834"),
          Some("00100"),
          Some("HELSINKI"),
          Some("vikke.lehto@example.org")
        )
      )

    val wb =
      ExcelWriter.writeToisenAsteenHakijatRaportti(
        hakijatResult,
        userLng,
        translations,
        hakijatParams,
      )

    assert(wb.getNumberOfSheets == 2)
    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Rautiainen-Testi")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "Dina Testi")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "1.2.246.562.11.00000000000002179045")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "Oppilaitos 1 sv")
    assert(wb.getSheetAt(0).getRow(1).getCell(7).getStringCellValue == "Toimipiste 1 sv")
    assert(wb.getSheetAt(0).getRow(1).getCell(8).getStringCellValue == "Hakukohde 1 SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(9).getNumericCellValue == 2)
    assert(wb.getSheetAt(0).getRow(1).getCell(10).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(11).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(12).getStringCellValue == "Hylatty SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(13).getStringCellValue == "3")
    assert(wb.getSheetAt(0).getRow(1).getCell(14).getStringCellValue == "8.5")
    assert(wb.getSheetAt(0).getRow(1).getCell(15).getStringCellValue == "")
    assert(wb.getSheetAt(0).getRow(1).getCell(16).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(17).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(18).getStringCellValue == "raportti.lasna_koko_lukuvuosi")
    assert(wb.getSheetAt(0).getRow(1).getCell(19).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(20).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(21).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(22).getStringCellValue == "Den grundläggande utbildningens lärokurs")
    assert(wb.getSheetAt(0).getRow(1).getCell(23).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(1).getCell(24).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(1).getCell(25).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(1).getCell(26).getStringCellValue == "Rämsöönranta 368")
    assert(wb.getSheetAt(0).getRow(1).getCell(27).getStringCellValue == "00100")
    assert(wb.getSheetAt(0).getRow(1).getCell(28).getStringCellValue == "HELSINKI")
    assert(wb.getSheetAt(0).getRow(1).getCell(29).getStringCellValue == "dina.rautiainen@example.org")
    assert(wb.getSheetAt(0).getRow(1).getCell(30) == null)

    assert(wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue == "Rautiainen-Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(1).getStringCellValue == "Dina Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(2).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(2).getCell(3).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(4).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(2).getCell(5).getStringCellValue == "1.2.246.562.11.00000000000002112891")
    assert(wb.getSheetAt(0).getRow(2).getCell(6).getStringCellValue == "Oppilaitos 2 sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(7).getStringCellValue == "Toimipiste 2 sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(8).getStringCellValue == "Hakukohde 2 SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(9).getNumericCellValue == 1)
    assert(wb.getSheetAt(0).getRow(2).getCell(10).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(11).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(12).getStringCellValue == "Hyvaksytty SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(13).getStringCellValue == "8")
    assert(wb.getSheetAt(0).getRow(2).getCell(14).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(15).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(16).getStringCellValue == "raportti.perunut")
    assert(wb.getSheetAt(0).getRow(2).getCell(17).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(18).getStringCellValue == "raportti.ei_tehty")
    assert(wb.getSheetAt(0).getRow(2).getCell(19).getStringCellValue == "raportti.koulutodistusten_vertailuvaikeudet")
    assert(wb.getSheetAt(0).getRow(2).getCell(20).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(2).getCell(21).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(2).getCell(22).getStringCellValue == "Den grundläggande utbildningens lärokurs")
    assert(wb.getSheetAt(0).getRow(2).getCell(23).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(24).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(25).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(26).getStringCellValue == "Rämsöönranta 368")
    assert(wb.getSheetAt(0).getRow(2).getCell(27).getStringCellValue == "00100")
    assert(wb.getSheetAt(0).getRow(2).getCell(28).getStringCellValue == "HELSINKI")
    assert(wb.getSheetAt(0).getRow(1).getCell(29).getStringCellValue == "dina.rautiainen@example.org")
    assert(wb.getSheetAt(0).getRow(2).getCell(30) == null)

    assert(wb.getSheetAt(0).getRow(3).getCell(0).getStringCellValue == "Lehto-Testi")
    assert(wb.getSheetAt(0).getRow(3).getCell(1).getStringCellValue == "Vikke Testi")
    assert(wb.getSheetAt(0).getRow(3).getCell(2).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(3).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(3).getCell(4).getStringCellValue == "1.2.246.562.24.18441866015")
    assert(wb.getSheetAt(0).getRow(3).getCell(5).getStringCellValue == "1.2.246.562.11.00000000000002126102")
    assert(wb.getSheetAt(0).getRow(3).getCell(6).getStringCellValue == "Oppilaitos 3 sv")
    assert(wb.getSheetAt(0).getRow(3).getCell(7).getStringCellValue == "Toimipiste 3 sv")
    assert(wb.getSheetAt(0).getRow(3).getCell(8).getStringCellValue == "Hakukohde 3 SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(9).getNumericCellValue == 1)
    assert(wb.getSheetAt(0).getRow(3).getCell(10).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(11).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(12).getStringCellValue == "Peruuntunut SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(13).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(14).getStringCellValue == "5.6")
    assert(
      wb.getSheetAt(0).getRow(3).getCell(15).getStringCellValue == "Annullerad, besättning av reservplatser har upphört"
    )
    assert(wb.getSheetAt(0).getRow(3).getCell(16).getStringCellValue == "raportti.perunut")
    assert(wb.getSheetAt(0).getRow(3).getCell(17).getStringCellValue == "26.6.2024")
    assert(wb.getSheetAt(0).getRow(3).getCell(18).getStringCellValue == "raportti.lasna_koko_lukuvuosi")
    assert(wb.getSheetAt(0).getRow(3).getCell(19).getStringCellValue == "raportti.yks_mat_ai")
    assert(wb.getSheetAt(0).getRow(3).getCell(20).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(21).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(22).getStringCellValue == "Utbildning utomlands")
    assert(wb.getSheetAt(0).getRow(3).getCell(23).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(24).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(25).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(26).getStringCellValue == "Laholanaukio 834")
    assert(wb.getSheetAt(0).getRow(3).getCell(27).getStringCellValue == "00100")
    assert(wb.getSheetAt(0).getRow(3).getCell(28).getStringCellValue == "HELSINKI")
    assert(wb.getSheetAt(0).getRow(1).getCell(29).getStringCellValue == "dina.rautiainen@example.org")
    assert(wb.getSheetAt(0).getRow(3).getCell(30) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 4)
    assert(wb.getSheetAt(0).getRow(5) == null)
  }

  "createValintatapajonokohtainenTilaHeadingRow" should "create merged valintatapajonokohtainen tila heading cell in the right index on the row" in {
    val wb: XSSFWorkbook                = new XSSFWorkbook()
    val sheet: XSSFSheet                = wb.createSheet()
    val headingCellStyle: XSSFCellStyle = wb.createCellStyle()
    val valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1704199256878262657431481297336",
        valintatapajononNimi = "Todistusvalintajono ensikertalaisille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Et ole ensikertalainen hakija.",
          Fi -> "Et ole ensikertalainen hakija.",
          Sv -> "Du är inte en förstagångssökande."
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "17000468320548583779630214204232",
        valintatapajononNimi = "Koevalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Et osallistunut valintakokeeseen",
          Fi -> "Et osallistunut valintakokeeseen",
          Sv -> "Et osallistunut valintakokeeseen SV"
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1707384694164-3621431542682802084",
        valintatapajononNimi = "Todistusvalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Fi -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Sv -> "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt."
        )
      )
    )
    val fieldNames = classOf[KkHakija].getDeclaredFields.map(_.getName).toList
    ExcelWriter.createValintatapajonokohtainenTilaHeadingRow(
      wb,
      sheet,
      translations,
      0,
      fieldNames,
      valintatapajonot,
      true
    )

    for (i <- 0 until 14) {
      assert(wb.getSheetAt(0).getRow(0).getCell(i) == null)
    }

    assert(wb.getSheetAt(0).getRow(0).getCell(15).getStringCellValue == "Valintatapajonokohtainen tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(15).getColumnIndex == 15)

    for (i <- 16 until 29) {
      assert(wb.getSheetAt(0).getRow(0).getCell(i) == null)
    }
  }

  it should "create merged valintatapajonokohtainen tila heading cell in the right index on the row when hetu is not shown" in {
    val wb: XSSFWorkbook                = new XSSFWorkbook()
    val sheet: XSSFSheet                = wb.createSheet()
    val headingCellStyle: XSSFCellStyle = wb.createCellStyle()
    val valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1704199256878262657431481297336",
        valintatapajononNimi = "Todistusvalintajono ensikertalaisille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Et ole ensikertalainen hakija.",
          Fi -> "Et ole ensikertalainen hakija.",
          Sv -> "Du är inte en förstagångssökande."
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "17000468320548583779630214204232",
        valintatapajononNimi = "Koevalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Et osallistunut valintakokeeseen",
          Fi -> "Et osallistunut valintakokeeseen",
          Sv -> "Et osallistunut valintakokeeseen SV"
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1707384694164-3621431542682802084",
        valintatapajononNimi = "Todistusvalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Fi -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Sv -> "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt."
        )
      )
    )
    val fieldNames = classOf[KkHakija].getDeclaredFields.map(_.getName).toList
    ExcelWriter.createValintatapajonokohtainenTilaHeadingRow(
      wb,
      sheet,
      translations,
      0,
      fieldNames,
      valintatapajonot,
      false
    )

    for (i <- 0 until 14) {
      assert(wb.getSheetAt(0).getRow(0).getCell(i) == null)
    }

    assert(wb.getSheetAt(0).getRow(0).getCell(14).getStringCellValue == "Valintatapajonokohtainen tila SV")
    assert(wb.getSheetAt(0).getRow(0).getCell(14).getColumnIndex == 14)

    for (i <- 15 until 28) {
      assert(wb.getSheetAt(0).getRow(0).getCell(i) == null)
    }
  }

  val kkHakijaRautiainen: KkHakija = KkHakija(
    hakijanSukunimi = "Rautiainen-Testi",
    hakijanEtunimi = "Dina Testi",
    turvakielto = Some(false),
    hetu = Some("120393-129E"),
    syntymaAika = Some(LocalDate.parse("1993-03-12")),
    kansalaisuudet = List(Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland")),
    oppijanumero = "1.2.246.562.24.30646006111",
    hakemusOid = "1.2.246.562.11.00000000000002179045",
    toimipiste = Map(En -> "Toimipiste 1 en", Fi -> "Toimipiste 1 fi", Sv -> "Toimipiste 1 sv"),
    hakukohteenNimi = Map(En -> "Hakukohde 1 EN", Fi -> "Hakukohde 1", Sv -> "Hakukohde 1 SV"),
    hakukelpoisuus = None,
    prioriteetti = 2,
    valintatieto = Some("HYVAKSYTTY"),
    ehdollisestiHyvaksytty = Some(true),
    valintatiedonPvm = Some(LocalDate.parse("2024-06-13")),
    valintatapajonot = List(),
    vastaanottotieto = Some("PERUNUT"),
    viimVastaanottopaiva = Some(LocalDate.parse("2024-06-26")),
    ensikertalainen = Some(true),
    ilmoittautuminen = Some("LASNA_KOKO_LUKUVUOSI"),
    pohjakoulutus = Some(s"""["pohjakoulutus_yo", "pohjakoulutus_kk"]"""),
    maksuvelvollisuus = Some("not-obligated"),
    hakemusmaksunTila = Some("overdue"),
    markkinointilupa = Some(true),
    sahkoinenViestintalupa = Some(true),
    lahiosoite = Some("Rämsöönranta 368"),
    postinumero = Some("00100"),
    postitoimipaikka = Some("HELSINKI"),
    kotikunta = Map(En -> "Helsinki", Fi -> "Helsinki", Sv -> "Helsingfors"),
    asuinmaa = Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland"),
    puhelinnumero = Some("050 64292261"),
    sahkoposti = Some("hakija-33919666@oph.fi"),
    arvosanat = Map("A" -> "E", "M" -> "E", "BB" -> "M", "EA" -> "C", "FY" -> "E", "KE" -> "L")
  )

  val kkHakijatRautiainenWithValintatapajonot: KkHakija = kkHakijaRautiainen.copy(
    valintatiedonPvm = None,
    pohjakoulutus = None,
    maksuvelvollisuus = None,
    valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1707384694164-3621431542682802084",
        valintatapajononNimi = "Todistusvalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Fi -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Sv -> "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 1."
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "17096467457545361875995955973551",
        valintatapajononNimi = "Todistusvalinta (YO)",
        valinnanTila = "HYVAKSYTTY",
        valinnanTilanKuvaus = Map(
          En -> null,
          Fi -> null,
          Sv -> null
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1707384681516-2842344525807969324",
        valintatapajononNimi = "Todistusvalintajono ensikertalaisille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Fi -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Sv -> "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 2."
        )
      )
    )
  )

  val kkHakijaLehto: KkHakija = KkHakija(
    hakijanSukunimi = "Lehto-Testi",
    hakijanEtunimi = "Vikke Testi",
    turvakielto = Some(true),
    hetu = Some("04041990-345K"),
    syntymaAika = Some(LocalDate.parse("1990-04-04")),
    kansalaisuudet = List(Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland")),
    oppijanumero = "1.2.246.562.24.18441866015",
    hakemusOid = "1.2.246.562.11.00000000000002126102",
    toimipiste = Map(En -> "Toimipiste 2 en", Fi -> "Toimipiste 2 fi", Sv -> "Toimipiste 2 sv"),
    hakukohteenNimi = Map(En -> "Hakukohde 2 EN", Fi -> "Hakukohde 2", Sv -> "Hakukohde 2 SV"),
    hakukelpoisuus = Some("uneligible"),
    prioriteetti = 1,
    valintatieto = Some("HYLATTY"),
    ehdollisestiHyvaksytty = None,
    valintatiedonPvm = Some(LocalDate.parse("2024-06-11")),
    valintatapajonot = List(),
    vastaanottotieto = None,
    viimVastaanottopaiva = Some(LocalDate.parse("2024-06-26")),
    ensikertalainen = Some(false),
    ilmoittautuminen = None,
    pohjakoulutus = None,
    maksuvelvollisuus = Some("unreviewed"),
    hakemusmaksunTila = None,
    markkinointilupa = Some(false),
    sahkoinenViestintalupa = Some(true),
    lahiosoite = Some("Laholanaukio 834"),
    postinumero = Some("15700"),
    postitoimipaikka = Some("LAHTI"),
    kotikunta = Map(En -> "Lahti", Fi -> "Lahti", Sv -> "Lahtis"),
    asuinmaa = Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland"),
    puhelinnumero = Some("050 64293345"),
    sahkoposti = Some("hakija-33919611@oph.fi"),
    arvosanat = Map("M" -> "M", "O" -> "L", "CA" -> "M", "EA" -> "M", "PS" -> "E", "YH" -> "E")
  )

  val kkHakijaLehtoWithValintatapajonot: KkHakija = kkHakijaLehto.copy(
    markkinointilupa = Some(true),
    valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1704199256878262657431481297336",
        valintatapajononNimi = "Todistusvalintajono ensikertalaisille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Et ole ensikertalainen hakija.",
          Fi -> "Et ole ensikertalainen hakija.",
          Sv -> "Du är inte en förstagångssökande."
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "17000468320548583779630214204232",
        valintatapajononNimi = "Koevalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Et osallistunut valintakokeeseen",
          Fi -> "Et osallistunut valintakokeeseen",
          Sv -> "Et osallistunut valintakokeeseen SV"
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1707384694164-3621431542682802084",
        valintatapajononNimi = "Todistusvalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Fi -> "Sinulla ei ole arvosanaa kemiasta tai arvosanasi ei ole kyllin hyvä.",
          Sv -> "Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt."
        )
      )
    )
  )

  val yokokeet = Vector(Koodi("A", Map(En -> "Äidinkielen koe, suomi", Fi -> "Äidinkielen koe", Sv -> "Provet i modersmålet, finska")),
    Koodi("BB", Map(En -> "Biologian koe", Fi -> "Biologian koe", Sv -> "Biologi provet")),
    Koodi("EA", Map(En -> "Englannin koe", Fi -> "Englannin koe", Sv -> "Engelska provet")),
    Koodi("I", Map(En -> "Äidinkielen koe, inarinsaame", Fi -> "Äidinkielen koe, inarinsaame", Sv -> "Provet i modersmålet, enaresamiska")),
    Koodi("M", Map(En -> "Matematiikan koe", Fi -> "Matematiikan koe", Sv -> "Matematikprovet")))

  def validateHeaders(sheet: XSSFSheet, rowIndex: Int = 0, expectedHeaders: List[String]): Unit = {
    val headerRow = sheet.getRow(rowIndex)
    expectedHeaders.zipWithIndex.foreach { case (expectedHeader, index) =>
      assert(headerRow.getCell(index).getStringCellValue == expectedHeader, s"Header at index $index does not match")
    }
  }

  def validateRow(sheet: XSSFSheet, rowIndex: Int, expectedValues: List[Any]): Unit = {
    val row = sheet.getRow(rowIndex)
    expectedValues.zipWithIndex.foreach { case (expectedValue, index) =>
      val cell = row.getCell(index)
      expectedValue match {
        case value: String => assert(cell.getStringCellValue == value, s"Value at index $index does not match")
        case value: Int => assert(cell.getNumericCellValue == value, s"Value at index $index does not match")
        case value: Double => assert(cell.getNumericCellValue == value, s"Value at index $index does not match")
        case null => assert(cell == null || cell.getStringCellValue.isEmpty, s"Value at index $index should be null")
        case _ => throw new IllegalArgumentException(s"Unsupported value type at index $index")
      }
    }
  }

  "writeKkHakijatRaportti" should "return excel with two result rows for kk-hakijat with hetu, postiosoite and arvosanat" in {
    val kkHakijatResult =
      Vector(
        kkHakijaRautiainen.copy(ehdollisestiHyvaksytty = Some(false)),
        kkHakijaLehto.copy(
          hakukelpoisuus = Some("eligible"),
          valintatieto = None,
          valintatiedonPvm = None,
          maksuvelvollisuus = None,
          puhelinnumero = None
        )
      )

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(true),
        maybeNaytaHetu = Some(true),
        maybeNaytaPostiosoite = Some(true),
        yokokeet = yokokeet,
        parametrit = hakijatParams
      )

    val expectedHeaders = List(
      "Sukunimi SV", "Etunimi SV", "Turvakielto SV", "raportti.hetu", "raportti.syntymaAika",
      "Kansalaisuudet SV", "raportti.oppijanumero", "raportti.hakemusOid", "Toimipiste SV",
      "Hakukohde SV", "Hakukelpoisuus SV", "raportti.prioriteetti", "Valintatieto SV",
      "raportti.ehdollisestiHyvaksytty", "Valintatiedon päivämäärä SV", "Vastaanottotieto SV",
      "raportti.viimVastaanottopaiva", "raportti.ensikertalainen", "raportti.ilmoittautuminen",
      "Pohjakoulutus SV", "raportti.maksuvelvollisuus", "raportti.hakemusmaksunTila",
      "LupaMark SV", "raportti.sahkoinenViestintalupa", "raportti.lahiosoite",
      "raportti.postinumero", "raportti.postitoimipaikka", "raportti.kotikunta", "raportti.asuinmaa",
      "Puhelinnumero SV", "raportti.sahkoposti", "A", "BB", "EA", "I", "M"
    )

    val expectedRows = List(
      List(
        "Rautiainen-Testi", "Dina Testi", "Nej", "120393-129E", "12.3.1993", "Finland",
        "1.2.246.562.24.30646006111", "1.2.246.562.11.00000000000002179045", "Toimipiste 1 sv",
        "Hakukohde 1 SV", "-", 2, "Hyvaksytty SV", "Nej", "13.6.2024", "raportti.perunut",
        "26.6.2024", "Ja", "raportti.lasna_koko_lukuvuosi", """["pohjakoulutus_yo", "pohjakoulutus_kk"]""",
        "Ei velvollinen", "raportti.overdue", "Ja", "Ja", "Rämsöönranta 368", "00100", "HELSINKI",
        "Helsingfors", "Finland", "050 64292261", "hakija-33919666@oph.fi",
        "E", "M", "C", "-", "E"
      ),
      List(
        "Lehto-Testi", "Vikke Testi", "Ja", "04041990-345K", "4.4.1990", "Finland",
        "1.2.246.562.24.18441866015", "1.2.246.562.11.00000000000002126102", "Toimipiste 2 sv",
        "Hakukohde 2 SV", "hakukelpoinen SV", 1, "-", "-", "-", "-",
        "26.6.2024", "Nej", "-", "-", "-", "-", "Nej", "Ja",
        "Laholanaukio 834", "15700", "LAHTI", "Lahtis", "Finland", "-", "hakija-33919611@oph.fi",
        "-", "-", "M", "-", "M"
      )
    )

    assert(wb.getNumberOfSheets == 3)
    assert(wb.getSheetAt(0).getRow(1) != null)
    val sheet = wb.getSheetAt(0)

    validateHeaders(sheet = sheet, expectedHeaders = expectedHeaders)
    expectedRows.zipWithIndex.foreach { case (expectedRow, rowIndex) =>
      validateRow(sheet, rowIndex + 1, expectedRow)
    }

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 3)
    assert(wb.getSheetAt(0).getRow(3) == null)
  }

  it should "return excel with two result rows for kk-hakijat without hetu and postiosoite and with arvosanat" in {
    val kkHakijatResult =
      Vector(
        kkHakijaRautiainen.copy(
          hakukelpoisuus = Some("conditionally-eligible"),
          valintatiedonPvm = Some(LocalDate.parse("2024-06-05")),
          maksuvelvollisuus = Some("obligated")
        ),
        kkHakijaLehto.copy(
          markkinointilupa = Some(true),
          puhelinnumero = Some("050 64293345"),
          sahkoposti = None
        )
      )

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(true),
        maybeNaytaHetu = Some(false),
        maybeNaytaPostiosoite = Some(false),
        yokokeet = yokokeet,
        parametrit = hakijatParams
      )

    assert(wb.getNumberOfSheets == 3)
    assert(wb.getSheetAt(0).getRow(1) != null)

    val expectedHeaders = List(
      "Sukunimi SV", "Etunimi SV", "Turvakielto SV", "raportti.syntymaAika",
      "Kansalaisuudet SV", "raportti.oppijanumero", "raportti.hakemusOid", "Toimipiste SV",
      "Hakukohde SV", "Hakukelpoisuus SV", "raportti.prioriteetti", "Valintatieto SV",
      "raportti.ehdollisestiHyvaksytty", "Valintatiedon päivämäärä SV", "Vastaanottotieto SV",
      "raportti.viimVastaanottopaiva", "raportti.ensikertalainen", "raportti.ilmoittautuminen",
      "Pohjakoulutus SV", "raportti.maksuvelvollisuus", "raportti.hakemusmaksunTila",
      "LupaMark SV", "raportti.sahkoinenViestintalupa", "raportti.kotikunta", "raportti.asuinmaa",
      "Puhelinnumero SV", "raportti.sahkoposti", "A", "BB", "EA", "I", "M"
    )

    val expectedRows = List(
      List(
        "Rautiainen-Testi", "Dina Testi", "Nej", "12.3.1993", "Finland",
        "1.2.246.562.24.30646006111", "1.2.246.562.11.00000000000002179045", "Toimipiste 1 sv",
        "Hakukohde 1 SV", "raportti.conditionally-eligible", 2, "Hyvaksytty SV", "Ja", "5.6.2024", "raportti.perunut",
        "26.6.2024", "Ja", "raportti.lasna_koko_lukuvuosi", """["pohjakoulutus_yo", "pohjakoulutus_kk"]""",
        "Velvollinen", "raportti.overdue", "Ja", "Ja",
        "Helsingfors", "Finland", "050 64292261", "hakija-33919666@oph.fi",
        "E", "M", "C", "-", "E"
      ),
      List(
        "Lehto-Testi", "Vikke Testi", "Ja", "4.4.1990", "Finland",
        "1.2.246.562.24.18441866015", "1.2.246.562.11.00000000000002126102", "Toimipiste 2 sv",
        "Hakukohde 2 SV", "raportti.uneligible", 1, "Hylatty SV", "-", "11.6.2024", "-",
        "26.6.2024", "Nej", "-", "-", "Tarkastamatta SV", "-", "Ja", "Ja",
        "Lahtis", "Finland", "050 64293345", "-",
        "-", "-", "M", "-", "M"
      )
    )

    val sheet = wb.getSheetAt(0)

    validateHeaders(sheet = sheet, expectedHeaders = expectedHeaders)
    expectedRows.zipWithIndex.foreach { case (expectedRow, rowIndex) =>
      validateRow(sheet, rowIndex + 1, expectedRow)
    }

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 3)
    assert(wb.getSheetAt(0).getRow(4) == null)
  }

  it should "return excel with one result row for kk-hakijat with hetu and arvosanat but without postiosoite" in {
    val kkHakijatResult =
      Vector(
        kkHakijaRautiainen.copy(
          valintatiedonPvm = None,
          pohjakoulutus = None,
          maksuvelvollisuus = None,
          kansalaisuudet = List(Map(En -> "Finland", Fi -> "Suomi", Sv -> "Finland"), Map(En -> "Sweden", Fi -> "Ruotsi", Sv -> "Sverige")),
          valintatapajonot = List(
            Valintatapajono(
              valintatapajonoOid = "1709304116443-4815957697640331820",
              valintatapajononNimi = "Lukiokoulutus",
              valinnanTila = "PERUUNTUNUT",
              valinnanTilanKuvaus = Map(
                En -> "Cancelled, accepted for a study place with higher priority",
                Fi -> "Peruuntunut, hyväksytty ylemmälle hakutoiveelle",
                Sv -> "Annullerad, godkänt till ansökningsmål med högre prioritet"
              )
            )
          )
        )
      )

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(true),
        maybeNaytaHetu = Some(true),
        maybeNaytaPostiosoite = Some(false),
        yokokeet = yokokeet,
        parametrit = hakijatParams
      )

    assert(wb.getNumberOfSheets == 3)
    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(0).getCell(0) == null)
    assert(wb.getSheetAt(0).getRow(0).getCell(15).getStringCellValue == "Valintatapajonokohtainen tila SV")

    val expectedHeaders = List(
      "Sukunimi SV", "Etunimi SV", "Turvakielto SV", "raportti.hetu", "raportti.syntymaAika",
      "Kansalaisuudet SV", "raportti.oppijanumero", "raportti.hakemusOid", "Toimipiste SV",
      "Hakukohde SV", "Hakukelpoisuus SV", "raportti.prioriteetti", "Valintatieto SV",
      "raportti.ehdollisestiHyvaksytty", "Valintatiedon päivämäärä SV", "Lukiokoulutus",
      "Vastaanottotieto SV", "raportti.viimVastaanottopaiva", "raportti.ensikertalainen",
      "raportti.ilmoittautuminen", "Pohjakoulutus SV", "raportti.maksuvelvollisuus", "raportti.hakemusmaksunTila",
      "LupaMark SV", "raportti.sahkoinenViestintalupa", "raportti.kotikunta", "raportti.asuinmaa",
      "Puhelinnumero SV", "raportti.sahkoposti", "A", "BB", "EA", "I", "M"
    )

    val expectedRows = List(
      List(
        "Rautiainen-Testi", "Dina Testi", "Nej", "120393-129E", "12.3.1993", "Finland, Sverige",
        "1.2.246.562.24.30646006111", "1.2.246.562.11.00000000000002179045", "Toimipiste 1 sv",
        "Hakukohde 1 SV", "-", 2, "Hyvaksytty SV", "Ja", "-", "Annullerad, godkänt till ansökningsmål med högre prioritet",
        "raportti.perunut", "26.6.2024", "Ja", "raportti.lasna_koko_lukuvuosi", "-",
        "-", "raportti.overdue", "Ja", "Ja",
        "Helsingfors", "Finland", "050 64292261", "hakija-33919666@oph.fi",
        "E", "M", "C", "-", "E"
      ),
    )

    val sheet = wb.getSheetAt(0)

    validateHeaders(sheet, 1, expectedHeaders)
    expectedRows.zip(1 to expectedRows.length).foreach { case (expectedRow, rowIndex) =>
      validateRow(sheet, rowIndex + 1, expectedRow)
    }

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 3)
    assert(wb.getSheetAt(0).getRow(4) == null)
  }

  it should "return excel with two result rows without hetu and with postiosoite and arvosanat for kk-hakijat that have several valintatapajono" in {
    val kkHakijatResult =
      Vector(
        kkHakijatRautiainenWithValintatapajonot,
        kkHakijaLehtoWithValintatapajonot
      )

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(true),
        maybeNaytaHetu = Some(false),
        maybeNaytaPostiosoite = Some(true),
        yokokeet = yokokeet,
        parametrit = hakijatParams
      )

    val expectedHeaders = List(
      "Sukunimi SV", "Etunimi SV", "Turvakielto SV", "raportti.syntymaAika",
      "Kansalaisuudet SV", "raportti.oppijanumero", "raportti.hakemusOid", "Toimipiste SV",
      "Hakukohde SV", "Hakukelpoisuus SV", "raportti.prioriteetti", "Valintatieto SV",
      "raportti.ehdollisestiHyvaksytty", "Valintatiedon päivämäärä SV",
      "Koevalintajono kaikille hakijoille", "Todistusvalinta (YO)",
      "Todistusvalintajono ensikertalaisille hakijoille", "Todistusvalintajono ensikertalaisille hakijoille",
      "Todistusvalintajono kaikille hakijoille", "Vastaanottotieto SV",
      "raportti.viimVastaanottopaiva", "raportti.ensikertalainen", "raportti.ilmoittautuminen",
      "Pohjakoulutus SV", "raportti.maksuvelvollisuus", "raportti.hakemusmaksunTila",
      "LupaMark SV", "raportti.sahkoinenViestintalupa", "raportti.lahiosoite",
      "raportti.postinumero", "raportti.postitoimipaikka", "raportti.kotikunta", "raportti.asuinmaa",
      "Puhelinnumero SV", "raportti.sahkoposti", "A", "BB", "EA", "I", "M"
    )

    val expectedRows = List(
      List(
        "Rautiainen-Testi", "Dina Testi", "Nej", "12.3.1993", "Finland",
        "1.2.246.562.24.30646006111", "1.2.246.562.11.00000000000002179045", "Toimipiste 1 sv",
        "Hakukohde 1 SV", "-", 2, "Hyvaksytty SV", "Ja", "-",
        // Koevalintajono kaikille hakijoille
        "-",
        // Todistusvalinta (YO)
        "Hyvaksytty SV",
        //Todistusvalintajono ensikertalaisille hakijoille
        "Hylatty SV, Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 2.",
        // Todistusvalintajono ensikertalaisille hakijoille
        "-",
        // Todistusvalintajono kaikille hakijoille
        "Hylatty SV, Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 1.",
        "raportti.perunut", "26.6.2024", "Ja", "raportti.lasna_koko_lukuvuosi", "-",
        "-", "raportti.overdue", "Ja", "Ja", "Rämsöönranta 368", "00100", "HELSINKI",
        "Helsingfors", "Finland", "050 64292261", "hakija-33919666@oph.fi",
        "E", "M", "C", "-", "E"
      ),
      List(
        "Lehto-Testi", "Vikke Testi", "Ja", "4.4.1990", "Finland",
        "1.2.246.562.24.18441866015", "1.2.246.562.11.00000000000002126102", "Toimipiste 2 sv",
        "Hakukohde 2 SV", "raportti.uneligible", 1, "Hylatty SV", "-",
        "11.6.2024",
        // Koevalintajono kaikille hakijoille
        "Hylatty SV, Et osallistunut valintakokeeseen SV",
        // Todistusvalinta (YO)
        "-",
        //Todistusvalintajono ensikertalaisille hakijoille - 1707384681516-2842344525807969324
        "-",
        // Todistusvalintajono ensikertalaisille hakijoille - 1704199256878262657431481297336
        "Hylatty SV, Du är inte en förstagångssökande.",
        // Todistusvalintajono kaikille hakijoille
        "Hylatty SV, Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt.",
        "-", "26.6.2024", "Nej", "-", "-", "Tarkastamatta SV", "-", "Ja", "Ja",
        "Laholanaukio 834", "15700", "LAHTI", "Lahtis", "Finland", "050 64293345", "hakija-33919611@oph.fi",
        "-", "-", "M", "-", "M"
      )
    )

    assert(wb.getNumberOfSheets == 3)
    assert(wb.getSheetAt(0).getRow(1) != null)
    val sheet = wb.getSheetAt(0)

    assert(wb.getSheetAt(0).getRow(0).getCell(0) == null)
    assert(wb.getSheetAt(0).getRow(0).getCell(14).getStringCellValue == "Valintatapajonokohtainen tila SV")

    validateHeaders(sheet, 1, expectedHeaders)
    expectedRows.zip(1 to expectedRows.length).foreach { case (expectedRow, rowIndex) =>
      validateRow(sheet, rowIndex + 1, expectedRow)
    }

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 4)
    assert(wb.getSheetAt(0).getRow(4) == null)
  }

  it should "return excel with arvosanat in their right place without hetu and postiosoite and several valintatapajono" in {
    val kkHakijatResult = Vector(kkHakijatRautiainenWithValintatapajonot, kkHakijaLehtoWithValintatapajonot)

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(true),
        maybeNaytaHetu = Some(false),
        maybeNaytaPostiosoite = Some(false),
        yokokeet = yokokeet,
        parametrit = hakijatParams
      )

    val expectedHeaders = List(
      "Sukunimi SV", "Etunimi SV", "Turvakielto SV", "raportti.syntymaAika",
      "Kansalaisuudet SV", "raportti.oppijanumero", "raportti.hakemusOid", "Toimipiste SV",
      "Hakukohde SV", "Hakukelpoisuus SV", "raportti.prioriteetti", "Valintatieto SV",
      "raportti.ehdollisestiHyvaksytty", "Valintatiedon päivämäärä SV",
      "Koevalintajono kaikille hakijoille", "Todistusvalinta (YO)",
      "Todistusvalintajono ensikertalaisille hakijoille", "Todistusvalintajono ensikertalaisille hakijoille",
      "Todistusvalintajono kaikille hakijoille", "Vastaanottotieto SV",
      "raportti.viimVastaanottopaiva", "raportti.ensikertalainen", "raportti.ilmoittautuminen",
      "Pohjakoulutus SV", "raportti.maksuvelvollisuus", "raportti.hakemusmaksunTila",
      "LupaMark SV", "raportti.sahkoinenViestintalupa", "raportti.kotikunta", "raportti.asuinmaa",
      "Puhelinnumero SV", "raportti.sahkoposti", "A", "BB", "EA", "I", "M"
    )

    val expectedRows = List(
      List(
        "Rautiainen-Testi", "Dina Testi", "Nej", "12.3.1993", "Finland",
        "1.2.246.562.24.30646006111", "1.2.246.562.11.00000000000002179045", "Toimipiste 1 sv",
        "Hakukohde 1 SV", "-", 2, "Hyvaksytty SV", "Ja", "-",
        // Koevalintajono kaikille hakijoille
        "-",
        // Todistusvalinta (YO)
        "Hyvaksytty SV",
        //Todistusvalintajono ensikertalaisille hakijoille
        "Hylatty SV, Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 2.",
        // Todistusvalintajono ensikertalaisille hakijoille
        "-",
        // Todistusvalintajono kaikille hakijoille
        "Hylatty SV, Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 1.",
        "raportti.perunut", "26.6.2024", "Ja", "raportti.lasna_koko_lukuvuosi", "-",
        "-", "raportti.overdue", "Ja", "Ja", "Helsingfors", "Finland",
        "050 64292261", "hakija-33919666@oph.fi",
        "E", "M", "C", "-", "E"
      ),
      List(
        "Lehto-Testi", "Vikke Testi", "Ja", "4.4.1990", "Finland",
        "1.2.246.562.24.18441866015", "1.2.246.562.11.00000000000002126102", "Toimipiste 2 sv",
        "Hakukohde 2 SV", "raportti.uneligible", 1, "Hylatty SV", "-",
        "11.6.2024",
        // Koevalintajono kaikille hakijoille
        "Hylatty SV, Et osallistunut valintakokeeseen SV",
        // Todistusvalinta (YO)
        "-",
        //Todistusvalintajono ensikertalaisille hakijoille - 1707384681516-2842344525807969324
        "-",
        // Todistusvalintajono ensikertalaisille hakijoille - 1704199256878262657431481297336
        "Hylatty SV, Du är inte en förstagångssökande.",
        // Todistusvalintajono kaikille hakijoille
        "Hylatty SV, Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt.",
        "-", "26.6.2024", "Nej", "-", "-", "Tarkastamatta SV", "-", "Ja", "Ja",
        "Lahtis", "Finland", "050 64293345", "hakija-33919611@oph.fi",
        "-", "-", "M", "-", "M"
      )
    )

    assert(wb.getNumberOfSheets == 3)
    assert(wb.getSheetAt(0).getRow(1) != null)

    val sheet = wb.getSheetAt(0)
    validateHeaders(sheet, 1, expectedHeaders)
    expectedRows.zip(1 to expectedRows.length).foreach { case (expectedRow, rowIndex) =>
      validateRow(sheet, rowIndex + 1, expectedRow)
    }

  }

  it should "return excel with yokokeet sheet when naytaYoArvosanat is true" in {
    val kkHakijatResult = Vector(kkHakijatRautiainenWithValintatapajonot, kkHakijaLehtoWithValintatapajonot)

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(true),
        maybeNaytaHetu = Some(false),
        maybeNaytaPostiosoite = Some(false),
        yokokeet = yokokeet,
        parametrit = hakijatParams
      )

    assert(wb.getNumberOfSheets == 3)
    assert(wb.getSheetAt(1).getRow(1) != null)

    assert(wb.getSheetAt(1).getRow(0).getCell(0).getStringCellValue == "raportti.yokoelyhenne")
    assert(wb.getSheetAt(1).getRow(0).getCell(1).getStringCellValue == "raportti.yokoeselite")
    assert(wb.getSheetAt(1).getRow(0).getCell(2) == null)
    assert(wb.getSheetAt(1).getRow(1).getCell(0).getStringCellValue == "A")
    assert(wb.getSheetAt(1).getRow(1).getCell(1).getStringCellValue == "Provet i modersmålet, finska")
    assert(wb.getSheetAt(1).getRow(2).getCell(0).getStringCellValue == "BB")
    assert(wb.getSheetAt(1).getRow(2).getCell(1).getStringCellValue == "Biologi provet")
    assert(wb.getSheetAt(1).getRow(3).getCell(0).getStringCellValue == "EA")
    assert(wb.getSheetAt(1).getRow(3).getCell(1).getStringCellValue == "Engelska provet")
    assert(wb.getSheetAt(1).getRow(4).getCell(0).getStringCellValue == "I")
    assert(wb.getSheetAt(1).getRow(4).getCell(1).getStringCellValue == "Provet i modersmålet, enaresamiska")
    assert(wb.getSheetAt(1).getRow(5).getCell(0).getStringCellValue == "M")
    assert(wb.getSheetAt(1).getRow(5).getCell(1).getStringCellValue == "Matematikprovet")

    assert(wb.getSheetAt(1).getPhysicalNumberOfRows == 6)
    assert(wb.getSheetAt(1).getRow(6) == null)
  }

  it should "create hakuparametrit sheet after yokokeet sheet" in {
    val hakuParams: List[(String, String | Boolean | List[String])] =
      List(
        "haku" -> List("1.2.246.562.29.00000000000000015722"),
        "oppilaitos" -> List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        "toimipiste" -> List("1.2.246.562.10.2781706420001"),
        "hakukohde" -> List("1.2.246.562.20.00000000000000059957"),
        "hakukohderyhma" -> List("1.2.246.562.28.28396122930"),
        "valintatieto" -> List("HYVAKSYTTY"),
        "vastaanottotieto" -> List("PERUUTETTU"),
        "kansalaisuus" -> List("1"),
        "markkinointilupa" -> true,
        "nayta-yo-arvosanat" -> true,
        "nayta-hetu" -> false,
        "nayta-postiosoite" -> false,
      )
    val hakijatQueryResult = Vector()
    val wb =
      ExcelWriter.writeToisenAsteenHakijatRaportti(
        hakijatQueryResult,
        userLng,
        translations,
        hakuParams,
      )

    val expectedHeaders = List("raportti.hakuehto", "raportti.hakuarvo")
    val expectedRows = List(
      List("Haku SV", "1.2.246.562.29.00000000000000015722"),
      List("Oppilaitos SV", "1.2.246.562.10.00000000001, 1.2.246.562.10.2781706420000"),
      List("Toimipiste SV", "1.2.246.562.10.2781706420001"),
      List("Hakukohde SV", "1.2.246.562.20.00000000000000059957"),
      List("Hakukohderyhma SV", "1.2.246.562.28.28396122930"),
      List("Valintatieto SV", "Hyvaksytty SV"),
      List("Vastaanottotieto SV", "Peruutettu SV"),
      List("Kansalaisuus SV", "raportti.kansalaisuus.suomi"),
      List("LupaMark SV", "Ja"),
      List("nayta-yo-arvosanat", "Ja"),
      List("nayta-hetu", "Nej"),
      List("nayta-postiosoite", "Nej")
    )
    assert(wb.getNumberOfSheets == 2)
    assert(wb.getSheetAt(1).getRow(1) != null)
    val sheet = wb.getSheetAt(1)

    validateHeaders(sheet = sheet, expectedHeaders = expectedHeaders)
    expectedRows.zipWithIndex.foreach { case (expectedRow, rowIndex) =>
      validateRow(sheet, rowIndex + 1, expectedRow)
    }
  }

  it should "return excel without arvosanat and yokokeet sheet and with parametrit sheet when nayta arvosanat is false" in {
    val kkHakijatResult = Vector(
      kkHakijatRautiainenWithValintatapajonot,
      kkHakijaLehtoWithValintatapajonot
    )

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(false),
        maybeNaytaHetu = Some(true),
        maybeNaytaPostiosoite = Some(true),
        yokokeet = Vector(),
        parametrit = hakijatParams
      )

    val expectedHeaders = List("raportti.hakuehto", "raportti.hakuarvo")
    val expectedRows = List(
      List("Haku SV", "1.2.246.562.29.00000000000000015722"),
      List("Oppilaitos SV", "1.2.246.562.10.00000000001"),
      List("Valintatieto SV", "Hyvaksytty SV"),
    )
    assert(wb.getNumberOfSheets == 2)
    assert(wb.getSheetAt(1).getRow(1) != null)
    val sheet = wb.getSheetAt(1)

    validateHeaders(sheet = sheet, expectedHeaders = expectedHeaders)
    expectedRows.zipWithIndex.foreach { case (expectedRow, rowIndex) =>
      validateRow(sheet, rowIndex + 1, expectedRow)
    }

    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(0).getCell(0) == null)
    assert(wb.getSheetAt(0).getRow(0).getCell(15).getStringCellValue == "Valintatapajonokohtainen tila SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Sukunimi SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "Etunimi SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "Turvakielto SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "raportti.hetu")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "raportti.syntymaAika")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "Kansalaisuudet SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(1).getCell(7).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(1).getCell(8).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(9).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(10).getStringCellValue == "Hakukelpoisuus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(11).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(1).getCell(12).getStringCellValue == "Valintatieto SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(13).getStringCellValue == "raportti.ehdollisestiHyvaksytty")
    assert(wb.getSheetAt(0).getRow(1).getCell(14).getStringCellValue == "Valintatiedon päivämäärä SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(15).getStringCellValue == "Koevalintajono kaikille hakijoille")
    assert(wb.getSheetAt(0).getRow(1).getCell(16).getStringCellValue == "Todistusvalinta (YO)")
    assert(
      wb.getSheetAt(0).getRow(1).getCell(17).getStringCellValue == "Todistusvalintajono ensikertalaisille hakijoille"
    )
    assert(
      wb.getSheetAt(0).getRow(1).getCell(18).getStringCellValue == "Todistusvalintajono ensikertalaisille hakijoille"
    )
    assert(wb.getSheetAt(0).getRow(1).getCell(19).getStringCellValue == "Todistusvalintajono kaikille hakijoille")
    assert(wb.getSheetAt(0).getRow(1).getCell(20).getStringCellValue == "Vastaanottotieto SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(21).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(1).getCell(22).getStringCellValue == "raportti.ensikertalainen")
    assert(wb.getSheetAt(0).getRow(1).getCell(23).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(1).getCell(24).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(25).getStringCellValue == "raportti.maksuvelvollisuus")
    assert(wb.getSheetAt(0).getRow(1).getCell(26).getStringCellValue == "raportti.hakemusmaksunTila")
    assert(wb.getSheetAt(0).getRow(1).getCell(27).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(28).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(1).getCell(29).getStringCellValue == "raportti.lahiosoite")
    assert(wb.getSheetAt(0).getRow(1).getCell(30).getStringCellValue == "raportti.postinumero")
    assert(wb.getSheetAt(0).getRow(1).getCell(31).getStringCellValue == "raportti.postitoimipaikka")
    assert(wb.getSheetAt(0).getRow(1).getCell(32).getStringCellValue == "raportti.kotikunta")
    assert(wb.getSheetAt(0).getRow(1).getCell(33).getStringCellValue == "raportti.asuinmaa")
    assert(wb.getSheetAt(0).getRow(1).getCell(34).getStringCellValue == "Puhelinnumero SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(35).getStringCellValue == "raportti.sahkoposti")
    assert(wb.getSheetAt(0).getRow(1).getCell(36) == null)

    assert(wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue == "Rautiainen-Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(1).getStringCellValue == "Dina Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(2).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(2).getCell(3).getStringCellValue == "120393-129E")
    assert(wb.getSheetAt(0).getRow(2).getCell(4).getStringCellValue == "12.3.1993")
    assert(wb.getSheetAt(0).getRow(2).getCell(5).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(6).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(2).getCell(7).getStringCellValue == "1.2.246.562.11.00000000000002179045")
    assert(wb.getSheetAt(0).getRow(2).getCell(8).getStringCellValue == "Toimipiste 1 sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(9).getStringCellValue == "Hakukohde 1 SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(10).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(11).getNumericCellValue == 2)
    assert(wb.getSheetAt(0).getRow(2).getCell(12).getStringCellValue == "Hyvaksytty SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(13).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(14).getStringCellValue == "-")
    // Valintatapajonosarakkeet alkaa
    // Koevalintajono kaikille hakijoille
    assert(wb.getSheetAt(0).getRow(2).getCell(15).getStringCellValue == "-")
    // Todistusvalinta (YO)
    assert(wb.getSheetAt(0).getRow(2).getCell(16).getStringCellValue == "Hyvaksytty SV")
    //Todistusvalintajono ensikertalaisille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(2)
        .getCell(17)
        .getStringCellValue == "Hylatty SV, Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 2."
    )
    // Todistusvalintajono ensikertalaisille hakijoille
    assert(
      wb.getSheetAt(0).getRow(2).getCell(18).getStringCellValue == "-"
    )
    // Todistusvalintajono kaikille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(2)
        .getCell(19)
        .getStringCellValue == "Hylatty SV, Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 1."
    )
    // Valintatapajonosarakkeet loppuu
    assert(wb.getSheetAt(0).getRow(2).getCell(20).getStringCellValue == "raportti.perunut")
    assert(wb.getSheetAt(0).getRow(2).getCell(21).getStringCellValue == "26.6.2024")
    assert(wb.getSheetAt(0).getRow(2).getCell(22).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(23).getStringCellValue == "raportti.lasna_koko_lukuvuosi")
    assert(wb.getSheetAt(0).getRow(2).getCell(24).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(25).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(26).getStringCellValue == "raportti.overdue")
    assert(wb.getSheetAt(0).getRow(2).getCell(27).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(28).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(29).getStringCellValue == "Rämsöönranta 368")
    assert(wb.getSheetAt(0).getRow(2).getCell(30).getStringCellValue == "00100")
    assert(wb.getSheetAt(0).getRow(2).getCell(31).getStringCellValue == "HELSINKI")
    assert(wb.getSheetAt(0).getRow(2).getCell(32).getStringCellValue == "Helsingfors")
    assert(wb.getSheetAt(0).getRow(2).getCell(33).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(34).getStringCellValue == "050 64292261")
    assert(wb.getSheetAt(0).getRow(2).getCell(35).getStringCellValue == "hakija-33919666@oph.fi")
    assert(wb.getSheetAt(0).getRow(2).getCell(36) == null)

    assert(wb.getSheetAt(0).getRow(3).getCell(0).getStringCellValue == "Lehto-Testi")
    assert(wb.getSheetAt(0).getRow(3).getCell(1).getStringCellValue == "Vikke Testi")
    assert(wb.getSheetAt(0).getRow(3).getCell(2).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(3).getStringCellValue == "04041990-345K")
    assert(wb.getSheetAt(0).getRow(3).getCell(4).getStringCellValue == "4.4.1990")
    assert(wb.getSheetAt(0).getRow(3).getCell(5).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(3).getCell(6).getStringCellValue == "1.2.246.562.24.18441866015")
    assert(wb.getSheetAt(0).getRow(3).getCell(7).getStringCellValue == "1.2.246.562.11.00000000000002126102")
    assert(wb.getSheetAt(0).getRow(3).getCell(8).getStringCellValue == "Toimipiste 2 sv")
    assert(wb.getSheetAt(0).getRow(3).getCell(9).getStringCellValue == "Hakukohde 2 SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(10).getStringCellValue == "raportti.uneligible")
    assert(wb.getSheetAt(0).getRow(3).getCell(11).getNumericCellValue == 1)
    assert(wb.getSheetAt(0).getRow(3).getCell(12).getStringCellValue == "Hylatty SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(13).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(14).getStringCellValue == "11.6.2024")
    // Valintatapajonosarakkeet alkaa
    // Koevalintajono kaikille hakijoille
    assert(wb.getSheetAt(0).getRow(3).getCell(15).getStringCellValue == "Hylatty SV, Et osallistunut valintakokeeseen SV")
    // Todistusvalinta (YO)
    assert(wb.getSheetAt(0).getRow(3).getCell(16).getStringCellValue == "-")
    //Todistusvalintajono ensikertalaisille hakijoille - 1707384681516-2842344525807969324
    assert(wb.getSheetAt(0).getRow(3).getCell(17).getStringCellValue == "-")
    // Todistusvalintajono ensikertalaisille hakijoille - 1704199256878262657431481297336
    assert(wb.getSheetAt(0).getRow(3).getCell(18).getStringCellValue == "Hylatty SV, Du är inte en förstagångssökande.")
    // Todistusvalintajono kaikille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(3)
        .getCell(19)
        .getStringCellValue == "Hylatty SV, Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt."
    )
    // Valintatapajonosarakkeet loppuu
    assert(wb.getSheetAt(0).getRow(3).getCell(20).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(21).getStringCellValue == "26.6.2024")
    assert(wb.getSheetAt(0).getRow(3).getCell(22).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(3).getCell(23).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(24).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(25).getStringCellValue == "Tarkastamatta SV")
    assert(wb.getSheetAt(0).getRow(3).getCell(26).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(3).getCell(27).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(28).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(3).getCell(29).getStringCellValue == "Laholanaukio 834")
    assert(wb.getSheetAt(0).getRow(3).getCell(30).getStringCellValue == "15700")
    assert(wb.getSheetAt(0).getRow(3).getCell(31).getStringCellValue == "LAHTI")
    assert(wb.getSheetAt(0).getRow(3).getCell(32).getStringCellValue == "Lahtis")
    assert(wb.getSheetAt(0).getRow(3).getCell(33).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(3).getCell(34).getStringCellValue == "050 64293345")
    assert(wb.getSheetAt(0).getRow(3).getCell(35).getStringCellValue == "hakija-33919611@oph.fi")
    assert(wb.getSheetAt(0).getRow(3).getCell(36) == null)

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 4)
    assert(wb.getSheetAt(0).getRow(4) == null)
  }

  it should "return excel without arvosanat in the heading and without yokokeet sheet when they don't exist" in {
    val kkHakijatResult = Vector(kkHakijatRautiainenWithValintatapajonot)

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = None,
        maybeNaytaHetu = Some(false),
        maybeNaytaPostiosoite = Some(false),
        yokokeet = Vector(),
        parametrit = hakijatParams
      )

    assert(wb.getNumberOfSheets == 2)
    assert(wb.getSheetAt(0).getRow(1) != null)

    assert(wb.getSheetAt(0).getRow(0).getCell(0) == null)
    assert(wb.getSheetAt(0).getRow(0).getCell(14).getStringCellValue == "Valintatapajonokohtainen tila SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue == "Sukunimi SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue == "Etunimi SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue == "Turvakielto SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue == "raportti.syntymaAika")
    assert(wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue == "Kansalaisuudet SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue == "raportti.oppijanumero")
    assert(wb.getSheetAt(0).getRow(1).getCell(6).getStringCellValue == "raportti.hakemusOid")
    assert(wb.getSheetAt(0).getRow(1).getCell(7).getStringCellValue == "Toimipiste SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(8).getStringCellValue == "Hakukohde SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(9).getStringCellValue == "Hakukelpoisuus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(10).getStringCellValue == "raportti.prioriteetti")
    assert(wb.getSheetAt(0).getRow(1).getCell(11).getStringCellValue == "Valintatieto SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(12).getStringCellValue == "raportti.ehdollisestiHyvaksytty")
    assert(wb.getSheetAt(0).getRow(1).getCell(13).getStringCellValue == "Valintatiedon päivämäärä SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(14).getStringCellValue == "Todistusvalinta (YO)")
    assert(
      wb.getSheetAt(0).getRow(1).getCell(15).getStringCellValue == "Todistusvalintajono ensikertalaisille hakijoille"
    )
    assert(wb.getSheetAt(0).getRow(1).getCell(16).getStringCellValue == "Todistusvalintajono kaikille hakijoille")
    assert(wb.getSheetAt(0).getRow(1).getCell(17).getStringCellValue == "Vastaanottotieto SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(18).getStringCellValue == "raportti.viimVastaanottopaiva")
    assert(wb.getSheetAt(0).getRow(1).getCell(19).getStringCellValue == "raportti.ensikertalainen")
    assert(wb.getSheetAt(0).getRow(1).getCell(20).getStringCellValue == "raportti.ilmoittautuminen")
    assert(wb.getSheetAt(0).getRow(1).getCell(21).getStringCellValue == "Pohjakoulutus SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(22).getStringCellValue == "raportti.maksuvelvollisuus")
    assert(wb.getSheetAt(0).getRow(1).getCell(23).getStringCellValue == "raportti.hakemusmaksunTila")
    assert(wb.getSheetAt(0).getRow(1).getCell(24).getStringCellValue == "LupaMark SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(25).getStringCellValue == "raportti.sahkoinenViestintalupa")
    assert(wb.getSheetAt(0).getRow(1).getCell(26).getStringCellValue == "raportti.kotikunta")
    assert(wb.getSheetAt(0).getRow(1).getCell(27).getStringCellValue == "raportti.asuinmaa")
    assert(wb.getSheetAt(0).getRow(1).getCell(28).getStringCellValue == "Puhelinnumero SV")
    assert(wb.getSheetAt(0).getRow(1).getCell(29).getStringCellValue == "raportti.sahkoposti")
    assert(wb.getSheetAt(0).getRow(1).getCell(30) == null)

    assert(wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue == "Rautiainen-Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(1).getStringCellValue == "Dina Testi")
    assert(wb.getSheetAt(0).getRow(2).getCell(2).getStringCellValue == "Nej")
    assert(wb.getSheetAt(0).getRow(2).getCell(3).getStringCellValue == "12.3.1993")
    assert(wb.getSheetAt(0).getRow(2).getCell(4).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(5).getStringCellValue == "1.2.246.562.24.30646006111")
    assert(wb.getSheetAt(0).getRow(2).getCell(6).getStringCellValue == "1.2.246.562.11.00000000000002179045")
    assert(wb.getSheetAt(0).getRow(2).getCell(7).getStringCellValue == "Toimipiste 1 sv")
    assert(wb.getSheetAt(0).getRow(2).getCell(8).getStringCellValue == "Hakukohde 1 SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(9).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(10).getNumericCellValue == 2)
    assert(wb.getSheetAt(0).getRow(2).getCell(11).getStringCellValue == "Hyvaksytty SV")
    assert(wb.getSheetAt(0).getRow(2).getCell(12).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(13).getStringCellValue == "-")
    // Valintatapajonosarakkeet alkaa
    // Todistusvalinta (YO)
    assert(wb.getSheetAt(0).getRow(2).getCell(14).getStringCellValue == "Hyvaksytty SV")
    //Todistusvalintajono ensikertalaisille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(2)
        .getCell(15)
        .getStringCellValue == "Hylatty SV, Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 2."
    )
    // Todistusvalintajono kaikille hakijoille
    assert(
      wb.getSheetAt(0)
        .getRow(2)
        .getCell(16)
        .getStringCellValue == "Hylatty SV, Du har inget vitsord i kemi, eller ditt vitsord i kemi är inte tillräckligt högt 1."
    )
    // Valintatapajonosarakkeet loppuu
    assert(wb.getSheetAt(0).getRow(2).getCell(17).getStringCellValue == "raportti.perunut")
    assert(wb.getSheetAt(0).getRow(2).getCell(18).getStringCellValue == "26.6.2024")
    assert(wb.getSheetAt(0).getRow(2).getCell(19).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(20).getStringCellValue == "raportti.lasna_koko_lukuvuosi")
    assert(wb.getSheetAt(0).getRow(2).getCell(21).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(22).getStringCellValue == "-")
    assert(wb.getSheetAt(0).getRow(2).getCell(23).getStringCellValue == "raportti.overdue")
    assert(wb.getSheetAt(0).getRow(2).getCell(24).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(25).getStringCellValue == "Ja")
    assert(wb.getSheetAt(0).getRow(2).getCell(26).getStringCellValue == "Helsingfors")
    assert(wb.getSheetAt(0).getRow(2).getCell(27).getStringCellValue == "Finland")
    assert(wb.getSheetAt(0).getRow(2).getCell(28).getStringCellValue == "050 64292261")
    assert(wb.getSheetAt(0).getRow(2).getCell(29).getStringCellValue == "hakija-33919666@oph.fi")
    assert(wb.getSheetAt(0).getRow(2).getCell(30) == null)
  }

  it should "not show prioriteetti if prioriteetti is negative (not in use)" in {
    val kkHakijatResult =
      Vector(
        kkHakijaRautiainen.copy(
          valintatiedonPvm = None,
          pohjakoulutus = None,
          maksuvelvollisuus = None,
          prioriteetti = -1,
          valintatapajonot = List(
            Valintatapajono(
              valintatapajonoOid = "1709304116443-4815957697640331820",
              valintatapajononNimi = "Lukiokoulutus",
              valinnanTila = "PERUUNTUNUT",
              valinnanTilanKuvaus = Map(
                En -> "Cancelled, accepted for a study place with higher priority",
                Fi -> "Peruuntunut, hyväksytty ylemmälle hakutoiveelle",
                Sv -> "Annullerad, godkänt till ansökningsmål med högre prioritet"
              )
            )
          )
        )
      )

    val wb =
      ExcelWriter.writeKkHakijatRaportti(
        hakijoidenHakutoiveet = kkHakijatResult,
        asiointikieli = userLng,
        translations = translations,
        maybeNaytaYoArvosanat = Some(true),
        maybeNaytaHetu = Some(true),
        maybeNaytaPostiosoite = Some(false),
        yokokeet = yokokeet,
        parametrit = hakijatParams
      )

    val expectedHeaders = List(
      "Sukunimi SV", "Etunimi SV", "Turvakielto SV", "raportti.hetu", "raportti.syntymaAika",
      "Kansalaisuudet SV", "raportti.oppijanumero", "raportti.hakemusOid", "Toimipiste SV",
      "Hakukohde SV", "Hakukelpoisuus SV", "raportti.prioriteetti", "Valintatieto SV",
      "raportti.ehdollisestiHyvaksytty", "Valintatiedon päivämäärä SV", "Lukiokoulutus",
      "Vastaanottotieto SV", "raportti.viimVastaanottopaiva", "raportti.ensikertalainen",
      "raportti.ilmoittautuminen", "Pohjakoulutus SV", "raportti.maksuvelvollisuus",
      "raportti.hakemusmaksunTila", "LupaMark SV", "raportti.sahkoinenViestintalupa",
      "raportti.kotikunta", "raportti.asuinmaa", "Puhelinnumero SV", "raportti.sahkoposti",
      "A", "BB", "EA", "I", "M"
    )

    val expectedRows = List(
      List(
        "Rautiainen-Testi", "Dina Testi", "Nej", "120393-129E", "12.3.1993", "Finland",
        "1.2.246.562.24.30646006111", "1.2.246.562.11.00000000000002179045", "Toimipiste 1 sv",
        "Hakukohde 1 SV", "-", "-", "Hyvaksytty SV", "Ja", "-",
        "Annullerad, godkänt till ansökningsmål med högre prioritet", "raportti.perunut",
        "26.6.2024", "Ja", "raportti.lasna_koko_lukuvuosi", "-",
        "-", "raportti.overdue", "Ja", "Ja",
        "Helsingfors", "Finland", "050 64292261", "hakija-33919666@oph.fi",
        "E", "M", "C", "-", "E"
      ),
    )

    assert(wb.getNumberOfSheets == 3)
    assert(wb.getSheetAt(0).getRow(1) != null)

    val sheet = wb.getSheetAt(0)
    validateHeaders(sheet, 1, expectedHeaders)
    expectedRows.zip(1 to expectedRows.length).foreach { case (expectedRow, rowIndex) =>
      validateRow(sheet, rowIndex + 1, expectedRow)
    }

    assert(wb.getSheetAt(0).getPhysicalNumberOfRows == 3)
    assert(wb.getSheetAt(0).getRow(4) == null)
  }

  val hakeneetHyvaksytytVastaanottaneetParams: List[(String, String | Boolean | List[String])] =
    List(
      "haku" -> List("1.2.246.562.29.00000000000000015722"),
      "oppilaitos" -> List("1.2.246.562.10.52251087186"),
      "toimipiste" -> List("1.2.246.562.10.23017513880"),
      "tulostustapa" -> "toimipisteittain"
    )
  "writeHakeneetHyvaksytytVastaanottaneetRaportti" should "return excel with three heading columns, three result rows and two summary rows for hakukohteittain" in {
    val data = List(
      HakeneetHyvaksytytVastaanottaneetHakukohteittain(
        hakukohdeNimi = Map(
          En -> "Ajoneuvoalan perustutkinto",
          Fi -> "Ajoneuvoalan perustutkinto",
          Sv -> "Grundexamen inom fordonsbranschen"
        ),
        hakuNimi = Map(
          En -> "Joint application to upper secondary education and preparatory education 2024",
          Fi -> "Perusopetuksen jälkeisen koulutuksen yhteishaku 2024",
          Sv -> "Gemensam ansökan till utbildning efter den grundläggande utbildningen 2024"
        ),
        organisaatioNimi = Map(
          En -> "OSAO, Haukiputaan yksikkö",
          Fi -> "OSAO, Haukiputaan yksikkö",
          Sv -> "OSAO, Haukiputaan yksikkö"
        ),
        hakijat = 354,
        ensisijaisia = 95,
        varasija = 354,
        hyvaksytyt = 100,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        aloituspaikat = 100,
        toive1 = 95,
        toive2 = 91,
        toive3 = 76,
        toive4 = 45,
        toive5 = 25,
        toive6 = 11,
        toive7 = 11
      ),
      HakeneetHyvaksytytVastaanottaneetHakukohteittain(
        hakukohdeNimi = Map(
          En -> "Ajoneuvoalan perustutkinto",
          Fi -> "Ajoneuvoalan perustutkinto",
          Sv -> "Grundexamen inom fordonsbranschen"
        ),
        hakuNimi = Map(
          En -> "Joint application to upper secondary education and preparatory education 2024",
          Fi -> "Perusopetuksen jälkeisen koulutuksen yhteishaku 2024",
          Sv -> "Gemensam ansökan till utbildning efter den grundläggande utbildningen 2024"
        ),
        organisaatioNimi = Map(
          En -> "OSAO, Muhoksen yksikkö",
          Fi -> "OSAO, Muhoksen yksikkö",
          Sv -> "OSAO, Muhoksen yksikkö"
        ),
        hakijat = 148,
        ensisijaisia = 39,
        varasija = 148,
        hyvaksytyt = 16,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        aloituspaikat = 24,
        toive1 = 39,
        toive2 = 29,
        toive3 = 33,
        toive4 = 14,
        toive5 = 12,
        toive6 = 14,
        toive7 = 7
      ),
      HakeneetHyvaksytytVastaanottaneetHakukohteittain(
        hakukohdeNimi = Map(
          En -> "Elintarvikealan perustutkinto",
          Fi -> "Elintarvikealan perustutkinto",
          Sv -> "Grundexamen inom livsmedelsbranschen"
        ),
        hakuNimi = Map(
          En -> "Joint application to upper secondary education and preparatory education 2024",
          Fi -> "Perusopetuksen jälkeisen koulutuksen yhteishaku 2024",
          Sv -> "Gemensam ansökan till utbildning efter den grundläggande utbildningen 2024"
        ),
        organisaatioNimi = Map(
          En -> "OSAO, Kaukovainion yksikkö, palvelut",
          Fi -> "OSAO, Kaukovainion yksikkö, palvelut",
          Sv -> "OSAO, Kaukovainion yksikkö, palvelut"
        ),
        hakijat = 112,
        ensisijaisia = 25,
        varasija = 112,
        hyvaksytyt = 17,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        aloituspaikat = 40,
        toive1 = 25,
        toive2 = 29,
        toive3 = 21,
        toive4 = 15,
        toive5 = 11,
        toive6 = 6,
        toive7 = 5
      )
    )

    val workbook = ExcelWriter.writeHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 450,
      naytaHakutoiveet = true,
      tulostustapa = "hakukohteittain",
      parametrit = hakeneetHyvaksytytVastaanottaneetParams
    )

    assert(workbook.getNumberOfSheets == 2)
    val sheet = workbook.getSheetAt(0)
    // Otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Hakukohde SV")
    assert(headingRow.getCell(1).getStringCellValue == "Organisaatio SV")
    assert(headingRow.getCell(2).getStringCellValue == "Haku SV")
    assert(headingRow.getCell(3).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(4).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(5).getStringCellValue == "Varasija SV")
    assert(headingRow.getCell(6).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(7).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(8).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(9).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(10).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(11).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getCell(12).getStringCellValue == "Toive1 SV")
    assert(headingRow.getCell(13).getStringCellValue == "Toive2 SV")
    assert(headingRow.getCell(14).getStringCellValue == "Toive3 SV")
    assert(headingRow.getCell(15).getStringCellValue == "Toive4 SV")
    assert(headingRow.getCell(16).getStringCellValue == "Toive5 SV")
    assert(headingRow.getCell(17).getStringCellValue == "Toive6 SV")
    assert(headingRow.getCell(18).getStringCellValue == "Toive7 SV")
    assert(headingRow.getCell(19) == null)

    // Datarivit
    assert(
      sheet.getRow(1).getCell(0).getStringCellValue == "Grundexamen inom fordonsbranschen"
    )
    assert(sheet.getRow(1).getCell(1).getStringCellValue == "OSAO, Haukiputaan yksikkö")
    assert(
      sheet.getRow(1).getCell(2).getStringCellValue == "Gemensam ansökan till utbildning efter den grundläggande utbildningen 2024"
    )
    assert(sheet.getRow(1).getCell(3).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(4).getNumericCellValue == 95)
    assert(sheet.getRow(1).getCell(5).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(6).getNumericCellValue == 100)
    assert(sheet.getRow(1).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(8).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(9).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(10).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(11).getNumericCellValue == 100)
    assert(sheet.getRow(1).getCell(12).getNumericCellValue == 95)
    assert(sheet.getRow(1).getCell(13).getNumericCellValue == 91)
    assert(sheet.getRow(1).getCell(14).getNumericCellValue == 76)
    assert(sheet.getRow(1).getCell(15).getNumericCellValue == 45)
    assert(sheet.getRow(1).getCell(16).getNumericCellValue == 25)
    assert(sheet.getRow(1).getCell(17).getNumericCellValue == 11)
    assert(sheet.getRow(1).getCell(18).getNumericCellValue == 11)
    assert(sheet.getRow(1).getCell(19) == null)

    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Grundexamen inom fordonsbranschen")
    assert(sheet.getRow(2).getCell(1).getStringCellValue == "OSAO, Muhoksen yksikkö")
    assert(
      sheet.getRow(2).getCell(2).getStringCellValue == "Gemensam ansökan till utbildning efter den grundläggande utbildningen 2024"
    )
    assert(sheet.getRow(2).getCell(3).getNumericCellValue == 148)
    assert(sheet.getRow(2).getCell(4).getNumericCellValue == 39)
    assert(sheet.getRow(2).getCell(5).getNumericCellValue == 148)
    assert(sheet.getRow(2).getCell(6).getNumericCellValue == 16)
    assert(sheet.getRow(2).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(2).getCell(8).getNumericCellValue == 0)
    assert(sheet.getRow(2).getCell(9).getNumericCellValue == 0)
    assert(sheet.getRow(2).getCell(10).getNumericCellValue == 0)
    assert(sheet.getRow(2).getCell(11).getNumericCellValue == 24)
    assert(sheet.getRow(2).getCell(12).getNumericCellValue == 39)
    assert(sheet.getRow(2).getCell(13).getNumericCellValue == 29)
    assert(sheet.getRow(2).getCell(14).getNumericCellValue == 33)
    assert(sheet.getRow(2).getCell(15).getNumericCellValue == 14)
    assert(sheet.getRow(2).getCell(16).getNumericCellValue == 12)
    assert(sheet.getRow(2).getCell(17).getNumericCellValue == 14)
    assert(sheet.getRow(2).getCell(18).getNumericCellValue == 7)
    assert(sheet.getRow(2).getCell(19) == null)

    assert(sheet.getRow(3).getCell(0).getStringCellValue == "Grundexamen inom livsmedelsbranschen")
    assert(
      sheet.getRow(3).getCell(1).getStringCellValue == "OSAO, Kaukovainion yksikkö, palvelut"
    )
    assert(
      sheet.getRow(3).getCell(2).getStringCellValue == "Gemensam ansökan till utbildning efter den grundläggande utbildningen 2024"
    )
    assert(sheet.getRow(3).getCell(3).getNumericCellValue == 112)
    assert(sheet.getRow(3).getCell(4).getNumericCellValue == 25)
    assert(sheet.getRow(3).getCell(5).getNumericCellValue == 112)
    assert(sheet.getRow(3).getCell(6).getNumericCellValue == 17)
    assert(sheet.getRow(3).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(3).getCell(8).getNumericCellValue == 0)
    assert(sheet.getRow(3).getCell(9).getNumericCellValue == 0)
    assert(sheet.getRow(3).getCell(10).getNumericCellValue == 0)
    assert(sheet.getRow(3).getCell(11).getNumericCellValue == 40)
    assert(sheet.getRow(3).getCell(12).getNumericCellValue == 25)
    assert(sheet.getRow(3).getCell(13).getNumericCellValue == 29)
    assert(sheet.getRow(3).getCell(14).getNumericCellValue == 21)
    assert(sheet.getRow(3).getCell(15).getNumericCellValue == 15)
    assert(sheet.getRow(3).getCell(16).getNumericCellValue == 11)
    assert(sheet.getRow(3).getCell(17).getNumericCellValue == 6)
    assert(sheet.getRow(3).getCell(18).getNumericCellValue == 5)
    assert(sheet.getRow(3).getCell(19) == null)

    // summarivit
    assert(sheet.getRow(4).getCell(0).getStringCellValue == "")
    assert(sheet.getRow(4).getCell(1).getStringCellValue == "")
    assert(sheet.getRow(4).getCell(2).getStringCellValue == "Yhteensä SV")
    assert(sheet.getRow(4).getCell(3).getNumericCellValue == 614)
    assert(sheet.getRow(4).getCell(0).getStringCellValue == "")
    assert(sheet.getRow(4).getCell(1).getStringCellValue == "")
    assert(sheet.getRow(5).getCell(2).getStringCellValue == "Yksittäiset hakijat SV")
    assert(sheet.getRow(5).getCell(3).getNumericCellValue == 450)
    assert(sheet.getPhysicalNumberOfRows == 6)

  }

  it should "create hakuparametrit sheet" in {
    val hakuParams: List[(String, String | Boolean | List[String])] =
      List(
        "haku" -> List("1.2.246.562.29.00000000000000015722"),
        "tulostustapa" -> "toimipisteittain",
        "oppilaitos" -> List("1.2.246.562.10.00000000001", "1.2.246.562.10.2781706420000"),
        "toimipiste" -> List("1.2.246.562.10.2781706420001"),
        "hakukohde" -> List("1.2.246.562.20.00000000000000059957"),
        "opetuskieli" -> List("1", "4"),
        "koulutusala1" -> List("10"),
        "koulutusala2" -> List("092"),
        "koulutusala3" -> List("1018"),
        "maakunta" -> List("01"),
        "kunta" -> List("091"),
        "harkinnanvaraisuus" -> List("OPPIMISVAIKEUDET"),
        "sukupuoli" -> List("2"),
        "nayta-hakutoiveet" -> true,
      )
    val data = List.empty
    val wb = ExcelWriter.writeHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 0,
      naytaHakutoiveet = true,
      tulostustapa = "toimipisteittain",
      parametrit = hakuParams
    )

    val expectedHeaders = List("raportti.hakuehto", "raportti.hakuarvo")
    val expectedRows = List(
      List("Haku SV", "1.2.246.562.29.00000000000000015722"),
      List("Tulostustapa SV", "toimipisteittain"),
      List("Oppilaitos SV", "1.2.246.562.10.00000000001, 1.2.246.562.10.2781706420000"),
      List("Toimipiste SV", "1.2.246.562.10.2781706420001"),
      List("Hakukohde SV", "1.2.246.562.20.00000000000000059957"),
      List("Opetuskieli SV", "1, 4"),
      List("Koulutusala 1 SV", "10"),
      List("Koulutusala 2 SV", "092"),
      List("Koulutusala 3 SV", "1018"),
      List("maakunta", "01"),
      List("kunta", "091"),
      List("Harkinnanvaraisuus SV", "Oppimisvaikeudet SV"),
      List("sukupuoli", "2"),
      List("nayta-hakutoiveet", "Ja")
    )
    assert(wb.getNumberOfSheets == 2)
    assert(wb.getSheetAt(1).getRow(1) != null)
    val sheet = wb.getSheetAt(1)

    validateHeaders(sheet = sheet, expectedHeaders = expectedHeaders)
    expectedRows.zipWithIndex.foreach { case (expectedRow, rowIndex) =>
      validateRow(sheet, rowIndex + 1, expectedRow)
    }
  }

  it should "return excel with one heading column, three result rows and two summary rows for toimipisteittäin" in {
    val data = List(
      HakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "OSAO, Haukiputaan yksikkö",
          Fi -> "OSAO, Haukiputaan yksikkö",
          Sv -> "OSAO, Haukiputaan yksikkö"
        ),
        hakijat = 354,
        ensisijaisia = 95,
        varasija = 354,
        hyvaksytyt = 100,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        aloituspaikat = 100,
        toive1 = 95,
        toive2 = 91,
        toive3 = 76,
        toive4 = 45,
        toive5 = 25,
        toive6 = 11,
        toive7 = 11
      ),
      HakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "OSAO, Muhoksen yksikkö",
          Fi -> "OSAO, Muhoksen yksikkö",
          Sv -> "OSAO, Muhoksen yksikkö"
        ),
        hakijat = 148,
        ensisijaisia = 39,
        varasija = 148,
        hyvaksytyt = 16,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        aloituspaikat = 24,
        toive1 = 39,
        toive2 = 29,
        toive3 = 33,
        toive4 = 14,
        toive5 = 12,
        toive6 = 14,
        toive7 = 7
      ),
      HakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "OSAO, Kaukovainion yksikkö, palvelut",
          Fi -> "OSAO, Kaukovainion yksikkö, palvelut",
          Sv -> "OSAO, Kaukovainion yksikkö, palvelut"
        ),
        hakijat = 112,
        ensisijaisia = 25,
        varasija = 112,
        hyvaksytyt = 17,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        aloituspaikat = 40,
        toive1 = 25,
        toive2 = 29,
        toive3 = 21,
        toive4 = 15,
        toive5 = 11,
        toive6 = 6,
        toive7 = 5
      )
    )

    val workbook = ExcelWriter.writeHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 450,
      naytaHakutoiveet = true,
      tulostustapa = "toimipisteittain",
      parametrit = hakeneetHyvaksytytVastaanottaneetParams
    )

    assert(workbook.getNumberOfSheets == 2)
    val sheet = workbook.getSheetAt(0)
    // Otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Toimipiste SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Varasija SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getCell(10).getStringCellValue == "Toive1 SV")
    assert(headingRow.getCell(11).getStringCellValue == "Toive2 SV")
    assert(headingRow.getCell(12).getStringCellValue == "Toive3 SV")
    assert(headingRow.getCell(13).getStringCellValue == "Toive4 SV")
    assert(headingRow.getCell(14).getStringCellValue == "Toive5 SV")
    assert(headingRow.getCell(15).getStringCellValue == "Toive6 SV")
    assert(headingRow.getCell(16).getStringCellValue == "Toive7 SV")
    assert(headingRow.getCell(17) == null)

    // Datarivit
    assert(
      sheet.getRow(1).getCell(0).getStringCellValue == "OSAO, Haukiputaan yksikkö"
    )
    assert(sheet.getRow(1).getCell(1).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(2).getNumericCellValue == 95)
    assert(sheet.getRow(1).getCell(3).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(4).getNumericCellValue == 100)
    assert(sheet.getRow(1).getCell(5).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(6).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(8).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(9).getNumericCellValue == 100)
    assert(sheet.getRow(1).getCell(10).getNumericCellValue == 95)
    assert(sheet.getRow(1).getCell(11).getNumericCellValue == 91)
    assert(sheet.getRow(1).getCell(12).getNumericCellValue == 76)
    assert(sheet.getRow(1).getCell(13).getNumericCellValue == 45)
    assert(sheet.getRow(1).getCell(14).getNumericCellValue == 25)
    assert(sheet.getRow(1).getCell(15).getNumericCellValue == 11)
    assert(sheet.getRow(1).getCell(16).getNumericCellValue == 11)
    assert(sheet.getRow(1).getCell(17) == null)

    assert(sheet.getRow(2).getCell(0).getStringCellValue == "OSAO, Muhoksen yksikkö")
    assert(sheet.getRow(2).getCell(1).getNumericCellValue == 148)
    assert(sheet.getRow(2).getCell(2).getNumericCellValue == 39)
    assert(sheet.getRow(2).getCell(3).getNumericCellValue == 148)
    assert(sheet.getRow(2).getCell(4).getNumericCellValue == 16)
    assert(sheet.getRow(2).getCell(5).getNumericCellValue == 0)
    assert(sheet.getRow(2).getCell(6).getNumericCellValue == 0)
    assert(sheet.getRow(2).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(2).getCell(8).getNumericCellValue == 0)
    assert(sheet.getRow(2).getCell(9).getNumericCellValue == 24)
    assert(sheet.getRow(2).getCell(10).getNumericCellValue == 39)
    assert(sheet.getRow(2).getCell(11).getNumericCellValue == 29)
    assert(sheet.getRow(2).getCell(12).getNumericCellValue == 33)
    assert(sheet.getRow(2).getCell(13).getNumericCellValue == 14)
    assert(sheet.getRow(2).getCell(14).getNumericCellValue == 12)
    assert(sheet.getRow(2).getCell(15).getNumericCellValue == 14)
    assert(sheet.getRow(2).getCell(16).getNumericCellValue == 7)
    assert(sheet.getRow(2).getCell(17) == null)

    assert(
      sheet
        .getRow(3)
        .getCell(0)
        .getStringCellValue == "OSAO, Kaukovainion yksikkö, palvelut"
    )
    assert(sheet.getRow(3).getCell(1).getNumericCellValue == 112)
    assert(sheet.getRow(3).getCell(2).getNumericCellValue == 25)
    assert(sheet.getRow(3).getCell(3).getNumericCellValue == 112)
    assert(sheet.getRow(3).getCell(4).getNumericCellValue == 17)
    assert(sheet.getRow(3).getCell(5).getNumericCellValue == 0)
    assert(sheet.getRow(3).getCell(6).getNumericCellValue == 0)
    assert(sheet.getRow(3).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(3).getCell(8).getNumericCellValue == 0)
    assert(sheet.getRow(3).getCell(9).getNumericCellValue == 40)
    assert(sheet.getRow(3).getCell(10).getNumericCellValue == 25)
    assert(sheet.getRow(3).getCell(11).getNumericCellValue == 29)
    assert(sheet.getRow(3).getCell(12).getNumericCellValue == 21)
    assert(sheet.getRow(3).getCell(13).getNumericCellValue == 15)
    assert(sheet.getRow(3).getCell(14).getNumericCellValue == 11)
    assert(sheet.getRow(3).getCell(15).getNumericCellValue == 6)
    assert(sheet.getRow(3).getCell(16).getNumericCellValue == 5)
    assert(sheet.getRow(3).getCell(17) == null)

    // summarivit
    assert(sheet.getRow(4).getCell(0).getStringCellValue == "Yhteensä SV")
    assert(sheet.getRow(4).getCell(1).getNumericCellValue == 614)
    assert(sheet.getRow(5).getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(sheet.getRow(5).getCell(1).getNumericCellValue == 450)
    assert(sheet.getPhysicalNumberOfRows == 6)

  }

  it should "return excel with correct tulostustapa heading" in {
    val data = List(
      HakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "OSAO, Haukiputaan yksikkö",
          Fi -> "OSAO, Haukiputaan yksikkö",
          Sv -> "OSAO, Haukiputaan yksikkö"
        ),
        hakijat = 354,
        ensisijaisia = 95,
        varasija = 354,
        hyvaksytyt = 100,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        aloituspaikat = 100,
        toive1 = 95,
        toive2 = 91,
        toive3 = 76,
        toive4 = 45,
        toive5 = 25,
        toive6 = 11,
        toive7 = 11
      )
    )

    val workbook = ExcelWriter.writeHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 300,
      naytaHakutoiveet = false,
      tulostustapa = "oppilaitoksittain",
      parametrit = hakeneetHyvaksytytVastaanottaneetParams
    )

    assert(workbook.getNumberOfSheets == 2)
    val sheet = workbook.getSheetAt(0)
    // Otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Oppilaitos SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Varasija SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getLastCellNum == 10)
    assert(headingRow.getCell(10) == null)

    // Datarivit
    assert(sheet.getRow(1).getCell(0).getStringCellValue == "OSAO, Haukiputaan yksikkö")
    assert(sheet.getRow(1).getCell(1).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(2).getNumericCellValue == 95)
    assert(sheet.getRow(1).getCell(3).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(4).getNumericCellValue == 100)
    assert(sheet.getRow(1).getCell(5).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(6).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(8).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(9).getNumericCellValue == 100)

    assert(sheet.getRow(1).getCell(10) == null)
    // summarivit
    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Yhteensä SV")
    assert(sheet.getRow(2).getCell(1).getNumericCellValue == 354)
    assert(sheet.getRow(3).getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(sheet.getRow(3).getCell(1).getNumericCellValue == 300)
    assert(sheet.getPhysicalNumberOfRows == 4)

  }

  it should "return excel without Toive sarakkeet if naytaHakutoiveet is false" in {
    val data = List(
      HakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "Ajoneuvoalan perustutkinto\nOSAO, Haukiputaan yksikkö",
          Fi -> "Ajoneuvoalan perustutkinto\nOSAO, Haukiputaan yksikkö",
          Sv -> "Grundexamen inom fordonsbranschen\nOSAO, Haukiputaan yksikkö"
        ),
        hakijat = 354,
        ensisijaisia = 95,
        varasija = 354,
        hyvaksytyt = 100,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        aloituspaikat = 100,
        toive1 = 95,
        toive2 = 91,
        toive3 = 76,
        toive4 = 45,
        toive5 = 25,
        toive6 = 11,
        toive7 = 11
      )
    )

    val workbook = ExcelWriter.writeHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 300,
      naytaHakutoiveet = false,
      tulostustapa = "toimipisteittain",
      parametrit = hakeneetHyvaksytytVastaanottaneetParams
    )

    assert(workbook.getNumberOfSheets == 2)
    val sheet = workbook.getSheetAt(0)
    // Otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Toimipiste SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Varasija SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getLastCellNum == 10)
    assert(headingRow.getCell(10) == null)

    // Datarivit
    assert(
      sheet.getRow(1).getCell(0).getStringCellValue == "Grundexamen inom fordonsbranschen\nOSAO, Haukiputaan yksikkö"
    )
    assert(sheet.getRow(1).getCell(1).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(2).getNumericCellValue == 95)
    assert(sheet.getRow(1).getCell(3).getNumericCellValue == 354)
    assert(sheet.getRow(1).getCell(4).getNumericCellValue == 100)
    assert(sheet.getRow(1).getCell(5).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(6).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(7).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(8).getNumericCellValue == 0)
    assert(sheet.getRow(1).getCell(9).getNumericCellValue == 100)
    assert(sheet.getRow(1).getCell(10) == null)
    // summarivit
    assert(sheet.getRow(2).getCell(0).getStringCellValue == "Yhteensä SV")
    assert(sheet.getRow(2).getCell(1).getNumericCellValue == 354)
    assert(sheet.getRow(3).getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(sheet.getRow(3).getCell(1).getNumericCellValue == 300)
    assert(sheet.getPhysicalNumberOfRows == 4)

  }

  "getHeadingFieldNames" should "return all field names except valintatapajonot and arvosanat for kk-hakija because valintatapajonot and arvosanat are empty" in {
    assert(
      ExcelWriter.getHeadingFieldNames(true, true, true, List(), List()) ==
        List(
          "hakijanSukunimi",
          "hakijanEtunimi",
          "turvakielto",
          "hetu",
          "syntymaAika",
          "kansalaisuudet",
          "oppijanumero",
          "hakemusOid",
          "toimipiste",
          "hakukohteenNimi",
          "hakukelpoisuus",
          "prioriteetti",
          "valintatieto",
          "ehdollisestiHyvaksytty",
          "valintatiedonPvm",
          "vastaanottotieto",
          "viimVastaanottopaiva",
          "ensikertalainen",
          "ilmoittautuminen",
          "pohjakoulutus",
          "maksuvelvollisuus",
          "hakemusmaksunTila",
          "markkinointilupa",
          "sahkoinenViestintalupa",
          "lahiosoite",
          "postinumero",
          "postitoimipaikka",
          "kotikunta",
          "asuinmaa",
          "puhelinnumero",
          "sahkoposti"
        )
    )
  }

  it should "return Lukiokoulutus field name, arvosanat and all class properties for kk-hakija" in {
    val valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1709304116443-4815957697640331820",
        valintatapajononNimi = "Lukiokoulutus",
        valinnanTila = "PERUUNTUNUT",
        valinnanTilanKuvaus = Map(
          En -> "Cancelled, accepted for a study place with higher priority",
          Fi -> "Peruuntunut, hyväksytty ylemmälle hakutoiveelle",
          Sv -> "Annullerad, godkänt till ansökningsmål med högre prioritet"
        )
      )
    )

    assert(
      ExcelWriter.getHeadingFieldNames(true, true, true, valintatapajonot, List("A", "EA", "PS")) ==
        List(
          "hakijanSukunimi",
          "hakijanEtunimi",
          "turvakielto",
          "hetu",
          "syntymaAika",
          "kansalaisuudet",
          "oppijanumero",
          "hakemusOid",
          "toimipiste",
          "hakukohteenNimi",
          "hakukelpoisuus",
          "prioriteetti",
          "valintatieto",
          "ehdollisestiHyvaksytty",
          "valintatiedonPvm",
          "Lukiokoulutus",
          "vastaanottotieto",
          "viimVastaanottopaiva",
          "ensikertalainen",
          "ilmoittautuminen",
          "pohjakoulutus",
          "maksuvelvollisuus",
          "hakemusmaksunTila",
          "markkinointilupa",
          "sahkoinenViestintalupa",
          "lahiosoite",
          "postinumero",
          "postitoimipaikka",
          "kotikunta",
          "asuinmaa",
          "puhelinnumero",
          "sahkoposti",
          "A",
          "EA",
          "PS"
        )
    )
  }

  it should "return Lukiokoulutus field name, arvosanat and all other properties except hetu for kk-hakija" in {
    val valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1709304116443-4815957697640331820",
        valintatapajononNimi = "Lukiokoulutus",
        valinnanTila = "PERUUNTUNUT",
        valinnanTilanKuvaus = Map(
          En -> "Cancelled, accepted for a study place with higher priority",
          Fi -> "Peruuntunut, hyväksytty ylemmälle hakutoiveelle",
          Sv -> "Annullerad, godkänt till ansökningsmål med högre prioritet"
        )
      )
    )

    assert(
      ExcelWriter.getHeadingFieldNames(true, false, true, valintatapajonot, List("A", "EA", "PS")) ==
        List(
          "hakijanSukunimi",
          "hakijanEtunimi",
          "turvakielto",
          "syntymaAika",
          "kansalaisuudet",
          "oppijanumero",
          "hakemusOid",
          "toimipiste",
          "hakukohteenNimi",
          "hakukelpoisuus",
          "prioriteetti",
          "valintatieto",
          "ehdollisestiHyvaksytty",
          "valintatiedonPvm",
          "Lukiokoulutus",
          "vastaanottotieto",
          "viimVastaanottopaiva",
          "ensikertalainen",
          "ilmoittautuminen",
          "pohjakoulutus",
          "maksuvelvollisuus",
          "hakemusmaksunTila",
          "markkinointilupa",
          "sahkoinenViestintalupa",
          "lahiosoite",
          "postinumero",
          "postitoimipaikka",
          "kotikunta",
          "asuinmaa",
          "puhelinnumero",
          "sahkoposti",
          "A",
          "EA",
          "PS"
        )
    )
  }

  it should "return three valintatapajono field names, arvosanat and all other properties except postiosoite for kk-hakija" in {
    val valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1712237413120-5813883472174467653",
        valintatapajononNimi = "Ammatillinen koulutus",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "",
          Fi -> "Hylätty harkinnanvaraisessa valinnassa",
          Sv -> "Underkänd i antagning enligt prövning"
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1699941505144-4366169695634781556",
        valintatapajononNimi = "Kurssivalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "",
          Fi -> "Et suorittanut kurssia.",
          Sv -> "Du har inte genomfört kursen."
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1709304116443-4815957697640331820",
        valintatapajononNimi = "Lukiokoulutus",
        valinnanTila = "PERUUNTUNUT",
        valinnanTilanKuvaus = Map(
          En -> "Cancelled, accepted for a study place with higher priority",
          Fi -> "Peruuntunut, hyväksytty ylemmälle hakutoiveelle",
          Sv -> "Annullerad, godkänt till ansökningsmål med högre prioritet"
        )
      )
    )

    assert(
      ExcelWriter.getHeadingFieldNames(true, true, false, valintatapajonot, List("A", "EA", "PS")) ==
        List(
          "hakijanSukunimi",
          "hakijanEtunimi",
          "turvakielto",
          "hetu",
          "syntymaAika",
          "kansalaisuudet",
          "oppijanumero",
          "hakemusOid",
          "toimipiste",
          "hakukohteenNimi",
          "hakukelpoisuus",
          "prioriteetti",
          "valintatieto",
          "ehdollisestiHyvaksytty",
          "valintatiedonPvm",
          "Ammatillinen koulutus",
          "Kurssivalintajono kaikille hakijoille",
          "Lukiokoulutus",
          "vastaanottotieto",
          "viimVastaanottopaiva",
          "ensikertalainen",
          "ilmoittautuminen",
          "pohjakoulutus",
          "maksuvelvollisuus",
          "hakemusmaksunTila",
          "markkinointilupa",
          "sahkoinenViestintalupa",
          "kotikunta",
          "asuinmaa",
          "puhelinnumero",
          "sahkoposti",
          "A",
          "EA",
          "PS"
        )
    )
  }

  it should "not return arvosanat in the heading when naytaArvosanat is false" in {
    val valintatapajonot = List(
      Valintatapajono(
        valintatapajonoOid = "1712237413120-5813883472174467653",
        valintatapajononNimi = "Ammatillinen koulutus",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "",
          Fi -> "Hylätty harkinnanvaraisessa valinnassa",
          Sv -> "Underkänd i antagning enligt prövning"
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1699941505144-4366169695634781556",
        valintatapajononNimi = "Kurssivalintajono kaikille hakijoille",
        valinnanTila = "HYLATTY",
        valinnanTilanKuvaus = Map(
          En -> "",
          Fi -> "Et suorittanut kurssia.",
          Sv -> "Du har inte genomfört kursen."
        )
      ),
      Valintatapajono(
        valintatapajonoOid = "1709304116443-4815957697640331820",
        valintatapajononNimi = "Lukiokoulutus",
        valinnanTila = "PERUUNTUNUT",
        valinnanTilanKuvaus = Map(
          En -> "Cancelled, accepted for a study place with higher priority",
          Fi -> "Peruuntunut, hyväksytty ylemmälle hakutoiveelle",
          Sv -> "Annullerad, godkänt till ansökningsmål med högre prioritet"
        )
      )
    )

    assert(
      ExcelWriter.getHeadingFieldNames(false, true, false, valintatapajonot, List("A", "EA", "PS")) ==
        List(
          "hakijanSukunimi",
          "hakijanEtunimi",
          "turvakielto",
          "hetu",
          "syntymaAika",
          "kansalaisuudet",
          "oppijanumero",
          "hakemusOid",
          "toimipiste",
          "hakukohteenNimi",
          "hakukelpoisuus",
          "prioriteetti",
          "valintatieto",
          "ehdollisestiHyvaksytty",
          "valintatiedonPvm",
          "Ammatillinen koulutus",
          "Kurssivalintajono kaikille hakijoille",
          "Lukiokoulutus",
          "vastaanottotieto",
          "viimVastaanottopaiva",
          "ensikertalainen",
          "ilmoittautuminen",
          "pohjakoulutus",
          "maksuvelvollisuus",
          "hakemusmaksunTila",
          "markkinointilupa",
          "sahkoinenViestintalupa",
          "kotikunta",
          "asuinmaa",
          "puhelinnumero",
          "sahkoposti"
        )
    )
  }

  "writeKkHakeneetHyvaksytytVastaanottaneetRaportti" should "return excel with heading row, two data rows and summary rows" in {
    val data = List(
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          Fi -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          En -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          Sv -> "Aalto-universitetet, Högskolan för konst design och arkitektur"
        ),
        hakijat = 815,
        ensisijaisia = 261,
        ensikertalaisia = 120,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 1,
        valinnanAloituspaikat = 55,
        aloituspaikat = 55,
        toive1 = 261,
        toive2 = 0,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          Fi -> "Aalto-yliopisto, Kauppakorkeakoulu",
          En -> "Aalto University, School of Business",
          Sv -> "Aalto-universitetet, Handelshögskolan"
        ),
        hakijat = 41,
        ensisijaisia = 26,
        ensikertalaisia = 5,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 1,
        valinnanAloituspaikat = 2,
        aloituspaikat = 2,
        toive1 = 26,
        toive2 = 0,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      )
    )

    val workbook: XSSFWorkbook = ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 820,
      ensikertalaisetYksittaisetHakijat = 126,
      maksuvelvollisetYksittaisetHakijat = 2,
      naytaHakutoiveet = true,
      tulostustapa = "toimipisteittain",
      hakeneetHyvaksytytVastaanottaneetParams
    )

    assert(workbook.getNumberOfSheets == 2)
    val sheet = workbook.getSheetAt(0)

    // otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Toimipiste SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Ensikertalaisia SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Maksuvelvollisia SV")
    assert(headingRow.getCell(10).getStringCellValue == "Valinnan aloituspaikat SV")
    assert(headingRow.getCell(11).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getCell(12).getStringCellValue == "Toive1 SV")
    assert(headingRow.getCell(13).getStringCellValue == "Toive2 SV")
    assert(headingRow.getCell(14).getStringCellValue == "Toive3 SV")
    assert(headingRow.getCell(15).getStringCellValue == "Toive4 SV")
    assert(headingRow.getCell(16).getStringCellValue == "Toive5 SV")
    assert(headingRow.getCell(17).getStringCellValue == "Toive6 SV")
    assert(headingRow.getCell(18) == null)

    val dataRow1 = sheet.getRow(1)
    assert(
      dataRow1
        .getCell(0)
        .getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(dataRow1.getCell(1).getNumericCellValue == 815)
    assert(dataRow1.getCell(2).getNumericCellValue == 261)
    assert(dataRow1.getCell(3).getNumericCellValue == 120)
    assert(dataRow1.getCell(4).getNumericCellValue == 0)
    assert(dataRow1.getCell(5).getNumericCellValue == 0)
    assert(dataRow1.getCell(6).getNumericCellValue == 0)
    assert(dataRow1.getCell(7).getNumericCellValue == 0)
    assert(dataRow1.getCell(8).getNumericCellValue == 0)
    assert(dataRow1.getCell(9).getNumericCellValue == 1)
    assert(dataRow1.getCell(10).getNumericCellValue == 55)
    assert(dataRow1.getCell(11).getNumericCellValue == 55)
    assert(dataRow1.getCell(12).getNumericCellValue == 261)
    assert(dataRow1.getCell(13).getNumericCellValue == 0)
    assert(dataRow1.getCell(14).getNumericCellValue == 0)
    assert(dataRow1.getCell(15).getNumericCellValue == 0)
    assert(dataRow1.getCell(16).getNumericCellValue == 0)
    assert(dataRow1.getCell(17).getNumericCellValue == 0)
    assert(dataRow1.getCell(18) == null)

    val dataRow2 = sheet.getRow(2)
    assert(
      dataRow2
        .getCell(0)
        .getStringCellValue == "Aalto-universitetet, Handelshögskolan"
    )
    assert(dataRow2.getCell(1).getNumericCellValue == 41)
    assert(dataRow2.getCell(2).getNumericCellValue == 26)
    assert(dataRow2.getCell(3).getNumericCellValue == 5)
    assert(dataRow2.getCell(4).getNumericCellValue == 0)
    assert(dataRow2.getCell(5).getNumericCellValue == 0)
    assert(dataRow2.getCell(6).getNumericCellValue == 0)
    assert(dataRow2.getCell(7).getNumericCellValue == 0)
    assert(dataRow2.getCell(8).getNumericCellValue == 0)
    assert(dataRow2.getCell(9).getNumericCellValue == 1)
    assert(dataRow2.getCell(10).getNumericCellValue == 2)
    assert(dataRow2.getCell(11).getNumericCellValue == 2)
    assert(dataRow2.getCell(12).getNumericCellValue == 26)
    assert(dataRow2.getCell(13).getNumericCellValue == 0)
    assert(dataRow2.getCell(14).getNumericCellValue == 0)
    assert(dataRow2.getCell(15).getNumericCellValue == 0)
    assert(dataRow2.getCell(16).getNumericCellValue == 0)
    assert(dataRow2.getCell(17).getNumericCellValue == 0)
    assert(dataRow2.getCell(18) == null)

    // yhteensä
    val summaryRow = sheet.getRow(3)
    assert(summaryRow.getCell(0).getStringCellValue == "Yhteensä SV")
    assert(summaryRow.getCell(1).getNumericCellValue == 856)
    assert(summaryRow.getCell(2).getNumericCellValue == 287)
    assert(summaryRow.getCell(3).getNumericCellValue == 125)
    assert(summaryRow.getCell(4).getNumericCellValue == 0)
    assert(summaryRow.getCell(5).getNumericCellValue == 0)
    assert(summaryRow.getCell(6).getNumericCellValue == 0)
    assert(summaryRow.getCell(7).getNumericCellValue == 0)
    assert(summaryRow.getCell(8).getNumericCellValue == 0)
    assert(summaryRow.getCell(9).getNumericCellValue == 2)
    assert(summaryRow.getCell(10).getNumericCellValue == 57)
    assert(summaryRow.getCell(11).getNumericCellValue == 57)
    assert(summaryRow.getCell(12).getNumericCellValue == 287)
    assert(summaryRow.getCell(13).getNumericCellValue == 0)
    assert(summaryRow.getCell(14).getNumericCellValue == 0)
    assert(summaryRow.getCell(15).getNumericCellValue == 0)
    assert(summaryRow.getCell(16).getNumericCellValue == 0)
    assert(summaryRow.getCell(17).getNumericCellValue == 0)
    assert(summaryRow.getCell(18) == null)

    // yksittäiset hakijat lasketaan vaan tiettyihin sarakkeisiin
    val hakijatSummaryRow = sheet.getRow(4)
    assert(hakijatSummaryRow.getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(hakijatSummaryRow.getCell(1).getNumericCellValue == 820)
    assert(hakijatSummaryRow.getCell(2).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(3).getNumericCellValue == 126)
    assert(hakijatSummaryRow.getCell(4).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(5).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(6).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(7).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(8).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(9).getNumericCellValue == 2)
  }

  it should "create hakuparametrit sheet" in {
    val hakuParams: List[(String, String | Boolean | List[String])] =
      List(
        "haku" -> List("1.2.246.562.29.00000000000000015722"),
        "tulostustapa" -> "toimipisteittain",
        "koulutustoimija" -> List("1.2.246.562.10.53814745062"),
        "oppilaitos" -> List("1.2.246.562.10.39218317368"),
        "toimipiste" -> List("1.2.246.562.10.94997228401"),
        "hakukohderyhma" -> List("1.2.246.562.28.87571420741"),
        "hakukohde" -> List("1.2.246.562.20.00000000000000038238","1.2.246.562.20.00000000000000038237"),
        "okm-ohjauksen-ala" -> List("10"),
        "tutkinnon-taso" -> List("1"),
        "aidinkieli" -> List("sv"),
        "kansalaisuus" -> List("muu"),
        "sukupuoli" -> List("2"),
        "ensikertalainen" -> false,
        "nayta-hakutoiveet" -> true,
      )
    val data = List.empty
    val wb = ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 0,
      ensikertalaisetYksittaisetHakijat = 0,
      maksuvelvollisetYksittaisetHakijat = 0,
      naytaHakutoiveet = true,
      tulostustapa = "toimipisteittain",
      parametrit = hakuParams
    )

    val expectedHeaders = List("raportti.hakuehto", "raportti.hakuarvo")
    val expectedRows = List(
      List("Haku SV", "1.2.246.562.29.00000000000000015722"),
      List("Tulostustapa SV", "toimipisteittain"),
      List("Koulutustoimija SV", "1.2.246.562.10.53814745062"),
      List("Oppilaitos SV", "1.2.246.562.10.39218317368"),
      List("Toimipiste SV", "1.2.246.562.10.94997228401"),
      List("Hakukohderyhma SV", "1.2.246.562.28.87571420741"),
      List("Hakukohde SV", "1.2.246.562.20.00000000000000038238, 1.2.246.562.20.00000000000000038237"),
      List("okm-ohjauksen-ala", "10"),
      List("tutkinnon-taso", "1"),
      List("aidinkieli", "sv"),
      List("Kansalaisuus SV", "raportti.kansalaisuus.muu"),
      List("sukupuoli", "2"),
      List("ensikertalainen", "Nej"),
      List("nayta-hakutoiveet", "Ja")
    )
    assert(wb.getNumberOfSheets == 2)
    assert(wb.getSheetAt(1).getRow(1) != null)
    val sheet = wb.getSheetAt(1)

    validateHeaders(sheet = sheet, expectedHeaders = expectedHeaders)
    expectedRows.zipWithIndex.foreach { case (expectedRow, rowIndex) =>
      validateRow(sheet, rowIndex + 1, expectedRow)
    }
  }

  it should "return excel with three title columns and summaries shifted accordingly if tulostustapa is hakukohteittain" in {
    val data = List(
      KkHakeneetHyvaksytytVastaanottaneetHakukohteittain(
        hakukohdeNimi = Map(
          Fi -> "Arkkitehtuuri, tekniikan kandidaatti ja arkkitehti (3 v + 2 v)",
          En -> "Arkkitehtuuri, tekniikan kandidaatti ja arkkitehti (3 v + 2 v)",
          Sv -> "Arkitektur, teknologie kandidat och arkitekt (3 år + 2 år)"
        ),
        organisaatioNimi = Map(
          Fi -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          En -> "Aalto University, School of Arts, Design and Architecture",
          Sv -> "Aalto-universitetet, Högskolan för konst design och arkitektur"
        ),
        hakuNimi = Map(
          Fi -> "DIA-yhteisvalinta 2024",
          En -> "DIA joint application 2024",
          Sv -> "DIA gemensamma antagning 2024"
        ),
        hakijat = 815,
        ensisijaisia = 261,
        ensikertalaisia = 120,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 1,
        valinnanAloituspaikat = 55,
        aloituspaikat = 55,
        toive1 = 261,
        toive2 = 0,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      ),
      KkHakeneetHyvaksytytVastaanottaneetHakukohteittain(
        hakukohdeNimi = Map(
          Fi -> "Dokumentaarinen elokuva, taiteen kandidaatti ja maisteri (3 v + 2 v)",
          En -> "Dokumentaarinen elokuva, taiteen kandidaatti ja maisteri (3 v + 2 v)",
          Sv -> "Dokumentärfilm, konstkandidat och -magister (3 år + 2 år)"
        ),
        organisaatioNimi = Map(
          Fi -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          En -> "Aalto University, School of Arts, Design and Architecture",
          Sv -> "Aalto-universitetet, Högskolan för konst design och arkitektur"
        ),
        hakuNimi = Map(
          Fi -> "DIA-yhteisvalinta 2024",
          En -> "DIA joint application 2024",
          Sv -> "DIA gemensamma antagning 2024"
        ),
        hakijat = 41,
        ensisijaisia = 26,
        ensikertalaisia = 5,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 1,
        valinnanAloituspaikat = 2,
        aloituspaikat = 2,
        toive1 = 26,
        toive2 = 0,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      )
    )

    val workbook: XSSFWorkbook = ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 820,
      ensikertalaisetYksittaisetHakijat = 126,
      maksuvelvollisetYksittaisetHakijat = 2,
      naytaHakutoiveet = true,
      tulostustapa = "hakukohteittain",
      hakeneetHyvaksytytVastaanottaneetParams
    )

    assert(workbook.getNumberOfSheets == 2)
    val sheet = workbook.getSheetAt(0)

    // otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Hakukohde SV")
    assert(headingRow.getCell(1).getStringCellValue == "Organisaatio SV")
    assert(headingRow.getCell(2).getStringCellValue == "Haku SV")
    assert(headingRow.getCell(3).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(4).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(5).getStringCellValue == "Ensikertalaisia SV")
    assert(headingRow.getCell(6).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(7).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(8).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(9).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(10).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(11).getStringCellValue == "Maksuvelvollisia SV")
    assert(headingRow.getCell(12).getStringCellValue == "Valinnan aloituspaikat SV")
    assert(headingRow.getCell(13).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getCell(14).getStringCellValue == "Toive1 SV")
    assert(headingRow.getCell(15).getStringCellValue == "Toive2 SV")
    assert(headingRow.getCell(16).getStringCellValue == "Toive3 SV")
    assert(headingRow.getCell(17).getStringCellValue == "Toive4 SV")
    assert(headingRow.getCell(18).getStringCellValue == "Toive5 SV")
    assert(headingRow.getCell(19).getStringCellValue == "Toive6 SV")
    assert(headingRow.getCell(20) == null)

    val dataRow1 = sheet.getRow(1)
    assert(
      dataRow1
        .getCell(0)
        .getStringCellValue == "Arkitektur, teknologie kandidat och arkitekt (3 år + 2 år)"
    )
    assert(dataRow1.getCell(1).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur")
    assert(dataRow1.getCell(2).getStringCellValue == "DIA gemensamma antagning 2024")
    assert(dataRow1.getCell(3).getNumericCellValue == 815)
    assert(dataRow1.getCell(4).getNumericCellValue == 261)
    assert(dataRow1.getCell(5).getNumericCellValue == 120)
    assert(dataRow1.getCell(6).getNumericCellValue == 0)
    assert(dataRow1.getCell(7).getNumericCellValue == 0)
    assert(dataRow1.getCell(8).getNumericCellValue == 0)
    assert(dataRow1.getCell(9).getNumericCellValue == 0)
    assert(dataRow1.getCell(10).getNumericCellValue == 0)
    assert(dataRow1.getCell(11).getNumericCellValue == 1)
    assert(dataRow1.getCell(12).getNumericCellValue == 55)
    assert(dataRow1.getCell(13).getNumericCellValue == 55)
    assert(dataRow1.getCell(14).getNumericCellValue == 261)
    assert(dataRow1.getCell(15).getNumericCellValue == 0)
    assert(dataRow1.getCell(16).getNumericCellValue == 0)
    assert(dataRow1.getCell(17).getNumericCellValue == 0)
    assert(dataRow1.getCell(18).getNumericCellValue == 0)
    assert(dataRow1.getCell(19).getNumericCellValue == 0)
    assert(dataRow1.getCell(20) == null)

    val dataRow2 = sheet.getRow(2)
    assert(
      dataRow2
        .getCell(0)
        .getStringCellValue == "Dokumentärfilm, konstkandidat och -magister (3 år + 2 år)"
    )
    assert(dataRow2.getCell(1).getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur")
    assert(dataRow2.getCell(2).getStringCellValue == "DIA gemensamma antagning 2024")
    assert(dataRow2.getCell(3).getNumericCellValue == 41)
    assert(dataRow2.getCell(4).getNumericCellValue == 26)
    assert(dataRow2.getCell(5).getNumericCellValue == 5)
    assert(dataRow2.getCell(6).getNumericCellValue == 0)
    assert(dataRow2.getCell(7).getNumericCellValue == 0)
    assert(dataRow2.getCell(8).getNumericCellValue == 0)
    assert(dataRow2.getCell(9).getNumericCellValue == 0)
    assert(dataRow2.getCell(10).getNumericCellValue == 0)
    assert(dataRow2.getCell(11).getNumericCellValue == 1)
    assert(dataRow2.getCell(12).getNumericCellValue == 2)
    assert(dataRow2.getCell(13).getNumericCellValue == 2)
    assert(dataRow2.getCell(14).getNumericCellValue == 26)
    assert(dataRow2.getCell(15).getNumericCellValue == 0)
    assert(dataRow2.getCell(16).getNumericCellValue == 0)
    assert(dataRow2.getCell(17).getNumericCellValue == 0)
    assert(dataRow2.getCell(18).getNumericCellValue == 0)
    assert(dataRow2.getCell(19).getNumericCellValue == 0)
    assert(dataRow2.getCell(20) == null)

    // yhteensä
    val summaryRow = sheet.getRow(3)
    assert(summaryRow.getCell(0).getStringCellValue == "")
    assert(summaryRow.getCell(1).getStringCellValue == "")
    assert(summaryRow.getCell(2).getStringCellValue == "Yhteensä SV")
    assert(summaryRow.getCell(3).getNumericCellValue == 856)
    assert(summaryRow.getCell(4).getNumericCellValue == 287)
    assert(summaryRow.getCell(5).getNumericCellValue == 125)
    assert(summaryRow.getCell(6).getNumericCellValue == 0)
    assert(summaryRow.getCell(7).getNumericCellValue == 0)
    assert(summaryRow.getCell(8).getNumericCellValue == 0)
    assert(summaryRow.getCell(9).getNumericCellValue == 0)
    assert(summaryRow.getCell(10).getNumericCellValue == 0)
    assert(summaryRow.getCell(11).getNumericCellValue == 2)
    assert(summaryRow.getCell(12).getNumericCellValue == 57)
    assert(summaryRow.getCell(13).getNumericCellValue == 57)
    assert(summaryRow.getCell(14).getNumericCellValue == 287)
    assert(summaryRow.getCell(15).getNumericCellValue == 0)
    assert(summaryRow.getCell(16).getNumericCellValue == 0)
    assert(summaryRow.getCell(17).getNumericCellValue == 0)
    assert(summaryRow.getCell(18).getNumericCellValue == 0)
    assert(summaryRow.getCell(19).getNumericCellValue == 0)
    assert(summaryRow.getCell(20) == null)

    // yksittäiset hakijat lasketaan vaan tiettyihin sarakkeisiin
    val hakijatSummaryRow = sheet.getRow(4)
    assert(hakijatSummaryRow.getCell(0).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(1).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(2).getStringCellValue == "Yksittäiset hakijat SV")
    assert(hakijatSummaryRow.getCell(3).getNumericCellValue == 820)
    assert(hakijatSummaryRow.getCell(4).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(5).getNumericCellValue == 126)
    assert(hakijatSummaryRow.getCell(6).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(7).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(8).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(9).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(10).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(11).getNumericCellValue == 2)
  }

  it should "return excel without hakutoiveet columns if naytaHakutoiveet is false" in {
    val data = List(
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          Fi -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          En -> "Aalto-yliopisto, Taiteiden ja suunnittelun korkeakoulu",
          Sv -> "Aalto-universitetet, Högskolan för konst design och arkitektur"
        ),
        hakijat = 815,
        ensisijaisia = 261,
        ensikertalaisia = 120,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 1,
        valinnanAloituspaikat = 55,
        aloituspaikat = 55,
        toive1 = 261,
        toive2 = 0,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          Fi -> "Aalto-yliopisto, Kauppakorkeakoulu",
          En -> "Aalto-yliopisto, School of Business",
          Sv -> "Aalto-universitetet, Handelshögskolan"
        ),
        hakijat = 41,
        ensisijaisia = 26,
        ensikertalaisia = 5,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 1,
        valinnanAloituspaikat = 2,
        aloituspaikat = 2,
        toive1 = 26,
        toive2 = 0,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      )
    )

    val workbook: XSSFWorkbook = ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
      asiointikieli = "sv",
      translations = translations,
      data = data,
      yksittaisetHakijat = 820,
      ensikertalaisetYksittaisetHakijat = 126,
      maksuvelvollisetYksittaisetHakijat = 2,
      naytaHakutoiveet = false,
      tulostustapa = "toimipisteittain",
      hakeneetHyvaksytytVastaanottaneetParams
    )

    assert(workbook.getNumberOfSheets == 2)
    val sheet = workbook.getSheetAt(0)

    // otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Toimipiste SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Ensikertalaisia SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Maksuvelvollisia SV")
    assert(headingRow.getCell(10).getStringCellValue == "Valinnan aloituspaikat SV")
    assert(headingRow.getCell(11).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getCell(12) == null)

    val dataRow1 = sheet.getRow(1)
    assert(
      dataRow1
        .getCell(0)
        .getStringCellValue == "Aalto-universitetet, Högskolan för konst design och arkitektur"
    )
    assert(dataRow1.getCell(1).getNumericCellValue == 815)
    assert(dataRow1.getCell(2).getNumericCellValue == 261)
    assert(dataRow1.getCell(3).getNumericCellValue == 120)
    assert(dataRow1.getCell(4).getNumericCellValue == 0)
    assert(dataRow1.getCell(5).getNumericCellValue == 0)
    assert(dataRow1.getCell(6).getNumericCellValue == 0)
    assert(dataRow1.getCell(7).getNumericCellValue == 0)
    assert(dataRow1.getCell(8).getNumericCellValue == 0)
    assert(dataRow1.getCell(9).getNumericCellValue == 1)
    assert(dataRow1.getCell(10).getNumericCellValue == 55)
    assert(dataRow1.getCell(11).getNumericCellValue == 55)
    assert(dataRow1.getCell(12) == null)

    val dataRow2 = sheet.getRow(2)
    assert(
      dataRow2
        .getCell(0)
        .getStringCellValue == "Aalto-universitetet, Handelshögskolan"
    )
    assert(dataRow2.getCell(1).getNumericCellValue == 41)
    assert(dataRow2.getCell(2).getNumericCellValue == 26)
    assert(dataRow2.getCell(3).getNumericCellValue == 5)
    assert(dataRow2.getCell(4).getNumericCellValue == 0)
    assert(dataRow2.getCell(5).getNumericCellValue == 0)
    assert(dataRow2.getCell(6).getNumericCellValue == 0)
    assert(dataRow2.getCell(7).getNumericCellValue == 0)
    assert(dataRow2.getCell(8).getNumericCellValue == 0)
    assert(dataRow2.getCell(9).getNumericCellValue == 1)
    assert(dataRow2.getCell(10).getNumericCellValue == 2)
    assert(dataRow2.getCell(11).getNumericCellValue == 2)
    assert(dataRow2.getCell(12) == null)

    // yhteensä
    val summaryRow = sheet.getRow(3)
    assert(summaryRow.getCell(0).getStringCellValue == "Yhteensä SV")
    assert(summaryRow.getCell(1).getNumericCellValue == 856)
    assert(summaryRow.getCell(2).getNumericCellValue == 287)
    assert(summaryRow.getCell(3).getNumericCellValue == 125)
    assert(summaryRow.getCell(4).getNumericCellValue == 0)
    assert(summaryRow.getCell(5).getNumericCellValue == 0)
    assert(summaryRow.getCell(6).getNumericCellValue == 0)
    assert(summaryRow.getCell(7).getNumericCellValue == 0)
    assert(summaryRow.getCell(8).getNumericCellValue == 0)
    assert(summaryRow.getCell(9).getNumericCellValue == 2)
    assert(summaryRow.getCell(10).getNumericCellValue == 57)
    assert(summaryRow.getCell(11).getNumericCellValue == 57)
    assert(summaryRow.getCell(12) == null)

    // yksittäiset hakijat lasketaan vaan tiettyihin sarakkeisiin
    val hakijatSummaryRow = sheet.getRow(4)
    assert(hakijatSummaryRow.getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(hakijatSummaryRow.getCell(1).getNumericCellValue == 820)
    assert(hakijatSummaryRow.getCell(2).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(3).getNumericCellValue == 126)
    assert(hakijatSummaryRow.getCell(4).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(5).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(6).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(7).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(8).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(9).getNumericCellValue == 2)
  }

  it should "return excel without aloituspaikat for kansalaisuuksittain" in {
    val data = List(
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(En -> "Afghanistan", Fi -> "Afganistan", Sv -> "Afghanistan"),
        hakijat = 2,
        ensisijaisia = 0,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 58,
        aloituspaikat = 58,
        toive1 = 0,
        toive2 = 0,
        toive3 = 2,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(En -> "Bulgaria", Fi -> "Bulgaria", Sv -> "Bulgarien"),
        hakijat = 2,
        ensisijaisia = 0,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 42,
        aloituspaikat = 42,
        toive1 = 0,
        toive2 = 1,
        toive3 = 1,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(En -> "China", Fi -> "Kiina", Sv -> "Kina"),
        hakijat = 4,
        ensisijaisia = 2,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 79,
        aloituspaikat = 79,
        toive1 = 2,
        toive2 = 1,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 1
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(Fi -> "Finland", Fi -> "Suomi", Sv -> "Finland"),
        hakijat = 2751,
        ensisijaisia = 1287,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 252,
        aloituspaikat = 252,
        toive1 = 1287,
        toive2 = 694,
        toive3 = 347,
        toive4 = 209,
        toive5 = 122,
        toive6 = 92
      )
    )

    val workbook: XSSFWorkbook = ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
      "sv",
      translations,
      data,
      yksittaisetHakijat = 2759,
      ensikertalaisetYksittaisetHakijat = 1289,
      maksuvelvollisetYksittaisetHakijat = 0,
      naytaHakutoiveet = true,
      tulostustapa = "kansalaisuuksittain",
      hakeneetHyvaksytytVastaanottaneetParams
    )

    assert(workbook.getNumberOfSheets == 2)
    val sheet = workbook.getSheetAt(0)

    // otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Kansalaisuus SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Ensikertalaisia SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Maksuvelvollisia SV")
    assert(headingRow.getCell(10).getStringCellValue == "Toive1 SV")
    assert(headingRow.getCell(11).getStringCellValue == "Toive2 SV")
    assert(headingRow.getCell(12).getStringCellValue == "Toive3 SV")
    assert(headingRow.getCell(13).getStringCellValue == "Toive4 SV")
    assert(headingRow.getCell(14).getStringCellValue == "Toive5 SV")
    assert(headingRow.getCell(15).getStringCellValue == "Toive6 SV")
    assert(headingRow.getCell(16) == null)

    // data rows
    val dataRow1 = sheet.getRow(1)
    assert(dataRow1.getCell(0).getStringCellValue == "Afghanistan")
    assert(dataRow1.getCell(1).getNumericCellValue == 2)
    assert(dataRow1.getCell(2).getNumericCellValue == 0)
    assert(dataRow1.getCell(3).getNumericCellValue == 0)
    assert(dataRow1.getCell(4).getNumericCellValue == 0)
    assert(dataRow1.getCell(5).getNumericCellValue == 0)
    assert(dataRow1.getCell(6).getNumericCellValue == 0)
    assert(dataRow1.getCell(7).getNumericCellValue == 0)
    assert(dataRow1.getCell(8).getNumericCellValue == 0)
    assert(dataRow1.getCell(9).getNumericCellValue == 0)
    assert(dataRow1.getCell(10).getNumericCellValue == 0)
    assert(dataRow1.getCell(11).getNumericCellValue == 0)
    assert(dataRow1.getCell(12).getNumericCellValue == 2)
    assert(dataRow1.getCell(13).getNumericCellValue == 0)
    assert(dataRow1.getCell(14).getNumericCellValue == 0)
    assert(dataRow1.getCell(15).getNumericCellValue == 0)
    assert(dataRow1.getCell(16) == null)

    val dataRow2 = sheet.getRow(2)
    assert(dataRow2.getCell(0).getStringCellValue == "Bulgarien")
    assert(dataRow2.getCell(1).getNumericCellValue == 2)
    assert(dataRow2.getCell(2).getNumericCellValue == 0)
    assert(dataRow2.getCell(3).getNumericCellValue == 0)
    assert(dataRow2.getCell(4).getNumericCellValue == 0)
    assert(dataRow2.getCell(5).getNumericCellValue == 0)
    assert(dataRow2.getCell(6).getNumericCellValue == 0)
    assert(dataRow2.getCell(7).getNumericCellValue == 0)
    assert(dataRow2.getCell(8).getNumericCellValue == 0)
    assert(dataRow2.getCell(9).getNumericCellValue == 0)
    assert(dataRow2.getCell(10).getNumericCellValue == 0)
    assert(dataRow2.getCell(11).getNumericCellValue == 1)
    assert(dataRow2.getCell(12).getNumericCellValue == 1)
    assert(dataRow2.getCell(13).getNumericCellValue == 0)
    assert(dataRow2.getCell(14).getNumericCellValue == 0)
    assert(dataRow2.getCell(15).getNumericCellValue == 0)
    assert(dataRow2.getCell(16) == null)

    val dataRow3 = sheet.getRow(3)
    assert(dataRow3.getCell(0).getStringCellValue == "Kina")
    assert(dataRow3.getCell(1).getNumericCellValue == 4)
    assert(dataRow3.getCell(2).getNumericCellValue == 2)
    assert(dataRow3.getCell(3).getNumericCellValue == 0)
    assert(dataRow3.getCell(4).getNumericCellValue == 0)
    assert(dataRow3.getCell(5).getNumericCellValue == 0)
    assert(dataRow3.getCell(6).getNumericCellValue == 0)
    assert(dataRow3.getCell(7).getNumericCellValue == 0)
    assert(dataRow3.getCell(8).getNumericCellValue == 0)
    assert(dataRow3.getCell(9).getNumericCellValue == 0)
    assert(dataRow3.getCell(10).getNumericCellValue == 2)
    assert(dataRow3.getCell(11).getNumericCellValue == 1)
    assert(dataRow3.getCell(12).getNumericCellValue == 0)
    assert(dataRow3.getCell(13).getNumericCellValue == 0)
    assert(dataRow3.getCell(14).getNumericCellValue == 0)
    assert(dataRow3.getCell(15).getNumericCellValue == 1)
    assert(dataRow3.getCell(16) == null)

    val dataRow4 = sheet.getRow(4)
    assert(dataRow4.getCell(0).getStringCellValue == "Finland")
    assert(dataRow4.getCell(1).getNumericCellValue == 2751)
    assert(dataRow4.getCell(2).getNumericCellValue == 1287)
    assert(dataRow4.getCell(3).getNumericCellValue == 0)
    assert(dataRow4.getCell(4).getNumericCellValue == 0)
    assert(dataRow4.getCell(5).getNumericCellValue == 0)
    assert(dataRow4.getCell(6).getNumericCellValue == 0)
    assert(dataRow4.getCell(7).getNumericCellValue == 0)
    assert(dataRow4.getCell(8).getNumericCellValue == 0)
    assert(dataRow4.getCell(9).getNumericCellValue == 0)
    assert(dataRow4.getCell(10).getNumericCellValue == 1287)
    assert(dataRow4.getCell(11).getNumericCellValue == 694)
    assert(dataRow4.getCell(12).getNumericCellValue == 347)
    assert(dataRow4.getCell(13).getNumericCellValue == 209)
    assert(dataRow4.getCell(14).getNumericCellValue == 122)
    assert(dataRow4.getCell(15).getNumericCellValue == 92)
    assert(dataRow4.getCell(16) == null)

    // summary rows
    val summaryRow = sheet.getRow(5)
    assert(summaryRow.getCell(0).getStringCellValue == "Yhteensä SV")
    assert(summaryRow.getCell(1).getNumericCellValue == 2759)
    assert(summaryRow.getCell(2).getNumericCellValue == 1289)
    assert(summaryRow.getCell(3).getNumericCellValue == 0)
    assert(summaryRow.getCell(4).getNumericCellValue == 0)
    assert(summaryRow.getCell(5).getNumericCellValue == 0)
    assert(summaryRow.getCell(6).getNumericCellValue == 0)
    assert(summaryRow.getCell(7).getNumericCellValue == 0)
    assert(summaryRow.getCell(8).getNumericCellValue == 0)
    assert(summaryRow.getCell(9).getNumericCellValue == 0)
    assert(summaryRow.getCell(10).getNumericCellValue == 1289)
    assert(summaryRow.getCell(11).getNumericCellValue == 696)
    assert(summaryRow.getCell(12).getNumericCellValue == 350)
    assert(summaryRow.getCell(13).getNumericCellValue == 209)
    assert(summaryRow.getCell(14).getNumericCellValue == 122)
    assert(summaryRow.getCell(15).getNumericCellValue == 93)
    assert(summaryRow.getCell(16) == null)

    val hakijatSummaryRow = sheet.getRow(6)
    assert(hakijatSummaryRow.getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(hakijatSummaryRow.getCell(1).getNumericCellValue == 2759)
    assert(hakijatSummaryRow.getCell(2).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(3).getNumericCellValue == 1289)
    assert(hakijatSummaryRow.getCell(4).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(5).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(6).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(7).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(8).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(9).getNumericCellValue == 0)
    assert(hakijatSummaryRow.getCell(10) == null)
  }

  it should "return excel with only hakijat yhteensä summary row heading row and provided data rows for hakukohderyhmittain" in {
    val data = List(
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "Automaattinen hakukelpoisuus kevät II 2024",
          Fi -> "Automaattinen hakukelpoisuus kevät II 2024",
          Sv -> "Automaattinen hakukelpoisuus kevät II 2024"
        ),
        hakijat = 5661,
        ensisijaisia = 1309,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 242,
        aloituspaikat = 242,
        toive1 = 1309,
        toive2 = 1163,
        toive3 = 993,
        toive4 = 1003,
        toive5 = 892,
        toive6 = 301
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "Hammaslääketieteen yhteisvalinta 2024",
          Fi -> "Hammaslääketieteen yhteisvalinta 2024",
          Sv -> "Hammaslääketieteen yhteisvalinta 2024"
        ),
        hakijat = 1078,
        ensisijaisia = 269,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 52,
        aloituspaikat = 52,
        toive1 = 269,
        toive2 = 241,
        toive3 = 220,
        toive4 = 219,
        toive5 = 79,
        toive6 = 50
      ),
      KkHakeneetHyvaksytytVastaanottaneetResult(
        otsikko = Map(
          En -> "Hoitotieteen/terveystieteiden valintakoeyhteistyö 2024",
          Fi -> "Hoitotieteen/terveystieteiden valintakoeyhteistyö 2024",
          Sv -> "Hoitotieteen/terveystieteiden valintakoeyhteistyö 2024"
        ),
        hakijat = 0,
        ensisijaisia = 0,
        ensikertalaisia = 0,
        hyvaksytyt = 0,
        vastaanottaneet = 0,
        lasna = 0,
        poissa = 0,
        ilmYht = 0,
        maksuvelvollisia = 0,
        valinnanAloituspaikat = 0,
        aloituspaikat = 0,
        toive1 = 0,
        toive2 = 0,
        toive3 = 0,
        toive4 = 0,
        toive5 = 0,
        toive6 = 0
      )
    )

    val workbook: XSSFWorkbook = ExcelWriter.writeKkHakeneetHyvaksytytVastaanottaneetRaportti(
      "sv",
      translations,
      data,
      yksittaisetHakijat = 6739,
      ensikertalaisetYksittaisetHakijat = 1578,
      maksuvelvollisetYksittaisetHakijat = 0,
      naytaHakutoiveet = true,
      tulostustapa = "hakukohderyhmittain",
      hakeneetHyvaksytytVastaanottaneetParams
    )

    assert(workbook.getNumberOfSheets == 2)
    val sheet = workbook.getSheetAt(0)

    // otsikkorivi
    val headingRow = sheet.getRow(0)
    assert(headingRow.getCell(0).getStringCellValue == "Hakukohderyhma SV")
    assert(headingRow.getCell(1).getStringCellValue == "Hakijat SV")
    assert(headingRow.getCell(2).getStringCellValue == "Ensisijaisia SV")
    assert(headingRow.getCell(3).getStringCellValue == "Ensikertalaisia SV")
    assert(headingRow.getCell(4).getStringCellValue == "Hyväksytyt SV")
    assert(headingRow.getCell(5).getStringCellValue == "Vastaanottaneet SV")
    assert(headingRow.getCell(6).getStringCellValue == "Läsnä SV")
    assert(headingRow.getCell(7).getStringCellValue == "Poissa SV")
    assert(headingRow.getCell(8).getStringCellValue == "IlmYht SV")
    assert(headingRow.getCell(9).getStringCellValue == "Maksuvelvollisia SV")
    assert(headingRow.getCell(10).getStringCellValue == "Valinnan aloituspaikat SV")
    assert(headingRow.getCell(11).getStringCellValue == "Aloituspaikat SV")
    assert(headingRow.getCell(12).getStringCellValue == "Toive1 SV")
    assert(headingRow.getCell(13).getStringCellValue == "Toive2 SV")
    assert(headingRow.getCell(14).getStringCellValue == "Toive3 SV")
    assert(headingRow.getCell(15).getStringCellValue == "Toive4 SV")
    assert(headingRow.getCell(16).getStringCellValue == "Toive5 SV")
    assert(headingRow.getCell(17).getStringCellValue == "Toive6 SV")
    assert(headingRow.getCell(18) == null)

    // data rows
    val dataRow1 = sheet.getRow(1)
    assert(dataRow1.getCell(0).getStringCellValue == "Automaattinen hakukelpoisuus kevät II 2024")
    assert(dataRow1.getCell(1).getNumericCellValue == 5661)
    assert(dataRow1.getCell(2).getNumericCellValue == 1309)
    assert(dataRow1.getCell(3).getNumericCellValue == 0)
    assert(dataRow1.getCell(4).getNumericCellValue == 0)
    assert(dataRow1.getCell(5).getNumericCellValue == 0)
    assert(dataRow1.getCell(6).getNumericCellValue == 0)
    assert(dataRow1.getCell(7).getNumericCellValue == 0)
    assert(dataRow1.getCell(8).getNumericCellValue == 0)
    assert(dataRow1.getCell(9).getNumericCellValue == 0)
    assert(dataRow1.getCell(10).getNumericCellValue == 242)
    assert(dataRow1.getCell(11).getNumericCellValue == 242)
    assert(dataRow1.getCell(12).getNumericCellValue == 1309)
    assert(dataRow1.getCell(13).getNumericCellValue == 1163)
    assert(dataRow1.getCell(14).getNumericCellValue == 993)
    assert(dataRow1.getCell(15).getNumericCellValue == 1003)
    assert(dataRow1.getCell(16).getNumericCellValue == 892)
    assert(dataRow1.getCell(17).getNumericCellValue == 301)
    assert(dataRow1.getCell(18) == null)

    val dataRow2 = sheet.getRow(2)
    assert(dataRow2.getCell(0).getStringCellValue == "Hammaslääketieteen yhteisvalinta 2024")
    assert(dataRow2.getCell(1).getNumericCellValue == 1078)
    assert(dataRow2.getCell(2).getNumericCellValue == 269)
    assert(dataRow2.getCell(3).getNumericCellValue == 0)
    assert(dataRow2.getCell(4).getNumericCellValue == 0)
    assert(dataRow2.getCell(5).getNumericCellValue == 0)
    assert(dataRow2.getCell(6).getNumericCellValue == 0)
    assert(dataRow2.getCell(7).getNumericCellValue == 0)
    assert(dataRow2.getCell(8).getNumericCellValue == 0)
    assert(dataRow2.getCell(9).getNumericCellValue == 0)
    assert(dataRow2.getCell(10).getNumericCellValue == 52)
    assert(dataRow2.getCell(11).getNumericCellValue == 52)
    assert(dataRow2.getCell(12).getNumericCellValue == 269)
    assert(dataRow2.getCell(13).getNumericCellValue == 241)
    assert(dataRow2.getCell(14).getNumericCellValue == 220)
    assert(dataRow2.getCell(15).getNumericCellValue == 219)
    assert(dataRow2.getCell(16).getNumericCellValue == 79)
    assert(dataRow2.getCell(17).getNumericCellValue == 50)
    assert(dataRow2.getCell(18) == null)

    val dataRow3 = sheet.getRow(3)
    assert(dataRow3.getCell(0).getStringCellValue == "Hoitotieteen/terveystieteiden valintakoeyhteistyö 2024")
    assert(dataRow3.getCell(1).getNumericCellValue == 0)
    assert(dataRow3.getCell(2).getNumericCellValue == 0)
    assert(dataRow3.getCell(3).getNumericCellValue == 0)
    assert(dataRow3.getCell(4).getNumericCellValue == 0)
    assert(dataRow3.getCell(5).getNumericCellValue == 0)
    assert(dataRow3.getCell(6).getNumericCellValue == 0)
    assert(dataRow3.getCell(7).getNumericCellValue == 0)
    assert(dataRow3.getCell(8).getNumericCellValue == 0)
    assert(dataRow3.getCell(9).getNumericCellValue == 0)
    assert(dataRow3.getCell(10).getNumericCellValue == 0)
    assert(dataRow3.getCell(11).getNumericCellValue == 0)
    assert(dataRow3.getCell(12).getNumericCellValue == 0)
    assert(dataRow3.getCell(13).getNumericCellValue == 0)
    assert(dataRow3.getCell(14).getNumericCellValue == 0)
    assert(dataRow3.getCell(15).getNumericCellValue == 0)
    assert(dataRow3.getCell(16).getNumericCellValue == 0)
    assert(dataRow3.getCell(17).getNumericCellValue == 0)
    assert(dataRow3.getCell(18) == null)

    // vain hakijat yhteensä summarivi
    val hakijatSummaryRow = sheet.getRow(4)
    assert(hakijatSummaryRow.getCell(0).getStringCellValue == "Yksittäiset hakijat SV")
    assert(hakijatSummaryRow.getCell(1).getNumericCellValue == 6739)
    assert(hakijatSummaryRow.getCell(2).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(3).getNumericCellValue == 1578)
    assert(hakijatSummaryRow.getCell(4).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(5).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(6).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(7).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(8).getStringCellValue == "")
    assert(hakijatSummaryRow.getCell(9).getNumericCellValue == 0)
    assert(hakijatSummaryRow.getCell(10) == null)

    assert(sheet.getPhysicalNumberOfRows == 5)
  }
}
