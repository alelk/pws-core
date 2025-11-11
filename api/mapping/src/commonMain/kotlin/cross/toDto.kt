package io.github.alelk.pws.api.mapping.cross

import io.github.alelk.pws.api.contract.cross.BookWithSongsDto
import io.github.alelk.pws.api.mapping.book.toDto
import io.github.alelk.pws.api.mapping.song.toDto
import io.github.alelk.pws.domain.cross.projection.BookWithSongs

fun BookWithSongs.toDto(): BookWithSongsDto = BookWithSongsDto(
  book = book.toDto(),
  songs = songs.mapValues { it.value.toDto() }
)

