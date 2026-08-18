package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.service.CommonService
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Service

import java.time.format.DateTimeFormatter
import scala.util.Try

@Service
class ExternalKKHakijatService(repository: ExternalKKHakijatRepository, commonService: CommonService) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ExternalKKHakijatService])

  private val AikaleimaFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

  def getKKHakijat(
    hakuOid: String,
    hakukohdeOid: Option[String],
    organisaatioOid: Option[String],
    valintarajaus: Valintarajaus = Valintarajaus.HAKENEET,
    scope: KayttooikeusScope = KayttooikeusScope.paakayttaja
  ): Either[String, Seq[KKHakija]] = {
    Try {
      // Sekä rajapinnan organisaatioOid (rajaava parametri) että käyttäjän oikeudet vertaillaan hakukohteen
      // jarjestyspaikka_oidia vasten, joka on tyypillisesti toimipiste. Molemmat on siis
      // laajennettava lapsiorganisaatioihin, jotta koulutustoimija- tai oppilaitostason
      // valinta ja oikeus osuvat alempana oleviin järjestyspaikkoihin.
      val organisaatioOids =
        organisaatioOid.map(oid => commonService.getOrganisaatioidenJaLastenOidit(List(oid))).getOrElse(Nil)
      val expandedScope =
        if (scope.isPaakayttaja) scope
        else KayttooikeusScope.limited(commonService.getOrganisaatioidenJaLastenOidit(scope.allowedOrgOids.toList).toSet)

      val kkHakijaRows = repository.selectKKHakijat(hakuOid, hakukohdeOid, organisaatioOids, valintarajaus)
      if (kkHakijaRows.isEmpty) {
        Nil
      } else {
        val hakemusOids          = kkHakijaRows.map(_.hakemusOid).toSet
        val hakemusRows          = repository.selectHakemukset(hakemusOids)
        val ylioppilaatByHakemus =
          repository.selectYlioppilaat(hakemusOids).map(y => y.hakemusOid -> y).toMap
        val hakemuksetByOid = hakemusRows.groupBy(_.hakemusOid)

        kkHakijaRows.flatMap { row =>
          val matchingRows = hakemuksetByOid
            .getOrElse(row.hakemusOid, Seq.empty)
            .filter(matchesFilters(_, hakukohdeOid, organisaatioOids, valintarajaus, expandedScope))
          if (matchingRows.isEmpty) None
          else {
            val hakemukset = matchingRows.map(mRow =>
              mRow.asKKHakemus(
                hakuOid = row.hakuOid,
                hakuVuosi = row.vuosi.getOrElse(0),
                hakuKausi = row.kausi.getOrElse(""),
                hakemusnumero = row.hakemusOid,
                hakemusJattoAikaleima = row.jatetty.map(_.format(AikaleimaFormatter)),
                hakemusViimeinenMuokkausAikaleima = row.muokattu.map(_.format(AikaleimaFormatter)),
                valinnanAikaleima = mRow.valinnanAikaleima.map(_.format(AikaleimaFormatter)),
                pohjakoulutus = row.pohjakoulutus,
                julkaisulupa = row.valintatuloksenJulkaisulupa
              )
            )
            Some(row.asHakija(hakemukset, ylioppilaatByHakemus.get(row.hakemusOid)))
          }
        }
      }
    }.toEither.left
      .map { exception =>
        LOG.error("Error fetching kk hakijat", exception)
        "virhe.tietokanta"
      }
  }

  private def matchesFilters(
    row: KKHakemusRow,
    hakukohdeOid: Option[String],
    organisaatioOids: Seq[String], // Rajapinnan parametri lapsiorganisaatioineen
    valintarajaus: Valintarajaus,
    scope: KayttooikeusScope // Käyttäjän oikeudet lapsiorganisaatioineen
  ): Boolean = {
    val hakukohdeMatch = hakukohdeOid.forall(_ == row.hakukohdeOid)
    val orgMatch       = organisaatioOids.isEmpty || row.jarjestyspaikkaOid.exists(organisaatioOids.contains)
    val allowedMatch   = scope.isPaakayttaja || row.jarjestyspaikkaOid.exists(scope.allowedOrgOids.contains)
    val stateMatch     = valintarajaus match {
      case Valintarajaus.HAKENEET   => true
      case Valintarajaus.HYVAKSYTYT =>
        row.valintatieto.exists(HyvaksytytValintatiedot.contains)
      case Valintarajaus.VASTAANOTTANEET =>
        row.vastaanottotieto.contains("VASTAANOTTANUT_SITOVASTI")
    }
    hakukohdeMatch && orgMatch && allowedMatch && stateMatch
  }

  private val HyvaksytytValintatiedot: Set[String] =
    Set("HYVAKSYTTY", "HARKINNANVARAISESTI_HYVAKSYTTY", "VARASIJALTA_HYVAKSYTTY")
}
