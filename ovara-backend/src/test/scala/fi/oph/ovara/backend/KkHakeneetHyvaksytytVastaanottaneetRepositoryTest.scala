package fi.oph.ovara.backend

import fi.oph.ovara.backend.repository.KkHakeneetHyvaksytytVastaanottaneetRepository
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KkHakeneetHyvaksytytVastaanottaneetRepositoryTest extends AnyFlatSpec with Matchers {

  "buildTutkinnonTasoFilters" should "return None when the list is empty" in {
    val repository = new KkHakeneetHyvaksytytVastaanottaneetRepository
    val result = repository.buildTutkinnonTasoFilters(List.empty)
    result shouldBe None
  }

  it should "return the correct filter for 'alempi-ja-ylempi'" in {
    val repository = new KkHakeneetHyvaksytytVastaanottaneetRepository
    val result = repository.buildTutkinnonTasoFilters(List("alempi-ja-ylempi"))
    result shouldBe Some("AND (h.alempi_kk_aste = true AND h.ylempi_kk_aste = true)")
  }

  it should "return the correct filter for 'alempi'" in {
    val repository = new KkHakeneetHyvaksytytVastaanottaneetRepository
    val result = repository.buildTutkinnonTasoFilters(List("alempi"))
    result shouldBe Some("AND (h.alempi_kk_aste = true AND h.ylempi_kk_aste = false)")
  }

  it should "return the correct filter for 'ylempi'" in {
    val repository = new KkHakeneetHyvaksytytVastaanottaneetRepository
    val result = repository.buildTutkinnonTasoFilters(List("ylempi"))
    result shouldBe Some("AND (h.alempi_kk_aste = false AND h.ylempi_kk_aste = true)")
  }

  it should "return the correct filter for multiple values" in {
    val repository = new KkHakeneetHyvaksytytVastaanottaneetRepository
    val result = repository.buildTutkinnonTasoFilters(List("alempi", "ylempi"))
    result shouldBe Some("AND (h.alempi_kk_aste = true AND h.ylempi_kk_aste = false) OR (h.alempi_kk_aste = false AND h.ylempi_kk_aste = true)")
  }
}