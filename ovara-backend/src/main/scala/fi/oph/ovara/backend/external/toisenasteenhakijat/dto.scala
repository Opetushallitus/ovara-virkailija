package fi.oph.ovara.backend.external.toisenasteenhakijat

import com.fasterxml.jackson.annotation.{JsonFormat, JsonInclude, JsonPropertyOrder}
import com.fasterxml.jackson.annotation.JsonInclude.Include
import fi.oph.ovara.backend.opiskelijavalintatieto.KielistettyResponse
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode

import java.lang.Boolean as JBoolean
import java.math.BigDecimal as JBigDecimal
import java.time.OffsetDateTime
import scala.annotation.meta.field
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

// Vastausluokat käyttävät Optionin sijaan nullattavia Java-tyyppejä, jotta springdoc osaa
// kuvata kentät oikein Swaggerissa. Null-arvoiset kentät jätetään pois JSONista (NON_NULL),
// ja kenttien järjestys on kiinnitetty @JsonPropertyOrder-annotaatiolla.

@JsonInclude(Include.NON_NULL)
case class HakijatResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakijat: java.util.List[HakijaResponse]
)

object HakijatResponse {
  def apply(hakijat: Seq[ToisenAsteenHakija]): HakijatResponse =
    HakijatResponse(hakijat.map(HakijaResponse.apply).asJava)
}

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(
  Array(
    "hetu",
    "oppijanumero",
    "sukunimi",
    "etunimet",
    "kutsumanimi",
    "lahiosoite",
    "postinumero",
    "postitoimipaikka",
    "maa",
    "kansalaisuudet",
    "matkapuhelin",
    "muupuhelin",
    "sahkoposti",
    "kotikunta",
    "sukupuoli",
    "aidinkieli",
    "opetuskieli",
    "koulutusmarkkinointilupa",
    "kiinnostunutoppisopimuksesta",
    "huoltaja1",
    "huoltaja2",
    "hakemus",
    "sahkoisenAsioinninLupa",
    "oppivelvollisuusVoimassaAsti",
    "oikeusMaksuttomaanKoulutukseenVoimassaAsti",
    "lisakysymykset"
  )
)
case class HakijaResponse(
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty hetu: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty oppijanumero: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty sukunimi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty etunimet: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty kutsumanimi: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty lahiosoite: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty postinumero: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty postitoimipaikka: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty maa: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kansalaisuudet: java.util.List[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty matkapuhelin: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty muupuhelin: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty sahkoposti: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty kotikunta: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty sukupuoli: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty aidinkieli: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty opetuskieli: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty koulutusmarkkinointilupa: JBoolean,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty kiinnostunutoppisopimuksesta: JBoolean,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty huoltaja1: HuoltajaResponse,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty huoltaja2: HuoltajaResponse,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakemus: HakijaHakemusResponse,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty sahkoisenAsioinninLupa: JBoolean,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty oppivelvollisuusVoimassaAsti: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty oikeusMaksuttomaanKoulutukseenVoimassaAsti: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty lisakysymykset: java.util.List[LisakysymysResponse]
)

object HakijaResponse {
  def apply(h: ToisenAsteenHakija): HakijaResponse =
    HakijaResponse(
      hetu = h.hetu.orNull,
      oppijanumero = h.oppijanumero,
      sukunimi = h.sukunimi.orNull,
      etunimet = h.etunimet.orNull,
      kutsumanimi = h.kutsumanimi.orNull,
      lahiosoite = h.lahiosoite,
      postinumero = h.postinumero,
      postitoimipaikka = h.postitoimipaikka,
      maa = h.maa.orNull,
      kansalaisuudet = h.kansalaisuudet.asJava,
      matkapuhelin = h.matkapuhelin,
      muupuhelin = h.muupuhelin.orNull,
      sahkoposti = h.sahkoposti,
      kotikunta = h.kotikunta.orNull,
      sukupuoli = h.sukupuoli.orNull,
      aidinkieli = h.aidinkieli.orNull,
      opetuskieli = h.opetuskieli.orNull,
      koulutusmarkkinointilupa = h.koulutusmarkkinointilupa.map(Boolean.box).orNull,
      kiinnostunutoppisopimuksesta = h.kiinnostunutoppisopimuksesta.map(Boolean.box).orNull,
      huoltaja1 = h.huoltaja1.map(HuoltajaResponse.apply).orNull,
      huoltaja2 = h.huoltaja2.map(HuoltajaResponse.apply).orNull,
      hakemus = HakijaHakemusResponse(h.hakemus),
      sahkoisenAsioinninLupa = h.sahkoisenAsioinninLupa.map(Boolean.box).orNull,
      oppivelvollisuusVoimassaAsti = h.oppivelvollisuusVoimassaAsti.orNull,
      oikeusMaksuttomaanKoulutukseenVoimassaAsti = h.oikeusMaksuttomaanKoulutukseenVoimassaAsti.orNull,
      lisakysymykset = h.lisakysymykset.map(LisakysymysResponse.apply).asJava
    )
}

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(Array("etunimi", "sukunimi", "puhelinnumero", "sahkoposti"))
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

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(
  Array(
    "vuosi",
    "kausi",
    "hakemusnumero",
    "hakemuksenJattopaiva",
    "hakemuksenMuokkauspaiva",
    "lahtokoulu",
    "lahtokoulunnimi",
    "luokka",
    "luokkataso",
    "pohjakoulutus",
    "todistusvuosi",
    "muukoulutus",
    "julkaisulupa",
    "yhteisetaineet",
    "lukiontasapisteet",
    "yleinenkoulumenestys",
    "lisapistekoulutus",
    "painotettavataineet",
    "hakutoiveet",
    "osaaminen"
  )
)
case class HakijaHakemusResponse(
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty vuosi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty kausi: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakemusnumero: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @(JsonFormat @field)(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  @BeanProperty hakemuksenJattopaiva: OffsetDateTime,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @(JsonFormat @field)(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  @BeanProperty hakemuksenMuokkauspaiva: OffsetDateTime,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty lahtokoulu: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty lahtokoulunnimi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty luokka: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty luokkataso: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty pohjakoulutus: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty todistusvuosi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty muukoulutus: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty julkaisulupa: JBoolean,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty yhteisetaineet: JBigDecimal,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty lukiontasapisteet: JBigDecimal,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty yleinenkoulumenestys: JBigDecimal,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty lisapistekoulutus: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty painotettavataineet: JBigDecimal,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakutoiveet: java.util.List[HakijaHakutoiveResponse],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty osaaminen: OsaaminenResponse
)

object HakijaHakemusResponse {
  def apply(h: HakijaHakemus): HakijaHakemusResponse =
    HakijaHakemusResponse(
      vuosi = h.vuosi.orNull,
      kausi = h.kausi.orNull,
      hakemusnumero = h.hakemusnumero,
      hakemuksenJattopaiva = h.hakemuksenJattopaiva.orNull,
      hakemuksenMuokkauspaiva = h.hakemuksenMuokkauspaiva.orNull,
      lahtokoulu = h.lahtokoulu.orNull,
      lahtokoulunnimi = h.lahtokoulunnimi.orNull,
      luokka = h.luokka.orNull,
      luokkataso = h.luokkataso.orNull,
      pohjakoulutus = h.pohjakoulutus.orNull,
      todistusvuosi = h.todistusvuosi.orNull,
      muukoulutus = h.muukoulutus.orNull,
      julkaisulupa = h.julkaisulupa.map(Boolean.box).orNull,
      yhteisetaineet = h.yhteisetaineet.map(_.bigDecimal).orNull,
      lukiontasapisteet = h.lukiontasapisteet.map(_.bigDecimal).orNull,
      yleinenkoulumenestys = h.yleinenkoulumenestys.map(_.bigDecimal).orNull,
      lisapistekoulutus = h.lisapistekoulutus.orNull,
      painotettavataineet = h.painotettavataineet.map(_.bigDecimal).orNull,
      hakutoiveet = h.hakutoiveet.map(HakijaHakutoiveResponse.apply).asJava,
      osaaminen = OsaaminenResponse(h.osaaminen)
    )
}

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(
  Array(
    "hakukohdeOid",
    "hakujno",
    "oppilaitos",
    "opetuspiste",
    "opetuspisteennimi",
    "koulutus",
    "harkinnanvaraisuusperuste",
    "urheilijanammatillinenkoulutus",
    "yhteispisteet",
    "valinta",
    "vastaanotto",
    "lasnaolo",
    "terveys",
    "aiempiperuminen",
    "kaksoistutkinto",
    "koulutuksenKieli",
    "keskiarvo",
    "urheilijanLisakysymykset"
  )
)
case class HakijaHakutoiveResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakukohdeOid: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakujno: Int,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty oppilaitos: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty opetuspiste: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty opetuspisteennimi: KielistettyResponse,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty koulutus: KoodistoArvoResponse,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty harkinnanvaraisuusperuste: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty urheilijanammatillinenkoulutus: JBoolean,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty yhteispisteet: JBigDecimal,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valinta: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty vastaanotto: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty lasnaolo: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty terveys: JBoolean,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty aiempiperuminen: JBoolean,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty kaksoistutkinto: JBoolean,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty koulutuksenKieli: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty keskiarvo: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty urheilijanLisakysymykset: UrheilijanLisakysymyksetResponse
)

object HakijaHakutoiveResponse {
  def apply(h: HakijaHakutoive): HakijaHakutoiveResponse =
    HakijaHakutoiveResponse(
      hakukohdeOid = h.hakukohdeOid,
      hakujno = h.hakujno,
      oppilaitos = h.oppilaitos.orNull,
      opetuspiste = h.opetuspiste.orNull,
      opetuspisteennimi = h.opetuspisteennimi.map(KielistettyResponse.apply).orNull,
      koulutus = h.koulutus.map(KoodistoArvoResponse.apply).orNull,
      harkinnanvaraisuusperuste = h.harkinnanvaraisuusperuste.orNull,
      urheilijanammatillinenkoulutus = h.urheilijanammatillinenkoulutus.map(Boolean.box).orNull,
      yhteispisteet = h.yhteispisteet.map(_.bigDecimal).orNull,
      valinta = h.valinta.orNull,
      vastaanotto = h.vastaanotto.orNull,
      lasnaolo = h.lasnaolo.orNull,
      terveys = h.terveys.map(Boolean.box).orNull,
      aiempiperuminen = h.aiempiperuminen.map(Boolean.box).orNull,
      kaksoistutkinto = h.kaksoistutkinto.map(Boolean.box).orNull,
      koulutuksenKieli = h.koulutuksenKieli.orNull,
      keskiarvo = h.keskiarvo.orNull,
      urheilijanLisakysymykset = h.urheilijanLisakysymykset.map(UrheilijanLisakysymyksetResponse.apply).orNull
    )
}

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(
  Array(
    "peruskoulu",
    "keskiarvo",
    "tamakausi",
    "viimekausi",
    "toissakausi",
    "sivulaji",
    "valmennusryhma_seurajoukkue",
    "valmennusryhma_piirijoukkue",
    "valmennusryhma_maajoukkue",
    "valmentaja_nimi",
    "valmentaja_email",
    "valmentaja_puh",
    "laji",
    "liitto",
    "seura"
  )
)
case class UrheilijanLisakysymyksetResponse(
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty peruskoulu: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty keskiarvo: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty tamakausi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty viimekausi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty toissakausi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty sivulaji: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valmennusryhma_seurajoukkue: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valmennusryhma_piirijoukkue: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valmennusryhma_maajoukkue: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valmentaja_nimi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valmentaja_email: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valmentaja_puh: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty laji: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty liitto: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty seura: String
)

object UrheilijanLisakysymyksetResponse {
  def apply(u: UrheilijanLisakysymykset): UrheilijanLisakysymyksetResponse =
    UrheilijanLisakysymyksetResponse(
      peruskoulu = u.peruskoulu.orNull,
      keskiarvo = u.keskiarvo.orNull,
      tamakausi = u.tamakausi.orNull,
      viimekausi = u.viimekausi.orNull,
      toissakausi = u.toissakausi.orNull,
      sivulaji = u.sivulaji.orNull,
      valmennusryhma_seurajoukkue = u.valmennusryhma_seurajoukkue.orNull,
      valmennusryhma_piirijoukkue = u.valmennusryhma_piirijoukkue.orNull,
      valmennusryhma_maajoukkue = u.valmennusryhma_maajoukkue.orNull,
      valmentaja_nimi = u.valmentaja_nimi.orNull,
      valmentaja_email = u.valmentaja_email.orNull,
      valmentaja_puh = u.valmentaja_puh.orNull,
      laji = u.laji.orNull,
      liitto = u.liitto.orNull,
      seura = u.seura.orNull
    )
}

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(
  Array(
    "yleinen_kielitutkinto_fi",
    "valtionhallinnon_kielitutkinto_fi",
    "yleinen_kielitutkinto_sv",
    "valtionhallinnon_kielitutkinto_sv",
    "yleinen_kielitutkinto_en",
    "valtionhallinnon_kielitutkinto_en"
  )
)
case class OsaaminenResponse(
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty yleinen_kielitutkinto_fi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valtionhallinnon_kielitutkinto_fi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty yleinen_kielitutkinto_sv: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valtionhallinnon_kielitutkinto_sv: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty yleinen_kielitutkinto_en: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valtionhallinnon_kielitutkinto_en: String
)

object OsaaminenResponse {
  def apply(o: Osaaminen): OsaaminenResponse =
    OsaaminenResponse(
      yleinen_kielitutkinto_fi = o.yleinen_kielitutkinto_fi.orNull,
      valtionhallinnon_kielitutkinto_fi = o.valtionhallinnon_kielitutkinto_fi.orNull,
      yleinen_kielitutkinto_sv = o.yleinen_kielitutkinto_sv.orNull,
      valtionhallinnon_kielitutkinto_sv = o.valtionhallinnon_kielitutkinto_sv.orNull,
      yleinen_kielitutkinto_en = o.yleinen_kielitutkinto_en.orNull,
      valtionhallinnon_kielitutkinto_en = o.valtionhallinnon_kielitutkinto_en.orNull
    )
}

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(Array("kysymysid", "hakukohdeOids", "kysymystyyppi", "kysymysteksti", "vastaukset"))
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

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(Array("vastausid", "vastausteksti"))
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

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(Array("versioituUri", "koodiarvo", "koodistoUri", "koodistoVersio", "nimi"))
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
