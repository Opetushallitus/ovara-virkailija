package fi.oph.ovara.backend.domain

import java.time.LocalDate

case class Alkamiskausi(
    alkamiskausityyppi: String,
    koulutuksenAlkamiskausiKoodiUri: Option[String] = None,
    koulutuksenAlkamisvuosi: Option[String] = None,
    koulutuksenAlkamispaivamaara: Option[LocalDate] = None,
    koulutuksenPaattymispaivamaara: Option[LocalDate] = None
)
