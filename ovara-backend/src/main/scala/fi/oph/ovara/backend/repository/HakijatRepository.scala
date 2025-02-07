package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.Hakija
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.springframework.stereotype.Component
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
class HakijatRepository extends Extractors {
  def selectWithParams(
      kayttooikeusOrganisaatiot: List[String],
      haut: List[String],
      oppilaitokset: List[String],
      toimipisteet: List[String],
      hakukohteet: List[String],
      vastaanottotieto: List[String],
      harkinnanvaraisuudet: List[String],
      kaksoistutkintoKiinnostaa: Option[Boolean],
      soraTerveys: Option[Boolean],
      markkinointilupa: Option[Boolean],
      julkaisulupa: Option[Boolean]
  ): SqlStreamingAction[Vector[Hakija], Hakija, Effect] = {
    val hakuStr                     = RepositoryUtils.makeListOfValuesQueryStr(haut)
    val raportointiorganisaatiotStr = RepositoryUtils.makeListOfValuesQueryStr(kayttooikeusOrganisaatiot)

    def mapVastaanottotiedotToDbValues(vastaanottotiedot: List[String]) = {
      vastaanottotiedot.flatMap {
        case "vastaanottaneet" => Some("VASTAANOTTANUT_SITOVASTI")
        case s: String         => Some(s.toUpperCase)
        case null              => None
      }
    }
    val vastaanottotiedotAsDbValues        = mapVastaanottotiedotToDbValues(vastaanottotieto)
    val harkinnanvaraisuudetWithSureValues = RepositoryUtils.enrichHarkinnanvaraisuudet(harkinnanvaraisuudet)

    sql"""SELECT concat_ws(',', hlo.sukunimi, hlo.etunimet), hlo.turvakielto,
                 hlo.kansalaisuus_nimi, hlo.henkilo_oid, hlo.hakemus_oid,
                 hk.hakukohde_nimi, hk.hakukohde_oid, ht.hakutoivenumero, ht2.kaksoistutkinto_kiinnostaa,
                 vt.valinnan_tila, ht.vastaanottotieto, ht2.harkinnanvaraisuuden_syy, ht2.sora_aiempi, ht2.sora_terveys, hlo.koulutusmarkkinointilupa,
                 hlo.valintatuloksen_julkaisulupa, hlo.sahkoinenviestintalupa,
                 hlo.lahiosoite, hlo.postinumero, hlo.postitoimipaikka
          FROM pub.pub_dim_henkilo hlo
          JOIN pub.pub_fct_hakemus hakemus
          ON hlo.hakemus_oid = hakemus.hakemus_oid
          JOIN pub.pub_dim_hakutoive ht
          ON ht.henkilo_oid = hlo.henkilo_oid
          JOIN pub.pub_dim_hakutoive_toinen_aste ht2
          ON ht.hakutoive_id = ht2.hakutoive_id
          JOIN pub.pub_dim_hakukohde hk
          ON ht.hakukohde_oid = hk.hakukohde_oid
          JOIN pub.pub_dim_organisaatio o
          ON hk.jarjestyspaikka_oid = o.organisaatio_oid
          LEFT JOIN pub.pub_dim_valinnantulos vt
          ON hakemus.hakemus_oid = vt.hakemus_oid AND hk.hakukohde_oid = vt.hakukohde_oid
          WHERE hakemus.haku_oid IN (#$hakuStr)
          AND hk.jarjestyspaikka_oid IN (#$raportointiorganisaatiotStr)
          #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "hk.hakukohde_oid", hakukohteet)}
          #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "ht.vastaanottotieto", vastaanottotiedotAsDbValues)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "ht2.kaksoistutkinto_kiinnostaa", kaksoistutkintoKiinnostaa)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "ht2.sora_terveys", soraTerveys)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "hlo.koulutusmarkkinointilupa", markkinointilupa)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "hlo.valintatuloksen_julkaisulupa", julkaisulupa)}
          #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "ht2.harkinnanvaraisuuden_syy", harkinnanvaraisuudetWithSureValues)}""".as[Hakija]
  }
}
