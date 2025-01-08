package fi.oph.ovara.backend.utils

import fi.oph.ovara.backend.domain.*
import org.scalatest.*
import org.scalatest.flatspec.*

class OrganisaatioUtilsSpec extends AnyFlatSpec {
  "getDescendantOids" should "return list of organisaatio descendants for organisaatio without children" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        List()
      )

    assert(OrganisaatioUtils.getDescendantOids(hierarkia) == List("1.2.246.562.10.41253773158"))
  }

  it should "return list of organisaatio descendants for organisaatio with one child" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.93483820481",
            Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
            List("02"),
            List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
            List()
          )
        )
      )

    assert(
      OrganisaatioUtils.getDescendantOids(hierarkia) == List("1.2.246.562.10.41253773158", "1.2.246.562.10.93483820481")
    )
  }

  it should "return list of organisaatio descendants for organisaatio with two children" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.93483820481",
            Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
            List("02"),
            List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
            List()
          ),
          OrganisaatioHierarkia(
            "1.2.246.562.10.95915936017",
            Map(
              En -> "Kemi-Tornion ammattikorkeakoulu",
              Fi -> "Kemi-Tornion ammattikorkeakoulu",
              Sv -> "Kemi-Tornion ammattikorkeakoulu"
            ),
            List("02"),
            List("1.2.246.562.10.95915936017", "1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
            List()
          )
        )
      )

    assert(
      OrganisaatioUtils.getDescendantOids(hierarkia) == List(
        "1.2.246.562.10.41253773158",
        "1.2.246.562.10.93483820481",
        "1.2.246.562.10.95915936017"
      )
    )
  }

  it should "return list of organisaatio descendants for organisaatio with two children and grandchildren and grandgrandchildren" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.93483820481",
            Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
            List("02"),
            List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.10645749713",
                Map(
                  En -> "Pop & Jazz Konservatorio Lappia",
                  Fi -> "Pop & Jazz Konservatorio Lappia",
                  Sv -> "Pop & Jazz Konservatorio Lappia"
                ),
                List("03"),
                List(
                  "1.2.246.562.10.10645749713",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158",
                  "1.2.246.562.10.93483820481"
                ),
                List(
                  OrganisaatioHierarkia(
                    "1.2.246.562.10.1064574971333",
                    Map(
                      En -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Fi -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Sv -> "Pop & Jazz Konservatorio Lappia alitoimipiste"
                    ),
                    List("03"),
                    List(
                      "1.2.246.562.10.1064574971333",
                      "1.2.246.562.10.10645749713",
                      "1.2.246.562.10.00000000001",
                      "1.2.246.562.10.41253773158",
                      "1.2.246.562.10.93483820481"
                    ),
                    List()
                  )
                )
              )
            )
          ),
          OrganisaatioHierarkia(
            "1.2.246.562.10.95915936017",
            Map(
              En -> "Kemi-Tornion ammattikorkeakoulu",
              Fi -> "Kemi-Tornion ammattikorkeakoulu",
              Sv -> "Kemi-Tornion ammattikorkeakoulu"
            ),
            List("02"),
            List("1.2.246.562.10.95915936017", "1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
            List()
          )
        )
      )

    assert(
      OrganisaatioUtils.getDescendantOids(hierarkia) == List(
        "1.2.246.562.10.41253773158",
        "1.2.246.562.10.93483820481",
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.1064574971333",
        "1.2.246.562.10.95915936017"
      )
    )
  }

  "mapOrganisaationHakukohteetToParent2" should "return koulutustoimijan hakukohteet for hierarkia with only koulutustoimija" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        List()
      )

    val kth = OrganisaationKoulutusToteutusHakukohde(
      Some("1.2.246.562.10.41253773158"),
      KoulutusToteutusHakukohdeResult(
        Map(En -> "hakukohde 1 en", Fi -> "hakukohde 1 fi", Sv -> "hakukohde 1 sv"),
        "1.2.246.562.20.00000000000000041885",
        Some("julkaistu"),
        Some("arkistoitu"),
        Some("arkistoitu"),
        Some(18),
        Some(true),
        Some(true),
        Some(false)
      )
    )

    val kth2 = kth.copy(koulutusToteutusHakukohde =
      kth._2.copy(
        hakukohdeNimi = Map(En -> "hakukohde 2 en", Fi -> "hakukohde 2 fi", Sv -> "hakukohde 2 sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000041886"
      )
    )

    assert(
      OrganisaatioUtils.mapOrganisaationHakukohteetToParent(
        hierarkia,
        Map(
          Some("1.2.246.562.10.41253773158") -> Vector(kth, kth2)
        )
      ) ==
        OrganisaatioHierarkiaWithHakukohteet(
          "1.2.246.562.10.41253773158",
          Map(
            En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
          ),
          List("01"),
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          List(),
          List(kth, kth2)
        )
    )
  }

  it should "return koulutustoimijan oppilaitos with hakukohteet for one hierarkia" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.10645749713",
            Map(
              En -> "Pop & Jazz Konservatorio Lappia",
              Fi -> "Pop & Jazz Konservatorio Lappia",
              Sv -> "Pop & Jazz Konservatorio Lappia"
            ),
            List("02"),
            List(
              "1.2.246.562.10.10645749713",
              "1.2.246.562.10.00000000001",
              "1.2.246.562.10.41253773158",
              "1.2.246.562.10.93483820481"
            ),
            List()
          )
        )
      )

    val kth = OrganisaationKoulutusToteutusHakukohde(
      Some("1.2.246.562.10.10645749713"),
      KoulutusToteutusHakukohdeResult(
        Map(En -> "hakukohde 1 en", Fi -> "hakukohde 1 fi", Sv -> "hakukohde 1 sv"),
        "1.2.246.562.20.00000000000000041885",
        Some("julkaistu"),
        Some("arkistoitu"),
        Some("arkistoitu"),
        Some(18),
        Some(true),
        Some(true),
        Some(false)
      )
    )

    val kth2 = kth.copy(
      koulutusToteutusHakukohde = kth._2.copy(
        hakukohdeNimi = Map(En -> "hakukohde 2 en", Fi -> "hakukohde 2 fi", Sv -> "hakukohde 2 sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000041886"
      )
    )

    assert(
      OrganisaatioUtils.mapOrganisaationHakukohteetToParent(
        hierarkia,
        Map(
          Some("1.2.246.562.10.10645749713") -> Vector(kth, kth2)
        )
      ) ==
        OrganisaatioHierarkiaWithHakukohteet(
          "1.2.246.562.10.41253773158",
          Map(
            En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
          ),
          List("01"),
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          List(
            OrganisaatioHierarkiaWithHakukohteet(
              "1.2.246.562.10.10645749713",
              Map(
                En -> "Pop & Jazz Konservatorio Lappia",
                Fi -> "Pop & Jazz Konservatorio Lappia",
                Sv -> "Pop & Jazz Konservatorio Lappia"
              ),
              List("02"),
              List(
                "1.2.246.562.10.10645749713",
                "1.2.246.562.10.00000000001",
                "1.2.246.562.10.41253773158",
                "1.2.246.562.10.93483820481"
              ),
              List(),
              List(kth, kth2)
            )
          ),
          hakukohteet = List()
        )
    )
  }

  it should "return one hakukohde for alitoimipiste and two for oppilaitos in one hierarkia" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.10645749713",
            Map(
              En -> "Pop & Jazz Konservatorio Lappia",
              Fi -> "Pop & Jazz Konservatorio Lappia",
              Sv -> "Pop & Jazz Konservatorio Lappia"
            ),
            List("02"),
            List(
              "1.2.246.562.10.10645749713",
              "1.2.246.562.10.00000000001",
              "1.2.246.562.10.41253773158"
            ),
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.10645749712223",
                Map(
                  En -> "Pop & Jazz Konservatorio Lappia",
                  Fi -> "Pop & Jazz Konservatorio Lappia",
                  Sv -> "Pop & Jazz Konservatorio Lappia"
                ),
                List("03"),
                List(
                  "1.2.246.562.10.10645749712223",
                  "1.2.246.562.10.10645749713",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158"
                ),
                List(
                  OrganisaatioHierarkia(
                    "1.2.246.562.10.1064574971333",
                    Map(
                      En -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Fi -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Sv -> "Pop & Jazz Konservatorio Lappia alitoimipiste"
                    ),
                    List("03"),
                    List(
                      "1.2.246.562.10.1064574971333",
                      "1.2.246.562.10.10645749712223",
                      "1.2.246.562.10.10645749713",
                      "1.2.246.562.10.00000000001",
                      "1.2.246.562.10.41253773158"
                    ),
                    List()
                  )
                )
              )
            )
          )
        )
      )

    val kth = OrganisaationKoulutusToteutusHakukohde(
      Some("1.2.246.562.10.10645749713"),
      KoulutusToteutusHakukohdeResult(
        Map(En -> "hakukohde 1 en", Fi -> "hakukohde 1 fi", Sv -> "hakukohde 1 sv"),
        "1.2.246.562.20.00000000000000041885",
        Some("julkaistu"),
        Some("arkistoitu"),
        Some("arkistoitu"),
        Some(18),
        Some(true),
        Some(true),
        Some(false)
      )
    )

    val kth2 = kth.copy(koulutusToteutusHakukohde =
      kth._2.copy(
        hakukohdeNimi = Map(En -> "hakukohde 2 en", Fi -> "hakukohde 2 fi", Sv -> "hakukohde 2 sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000041886"
      )
    )

    val alitoimipisteKth = OrganisaationKoulutusToteutusHakukohde(
      Some("1.2.246.562.10.1064574971333"),
      KoulutusToteutusHakukohdeResult(
        Map(
          En -> "Alitoimipisteen hakukohde 1 en",
          Fi -> "Alitoimipisteen hakukohde 1 fi",
          Sv -> "Alitoimipisteen hakukohde 1 sv"
        ),
        "1.2.246.562.20.000000000000000419995",
        Some("julkaistu"),
        Some("arkistoitu"),
        Some("arkistoitu"),
        Some(18),
        Some(true),
        Some(true),
        Some(false)
      )
    )

    val ylimaarainenKth = OrganisaationKoulutusToteutusHakukohde(
      Some("1.2.246.562.10.1064574971445"),
      KoulutusToteutusHakukohdeResult(
        Map(
          En -> "Ylimääräinen hakukohde 1 en",
          Fi -> "Ylimääräinen hakukohde 1 fi",
          Sv -> "Ylimääräinen hakukohde 1 sv"
        ),
        "1.2.246.562.20.00000000000000041999100",
        Some("julkaistu"),
        Some("arkistoitu"),
        Some("arkistoitu"),
        Some(18),
        Some(true),
        Some(true),
        Some(false)
      )
    )

    assert(
      OrganisaatioUtils.mapOrganisaationHakukohteetToParent(
        hierarkia,
        Map(
          Some("1.2.246.562.10.10645749713")   -> Vector(kth, kth2),
          Some("1.2.246.562.10.1064574971333") -> Vector(alitoimipisteKth),
          Some("1.2.246.562.10.1064574971445") -> Vector(ylimaarainenKth)
        )
      ) ==
        OrganisaatioHierarkiaWithHakukohteet(
          "1.2.246.562.10.41253773158",
          Map(
            En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
          ),
          List("01"),
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          List(
            OrganisaatioHierarkiaWithHakukohteet(
              "1.2.246.562.10.10645749713",
              Map(
                En -> "Pop & Jazz Konservatorio Lappia",
                Fi -> "Pop & Jazz Konservatorio Lappia",
                Sv -> "Pop & Jazz Konservatorio Lappia"
              ),
              List("02"),
              List(
                "1.2.246.562.10.10645749713",
                "1.2.246.562.10.00000000001",
                "1.2.246.562.10.41253773158"
              ),
              List(
                OrganisaatioHierarkiaWithHakukohteet(
                  "1.2.246.562.10.10645749712223",
                  Map(
                    En -> "Pop & Jazz Konservatorio Lappia",
                    Fi -> "Pop & Jazz Konservatorio Lappia",
                    Sv -> "Pop & Jazz Konservatorio Lappia"
                  ),
                  List("03"),
                  List(
                    "1.2.246.562.10.10645749712223",
                    "1.2.246.562.10.10645749713",
                    "1.2.246.562.10.00000000001",
                    "1.2.246.562.10.41253773158"
                  ),
                  List(
                    OrganisaatioHierarkiaWithHakukohteet(
                      "1.2.246.562.10.1064574971333",
                      Map(
                        En -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                        Fi -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                        Sv -> "Pop & Jazz Konservatorio Lappia alitoimipiste"
                      ),
                      List("03"),
                      List(
                        "1.2.246.562.10.1064574971333",
                        "1.2.246.562.10.10645749712223",
                        "1.2.246.562.10.10645749713",
                        "1.2.246.562.10.00000000001",
                        "1.2.246.562.10.41253773158"
                      ),
                      List(),
                      List(alitoimipisteKth)
                    )
                  ),
                  List()
                )
              ),
              List(kth, kth2)
            )
          ),
          hakukohteet = List()
        )
    )
  }

  "mapOrganisaationHakukohteetToParents" should "return two hakukohde mapped to koulutustoimija" in {
    val hierarkia =
      List(
        OrganisaatioHierarkia(
          "1.2.246.562.10.41253773158",
          Map(
            En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
          ),
          List("01"),
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          List()
        )
      )

    val kth = OrganisaationKoulutusToteutusHakukohde(
      Some("1.2.246.562.10.41253773158"),
      KoulutusToteutusHakukohdeResult(
        Map(En -> "hakukohde 1 en", Fi -> "hakukohde 1 fi", Sv -> "hakukohde 1 sv"),
        "1.2.246.562.20.00000000000000041885",
        Some("julkaistu"),
        Some("arkistoitu"),
        Some("arkistoitu"),
        Some(18),
        Some(true),
        Some(true),
        Some(false)
      )
    )

    val kth2 = kth.copy(koulutusToteutusHakukohde =
      kth._2.copy(
        hakukohdeNimi = Map(En -> "hakukohde 2 en", Fi -> "hakukohde 2 fi", Sv -> "hakukohde 2 sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000041886"
      )
    )

    assert(
      OrganisaatioUtils.mapOrganisaationHakukohteetToParents(
        hierarkia,
        Map(
          Some("1.2.246.562.10.41253773158") -> Vector(kth, kth2)
        )
      ) == List(
        OrganisaatioHierarkiaWithHakukohteet(
          "1.2.246.562.10.41253773158",
          Map(
            En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
          ),
          List("01"),
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          List(),
          hakukohteet = List(kth, kth2)
        )
      )
    )
  }

  it should "return koulutustoimija without hakukohteet" in {
    val hierarkia =
      List(
        OrganisaatioHierarkia(
          "1.2.246.562.10.41253773158",
          Map(
            En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
          ),
          List("01"),
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          List()
        )
      )

    val kth = OrganisaationKoulutusToteutusHakukohde(
      Some("1.2.246.562.10.10065598749"),
      KoulutusToteutusHakukohdeResult(
        Map(En -> "hakukohde 1 en", Fi -> "hakukohde 1 fi", Sv -> "hakukohde 1 sv"),
        "1.2.246.562.20.00000000000000041885",
        Some("julkaistu"),
        Some("arkistoitu"),
        Some("arkistoitu"),
        Some(18),
        Some(true),
        Some(true),
        Some(false)
      )
    )

    assert(
      OrganisaatioUtils.mapOrganisaationHakukohteetToParents(
        hierarkia,
        Map(
          Some("1.2.246.562.10.10065598749") -> Vector(kth)
        )
      ) == List(
        OrganisaatioHierarkiaWithHakukohteet(
          "1.2.246.562.10.41253773158",
          Map(
            En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
          ),
          List("01"),
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          List(),
          hakukohteet = List()
        )
      )
    )
  }

  it should "return koulutustoimijan oppilaitos with hakukohteet" in {
    val hierarkia =
      List(
        OrganisaatioHierarkia(
          "1.2.246.562.10.41253773158",
          Map(
            En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
          ),
          List("01"),
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          List(
            OrganisaatioHierarkia(
              "1.2.246.562.10.10645749713",
              Map(
                En -> "Pop & Jazz Konservatorio Lappia",
                Fi -> "Pop & Jazz Konservatorio Lappia",
                Sv -> "Pop & Jazz Konservatorio Lappia"
              ),
              List("03"),
              List(
                "1.2.246.562.10.10645749713",
                "1.2.246.562.10.00000000001",
                "1.2.246.562.10.41253773158",
                "1.2.246.562.10.93483820481"
              ),
              List()
            )
          )
        )
      )

    val kth = OrganisaationKoulutusToteutusHakukohde(
      Some("1.2.246.562.10.10645749713"),
      KoulutusToteutusHakukohdeResult(
        Map(En -> "hakukohde 1 en", Fi -> "hakukohde 1 fi", Sv -> "hakukohde 1 sv"),
        "1.2.246.562.20.00000000000000041885",
        Some("julkaistu"),
        Some("arkistoitu"),
        Some("arkistoitu"),
        Some(18),
        Some(true),
        Some(true),
        Some(false)
      )
    )

    val kth2 = kth.copy(koulutusToteutusHakukohde =
      kth._2.copy(
        hakukohdeNimi = Map(En -> "hakukohde 2 en", Fi -> "hakukohde 2 fi", Sv -> "hakukohde 2 sv"),
        hakukohdeOid = "1.2.246.562.20.00000000000000041886"
      )
    )

    assert(
      OrganisaatioUtils.mapOrganisaationHakukohteetToParents(
        hierarkia,
        Map(
          Some("1.2.246.562.10.10645749713") -> Vector(kth, kth2)
        )
      ) == List(
        OrganisaatioHierarkiaWithHakukohteet(
          "1.2.246.562.10.41253773158",
          Map(
            En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
          ),
          List("01"),
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          List(
            OrganisaatioHierarkiaWithHakukohteet(
              "1.2.246.562.10.10645749713",
              Map(
                En -> "Pop & Jazz Konservatorio Lappia",
                Fi -> "Pop & Jazz Konservatorio Lappia",
                Sv -> "Pop & Jazz Konservatorio Lappia"
              ),
              List("03"),
              List(
                "1.2.246.562.10.10645749713",
                "1.2.246.562.10.00000000001",
                "1.2.246.562.10.41253773158",
                "1.2.246.562.10.93483820481"
              ),
              List(),
              List(kth, kth2)
            )
          ),
          hakukohteet = List()
        )
      )
    )
  }
}
