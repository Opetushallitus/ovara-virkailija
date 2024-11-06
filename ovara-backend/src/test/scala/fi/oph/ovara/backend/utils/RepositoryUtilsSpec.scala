package fi.oph.ovara.backend.utils

import org.scalatest.*
import org.scalatest.flatspec.*

class RepositoryUtilsSpec extends AnyFlatSpec {
  "extractAlkamisvuosiAndKausi" should "return year and alkamiskausikoodiuri for one alkamiskausi" in {
    assert(RepositoryUtils.extractAlkamisvuosiAndKausi(List("2023_syksy")) == List((2023, "kausi_s")))
  }

  it should "return year and alkamiskausikoodiuri for several alkamiskaudet" in {
    assert(
      RepositoryUtils.extractAlkamisvuosiAndKausi(
        List("2023_syksy", "2021_kevat", "not_alkamiskausi", "2020_syksy")
      ) == List((2023, "kausi_s"), (2021, "kausi_k"), (2020, "kausi_s"))
    )
  }

  it should "return empty list when no alkamiskaudet specified" in {
    assert(
      RepositoryUtils.extractAlkamisvuosiAndKausi(List()) == List()
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
        "AND",
        List("t", "hk"),
        List((2023, "kausi_s"))
      ) == "AND ((t.koulutuksen_alkamisvuosi = 2023 AND t.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_s%') " +
        "OR (hk.koulutuksen_alkamisvuosi = 2023 AND hk.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_s%'))"
    )
  }

  it should "return query string with several alkamiskaudet" in {
    assert(
      RepositoryUtils.makeAlkamiskaudetQueryStr(
        "AND",
        List("t", "hk"),
        List((2023, "kausi_s"), (2021, "kausi_k"))
      ) == "AND (((t.koulutuksen_alkamisvuosi = 2023 AND t.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_s%') " +
        "OR (hk.koulutuksen_alkamisvuosi = 2023 AND hk.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_s%')) " +
        "OR ((t.koulutuksen_alkamisvuosi = 2021 AND t.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_k%') " +
        "OR (hk.koulutuksen_alkamisvuosi = 2021 AND hk.koulutuksen_alkamiskausi_koodiuri LIKE 'kausi_k%')))"
    )
  }

  it should "return empty string when there are no alkamiskaudet specified" in {
    assert(RepositoryUtils.makeAlkamiskaudetQueryStr("AND", List("t", "hk"), List()) == "")
  }

  "makeListOfValuesQueryStr" should "return a list of values concatenated together as a string separated by a comma'" in {
    assert(RepositoryUtils.makeListOfValuesQueryStr(
      List("1.2.246.562.10.81934895871", "1.2.246.562.10.752369", "1.2.246.562.10.5132568"))
      == "'1.2.246.562.10.81934895871', '1.2.246.562.10.752369', '1.2.246.562.10.5132568'")
  }

  it should "return a string of one value without separating comma at the end" in {
    assert(RepositoryUtils.makeListOfValuesQueryStr(List("1.2.246.562.10.81934895871")) == "'1.2.246.562.10.81934895871'")
  }
}
