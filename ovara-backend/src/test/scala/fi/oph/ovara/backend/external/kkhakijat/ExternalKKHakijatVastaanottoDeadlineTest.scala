package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.utils.Constants.HELSINKI_TIMEZONE
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.{LocalDateTime, OffsetDateTime}

// Vastaanoton määräajan laskenta valinta-tulos-servicen VastaanottoUtils.laskeVastaanottoDeadlinen tapaan.
class ExternalKKHakijatVastaanottoDeadlineTest extends AnyFlatSpec with Matchers {

  private def hki(aika: String): OffsetDateTime =
    LocalDateTime.parse(aika).atZone(HELSINKI_TIMEZONE).toOffsetDateTime

  private def parametrit(
    vastaanottoPaattyy: Option[String],
    paivia: Option[Int] = None,
    valintaesitysHyvaksyttavissa: Option[String] = None
  ) = HaunOhjausparametrit(
    vastaanottoPaattyy = vastaanottoPaattyy.map(hki),
    hakijakohtainenVastaanottoaikaPaivia = paivia,
    valintaesitysHyvaksyttavissa = valintaesitysHyvaksyttavissa.map(hki),
    varasijasaannotVoimaan = None,
    kaikkiJonotSijoittelussa = None,
    sijoittelu = false,
    jarjestetytHakutoiveet = false
  )

  private def deadline(p: HaunOhjausparametrit, julkaistu: Option[String]) =
    vastaanottoDeadline(p, julkaistu.map(hki)).map(_.toInstant)

  "vastaanottoDeadline" should "be missing without the haku's vastaanotto deadline (PH_OPVP)" in {
    deadline(parametrit(None, Some(14)), Some("2026-06-01T10:00")) shouldBe None
  }

  it should "be the haku's deadline without hakijakohtainen vastaanottoaika (PH_HPVOA)" in {
    deadline(parametrit(Some("2026-06-05T15:00")), Some("2026-06-01T10:00")) shouldBe
      Some(hki("2026-06-05T15:00").toInstant)
  }

  it should "add the buffer days to the publication with the time of the haku's deadline" in {
    deadline(parametrit(Some("2026-06-05T15:00"), Some(14)), Some("2026-06-01T10:00")) shouldBe
      Some(hki("2026-06-15T15:00").toInstant)
  }

  it should "add one more day when published later in the day than the haku's deadline" in {
    deadline(parametrit(Some("2026-06-05T15:00"), Some(14)), Some("2026-06-01T16:00")) shouldBe
      Some(hki("2026-06-16T15:00").toInstant)
  }

  it should "not add a day when published exactly at the time of the haku's deadline" in {
    deadline(parametrit(Some("2026-06-05T15:00"), Some(14)), Some("2026-06-01T15:00")) shouldBe
      Some(hki("2026-06-15T15:00").toInstant)
  }

  it should "count from a later PH_VEH, with the extra day still decided by the publication time" in {
    val p = parametrit(Some("2026-06-05T15:00"), Some(14), valintaesitysHyvaksyttavissa = Some("2026-06-03T09:00"))
    deadline(p, Some("2026-06-01T16:00")) shouldBe Some(hki("2026-06-18T15:00").toInstant)
  }

  it should "be the haku's deadline when it is later than the hakijakohtainen deadline" in {
    deadline(parametrit(Some("2026-07-01T15:00"), Some(14)), Some("2026-06-01T10:00")) shouldBe
      Some(hki("2026-07-01T15:00").toInstant)
  }

  it should "add days to the publication with its fixed offset across a DST change, as VTS does" in {
    // 2026-03-20 23:30 +02:00 + 15 päivää kiinteällä +02:00:lla = 2026-04-05 00:30 Suomen kesäaikaa.
    deadline(parametrit(Some("2026-03-01T15:00"), Some(14)), Some("2026-03-20T23:30")) shouldBe
      Some(hki("2026-04-05T15:00").toInstant)
  }

  it should "add days to PH_VEH in Helsinki time when it equals the publication" in {
    val p = parametrit(Some("2026-03-01T15:00"), Some(14), valintaesitysHyvaksyttavissa = Some("2026-03-20T23:30"))
    deadline(p, Some("2026-03-20T23:30")) shouldBe Some(hki("2026-04-04T15:00").toInstant)
  }
}
