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
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
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
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.93483820481",
            Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
            List("02"),
            Some("oppilaitostyyppi_21#1"),
            "AKTIIVINEN",
            List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
            None,
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
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.93483820481",
            Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
            List("02"),
            Some("oppilaitostyyppi_21#1"),
            "AKTIIVINEN",
            List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
            None,
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
            Some("oppilaitostyyppi_21#1"),
            "LAKKAUTETTU",
            List("1.2.246.562.10.95915936017", "1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
            None,
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
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.93483820481",
            Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
            List("02"),
            Some("oppilaitostyyppi_21#1"),
            "AKTIIVINEN",
            List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
            None,
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.10645749713",
                Map(
                  En -> "Pop & Jazz Konservatorio Lappia",
                  Fi -> "Pop & Jazz Konservatorio Lappia",
                  Sv -> "Pop & Jazz Konservatorio Lappia"
                ),
                List("03"),
                None,
                "AKTIIVINEN",
                List(
                  "1.2.246.562.10.10645749713",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158",
                  "1.2.246.562.10.93483820481"
                ),
                None,
                List(
                  OrganisaatioHierarkia(
                    "1.2.246.562.10.1064574971333",
                    Map(
                      En -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Fi -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Sv -> "Pop & Jazz Konservatorio Lappia alitoimipiste"
                    ),
                    List("03"),
                    None,
                    "AKTIIVINEN",
                    List(
                      "1.2.246.562.10.1064574971333",
                      "1.2.246.562.10.10645749713",
                      "1.2.246.562.10.00000000001",
                      "1.2.246.562.10.41253773158",
                      "1.2.246.562.10.93483820481"
                    ),
                    None,
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
            Some("oppilaitostyyppi_41#1"),
            "LAKKAUTETTU",
            List("1.2.246.562.10.95915936017", "1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
            None,
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

  "mapOrganisaationHakukohteetToParent" should "return koulutustoimijan hakukohteet for hierarkia with only koulutustoimija" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
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
        hakukohteenNimi = Map(En -> "hakukohde 2 en", Fi -> "hakukohde 2 fi", Sv -> "hakukohde 2 sv"),
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
          None,
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
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.10645749713",
            Map(
              En -> "Pop & Jazz Konservatorio Lappia",
              Fi -> "Pop & Jazz Konservatorio Lappia",
              Sv -> "Pop & Jazz Konservatorio Lappia"
            ),
            List("02"),
            Some("oppilaitostyyppi_21#1"),
            "AKTIIVINEN",
            List(
              "1.2.246.562.10.10645749713",
              "1.2.246.562.10.00000000001",
              "1.2.246.562.10.41253773158",
              "1.2.246.562.10.93483820481"
            ),
            None,
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
        hakukohteenNimi = Map(En -> "hakukohde 2 en", Fi -> "hakukohde 2 fi", Sv -> "hakukohde 2 sv"),
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
          None,
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
              None,
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
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.10645749713",
            Map(
              En -> "Pop & Jazz Konservatorio Lappia",
              Fi -> "Pop & Jazz Konservatorio Lappia",
              Sv -> "Pop & Jazz Konservatorio Lappia"
            ),
            List("02"),
            Some("oppilaitostyyppi_21#1"),
            "AKTIIVINEN",
            List(
              "1.2.246.562.10.10645749713",
              "1.2.246.562.10.00000000001",
              "1.2.246.562.10.41253773158"
            ),
            None,
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.10645749712223",
                Map(
                  En -> "Pop & Jazz Konservatorio Lappia",
                  Fi -> "Pop & Jazz Konservatorio Lappia",
                  Sv -> "Pop & Jazz Konservatorio Lappia"
                ),
                List("03"),
                None,
                "AKTIIVINEN",
                List(
                  "1.2.246.562.10.10645749712223",
                  "1.2.246.562.10.10645749713",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158"
                ),
                None,
                List(
                  OrganisaatioHierarkia(
                    "1.2.246.562.10.1064574971333",
                    Map(
                      En -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Fi -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Sv -> "Pop & Jazz Konservatorio Lappia alitoimipiste"
                    ),
                    List("03"),
                    None,
                    "AKTIIVINEN",
                    List(
                      "1.2.246.562.10.1064574971333",
                      "1.2.246.562.10.10645749712223",
                      "1.2.246.562.10.10645749713",
                      "1.2.246.562.10.00000000001",
                      "1.2.246.562.10.41253773158"
                    ),
                    None,
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
        hakukohteenNimi = Map(En -> "hakukohde 2 en", Fi -> "hakukohde 2 fi", Sv -> "hakukohde 2 sv"),
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
          None,
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
              None,
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
                  None,
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
                      None,
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
          None,
          "AKTIIVINEN",
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          None,
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
        hakukohteenNimi = Map(En -> "hakukohde 2 en", Fi -> "hakukohde 2 fi", Sv -> "hakukohde 2 sv"),
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
          None,
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
          None,
          "AKTIIVINEN",
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          None,
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
          None,
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
          None,
          "AKTIIVINEN",
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          None,
          List(
            OrganisaatioHierarkia(
              "1.2.246.562.10.10645749713",
              Map(
                En -> "Pop & Jazz Konservatorio Lappia",
                Fi -> "Pop & Jazz Konservatorio Lappia",
                Sv -> "Pop & Jazz Konservatorio Lappia"
              ),
              List("03"),
              None,
              "AKTIIVINEN",
              List(
                "1.2.246.562.10.10645749713",
                "1.2.246.562.10.00000000001",
                "1.2.246.562.10.41253773158",
                "1.2.246.562.10.93483820481"
              ),
              None,
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
        hakukohteenNimi = Map(En -> "hakukohde 2 en", Fi -> "hakukohde 2 fi", Sv -> "hakukohde 2 sv"),
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
          None,
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
              None,
              List(),
              List(kth, kth2)
            )
          ),
          hakukohteet = List()
        )
      )
    )
  }

  "getKayttooikeusDescendantOids" should "return list with only org self when org does not have descendants" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.93483820481",
            Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
            List("02"),
            Some("oppilaitostyyppi_21#1"),
            "AKTIIVINEN",
            List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
            None,
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.10645749713",
                Map(
                  En -> "Pop & Jazz Konservatorio Lappia",
                  Fi -> "Pop & Jazz Konservatorio Lappia",
                  Sv -> "Pop & Jazz Konservatorio Lappia"
                ),
                List("03"),
                None,
                "AKTIIVINEN",
                List(
                  "1.2.246.562.10.10645749713",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158",
                  "1.2.246.562.10.93483820481"
                ),
                None,
                List(
                  OrganisaatioHierarkia(
                    "1.2.246.562.10.1064574971333",
                    Map(
                      En -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Fi -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Sv -> "Pop & Jazz Konservatorio Lappia alitoimipiste"
                    ),
                    List("03"),
                    None,
                    "AKTIIVINEN",
                    List(
                      "1.2.246.562.10.1064574971333",
                      "1.2.246.562.10.10645749713",
                      "1.2.246.562.10.00000000001",
                      "1.2.246.562.10.41253773158",
                      "1.2.246.562.10.93483820481"
                    ),
                    None,
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
            Some("oppilaitostyyppi_41#1"),
            "LAKKAUTETTU",
            List("1.2.246.562.10.95915936017", "1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
            None,
            List()
          )
        )
      )

    assert(
      OrganisaatioUtils.getKayttooikeusDescendantAndSelfOids(hierarkia, List("1.2.246.562.10.95915936017")) == List(
        "1.2.246.562.10.95915936017"
      )
    )
  }

  it should "return list of käyttöoikeusdescendants for one käyttöoikeus org" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.93483820481",
            Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
            List("02"),
            Some("oppilaitostyyppi_21#1"),
            "AKTIIVINEN",
            List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
            None,
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.10645749713",
                Map(
                  En -> "Pop & Jazz Konservatorio Lappia",
                  Fi -> "Pop & Jazz Konservatorio Lappia",
                  Sv -> "Pop & Jazz Konservatorio Lappia"
                ),
                List("03"),
                None,
                "AKTIIVINEN",
                List(
                  "1.2.246.562.10.10645749713",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158",
                  "1.2.246.562.10.93483820481"
                ),
                None,
                List(
                  OrganisaatioHierarkia(
                    "1.2.246.562.10.1064574971333",
                    Map(
                      En -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Fi -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Sv -> "Pop & Jazz Konservatorio Lappia alitoimipiste"
                    ),
                    List("03"),
                    None,
                    "AKTIIVINEN",
                    List(
                      "1.2.246.562.10.1064574971333",
                      "1.2.246.562.10.10645749713",
                      "1.2.246.562.10.00000000001",
                      "1.2.246.562.10.41253773158",
                      "1.2.246.562.10.93483820481"
                    ),
                    None,
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
            Some("oppilaitostyyppi_41#1"),
            "LAKKAUTETTU",
            List("1.2.246.562.10.95915936017", "1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
            None,
            List()
          )
        )
      )

    assert(
      OrganisaatioUtils.getKayttooikeusDescendantAndSelfOids(hierarkia, List("1.2.246.562.10.41253773158")) == List(
        "1.2.246.562.10.41253773158",
        "1.2.246.562.10.93483820481",
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.1064574971333",
        "1.2.246.562.10.95915936017"
      )
    )
  }

  it should "return list of käyttöoikeusdescendants for two toimipiste käyttöoikeus orgs" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.93483820481",
            Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
            List("02"),
            Some("oppilaitostyyppi_41#1"),
            "AKTIIVINEN",
            List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
            None,
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.10645749713",
                Map(
                  En -> "Pop & Jazz Konservatorio Lappia",
                  Fi -> "Pop & Jazz Konservatorio Lappia",
                  Sv -> "Pop & Jazz Konservatorio Lappia"
                ),
                List("03"),
                None,
                "AKTIIVINEN",
                List(
                  "1.2.246.562.10.10645749713",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158",
                  "1.2.246.562.10.93483820481"
                ),
                None,
                List(
                  OrganisaatioHierarkia(
                    "1.2.246.562.10.1064574971333",
                    Map(
                      En -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Fi -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Sv -> "Pop & Jazz Konservatorio Lappia alitoimipiste"
                    ),
                    List("03"),
                    None,
                    "AKTIIVINEN",
                    List(
                      "1.2.246.562.10.1064574971333",
                      "1.2.246.562.10.10645749713",
                      "1.2.246.562.10.00000000001",
                      "1.2.246.562.10.41253773158",
                      "1.2.246.562.10.93483820481"
                    ),
                    None,
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
            Some("oppilaitostyyppi_41#1"),
            "LAKKAUTETTU",
            List("1.2.246.562.10.95915936017", "1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
            None,
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.77504323534",
                Map(
                  En -> "Kemi-Tornion ammattikorkeakoulu, Kulttuuriala",
                  Fi -> "Kemi-Tornion ammattikorkeakoulu, Kulttuuriala",
                  Sv -> "Kemi-Tornion ammattikorkeakoulu, Kulttuuriala"
                ),
                List("03"),
                None,
                "LAKKAUTETTU",
                List(
                  "1.2.246.562.10.95915936017",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158",
                  "1.2.246.562.10.77504323534"
                ),
                None,
                List()
              )
            )
          )
        )
      )

    assert(
      OrganisaatioUtils.getKayttooikeusDescendantAndSelfOids(
        hierarkia,
        List("1.2.246.562.10.95915936017", "1.2.246.562.10.93483820481")
      ) == List(
        "1.2.246.562.10.93483820481",
        "1.2.246.562.10.10645749713",
        "1.2.246.562.10.1064574971333",
        "1.2.246.562.10.95915936017",
        "1.2.246.562.10.77504323534"
      )
    )
  }

  "filterActiveOrgsWithoutPeruskoulu" should "remove POISTETTU org and childs from hierarkia" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.93483820481",
            Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
            List("02"),
            Some("oppilaitostyyppi_41#1"),
            "AKTIIVINEN",
            List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
            None,
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.10645749713",
                Map(
                  En -> "Pop & Jazz Konservatorio Lappia",
                  Fi -> "Pop & Jazz Konservatorio Lappia",
                  Sv -> "Pop & Jazz Konservatorio Lappia"
                ),
                List("03"),
                None,
                "AKTIIVINEN",
                List(
                  "1.2.246.562.10.10645749713",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158",
                  "1.2.246.562.10.93483820481"
                ),
                None,
                List(
                  OrganisaatioHierarkia(
                    "1.2.246.562.10.1064574971333",
                    Map(
                      En -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Fi -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Sv -> "Pop & Jazz Konservatorio Lappia alitoimipiste"
                    ),
                    List("03"),
                    None,
                    "AKTIIVINEN",
                    List(
                      "1.2.246.562.10.1064574971333",
                      "1.2.246.562.10.10645749713",
                      "1.2.246.562.10.00000000001",
                      "1.2.246.562.10.41253773158",
                      "1.2.246.562.10.93483820481"
                    ),
                    None,
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
            Some("oppilaitostyyppi_41#1"),
            "POISTETTU",
            List("1.2.246.562.10.95915936017", "1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
            None,
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.77504323534",
                Map(
                  En -> "Kemi-Tornion ammattikorkeakoulu, Kulttuuriala",
                  Fi -> "Kemi-Tornion ammattikorkeakoulu, Kulttuuriala",
                  Sv -> "Kemi-Tornion ammattikorkeakoulu, Kulttuuriala"
                ),
                List("03"),
                None,
                "POISTETTU",
                List(
                  "1.2.246.562.10.95915936017",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158",
                  "1.2.246.562.10.77504323534"
                ),
                None,
                List()
              )
            )
          )
        )
      )

    val expectedResult =
      Some(
        OrganisaatioHierarkia(
          "1.2.246.562.10.41253773158",
          Map(
            En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
          ),
          List("01"),
          None,
          "AKTIIVINEN",
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          None,
          List(
            OrganisaatioHierarkia(
              "1.2.246.562.10.93483820481",
              Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
              List("02"),
              Some("oppilaitostyyppi_41#1"),
              "AKTIIVINEN",
              List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
              None,
              List(
                OrganisaatioHierarkia(
                  "1.2.246.562.10.10645749713",
                  Map(
                    En -> "Pop & Jazz Konservatorio Lappia",
                    Fi -> "Pop & Jazz Konservatorio Lappia",
                    Sv -> "Pop & Jazz Konservatorio Lappia"
                  ),
                  List("03"),
                  None,
                  "AKTIIVINEN",
                  List(
                    "1.2.246.562.10.10645749713",
                    "1.2.246.562.10.00000000001",
                    "1.2.246.562.10.41253773158",
                    "1.2.246.562.10.93483820481"
                  ),
                  None,
                  List(
                    OrganisaatioHierarkia(
                      "1.2.246.562.10.1064574971333",
                      Map(
                        En -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                        Fi -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                        Sv -> "Pop & Jazz Konservatorio Lappia alitoimipiste"
                      ),
                      List("03"),
                      None,
                      "AKTIIVINEN",
                      List(
                        "1.2.246.562.10.1064574971333",
                        "1.2.246.562.10.10645749713",
                        "1.2.246.562.10.00000000001",
                        "1.2.246.562.10.41253773158",
                        "1.2.246.562.10.93483820481"
                      ),
                      None,
                      List()
                    )
                  )
                )
              )
            )
          )
        )
      )

    assert(OrganisaatioUtils.filterActiveOrgsWithoutPeruskoulu(hierarkia) == expectedResult)
  }

  "filterActiveOrgsWithoutPeruskoulu" should "remove SUUNNITELTU org from hierarkia" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.93483820481",
            Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
            List("02"),
            Some("oppilaitostyyppi_41#1"),
            "AKTIIVINEN",
            List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
            None,
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.10645749713",
                Map(
                  En -> "Pop & Jazz Konservatorio Lappia",
                  Fi -> "Pop & Jazz Konservatorio Lappia",
                  Sv -> "Pop & Jazz Konservatorio Lappia"
                ),
                List("03"),
                None,
                "AKTIIVINEN",
                List(
                  "1.2.246.562.10.10645749713",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158",
                  "1.2.246.562.10.93483820481"
                ),
                None,
                List(
                  OrganisaatioHierarkia(
                    "1.2.246.562.10.1064574971333",
                    Map(
                      En -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Fi -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Sv -> "Pop & Jazz Konservatorio Lappia alitoimipiste"
                    ),
                    List("03"),
                    None,
                    "AKTIIVINEN",
                    List(
                      "1.2.246.562.10.1064574971333",
                      "1.2.246.562.10.10645749713",
                      "1.2.246.562.10.00000000001",
                      "1.2.246.562.10.41253773158",
                      "1.2.246.562.10.93483820481"
                    ),
                    None,
                    List()
                  )
                )
              )
            )
          ),
          OrganisaatioHierarkia(
            "1.2.246.562.10.95915936017",
            Map(
              En -> "Uusi hieno ammattikorkeakoulu",
              Fi -> "Uusi hieno ammattikorkeakoulu",
              Sv -> "Uusi hieno ammattikorkeakoulu"
            ),
            List("02"),
            Some("oppilaitostyyppi_41#1"),
            "SUUNNITELTU",
            List("1.2.246.562.10.95915936017", "1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
            None,
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.77504323534",
                Map(
                  En -> "Uusi hieno ammattikorkeakoulu, Kulttuuriala",
                  Fi -> "Uusi hieno ammattikorkeakoulu, Kulttuuriala",
                  Sv -> "Uusi hieno ammattikorkeakoulu, Kulttuuriala"
                ),
                List("03"),
                None,
                "SUUNNITELTU",
                List(
                  "1.2.246.562.10.95915936017",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158",
                  "1.2.246.562.10.77504323534"
                ),
                None,
                List()
              )
            )
          )
        )
      )

    val expectedResult =
      Some(
        OrganisaatioHierarkia(
          "1.2.246.562.10.41253773158",
          Map(
            En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
          ),
          List("01"),
          None,
          "AKTIIVINEN",
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          None,
          List(
            OrganisaatioHierarkia(
              "1.2.246.562.10.93483820481",
              Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
              List("02"),
              Some("oppilaitostyyppi_41#1"),
              "AKTIIVINEN",
              List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
              None,
              List(
                OrganisaatioHierarkia(
                  "1.2.246.562.10.10645749713",
                  Map(
                    En -> "Pop & Jazz Konservatorio Lappia",
                    Fi -> "Pop & Jazz Konservatorio Lappia",
                    Sv -> "Pop & Jazz Konservatorio Lappia"
                  ),
                  List("03"),
                  None,
                  "AKTIIVINEN",
                  List(
                    "1.2.246.562.10.10645749713",
                    "1.2.246.562.10.00000000001",
                    "1.2.246.562.10.41253773158",
                    "1.2.246.562.10.93483820481"
                  ),
                  None,
                  List(
                    OrganisaatioHierarkia(
                      "1.2.246.562.10.1064574971333",
                      Map(
                        En -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                        Fi -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                        Sv -> "Pop & Jazz Konservatorio Lappia alitoimipiste"
                      ),
                      List("03"),
                      None,
                      "AKTIIVINEN",
                      List(
                        "1.2.246.562.10.1064574971333",
                        "1.2.246.562.10.10645749713",
                        "1.2.246.562.10.00000000001",
                        "1.2.246.562.10.41253773158",
                        "1.2.246.562.10.93483820481"
                      ),
                      None,
                      List()
                    )
                  )
                )
              )
            )
          )
        )
      )

    assert(OrganisaatioUtils.filterActiveOrgsWithoutPeruskoulu(hierarkia) == expectedResult)
  }

  it should "remove POISTETTU and LAKKAUTETTU alitoimipiste from deeper in hierarkia" in {
    val hierarkia =
      OrganisaatioHierarkia(
        "1.2.246.562.10.41253773158",
        Map(
          En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
          Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
        ),
        List("01"),
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
        None,
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.93483820481",
            Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
            List("02"),
            Some("oppilaitostyyppi_41#1"),
            "AKTIIVINEN",
            List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
            None,
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.10645749713",
                Map(
                  En -> "Pop & Jazz Konservatorio Lappia",
                  Fi -> "Pop & Jazz Konservatorio Lappia",
                  Sv -> "Pop & Jazz Konservatorio Lappia"
                ),
                List("03"),
                None,
                "AKTIIVINEN",
                List(
                  "1.2.246.562.10.10645749713",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158",
                  "1.2.246.562.10.93483820481"
                ),
                None,
                List(
                  OrganisaatioHierarkia(
                    "1.2.246.562.10.1064574971333",
                    Map(
                      En -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Fi -> "Pop & Jazz Konservatorio Lappia alitoimipiste",
                      Sv -> "Pop & Jazz Konservatorio Lappia alitoimipiste"
                    ),
                    List("03"),
                    None,
                    "POISTETTU",
                    List(
                      "1.2.246.562.10.1064574971333",
                      "1.2.246.562.10.10645749713",
                      "1.2.246.562.10.00000000001",
                      "1.2.246.562.10.41253773158",
                      "1.2.246.562.10.93483820481"
                    ),
                    None,
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
            Some("oppilaitostyyppi_41#1"),
            "LAKKAUTETTU",
            List("1.2.246.562.10.95915936017", "1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
            None,
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.77504323534",
                Map(
                  En -> "Kemi-Tornion ammattikorkeakoulu, Kulttuuriala",
                  Fi -> "Kemi-Tornion ammattikorkeakoulu, Kulttuuriala",
                  Sv -> "Kemi-Tornion ammattikorkeakoulu, Kulttuuriala"
                ),
                List("03"),
                None,
                "LAKKAUTETTU",
                List(
                  "1.2.246.562.10.95915936017",
                  "1.2.246.562.10.00000000001",
                  "1.2.246.562.10.41253773158",
                  "1.2.246.562.10.77504323534"
                ),
                None,
                List()
              )
            )
          )
        )
      )

    val expectedResult =
      Some(
        OrganisaatioHierarkia(
          "1.2.246.562.10.41253773158",
          Map(
            En -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Fi -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia",
            Sv -> "Kemi-Tornionlaakson koulutuskuntayhtymä Lappia"
          ),
          List("01"),
          None,
          "AKTIIVINEN",
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.41253773158"),
          None,
          List(
            OrganisaatioHierarkia(
              "1.2.246.562.10.93483820481",
              Map(En -> "Ammattiopisto Lappia", Fi -> "Ammattiopisto Lappia", Sv -> "Ammattiopisto Lappia"),
              List("02"),
              Some("oppilaitostyyppi_41#1"),
              "AKTIIVINEN",
              List("1.2.246.562.10.41253773158", "1.2.246.562.10.00000000001", "1.2.246.562.10.93483820481"),
              None,
              List(
                OrganisaatioHierarkia(
                  "1.2.246.562.10.10645749713",
                  Map(
                    En -> "Pop & Jazz Konservatorio Lappia",
                    Fi -> "Pop & Jazz Konservatorio Lappia",
                    Sv -> "Pop & Jazz Konservatorio Lappia"
                  ),
                  List("03"),
                  None,
                  "AKTIIVINEN",
                  List(
                    "1.2.246.562.10.10645749713",
                    "1.2.246.562.10.00000000001",
                    "1.2.246.562.10.41253773158",
                    "1.2.246.562.10.93483820481"
                  ),
                  None,
                  List()
                )
              )
            )
          )
        )
      )

    assert(OrganisaatioUtils.filterActiveOrgsWithoutPeruskoulu(hierarkia) == expectedResult)
  }

  "filterActiveOrgsWithoutPeruskoulu" should "filter out organizations with oppilaitostyyppi oppilaitostyyppi_11#1" in {
    val hierarkia = OrganisaatioHierarkia(
      "1.2.246.562.10.10063814452",
      Map(
        En -> "Iin kunta",
        Fi -> "Iin kunta",
        Sv -> "Iin kunta"
      ),
      List("01", "07", "09"),
      None,
      "AKTIIVINEN",
      List("1.2.246.562.10.00000000001", "1.2.246.562.10.10063814452"),
      None,
      List(
        OrganisaatioHierarkia(
          "1.2.246.562.10.13792634993",
          Map(
            En -> "Aseman koulu",
            Fi -> "Aseman koulu",
            Sv -> "Aseman koulu"
          ),
          List("02"),
          Some("oppilaitostyyppi_11#1"),
          "AKTIIVINEN",
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.10063814452", "1.2.246.562.10.13792634993"),
          None,
          List()
        ),
        OrganisaatioHierarkia(
          "1.2.246.562.10.27440356239",
          Map(
            En -> "Pohjois-Iin koulu",
            Fi -> "Pohjois-Iin koulu",
            Sv -> "Pohjois-Iin koulu"
          ),
          List("02"),
          Some("oppilaitostyyppi_11#1"),
          "AKTIIVINEN",
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.10063814452", "1.2.246.562.10.27440356239"),
          None,
          List()
        ),
        OrganisaatioHierarkia(
          "1.2.246.562.10.95483002572",
          Map(
            En -> "Tiernan koulu",
            Fi -> "Tiernan koulu",
            Sv -> "Tiernan koulu"
          ),
          List("02"),
          Some("oppilaitostyyppi_12#1"),
          "AKTIIVINEN",
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.80381044462", "1.2.246.562.10.95483002572"),
          None,
          List(
            OrganisaatioHierarkia(
              "1.2.246.562.10.22667866366",
              Map(
                En -> "Pajaluokat opetuspiste",
                Fi -> "Pajaluokat opetuspiste",
                Sv -> "Pajaluokat opetuspiste"
              ),
              List("03"),
              None,
              "AKTIIVINEN",
              List("1.2.246.562.10.00000000001", "1.2.246.562.10.22667866366", "1.2.246.562.10.80381044462", "1.2.246.562.10.95483002572"),
              None,
              List()
            )
          )
        ),
        OrganisaatioHierarkia(
          "1.2.246.562.10.44529610774",
          Map(
            En -> "Iin lukio",
            Fi -> "Iin lukio",
            Sv -> "Iin lukio"
          ),
          List("02"),
          Some("oppilaitostyyppi_15#1"),
          "AKTIIVINEN",
          List("1.2.246.562.10.00000000001", "1.2.246.562.10.10063814452", "1.2.246.562.10.44529610774"),
          None,
          List(
            OrganisaatioHierarkia(
              "1.2.246.562.10.41383012972",
              Map(
                En -> "Iin lukio",
                Fi -> "Iin lukio",
                Sv -> "Iin lukio"
              ),
              List("03"),
              None,
              "AKTIIVINEN",
              List("1.2.246.562.10.00000000001", "1.2.246.562.10.10063814452", "1.2.246.562.10.41383012972", "1.2.246.562.10.44529610774"),
              None,
              List()
            )
          )
        )
      )
    )

    val expectedResult = Some(
      OrganisaatioHierarkia(
        "1.2.246.562.10.10063814452",
        Map(
          En -> "Iin kunta",
          Fi -> "Iin kunta",
          Sv -> "Iin kunta"
        ),
        List("01", "07", "09"),
        None,
        "AKTIIVINEN",
        List("1.2.246.562.10.00000000001", "1.2.246.562.10.10063814452"),
        None,
        List(
          OrganisaatioHierarkia(
            "1.2.246.562.10.44529610774",
            Map(
              En -> "Iin lukio",
              Fi -> "Iin lukio",
              Sv -> "Iin lukio"
            ),
            List("02"),
            Some("oppilaitostyyppi_15#1"),
            "AKTIIVINEN",
            List("1.2.246.562.10.00000000001", "1.2.246.562.10.10063814452", "1.2.246.562.10.44529610774"),
            None,
            List(
              OrganisaatioHierarkia(
                "1.2.246.562.10.41383012972",
                Map(
                  En -> "Iin lukio",
                  Fi -> "Iin lukio",
                  Sv -> "Iin lukio"
                ),
                List("03"),
                None,
                "AKTIIVINEN",
                List("1.2.246.562.10.00000000001", "1.2.246.562.10.10063814452", "1.2.246.562.10.41383012972", "1.2.246.562.10.44529610774"),
                None,
                List()
              )
            )
          )
        )
      )
    )

    assert(OrganisaatioUtils.filterActiveOrgsWithoutPeruskoulu(hierarkia) == expectedResult)
  }
}
