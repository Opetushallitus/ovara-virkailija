package fi.oph.ovara.backend.valpas

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import fi.oph.ovara.backend.opiskelijavalintatieto.KielistettyResponse
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import org.slf4j.{Logger, LoggerFactory}

import java.time.{LocalDateTime, OffsetDateTime}
import scala.annotation.meta.field
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

@JsonInclude(Include.NON_ABSENT)
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
  @BeanProperty aktiivinenHaku: Option[Boolean],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty haunAlkamispaivamaara: Option[LocalDateTime],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty oppijaOid: String,
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
      postitoimipaikka = hakemus.postitoimipaikka,
      maa = KoodistoArvoResponse(hakemus.maa),
      hakuOid = hakemus.hakuOid,
      hakuNimi = KielistettyResponse(hakemus.hakuNimi),
      hakutapa = KoodistoArvoResponse(hakemus.hakutapa),
      aktiivinenHaku = hakemus.aktiivinenHaku,
      haunAlkamispaivamaara = hakemus.haunAlkamispaivamaara,
      oppijaOid = hakemus.oppijaOid,
      hakutoiveet = hakemus.hakutoiveet.map(HakutoiveResponse.apply).asJava
    )
}

@JsonInclude(Include.NON_ABSENT)
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
    description = "Oletus on KESKEN, jos vastaanottotietoa ei ole.",
    requiredMode = RequiredMode.REQUIRED,
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
    description = "Oletus on KESKEN, jos valintatietoa ei ole.",
    requiredMode = RequiredMode.REQUIRED,
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
    description = "Oletus on EI_YRHTY, jos ilmoittautumistietoa ei ole.",
    requiredMode = RequiredMode.REQUIRED,
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
  @(Schema @field)(
    requiredMode = RequiredMode.NOT_REQUIRED,
    allowableValues = Array(
      "SURE_YKS_MAT_AI",
      "SURE_EI_PAATTOTODISTUSTA",
      "ATARU_YKS_MAT_AI",
      "ATARU_ULKOMAILLA_OPISKELTU",
      "ATARU_EI_PAATTOTODISTUSTA",
      "ATARU_SOSIAALISET_SYYT",
      "ATARU_OPPIMISVAIKEUDET",
      "ATARU_KOULUTODISTUSTEN_VERTAILUVAIKEUDET",
      "ATARU_RIITTAMATON_TUTKINTOKIELEN_TAITO",
      "EI_HARKINNANVARAINEN",
      "EI_HARKINNANVARAINEN_HAKUKOHDE"
    )
  )
  @BeanProperty harkinnanvaraisuus: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty alinHyvaksyttyPistemaara: Option[BigDecimal],
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty pisteet: Option[BigDecimal],
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty varasijanumero: Option[Int]
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
      hakutoive.alinHyvaksyttyPistemaara,
      hakutoive.pisteet,
      hakutoive.varasijanumero
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
  @BeanProperty nimi: KielistettyResponse
)

object KoodistoArvoResponse {
  def apply(koodistoArvo: KoodistoArvo): KoodistoArvoResponse = {

    new KoodistoArvoResponse(
      versioituUri = koodistoArvo.versioituUri,
      koodiarvo = koodistoArvo.koodiarvo,
      koodistoUri = koodistoArvo.koodistoUri,
      koodistoVersio = koodistoArvo.koodistoVersio,
      nimi = KielistettyResponse(koodistoArvo.nimi)
    )
  }
}
