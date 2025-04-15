package fi.oph.ovara.backend.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParameterValidatorSpec extends AnyFlatSpec with Matchers {

  "validateKoulutuksetToteutuksetHakukohteetParams" should "return errors for invalid parameters" in {
    val hakuList = List("invalid-oid")
    val maybeKoulutustoimija = Some("1234")
    val oppilaitosList = List("invalid-oid")
    val toimipisteList = List("1.2.246.562.10.1000000000000000001")
    val maybeKoulutuksenTila = Some("invalid-tila")
    val maybeToteutuksenTila = Some("julkaistu")
    val maybeHakukohteenTila = None
    val valintakoe = "invalid-valintakoe"

    val errors = ParameterValidator.validateKoulutuksetToteutuksetHakukohteetParams(
      hakuList,
      maybeKoulutustoimija,
      oppilaitosList,
      toimipisteList,
      maybeKoulutuksenTila,
      maybeToteutuksenTila,
      maybeHakukohteenTila,
      valintakoe
    )

    errors should contain("haut.invalid.oid")
    errors should contain("koulutustoimija.invalid.org")
    errors should contain("oppilaitokset.invalid.org")
    errors should contain("koulutuksen-tila.invalid")
    errors should contain("valintakoe.invalid")
    errors.length shouldEqual 5
  }

  it should "return no errors for valid parameters" in {
    val hakuList = List("1.2.246.562.29.00000000000000049925")
    val maybeKoulutustoimija = Some("1.2.246.562.10.1000000000000000000")
    val oppilaitosList = List("1.2.246.562.10.1000000000000000001")
    val toimipisteList = List("1.2.246.562.10.1000000000000000002")
    val maybeKoulutuksenTila = Some("julkaistu")
    val maybeToteutuksenTila = Some("julkaistu")
    val maybeHakukohteenTila = Some("julkaistu")
    val valintakoe = "false"

    val errors = ParameterValidator.validateKoulutuksetToteutuksetHakukohteetParams(
      hakuList,
      maybeKoulutustoimija,
      oppilaitosList,
      toimipisteList,
      maybeKoulutuksenTila,
      maybeToteutuksenTila,
      maybeHakukohteenTila,
      valintakoe
    )

    errors shouldBe empty
  }

  "validateKkKoulutuksetToteutuksetHakukohteetParams" should "return errors for invalid parameters" in {
    val hakuList = List("invalid-oid")
    val oppilaitosList = List("invalid-oid")
    val toimipisteList = List("1.2.246.999.10.1000000000000000001")
    val hakukohderyhmaList = List("invalid-oid")
    val tulostustapa = "invalid-tulostustapa"
    val maybeKoulutuksenTila = Some("invalid-tila")
    val maybeToteutuksenTila = Some("invalid-tila")
    val maybeHakukohteenTila = Some("invalid-tila")
    val tutkinnonTasoList = List("invalid-taso")

    val errors = ParameterValidator.validateKkKoulutuksetToteutuksetHakukohteetParams(
      hakuList,
      oppilaitosList,
      toimipisteList,
      hakukohderyhmaList,
      tulostustapa,
      maybeKoulutuksenTila,
      maybeToteutuksenTila,
      maybeHakukohteenTila,
      tutkinnonTasoList,
    )

    errors.length shouldEqual 9
    errors should contain("haut.invalid.oid")
    errors should contain("oppilaitokset.invalid.org")
    errors should contain("toimipisteet.invalid.org")
    errors should contain("hakukohderyhmat.invalid.oid")
    errors should contain("tulostustapa.invalid")
    errors should contain("koulutuksen-tila.invalid")
    errors should contain("toteutuksen-tila.invalid")
    errors should contain("hakukohteen-tila.invalid")
    errors should contain("tutkinnontasot.invalid")
  }

  "validateHakijatParams" should "return errors for invalid parameters" in {
    val hakuList = List("invalid-oid")
    val oppilaitosList = List("invalid-oid")
    val toimipisteList = List("1.2.246.999.10.1000000000000000001")
    val pohjakoulutusList = List("abc")
    val valintatietoList = List("hyvaksytty'||pg_sleep(20)–")
    val vastaanottotietoList = List("FOOBAR'")
    val harkinnanvaraisuusList = List("FOOBAR'")
    val invalidBoolean = "invalid-boolean"

    val errors = ParameterValidator.validateHakijatParams(
      hakuList,
      oppilaitosList,
      toimipisteList,
      pohjakoulutusList,
      valintatietoList,
      vastaanottotietoList,
      harkinnanvaraisuusList,
      invalidBoolean,
      invalidBoolean,
      invalidBoolean,
      invalidBoolean,
      invalidBoolean,
      invalidBoolean,
    )

    errors.length shouldEqual 13
    errors should contain("haut.invalid.oid")
    errors should contain("oppilaitokset.invalid.org")
    errors should contain("pohjakoulutukset.invalid")
    errors should contain("valintatiedot.invalid")
    errors should contain("harkinnanvaraisuudet.invalid")
    errors should contain("vastaanottotiedot.invalid")
    errors should contain("kaksoistutkinto-kiinnostaa.invalid")
    errors should contain("urheilijatutkinto-kiinnostaa.invalid")
    errors should contain("soraterveys.invalid")
    errors should contain("sora-aiempi.invalid")
    errors should contain("markkinointilupa.invalid")
    errors should contain("julkaisulupa.invalid")
  }
}
