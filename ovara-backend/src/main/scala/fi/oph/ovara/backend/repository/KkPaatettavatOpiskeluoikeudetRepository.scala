package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.{KKPaatettavaOpiskeluoikeusEntity, KKSitovastiVastaanottanut, YosHenkilo, YosValintarekisteriTiedot}
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
    val oppijanumeroQueryPart = oppijanumero.filterNot(_.isBlank).map(o => s"AND oo.henkilo_oid = '$o'").getOrElse("")
    val opiskeluOikeudenTilaQueryPart: String =
      opiskeluoikeudenTila
        .filterNot(_.isBlank)
        .map(t =>
          if (t.equals("paatettavissa"))
            "AND oo.virta_opiskeluoikeuden_tila in ('1', '2', '4')"
          else if (t.equals("paatetty")) "AND oo.virta_opiskeluoikeuden_tila = '7'"
          else ""
        )
        .getOrElse("")
    val query = sql"""
        SELECT oo.henkilo_oid AS opiskelijaAvain, oo.virta_tunniste AS opiskeluoikeusAvain, oo.nimi_fi, oo.nimi_sv, oo.nimi_en, oo.virta_opiskeluoikeuden_tila AS opiskeluoikeudenViimeisinTila, oo.koulutusaste, oo.koulutus_koodi AS koulutusKoodi, linkitetty.koulutusaste AS linkitettyKoulutusAste
        FROM gen.gen_opiskeluoikeus_kk oo
        LEFT JOIN gen.gen_opiskeluoikeus_kk linkitetty on linkitetty.virta_tunniste = oo.liittyva_opiskeluoikeus_avain
        WHERE oo.yos IS TRUE AND oo.organisaatio_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(organisaatioOids)})
        #$oppijanumeroQueryPart #$opiskeluOikeudenTilaQueryPart
      """.as[KKPaatettavaOpiskeluoikeusEntity]
    LOG.debug(s"opiskeluoikeudetQuery: ${query.statements.head}")
    query
  }

  def vastaanottaneetQuery(
    henkiloOids: List[String]
  ): SqlStreamingAction[Vector[KKSitovastiVastaanottanut], KKSitovastiVastaanottanut, Effect] = {
    val query = sql"""
        SELECT vr.henkilo_oid AS oppijanumero,
          vr.hakemus_oid AS hakemusOid,
          vr.hakukohde_oid AS hakukohdeOid,
          hk.hakukohde_nimi_fi, hk.hakukohde_nimi_sv, hk.hakukohde_nimi_en,
          vr.vastaanotto_aikaleima as vastaanottoAjankohta,
          hk.haku_oid AS hakuOid,
          hk.koulutusasteet,
          haku.haku_nimi_fi, haku.haku_nimi_sv, haku.haku_nimi_en,
          org.organisaatio_oid,
          org.nimi_fi, org.nimi_sv, org.nimi_en
        FROM gen.gen_valintarekisteri vr 
        INNER JOIN gen.gen_hakukohde hk ON vr.hakukohde_oid = hk.hakukohde_oid
        INNER JOIN gen.gen_haku haku on haku.haku_oid = hk.haku_oid
        INNER JOIN gen.gen_organisaatio org on org.organisaatio_oid = hk.jarjestyspaikka_oid
        WHERE hk.yos IS TRUE
        AND vr.ehdollisesti_hyvaksyttavissa IS FALSE
        AND vr.vastaanotto_tila = 'VASTAANOTTANUT_SITOVASTI' 
        AND vr.henkilo_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(henkiloOids)})
    """.as[KKSitovastiVastaanottanut]
    LOG.debug(s"sitovastiVastaanottaneetQuery: ${query.statements.head}")
    query
  }

  def henkilotQuery(
    henkiloOids: List[String],
    params: KkPaatettavatOpiskeluoikeudetParams
  ): SqlStreamingAction[Vector[YosHenkilo], YosHenkilo, Effect] = {
    val sukunimiQueryPart = params.sukunimi.filterNot(_.isBlank).map(s => s"AND h.sukunimi LIKE '%$s%'").getOrElse("")
    val etunimetQueryPart = params.etunimet.filterNot(_.isBlank).map(e => s"AND h.etunimet LIKE '%$e%'").getOrElse("")
    val hetuQueryPart     = params.hetu.filterNot(_.isBlank).map(h => s"AND h.hetu = '$h'").getOrElse("")
    val query             = sql"""
          SELECT h.sukunimi, h.etunimet, hakemus.kutsumanimi, h.hetu, h.syntymaaika, h.oppijanumero
          FROM gen.gen_henkilo h
          LEFT JOIN LATERAL (SELECT * FROM gen.gen_hakemus hak
                             WHERE hak.kutsumanimi IS NOT NULL
                             AND hak.henkilo_oid = h.oppijanumero
                             ORDER BY hak.jatetty DESC LIMIT 1)
                             hakemus on true
          WHERE h.oppijanumero IN (#${RepositoryUtils.makeListOfValuesQueryStr(henkiloOids)})
          #$sukunimiQueryPart #$etunimetQueryPart #$hetuQueryPart
      """.as[YosHenkilo]
    LOG.debug(s"yosHenkilotQuery: ${query.statements.head}")
    query
  }

  def valintarekisteriYosQuery(
    henkiloOids: List[String]
  ): SqlStreamingAction[Vector[YosValintarekisteriTiedot], YosValintarekisteriTiedot, Effect] = {
    val query = sql"""
        SELECT y.henkilo_oid, y.hakukohde_oid, y.hakemus_oid, y.paatelty_aloituspvm, y.virta_opiskeluikeus_id
        FROM gen.gen_valintarekisteri_yos y
        WHERE y.henkilo_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(henkiloOids)})
         """.as[YosValintarekisteriTiedot]
    LOG.debug(s"valintarekisteriYosQuery: ${query.statements.head}")
    query
  }
}
