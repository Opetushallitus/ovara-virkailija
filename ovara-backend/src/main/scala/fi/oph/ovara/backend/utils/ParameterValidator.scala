package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.raportointi.dto.{RawHakeneetHyvaksytytVastaanottaneetParams, RawHakijatParams, RawKkHakeneetHyvaksytytVastaanottaneetParams, RawKkHakijatParams, RawKkKoulutuksetToteutuksetHakukohteetParams, RawKoulutuksetToteutuksetHakukohteetParams, ValidatedHakeneetHyvaksytytVastaanottaneetParams, ValidatedHakijatParams, ValidatedKkHakeneetHyvaksytytVastaanottaneetParams, ValidatedKkHakijatParams, ValidatedKkKoulutuksetToteutuksetHakukohteetParams, ValidatedKoulutuksetToteutuksetHakukohteetParams}

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
    "okm-ohjauksen-aloittain", "koulutusaloittain", "kansalaisuuksittain", "koulutuksittain", "toteutuksittain")
  val TILAT = Set("julkaistu", "tallennettu", "arkistoitu")
  val KK_TUTKINNON_TASOT = Set("alempi-ja-ylempi", "alempi", "ylempi")

  private def strToOptionBoolean(value: String): Option[Boolean] = value match {
    case null | "" => None
    case v if v.equalsIgnoreCase("true") => Some(true)
    case v if v.equalsIgnoreCase("false") => Some(false)
    case _ => None // eroteltu parsiminen ja validointi
  }

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
  def validateAlphanumeric(opt: Option[String], fieldName: String): Option[String] =
    opt.filter(_.nonEmpty).collect {
      case value if !alphanumericPattern.matches(value) => s"$fieldName.invalid"
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

  def validateKoulutuksetToteutuksetHakukohteetParams(params: RawKoulutuksetToteutuksetHakukohteetParams
                                                     ): Either[List[String], ValidatedKoulutuksetToteutuksetHakukohteetParams] = {
    val errors = List(
      validateNonEmpty(params.haut, "haut"),
      validateOidList(params.haut, "haut"),
      validateOrganisaatioOid(params.koulutustoimija, "koulutustoimija"),
      validateOrganisaatioOidList(params.oppilaitokset, "oppilaitokset"),
      validateOrganisaatioOidList(params.toimipisteet, "toimipisteet"),
      valueBelongsToSetOfValidValues(params.koulutuksenTila, "koulutuksen-tila", TILAT),
      valueBelongsToSetOfValidValues(params.toteutuksenTila, "toteutuksen-tila", TILAT),
      valueBelongsToSetOfValidValues(params.hakukohteenTila, "hakukohteen-tila", TILAT),
      validateBoolean(params.valintakoe, "valintakoe"),
    ).flatten

    if(errors.nonEmpty) {
      Left(errors.distinct)
    } else {
      Right(
        ValidatedKoulutuksetToteutuksetHakukohteetParams(
          params.haut,
          params.koulutustoimija,
          params.oppilaitokset,
          params.toimipisteet,
          params.koulutuksenTila,
          params.toteutuksenTila,
          params.hakukohteenTila,
          strToOptionBoolean(params.valintakoe)
        )
      )
    }
  }

  def validateKkKoulutuksetToteutuksetHakukohteetParams(params: RawKkKoulutuksetToteutuksetHakukohteetParams
                                                       ): Either[List[String], ValidatedKkKoulutuksetToteutuksetHakukohteetParams] = {
    val errors = List(
      validateNonEmpty(params.haut, "haut"),
      validateOidList(params.haut, "haut"),
      validateOrganisaatioOidList(params.oppilaitokset, "oppilaitokset"),
      validateOrganisaatioOidList(params.toimipisteet, "toimipisteet"),
      validateOidList(params.hakukohderyhmat, "hakukohderyhmat"),
      valueBelongsToSetOfValidValues(Some(params.tulostustapa), "tulostustapa", TULOSTUSTAVAT),
      valueBelongsToSetOfValidValues(params.koulutuksenTila, "koulutuksen-tila", TILAT),
      valueBelongsToSetOfValidValues(params.toteutuksenTila, "toteutuksen-tila", TILAT),
      valueBelongsToSetOfValidValues(params.hakukohteenTila, "hakukohteen-tila", TILAT),
      valuesBelongToSetOfValidValues(params.tutkinnonTasot, "tutkinnontasot", KK_TUTKINNON_TASOT)
    ).flatten

    if (errors.nonEmpty) {
      Left(errors.distinct)
    } else {
      Right(
        ValidatedKkKoulutuksetToteutuksetHakukohteetParams(
          params.haut,
          params.tulostustapa,
          params.oppilaitokset,
          params.toimipisteet,
          params.hakukohderyhmat,
          params.koulutuksenTila,
          params.toteutuksenTila,
          params.hakukohteenTila,
          params.tutkinnonTasot
        )
      )
    }
  }

  def validateHakijatParams(params: RawHakijatParams): Either[List[String], ValidatedHakijatParams] = {

    val errors = List(
      validateNonEmpty(params.haut, "haut"),
      validateOidList(params.haut, "haut"),
      validateOrganisaatioOidList(params.oppilaitokset, "oppilaitokset"),
      validateOrganisaatioOidList(params.toimipisteet, "toimipisteet"),
      validateOidList(params.hakukohteet, "hakukohteet"),
      validateNumericList(params.pohjakoulutukset, "pohjakoulutukset"),
      validateAlphanumericList(params.valintatiedot, "valintatiedot"),
      validateAlphanumericList(params.vastaanottotiedot, "vastaanottotiedot"),
      validateAlphanumericList(params.harkinnanvaraisuudet, "harkinnanvaraisuudet"),
      validateBoolean(params.kaksoistutkinto, "kaksoistutkinto-kiinnostaa"),
      validateBoolean(params.urheilijatutkinto, "urheilijatutkinto-kiinnostaa"),
      validateBoolean(params.soraTerveys, "soraterveys"),
      validateBoolean(params.soraAiempi, "sora-aiempi"),
      validateBoolean(params.markkinointilupa, "markkinointilupa"),
      validateBoolean(params.julkaisulupa, "julkaisulupa")
    ).flatten

    val combinedErrors = errors ++ Option.when(params.oppilaitokset.isEmpty && params.toimipisteet.isEmpty)("error.required.missing").toList

    if (combinedErrors.nonEmpty) {
      Left(combinedErrors.distinct)
    } else {
      Right(
        ValidatedHakijatParams(
          params.haut,
          params.oppilaitokset,
          params.toimipisteet,
          params.hakukohteet,
          params.pohjakoulutukset,
          params.valintatiedot,
          params.vastaanottotiedot,
          params.harkinnanvaraisuudet,
          strToOptionBoolean(params.kaksoistutkinto),
          strToOptionBoolean(params.urheilijatutkinto),
          strToOptionBoolean(params.soraTerveys),
          strToOptionBoolean(params.soraAiempi),
          strToOptionBoolean(params.markkinointilupa),
          strToOptionBoolean(params.julkaisulupa)
        )
      )
    }
  }

  def validateKkHakijatParams(params: RawKkHakijatParams): Either[List[String], ValidatedKkHakijatParams] = {

    val errors = List(
      validateNonEmpty(params.haut, "haut"),
      validateOidList(params.haut, "haut"),
      validateOrganisaatioOidList(params.oppilaitokset, "oppilaitokset"),
      validateOrganisaatioOidList(params.toimipisteet, "toimipisteet"),
      validateOidList(params.hakukohteet, "hakukohteet"),
      validateAlphanumericList(params.valintatiedot, "valintatiedot"),
      validateAlphanumericList(params.vastaanottotiedot, "vastaanottotiedot"),
      validateOidList(params.hakukohderyhmat, "hakukohderyhmat"),
      validateNumericList(params.kansalaisuusluokat, "kansalaisuusluokat"),
      validateBoolean(params.markkinointilupa, "markkinointilupa"),
      validateBoolean(params.naytaYoArvosanat, "nayta-yo-arvosanat"),
      validateBoolean(params.naytaHetu, "nayta-hetu"),
      validateBoolean(params.naytaPostiosoite, "nayta-postiosoite")
    ).flatten

    val combinedErrors = errors ++ Option.when(
      params.oppilaitokset.isEmpty && params.toimipisteet.isEmpty && params.hakukohderyhmat.isEmpty
    )("error.required.missing").toList

    if (combinedErrors.nonEmpty) {
      Left(combinedErrors.distinct)
    } else {
      Right(
        ValidatedKkHakijatParams(
          params.haut,
          params.oppilaitokset,
          params.toimipisteet,
          params.hakukohteet,
          params.valintatiedot,
          params.vastaanottotiedot,
          params.hakukohderyhmat,
          params.kansalaisuusluokat,
          strToOptionBoolean(params.markkinointilupa),
          params.naytaYoArvosanat.toBoolean,
          params.naytaHetu.toBoolean,
          params.naytaPostiosoite.toBoolean
        )
      )
    }
  }

  def validateHakeneetHyvaksytytVastaanottaneetParams(
                                                       params: RawHakeneetHyvaksytytVastaanottaneetParams
                                                     ): Either[List[String], ValidatedHakeneetHyvaksytytVastaanottaneetParams] = {

    val errors = List(
      validateNonEmpty(params.haut, "haut"),
      validateOidList(params.haut, "haut"),
      valueBelongsToSetOfValidValues(Some(params.tulostustapa), "tulostustapa", TULOSTUSTAVAT),
      validateOrganisaatioOid(params.koulutustoimija, "koulutustoimija"),
      validateOrganisaatioOidList(params.oppilaitokset, "oppilaitokset"),
      validateOrganisaatioOidList(params.toimipisteet, "toimipisteet"),
      validateOidList(params.hakukohteet, "hakukohteet"),
      validateNumericList(params.koulutusalat1, "koulutusalat1"),
      validateNumericList(params.koulutusalat2, "koulutusalat2"),
      validateNumericList(params.koulutusalat3, "koulutusalat3"),
      validateAlphanumericList(params.opetuskielet, "opetuskielet"),
      validateAlphanumericList(params.maakunnat, "maakunnat"),
      validateAlphanumericList(params.kunnat, "kunnat"),
      validateAlphanumericList(params.harkinnanvaraisuudet, "harkinnanvaraisuudet"),
      validateNumeric(params.sukupuoli, "sukupuoli"),
      validateBoolean(params.naytaHakutoiveet, "nayta-hakutoiveet")
    ).flatten

    if (errors.nonEmpty) {
      Left(errors.distinct)
    } else {
      Right(
        ValidatedHakeneetHyvaksytytVastaanottaneetParams(
          params.haut,
          params.tulostustapa,
          params.koulutustoimija,
          params.oppilaitokset,
          params.toimipisteet,
          params.hakukohteet,
          params.koulutusalat1,
          params.koulutusalat2,
          params.koulutusalat3,
          params.opetuskielet,
          params.maakunnat,
          params.kunnat,
          params.harkinnanvaraisuudet,
          strToOptionBoolean(params.naytaHakutoiveet).getOrElse(true),
          params.sukupuoli
        )
      )
    }
  }

  def validateKkHakeneetHyvaksytytVastaanottaneetParams(
                                                         params: RawKkHakeneetHyvaksytytVastaanottaneetParams
                                                       ): Either[List[String], ValidatedKkHakeneetHyvaksytytVastaanottaneetParams] = {

    val errors = List(
      validateNonEmpty(params.haut, "haut"),
      validateOidList(params.haut, "haut"),
      valueBelongsToSetOfValidValues(Some(params.tulostustapa), "tulostustapa", TULOSTUSTAVAT),
      validateOrganisaatioOid(params.koulutustoimija, "koulutustoimija"),
      validateOrganisaatioOidList(params.oppilaitokset, "oppilaitokset"),
      validateOrganisaatioOidList(params.toimipisteet, "toimipisteet"),
      validateOidList(params.hakukohteet, "hakukohteet"),
      validateOidList(params.hakukohderyhmat, "hakukohderyhmat"),
      validateNumericList(params.okmOhjauksenAlat, "okm-ohjauksen-alat"),
      valuesBelongToSetOfValidValues(params.tutkinnonTasot, "tutkinnontasot", KK_TUTKINNON_TASOT),
      validateAlphanumericList(params.aidinkielet, "aidinkielet"),
      validateNumericList(params.kansalaisuusluokat, "kansalaisuusluokat"),
      validateNumeric(params.sukupuoli, "sukupuoli"),
      validateBoolean(params.ensikertalainen, "ensikertalainen"),
      validateBoolean(params.naytaHakutoiveet, "nayta-hakutoiveet")
    ).flatten

    if (errors.nonEmpty) {
      Left(errors.distinct)
    } else {
      Right(
        ValidatedKkHakeneetHyvaksytytVastaanottaneetParams(
          params.haut,
          params.tulostustapa,
          params.koulutustoimija,
          params.oppilaitokset,
          params.toimipisteet,
          params.hakukohteet,
          params.hakukohderyhmat,
          params.okmOhjauksenAlat,
          params.tutkinnonTasot,
          params.aidinkielet,
          params.kansalaisuusluokat,
          params.sukupuoli,
          strToOptionBoolean(params.ensikertalainen),
          strToOptionBoolean(params.naytaHakutoiveet).getOrElse(true)
        )
      )
    }
  }
}
