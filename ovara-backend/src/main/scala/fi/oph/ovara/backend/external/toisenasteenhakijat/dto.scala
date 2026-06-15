package fi.oph.ovara.backend.external.toisenasteenhakijat

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import fi.oph.ovara.backend.opiskelijavalintatieto.KielistettyResponse
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode

import scala.annotation.meta.field
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

@JsonInclude(Include.ALWAYS)
case class HakijatResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakijat: java.util.List[HakijaResponse]
)

object HakijatResponse {
  def apply(hakijat: Seq[ToisenAsteenHakija]): HakijatResponse =
    HakijatResponse(hakijat.map(HakijaResponse.apply).asJava)
}

@JsonInclude(Include.ALWAYS)
case class HakijaResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hetu: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty oppijanumero: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty sukunimi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty etunimet: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kutsumanimi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty lahiosoite: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty postinumero: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty postitoimipaikka: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty maa: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kansalaisuudet: java.util.List[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty matkapuhelin: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty muupuhelin: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty sahkoposti: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kotikunta: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty sukupuoli: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty aidinkieli: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty opetuskieli: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty koulutusmarkkinointilupa: Option[Boolean],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kiinnostunutoppisopimuksesta: Option[Boolean],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty huoltaja1: Option[HuoltajaResponse],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty huoltaja2: Option[HuoltajaResponse],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty oikeusMaksuttomaanKoulutukseenVoimassaAsti: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty oppivelvollisuusVoimassaAsti: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty sahkoisenAsioinninLupa: Option[Boolean],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakemus: HakijaHakemusResponse,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty lisakysymykset: java.util.List[LisakysymysResponse]
)

object HakijaResponse {
  def apply(h: ToisenAsteenHakija): HakijaResponse =
    HakijaResponse(
      hetu = h.hetu,
      oppijanumero = h.oppijanumero,
      sukunimi = h.sukunimi,
      etunimet = h.etunimet,
      kutsumanimi = h.kutsumanimi,
      lahiosoite = h.lahiosoite,
      postinumero = h.postinumero,
      postitoimipaikka = h.postitoimipaikka,
      maa = h.maa,
      kansalaisuudet = h.kansalaisuudet.asJava,
      matkapuhelin = h.matkapuhelin,
      muupuhelin = h.muupuhelin,
      sahkoposti = h.sahkoposti,
      kotikunta = h.kotikunta,
      sukupuoli = h.sukupuoli,
      aidinkieli = h.aidinkieli,
      opetuskieli = h.opetuskieli,
      koulutusmarkkinointilupa = h.koulutusmarkkinointilupa,
      kiinnostunutoppisopimuksesta = h.kiinnostunutoppisopimuksesta,
      huoltaja1 = h.huoltaja1.map(HuoltajaResponse.apply),
      huoltaja2 = h.huoltaja2.map(HuoltajaResponse.apply),
      oikeusMaksuttomaanKoulutukseenVoimassaAsti = h.oikeusMaksuttomaanKoulutukseenVoimassaAsti,
      oppivelvollisuusVoimassaAsti = h.oppivelvollisuusVoimassaAsti,
      sahkoisenAsioinninLupa = h.sahkoisenAsioinninLupa,
      hakemus = HakijaHakemusResponse(h.hakemus),
      lisakysymykset = h.lisakysymykset.map(LisakysymysResponse.apply).asJava
    )
}

@JsonInclude(Include.ALWAYS)
case class HuoltajaResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty etunimi: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty sukunimi: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty puhelinnumero: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty sahkoposti: String
)

object HuoltajaResponse {
  def apply(h: Huoltaja): HuoltajaResponse =
    HuoltajaResponse(h.etunimi, h.sukunimi, h.puhelinnumero, h.sahkoposti)
}

@JsonInclude(Include.ALWAYS)
case class HakijaHakemusResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty vuosi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kausi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakemusnumero: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakemuksenJattopaiva: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakemuksenMuokkauspaiva: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty lahtokoulu: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty lahtokoulunnimi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty luokka: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty luokkataso: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty pohjakoulutus: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty todistusvuosi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty muukoulutus: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty julkaisulupa: Option[Boolean],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty yhteisetaineet: Option[BigDecimal],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty lukiontasapisteet: Option[BigDecimal],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty lisapistekoulutus: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty yleinenkoulumenestys: Option[BigDecimal],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty painotettavataineet: Option[BigDecimal],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakutoiveet: java.util.List[HakijaHakutoiveResponse],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty osaaminen: OsaaminenResponse
)

object HakijaHakemusResponse {
  def apply(h: HakijaHakemus): HakijaHakemusResponse =
    HakijaHakemusResponse(
      vuosi = h.vuosi,
      kausi = h.kausi,
      hakemusnumero = h.hakemusnumero,
      hakemuksenJattopaiva = h.hakemuksenJattopaiva,
      hakemuksenMuokkauspaiva = h.hakemuksenMuokkauspaiva,
      lahtokoulu = h.lahtokoulu,
      lahtokoulunnimi = h.lahtokoulunnimi,
      luokka = h.luokka,
      luokkataso = h.luokkataso,
      pohjakoulutus = h.pohjakoulutus,
      todistusvuosi = h.todistusvuosi,
      muukoulutus = h.muukoulutus,
      julkaisulupa = h.julkaisulupa,
      yhteisetaineet = h.yhteisetaineet,
      lukiontasapisteet = h.lukiontasapisteet,
      lisapistekoulutus = h.lisapistekoulutus,
      yleinenkoulumenestys = h.yleinenkoulumenestys,
      painotettavataineet = h.painotettavataineet,
      hakutoiveet = h.hakutoiveet.map(HakijaHakutoiveResponse.apply).asJava,
      osaaminen = OsaaminenResponse(h.osaaminen)
    )
}

@JsonInclude(Include.ALWAYS)
case class HakijaHakutoiveResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakukohdeOid: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakujno: Int,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty oppilaitos: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty opetuspiste: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty opetuspisteennimi: Option[KielistettyResponse],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty koulutus: Option[KoodistoArvoResponse],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty harkinnanvaraisuusperuste: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty urheilijanammatillinenkoulutus: Option[Boolean],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty yhteispisteet: Option[BigDecimal],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valinta: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty vastaanotto: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty lasnaolo: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty terveys: Option[Boolean],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty aiempiperuminen: Option[Boolean],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kaksoistutkinto: Option[Boolean],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty koulutuksenKieli: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty keskiarvo: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty urheilijanLisakysymykset: Option[UrheilijanLisakysymyksetResponse]
)

object HakijaHakutoiveResponse {
  def apply(h: HakijaHakutoive): HakijaHakutoiveResponse =
    HakijaHakutoiveResponse(
      hakukohdeOid = h.hakukohdeOid,
      hakujno = h.hakujno,
      oppilaitos = h.oppilaitos,
      opetuspiste = h.opetuspiste,
      opetuspisteennimi = h.opetuspisteennimi.map(KielistettyResponse.apply),
      koulutus = h.koulutus.map(KoodistoArvoResponse.apply),
      harkinnanvaraisuusperuste = h.harkinnanvaraisuusperuste,
      urheilijanammatillinenkoulutus = h.urheilijanammatillinenkoulutus,
      yhteispisteet = h.yhteispisteet,
      valinta = h.valinta,
      vastaanotto = h.vastaanotto,
      lasnaolo = h.lasnaolo,
      terveys = h.terveys,
      aiempiperuminen = h.aiempiperuminen,
      kaksoistutkinto = h.kaksoistutkinto,
      koulutuksenKieli = h.koulutuksenKieli,
      keskiarvo = h.keskiarvo,
      urheilijanLisakysymykset = h.urheilijanLisakysymykset.map(UrheilijanLisakysymyksetResponse.apply)
    )
}

@JsonInclude(Include.ALWAYS)
case class UrheilijanLisakysymyksetResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty peruskoulu: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty keskiarvo: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty tamakausi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty viimekausi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty toissakausi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty sivulaji: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valmennusryhma_seurajoukkue: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valmennusryhma_piirijoukkue: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valmennusryhma_maajoukkue: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valmentaja_nimi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valmentaja_email: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valmentaja_puh: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty laji: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty liitto: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty seura: Option[String]
)

object UrheilijanLisakysymyksetResponse {
  def apply(u: UrheilijanLisakysymykset): UrheilijanLisakysymyksetResponse =
    UrheilijanLisakysymyksetResponse(
      peruskoulu = u.peruskoulu,
      keskiarvo = u.keskiarvo,
      tamakausi = u.tamakausi,
      viimekausi = u.viimekausi,
      toissakausi = u.toissakausi,
      sivulaji = u.sivulaji,
      valmennusryhma_seurajoukkue = u.valmennusryhma_seurajoukkue,
      valmennusryhma_piirijoukkue = u.valmennusryhma_piirijoukkue,
      valmennusryhma_maajoukkue = u.valmennusryhma_maajoukkue,
      valmentaja_nimi = u.valmentaja_nimi,
      valmentaja_email = u.valmentaja_email,
      valmentaja_puh = u.valmentaja_puh,
      laji = u.laji,
      liitto = u.liitto,
      seura = u.seura
    )
}

@JsonInclude(Include.ALWAYS)
case class OsaaminenResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty yleinen_kielitutkinto_fi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valtionhallinnon_kielitutkinto_fi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty yleinen_kielitutkinto_sv: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valtionhallinnon_kielitutkinto_sv: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty yleinen_kielitutkinto_en: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valtionhallinnon_kielitutkinto_en: Option[String]
)

object OsaaminenResponse {
  def apply(o: Osaaminen): OsaaminenResponse =
    OsaaminenResponse(
      yleinen_kielitutkinto_fi = o.yleinen_kielitutkinto_fi,
      valtionhallinnon_kielitutkinto_fi = o.valtionhallinnon_kielitutkinto_fi,
      yleinen_kielitutkinto_sv = o.yleinen_kielitutkinto_sv,
      valtionhallinnon_kielitutkinto_sv = o.valtionhallinnon_kielitutkinto_sv,
      yleinen_kielitutkinto_en = o.yleinen_kielitutkinto_en,
      valtionhallinnon_kielitutkinto_en = o.valtionhallinnon_kielitutkinto_en
    )
}

@JsonInclude(Include.ALWAYS)
case class LisakysymysResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kysymysid: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakukohdeOids: java.util.List[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kysymystyyppi: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kysymysteksti: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty vastaukset: java.util.List[LisakysymysVastausResponse]
)

object LisakysymysResponse {
  def apply(l: Lisakysymys): LisakysymysResponse =
    LisakysymysResponse(
      kysymysid = l.kysymysid,
      hakukohdeOids = l.hakukohdeOids.asJava,
      kysymystyyppi = l.kysymystyyppi,
      kysymysteksti = l.kysymysteksti,
      vastaukset = l.vastaukset.map(LisakysymysVastausResponse.apply).asJava
    )
}

@JsonInclude(Include.ALWAYS)
case class LisakysymysVastausResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty vastausid: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty vastausteksti: String
)

object LisakysymysVastausResponse {
  def apply(l: LisakysymysVastaus): LisakysymysVastausResponse =
    LisakysymysVastausResponse(l.vastausid, l.vastausteksti)
}

@JsonInclude(Include.ALWAYS)
case class KoodistoArvoResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty versioituUri: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty koodiarvo: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty koodistoUri: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty koodistoVersio: Int,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty nimi: KielistettyResponse
)

object KoodistoArvoResponse {
  def apply(k: KoodistoArvo): KoodistoArvoResponse =
    KoodistoArvoResponse(
      versioituUri = k.versioituUri,
      koodiarvo = k.koodiarvo,
      koodistoUri = k.koodistoUri,
      koodistoVersio = k.koodistoVersio,
      nimi = KielistettyResponse(k.nimi)
    )
}
