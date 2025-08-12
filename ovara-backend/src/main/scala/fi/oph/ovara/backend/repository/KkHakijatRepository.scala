package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.KkHakija
import fi.oph.ovara.backend.utils.RepositoryUtils
import fi.oph.ovara.backend.utils.RepositoryUtils.*
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.{Component, Repository}
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
@Repository
class KkHakijatRepository extends Extractors {
  val LOG: Logger = LoggerFactory.getLogger(classOf[KkHakijatRepository])

  def selectWithParams(
      kayttooikeusOrganisaatiot: List[String],
      isOrganisaatioRajain: Boolean,
      kayttooikeusHakukohderyhmat: List[String],
      hakukohderyhmat: List[String],
      haut: List[String],
      oppilaitokset: List[String],
      toimipisteet: List[String],
      hakukohteet: List[String],
      valintatiedot: List[String],
      vastaanottotiedot: List[String],
      kansalaisuusluokat: List[String],
      markkinointilupa: Option[Boolean]
  ): SqlStreamingAction[Vector[KkHakija], KkHakija, Effect] = {
    val hakuStr                     = makeListOfValuesQueryStr(haut)

    val vastaanottotiedotAsDbValues = mapVastaanottotiedotToDbValues(vastaanottotiedot)
    val valintatiedotAsDbValues     = mapValintatiedotToDbValues(valintatiedot)
    val optionalHakukohdeQuery =
      makeOptionalListOfValuesQueryStr("AND", "hk.hakukohde_oid", hakukohteet)
    val optionalValintatietoQuery =
      makeOptionalListOfValuesQueryStr(
        "AND",
        "ht.valintatieto",
        valintatiedotAsDbValues
      )
    val optionalVastaanottotietoQuery =
      makeOptionalListOfValuesQueryStr("AND", "ht.vastaanottotieto", vastaanottotiedotAsDbValues)

    val optionalKansalaisuusQuery =
      makeOptionalListOfValuesQueryStr("AND", "hlo.kansalaisuusluokka", kansalaisuusluokat)
    val optionalMarkkinointilupaQuery =
      makeEqualsQueryStrOfOptionalBoolean("AND", "hlo.koulutusmarkkinointilupa", markkinointilupa)

    val organisaatioKayttooikeusQueryStr =
      if (isOrganisaatioRajain) {
        // jos organisaatio on valittu, ei huomioida käyttäjän organisaatioiden ulkopuolisia hakukohderyhmiä
        RepositoryUtils.makeHakukohderyhmaSubSelectQueryWithKayttooikeudet(kayttooikeusOrganisaatiot, List.empty)
      } else {
        RepositoryUtils.makeHakukohderyhmaSubSelectQueryWithKayttooikeudet(kayttooikeusOrganisaatiot, kayttooikeusHakukohderyhmat)
      }

    val optionalHakukohderyhmaSubSelect = makeOptionalHakukohderyhmatSubSelectQueryStr(hakukohderyhmat)

    val query = sql"""SELECT hlo.sukunimi, hlo.etunimet, hlo.turvakielto, hlo.hetu, hlo.syntymaaika,
                 hlo.kansalaisuudet_nimi, hlo.henkilo_oid, hlo.hakemus_oid, hk.organisaatio_nimi,
                 hk.hakukohde_nimi, kkh.hakukelpoisuus, ht.hakutoivenumero, ht.valintatieto,
                 ht.ehdollisesti_hyvaksytty, ht.valintatiedon_pvm, ht.valintatapajonot,
                 ht.vastaanottotieto, ht.viimeinen_vastaanottopaiva, ht.ensikertalainen,
                 ht.ilmoittautumisen_tila, kkh.pohjakoulutus, kkh.maksuvelvollisuus, hlo.hakemusmaksun_tila,
                 hlo.koulutusmarkkinointilupa, hlo.sahkoinenviestintalupa,
                 hlo.lahiosoite, hlo.postinumero, hlo.postitoimipaikka, hlo.kotikunta_nimi,
                 hlo.asuinmaa_nimi, hlo.puhelin, hlo.sahkoposti, yo.arvosanat
          FROM pub.pub_dim_henkilo hlo
          JOIN pub.pub_dim_hakutoive ht
          ON ht.henkilo_hakemus_id = hlo.henkilo_hakemus_id
          JOIN pub.pub_dim_hakukohde hk
          ON ht.hakukohde_oid = hk.hakukohde_oid
          JOIN pub.pub_fct_raportti_hakijat_kk kkh
          ON ht.hakutoive_id = kkh.hakutoive_id
          LEFT JOIN pub.pub_dim_arvosana_yo yo
          ON yo.henkilo_oid = ht.henkilo_oid
          WHERE ht.haku_oid IN (#$hakuStr)
          #$organisaatioKayttooikeusQueryStr
          #$optionalHakukohderyhmaSubSelect
          #$optionalHakukohdeQuery
          #$optionalValintatietoQuery
          #$optionalVastaanottotietoQuery
          #$optionalKansalaisuusQuery
          #$optionalMarkkinointilupaQuery
          """.as[KkHakija]

    LOG.debug(s"selectWithParams: ${query.statements.head}")

    query
  }
}
