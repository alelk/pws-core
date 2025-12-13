package io.github.alelk.pws.api.mapping.song

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SongCreateRequestDto
import io.github.alelk.pws.api.contract.song.SongUpdateRequestDto
import io.github.alelk.pws.api.mapping.core.nonEmpty
import io.github.alelk.pws.api.mapping.core.toDomain
import io.github.alelk.pws.domain.core.BibleRef
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.command.UpdateSongCommand

fun SongCreateRequestDto.toDomainCommand(): CreateSongCommand = CreateSongCommand(
  id = id.toDomain(),
  locale = locale.toDomain(),
  name = nonEmpty(name, "SongCreateRequestDto.name"),
  lyric = lyric.toDomain(),
  author = author?.toDomain(),
  translator = translator?.toDomain(),
  composer = composer?.toDomain(),
  tonalities = tonalities?.map { it.toDomain() },
  year = year?.toDomain(),
  bibleRef = bibleRef?.let { BibleRef(it) },
  edited = edited
)

fun SongUpdateRequestDto.toDomainCommand(): UpdateSongCommand = UpdateSongCommand(
  id = id.toDomain(),
  locale = locale?.toDomain(),
  name = name?.let { nonEmpty(it, "SongUpdateRequestDto.name") },
  lyric = lyric?.toDomain(),
  author = OptionalField.fromNullable(author?.toDomain(), treatNullAsClear = false),
  translator = OptionalField.fromNullable(translator?.toDomain(), treatNullAsClear = false),
  composer = OptionalField.fromNullable(composer?.toDomain(), treatNullAsClear = false),
  tonalities = tonalities?.map { it.toDomain() }?.let { OptionalField.Set(it) } ?: OptionalField.Unchanged,
  year = OptionalField.fromNullable(year?.toDomain(), treatNullAsClear = false),
  bibleRef = bibleRef?.let { OptionalField.Set(BibleRef(it)) } ?: OptionalField.Unchanged,
  expectVersion = expectedVersion?.toDomain()
)

/**
 * Convert to UpdateSongCommand using the song ID from the URL path.
 * This is useful for user book songs where the ID comes from the URL.
 */
fun SongUpdateRequestDto.toDomainCommand(songId: SongIdDto): UpdateSongCommand = UpdateSongCommand(
  id = songId.toDomain(),
  locale = locale?.toDomain(),
  name = name?.let { nonEmpty(it, "SongUpdateRequestDto.name") },
  lyric = lyric?.toDomain(),
  author = OptionalField.fromNullable(author?.toDomain(), treatNullAsClear = false),
  translator = OptionalField.fromNullable(translator?.toDomain(), treatNullAsClear = false),
  composer = OptionalField.fromNullable(composer?.toDomain(), treatNullAsClear = false),
  tonalities = tonalities?.map { it.toDomain() }?.let { OptionalField.Set(it) } ?: OptionalField.Unchanged,
  year = OptionalField.fromNullable(year?.toDomain(), treatNullAsClear = false),
  bibleRef = bibleRef?.let { OptionalField.Set(BibleRef(it)) } ?: OptionalField.Unchanged,
  expectVersion = expectedVersion?.toDomain()
)

