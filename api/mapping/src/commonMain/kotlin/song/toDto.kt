package io.github.alelk.pws.api.mapping.song

import io.github.alelk.pws.api.contract.song.*
import io.github.alelk.pws.api.mapping.core.*
import io.github.alelk.pws.domain.song.lyric.*
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.tonality.Tonality

fun SongSummary.toDto(): SongSummaryDto = SongSummaryDto(
  id = id.toDto(),
  version = version.toDto(),
  locale = locale.toDto(),
  name = name.value,
  edited = edited
)

fun LyricPart.toDto(): LyricPartDto = when (this) {
  is Chorus -> LyricPartDto.Chorus(numbers, text)
  is Verse -> LyricPartDto.Verse(numbers, text)
  is Bridge -> LyricPartDto.Bridge(numbers, text)
}

fun Lyric.toDto(): LyricDto = LyricDto(map { it.toDto() })

fun SongDetail.toDto(): SongDetailDto = SongDetailDto(
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
  edited = edited
)

