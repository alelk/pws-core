package io.github.alelk.pws.api.mapping.cross

import io.github.alelk.pws.api.contract.cross.BookWithSongsDto
import io.github.alelk.pws.api.mapping.book.toDomain
import io.github.alelk.pws.api.mapping.song.toDomain
import io.github.alelk.pws.domain.cross.projection.BookWithSongs

fun BookWithSongsDto.toDomain(): BookWithSongs = BookWithSongs(
  book = book.toDomain(),
  songs = songs.mapValues { it.value.toDomain() }
)

