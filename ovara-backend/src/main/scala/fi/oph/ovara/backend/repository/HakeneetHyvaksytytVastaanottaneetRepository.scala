package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.HakeneetHyvaksytytVastaanottaneet
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.springframework.stereotype.Component
import slick.dbio.Effect
import slick.sql.SqlStreamingAction
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*

@Component
class HakeneetHyvaksytytVastaanottaneetRepository extends Extractors {

  def selectWithParams(
                        selectedKayttooikeusOrganisaatiot: List[String],
                        haut: List[String],
                        hakukohteet: List[String],
                        opetuskielet: List[String],
                        harkinnanvaraisuudet: List[String],
                        sukupuoli: Option[String],
                      ): SqlStreamingAction[Vector[HakeneetHyvaksytytVastaanottaneet], HakeneetHyvaksytytVastaanottaneet, Effect] = {
    val hakuStr = RepositoryUtils.makeListOfValuesQueryStr(haut)
    val raportointiorganisaatiotStr = RepositoryUtils.makeListOfValuesQueryStr(selectedKayttooikeusOrganisaatiot)
    val harkinnanvaraisuudetWithSureValues = RepositoryUtils.enrichHarkinnanvaraisuudet(harkinnanvaraisuudet)
    val opetuskieletFilter = if (opetuskielet.nonEmpty) {
      val opetuskieletStr = opetuskielet.map(k => s"'$k'").mkString(", ")
      s"""AND EXISTS (
              SELECT 1 FROM jsonb_array_elements_text(h.oppilaitoksen_opetuskieli) AS elem
              WHERE elem IN ($opetuskieletStr)
            )"""
    } else ""

    sql"""SELECT t.hakukohde_oid, h.hakukohde_nimi, h.organisaatio_nimi, SUM(t.hakijat) AS hakijat, SUM(t.ensisijaisia) AS ensisijaisia, SUM(t.varasija) AS varasija, SUM(t.hyvaksytyt) AS hyvaksytyt,
    SUM(t.vastaanottaneet) AS vastaanottaneet, SUM(t.lasna) AS lasna, SUM(t.poissa) AS poissa, SUM(t.ilm_yht) AS ilm_yht, MIN(t.aloituspaikat) AS aloituspaikat
    FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    WHERE h.haku_oid IN (#$hakuStr)
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.jarjestyspaikka_oid", selectedKayttooikeusOrganisaatiot)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.harkinnanvaraisuuden_syy", harkinnanvaraisuudetWithSureValues)}
    #$opetuskieletFilter
    GROUP BY t.hakukohde_oid, h.hakukohde_nimi, h.organisaatio_nimi""".as[HakeneetHyvaksytytVastaanottaneet]
  }
}
