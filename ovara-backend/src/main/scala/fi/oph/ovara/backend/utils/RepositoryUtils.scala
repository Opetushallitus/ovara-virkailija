package fi.oph.ovara.backend.utils

import scala.util.matching.Regex

object RepositoryUtils {
  def extractAlkamisvuosiAndKausi(alkamiskaudet: List[String]): List[(Int, String)] = {
    val alkamiskausiRegex: Regex = """(\d{4})_([a-z]+)""".r
    alkamiskaudet.flatMap(alkamiskausi =>
      for alkamiskausiMatches <- alkamiskausiRegex.findAllMatchIn(alkamiskausi) yield
        val kausi = alkamiskausiMatches.group(2) match
          case "syksy" => "kausi_s"
          case "kevat" => "kausi_k"

        (alkamiskausiMatches.group(1).toInt, kausi)
    )
  }

  def makeAlkamiskausiQueryStr(tableNames: List[String], alkamiskausi: (Int, String)): String = {
    s"(${tableNames.map(tableName => s"($tableName.koulutuksen_alkamisvuosi = ${alkamiskausi._1} AND " +
      s"$tableName.koulutuksen_alkamiskausi_koodiuri LIKE '${alkamiskausi._2}%')").mkString(" OR ")})"
  }

  def makeAlkamiskaudetQueryStr(operator: String, tableNames: List[String], alkamiskaudet: List[(Int, String)]): String = {
    def hasMoreThanOne(alkamiskaudet: List[(Int, String)]) = {
      alkamiskaudet.size > 1
    }

    if (alkamiskaudet.isEmpty) {
      ""
    } else {
      s"$operator ${if hasMoreThanOne(alkamiskaudet) then "(" else ""}" +
        s"${alkamiskaudet.map(alkamiskausi => makeAlkamiskausiQueryStr(tableNames = tableNames, alkamiskausi = alkamiskausi)).mkString(" OR ")}" +
        s"${if hasMoreThanOne(alkamiskaudet) then ")" else ""}"
    }
  }

  def makeListOfValuesQueryStr(values: List[String]): String = {
      s"${values.map(s => s"'$s'").mkString(", ")}"
  }

  def makeEqualsQueryStrOfOptional(operator: String, fieldName: String, value: Option[String]): String = {
    value match
      case Some(v) => s"$operator $fieldName = '$v'"
      case None => ""
  }
}
