package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.valpas.ValpasFactory.*
import org.scalatest.flatspec.AnyFlatSpec

class HakemusRowSpec extends AnyFlatSpec {
  "isAktiivinen" should "be true when first hakuaika has started and hakukierros has not ended" in {
    val hakemus = createHakemusRow()

    assert(hakemus.isAktiivinen.contains(true))
  }
  it should "be true when any hakuaika has started" in {
    val hakemus = createHakemusRow(hakuajat =
      List(
        createValpasHakuaika(TOMORROW.toLocalDateTime, null),
        createValpasHakuaika(YESTERDAY.toLocalDateTime, null),
        createValpasHakuaika(null, null)
      )
    )

    assert(hakemus.isAktiivinen.contains(true))
  }

  it should "be None when hakuajat is empty" in {
    val hakemus = createHakemusRow(hakuajat = List.empty)

    assert(hakemus.isAktiivinen.isEmpty)
  }

  it should "be None when hakuajat has no start date" in {
    val hakemus = createHakemusRow(hakuajat = List(createValpasHakuaika(null, TOMORROW.toLocalDateTime)))

    assert(hakemus.isAktiivinen.isEmpty)
  }

  it should "be None when hakukierros paattyy is not defined" in {
    val hakemus = createHakemusRow(hakukierrosPaattyy = None)

    assert(hakemus.isAktiivinen.isEmpty)
  }

  it should "be false when first hakuaika is in the future" in {
    val hakemus = createHakemusRow(hakuajat = List(createValpasHakuaika(TOMORROW.toLocalDateTime, null)))

    assert(hakemus.isAktiivinen.contains(false))
  }

  it should "be false when hakukierros päättyy is in the past" in {
    val hakemus = createHakemusRow(hakukierrosPaattyy = Some(YESTERDAY))

    assert(hakemus.isAktiivinen.contains(false))
  }
}
