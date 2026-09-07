package fi.oph.ovara.backend.external

import fi.oph.ovara.backend.service.CommonService
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.invocation.InvocationOnMock

/**
 * Apuri external-rajapintojen testeille. Hakukohderyhmän laajennus hakukohde-oideiksi luetaan
 *  pub-skeemasta, jota näiden testien H2-kanta ei sisällä -- testit luovat vain gen-taulut.
 *  Siksi `CommonService` mockataan (`@MockitoBean`) ja laajennus stubataan tässä, samaan tapaan
 *  kuin organisaatiohierarkia [[OrganisaatioHierarkiaStub]]-traitissa.
 *
 *  `withHakukohderyhmat` on argumenttitietoinen, jotta sama testi voi antaa eri laajennuksen
 *  käyttäjän oikeusryhmälle ja pyynnön rajaimena olevalle ryhmälle. `withHakukohderyhmatPerHaku`
 *  erottelee lisäksi haun, jolla voi varmistaa että laajennus tehdään pyydetyllä hakuOidilla.
 *
 *  HUOM: mockattu `CommonService` palauttaa stubbaamattomalta metodilta `null` eikä `Nil`, joten
 *  jokainen testi jossa käyttäjällä on ryhmäoikeuksia on stubattava -- muuten NPE kääriytyy
 *  muotoon `Left("virhe.tietokanta")` ja testi hajoaa harhaanjohtavasti.
 */
trait HakukohderyhmaStub {
  val commonService: CommonService

  /** Sama laajennus kaikille ryhmille ja hauille. */
  def stubHakukohderyhma(hakukohdeOids: List[String]): Unit =
    stubHakukohderyhmat((_, _) => hakukohdeOids)

  /** Laajennus ryhmäkohtaisesti; tuntematon ryhmä laajenee tyhjäksi. */
  def withHakukohderyhmat(hakukohteetRyhmalla: Map[String, List[String]]): Unit =
    stubHakukohderyhmat((ryhma, _) => hakukohteetRyhmalla.getOrElse(ryhma, Nil))

  /** Laajennus (ryhmä, haku) -parin mukaan; tuntematon pari laajenee tyhjäksi. */
  def withHakukohderyhmatPerHaku(hakukohteetRyhmallaJaHaulla: Map[(String, String), List[String]]): Unit =
    stubHakukohderyhmat((ryhma, haku) => hakukohteetRyhmallaJaHaulla.getOrElse((ryhma, haku), Nil))

  private def stubHakukohderyhmat(expand: (String, String) => List[String]): Unit =
    doAnswer((invocation: InvocationOnMock) =>
      expand(invocation.getArgument[String](0), invocation.getArgument[String](1))
    ).when(commonService)
      .getHakukohderyhmanHakukohdeOids(any[String](), any[String]())
}
