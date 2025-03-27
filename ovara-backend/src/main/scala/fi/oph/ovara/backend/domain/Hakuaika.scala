package fi.oph.ovara.backend.domain

import java.time.LocalDate

case class Hakuaika(
    alkaa: Option[LocalDate],
    paattyy: Option[LocalDate]
)
