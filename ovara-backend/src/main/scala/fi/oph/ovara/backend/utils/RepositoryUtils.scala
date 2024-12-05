package fi.oph.ovara.backend.utils

import scala.util.matching.Regex

object RepositoryUtils {
  def extractAlkamisvuosiKausiAndHenkkohtSuunnitelma(alkamiskaudet: List[String]): (List[(Int, String)], Boolean) = {
    val alkamiskausiRegex: Regex = """(\d{4})_([a-z]+)""".r
    val alkamiskaudetAndVuodet = alkamiskaudet.flatMap(alkamiskausi =>
      for alkamiskausiMatches <- alkamiskausiRegex.findAllMatchIn(alkamiskausi) yield
        val kausi = alkamiskausiMatches.group(2) match
          case "syksy" => "kausi_s"
          case "kevat" => "kausi_k"

        (alkamiskausiMatches.group(1).toInt, kausi)
    )
    val henkilokohtainenSuunnitelma = alkamiskaudet.contains("henkilokohtainen_suunnitelma")

    (alkamiskaudetAndVuodet, henkilokohtainenSuunnitelma)
  }

  def makeListOfValuesQueryStr(values: List[String]): String = {
    s"${values.map(s => s"'$s'").mkString(", ")}"
  }

  def makeEqualsQueryStrOfOptional(operator: String, fieldName: String, value: Option[String]): String = {
    value match
      case Some(v) => s"$operator $fieldName = '$v'"
      case None => ""
  }

  def makeEqualsQueryStrOfOptionalBoolean(operator: String, fieldName: String, value: Option[Boolean]): String = {
    value match
      case Some(v) => s"$operator $fieldName = $v"
      case None => ""
  }

  def makeAlkamiskaudetAndHenkkohtSuunnitelmaQuery(alkamiskaudetAndHenkkohtSuunnitelma: (List[(Int, String)], Boolean)): String = {
    val alkamiskaudet = alkamiskaudetAndHenkkohtSuunnitelma._1
    val henkkohtSuunnitelma = alkamiskaudetAndHenkkohtSuunnitelma._2

    if (alkamiskaudet.isEmpty & !henkkohtSuunnitelma) {
      ""
    } else {
      val henkkohtSuunnitelmaQueryStr = if henkkohtSuunnitelma then makeOptionalHenkilokohtainenSuunnitelmaQuery(henkkohtSuunnitelma) else ""

      val andOrOrQueryStr = if (henkkohtSuunnitelma & alkamiskaudet.isEmpty) {
        s"$henkkohtSuunnitelmaQueryStr"
      } else if (henkkohtSuunnitelma & alkamiskaudet.nonEmpty) {
        s" OR $henkkohtSuunnitelmaQueryStr"
      } else {
        ""
      }

      s"AND (${makeAlkamiskaudetQueryStr(List("t", "hk"), alkamiskaudetAndHenkkohtSuunnitelma._1)}" +
        s"$andOrOrQueryStr)"
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
    s"(${
      tableNames.map(tableName => s"($tableName.koulutuksen_alkamisvuosi = ${alkamiskausi._1} AND " +
        s"$tableName.koulutuksen_alkamiskausi_koodiuri LIKE '${alkamiskausi._2}%')").mkString(" OR ")
    })"
  }

  def makeOptionalHenkilokohtainenSuunnitelmaQuery(henkkohtSuunnitelma: Boolean): String = {
    if henkkohtSuunnitelma then "hk.koulutuksen_alkamiskausi_tyyppi = 'henkilokohtainen suunnitelma'" else ""
  }
}
