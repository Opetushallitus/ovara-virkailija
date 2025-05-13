package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.raportointi.dto.{RawHakeneetHyvaksytytVastaanottaneetParams, RawHakijatParams, RawKkHakeneetHyvaksytytVastaanottaneetParams, RawKkHakijatParams, RawKoulutuksetToteutuksetHakukohteetParams, ValidatedHakeneetHyvaksytytVastaanottaneetParams, ValidatedKkHakeneetHyvaksytytVastaanottaneetParams, ValidatedKoulutuksetToteutuksetHakukohteetParams}
import fi.oph.ovara.backend.utils.AuditOperation.KoulutuksetToteutuksetHakukohteet
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParameterValidatorSpec extends AnyFlatSpec with Matchers {

  "validateKoulutuksetToteutuksetHakukohteetParams" should "return errors for invalid parameters" in {
    val params = RawKoulutuksetToteutuksetHakukohteetParams(
      haut = List("invalid-oid"),
      koulutustoimija = Some("1234"),
      oppilaitokset = List("invalid-oid"),
      toimipisteet = List("1.2.246.562.10.1000000000000000001"),
      koulutuksenTila = Some("invalid-tila"),
      toteutuksenTila = Some("julkaistu"),
      hakukohteenTila = None,
      valintakoe = "invalid-valintakoe"
    )

    val result = ParameterValidator.validateKoulutuksetToteutuksetHakukohteetParams(params)

    result shouldBe Left(List(
      "haut.invalid.oid",
      "koulutustoimija.invalid.org",
      "oppilaitokset.invalid.org",
      "koulutuksen-tila.invalid",
      "valintakoe.invalid"
    ))
  }

  it should "return no errors for valid parameters" in {
    val params = RawKoulutuksetToteutuksetHakukohteetParams(
      haut = List("1.2.246.562.29.00000000000000049925"),
      koulutustoimija = Some("1.2.246.562.10.1000000000000000000"),
      oppilaitokset = List("1.2.246.562.10.1000000000000000001"),
      toimipisteet = List("1.2.246.562.10.1000000000000000002"),
      koulutuksenTila = Some("julkaistu"),
      toteutuksenTila = Some("julkaistu"),
      hakukohteenTila = Some("julkaistu"),
      valintakoe = "false"
    )

    val result = ParameterValidator.validateKoulutuksetToteutuksetHakukohteetParams(params)

    val expected =  ValidatedKoulutuksetToteutuksetHakukohteetParams(
      haut = List("1.2.246.562.29.00000000000000049925"),
      koulutustoimija = Some("1.2.246.562.10.1000000000000000000"),
      oppilaitokset = List("1.2.246.562.10.1000000000000000001"),
      toimipisteet = List("1.2.246.562.10.1000000000000000002"),
      koulutuksenTila = Some("julkaistu"),
      toteutuksenTila = Some("julkaistu"),
      hakukohteenTila = Some("julkaistu"),
      valintakoe = Some(false))

    result shouldBe Right(expected)
  }


  "validateHakijatParams" should "return errors for invalid parameters" in {
    val params = RawHakijatParams(
      haut = List("invalid-oid"),
      oppilaitokset = List("invalid-oid"),
      toimipisteet = List("1.2.246.999.10.1000000000000000001"),
      hakukohteet = List("invalid-oid"),
      pohjakoulutukset = List("abc"),
      valintatiedot = List("hyvaksytty'||pg_sleep(20)–"),
      vastaanottotiedot = List("FOOBAR'"),
      harkinnanvaraisuudet = List("FOOBAR'"),
      kaksoistutkinto = "invalid-boolean",
      urheilijatutkinto = "invalid-boolean",
      soraTerveys = "invalid-boolean",
      soraAiempi = "invalid-boolean",
      markkinointilupa = "invalid-boolean",
      julkaisulupa = "invalid-boolean"
    )

    val result = ParameterValidator.validateHakijatParams(params)

    result shouldBe Left(List(
      "haut.invalid.oid",
      "oppilaitokset.invalid.org",
      "toimipisteet.invalid.org",
      "hakukohteet.invalid.oid",
      "pohjakoulutukset.invalid",
      "valintatiedot.invalid",
      "vastaanottotiedot.invalid",
      "harkinnanvaraisuudet.invalid",
      "kaksoistutkinto-kiinnostaa.invalid",
      "urheilijatutkinto-kiinnostaa.invalid",
      "soraterveys.invalid",
      "sora-aiempi.invalid",
      "markkinointilupa.invalid",
      "julkaisulupa.invalid"
    ))
  }

  "validateKkHakijatParams" should "return errors for invalid parameters" in {
    val params = RawKkHakijatParams(
      haut = List("invalid-oid"),
      oppilaitokset = List("invalid-oid"),
      toimipisteet = List("1.2.246.999.10.1000000000000000001"),
      hakukohteet = List("invalid-oid"),
      valintatiedot = List("hyvaksytty'||pg_sleep(20)–"),
      vastaanottotiedot = List("FOOBAR'"),
      hakukohderyhmat = List("invalid-oid"),
      kansalaisuusluokat = List("abc"),
      markkinointilupa = "invalid-boolean",
      naytaYoArvosanat = "invalid-boolean",
      naytaHetu = "invalid-boolean",
      naytaPostiosoite = "invalid-boolean"
    )

    val result = ParameterValidator.validateKkHakijatParams(params)

    result shouldBe Left(List(
      "haut.invalid.oid",
      "oppilaitokset.invalid.org",
      "toimipisteet.invalid.org",
      "hakukohteet.invalid.oid",
      "valintatiedot.invalid",
      "vastaanottotiedot.invalid",
      "hakukohderyhmat.invalid.oid",
      "kansalaisuusluokat.invalid",
      "markkinointilupa.invalid",
      "nayta-yo-arvosanat.invalid",
      "nayta-hetu.invalid",
      "nayta-postiosoite.invalid"
    ))
  }

  "validateHakeneetHyvaksytytVastaanottaneetParams" should "return errors for invalid parameters" in {
    val params = RawHakeneetHyvaksytytVastaanottaneetParams(
      haut = List("invalid-oid"),
      tulostustapa = "invalid-tulostustapa",
      koulutustoimija = Some("invalid-koulutustoimija"),
      oppilaitokset = List("invalid-oid"),
      toimipisteet = List("1.2.246.999.10.1000000000000000001"),
      hakukohteet = List("invalid-oid"),
      koulutusalat1 = List("foo"),
      koulutusalat2 = List("bar"),
      koulutusalat3 = List("baz"),
      opetuskielet = List("invalid*-kieli"),
      maakunnat = List("invalid*-maakunta"),
      kunnat = List("invalid*-kunta"),
      harkinnanvaraisuudet = List("FOOBAR''"),
      sukupuoli = Some("invalid-sukupuoli"),
      naytaHakutoiveet = "invalid-boolean"
    )

    val result = ParameterValidator.validateHakeneetHyvaksytytVastaanottaneetParams(params)

    result shouldBe Left(List(
      "haut.invalid.oid",
      "tulostustapa.invalid",
      "koulutustoimija.invalid.org",
      "oppilaitokset.invalid.org",
      "toimipisteet.invalid.org",
      "hakukohteet.invalid.oid",
      "koulutusalat1.invalid",
      "koulutusalat2.invalid",
      "koulutusalat3.invalid",
      "opetuskielet.invalid",
      "maakunnat.invalid",
      "kunnat.invalid",
      "harkinnanvaraisuudet.invalid",
      "sukupuoli.invalid",
      "nayta-hakutoiveet.invalid"
    ))
  }

  "validateKkHakeneetHyvaksytytVastaanottaneetParams" should "return errors for invalid parameters" in {
    val params = RawKkHakeneetHyvaksytytVastaanottaneetParams(
      haut = List("invalid-oid"),
      tulostustapa = "invalid-tulostustapa",
      koulutustoimija = Some("invalid-koulutustoimija"),
      oppilaitokset = List("invalid-oid"),
      toimipisteet = List("1.2.246.999.10.1000000000000000001"),
      hakukohteet = List("invalid-oid"),
      hakukohderyhmat = List("invalid-oid"),
      okmOhjauksenAlat = List("foo"),
      tutkinnonTasot = List("bar"),
      aidinkielet = List("baz'"),
      kansalaisuusluokat = List("invalid-kansalaisuus"),
      sukupuoli = Some("invalid-sukupuoli"),
      ensikertalainen = "invalid-boolean",
      naytaHakutoiveet = "invalid-boolean"
    )

    val result = ParameterValidator.validateKkHakeneetHyvaksytytVastaanottaneetParams(params)

    result shouldBe Left(List(
      "haut.invalid.oid",
      "tulostustapa.invalid",
      "koulutustoimija.invalid.org",
      "oppilaitokset.invalid.org",
      "toimipisteet.invalid.org",
      "hakukohteet.invalid.oid",
      "hakukohderyhmat.invalid.oid",
      "okm-ohjauksen-alat.invalid",
      "tutkinnontasot.invalid",
      "aidinkielet.invalid",
      "kansalaisuusluokat.invalid",
      "sukupuoli.invalid",
      "ensikertalainen.invalid",
      "nayta-hakutoiveet.invalid"
    ))
  }

  it should "return no errors for valid parameters" in {

    val params = RawHakeneetHyvaksytytVastaanottaneetParams(
      haut = List("1.2.246.562.29.00000000000000056840"),
      tulostustapa = "hakukohteittain",
      koulutustoimija = Some("1.2.246.562.10.346830761110"),
      oppilaitokset = List("1.2.246.562.10.52251087186"),
      toimipisteet = List("1.2.246.562.10.55711304158"),
      hakukohteet = List("1.2.246.562.20.00000000000000059958"),
      koulutusalat1 = List("09"),
      koulutusalat2 = List("092"),
      koulutusalat3 = List("0922"),
      opetuskielet = List("oppilaitoksenopetuskieli_1"),
      maakunnat = List("maakunta_21"),
      kunnat = List("kunta_004"),
      harkinnanvaraisuudet = List("ATARU_EI_PAATTOTODISTUSTA"),
      sukupuoli = Some("2"),
      naytaHakutoiveet = "true"
    )

    val expected = ValidatedHakeneetHyvaksytytVastaanottaneetParams(
      haut = List("1.2.246.562.29.00000000000000056840"),
      tulostustapa = "hakukohteittain",
      koulutustoimija = Some("1.2.246.562.10.346830761110"),
      oppilaitokset = List("1.2.246.562.10.52251087186"),
      toimipisteet = List("1.2.246.562.10.55711304158"),
      hakukohteet = List("1.2.246.562.20.00000000000000059958"),
      koulutusalat1 = List("09"),
      koulutusalat2 = List("092"),
      koulutusalat3 = List("0922"),
      opetuskielet = List("oppilaitoksenopetuskieli_1"),
      maakunnat = List("maakunta_21"),
      kunnat = List("kunta_004"),
      harkinnanvaraisuudet = List("ATARU_EI_PAATTOTODISTUSTA"),
      sukupuoli = Some("2"),
      naytaHakutoiveet = true,
    )
    val result = ParameterValidator.validateHakeneetHyvaksytytVastaanottaneetParams(params)

    result shouldBe Right(expected)
  }
}
