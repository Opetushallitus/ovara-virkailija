package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.{KKPaatettavaOpiskeluoikeusEntity, KKSitovastiVastaanottanut, YosHenkilo}
import fi.oph.ovara.backend.raportointi.dto.KkPaatettavatOpiskeluoikeudetParams
import fi.oph.ovara.backend.utils.{ParametriKaannos, RepositoryUtils}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.{Component, Repository}
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
@Repository
class KkPaatettavatOpiskeluoikeudetRepository extends Extractors {

  val LOG: Logger = LoggerFactory.getLogger(classOf[KkPaatettavatOpiskeluoikeudetRepository])
  def organisaatioNameQuery(
    oppilaitos: String
  ): SqlStreamingAction[Vector[ParametriKaannos], ParametriKaannos, Effect] = {

    val query = sql"""
       SELECT 'oppilaitos' AS param, organisaatio_nimi AS nimi
       FROM pub.pub_dim_organisaatio
       WHERE organisaatio_oid = $oppilaitos
      """.as[ParametriKaannos]
    LOG.debug(s"hakuParamNamesQuery: ${query.statements.head}")
    query
  }

  def opiskeluoikeudetQuery(
   organisaatioOids: List[String],
   oppijanumero: Option[String],
   opiskeluoikeudenTila: Option[String]
   ): SqlStreamingAction[Vector[KKPaatettavaOpiskeluoikeusEntity], KKPaatettavaOpiskeluoikeusEntity, Effect] = {
    val queryOppijanumero = oppijanumero.map(o => s"AND oppijanumero = '$o'").getOrElse("")
    val queryOpiskeluOikeudenTila = opiskeluoikeudenTila.map(t => s"AND virta_opiskeluoikeuden_tila = '$t'")
    val query = sql"""
        SELECT henkilo_oid AS opiskelijaAvain, virta_tunniste AS opiskeluoikeusAvain, nimi_fi, nimi_sv, nimi_en, virta_tila_nimi_fi AS opiskeluoikeudenViimeisinTila, koulutusaste, koulutus_koodi AS koulutusKoodi
        FROM gen.gen_opiskeluoikeus_kk
        WHERE yos IS TRUE AND organisaatio_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(organisaatioOids)})
        $queryOppijanumero $queryOpiskeluOikeudenTila
      """.as[KKPaatettavaOpiskeluoikeusEntity]
    LOG.debug(s"opiskeluoikeudetQuery: ${query.statements.head}")
    query
  }

  def vastaanottaneetQuery(
    henkiloOids: List[String]
  ): SqlStreamingAction[Vector[KKSitovastiVastaanottanut], KKSitovastiVastaanottanut, Effect] = {
    val query = sql"""
        SELECT vr.henkilo_oid AS oppijanumero, vr.hakemus_oid AS hakemusOid, vr.hakukohde_oid AS hakukohdeOid, hk.hakukohde_nimi_fi, hk.hakukohde_nimi_sv, hk.hakukohde_nimi_en, vr.vastaanotto_aikaleima as vastaanottoAjankohta, hk.haku_oid AS hakuOid, koulutusaste
        FROM gen.gen_valintarekisteri vr 
        INNER JOIN gen.gen_hakukohde hk ON vr.hakukohde_oid = hk.hakukohde_oid
        WHERE hk.yos IS TRUE
        AND vr.vastaanotto_tila = 'VASTAANOTTANUT_SITOVASTI' 
        AND vr.henkilo_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(henkiloOids)})
    """.as[KKSitovastiVastaanottanut]
    LOG.debug(s"sitovastiVastaanottaneetQuery: ${query.statements.head}")
    query
  }

  def henkilotQuery(henkiloOids: List[String], params: KkPaatettavatOpiskeluoikeudetParams) : SqlStreamingAction[Vector[YosHenkilo], YosHenkilo, Effect] = {
    val query = sql"""
          SELECT sukunimi, etunimet, kutsumanimi, hetu, syntymaAika, oppijanumero
          FROM gen.gen_henkilo
          WHERE oppijanumero IN (#${RepositoryUtils.makeListOfValuesQueryStr(henkiloOids)})
      """.as[YosHenkilo]
    LOG.debug(s"yosHenkilotQuery: ${query.statements.head}")
    query
  }
}
