package io.github.alelk.pws.api.mapping.core

import io.github.alelk.pws.api.contract.core.*
import io.github.alelk.pws.api.contract.core.ids.*
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.Year
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.person.Person
import io.github.alelk.pws.domain.tonality.Tonality

fun LocaleDto.toDomain(): Locale = Locale.of(value)
fun VersionDto.toDomain(): Version = Version.fromString(value)
fun YearDto.toDomain(): Year = Year(value)
fun BookIdDto.toDomain(): BookId = BookId.parse(value)
fun SongIdDto.toDomain(): SongId = SongId(value)
fun SongNumberIdDto.toDomain(): SongNumberId = SongNumberId.parse(value)
fun TonalityDto.toDomain(): Tonality = Tonality.fromIdentifier(value)
fun PersonDto.toDomain(): Person = Person(value)

