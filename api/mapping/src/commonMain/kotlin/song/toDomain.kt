package io.github.alelk.pws.api.mapping.song

import io.github.alelk.pws.api.contract.song.LyricDto
import io.github.alelk.pws.api.contract.song.LyricPartDto
import io.github.alelk.pws.api.contract.song.SongDetailDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
import io.github.alelk.pws.api.mapping.core.nonEmpty
import io.github.alelk.pws.api.mapping.core.toDomain
import io.github.alelk.pws.domain.core.BibleRef
import io.github.alelk.pws.domain.song.lyric.Bridge
import io.github.alelk.pws.domain.song.lyric.Chorus
import io.github.alelk.pws.domain.song.lyric.Lyric
import io.github.alelk.pws.domain.song.lyric.LyricPart
import io.github.alelk.pws.domain.song.lyric.Verse
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.model.SongSummary

fun SongSummaryDto.toDomain(): SongSummary = SongSummary(
  id = id.toDomain(),
  version = version.toDomain(),
  locale = locale.toDomain(),
  name = nonEmpty(name, "SongSummaryDto.name"),
  edited = edited
)

fun LyricPartDto.toDomain(): LyricPart = when (this) {
  is LyricPartDto.Chorus -> Chorus(numbers, text)
  is LyricPartDto.Verse -> Verse(numbers, text)
  is LyricPartDto.Bridge -> Bridge(numbers, text)
}

fun LyricDto.toDomain(): Lyric = Lyric(map { it.toDomain() })

fun SongDetailDto.toDomain(): SongDetail = SongDetail(
  id = id.toDomain(),
  version = version.toDomain(),
  locale = locale.toDomain(),
  name = nonEmpty(name, "SongDetailDto.name"),
  lyric = lyric.toDomain(),
  author = author?.toDomain(),
  translator = translator?.toDomain(),
  composer = composer?.toDomain(),
  tonalities = tonalities?.map { it.toDomain() },
  year = year?.toDomain(),
  bibleRef = bibleRef?.let { BibleRef(it) },
  edited = edited
)

