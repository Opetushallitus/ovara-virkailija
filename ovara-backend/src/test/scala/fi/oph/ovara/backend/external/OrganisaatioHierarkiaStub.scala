package fi.oph.ovara.backend.external

import fi.oph.ovara.backend.service.CommonService
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.invocation.InvocationOnMock

/**
 * Apuri external-rajapintojen testeille. Organisaatiohierarkia luetaan pub-skeemasta,
 *  jota näiden testien H2-kanta ei sisällä -- testit luovat vain gen-taulut. Siksi
 *  `CommonService` mockataan (`@MockitoBean`) ja hierarkian laajennus stubataan tässä.
 *
 *  Oletusstubi palauttaa syötteen sellaisenaan, jolloin olemassa olevat testit mittaavat
 *  edelleen pelkkää suodatuslogiikkaa. `withOrganisaatioHierarkia` antaa laajennukselle
 *  oikean lapsiorganisaatiojoukon, jolloin voidaan varmistaa että koulutustoimija- tai
 *  oppilaitostason valinta ja käyttöoikeus osuvat alempana olevaan järjestyspaikkaan.
 *
 *  Stubit asetetaan `doAnswer(...).when(mock)`-muodossa eikä `when(mock.method(...))`:lla,
 *  koska jälkimmäinen kutsuisi metodia mockilla ja siten edellisen testin vastauksen
 *  null-argumentilla.
 */
trait OrganisaatioHierarkiaStub {
  val commonService: CommonService

  def stubOrganisaatioHierarkiaAsIdentity(): Unit =
    stubOrganisaatioHierarkia(oids => oids)

  /** Laajentaa jokaisen annetun organisaation itsekseen + `lapset`-listalla. */
  def withOrganisaatioHierarkia(lapset: Map[String, List[String]]): Unit =
    stubOrganisaatioHierarkia(oids => (oids ++ oids.flatMap(oid => lapset.getOrElse(oid, Nil))).distinct)

  private def stubOrganisaatioHierarkia(expand: List[String] => List[String]): Unit =
    doAnswer((invocation: InvocationOnMock) => expand(invocation.getArgument[List[String]](0)))
      .when(commonService)
      .getOrganisaatioidenJaLastenOidit(any())
}
