package fi.oph.ovara.backend

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

}
