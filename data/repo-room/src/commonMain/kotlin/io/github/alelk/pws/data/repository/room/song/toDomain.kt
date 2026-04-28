package io.github.alelk.pws.data.repository.room.song

import io.github.alelk.pws.database.song.SongEntity
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.lyric.format.parseLyric

fun SongEntity.toDomain(): SongDetail = SongDetail(
  id = id,
  version = version,
  locale = locale,
  name = NonEmptyString(name),
  lyric = parseLyric(lyric, locale),
  author = author,
  translator = translator,
  composer = composer,
  tonalities = tonalities,
  year = year,
  bibleRef = bibleRef,
  edited = edited,
)

fun SongEntity.toSummary(): SongSummary = SongSummary(
  id = id,
  version = version,
  locale = locale,
  name = NonEmptyString(name),
  edited = edited,
)
