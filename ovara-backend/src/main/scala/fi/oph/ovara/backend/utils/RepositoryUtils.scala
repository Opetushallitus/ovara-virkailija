package fi.oph.ovara.backend.utils

import scala.util.matching.Regex

object RepositoryUtils {
  def extractAlkamisvuosiKausiAndHenkkohtSuunnitelma(
      alkamiskaudet: List[String]
  ): (List[(Int, String)], Boolean, Boolean) = {
    val alkamiskausiRegex: Regex = """(\d{4})_([a-z]+)""".r
    val alkamiskaudetAndVuodet = alkamiskaudet.flatMap { alkamiskausi =>
      for {
        alkamiskausiMatches <- alkamiskausiRegex.findAllMatchIn(alkamiskausi)
      } yield {
        val kausi = alkamiskausiMatches.group(2) match {
          case "syksy" => "kausi_s"
          case "kevat" => "kausi_k"
        }
        (alkamiskausiMatches.group(1).toInt, kausi)
      }
    }
    val henkilokohtainenSuunnitelma = alkamiskaudet.contains("henkilokohtainen_suunnitelma")
    val eiAlkamiskautta             = alkamiskaudet.contains("ei_alkamiskautta")

    (alkamiskaudetAndVuodet, henkilokohtainenSuunnitelma, eiAlkamiskautta)
  }

  def makeListOfValuesQueryStr(values: List[String]): String = {
    s"${values.map(s => s"'$s'").mkString(", ")}"
  }

  def makeOptionalListOfValuesQueryStr(operator: String, columnName: String, values: List[String]): String = {
    val valuesStr = makeListOfValuesQueryStr(values)
    if (valuesStr.isEmpty) {
      ""
    } else {
      s"$operator $columnName in ($valuesStr)"
    }
  }

  def makeEqualsQueryStrOfOptional(operator: String, fieldName: String, value: Option[String]): String = {
    value match {
      case Some(v) => s"$operator $fieldName = '$v'"
      case None    => ""
    }
  }

  def makeEqualsQueryStrOfOptionalBoolean(operator: String, fieldName: String, value: Option[Boolean]): String = {
    value match {
      case Some(v) => s"$operator $fieldName = $v"
      case None    => ""
    }
  }

  def makeOptionalWhereClause(conditions: Map[String, List[String]]): String = {
    val clauses = conditions.collect {
      case (column, values) if values.nonEmpty =>
        val valuesStr = makeListOfValuesQueryStr(values)
        s"$column IN ($valuesStr)"
    }.toList

    if (clauses.nonEmpty) {
      "WHERE " + clauses.mkString(" OR ")
    } else {
      ""
    }
  }

  def makeAlkamiskaudetQueryStr(tableNames: List[String], alkamiskaudet: List[(Int, String)]): String = {
    def hasMoreThanOne(alkamiskaudet: List[(Int, String)]) = {
      alkamiskaudet.size > 1
    }

    if (alkamiskaudet.isEmpty) {
      ""
    } else {
      s"${alkamiskaudet.map(alkamiskausi => makeAlkamiskausiQueryStr(tableNames = tableNames, alkamiskausi = alkamiskausi)).mkString(" OR ")}"
    }
  }

  def makeAlkamiskausiQueryStr(tableNames: List[String], alkamiskausi: (Int, String)): String = {
    s"(${tableNames
      .map(tableName =>
        s"($tableName.koulutuksen_alkamisvuosi = ${alkamiskausi._1} AND " +
          s"$tableName.koulutuksen_alkamiskausi_koodiuri LIKE '${alkamiskausi._2}%')"
      )
      .mkString(" OR ")})"
  }

  def makeOptionalHenkilokohtainenSuunnitelmaQuery(henkkohtSuunnitelma: Boolean): String = {
    if (henkkohtSuunnitelma) {
      "hk.koulutuksen_alkamiskausi_tyyppi = 'henkilokohtainen suunnitelma'"
    } else {
      ""
    }
  }

  def makeHakuTableAlkamiskausiQueryStr(alkamiskausi: (Int, String)): String = {
    val vuosi = alkamiskausi._1
    val kausi = alkamiskausi._2
    s"h.koulutuksen_alkamiskausi @> '[{\"koulutuksenAlkamisvuosi\": $vuosi, \"koulutuksenAlkamiskausiKoodiUri\": \"$kausi#1\"}]'::jsonb"
  }

  def makeHakuQueryWithAlkamiskausiParams(
      alkamiskaudetAndHenkKohtSuunnitelma: (List[(Int, String)], Boolean, Boolean)
  ): String = {
    val hlokohtSuunnitelma = alkamiskaudetAndHenkKohtSuunnitelma._2
    val hlokohtSuunnitelmaQueryStr = if (hlokohtSuunnitelma) {
      s"h.koulutuksen_alkamiskausi @> '[{\"type\": \"henkkoht\"}]'::jsonb"
    } else {
      ""
    }

    val eiAlkamiskautta = alkamiskaudetAndHenkKohtSuunnitelma._3
    val eiAlkamiskauttaQueryStr = if (eiAlkamiskautta) {
      val operator = if (hlokohtSuunnitelma) {
        " OR "
      } else {
        ""
      }

      s"${operator}h.koulutuksen_alkamiskausi@> '[{\"type\": \"eialkamiskautta\"}]'::jsonb"
    } else {
      ""
    }

    val operator =
      if (
        alkamiskaudetAndHenkKohtSuunnitelma._1.isEmpty && !alkamiskaudetAndHenkKohtSuunnitelma._2 && !alkamiskaudetAndHenkKohtSuunnitelma._3
      ) {
        ""
      } else {
        "("
      }

    val alkamiskaudetAndVuodet = alkamiskaudetAndHenkKohtSuunnitelma._1
    val alkamiskaudetStr = alkamiskaudetAndVuodet
      .map(alkamiskausiAndVuosi => makeHakuTableAlkamiskausiQueryStr(alkamiskausiAndVuosi))
      .mkString(" OR ")
    val alkamiskaudetQueryStr = if (alkamiskaudetStr.isEmpty) {
      ""
    } else {
      if (hlokohtSuunnitelmaQueryStr.isEmpty && !eiAlkamiskautta) {
        s"$alkamiskaudetStr"
      } else {
        s" OR $alkamiskaudetStr"
      }
    }

    if (operator.isEmpty) {
      ""
    } else {
      s"$operator$hlokohtSuunnitelmaQueryStr" +
        s"$eiAlkamiskauttaQueryStr" +
        s"$alkamiskaudetQueryStr)"
    }
  }

  def makeHakukohderyhmaQueryWithKayttooikeudet(
      kayttooikeusOrgOids: List[String],
      kayttooikeusHakukohderyhmaOids: List[String],
      hakukohderyhmaTablename: String = "hkr"
  ): String = {
    val hakukohderyhmaStr      = RepositoryUtils.makeListOfValuesQueryStr(kayttooikeusHakukohderyhmaOids)
    val hakukohderyhmaQueryStr = s"$hakukohderyhmaTablename.hakukohderyhma_oid IN ($hakukohderyhmaStr)"

    val hakukohdeOrgStr = RepositoryUtils.makeListOfValuesQueryStr(kayttooikeusOrgOids)
    val hakukohdeQueryStr = s"hk.jarjestyspaikka_oid IN ($hakukohdeOrgStr)"

    (kayttooikeusHakukohderyhmaOids.isEmpty, kayttooikeusOrgOids.isEmpty) match {
      case (true, true)   => ""
      case (true, false)  => s"AND $hakukohdeQueryStr"
      case (false, true)  => s"AND $hakukohderyhmaQueryStr"
      case (false, false) => s"AND ($hakukohderyhmaQueryStr OR $hakukohdeQueryStr)"
    }
  }

  def buildHakukohdeFilterQuery(
      selectedHakukohteet: List[String],
      selectedHaut: List[String],
      selectedHakukohderyhmat: List[String],
      kayttooikeusHakukohderyhmat: List[String],
      orgs: List[String],
      isOrganisaatioRajain: Boolean,
      isOphPaakayttaja: Boolean
  ): String = {
    if (selectedHaut.isEmpty)
      throw new IllegalArgumentException("Haku must be selected before fetching hakukohteet")
    if (orgs.isEmpty && kayttooikeusHakukohderyhmat.isEmpty && !isOphPaakayttaja)
      throw new IllegalArgumentException("Non superuser must have either organization or hakukohderyhma limitation")
    // käyttäjälle sallitut / organisaatiorajatut hakukohteet AND (selected hakukohde OR (selected hakukohderyhmä AND selected haku))
    val hakukohteetOrganisaatioJaKäyttöoikeusrajauksilla = if (isOrganisaatioRajain) {
      makeHakukohderyhmaQueryWithKayttooikeudet(orgs, List.empty, "hkr_hk")
    } else if (isOphPaakayttaja) {
        ""
    } else {
        makeHakukohderyhmaQueryWithKayttooikeudet(orgs, kayttooikeusHakukohderyhmat, "hkr_hk")
    }
    val selectedHakukohdeRajaus = if (selectedHakukohteet.nonEmpty) {
      s"hk.hakukohde_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(selectedHakukohteet)}) OR"
    } else {
      ""
    }
    val selectedHakukohderyhmaRajaus = if (selectedHakukohderyhmat.nonEmpty) {
      s"hkr_hk.hakukohderyhma_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(selectedHakukohderyhmat)}) AND"
    } else {
      ""
    }
    // haku on aina pakollinen rajain, validointi tehdään controllerissa
    val selectedHakuRajaus = s"hk.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(selectedHaut)})"
    s"$hakukohteetOrganisaatioJaKäyttöoikeusrajauksilla AND ($selectedHakukohdeRajaus ($selectedHakukohderyhmaRajaus $selectedHakuRajaus))"
  }

  def enrichHarkinnanvaraisuudet(harkinnanvaraisuudet: List[String]): List[String] = {
    harkinnanvaraisuudet.flatMap(harkinnanvaraisuus => {
      if (List("ATARU_EI_PAATTOTODISTUSTA", "ATARU_YKS_MAT_AI").contains(harkinnanvaraisuus)) {
        val r: Regex = "ATARU(_\\w*)".r
        val group    = for (m <- r.findFirstMatchIn(harkinnanvaraisuus)) yield m.group(1)
        val value    = group.getOrElse("")
        if (value.nonEmpty) {
          s"SURE$value" :: List(harkinnanvaraisuus)
        } else {
          List(harkinnanvaraisuus)
        }
      } else if (harkinnanvaraisuus == "ATARU_KOULUTODISTUSTEN_VERTAILUVAIKEUDET") {
        s"ATARU_ULKOMAILLA_OPISKELTU" :: List(harkinnanvaraisuus)
      } else {
        List(harkinnanvaraisuus)
      }
    })
  }

  def mapVastaanottotiedotToDbValues(vastaanottotiedot: List[String]): List[String] = {
    vastaanottotiedot.flatMap {
      case s: String if s == "PERUNUT"        => s :: List("EI_VASTAANOTETTU_MAARA_AIKANA")
      case s: String if s == "VASTAANOTTANUT" => List(s"${s}_SITOVASTI")
      case s: String                          => List(s)
    }
  }

  def mapValintatiedotToDbValues(valintatiedot: List[String]): List[String] = {
    valintatiedot.flatMap {
      case s: String if s == "HYVAKSYTTY" =>
        s :: List("HYVAKSYTTY_HARKINNANVARAISESTI", "VARASIJALTA_HYVAKSYTTY", "PERUNUT", "PERUUTETTU")
      case s: String => List(s)
    }
  }

  def buildTutkinnonTasoFilters(
      tutkinnonTasot: List[String],
      hakukohdeTable: String
  ): String = {
    if (tutkinnonTasot.nonEmpty) {
      var conditions = List[String]()
      if (tutkinnonTasot.contains("alempi-ja-ylempi")) {
        conditions = conditions :+ s"$hakukohdeTable.alempi_kk_aste = true AND $hakukohdeTable.ylempi_kk_aste = true"
      }
      if (tutkinnonTasot.contains("alempi")) {
        conditions = conditions :+ s"$hakukohdeTable.alempi_kk_aste = true AND $hakukohdeTable.ylempi_kk_aste = false"
      }
      if (tutkinnonTasot.contains("ylempi")) {
        conditions = conditions :+ s"${hakukohdeTable}.alempi_kk_aste = false AND $hakukohdeTable.ylempi_kk_aste = true"
      }
      s"AND (" + conditions.mkString(" OR ") + ")"
    } else
      ""
  }

  def makeOptionalJarjestyspaikkaQuery(selectedKayttooikeusOrganisaatiot: List[String]): String = {
    RepositoryUtils.makeOptionalListOfValuesQueryStr(
      "AND",
      "hk.jarjestyspaikka_oid",
      selectedKayttooikeusOrganisaatiot
    )
  }

  def makeOptionalHakukohderyhmatSubSelectQueryStr(hakukohderyhmat: List[String]): String = {
    val hakukohderyhmatStr = makeListOfValuesQueryStr(hakukohderyhmat)
    if (hakukohderyhmatStr.isEmpty) {
      ""
    } else {
      "AND hk.hakukohde_oid IN (" +
        "SELECT hkr_hk.hakukohde_oid FROM pub.pub_dim_hakukohderyhma_ja_hakukohteet hkr_hk " +
        s"WHERE hkr_hk.hakukohderyhma_oid IN ($hakukohderyhmatStr))"
    }
  }
}
