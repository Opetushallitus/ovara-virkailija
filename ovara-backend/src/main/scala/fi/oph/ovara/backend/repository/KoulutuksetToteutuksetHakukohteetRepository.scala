package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.OrganisaationKoulutusToteutusHakukohde
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.springframework.stereotype.Component
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
class KoulutuksetToteutuksetHakukohteetRepository extends Extractors {
  def selectWithParams(
      selectedKayttooikeusOrganisaatiot: List[String],
      haku: List[String],
      koulutuksenTila: Option[String],
      toteutuksenTila: Option[String],
      hakukohteenTila: Option[String],
      valintakoe: Option[Boolean]
  ): SqlStreamingAction[Vector[
    OrganisaationKoulutusToteutusHakukohde
  ], OrganisaationKoulutusToteutusHakukohde, Effect] = {
    val raportointiorganisaatiotStr = RepositoryUtils.makeListOfValuesQueryStr(selectedKayttooikeusOrganisaatiot)
    sql"""SELECT hk.hakukohde_nimi,
                 hk.hakukohde_oid,
                 k.tila AS koulutuksen_tila,
                 t.tila AS toteutuksen_tila,
                 hk.tila AS hakukohteen_tila,
                 hk.aloituspaikat,
                 hk.on_valintakoe,
                 hk.toinen_aste_onko_kaksoistutkinto,
                 hk.jarjestaa_urheilijan_ammkoulutusta,
                 o.organisaatio_oid
          FROM pub.pub_dim_haku h
          JOIN pub.pub_dim_hakukohde hk
          ON h.haku_oid = hk.haku_oid
          JOIN pub.pub_dim_toteutus t
          ON hk.toteutus_oid = t.toteutus_oid
          JOIN pub.pub_dim_koulutus k
          ON k.koulutus_oid = t.koulutus_oid
          JOIN pub.pub_dim_organisaatio o
          ON jarjestyspaikka_oid = o.organisaatio_oid
          WHERE h.haku_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(haku)})
          AND (hk.jarjestyspaikka_oid IN (#$raportointiorganisaatiotStr))
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "k.tila", koulutuksenTila)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "t.tila", toteutuksenTila)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "hk.tila", hakukohteenTila)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "hk.on_valintakoe", valintakoe)}
          """.as[OrganisaationKoulutusToteutusHakukohde]
  }
}
