package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Repository
import slick.jdbc.PostgresProfile.api.actionBasedSQLInterpolation

@Repository
class ValpasRepository(db: ReadOnlyDatabase) extends ValpasExtractors {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ValpasRepository])

  def selectHakemukset(oppijanumerot: List[String]): Seq[HakemusRow] = {
    val query = sql"""
    SELECT haku.haku_oid,
      '2024-08-31 23:59:00.365924+03'::timestamp with time zone as haunAlku, -- koutan hakuajat? hakukohteen hakuajat?
      '2027-08-31 23:59:00.365924+03'::timestamp with time zone as haunLoppu, -- koutan hakuajat? hakukohteen hakuajat? ohjausparametrien PH_HKP?
      haku.hakutapakoodiuri,
      'hakutyyppi_01#1' as hakutyyppi, -- mistä?
      haku.haku_nimi_fi,
      haku.haku_nimi_sv,
      haku.haku_nimi_en,
      hlo.oppijanumero,
      hakemus.hakemus_oid,
      hakemus.muokattu,   -- hakemuksenMuokkauksenAikaleima ?
      hakemus.sahkoposti, -- yhteystiedot hakemukselta?
      hakemus.puhelin,
      hakemus.asuinmaa,   -- myös maa hakemukselta? lyhytnimi?
      hakemus.lahiosoite,
      hakemus.postinumero,
      hakemus.postitoimipaikka,
      null as huoltajanNimi, -- mistä? onko aina null atarun kanssa?
      null as huoltajanPuhelinnumero, -- mistä? onko aina null atarun kanssa?
      null as huoltajanSahkoposti -- mistä? onko aina null atarun kanssa?
    FROM gen.gen_henkilo hlo
    INNER JOIN gen.gen_hakemus hakemus on hakemus.henkilo_oid = hlo.henkilo_oid
    INNER JOIN gen.gen_haku haku on hakemus.haku_oid = haku.haku_oid
    WHERE hlo.oppijanumero in (#${RepositoryUtils.makeListOfValuesQueryStr(oppijanumerot)})
    AND haku.kohdejoukko_koodiuri LIKE 'haunkohdejoukko_11%'
    """.as[HakemusRow]

    LOG.debug(s"selectHakemuksetQuery: ${query.statements.head}")
    db.run(query, "selectValpasHakemukset")
  }

  def selectHakutoiveet(hakemusOids: Iterable[String]): Seq[HakutoiveRow] = {
    val query = sql"""
    SELECT
      ht.hakemus_oid,
      ht.hakukohde_oid,
      ht.hakutoivenumero,
      hk.hakukohde_nimi_fi,
      hk.hakukohde_nimi_sv,
      hk.hakukohde_nimi_en,
      hk.jarjestyspaikka_oid,
      o.nimi_fi,
      o.nimi_sv,
      null as nimi_en,
      k.koulutus_oid,
      k.koulutus_nimi_fi,
      k.koulutus_nimi_sv,
      k.koulutus_nimi_en,
      k.koulutukset_koodiuri,
      ht.vastaanottotieto,
      ht.ilmoittautumisen_tila,
      ht.valintatieto,
      ht.harkinnanvaraisuuden_syy
    FROM gen.gen_hakutoive ht
    LEFT JOIN gen.gen_hakukohde hk ON ht.hakukohde_oid = hk.hakukohde_oid
    LEFT JOIN gen.gen_organisaatio o ON hk.jarjestyspaikka_oid = o.organisaatio_oid
    LEFT JOIN gen.gen_toteutus t ON hk.toteutus_oid = t.toteutus_oid
    LEFT JOIN gen.gen_koulutus k ON t.koulutus_oid = k.koulutus_oid
    WHERE ht.hakemus_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(hakemusOids)})
    """.as[HakutoiveRow]

    LOG.debug(s"selectHakutoiveetQuery: ${query.statements.head}")
    db.run(query, "selectValpasHakutoiveet")
  }

  def selectKoodistot(koodiUrit: Set[String]): Seq[KoodistoArvo] = {
    val query = sql"""
    SELECT versioitu_koodiuri,
      koodiarvo,
      koodistouri,
      koodiversio,
      nimi_fi,
      nimi_sv,
      nimi_en,
      null as lyhytnimi_fi, -- Puuttuu Ovarasta?
      null as lyhytnimi_sv, -- Puuttuu Ovarasta?
      null as lyhytnimi_en -- Puuttuu Ovarasta?
    FROM gen.gen_koodi
    WHERE versioitu_koodiuri in (#${RepositoryUtils.makeListOfValuesQueryStr(koodiUrit)})
    """.as[KoodistoArvo]

    LOG.debug(s"selectKoodistotQuery: ${query.statements.head}")
    db.run(query, "selectValpasKoodistot")
  }
}
