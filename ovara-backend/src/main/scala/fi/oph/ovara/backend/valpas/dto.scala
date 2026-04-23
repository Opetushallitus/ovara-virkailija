package fi.oph.ovara.backend.valpas

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import fi.oph.ovara.backend.opiskelijavalintatieto.KielistettyResponse
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import org.slf4j.{Logger, LoggerFactory}

import java.time.{LocalDate, OffsetDateTime}
import scala.annotation.meta.field
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

val LOG: Logger = LoggerFactory.getLogger("Valpas DTO")

case class HakemusResponse(
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakemusOid: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakemusUrl: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakemuksenMuokkauksenAikaleima: Option[OffsetDateTime],
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty email: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty matkapuhelin: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty lahiosoite: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty postinumero: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty postitoimipaikka: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty maa: KoodistoArvoResponse,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakuOid: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakuNimi: KielistettyResponse,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakutapa: KoodistoArvoResponse,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakutyyppi: KoodistoArvoResponse,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty aktiivinenHaku: Option[Boolean],
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty haunAlkamispaivamaara: Option[LocalDate],
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty oppijaOid: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty huoltajanNimi: Option[String],
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty huoltajanPuhelinnumero: Option[String],
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty huoltajanSahkoposti: Option[String],
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakutoiveet: java.util.List[HakutoiveResponse]
)

object HakemusResponse {
  def apply(hakemus: Hakemus, virkailijaUrl: String): HakemusResponse =
    new HakemusResponse(
      hakemusOid = hakemus.hakemusOid,
      hakemusUrl = s"$virkailijaUrl/lomake-editori/applications/search?term=${hakemus.hakemusOid}",
      hakemuksenMuokkauksenAikaleima = hakemus.hakemuksenMuokkauksenAikaleima,
      email = hakemus.email,
      matkapuhelin = hakemus.matkapuhelin,
      lahiosoite = hakemus.lahiosoite,
      postinumero = hakemus.postinumero,
      postitoimipaikka = hakemus.postinumero,
      maa = KoodistoArvoResponse(hakemus.maa),
      hakuOid = hakemus.hakuOid,
      hakuNimi = KielistettyResponse(hakemus.hakuNimi),
      hakutapa = KoodistoArvoResponse(hakemus.hakutapa),
      hakutyyppi = KoodistoArvoResponse(hakemus.hakutyyppi),
      aktiivinenHaku = hakemus.aktiivinenHaku,
      haunAlkamispaivamaara = hakemus.haunAlkamispaivamaara,
      oppijaOid = hakemus.oppijaOid,
      huoltajanNimi = hakemus.huoltajanNimi,
      huoltajanPuhelinnumero = hakemus.huoltajanPuhelinnumero,
      huoltajanSahkoposti = hakemus.huoltajanSahkoposti,
      hakutoiveet = hakemus.hakutoiveet.map(HakutoiveResponse.apply).asJava
    )
}

case class HakutoiveResponse(
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakukohdeOid: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakukohdeNimi: KielistettyResponse,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakutoivenumero: Int,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakukohdeOrganisaatio: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty organisaatioNimi: KielistettyResponse,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty koulutusOid: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty koulutusNimi: KielistettyResponse,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakukohdeKoulutuskoodi: java.util.List[KoodistoArvoResponse],
    @(Schema @field)(
      requiredMode = RequiredMode.NOT_REQUIRED,
      allowableValues = Array(
        "KESKEN",
        "VASTAANOTTANUT_SITOVASTI",
        "EI_VASTAANOTETTU_MAARA_AIKANA",
        "PERUNUT",
        "PERUUTETTU",
        "OTTANUT_VASTAAN_TOISEN_PAIKAN",
        "EHDOLLISESTI_VASTAANOTTANUT"
      )
    )
    @BeanProperty vastaanottotieto: String,
    @(Schema @field)(
      requiredMode = RequiredMode.NOT_REQUIRED,
      allowableValues = Array(
        "HYVAKSYTTY",
        "VARASIJALTA_HYVAKSYTTY",
        "HARKINNANVARAISESTI_HYVAKSYTTY",
        "VARALLA",
        "HYLATTY",
        "PERUUNTUNUT",
        "PERUNUT",
        "PERUUTETTU",
        "KESKEN"
      )
    )
    @BeanProperty valintatila: String,
    @(Schema @field)(
      requiredMode = RequiredMode.NOT_REQUIRED,
      allowableValues = Array(
        "EI_TEHTY",
        "LASNA_KOKO_LUKUVUOSI",
        "POISSA_KOKO_LUKUVUOSI",
        "EI_ILMOITTAUTUNUT",
        "LASNA_SYKSY",
        "POISSA_SYKSY",
        "LASNA",
        "POISSA"
      )
    )
    @BeanProperty ilmoittautumistila: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty harkinnanvaraisuus: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty paasykoe: Option[PaasykoeResponse],
    @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
    @BeanProperty kielikoe: Option[PaasykoeResponse],
    @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
    @BeanProperty lisanaytto: Option[PaasykoeResponse],
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty liitteetTarkastettu: Boolean,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty valintakoe: java.util.List[ValintakoeResponse],
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty alinHyvaksyttyPistemaara: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty alinValintaPistemaara: Int,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty pisteet: Int,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty varasijanumero: Int
)

object HakutoiveResponse {
  def apply(hakutoive: Hakutoive): HakutoiveResponse =
    HakutoiveResponse(
      hakutoive.hakukohdeOid,
      KielistettyResponse(hakutoive.hakukohdeNimi),
      hakutoive.hakutoivenumero,
      hakutoive.hakukohdeOrganisaatio,
      KielistettyResponse(hakutoive.organisaatioNimi),
      hakutoive.koulutusOid,
      KielistettyResponse(hakutoive.koulutusNimi),
      hakutoive.hakukohdeKoulutuskoodi.map(KoodistoArvoResponse.apply).asJava,
      hakutoive.vastaanottotieto,
      hakutoive.valintatila,
      hakutoive.ilmoittautumistila,
      hakutoive.harkinnanvaraisuus,
      hakutoive.paasykoe.map(PaasykoeResponse.apply),
      hakutoive.kielikoe.map(PaasykoeResponse.apply),
      hakutoive.lisanaytto.map(PaasykoeResponse.apply),
      hakutoive.liitteetTarkastettu,
      hakutoive.valintakoe.map(ValintakoeResponse.apply).asJava,
      hakutoive.alinHyvaksyttyPistemaara,
      hakutoive.alinValintaPistemaara,
      hakutoive.pisteet,
      hakutoive.varasijanumero
    )
}

case class PaasykoeResponse(
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty tunniste: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty arvo: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty osallistuminen: String
)

object PaasykoeResponse {
  def apply(paasykoe: Paasykoe): PaasykoeResponse =
    PaasykoeResponse(
      tunniste = paasykoe.tunniste,
      arvo = paasykoe.arvo,
      osallistuminen = paasykoe.osallistuminen
    )
}

case class ValintakoeResponse(
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty osallistuminen: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty laskentatila: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty valintakoeOid: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty valintakoeTunniste: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty nimi: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty valinnanVaiheOid: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty valinnanVaiheJarjestysluku: Int,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty arvo: String
)

object ValintakoeResponse {
  def apply(valintakoe: Valintakoe) =
    new ValintakoeResponse(
      osallistuminen = valintakoe.osallistuminen,
      laskentatila = valintakoe.laskentatila,
      valintakoeOid = valintakoe.valintakoeOid,
      valintakoeTunniste = valintakoe.valintakoeTunniste,
      nimi = valintakoe.nimi,
      valinnanVaiheOid = valintakoe.valinnanVaiheOid,
      valinnanVaiheJarjestysluku = valintakoe.valinnanVaiheJarjestysluku,
      arvo = valintakoe.arvo
    )
}

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
    @BeanProperty nimi: KielistettyResponse,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty
    @JsonInclude(Include.NON_EMPTY)
    lyhytNimi: KielistettyResponse
)

object KoodistoArvoResponse {
  def apply(koodistoArvo: KoodistoArvo): KoodistoArvoResponse = {
    LOG.info(s"ASDF lyhyt nimi: ${koodistoArvo.lyhytNimi} (${new ObjectMapper().writeValueAsString(koodistoArvo.lyhytNimi)})")

    new KoodistoArvoResponse(
      versioituUri = koodistoArvo.versioituUri,
      koodiarvo = koodistoArvo.koodiarvo,
      koodistoUri = koodistoArvo.koodistoUri,
      koodistoVersio = koodistoArvo.koodistoVersio,
      nimi = KielistettyResponse(koodistoArvo.nimi),
      lyhytNimi = if (koodistoArvo.lyhytNimi.isEmpty) null else KielistettyResponse(koodistoArvo.lyhytNimi)
    )
  }
}
