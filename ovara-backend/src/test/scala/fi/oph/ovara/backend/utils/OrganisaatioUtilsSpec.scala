package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*
import org.scalatest.*
import org.scalatest.flatspec.*

class OrganisaatioUtilsSpec extends AnyFlatSpec {
  "recursiveListParentsAndSelf" should "return list of only self oid for the only org in the vector" in {
    val orgs = Vector(
      OrganisaatioParentChild(
        "1.2.246.562.10.1064574979797",
        "1.2.246.562.10.1064574979797",
        Organisaatio(
          "1.2.246.562.10.1064574979797",
          Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
          List("02")
        )
      )
    )

    assert(
      OrganisaatioUtils.recursiveListParentsAndSelf(
        "1.2.246.562.10.1064574979797",
        orgs
      ) == List(
        Organisaatio(
          "1.2.246.562.10.1064574979797",
          Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
          List("02")
        )
      )
    )
  }

  it should "return koulutustoimija parent oid and oppilaitos oid for oppilaitos org" in {
    val orgs = Vector(
      OrganisaatioParentChild(
        "1.2.246.562.10.1064574979797",
        "1.2.246.562.10.1064574979797",
        Organisaatio(
          "1.2.246.562.10.1064574979797",
          Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
          List("02")
        )
      ),
      OrganisaatioParentChild(
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.1064574979797",
        Organisaatio(
          "1.2.246.562.10.1064574979797",
          Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
          List("02")
        )
      ),
      OrganisaatioParentChild(
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.1064574979696",
        Organisaatio(
          "1.2.246.562.10.1064574979696",
          Map(En -> "Oppilaitos 2 en", Fi -> "Oppilaitos 2 fi", Sv -> "Oppilaitos 2 sv"),
          List("02")
        )
      ),
      OrganisaatioParentChild(
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.10645749713",
        Organisaatio(
          "1.2.246.562.10.10645749713",
          Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
          List("01")
        )
      )
    )

    assert(
      OrganisaatioUtils.recursiveListParentsAndSelf("1.2.246.562.10.1064574979797", orgs) ==
        List(
          Organisaatio(
            "1.2.246.562.10.1064574979797",
            Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
            List("02")
          ),
          Organisaatio(
            "1.2.246.562.10.1064574979797",
            Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
            List("02")
          ),
          Organisaatio(
            "1.2.246.562.10.10645749713",
            Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
            List("01")
          )
        )
    )
  }

  it should "list koulutustoimija, oppilaitos and toimipiste as parents for toimipiste" in {
    val orgs = Vector(
      OrganisaatioParentChild(
        "1.2.246.562.10.1064574979797",
        "1.2.246.562.10.1064574979856",
        Organisaatio(
          "1.2.246.562.10.1064574979856",
          Map(En -> "Toimipiste en", Fi -> "Toimipiste fi", Sv -> "Toimipiste sv"),
          List("03")
        )
      ),
      OrganisaatioParentChild(
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.1064574979696",
        Organisaatio(
          "1.2.246.562.10.1064574979696",
          Map(En -> "Oppilaitos 2 en", Fi -> "Oppilaitos 2 fi", Sv -> "Oppilaitos 2 sv"),
          List("02")
        )
      ),
      OrganisaatioParentChild(
        "1.2.246.562.10.1064574979797",
        "1.2.246.562.10.1064574979797",
        Organisaatio(
          "1.2.246.562.10.1064574979797",
          Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
          List("02")
        )
      ),
      OrganisaatioParentChild(
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.1064574979797",
        Organisaatio(
          "1.2.246.562.10.1064574979797",
          Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
          List("02")
        )
      ),
      OrganisaatioParentChild(
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.10645749713",
        Organisaatio(
          "1.2.246.562.10.10645749713",
          Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
          List("01")
        )
      )
    )

    assert(
      OrganisaatioUtils.recursiveListParentsAndSelf(
        "1.2.246.562.10.1064574979856",
        orgs
      ) ==
        List(
          Organisaatio(
            "1.2.246.562.10.1064574979856",
            Map(En -> "Toimipiste en", Fi -> "Toimipiste fi", Sv -> "Toimipiste sv"),
            List("03")
          ),
          Organisaatio(
            "1.2.246.562.10.1064574979797",
            Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
            List("02")
          ),
          Organisaatio(
            "1.2.246.562.10.1064574979797",
            Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
            List("02")
          ),
          Organisaatio(
            "1.2.246.562.10.10645749713",
            Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
            List("01")
          )
        )
    )
  }

  it should "list koulutustoimija and oppilaitos as parents for oppilaitos when koulutustoimija is earlier in the list" in {
    val orgs = Vector(
      OrganisaatioParentChild(
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.10645749713",
        Organisaatio(
          "1.2.246.562.10.10645749713",
          Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
          List("01")
        )
      ),
      OrganisaatioParentChild(
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.10645749797",
        Organisaatio(
          "1.2.246.562.10.10645749797",
          Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
          List("02")
        )
      )
    )

    assert(
      OrganisaatioUtils.recursiveListParentsAndSelf(
        "1.2.246.562.10.10645749797",
        orgs
      ) ==
        List(
          Organisaatio(
            "1.2.246.562.10.10645749797",
            Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
            List("02")
          ),
          Organisaatio(
            "1.2.246.562.10.10645749713",
            Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
            List("01")
          )
        )
    )
  }

  "mapToParent" should "return two hakukohde mapped to koulutustoimija" in {
    val orgs = Vector(
      OrganisaatioParentChild(
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.10645749713",
        Organisaatio(
          "1.2.246.562.10.10645749713",
          Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
          List("01")
        )
      )
    )

    val kth = KoulutuksetToteutuksetHakukohteetResult(
      Map(En -> "hakukohde 1 en", Fi -> "hakukohde 1 fi", Sv -> "hakukohde 1 sv"),
      "1.2.246.562.20.00000000000000041885",
      Some("julkaistu"),
      Some("arkistoitu"),
      Some("arkistoitu"),
      Some(18),
      Some(true),
      Some(true),
      Some(false),
      Some("1.2.246.562.10.10645749713"),
      Some("1.2.246.562.10.10645749713"),
      Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
      List("01")
    )

    val kth2 = kth.copy(
      hakukohdeNimi = Map(En -> "hakukohde 2 en", Fi -> "hakukohde 2 fi", Sv -> "hakukohde 2 sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000041886"
    )

    assert(
      OrganisaatioUtils.mapOrganisaationHakukohteetToParent(
        orgs,
        Map(
          Some("1.2.246.562.10.10645749713") -> Vector(kth, kth2)
        )
      ) == List(
        (
          Organisaatio(
            "1.2.246.562.10.10645749713",
            Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
            List("01")
          ),
          OrganisaationKoulutuksetToteutuksetHakukohteet(
            Some(
              Organisaatio(
                "1.2.246.562.10.10645749713",
                Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
                List("01")
              )
            ),
            Vector(kth, kth2)
          )
        )
      )
    )
  }

  it should "return two hakukohde mapped to oppilaitos with koulutustoimija as the parent" in {
    val orgs = Vector(
      OrganisaatioParentChild(
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.10645749713",
        Organisaatio(
          "1.2.246.562.10.10645749713",
          Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
          List("01")
        )
      ),
      OrganisaatioParentChild(
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.10645749797",
        Organisaatio(
          "1.2.246.562.10.10645749797",
          Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
          List("02")
        )
      )
    )

    val kth = KoulutuksetToteutuksetHakukohteetResult(
      Map(En -> "hakukohde 1 en", Fi -> "hakukohde 1 fi", Sv -> "hakukohde 1 sv"),
      "1.2.246.562.20.00000000000000041885",
      Some("julkaistu"),
      Some("arkistoitu"),
      Some("arkistoitu"),
      Some(18),
      Some(true),
      Some(true),
      Some(false),
      Some("1.2.246.562.10.10645749797"),
      Some("1.2.246.562.10.10645749797"),
      Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
      List("02")
    )

    val kth2 = kth.copy(
      hakukohdeNimi = Map(En -> "hakukohde 2 en", Fi -> "hakukohde 2 fi", Sv -> "hakukohde 2 sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000041886"
    )

    assert(
      OrganisaatioUtils.mapOrganisaationHakukohteetToParent(
        orgs,
        Map(
          Some("1.2.246.562.10.10645749797") -> Vector(kth, kth2)
        )
      ) == List(
        (
          Organisaatio(
            "1.2.246.562.10.10645749713",
            Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
            List("01")
          ),
          OrganisaationKoulutuksetToteutuksetHakukohteet(
            Some(
              Organisaatio(
                "1.2.246.562.10.10645749797",
                Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
                List("02")
              )
            ),
            Vector(kth, kth2)
          )
        )
      )
    )
  }

  it should "return" in {
    val orgs =
      Vector(
        OrganisaatioParentChild(
          "1.2.246.562.10.1064574979797",
          "1.2.246.562.10.1064574979856",
          Organisaatio(
            "1.2.246.562.10.1064574979856",
            Map(En -> "Toimipiste en", Fi -> "Toimipiste fi", Sv -> "Toimipiste sv"),
            List("03")
          )
        ),
        OrganisaatioParentChild(
          "1.2.246.562.10.10645749713",
          "1.2.246.562.10.1064574979696",
          Organisaatio(
            "1.2.246.562.10.1064574979696",
            Map(En -> "Oppilaitos 2 en", Fi -> "Oppilaitos 2 fi", Sv -> "Oppilaitos 2 sv"),
            List("02")
          )
        ),
        OrganisaatioParentChild(
          "1.2.246.562.10.1064574979797",
          "1.2.246.562.10.1064574979797",
          Organisaatio(
            "1.2.246.562.10.1064574979797",
            Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
            List("02")
          )
        ),
        OrganisaatioParentChild(
          "1.2.246.562.10.10645749713",
          "1.2.246.562.10.1064574979797",
          Organisaatio(
            "1.2.246.562.10.1064574979797",
            Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
            List("02")
          )
        ),
        OrganisaatioParentChild(
          "1.2.246.562.10.10645749713",
          "1.2.246.562.10.10645749713",
          Organisaatio(
            "1.2.246.562.10.10645749713",
            Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
            List("01")
          )
        )
      )

    val kth = KoulutuksetToteutuksetHakukohteetResult(
      Map(En -> "hakukohde 1 en", Fi -> "hakukohde 1 fi", Sv -> "hakukohde 1 sv"),
      "1.2.246.562.20.00000000000000041885",
      Some("julkaistu"),
      Some("arkistoitu"),
      Some("arkistoitu"),
      Some(18),
      Some(true),
      Some(true),
      Some(false),
      Some("1.2.246.562.10.10645749797"),
      Some("1.2.246.562.10.10645749797"),
      Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
      List("02")
    )

    val kth2 = kth.copy(
      hakukohdeNimi = Map(En -> "hakukohde 2 en", Fi -> "hakukohde 2 fi", Sv -> "hakukohde 2 sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000041886"
    )

    val kth3 = kth.copy(
      hakukohdeNimi = Map(En -> "hakukohde 3 en", Fi -> "hakukohde 3 fi", Sv -> "hakukohde 3 sv"),
      hakukohdeOid = "1.2.246.562.20.00000000000000050000",
      jarjestyspaikka_oid = Some("1.2.246.562.10.1064574979856"),
      organisaatio_oid = Some("1.2.246.562.10.1064574979856"),
      organisaatio_nimi = Map(En -> "Toimipiste en", Fi -> "Toimipiste fi", Sv -> "Toimipiste sv"),
      organisaatiotyypit = List("03")
    )

    val kth4 = kth3.copy(
      hakukohdeNimi = Map(En -> "hakukohde 4 en", Fi -> "hakukohde 4 fi", Sv -> "hakukohde 4 sv"),
      hakukohdeOid = "1.2.246.562.20.000000000000000500001"
    )

    assert(
      OrganisaatioUtils.mapOrganisaationHakukohteetToParent(
        orgs,
        Map(
          Some("1.2.246.562.10.1064574979797")   -> Vector(kth, kth2),
          Some("1.2.246.562.10.1064574979856") -> Vector(kth3, kth4)
        )
      ) == List(
        (
          Organisaatio(
            "1.2.246.562.10.10645749713",
            Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
            List("01")
          ),
          OrganisaationKoulutuksetToteutuksetHakukohteet(
            Some(
              Organisaatio(
                "1.2.246.562.10.1064574979797",
                Map(En -> "Oppilaitos en", Fi -> "Oppilaitos fi", Sv -> "Oppilaitos sv"),
                List("02")
              )
            ),
            Vector(kth, kth2)
          )
        ),
        (
          Organisaatio(
            "1.2.246.562.10.10645749713",
            Map(En -> "Koulutustoimija en", Fi -> "Koulutustoimija fi", Sv -> "Koulutustoimija sv"),
            List("01")
          ),
          OrganisaationKoulutuksetToteutuksetHakukohteet(
            Some(
              Organisaatio(
                "1.2.246.562.10.1064574979856",
                Map(En -> "Toimipiste en", Fi -> "Toimipiste fi", Sv -> "Toimipiste sv"),
                List("03")
              )
            ),
            Vector(kth3, kth4)
          )
        )
      )
    )
  }
}
