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
    import params._
    val errors = List(
      validateNonEmpty(haut, "haut"),
      validateOidList(haut, "haut"),
      validateOrganisaatioOid(koulutustoimija, "koulutustoimija"),
      validateOrganisaatioOidList(oppilaitokset, "oppilaitokset"),
      validateOrganisaatioOidList(toimipisteet, "toimipisteet"),
      valueBelongsToSetOfValidValues(koulutuksenTila, "koulutuksen-tila", TILAT),
      valueBelongsToSetOfValidValues(toteutuksenTila, "toteutuksen-tila", TILAT),
      valueBelongsToSetOfValidValues(hakukohteenTila, "hakukohteen-tila", TILAT),
      validateBoolean(valintakoe, "valintakoe"),
    ).flatten

    if(errors.nonEmpty) {
      Left(errors.distinct)
    } else {
      Right(
        ValidatedKoulutuksetToteutuksetHakukohteetParams(
          haut,
          koulutustoimija,
          oppilaitokset,
          toimipisteet,
          koulutuksenTila,
          toteutuksenTila,
          hakukohteenTila,
          strToOptionBoolean(valintakoe)
        )
      )
    }
  }

  def validateKkKoulutuksetToteutuksetHakukohteetParams(params: RawKkKoulutuksetToteutuksetHakukohteetParams
                                                       ): Either[List[String], ValidatedKkKoulutuksetToteutuksetHakukohteetParams] = {
    import params._

    val errors = List(
      validateNonEmpty(haut, "haut"),
      validateOidList(haut, "haut"),
      validateOrganisaatioOidList(oppilaitokset, "oppilaitokset"),
      validateOrganisaatioOidList(toimipisteet, "toimipisteet"),
      validateOidList(hakukohderyhmat, "hakukohderyhmat"),
      valueBelongsToSetOfValidValues(Some(tulostustapa), "tulostustapa", TULOSTUSTAVAT),
      valueBelongsToSetOfValidValues(koulutuksenTila, "koulutuksen-tila", TILAT),
      valueBelongsToSetOfValidValues(toteutuksenTila, "toteutuksen-tila", TILAT),
      valueBelongsToSetOfValidValues(hakukohteenTila, "hakukohteen-tila", TILAT),
      valuesBelongToSetOfValidValues(tutkinnonTasot, "tutkinnontasot", KK_TUTKINNON_TASOT)
    ).flatten

    if (errors.nonEmpty) {
      Left(errors.distinct)
    } else {
      Right(
        ValidatedKkKoulutuksetToteutuksetHakukohteetParams(
          haut,
          tulostustapa,
          oppilaitokset,
          toimipisteet,
          hakukohderyhmat,
          koulutuksenTila,
          toteutuksenTila,
          hakukohteenTila,
          tutkinnonTasot
        )
      )
    }
  }

  def validateHakijatParams(params: RawHakijatParams): Either[List[String], ValidatedHakijatParams] = {
    import params._
    val errors = List(
      validateNonEmpty(haut, "haut"),
      validateOidList(haut, "haut"),
      validateOrganisaatioOidList(oppilaitokset, "oppilaitokset"),
      validateOrganisaatioOidList(toimipisteet, "toimipisteet"),
      validateOidList(hakukohteet, "hakukohteet"),
      validateNumericList(pohjakoulutukset, "pohjakoulutukset"),
      validateAlphanumericList(valintatiedot, "valintatiedot"),
      validateAlphanumericList(vastaanottotiedot, "vastaanottotiedot"),
      validateAlphanumericList(harkinnanvaraisuudet, "harkinnanvaraisuudet"),
      validateBoolean(kaksoistutkinto, "kaksoistutkinto-kiinnostaa"),
      validateBoolean(urheilijatutkinto, "urheilijatutkinto-kiinnostaa"),
      validateBoolean(soraTerveys, "soraterveys"),
      validateBoolean(soraAiempi, "sora-aiempi"),
      validateBoolean(markkinointilupa, "markkinointilupa"),
      validateBoolean(julkaisulupa, "julkaisulupa")
    ).flatten

    val combinedErrors = errors ++ Option.when(oppilaitokset.isEmpty && toimipisteet.isEmpty)("error.required.missing").toList

    if (combinedErrors.nonEmpty) {
      Left(combinedErrors.distinct)
    } else {
      Right(
        ValidatedHakijatParams(
          haut,
          oppilaitokset,
          toimipisteet,
          hakukohteet,
          pohjakoulutukset,
          valintatiedot,
          vastaanottotiedot,
          harkinnanvaraisuudet,
          strToOptionBoolean(kaksoistutkinto),
          strToOptionBoolean(urheilijatutkinto),
          strToOptionBoolean(soraTerveys),
          strToOptionBoolean(soraAiempi),
          strToOptionBoolean(markkinointilupa),
          strToOptionBoolean(julkaisulupa)
        )
      )
    }
  }

  def validateKkHakijatParams(params: RawKkHakijatParams): Either[List[String], ValidatedKkHakijatParams] = {
    import params._
    val errors = List(
      validateNonEmpty(haut, "haut"),
      validateOidList(haut, "haut"),
      validateOrganisaatioOidList(oppilaitokset, "oppilaitokset"),
      validateOrganisaatioOidList(toimipisteet, "toimipisteet"),
      validateOidList(hakukohteet, "hakukohteet"),
      validateAlphanumericList(valintatiedot, "valintatiedot"),
      validateAlphanumericList(vastaanottotiedot, "vastaanottotiedot"),
      validateOidList(hakukohderyhmat, "hakukohderyhmat"),
      validateNumericList(kansalaisuusluokat, "kansalaisuusluokat"),
      validateBoolean(markkinointilupa, "markkinointilupa"),
      validateBoolean(naytaYoArvosanat, "nayta-yo-arvosanat"),
      validateBoolean(naytaHetu, "nayta-hetu"),
      validateBoolean(naytaPostiosoite, "nayta-postiosoite")
    ).flatten

    val combinedErrors = errors ++ Option.when(
      oppilaitokset.isEmpty && toimipisteet.isEmpty && hakukohderyhmat.isEmpty
    )("error.required.missing").toList

    if (combinedErrors.nonEmpty) {
      Left(combinedErrors.distinct)
    } else {
      Right(
        ValidatedKkHakijatParams(
          haut,
          oppilaitokset,
          toimipisteet,
          hakukohteet,
          valintatiedot,
          vastaanottotiedot,
          hakukohderyhmat,
          kansalaisuusluokat,
          strToOptionBoolean(markkinointilupa),
          naytaYoArvosanat.toBoolean,
          naytaHetu.toBoolean,
          naytaPostiosoite.toBoolean
        )
      )
    }
  }

  def validateHakeneetHyvaksytytVastaanottaneetParams(
                                                       params: RawHakeneetHyvaksytytVastaanottaneetParams
                                                     ): Either[List[String], ValidatedHakeneetHyvaksytytVastaanottaneetParams] = {
    import params._

    val errors = List(
      validateNonEmpty(haut, "haut"),
      validateOidList(haut, "haut"),
      valueBelongsToSetOfValidValues(Some(tulostustapa), "tulostustapa", TULOSTUSTAVAT),
      validateOrganisaatioOid(koulutustoimija, "koulutustoimija"),
      validateOrganisaatioOidList(oppilaitokset, "oppilaitokset"),
      validateOrganisaatioOidList(toimipisteet, "toimipisteet"),
      validateOidList(hakukohteet, "hakukohteet"),
      validateNumericList(koulutusalat1, "koulutusalat1"),
      validateNumericList(koulutusalat2, "koulutusalat2"),
      validateNumericList(koulutusalat3, "koulutusalat3"),
      validateAlphanumericList(opetuskielet, "opetuskielet"),
      validateAlphanumericList(maakunnat, "maakunnat"),
      validateAlphanumericList(kunnat, "kunnat"),
      validateAlphanumericList(harkinnanvaraisuudet, "harkinnanvaraisuudet"),
      validateNumeric(sukupuoli, "sukupuoli"),
      validateBoolean(naytaHakutoiveet, "nayta-hakutoiveet")
    ).flatten

    if (errors.nonEmpty) {
      Left(errors.distinct)
    } else {
      Right(
        ValidatedHakeneetHyvaksytytVastaanottaneetParams(
          haut,
          tulostustapa,
          koulutustoimija,
          oppilaitokset,
          toimipisteet,
          hakukohteet,
          koulutusalat1,
          koulutusalat2,
          koulutusalat3,
          opetuskielet,
          maakunnat,
          kunnat,
          harkinnanvaraisuudet,
          strToOptionBoolean(naytaHakutoiveet).getOrElse(true),
          sukupuoli
        )
      )
    }
  }

  def validateKkHakeneetHyvaksytytVastaanottaneetParams(
                                                         params: RawKkHakeneetHyvaksytytVastaanottaneetParams
                                                       ): Either[List[String], ValidatedKkHakeneetHyvaksytytVastaanottaneetParams] = {
    import params._

    val errors = List(
      validateNonEmpty(haut, "haut"),
      validateOidList(haut, "haut"),
      valueBelongsToSetOfValidValues(Some(tulostustapa), "tulostustapa", TULOSTUSTAVAT),
      validateOrganisaatioOid(koulutustoimija, "koulutustoimija"),
      validateOrganisaatioOidList(oppilaitokset, "oppilaitokset"),
      validateOrganisaatioOidList(toimipisteet, "toimipisteet"),
      validateOidList(hakukohteet, "hakukohteet"),
      validateOidList(hakukohderyhmat, "hakukohderyhmat"),
      validateNumericList(okmOhjauksenAlat, "okm-ohjauksen-alat"),
      valuesBelongToSetOfValidValues(tutkinnonTasot, "tutkinnontasot", KK_TUTKINNON_TASOT),
      validateAlphanumericList(aidinkielet, "aidinkielet"),
      validateNumericList(kansalaisuusluokat, "kansalaisuusluokat"),
      validateNumeric(sukupuoli, "sukupuoli"),
      validateBoolean(ensikertalainen, "ensikertalainen"),
      validateBoolean(naytaHakutoiveet, "nayta-hakutoiveet")
    ).flatten

    if (errors.nonEmpty) {
      Left(errors.distinct)
    } else {
      Right(
        ValidatedKkHakeneetHyvaksytytVastaanottaneetParams(
          haut,
          tulostustapa,
          koulutustoimija,
          oppilaitokset,
          toimipisteet,
          hakukohteet,
          hakukohderyhmat,
          okmOhjauksenAlat,
          tutkinnonTasot,
          aidinkielet,
          kansalaisuusluokat,
          sukupuoli,
          strToOptionBoolean(ensikertalainen),
          strToOptionBoolean(naytaHakutoiveet).getOrElse(true)
        )
      )
    }
  }
}
