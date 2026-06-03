package fi.oph.ovara.backend.opiskelijavalintatieto

import fi.oph.ovara.backend.repository.Extractors
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Repository
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Repository
class OpiskelijavalintatietoRepository extends OpiskelijavalintatietoExtractors {
  val LOG: Logger = LoggerFactory.getLogger(classOf[OpiskelijavalintatietoRepository])

  def selectOppijat(oppijanumerot: List[String]): SqlStreamingAction[Vector[OppijaRow], OppijaRow, Effect] = {
    sql"""SELECT hlo.oppijanumero, hlo.hetu, hlo.syntymaaika, hlo.sukunimi, hlo.etunimet
          FROM gen.gen_henkilo hlo
          WHERE hlo.oppijanumero in (#${RepositoryUtils.makeListOfValuesQueryStr(oppijanumerot)})
        """.as[OppijaRow]
  }

  def selectHakemukset(oppijanumerot: Seq[String]): SqlStreamingAction[Vector[HakemusRow], HakemusRow, Effect] = {
    sql"""SELECT hlo.oppijanumero,
                 ht.hakemus_oid,
                 ht.haku_oid,
                 h.haku_nimi_fi,
                 h.haku_nimi_sv,
                 h.haku_nimi_en,
                 h.kohdejoukko_koodiuri,
                 h.hakutapakoodiuri,
                 ht.hakukohde_oid,
                 hk.hakukohde_nimi_fi,
                 hk.hakukohde_nimi_sv,
                 hk.hakukohde_nimi_en,
                 hk.jarjestyspaikka_oid,
                 o.nimi_fi as org_nimi_fi,
                 o.nimi_sv as org_nimi_sv,
                 null as org_nimi_en,
                 coalesce(hk.koulutuksen_alkamiskausiuri, h.koulutuksen_alkamiskausiuri) as koulutuksen_alkamiskausiuri,
                 coalesce(hk.koulutuksen_alkamisvuosi, h.koulutuksen_alkamisvuosi) as koulutuksen_alkamisvuosi,
                 ht.valintatieto,
                 ht.vastaanottotieto,
                 ht.ilmoittautumisen_tila
          FROM gen.gen_henkilo hlo
          INNER JOIN gen.gen_hakutoive ht ON ht.henkilo_oid = hlo.henkilo_oid
          LEFT JOIN gen.gen_hakukohde hk ON ht.hakukohde_oid = hk.hakukohde_oid
          LEFT JOIN gen.gen_haku h ON hk.haku_oid = h.haku_oid
          LEFT JOIN gen.gen_organisaatio o ON hk.jarjestyspaikka_oid = o.organisaatio_oid
          WHERE hlo.oppijanumero in (#${RepositoryUtils.makeListOfValuesQueryStr(oppijanumerot)})
          ORDER BY hlo.oppijanumero, ht.hakemus_oid
         """.as[HakemusRow]
  }
}
