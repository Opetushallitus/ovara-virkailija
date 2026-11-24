package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.external.kkhakijat.Valintatila.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.OffsetDateTime

// Yhden paikan säännös valinta-tulos-servicen tapaan (ValintatulosService.asetaVastaanotettavuusValintarekisterinPerusteella)
// ja hakukohteen kuuluminen säännön piiriin kouta-indeksoijan tapaan (assoc-yps).
class ExternalKKHakijatYhdenPaikanSaantoTest extends AnyFlatSpec with Matchers {

  private val syksy2026 = Alkamiskausi("S", 2026)
  private val kevat2027 = Alkamiskausi("K", 2027)

  private def tila(valinnanTila: Valintatila, vastaanotto: Vastaanottotila = Vastaanottotila.KESKEN) =
    VtsTila(valinnanTila, vastaanotto, julkaistu = true)

  private val ottanutVastaanToisenPaikan =
    VtsTila(PERUUNTUNUT, Vastaanottotila.OTTANUT_VASTAAN_TOISEN_PAIKAN, julkaistu = true)

  /** Hakutoive: tila, tallennettu vastaanotto ja YPS-kausi. Tallennettu vastaanotto on oletuksena tilan vastaanotto. */
  private case class Toive(
    tila: VtsTila,
    kausi: Option[Alkamiskausi] = Some(syksy2026),
    tallennettu: Option[Vastaanottotila] = None
  )

  /**
   * Hakutoiveet ja kaudet, joilla henkilöllä on vastaanotto muissa hauissa. Hakemuksen omien hakutoiveiden
   *  tallennetut vastaanotot lisätään kausiin, kuten selectYpsVastaanotot tekee.
   */
  private def saanto(toiveet: Toive*)(vastaanototMuualla: Set[Alkamiskausi] = Set.empty): Seq[VtsTila] = {
    val rivit = toiveet.zipWithIndex.map { case (t, i) =>
      (i, t.tila, t.tallennettu.getOrElse(t.tila.vastaanottotila), t.kausi)
    }
    val omatVastaanotot = rivit.collect {
      case (_, _, Vastaanottotila.VASTAANOTTANUT_SITOVASTI | Vastaanottotila.EHDOLLISESTI_VASTAANOTTANUT, kausi) =>
        kausi
    }.flatten
    val tulos = yhdenPaikanSaanto(rivit, vastaanototMuualla ++ omatVastaanotot)
    toiveet.indices.map(tulos)
  }

  private val sitova = tila(HYVAKSYTTY, Vastaanottotila.VASTAANOTTANUT_SITOVASTI)

  "yhdenPaikanSaanto" should "make the other hyväksytty and varalla toiveet of the kausi ottanut vastaan toisen paikan" in {
    saanto(
      Toive(sitova),
      Toive(tila(HYVAKSYTTY)),
      Toive(tila(VARALLA)),
      Toive(tila(VARASIJALTA_HYVAKSYTTY)),
      Toive(tila(HARKINNANVARAISESTI_HYVAKSYTTY))
    )() shouldBe Seq(sitova) ++ Seq.fill(4)(ottanutVastaanToisenPaikan)
  }

  it should "not change hylätty, peruuntunut, kesken, perunut or peruutettu toiveet" in {
    val muut = Seq(HYLATTY, PERUUNTUNUT, KESKEN, PERUNUT, PERUUTETTU).map(tila(_))
    saanto(Toive(sitova) +: muut.map(Toive(_))*)() shouldBe sitova +: muut
  }

  it should "not change anything when a toive of the kausi is ehdollisesti vastaanotettu" in {
    val ehdollinen = tila(HYVAKSYTTY, Vastaanottotila.EHDOLLISESTI_VASTAANOTTANUT)
    saanto(Toive(ehdollinen), Toive(sitova), Toive(tila(VARALLA)))(Set(syksy2026)) shouldBe
      Seq(ehdollinen, sitova, tila(VARALLA))
  }

  it should "not let an ehdollinen vastaanotto on another kausi or outside the säännön piiri block the kausi" in {
    val ehdollinen = tila(HYVAKSYTTY, Vastaanottotila.EHDOLLISESTI_VASTAANOTTANUT)
    saanto(Toive(ehdollinen, kausi = Some(kevat2027)), Toive(tila(VARALLA)))(Set(syksy2026)) shouldBe
      Seq(ehdollinen, ottanutVastaanToisenPaikan)
    saanto(Toive(ehdollinen, kausi = None), Toive(tila(VARALLA)))(Set(syksy2026)) shouldBe
      Seq(ehdollinen, ottanutVastaanToisenPaikan)
  }

  // VTS katsoo ehdollisuuden lasketusta vastaanottotilasta, jonka julkaisematon tulos piilottaa. Tallennettu
  // vastaanotto on silti henkilön vastaanotto kaudella.
  it should "count a hidden ehdollinen vastaanotto of an unpublished toive as a vastaanotto, not as ehdollinen" in {
    val julkaisematon = VtsTila(KESKEN, Vastaanottotila.KESKEN, julkaistu = false)
    saanto(
      Toive(julkaisematon, tallennettu = Some(Vastaanottotila.EHDOLLISESTI_VASTAANOTTANUT)),
      Toive(tila(VARALLA))
    )() shouldBe Seq(julkaisematon, ottanutVastaanToisenPaikan)
  }

  it should "apply when the person has a vastaanotto on the kausi in another haku" in {
    saanto(Toive(tila(HYVAKSYTTY)), Toive(tila(VARALLA)))(Set(syksy2026)) shouldBe
      Seq(ottanutVastaanToisenPaikan, ottanutVastaanToisenPaikan)
  }

  it should "apply for a stored vastaanotto of the hakemus that is hidden because its toive is unpublished" in {
    val julkaisematon = VtsTila(KESKEN, Vastaanottotila.KESKEN, julkaistu = false)
    saanto(
      Toive(julkaisematon, tallennettu = Some(Vastaanottotila.VASTAANOTTANUT_SITOVASTI)),
      Toive(tila(VARALLA))
    )() shouldBe
      Seq(julkaisematon, ottanutVastaanToisenPaikan)
  }

  it should "not apply for a vastaanotto on another kausi" in {
    saanto(Toive(tila(HYVAKSYTTY)))(Set(kevat2027)) shouldBe Seq(tila(HYVAKSYTTY))
    saanto(Toive(sitova, kausi = Some(kevat2027)), Toive(tila(HYVAKSYTTY)))() shouldBe Seq(sitova, tila(HYVAKSYTTY))
  }

  it should "not change toiveet outside the säännön piiri" in {
    saanto(Toive(sitova), Toive(tila(HYVAKSYTTY), kausi = None))(Set(syksy2026)) shouldBe
      Seq(sitova, tila(HYVAKSYTTY))
  }

  it should "not change toiveet with a stored vastaanotto" in {
    val peruttu     = tila(PERUNUT, Vastaanottotila.PERUNUT)
    val myohastynyt = tila(HYVAKSYTTY, Vastaanottotila.EI_VASTAANOTETTU_MAARA_AIKANA)
    saanto(Toive(sitova), Toive(peruttu), Toive(myohastynyt))() shouldBe Seq(sitova, peruttu, myohastynyt)
  }

  it should "change a toive whose vastaanotto deadline has passed without a stored vastaanotto" in {
    val myohastynyt = tila(HYVAKSYTTY, Vastaanottotila.EI_VASTAANOTETTU_MAARA_AIKANA)
    saanto(Toive(sitova), Toive(myohastynyt, tallennettu = Some(Vastaanottotila.KESKEN)))() shouldBe
      Seq(sitova, ottanutVastaanToisenPaikan)
  }

  it should "keep the valinnan tila of an unpublished toive" in {
    val julkaisematon = VtsTila(VARALLA, Vastaanottotila.KESKEN, julkaistu = false)
    saanto(Toive(julkaisematon))(Set(syksy2026)) shouldBe
      Seq(VtsTila(VARALLA, Vastaanottotila.OTTANUT_VASTAAN_TOISEN_PAIKAN, julkaistu = false))
  }

  private val eiAlkamiskautta = AlkamiskausiRow(None, None, None)

  private def hakukohde(
    hakuTila: Option[String] = Some("julkaistu"),
    hakukohdeTila: Option[String] = Some("julkaistu"),
    kohdejoukonTarkenne: Option[String] = None,
    koulutustyyppi: Option[String] = Some("yo"),
    johtaaTutkintoon: Option[Boolean] = Some(true),
    hakukohteenAlkamiskausi: AlkamiskausiRow = AlkamiskausiRow(Some("S"), Some(2026), None),
    haunAlkamiskausi: AlkamiskausiRow = eiAlkamiskautta,
    toteutuksenAlkamiskausi: AlkamiskausiRow = eiAlkamiskautta
  ) = YpsHakukohdeRow(
    hakuTila,
    hakukohdeTila,
    kohdejoukonTarkenne,
    koulutustyyppi,
    johtaaTutkintoon,
    hakukohteenAlkamiskausi,
    haunAlkamiskausi,
    toteutuksenAlkamiskausi
  )

  "yhdenPaikanSaantoVoimassa" should "hold for degree programmes of yliopistot and ammattikorkeakoulut" in {
    hakukohde(koulutustyyppi = Some("yo")).yhdenPaikanSaantoVoimassa shouldBe true
    hakukohde(koulutustyyppi = Some("amk")).yhdenPaikanSaantoVoimassa shouldBe true
  }

  it should "not hold for other koulutustyypit or koulutukset not leading to a degree" in {
    hakukohde(koulutustyyppi = Some("amm")).yhdenPaikanSaantoVoimassa shouldBe false
    hakukohde(koulutustyyppi = Some("kk-opintojakso")).yhdenPaikanSaantoVoimassa shouldBe false
    hakukohde(koulutustyyppi = None).yhdenPaikanSaantoVoimassa shouldBe false
    hakukohde(johtaaTutkintoon = Some(false)).yhdenPaikanSaantoVoimassa shouldBe false
    hakukohde(johtaaTutkintoon = None).yhdenPaikanSaantoVoimassa shouldBe false
  }

  it should "not hold for a luonnos haku or hakukohde, but hold for archived ones" in {
    hakukohde(hakuTila = Some("tallennettu")).yhdenPaikanSaantoVoimassa shouldBe false
    hakukohde(hakukohdeTila = Some("tallennettu")).yhdenPaikanSaantoVoimassa shouldBe false
    hakukohde(hakuTila = Some("arkistoitu"), hakukohdeTila = Some("arkistoitu")).yhdenPaikanSaantoVoimassa shouldBe true
  }

  it should "hold only for jatkotutkinto and Erasmus Mundus kohdejoukon tarkenteet" in {
    hakukohde(kohdejoukonTarkenne = Some("")).yhdenPaikanSaantoVoimassa shouldBe true
    hakukohde(kohdejoukonTarkenne = Some("haunkohdejoukontarkenne_3#1")).yhdenPaikanSaantoVoimassa shouldBe true
    hakukohde(kohdejoukonTarkenne = Some("haunkohdejoukontarkenne_010#1")).yhdenPaikanSaantoVoimassa shouldBe true
    hakukohde(kohdejoukonTarkenne = Some("haunkohdejoukontarkenne_1#1")).yhdenPaikanSaantoVoimassa shouldBe false
  }

  "ypsKausi" should "be the päätelty alkamiskausi of a hakukohde in the säännön piiri" in {
    hakukohde().ypsKausi shouldBe Some(syksy2026)
    hakukohde(koulutustyyppi = Some("amm")).ypsKausi shouldBe None
    hakukohde(hakukohteenAlkamiskausi = eiAlkamiskautta).ypsKausi shouldBe None
  }

  "paateltyAlkamiskausi" should "come from the hakukohde, then the haku, then the toteutus" in {
    val haku     = AlkamiskausiRow(Some("K"), Some(2027), None)
    val toteutus = AlkamiskausiRow(Some("S"), Some(2028), None)
    hakukohde(haunAlkamiskausi = haku, toteutuksenAlkamiskausi = toteutus).paateltyAlkamiskausi shouldBe
      Some(syksy2026)
    hakukohde(
      hakukohteenAlkamiskausi = eiAlkamiskautta,
      haunAlkamiskausi = haku,
      toteutuksenAlkamiskausi = toteutus
    ).paateltyAlkamiskausi shouldBe Some(kevat2027)
    hakukohde(
      hakukohteenAlkamiskausi = eiAlkamiskautta,
      toteutuksenAlkamiskausi = toteutus
    ).paateltyAlkamiskausi shouldBe Some(Alkamiskausi("S", 2028))
  }

  it should "skip a source without both kausi and vuosi or an alkamisaika" in {
    val vainKausi = AlkamiskausiRow(Some("S"), None, None)
    val haku      = AlkamiskausiRow(Some("K"), Some(2027), None)
    hakukohde(hakukohteenAlkamiskausi = vainKausi, haunAlkamiskausi = haku).paateltyAlkamiskausi shouldBe
      Some(kevat2027)
  }

  "AlkamiskausiRow.paatelty" should "derive the kausi from the alkamisaika in Finnish time" in {
    def paatelty(aika: String) = AlkamiskausiRow(None, None, Some(OffsetDateTime.parse(aika))).paatelty
    paatelty("2026-09-01T00:00:00+03:00") shouldBe Some(syksy2026)
    paatelty("2027-01-15T00:00:00+02:00") shouldBe Some(kevat2027)
    paatelty("2027-07-31T12:00:00+03:00") shouldBe Some(Alkamiskausi("K", 2027))
    // 2026-08-01 02:30 Suomen aikaa.
    paatelty("2026-07-31T23:30:00Z") shouldBe Some(syksy2026)
  }

  it should "prefer kausi and vuosi over the alkamisaika" in {
    AlkamiskausiRow(Some("K"), Some(2027), Some(OffsetDateTime.parse("2026-09-01T00:00:00+03:00"))).paatelty shouldBe
      Some(kevat2027)
  }
}
