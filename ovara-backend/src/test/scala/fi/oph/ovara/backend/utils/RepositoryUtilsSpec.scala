package fi.oph.ovara.backend.utils

import org.scalatest.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.should.Matchers.shouldBe

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

  "makeOptionalWhereClause" should "return an empty string when no conditions are provided" in {
    val conditions = Map.empty[String, List[String]]
    assert(
      RepositoryUtils.makeOptionalWhereClause(conditions) == ""
    )
  }

  it should "return an empty string when conditions are empty" in {
    val conditions = Map("kansallinenkoulutusluokitus2016koulutusastetaso2" -> List())
    assert(
      RepositoryUtils.makeOptionalWhereClause(conditions) == ""
    )
  }

  it should "return a WHERE clause with a single condition" in {
    val conditions = Map("kansallinenkoulutusluokitus2016koulutusastetaso2" -> List("02"))
    assert(
      RepositoryUtils.makeOptionalWhereClause(conditions) == "WHERE kansallinenkoulutusluokitus2016koulutusastetaso2 IN ('02')")
  }

  it should "return a WHERE clause with multiple conditions" in {
    val conditions = Map(
      "kansallinenkoulutusluokitus2016koulutusastetaso2" -> List("001", "021"),
      "kansallinenkoulutusluokitus2016koulutusalataso3" -> List("0732")
    )
    assert(
      RepositoryUtils.makeOptionalWhereClause(conditions) ==
        "WHERE kansallinenkoulutusluokitus2016koulutusastetaso2 IN ('001', '021') OR kansallinenkoulutusluokitus2016koulutusalataso3 IN ('0732')")
  }

  it should "ignore empty lists in conditions" in {
    val conditions = Map(
      "kansallinenkoulutusluokitus2016koulutusastetaso2" -> List("001"),
      "kansallinenkoulutusluokitus2016koulutusalataso3" -> List()
    )
    assert(
      RepositoryUtils.makeOptionalWhereClause(conditions) == "WHERE kansallinenkoulutusluokitus2016koulutusastetaso2 IN ('001')")
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

  "makeHakuTableAlkamiskausiQueryStr" should "return full query string for one alkamiskausi" in {
    assert(
      RepositoryUtils.makeHakuTableAlkamiskausiQueryStr(
        (2024, "kausi_s")
      ) == "h.koulutuksen_alkamiskausi @> '[{\"koulutuksenAlkamisvuosi\": 2024, \"koulutuksenAlkamiskausiKoodiUri\": \"kausi_s#1\"}]'::jsonb"
    )
  }

  "makeHakuQueryWithAlkamiskausiParams" should "return empty string if no alkamiskaudet defined" in {
    assert(RepositoryUtils.makeHakuQueryWithAlkamiskausiParams((List(), false, false)) == "")
  }

  it should "return string with only henkilokohtainen suunnitelma clause" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List(), true, false)
      ) == "(h.koulutuksen_alkamiskausi @> '[{\"type\": \"henkkoht\"}]'::jsonb)"
    )
  }

  it should "return query string for syksy 2024 alkamiskausi" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List((2024, "kausi_s")), false, false)
      ) == "(h.koulutuksen_alkamiskausi @> '[{\"koulutuksenAlkamisvuosi\": 2024, " +
        "\"koulutuksenAlkamiskausiKoodiUri\": \"kausi_s#1\"}]'::jsonb)"
    )
  }

  it should "return query string for kevat 2021 alkamiskausi" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List((2021, "kausi_k")), false, false)
      ) == "(h.koulutuksen_alkamiskausi @> '[{\"koulutuksenAlkamisvuosi\": 2021, " +
        "\"koulutuksenAlkamiskausiKoodiUri\": \"kausi_k#1\"}]'::jsonb)"
    )
  }

  it should "return query string for two alkamiskausi and henkilokohtainen suunnitelma" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List((2021, "kausi_k"), (2024, "kausi_s")), true, false)
      ) == "(h.koulutuksen_alkamiskausi @> '[{\"type\": \"henkkoht\"}]'::jsonb " +
        // 2021_kevat haulla
        "OR h.koulutuksen_alkamiskausi @> '[{\"koulutuksenAlkamisvuosi\": 2021, " +
        "\"koulutuksenAlkamiskausiKoodiUri\": \"kausi_k#1\"}]'::jsonb " +
        // 2024_syksy haulla
        "OR h.koulutuksen_alkamiskausi @> '[{\"koulutuksenAlkamisvuosi\": 2024, " +
        "\"koulutuksenAlkamiskausiKoodiUri\": \"kausi_s#1\"}]'::jsonb)"
    )
  }

  it should "return query string for one alkamiskausi, henkilokohtainen suunnitelma and eiAlkamiskautta" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List((2024, "kausi_s")), true, true)
      ) == "(h.koulutuksen_alkamiskausi @> '[{\"type\": \"henkkoht\"}]'::jsonb " +
        "OR h.koulutuksen_alkamiskausi@> '[{\"type\": \"eialkamiskautta\"}]'::jsonb " +
        "OR h.koulutuksen_alkamiskausi @> '[{\"koulutuksenAlkamisvuosi\": 2024, \"koulutuksenAlkamiskausiKoodiUri\": \"kausi_s#1\"}]'::jsonb)"
    )
  }

  it should "return query string for only eiAlkamiskautta selection" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List(), false, true)
      ) == "(h.koulutuksen_alkamiskausi@> '[{\"type\": \"eialkamiskautta\"}]'::jsonb)"
    )
  }

  it should "return query string for eiAlkamiskautta and henkilokohtainen suunnitelma selection" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List(), true, true)
      ) == "(h.koulutuksen_alkamiskausi @> '[{\"type\": \"henkkoht\"}]'::jsonb OR " +
        "h.koulutuksen_alkamiskausi@> '[{\"type\": \"eialkamiskautta\"}]'::jsonb)"
    )
  }

  it should "return query string for eiAlkamiskautta and one alkamiskausi selection" in {
    assert(
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(
        (List((2024, "kausi_s")), false, true)
      ) == "(h.koulutuksen_alkamiskausi@> '[{\"type\": \"eialkamiskautta\"}]'::jsonb OR " +
        "h.koulutuksen_alkamiskausi @> '[{\"koulutuksenAlkamisvuosi\": 2024, \"koulutuksenAlkamiskausiKoodiUri\": \"kausi_s#1\"}]'::jsonb)"
    )
  }

  "makeHakukohderyhmaQueryWithKayttooikeudet" should "return an empty string when both lists are empty" in {
    val result = RepositoryUtils.makeHakukohderyhmaQueryWithKayttooikeudet(List.empty, List.empty)
    result shouldBe ""
  }

  it should "return the hakukohde query string when only kayttooikeusOrgOids contains values" in {
    val result = RepositoryUtils.makeHakukohderyhmaQueryWithKayttooikeudet(List("1.2.3"), List.empty)
    result shouldBe "AND (hkr_hk.hakukohde_oid IN (SELECT hakukohde_oid FROM pub.pub_dim_hakukohde WHERE jarjestyspaikka_oid IN ('1.2.3')))"
  }

  it should "return the hakukohderyhma query string when only kayttooikeusHakukohderyhmaOids contains values" in {
    val result = RepositoryUtils.makeHakukohderyhmaQueryWithKayttooikeudet(List.empty, List("4.5.6"))
    result shouldBe "AND (hkr.hakukohderyhma_oid IN ('4.5.6'))"
  }

  it should "return both query strings combined with OR when both lists contain values" in {
    val result = RepositoryUtils.makeHakukohderyhmaQueryWithKayttooikeudet(List("1.2.3"), List("4.5.6"))
    result shouldBe "AND (hkr.hakukohderyhma_oid IN ('4.5.6') OR hkr_hk.hakukohde_oid IN (SELECT hakukohde_oid FROM pub.pub_dim_hakukohde WHERE jarjestyspaikka_oid IN ('1.2.3')))"
  }

  "buildOrgAndHakukohderyhmaFilter" should "return an empty string for OphPaakayttaja when all filters are empty" in {
    val result = RepositoryUtils.buildOrgAndHakukohderyhmaFilter(
      orgs = List.empty,
      isOrganisaatioRajain = false,
      selectedHakukohderyhmat = List.empty,
      kayttooikeusHakukohderyhmat = List.empty,
      isOphPaakayttaja = true
    )
    result.trim shouldBe ""
  }

  it should "return jarjestyspaikka filter for OphPaakayttaja when only orgs filter is provided" in {
    val result = RepositoryUtils.buildOrgAndHakukohderyhmaFilter(
      orgs = List("1.2.246.562.10.80593660139"),
      isOrganisaatioRajain = false,
      selectedHakukohderyhmat = List.empty,
      kayttooikeusHakukohderyhmat = List.empty,
      isOphPaakayttaja = true
    )
    result.trim shouldBe "AND hk.jarjestyspaikka_oid IN ('1.2.246.562.10.80593660139')"
  }

  it should "return jarjestyspaikka for normal user when only orgs filter is provided" in {
    val result = RepositoryUtils.buildOrgAndHakukohderyhmaFilter(
      orgs = List("1.2.246.562.10.80593660139"),
      isOrganisaatioRajain = true,
      selectedHakukohderyhmat = List.empty,
      kayttooikeusHakukohderyhmat = List.empty,
      isOphPaakayttaja = false
    )
    result shouldBe "AND hk.jarjestyspaikka_oid IN ('1.2.246.562.10.80593660139')"
  }

  it should "return jarjestyspaikka filter for normal user when no filter is provided" in {
    val result = RepositoryUtils.buildOrgAndHakukohderyhmaFilter(
      orgs = List("1.2.246.562.10.80593660139"),
      isOrganisaatioRajain = false,
      selectedHakukohderyhmat = List.empty,
      kayttooikeusHakukohderyhmat = List.empty,
      isOphPaakayttaja = false
    )
    result.trim shouldBe "AND hk.jarjestyspaikka_oid IN ('1.2.246.562.10.80593660139')"
  }

  it should "return a filter for selectedHakukohderyhmat for OphPaakayttaja when only selectedHakukohderyhmat filter is provided" in {
    val result = RepositoryUtils.buildOrgAndHakukohderyhmaFilter(
      orgs = List.empty,
      isOrganisaatioRajain = false,
      selectedHakukohderyhmat = List("1.2.246.562.28.95751395146"),
      kayttooikeusHakukohderyhmat = List.empty,
      isOphPaakayttaja = true
    )
    result.trim shouldBe "AND hkr_hk.hakukohderyhma_oid IN ('1.2.246.562.28.95751395146')"
  }

  it should "return a combined filter for orgs and selectedHakukohderyhmat for OphPaakayttaja when both filters are provided" in {
    val result = RepositoryUtils.buildOrgAndHakukohderyhmaFilter(
      orgs = List("1.2.246.562.10.80593660139"),
      isOrganisaatioRajain = false,
      selectedHakukohderyhmat = List("1.2.246.562.28.95751395146"),
      kayttooikeusHakukohderyhmat = List.empty,
      isOphPaakayttaja = true
    )
    result shouldBe "AND hk.jarjestyspaikka_oid IN ('1.2.246.562.10.80593660139') AND hkr_hk.hakukohderyhma_oid IN ('1.2.246.562.28.95751395146')"
  }

  it should "return a combined filter to both organisaatios and selected hakukohderyhmas when both filters are provided" in {
    val result = RepositoryUtils.buildOrgAndHakukohderyhmaFilter(
      orgs = List("1.2.246.562.10.80593660139"),
      isOrganisaatioRajain = true,
      selectedHakukohderyhmat = List("1.2.246.562.28.95751395146", "1.2.246.562.28.81326045473"),
      kayttooikeusHakukohderyhmat = List("1.2.246.562.28.95751395146"),
      isOphPaakayttaja = false
    )
    result shouldBe "AND hk.jarjestyspaikka_oid IN ('1.2.246.562.10.80593660139') AND hkr_hk.hakukohderyhma_oid IN ('1.2.246.562.28.95751395146', '1.2.246.562.28.81326045473')"
  }

  it should "return a filter including hakukohderyhmat outside of organisation if there is no org filter and hakukohderyhmafilter is provided" in {
    val result = RepositoryUtils.buildOrgAndHakukohderyhmaFilter(
      orgs = List("1.2.246.562.10.80593660139"),
      isOrganisaatioRajain = false,
      selectedHakukohderyhmat = List("1.2.246.562.28.95751395146", "1.2.246.562.28.81326045473"),
      kayttooikeusHakukohderyhmat = List("1.2.246.562.28.95751395146"),
      isOphPaakayttaja = false
    )
    result shouldBe "AND (hk.jarjestyspaikka_oid IN ('1.2.246.562.10.80593660139') AND hkr_hk.hakukohderyhma_oid IN ('1.2.246.562.28.95751395146', '1.2.246.562.28.81326045473') OR hkr_hk.hakukohderyhma_oid IN ('1.2.246.562.28.95751395146'))"
  }

  it should "return a filter for jarjestyspaikka and kayttooikeusHakukohderyhmat when no org or hakukohderyhma filters are provided" in {
    val result = RepositoryUtils.buildOrgAndHakukohderyhmaFilter(
      orgs = List("1.2.246.562.10.80593660139"),
      isOrganisaatioRajain = false,
      selectedHakukohderyhmat = List.empty,
      kayttooikeusHakukohderyhmat = List("1.2.246.562.28.95751395146"),
      isOphPaakayttaja = false
    )
    result shouldBe "AND hk.jarjestyspaikka_oid IN ('1.2.246.562.10.80593660139') OR hkr_hk.hakukohderyhma_oid IN ('1.2.246.562.28.95751395146')"
  }

  "enrichHarkinnanvaraisuudet" should "add SURE_EI_PAATTOTODISTUSTA to harkinnanvaraisuudet when ATARU_EI_PAATTOTODISTUSTA is selected" in {
    assert(
      RepositoryUtils.enrichHarkinnanvaraisuudet(List("ATARU_OPPIMISVAIKEUDET", "ATARU_EI_PAATTOTODISTUSTA")) == List(
        "ATARU_OPPIMISVAIKEUDET",
        "SURE_EI_PAATTOTODISTUSTA",
        "ATARU_EI_PAATTOTODISTUSTA"
      )
    )
  }

  it should "add both SURE_EI_PAATTOTODISTUSTA and SURE_YKS_MAT_AI to harkinnanvaraisuudet when ATARU_EI_PAATTOTODISTUSTA and ATARU_YKS_MAT_AI are selected" in {
    assert(
      RepositoryUtils.enrichHarkinnanvaraisuudet(
        List("ATARU_OPPIMISVAIKEUDET", "ATARU_YKS_MAT_AI", "ATARU_EI_PAATTOTODISTUSTA")
      ) == List(
        "ATARU_OPPIMISVAIKEUDET",
        "SURE_YKS_MAT_AI",
        "ATARU_YKS_MAT_AI",
        "SURE_EI_PAATTOTODISTUSTA",
        "ATARU_EI_PAATTOTODISTUSTA"
      )
    )
  }

  it should "add ATARU_ULKOMAILLA_OPISKELTU to harkinnanvaraisuudet when ATARU_KOULUTODISTUSTEN_VERTAILUVAIKEUDET is selected" in {
    assert(
      RepositoryUtils.enrichHarkinnanvaraisuudet(
        List("ATARU_OPPIMISVAIKEUDET", "ATARU_KOULUTODISTUSTEN_VERTAILUVAIKEUDET")
      ) == List(
        "ATARU_OPPIMISVAIKEUDET",
        "ATARU_ULKOMAILLA_OPISKELTU",
        "ATARU_KOULUTODISTUSTEN_VERTAILUVAIKEUDET"
      )
    )
  }

  "mapVastaanottotiedotToDbValues" should "add EI_VASTAANOTETTU_MAARA_AIKANA to vastaanottotiedot list if list contains PERUNUT" in {
    assert(
      RepositoryUtils.mapVastaanottotiedotToDbValues(List("PERUNUT")) == List(
        "PERUNUT",
        "EI_VASTAANOTETTU_MAARA_AIKANA"
      )
    )
  }

  it should "change VASTAANOTTANUT to VASTAANOTTANUT_SITOVASTI in vastaanottotiedot list if list contains VASTAANOTTANUT" in {
    assert(
      RepositoryUtils.mapVastaanottotiedotToDbValues(List("VASTAANOTTANUT")) == List(
        "VASTAANOTTANUT_SITOVASTI"
      )
    )
  }

  it should "keep vastaanottotiedot list as it is when PERUUTETTU is the only value" in {
    assert(
      RepositoryUtils.mapVastaanottotiedotToDbValues(List("PERUUTETTU")) == List(
        "PERUUTETTU"
      )
    )
  }

  it should "change VASTAANOTTANUT to VASTAANOTTANUT_SITOVASTI and add EI_VASTAANOTETTU_MAARA_AIKANA when PERUNUT, PERUUTETTU and VASTAANOTTANUT are in the original list" in {
    assert(
      RepositoryUtils.mapVastaanottotiedotToDbValues(List("PERUNUT", "PERUUTETTU", "VASTAANOTTANUT")) == List(
        "PERUNUT",
        "EI_VASTAANOTETTU_MAARA_AIKANA",
        "PERUUTETTU",
        "VASTAANOTTANUT_SITOVASTI"
      )
    )
  }

  "mapValintatiedotToDbValues" should "add HYVAKSYTTY_HARKINNANVARAISESTI, VARASIJALTA_HYVAKSYTTY, PERUNUT and PERUUTETTU to list when orig list contains only HYVAKSYTTY" in {
    assert(
      RepositoryUtils.mapValintatiedotToDbValues(List("HYVAKSYTTY")) == List(
        "HYVAKSYTTY",
        "HYVAKSYTTY_HARKINNANVARAISESTI",
        "VARASIJALTA_HYVAKSYTTY",
        "PERUNUT",
        "PERUUTETTU"
      )
    )
  }

  it should "keep valintatiedot list as it is when the original list has values HYLATTY, PERUUNTUNUT and VARALLA" in {
    assert(
      RepositoryUtils.mapValintatiedotToDbValues(List("HYLATTY", "PERUUNTUNUT", "VARALLA")) == List(
        "HYLATTY",
        "PERUUNTUNUT",
        "VARALLA"
      )
    )
  }

  it should "add HYVAKSYTTY values to the list when the original list has values HYVAKSYTTY, HYLATTY, PERUUNTUNUT and VARALLA" in {
    assert(
      RepositoryUtils.mapValintatiedotToDbValues(
        List(
          "HYVAKSYTTY",
          "HYLATTY",
          "PERUUNTUNUT",
          "VARALLA"
        )
      ) == List(
        "HYVAKSYTTY",
        "HYVAKSYTTY_HARKINNANVARAISESTI",
        "VARASIJALTA_HYVAKSYTTY",
        "PERUNUT",
        "PERUUTETTU",
        "HYLATTY",
        "PERUUNTUNUT",
        "VARALLA"
      )
    )
  }

  "buildTutkinnonTasoFilters" should "return None when the list is empty" in {
    val result = RepositoryUtils.buildTutkinnonTasoFilters(List.empty, "h")
    result shouldBe ""
  }

  it should "return the correct filter for 'alempi-ja-ylempi'" in {
    val result = RepositoryUtils.buildTutkinnonTasoFilters(List("alempi-ja-ylempi"), "h")
    result shouldBe "AND (h.alempi_kk_aste = true AND h.ylempi_kk_aste = true)"
  }

  it should "return the correct filter for 'alempi'" in {
    val result = RepositoryUtils.buildTutkinnonTasoFilters(List("alempi"), "h")
    result shouldBe "AND (h.alempi_kk_aste = true AND h.ylempi_kk_aste = false)"
  }

  it should "return the correct filter for 'ylempi'" in {
    val result = RepositoryUtils.buildTutkinnonTasoFilters(List("ylempi"), "h")
    result shouldBe "AND (h.alempi_kk_aste = false AND h.ylempi_kk_aste = true)"
  }

  it should "return the correct filter for multiple values" in {
    val result = RepositoryUtils.buildTutkinnonTasoFilters(List("alempi", "ylempi"), "h")
    result shouldBe "AND (h.alempi_kk_aste = true AND h.ylempi_kk_aste = false OR h.alempi_kk_aste = false AND h.ylempi_kk_aste = true)"
  }
}
