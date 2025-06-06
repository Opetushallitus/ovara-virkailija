package fi.oph.ovara.backend

import fi.oph.ovara.backend.domain.{Fi, Organisaatio, OrganisaatioHierarkia, User}
import fi.oph.ovara.backend.repository.CommonRepository
import fi.oph.ovara.backend.service.{CommonService, UserService}
import org.mockito.Mockito.{mock, *}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CommonServiceTest extends AnyFlatSpec with Matchers {

  val mockCommonRepository: CommonRepository = mock(classOf[CommonRepository])
  val mockUserService: UserService = mock(classOf[UserService])
  val commonService = new CommonService(mockCommonRepository, mockUserService)

  "selectToimipisteDescendants" should "return empty descendant list when no organisations are provided" in {
    val toimipisteet = List()

    val result = commonService.getToimipistehierarkiat(toimipisteet)
    result shouldBe empty
    verifyNoInteractions(mockCommonRepository)
  }

  "selectOppilaitosDescendants" should "return empty descendant list when no organisations are provided" in {
    val oppilaitokset = List()

    val result = commonService.getOppilaitoshierarkiat(oppilaitokset)
    result shouldBe empty
    verifyNoInteractions(mockCommonRepository)
  }

  "selectKoulutustoimijaDescendants" should "return empty descendant list when no organisations are provided" in {
    val koulutustoimijat = List()

    val result = commonService.getKoulutustoimijahierarkia(koulutustoimijat)
    result shouldBe empty
    verifyNoInteractions(mockCommonRepository)
  }

  "getAllowedOrgOidsFromOrgSelection" should "return descendants of kayttooikeusOrganisaatioOids when all organization inputs are empty" in {
    val kayttooikeusOrganisaatioOids = List("1.2.246.562.10.12345678901")
    val oppilaitosOids = List()
    val toimipisteOids = List()
    val koulutustoimijaOid = None

    val mockOrganisaatioHierarkia = OrganisaatioHierarkia(
      organisaatio_oid = "1.2.246.562.10.12345678901",
      organisaatio_nimi = Map(Fi -> "Mock Organisaatio"),
      organisaatiotyypit = List("type1", "type2"),
      oppilaitostyyppi = Some("mockType"),
      tila = "AKTIIVINEN",
      parent_oids = List("1.2.246.562.10.98765432101"),
      koulutustoimijaParent = Some(Organisaatio(
        organisaatio_oid = "1.2.246.562.10.98765432101",
        organisaatio_nimi = Map(Fi -> "Parent Organisaatio"),
        organisaatiotyypit = List("parentType")
      )),
      children = List(
        OrganisaatioHierarkia(
          organisaatio_oid = "1.2.246.562.10.12345678902",
          organisaatio_nimi = Map(Fi -> "Child Organisaatio"),
          organisaatiotyypit = List("childType"),
          oppilaitostyyppi = None,
          tila = "AKTIIVINEN",
          parent_oids = List("1.2.246.562.10.12345678901"),
          koulutustoimijaParent = None,
          children = List()
        )
      )
    )

    val mockOrganisaatioHierarkiaList = List(mockOrganisaatioHierarkia)
    val spyCommonService = spy(commonService)
    when(spyCommonService.getOrganisaatioHierarkiatWithUserRights).thenReturn(Right(mockOrganisaatioHierarkiaList))

    val result = spyCommonService.getAllowedOrgOidsFromOrgSelection(
      kayttooikeusOrganisaatioOids,
      oppilaitosOids,
      toimipisteOids,
      koulutustoimijaOid
    )

    result shouldBe List("1.2.246.562.10.12345678902")
  }

}
