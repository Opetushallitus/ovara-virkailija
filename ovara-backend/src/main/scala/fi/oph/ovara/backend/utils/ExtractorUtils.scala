package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*
import org.json4s.jackson.Serialization.read

import java.sql.Date
import java.time.LocalDate

object ExtractorUtils extends GenericOvaraJsonFormats {
  def extractValintatapajonot(jsonArray: Option[String]): List[Valintatapajono] = {
    jsonArray.map(read[List[Valintatapajono]]).getOrElse(List())
  }

  def extractHakuaika(jsonObject: Option[String]): Option[Hakuaika] = {
    jsonObject.map(read[Hakuaika])
  }

  def extractAlkamiskausi(jsonObject: Option[String]): Option[Alkamiskausi] = {
    jsonObject.map(read[Alkamiskausi])
  }
  
  def extractKielistetty(json: Option[String]): Kielistetty =
    json.map(read[Map[Kieli, String]]).getOrElse(Map())

  def extractArray(json: Option[String]): List[String] = {
    json.map(read[List[String]]).getOrElse(List())
  }

  def extractMap(json: Option[String]): Map[String, String] =
    json.map(read[Map[String, String]]).getOrElse(Map())

  def extractCommaSeparatedString(json: Option[String]): Option[String] = {
    val list = extractArray(json)
    if (list.isEmpty) None else Some(list.mkString(","))
  }
  
  def extractOpintojenlaajuus(laajuusnumero: Option[String], laajuusyksikko: Option[String]): Kielistetty = {
    val kielistettyLaajuusyksikko = extractKielistetty(laajuusyksikko)
    laajuusnumero match {
      case Some(numero) =>
        kielistettyLaajuusyksikko.map((kieli: Kieli, yksikko: String) => kieli -> s"$numero $yksikko")
      case None => Map()
    }
  }

  def extractKoulutuksenAlkamisaika(
      alkamiskausikoodiuri: Option[String],
      alkamivuosi: Option[String],
      alkamiskausiJsonObject: Option[String],
      kausienNimet: Option[String]
  ): Kielistetty | Option[LocalDate] = {
    val alkamiskausi            = extractAlkamiskausi(alkamiskausiJsonObject)
    val kielistetytKausienNimet = extractKielistetty(kausienNimet)
    alkamiskausi match {
      case Some(ak: Alkamiskausi) if ak.alkamiskausityyppi == "tarkka alkamisajankohta" =>
        ak.koulutuksenAlkamispaivamaara
      case _ =>
        alkamiskausikoodiuri match {
          case Some(_) if kielistetytKausienNimet.nonEmpty =>
            kielistetytKausienNimet.map((kieli: Kieli, nimi: String) => kieli -> s"$nimi ${alkamivuosi.getOrElse("")}")
          case _ => Map()
        }
    }
  }

  def extractDateOption(date: Option[Date]) = {
    date match {
      case Some(d) => Some(d.toLocalDate)
      case None    => None
    }
  }
}
