package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.valpas.ValpasFactory.*
import org.scalatest.*
import org.scalatest.flatspec.*

import java.time.OffsetDateTime
import scala.jdk.CollectionConverters.*

class HakutoiveRowSpec extends AnyFlatSpec {

  "asHakutoive" should "return tilatiedot when they are present" in {
    val toive = createHakutoiveRow()

    val result = toive.asHakutoive(Map(KOULUTUS_KOODIURI -> createKoulutusKoodi()), Some(YESTERDAY))

    assert(result.vastaanottotieto == "VASTAANOTTANUT_SITOVASTI")
    assert(result.ilmoittautumistila == "LASNA_KOKO_LUKUVUOSI")
    assert(result.valintatila == "HYVAKSYTTY")
  }

  it should "return tilatiedot as KESKEN when they are missing" in {
    val toive = createHakutoiveRow(vastaanottotieto = None, ilmoittautumistila = None, valintatila = None)

    val result = toive.asHakutoive(Map(KOULUTUS_KOODIURI -> createKoulutusKoodi()), Some(YESTERDAY))

    assert(result.vastaanottotieto == "KESKEN")
    assert(result.ilmoittautumistila == "EI_TEHTY")
    assert(result.valintatila == "KESKEN")
  }

  it should "return tilatiedot as KESKEN when julkaistavissa is missing" in {
    val toive = createHakutoiveRow(julkaistavissa = None)

    val result = toive.asHakutoive(Map(KOULUTUS_KOODIURI -> createKoulutusKoodi()), Some(YESTERDAY))

    assert(result.vastaanottotieto == "KESKEN")
    assert(result.ilmoittautumistila == "EI_TEHTY")
    assert(result.valintatila == "KESKEN")
  }

  it should "return tilatiedot as KESKEN when julkaistavissa is false" in {
    val toive = createHakutoiveRow(julkaistavissa = Some(false))

    val result = toive.asHakutoive(Map(KOULUTUS_KOODIURI -> createKoulutusKoodi()), Some(YESTERDAY))

    assert(result.vastaanottotieto == "KESKEN")
    assert(result.ilmoittautumistila == "EI_TEHTY")
    assert(result.valintatila == "KESKEN")
  }

  it should "return tilatiedot as KESKEN when PH_VTJH is in the future" in {
    val toive = createHakutoiveRow()

    val result =
      toive.asHakutoive(Map(KOULUTUS_KOODIURI -> createKoulutusKoodi()), Some(OffsetDateTime.now().plusMinutes(1)))

    assert(result.vastaanottotieto == "KESKEN")
    assert(result.ilmoittautumistila == "EI_TEHTY")
    assert(result.valintatila == "KESKEN")
  }

  it should "return tilatiedot when PH_VTJH is missing" in {
    val toive = createHakutoiveRow()

    val result = toive.asHakutoive(Map(KOULUTUS_KOODIURI -> createKoulutusKoodi()), None)

    assert(result.vastaanottotieto == "VASTAANOTTANUT_SITOVASTI")
    assert(result.ilmoittautumistila == "LASNA_KOKO_LUKUVUOSI")
    assert(result.valintatila == "HYVAKSYTTY")
  }

  it should "return pisteet when they are positive" in {
    val toive = createHakutoiveRow(pisteet = Some(23.7))

    val result = toive.asHakutoive(Map(KOULUTUS_KOODIURI -> createKoulutusKoodi()), Some(YESTERDAY))

    assert(result.pisteet.contains(BigDecimal(23.7)))
  }

  it should "return pisteet when they are zero" in {
    val toive = createHakutoiveRow(pisteet = Some(0))

    val result = toive.asHakutoive(Map(KOULUTUS_KOODIURI -> createKoulutusKoodi()), Some(YESTERDAY))

    assert(result.pisteet.contains(BigDecimal(0)))
  }

  it should "not return pisteet when they are negative" in {
    val toive = createHakutoiveRow(pisteet = Some(-1.5))

    val result = toive.asHakutoive(Map(KOULUTUS_KOODIURI -> createKoulutusKoodi()), Some(YESTERDAY))

    assert(result.pisteet.isEmpty)
    assert(result.valintatila == "HYVAKSYTTY")
    assert(result.alinHyvaksyttyPistemaara.contains(BigDecimal(21.1)))
    assert(result.varasijanumero.contains(4))
  }

  it should "not return pisteet when julkaistavissa is false" in {
    val toive = createHakutoiveRow(julkaistavissa = Some(false))

    val result = toive.asHakutoive(Map(KOULUTUS_KOODIURI -> createKoulutusKoodi()), Some(YESTERDAY))

    assert(result.pisteet.isEmpty)
  }
}
