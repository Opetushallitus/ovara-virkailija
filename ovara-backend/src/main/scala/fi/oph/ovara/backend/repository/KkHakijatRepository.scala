package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.KkHakija
import fi.oph.ovara.backend.utils.RepositoryUtils
import fi.oph.ovara.backend.utils.RepositoryUtils.makeListOfValuesQueryStr
import org.springframework.stereotype.Component
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
class KkHakijatRepository extends Extractors {
  def selectWithParams(
      kayttooikeusOrganisaatiot: List[String],
      hakukohderyhmat: List[String],
      haut: List[String],
      oppilaitokset: List[String],
      toimipisteet: List[String],
      hakukohteet: List[String],
      valintatieto: List[String],
      vastaanottotieto: List[String],
      kansalaisuus: List[String],
      markkinointilupa: Option[Boolean]
  ): SqlStreamingAction[Vector[KkHakija], KkHakija, Effect] = {
    val hakuStr                     = RepositoryUtils.makeListOfValuesQueryStr(haut)
    val raportointiorganisaatiotStr = RepositoryUtils.makeListOfValuesQueryStr(kayttooikeusOrganisaatiot)

    val vastaanottotiedotAsDbValues = RepositoryUtils.mapVastaanottotiedotToDbValues(vastaanottotieto)
    val valintatiedotAsDbValues     = RepositoryUtils.mapValintatiedotToDbValues(valintatieto)
    val optionalHakukohdeQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "hk.hakukohde_oid", hakukohteet)
    val optionalValintatietoQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr(
        "AND",
        "ht.valintatapajonot->0->>'valinnan_tila'",
        valintatiedotAsDbValues
      )
    val optionalVastaanottotietoQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "ht.vastaanottotieto", vastaanottotiedotAsDbValues)

    val optionalKansalaisuusQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "hlo.kansalaisuusluokka", kansalaisuus)
    val optionalMarkkinointilupaQuery =
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "hlo.koulutusmarkkinointilupa", markkinointilupa)

    val optionalJarjestyspaikkaQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "hk.jarjestyspaikka_oid", kayttooikeusOrganisaatiot)

    val hakukohderyhmatStr = makeListOfValuesQueryStr(hakukohderyhmat)
    val optionalHakukohderyhmaSubSelect = if (hakukohderyhmatStr.isEmpty) {
      ""
    } else {
      "AND hk.hakukohde_oid IN (" +
        "SELECT hkr_hk.hakukohde_oid FROM pub.pub_dim_hakukohderyhma_ja_hakukohteet hkr_hk " +
        s"WHERE hkr_hk.hakukohderyhma_oid IN ($hakukohderyhmatStr))"
    }

    //TODO: palautetaan yo-arvosanat
    sql"""SELECT hlo.sukunimi, hlo.etunimet, hlo.hetu, hlo.syntymaaika,
                 hlo.kansalaisuus_nimi, hlo.henkilo_oid, hlo.hakemus_oid, hk.organisaatio_nimi,
                 hk.hakukohde_nimi, kkh.hakukelpoisuus, ht.hakutoivenumero, ht.valintatieto,
                 ht.ehdollisesti_hyvaksytty, ht.valintatiedon_pvm, ht.valintatapajonot,
                 ht.vastaanottotieto, ht.viimeinen_vastaanottopaiva, ht.ensikertalainen,
                 ht.ilmoittautumisen_tila, kkh.pohjakoulutus, kkh.maksuvelvollisuus,
                 hlo.valintatuloksen_julkaisulupa, hlo.koulutusmarkkinointilupa, hlo.sahkoinenviestintalupa,
                 hlo.lahiosoite, hlo.postinumero, hlo.postitoimipaikka, hlo.kotikunta_nimi,
                 hlo.asuinmaa_nimi, hlo.puhelin, hlo.sahkoposti
          FROM pub.pub_dim_henkilo hlo
          JOIN pub.pub_dim_hakutoive ht
          ON ht.henkilo_hakemus_id = hlo.henkilo_hakemus_id
          JOIN pub.pub_dim_hakukohde hk
          ON ht.hakukohde_oid = hk.hakukohde_oid
          JOIN pub.pub_fct_raportti_hakijat_kk kkh
          ON ht.hakutoive_id = kkh.hakutoive_id
          WHERE ht.haku_oid IN (#$hakuStr)
          #$optionalJarjestyspaikkaQuery
          #$optionalHakukohderyhmaSubSelect
          #$optionalHakukohdeQuery
          #$optionalValintatietoQuery
          #$optionalVastaanottotietoQuery
          #$optionalKansalaisuusQuery
          #$optionalMarkkinointilupaQuery
          """.as[KkHakija]
  }
}
