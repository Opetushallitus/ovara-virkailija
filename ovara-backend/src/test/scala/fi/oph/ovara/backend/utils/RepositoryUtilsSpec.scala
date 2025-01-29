package fi.oph.ovara.backend.utils

import org.scalatest.*
import org.scalatest.flatspec.*

class RepositoryUtilsSpec extends AnyFlatSpec {
  "extractAlkamisvuosiKausiAndHenkKohtSuunnitelma" should "return year and alkamiskausikoodiuri for one alkamiskausi and false for henkilokohtainen suunnitelma" in {
    assert(
      RepositoryUtils.extractAlkamisvuosiKausiAndHenkkohtSuunnitelma(List("2023_syksy")) == (List(
        (2023, "kausi_s")
      ), false, false)
    )
  }

  it should "return year and alkamiskausikoodiuri for several alkamiskaudet and false for henkilokohtainen suunnitelma" in {
    assert(
      RepositoryUtils.extractAlkamisvuosiKausiAndHenkkohtSuunnitelma(
        List("2023_syksy", "2021_kevat", "not_alkamiskausi", "2020_syksy")
      ) == (List((2023, "kausi_s"), (2021, "kausi_k"), (2020, "kausi_s")), false, false)
    )
  }

  it should "return empty list when no alkamiskaudet specified and false for henkilokohtainen suunnitelma" in {
    assert(
      RepositoryUtils.extractAlkamisvuosiKausiAndHenkkohtSuunnitelma(List()) == (List(), false, false)
    )
  }

  it should "return empty list when no alkamiskaudet and true for henkilokohtainen suunnitelma" in {
    assert(
      RepositoryUtils.extractAlkamisvuosiKausiAndHenkkohtSuunnitelma(
        List("henkilokohtainen_suunnitelma")
      ) == (List(), true, false)
    )
  }

  it should "return true for henkilokohtainen suunnitelma and ei_alkamiskautta amongst alkamiskaudet and random string" in {
    assert(
      RepositoryUtils.extractAlkamisvuosiKausiAndHenkkohtSuunnitelma(
        List(
          "2023_syksy",
          "2021_kevat",
          "henkilokohtainen_suunnitelma",
          "not_alkamiskausi",
          "ei_alkamiskautta",
          "2020_syksy"
        )
      ) == (List((2023, "kausi_s"), (2021, "kausi_k"), (2020, "kausi_s")), true, true)
    )
  }

  "makeAlkamiskausiQueryStr" should "return query string with alkamisvuosi and alkamiskausityyppi in combined query" in {
    assert(
      RepositoryUtils.makeAlkamiskausiQueryStr(
        List("t", "hk"),
        (2023, "kausi_s")
      ) == "((t.koulutuksen_alkamisvuosi = 2023 AND t.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_s%') " +
        "OR (hk.koulutuksen_alkamisvuosi = 2023 AND hk.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_s%'))"
    )
  }

  "makeAlkamiskaudetQueryStr" should "return query string with one alkamiskaudet" in {
    assert(
      RepositoryUtils.makeAlkamiskaudetQueryStr(
        List("t", "hk"),
        List((2023, "kausi_s"))
      ) == "((t.koulutuksen_alkamisvuosi = 2023 AND t.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_s%') " +
        "OR (hk.koulutuksen_alkamisvuosi = 2023 AND hk.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_s%'))"
    )
  }

  it should "return query string with several alkamiskaudet" in {
    assert(
      RepositoryUtils.makeAlkamiskaudetQueryStr(
        List("t", "hk"),
        List((2023, "kausi_s"), (2021, "kausi_k"))
      ) == "((t.koulutuksen_alkamisvuosi = 2023 AND t.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_s%') " +
        "OR (hk.koulutuksen_alkamisvuosi = 2023 AND hk.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_s%')) " +
        "OR ((t.koulutuksen_alkamisvuosi = 2021 AND t.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_k%') " +
        "OR (hk.koulutuksen_alkamisvuosi = 2021 AND hk.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_k%'))"
    )
  }

  it should "return empty string when there are no alkamiskaudet specified" in {
    assert(RepositoryUtils.makeAlkamiskaudetQueryStr(List("t", "hk"), List()) == "")
  }

  "makeListOfValuesQueryStr" should "return a list of values concatenated together as a string separated by a comma'" in {
    assert(
      RepositoryUtils.makeListOfValuesQueryStr(
        List("1.2.246.562.10.81934895871", "1.2.246.562.10.752369", "1.2.246.562.10.5132568")
      )
        == "'1.2.246.562.10.81934895871', '1.2.246.562.10.752369', '1.2.246.562.10.5132568'"
    )
  }

  it should "return a string of one value without separating comma at the end" in {
    assert(
      RepositoryUtils.makeListOfValuesQueryStr(List("1.2.246.562.10.81934895871")) == "'1.2.246.562.10.81934895871'"
    )
  }

  "makeEqualsQueryStrOfOptional" should "return koulutuksen tila as a query string" in {
    assert(
      RepositoryUtils.makeEqualsQueryStrOfOptional(
        operator = "AND",
        fieldName = "k.tila",
        value = Some("julkaistu")
      ) == "AND k.tila = 'julkaistu'"
    )
  }

  it should "return empty string as a query string when value is None" in {
    assert(RepositoryUtils.makeEqualsQueryStrOfOptional(operator = "AND", fieldName = "k.tila", value = None) == "")
  }

  "makeEqualsQueryStrOfOptionalBoolean" should "return query str with '= true' when valintakoe selection is true" in {
    assert(
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean(
        operator = "AND",
        fieldName = "hk.on_valintakoe",
        value = Some(true)
      ) == "AND hk.on_valintakoe = true"
    )
  }

  it should "return query str with '= false' when valintakoe selection is true" in {
    assert(
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean(
        operator = "AND",
        fieldName = "hk.on_valintakoe",
        value = Some(false)
      ) == "AND hk.on_valintakoe = false"
    )
  }

  it should "return empty query str when valintakoe selection is not defined" in {
    assert(
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean(
        operator = "AND",
        fieldName = "hk.on_valintakoe",
        value = None
      ) == ""
    )
  }

  "optionalHenkilokohtainenSuunnitelmaQuery" should "return empty query str when 'henkilokohtainenSuunnitelma' is false" in {
    assert(
      RepositoryUtils.makeOptionalHenkilokohtainenSuunnitelmaQuery(false) == ""
    )
  }

  it should "return query string where koulutuksen_alkamiskausi_tyyppi is 'henkilokohtainen suunnitelma' when 'henkilokohtainenSuunnitelma' is true" in {
    assert(
      RepositoryUtils.makeOptionalHenkilokohtainenSuunnitelmaQuery(
        true
      ) == "hk.koulutuksen_alkamiskausi_tyyppi = 'henkilokohtainen suunnitelma'"
    )
  }

  "makeAlkamiskaudetAndHenkkohtSuunnitelmaQuery" should "return empty query str when alkamiskaudet is empty and henkilokohtainenSuunnitelma and eiAlkamiskautta are false" in {
    assert(
      RepositoryUtils.makeAlkamiskaudetAndHenkkohtSuunnitelmaQuery((List(), false, false)) == ""
    )
  }

  it should "return AND query string for alkamiskaudet when henkilokohtainenSuunnitelma and eiAlkamiskautta are false" in {
    assert(
      RepositoryUtils.makeAlkamiskaudetAndHenkkohtSuunnitelmaQuery(
        (List((2023, "kausi_s"), (2021, "kausi_k")), false, false)
      ) ==
        "AND (((t.koulutuksen_alkamisvuosi = 2023 AND t.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_s%') " +
        "OR (hk.koulutuksen_alkamisvuosi = 2023 AND hk.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_s%')) " +
        "OR ((t.koulutuksen_alkamisvuosi = 2021 AND t.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_k%') " +
        "OR (hk.koulutuksen_alkamisvuosi = 2021 AND hk.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_k%')))"
    )
  }

  it should "return AND query string for alkamiskaudet with OR query for henkilokohtainen suunnitelma when henkilokohtainenSuunnitelma is true" in {
    assert(
      RepositoryUtils.makeAlkamiskaudetAndHenkkohtSuunnitelmaQuery((List((2021, "kausi_k")), true, false)) ==
        "AND (((t.koulutuksen_alkamisvuosi = 2021 AND t.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_k%') " +
        "OR (hk.koulutuksen_alkamisvuosi = 2021 AND hk.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_k%')) " +
        "OR hk.koulutuksen_alkamiskausi_tyyppi = 'henkilokohtainen suunnitelma')"
    )
  }

  it should "return AND query string for henkilokohtainen suunnitelma when henkilokohtainenSuunnitelma is true and alkamiskaudet is empty" in {
    assert(
      RepositoryUtils.makeAlkamiskaudetAndHenkkohtSuunnitelmaQuery((List(), true, false)) ==
        "AND (hk.koulutuksen_alkamiskausi_tyyppi = 'henkilokohtainen suunnitelma')"
    )
  }

  //TODO: Testi eiAlkamiskautta

  "makeHakuTableAlkamiskausiQueryStr" should "return full query string for one alkamiskausi" in {
    assert(
      RepositoryUtils.makeHakuTableAlkamiskausiQueryStr(
        (2024, "kausi_s")
      ) == "(alkamiskausi->>'koulutuksenAlkamisvuosi' = '2024' "
        + "AND alkamiskausi->>'koulutuksenAlkamiskausiKoodiUri' ^@ 'kausi_s')"
    )
  }

  "makeHakuQueryWithAlkamiskausiParams" should "return empty string if no alkamiskaudet defined" in {
    assert(RepositoryUtils.makeHakuQueryWithAlkamiskausiParams((List(), false, false)) == "")
  }

  it should "return string with only henkilokohtainen suunnitelma clause" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List(), true, false)
      ) == "AND alkamiskausi->>'type' = 'henkkoht'"
    )
  }

  it should "return query string for syksy 2024 alkamiskausi" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List((2024, "kausi_s")), false, false)
      ) == "AND (alkamiskausi->>'koulutuksenAlkamisvuosi' = '2024' " +
        "AND alkamiskausi->>'koulutuksenAlkamiskausiKoodiUri' ^@ 'kausi_s')"
    )
  }

  it should "return query string for kevat 2021 alkamiskausi" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List((2021, "kausi_k")), false, false)
      ) == "AND (alkamiskausi->>'koulutuksenAlkamisvuosi' = '2021' " +
        "AND alkamiskausi->>'koulutuksenAlkamiskausiKoodiUri' ^@ 'kausi_k')"
    )
  }

  it should "return query string for two alkamiskausi and henkilokohtainen suunnitelma" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List((2021, "kausi_k"), (2024, "kausi_s")), true, false)
      ) == "AND alkamiskausi->>'type' = 'henkkoht' " +
        // 2021_kevat haulla
        "OR (alkamiskausi->>'koulutuksenAlkamisvuosi' = '2021' " +
        "AND alkamiskausi->>'koulutuksenAlkamiskausiKoodiUri' ^@ 'kausi_k') " +
        // 2024_syksy haulla
        "OR (alkamiskausi->>'koulutuksenAlkamisvuosi' = '2024' " +
        "AND alkamiskausi->>'koulutuksenAlkamiskausiKoodiUri' ^@ 'kausi_s')"
    )
  }

  it should "return query string for one alkamiskausi, henkilokohtainen suunnitelma and eiAlkamiskautta" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List((2024, "kausi_s")), true, true)
      ) == "AND alkamiskausi->>'type' = 'henkkoht' " +
        "OR koulutuksen_alkamiskausi IS NULL " +
        "OR (alkamiskausi->>'koulutuksenAlkamisvuosi' = '2024' " +
        "AND alkamiskausi->>'koulutuksenAlkamiskausiKoodiUri' ^@ 'kausi_s')"
    )
  }

  it should "return query string for only eiAlkamiskautta selection" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List(), false, true)
      ) == "AND koulutuksen_alkamiskausi IS NULL"
    )
  }

  it should "return query string for eiAlkamiskautta and henkilokohtainen suunnitelma selection" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List(), true, true)
      ) == "AND alkamiskausi->>'type' = 'henkkoht' " +
        "OR koulutuksen_alkamiskausi IS NULL"
    )
  }

  it should "return query string for eiAlkamiskautta and one alkamiskausi selection" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List((2024, "kausi_s")), false, true)
      ) == "AND koulutuksen_alkamiskausi IS NULL " +
        "OR (alkamiskausi->>'koulutuksenAlkamisvuosi' = '2024' " +
        "AND alkamiskausi->>'koulutuksenAlkamiskausiKoodiUri' ^@ 'kausi_s')"
    )
  }
}
