package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.external.kkhakijat.Valintatila.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

// Hakutoiveiden väliset säännöt valinta-tulos-servicen ValintatulosService.julkaistavaTulosin tapaan.
class ExternalKKHakijatHakutoiveidenValisetSaannotTest extends AnyFlatSpec with Matchers {

  private def tila(
    valinnanTila: Valintatila,
    vastaanotto: Vastaanottotila = Vastaanottotila.KESKEN,
    julkaistu: Boolean = true
  ) = VtsTila(valinnanTila, vastaanotto, julkaistu)

  private val julkaisematon = VtsTila(KESKEN, Vastaanottotila.KESKEN, julkaistu = false)

  /** Hakutoiveet annetussa hakutoivejärjestyksessä; tulos samassa järjestyksessä. */
  private def saannot(
    hakutoiveet: VtsTila*
  )(sijoitteluJaPriorisointi: Boolean = true, varasijasaannotVoimassa: Boolean = true): Seq[VtsTila] = {
    val tulos = hakemuksenVtsTilat(
      hakutoiveet.zipWithIndex.map { case (t, i) => (i, i + 1, t) },
      sijoitteluJaPriorisointi,
      varasijasaannotVoimassa
    )
    hakutoiveet.indices.map(tulos)
  }

  "Julkaisematonta alemmat (näytäJulkaisematontaAlemmatPeruuntuneetKeskeneräisinä)" should
    "show peruuntunut and kesken toiveet below an unpublished toive as kesken and unpublished" in {
      saannot(julkaisematon, tila(PERUUNTUNUT), tila(KESKEN), tila(HYLATTY))() shouldBe
        Seq(julkaisematon, julkaisematon, julkaisematon, tila(HYLATTY))
    }

  "Valmista alemmat (peruunnutaValmistaAlemmatKeskeneräisetJosKäytetäänSijoittelua)" should
    "make kesken toiveet below a hyväksytty toive peruuntunut" in {
      saannot(tila(HYVAKSYTTY), tila(KESKEN))() shouldBe Seq(tila(HYVAKSYTTY), tila(PERUUNTUNUT))
    }

  it should "make kesken toiveet below a perunut toive peruuntunut" in {
    saannot(tila(PERUNUT, Vastaanottotila.PERUNUT), tila(KESKEN))() shouldBe
      Seq(tila(PERUNUT, Vastaanottotila.PERUNUT), tila(PERUUNTUNUT))
  }

  it should "not change kesken toiveet above the first hyväksytty toive" in {
    saannot(tila(KESKEN), tila(HYVAKSYTTY))() shouldBe Seq(tila(KESKEN), tila(HYVAKSYTTY))
  }

  "Varasijalta hyväksytty (näytäVarasijaltaHyväksytytHyväksyttyinäJosVarasijasäännötEiVoimassa)" should
    "be shown as hyväksytty while the varasija rules are not in force, also without sijoittelu" in {
      saannot(tila(VARASIJALTA_HYVAKSYTTY))(varasijasaannotVoimassa = false) shouldBe Seq(tila(HYVAKSYTTY))
      saannot(tila(VARASIJALTA_HYVAKSYTTY))(sijoitteluJaPriorisointi = false, varasijasaannotVoimassa = false) shouldBe
        Seq(tila(HYVAKSYTTY))
      saannot(tila(VARASIJALTA_HYVAKSYTTY))() shouldBe Seq(tila(VARASIJALTA_HYVAKSYTTY))
    }

  "Sitovasti vastaanotettu (sovellaSijoitteluaKayttanvaKorkeakouluhaunSaantoja)" should
    "make the other hyväksytty toiveet without a vastaanotto peruuntunut" in {
      val sitova = tila(HYVAKSYTTY, Vastaanottotila.VASTAANOTTANUT_SITOVASTI)
      saannot(tila(HYVAKSYTTY), sitova)() shouldBe Seq(tila(PERUUNTUNUT), sitova)
    }

  it should "not change a hyväksytty toive whose vastaanotto deadline has passed" in {
    val myohastynyt = tila(HYVAKSYTTY, Vastaanottotila.EI_VASTAANOTETTU_MAARA_AIKANA)
    val sitova      = tila(HYVAKSYTTY, Vastaanottotila.VASTAANOTTANUT_SITOVASTI)
    saannot(myohastynyt, sitova)() shouldBe Seq(myohastynyt, sitova)
  }

  it should "make varalla and kesken toiveet peruuntunut" in {
    val sitova = tila(HYVAKSYTTY, Vastaanottotila.VASTAANOTTANUT_SITOVASTI)
    saannot(tila(VARALLA), tila(KESKEN), sitova)() shouldBe Seq(tila(PERUUNTUNUT), tila(PERUUNTUNUT), sitova)
  }

  it should "not apply to an ehdollinen vastaanotto" in {
    val ehdollinen = tila(HYVAKSYTTY, Vastaanottotila.EHDOLLISESTI_VASTAANOTTANUT)
    saannot(tila(VARALLA), ehdollinen)() shouldBe Seq(tila(VARALLA), ehdollinen)
  }

  "Keskeneräistä alemmat (näytäAlemmatPeruuntuneetHakukohteetKeskeneräisinäJosYlemmätKeskeneräisiä)" should
    "show peruuntunut toiveet below a kesken toive as kesken and unpublished" in {
      saannot(tila(KESKEN), tila(PERUUNTUNUT))() shouldBe Seq(tila(KESKEN), julkaisematon)
    }

  "Without sijoittelu and hakutoiveiden priorisointi" should "not apply the rules between hakutoiveet" in {
    val sitova = tila(HYVAKSYTTY, Vastaanottotila.VASTAANOTTANUT_SITOVASTI)
    saannot(julkaisematon, tila(PERUUNTUNUT))(sijoitteluJaPriorisointi = false) shouldBe
      Seq(julkaisematon, tila(PERUUNTUNUT))
    saannot(tila(HYVAKSYTTY), tila(KESKEN))(sijoitteluJaPriorisointi = false) shouldBe
      Seq(tila(HYVAKSYTTY), tila(KESKEN))
    saannot(tila(VARALLA), sitova)(sijoitteluJaPriorisointi = false) shouldBe Seq(tila(VARALLA), sitova)
    saannot(tila(KESKEN), tila(PERUUNTUNUT))(sijoitteluJaPriorisointi = false) shouldBe
      Seq(tila(KESKEN), tila(PERUUNTUNUT))
  }

  "hakemuksenVtsTilat" should "order the toiveet by hakutoivenumero, not by input order" in {
    val tulos = hakemuksenVtsTilat(
      Seq(("toinen", 2, tila(KESKEN)), ("ensimmainen", 1, tila(HYVAKSYTTY))),
      sijoitteluJaPriorisointi = true,
      varasijasaannotVoimassa = true
    )
    tulos shouldBe Map("ensimmainen" -> tila(HYVAKSYTTY), "toinen" -> tila(PERUUNTUNUT))
  }
}
