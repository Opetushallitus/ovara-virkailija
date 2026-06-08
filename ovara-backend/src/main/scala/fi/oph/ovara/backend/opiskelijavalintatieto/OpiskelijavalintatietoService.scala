package fi.oph.ovara.backend.opiskelijavalintatieto

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Service

import scala.util.Try

@Service
class OpiskelijavalintatietoService(db: ReadOnlyDatabase, repository: OpiskelijavalintatietoRepository) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[OpiskelijavalintatietoService])

  def get(oppijanumerot: List[String]): Either[String, Seq[Opiskelijavalintatieto]] = {
    Try {
      db.run(repository.selectOppijat(oppijanumerot), "selectHenkilot")
    }.map {
      case empty if empty.isEmpty => Nil
      case oppijat                =>
        val hakemusRows = db
          .run(repository.selectHakemukset(oppijat.map(_.oppijanumero)), "selectHakemukset")
          .groupBy(_.oppijanumero)

        oppijat.map { oppija =>
          val hakemukset = hakemusRows.get(oppija.oppijanumero).map {
            _.groupBy(_.hakemusOid).values
              .map { rowsForHakemus =>
                val hakutoiveet = rowsForHakemus.map(_.asHakutoive)
                rowsForHakemus.head.asHakemus(hakutoiveet)
              }
              .toSeq
          }

          oppija.asOpiskelijavalintatieto(hakemukset.getOrElse(Seq.empty))
        }
    }.toEither
      .left
      .map { exception =>
        LOG.error("Error fetching hakukohteet", exception)
        "virhe.tietokanta"
      }
  }
}
