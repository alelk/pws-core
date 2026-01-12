package io.github.alelk.pws.api.mapping.usersong

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.usersong.UserSongOverrideRequestDto
import io.github.alelk.pws.api.mapping.core.nonEmpty
import io.github.alelk.pws.api.mapping.core.toDomain
import io.github.alelk.pws.api.mapping.song.toDomain
import io.github.alelk.pws.domain.core.BibleRef
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.song.command.OverrideSongCommand

/**
 * Convert override request to domain command.
 */
fun UserSongOverrideRequestDto.toDomainCommand(songId: SongIdDto): OverrideSongCommand = OverrideSongCommand(
  songId = songId.toDomain(),
  name = name?.let { nonEmpty(it, "UserSongOverrideRequestDto.name") },
  lyric = lyric?.toDomain(),
  author = OptionalField.fromNullable(author?.toDomain(), treatNullAsClear = false),
  translator = OptionalField.fromNullable(translator?.toDomain(), treatNullAsClear = false),
  composer = OptionalField.fromNullable(composer?.toDomain(), treatNullAsClear = false),
  bibleRef = bibleRef?.let { OptionalField.Set(BibleRef(it)) } ?: OptionalField.Unchanged,
  tonalities = tonalities?.map { it.toDomain() }?.let { OptionalField.Set(it) } ?: OptionalField.Unchanged
)

