package io.github.alelk.pws.api.mapping.usersong

import io.github.alelk.pws.api.contract.usersong.SongSourceDto
import io.github.alelk.pws.api.contract.usersong.UserSongDetailDto
import io.github.alelk.pws.api.contract.usersong.UserSongSummaryDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.api.mapping.song.toDto
import io.github.alelk.pws.domain.song.model.MergedSongDetail
import io.github.alelk.pws.domain.song.model.MergedSongSummary
import io.github.alelk.pws.domain.song.model.SongField
import io.github.alelk.pws.domain.song.model.SongSource
import io.github.alelk.pws.domain.tonality.Tonality

fun SongSource.toDto(): SongSourceDto = when (this) {
  SongSource.GLOBAL -> SongSourceDto.GLOBAL
  SongSource.USER -> SongSourceDto.USER
}

fun SongField.toFieldName(): String = when (this) {
  SongField.NAME -> "name"
  SongField.LYRIC -> "lyric"
  SongField.AUTHOR -> "author"
  SongField.TRANSLATOR -> "translator"
  SongField.COMPOSER -> "composer"
  SongField.TONALITIES -> "tonalities"
  SongField.BIBLE_REF -> "bibleRef"
}

fun MergedSongDetail.toDto(): UserSongDetailDto = UserSongDetailDto(
  id = id.toDto(),
  version = version.toDto(),
  locale = locale.toDto(),
  name = name.value,
  lyric = lyric.toDto(),
  author = author?.toDto(),
  translator = translator?.toDto(),
  composer = composer?.toDto(),
  tonalities = tonalities?.map(Tonality::toDto),
  year = year?.toDto(),
  bibleRef = bibleRef?.text,
  source = source.toDto(),
  hasOverride = hasOverride,
  overriddenFields = overriddenFields.map { it.toFieldName() }
)

fun MergedSongSummary.toDto(): UserSongSummaryDto = UserSongSummaryDto(
  id = id.toDto(),
  locale = locale.toDto(),
  name = name.value,
  author = author?.toDto(),
  tonalities = tonalities?.map(Tonality::toDto),
  source = source.toDto(),
  hasOverride = hasOverride
)

