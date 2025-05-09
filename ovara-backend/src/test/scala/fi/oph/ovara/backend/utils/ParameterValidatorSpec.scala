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
    val hakukohteetList = List("invalid-oid")
    val pohjakoulutusList = List("abc")
    val valintatietoList = List("hyvaksytty'||pg_sleep(20)–")
    val vastaanottotietoList = List("FOOBAR'")
    val harkinnanvaraisuusList = List("FOOBAR'")
    val invalidBoolean = "invalid-boolean"

    val errors = ParameterValidator.validateHakijatParams(
      hakuList,
      oppilaitosList,
      toimipisteList,
      hakukohteetList,
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

    errors.length shouldEqual 14
    errors should contain("haut.invalid.oid")
    errors should contain("oppilaitokset.invalid.org")
    errors should contain("toimipisteet.invalid.org")
    errors should contain("hakukohteet.invalid.oid")
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

  "validateKkHakijatParams" should "return errors for invalid parameters" in {
    val hakuList = List("invalid-oid")
    val oppilaitosList = List("invalid-oid")
    val toimipisteList = List("1.2.246.999.10.1000000000000000001")
    val hakukohteetList = List("invalid-oid")
    val valintatietoList = List("hyvaksytty'||pg_sleep(20)–")
    val vastaanottotietoList = List("FOOBAR'")
    val hakukohderyhmaList = List("invalid-oid")
    val kansalaisuusList = List("abc")
    val invalidBoolean = "invalid-boolean"

    val errors = ParameterValidator.validateKkHakijatParams(
      hakuList,
      oppilaitosList,
      toimipisteList,
      hakukohteetList,
      valintatietoList,
      vastaanottotietoList,
      hakukohderyhmaList,
      kansalaisuusList,
      invalidBoolean,
      invalidBoolean,
      invalidBoolean,
      invalidBoolean,
    )

    errors.length shouldEqual 12
    errors should contain("haut.invalid.oid")
    errors should contain("oppilaitokset.invalid.org")
    errors should contain("toimipisteet.invalid.org")
    errors should contain("hakukohteet.invalid.oid")
    errors should contain("valintatiedot.invalid")
    errors should contain("vastaanottotiedot.invalid")
    errors should contain("hakukohderyhmat.invalid.oid")
    errors should contain("kansalaisuusluokat.invalid")
    errors should contain("markkinointilupa.invalid")
    errors should contain("nayta-yo-arvosanat.invalid")
    errors should contain("nayta-hetu.invalid")
    errors should contain("nayta-postiosoite.invalid")
  }

  "validateHakeneetHyvaksytytVastaanottaneetParams" should "return errors for invalid parameters" in {
    val hakuList = List("invalid-oid")
    val tulostustapa = "invalid-tulostustapa"
    val koulutustoimija = Some("invalid-koulutustoimija")
    val oppilaitosList = List("invalid-oid")
    val toimipisteList = List("1.2.246.999.10.1000000000000000001")
    val hakukohteetList = List("invalid-oid")
    val koulutusala1List = List("foo")
    val koulutusala2List = List("bar")
    val koulutusala3List = List("baz")
    val opetuskieliList = List("invalid-kieli")
    val maakuntaList = List("invalid-maakunta")
    val kuntaList = List("invalid-kunta")
    val harkinnanvaraisuusList = List("FOOBAR''")
    val sukupuoli = Some("invalid-sukupuoli")
    val invalidBoolean = "invalid-boolean"

    val errors = ParameterValidator.validateHakeneetHyvaksytytVastaanottaneetParams(
      hakuList,
      tulostustapa,
      koulutustoimija,
      oppilaitosList,
      toimipisteList,
      hakukohteetList,
      koulutusala1List,
      koulutusala2List,
      koulutusala3List,
      opetuskieliList,
      maakuntaList,
      kuntaList,
      harkinnanvaraisuusList,
      sukupuoli,
      invalidBoolean,
    )

    errors.length shouldEqual 15
    errors should contain("haut.invalid.oid")
    errors should contain("tulostustapa.invalid")
    errors should contain("koulutustoimija.invalid.org")
    errors should contain("oppilaitokset.invalid.org")
    errors should contain("toimipisteet.invalid.org")
    errors should contain("hakukohteet.invalid.oid")
    errors should contain("koulutusalat1.invalid")
    errors should contain("koulutusalat2.invalid")
    errors should contain("koulutusalat3.invalid")
    errors should contain("opetuskielet.invalid")
    errors should contain("maakunnat.invalid")
    errors should contain("kunnat.invalid")
    errors should contain("harkinnanvaraisuudet.invalid")
    errors should contain("sukupuoli.invalid")
    errors should contain("nayta-hakutoiveet.invalid")
  }

  it should "return no errors for valid parameters" in {
    val hakuList = List("1.2.246.562.29.00000000000000056840")
    val tulostustapa = "hakukohteittain"
    val koulutustoimija = Some("1.2.246.562.10.346830761110")
    val oppilaitosList = List("1.2.246.562.10.52251087186")
    val toimipisteList = List("1.2.246.562.10.55711304158")
    val hakukohteetList = List("1.2.246.562.20.00000000000000059958")
    val koulutusala1List = List("09")
    val koulutusala2List = List("092")
    val koulutusala3List = List("0922")
    val opetuskieliList = List("1")
    val maakuntaList = List("21")
    val kuntaList = List("004")
    val harkinnanvaraisuusList = List("ATARU_EI_PAATTOTODISTUSTA")
    val sukupuoli = Some("2")
    val naytaHakutoiveet = "false"

    val errors = ParameterValidator.validateHakeneetHyvaksytytVastaanottaneetParams(
      hakuList,
      tulostustapa,
      koulutustoimija,
      oppilaitosList,
      toimipisteList,
      hakukohteetList,
      koulutusala1List,
      koulutusala2List,
      koulutusala3List,
      opetuskieliList,
      maakuntaList,
      kuntaList,
      harkinnanvaraisuusList,
      sukupuoli,
      naytaHakutoiveet,
    )

    errors shouldBe empty
  }

  "validateKkHakeneetHyvaksytytVastaanottaneetParams" should "return errors for invalid parameters" in {
    val hakuList = List("invalid-oid")
    val tulostustapa = "invalid-tulostustapa"
    val koulutustoimija = Some("invalid-koulutustoimija")
    val oppilaitosList = List("invalid-oid")
    val toimipisteList = List("1.2.246.999.10.1000000000000000001")
    val hakukohteetList = List("invalid-oid")
    val hakukohderyhmaList = List("invalid-oid")
    val okmOhjauksenAlaList = List("foo")
    val tutkinnonTasoList = List("bar")
    val aidinkieliList = List("baz'")
    val kansalaisuusList = List("invalid-kansalaisuus")
    val sukupuoli = Some("invalid-sukupuoli")
    val invalidBoolean = "invalid-boolean"

    val errors = ParameterValidator.validateKkHakeneetHyvaksytytVastaanottaneetParams(
      hakuList,
      tulostustapa,
      koulutustoimija,
      oppilaitosList,
      toimipisteList,
      hakukohteetList,
      hakukohderyhmaList,
      okmOhjauksenAlaList,
      tutkinnonTasoList,
      aidinkieliList,
      kansalaisuusList,
      sukupuoli,
      invalidBoolean,
      invalidBoolean
    )

    errors.length shouldEqual 14
    errors should contain("haut.invalid.oid")
    errors should contain("tulostustapa.invalid")
    errors should contain("koulutustoimija.invalid.org")
    errors should contain("oppilaitokset.invalid.org")
    errors should contain("toimipisteet.invalid.org")
    errors should contain("hakukohteet.invalid.oid")
    errors should contain("hakukohderyhmat.invalid.oid")
    errors should contain("okm-ohjauksen-alat.invalid")
    errors should contain("tutkinnontasot.invalid")
    errors should contain("aidinkielet.invalid")
    errors should contain("kansalaisuusluokat.invalid")
    errors should contain("sukupuoli.invalid")
    errors should contain("ensikertalainen.invalid")
    errors should contain("nayta-hakutoiveet.invalid")
  }
}
