package fi.oph.ovara.backend.utils

import scala.util.matching.Regex


object ParameterValidator {

  val ophOidPattern: Regex = "^1\\.2\\.246\\.562\\.\\d+\\.\\d+$".r
  val organisaatioOidPattern: Regex = "^1\\.2\\.246\\.562\\.(10|99|199|299)\\.\\d+$".r
  val alphanumericPattern: Regex = """^[a-zA-Z0-9_\\-]+$""".r
  private val numericRegex = """^\d+$""".r

  private val tulostustavat = Set("hakukohteittain", "oppilaitoksittain")
  private val oidPattern = """^1\.\d{4}\.\w{1,}$""".r
  private val koodiarvoPattern = """^\d+$""".r


  val TULOSTUSTAVAT = Set("koulutustoimijoittain", "oppilaitoksittain", "toimipisteittain", "hauittain", "hakukohteittain", "hakukohderyhmittain",
    "okm-ohjauksen-aloittain", "koulutusaloittain", "kansalaisuuksittain")
  val TILAT = Set("julkaistu", "tallennettu", "arkistoitu")
  val KK_TUTKINNON_TASOT = Set("alempi-ja-ylempi", "alempi", "ylempi")

  def validateTulostustapa(tulostustapa: Option[String]): Option[String] =
    tulostustapa match {
      case None =>
        Some("tulostustapa.required")
      case Some(tapa) if !TULOSTUSTAVAT.contains(tapa) =>
        Some(s"tulostustapa.invalid")
      case _ => None
    }

  def valueBelongsToSetOfValidValues(value: Option[String], fieldName: String, validValues: Set[String]): Option[String] =
    value.filter(_.nonEmpty).collect {
      case v if !validValues.contains(v) => s"$fieldName.invalid"
    }

  def valuesBelongToSetOfValidValues(values: List[String], fieldName: String, validValues: Set[String]): List[String] =
    values.filterNot(validValues.contains).map(invalidValue => s"$fieldName.invalid")

  def validateOid(opt: Option[String], fieldName: String): Option[String] =
    opt.filter(_.nonEmpty).collect {
      case oid if !ophOidPattern.matches(oid) => s"$fieldName.invalid.oid"
    }

  def validateOrganisaatioOid(opt: Option[String], fieldName: String): Option[String] =
    opt.filter(_.nonEmpty).collect {
      case oid if !organisaatioOidPattern.matches(oid) => s"$fieldName.invalid.org"
    }

  def validateBoolean(value: String, fieldName: String): Option[String] = {
    Option(value).filter(_.nonEmpty).collect {
      case v if !Set("true", "false").contains(v.toLowerCase) => s"$fieldName.invalid"
    }
  }

  def validateNumeric(opt: Option[String], fieldName: String): Option[String] =
    opt.filter(_.nonEmpty).collect {
      case value if !numericRegex.matches(value) => s"$fieldName.invalid"
    }

  def validateNumericList(list: List[String], fieldName: String): List[String] =
    list.filterNot(numericRegex.matches).map(invalid => s"$fieldName.invalid")

  def validateAlphanumericList(list: List[String], fieldName: String): List[String] =
    list.filterNot(alphanumericPattern.matches).map(invalid => s"$fieldName.invalid")

  def validateOidList(list: List[String], fieldName: String): List[String] =
    list.filterNot(ophOidPattern.matches).map(invalid => s"$fieldName.invalid.oid")

  def validateOrganisaatioOidList(list: List[String], fieldName: String): List[String] =
    list.filterNot(ophOidPattern.matches).map(invalid => s"$fieldName.invalid.org")

  def validateNonEmpty(list: List[String], fieldName: String): Option[String] =
    if (list.isEmpty) Some(s"$fieldName.required") else None

  def validateKoulutuksetToteutuksetHakukohteetParams(
                                                       hakuList: List[String],
                                                       maybeKoulutustoimija: Option[String],
                                                       oppilaitosList: List[String],
                                                       toimipisteList: List[String],
                                                       maybeKoulutuksenTila: Option[String],
                                                       maybeToteutuksenTila: Option[String],
                                                       maybeHakukohteenTila: Option[String],
                                                       valintakoe: String
                                                     ): List[String] = {

    val errors = List(
      validateNonEmpty(hakuList, "haut"),
      validateOidList(hakuList, "haut"),
      validateOrganisaatioOid(maybeKoulutustoimija, "koulutustoimija"),
      validateOrganisaatioOidList(oppilaitosList, "oppilaitokset"),
      validateOrganisaatioOidList(toimipisteList, "toimipisteet"),
      valueBelongsToSetOfValidValues(maybeKoulutuksenTila, "koulutuksen-tila", TILAT),
      valueBelongsToSetOfValidValues(maybeToteutuksenTila, "toteutuksen-tila", TILAT),
      valueBelongsToSetOfValidValues(maybeHakukohteenTila, "hakukohteen-tila", TILAT),
      validateBoolean(valintakoe, "valintakoe"),
    ).flatten

    errors
  }

  def validateKkKoulutuksetToteutuksetHakukohteetParams(
                                                         hakuList: List[String],
                                                         oppilaitosList: List[String],
                                                         toimipisteList: List[String],
                                                         hakukohderyhmaList: List[String],
                                                         tulostustapa: String,
                                                         maybeKoulutuksenTila: Option[String],
                                                         maybeToteutuksenTila: Option[String],
                                                         maybeHakukohteenTila: Option[String],
                                                         tutkinnonTasoList: List[String]
                                                       ): List[String] = {

    val errors = List(
      validateNonEmpty(hakuList, "haut"),
      validateOidList(hakuList, "haut"),
      validateOrganisaatioOidList(oppilaitosList, "oppilaitokset"),
      validateOrganisaatioOidList(toimipisteList, "toimipisteet"),
      validateOidList(hakukohderyhmaList, "hakukohderyhmat"),
      valueBelongsToSetOfValidValues(Some(tulostustapa), "tulostustapa", TULOSTUSTAVAT),
      valueBelongsToSetOfValidValues(maybeKoulutuksenTila, "koulutuksen-tila", TILAT),
      valueBelongsToSetOfValidValues(maybeToteutuksenTila, "toteutuksen-tila", TILAT),
      valueBelongsToSetOfValidValues(maybeHakukohteenTila, "hakukohteen-tila", TILAT),
      valuesBelongToSetOfValidValues(tutkinnonTasoList, "tutkinnontasot", KK_TUTKINNON_TASOT),
    ).flatten

    errors
  }

  def validateHakijatParams(
                             hakuList: List[String],
                             oppilaitosList: List[String],
                             toimipisteList: List[String],
                             pohjakoulutusList: List[String],
                             valintatietoList: List[String],
                             vastaanottotietoList: List[String],
                             harkinnanvaraisuusList: List[String],
                             kaksoistutkinto: String,
                             urheilijatutkinto: String,
                             soraterveys: String,
                             soraAiempi: String,
                             markkinointilupa: String,
                             julkaisulupa: String
                           ): List[String] = {

    val errors = List(
      validateNonEmpty(hakuList, "haut"),
      validateOidList(hakuList, "haut"),
      validateOrganisaatioOidList(oppilaitosList, "oppilaitokset"),
      validateOrganisaatioOidList(toimipisteList, "toimipisteet"),
      validateNumericList(pohjakoulutusList, "pohjakoulutukset"),
      validateAlphanumericList(valintatietoList, "valintatiedot"),
      validateAlphanumericList(vastaanottotietoList, "vastaanottotiedot"),
      validateAlphanumericList(harkinnanvaraisuusList, "harkinnanvaraisuudet"),
      validateBoolean(kaksoistutkinto, "kaksoistutkinto-kiinnostaa"),
      validateBoolean(urheilijatutkinto, "urheilijatutkinto-kiinnostaa"),
      validateBoolean(soraterveys, "soraterveys"),
      validateBoolean(soraAiempi, "sora-aiempi"),
      validateBoolean(markkinointilupa, "markkinointilupa"),
      validateBoolean(julkaisulupa, "julkaisulupa")
    ).flatten

    errors
  }
}
