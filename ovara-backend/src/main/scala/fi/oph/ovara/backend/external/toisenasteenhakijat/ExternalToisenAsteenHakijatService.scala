package fi.oph.ovara.backend.external.toisenasteenhakijat

import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Service

import scala.util.Try

@Service
class ExternalToisenAsteenHakijatService(repository: ExternalToisenAsteenHakijatRepository) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ExternalToisenAsteenHakijatService])

  def getHakijat(
    hakuOid: String,
    hakukohdeOid: Option[String],
    organisaatioOid: Option[String]
  ): Either[String, Seq[ToisenAsteenHakija]] = {
    Try {
      val hakijaRows = repository.selectHakijat(hakuOid, hakukohdeOid, organisaatioOid)
      if (hakijaRows.isEmpty) {
        Nil
      } else {
        val hakemusOids   = hakijaRows.map(_.hakemusOid).toSet
        val hakutoiveRows = repository.selectHakutoiveet(hakemusOids)

        val koodiUrit = hakutoiveRows.flatMap(_.koulutusKoodiurit).toSet
        val koodistot =
          if (koodiUrit.isEmpty) Map.empty[String, KoodistoArvo]
          else repository.selectKoodistot(koodiUrit).map(k => k.versioituUri -> k).toMap

        val hakutoiveetByHakemus = hakutoiveRows.groupBy(_.hakemusOid)
        hakijaRows.map { row =>
          val toiveet = hakutoiveetByHakemus
            .getOrElse(row.hakemusOid, Seq.empty)
            .map(_.asHakutoive(koodistot))
          row.asHakija(toiveet)
        }
      }
    }.toEither.left
      .map { exception =>
        LOG.error("Error fetching hakijat", exception)
        "virhe.tietokanta"
      }
  }
}
