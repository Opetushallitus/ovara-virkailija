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

    sql"""SELECT t.hakukohde_oid, t.hakukohde_nimi, SUM(t.hakijat) AS hakijat, SUM(t.ensisijaisia) AS ensisijaisia, SUM(t.varasija) AS varasija, SUM(t.hyvaksytyt) AS hyvaksytyt,
    SUM(t.vastaanottaneet) AS vastaanottaneet, SUM(t.lasna) AS lasna, SUM(t.poissa) AS poissa, SUM(t.ilm_yht) AS ilm_yht, SUM(t.aloituspaikat) AS aloituspaikat
    FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
    WHERE t.haku_oid IN (#$hakuStr)
    AND t.organisaatio_oid IN (#$raportointiorganisaatiotStr)
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)}
    #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "t.sukupuoli", sukupuoli)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.harkinnanvaraisuuden_syy", harkinnanvaraisuudetWithSureValues)}
    GROUP BY t.hakukohde_oid, t.hakukohde_nimi""".as[HakeneetHyvaksytytVastaanottaneet]
  }
}
