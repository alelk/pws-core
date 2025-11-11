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

fun Locale.toDto(): LocaleDto = LocaleDto(value)
fun Version.toDto(): VersionDto = VersionDto(toString())
fun Year.toDto(): YearDto = YearDto(toString().toInt())
fun BookId.toDto(): BookIdDto = BookIdDto(toString())
fun SongId.toDto(): SongIdDto = SongIdDto(value)
fun SongNumberId.toDto(): SongNumberIdDto = SongNumberIdDto(toString())
fun Tonality.toDto(): TonalityDto = TonalityDto(identifier)
fun Person.toDto(): PersonDto = PersonDto(toString())

