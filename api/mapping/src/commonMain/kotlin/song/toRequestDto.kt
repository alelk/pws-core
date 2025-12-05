package io.github.alelk.pws.api.mapping.song

import io.github.alelk.pws.api.contract.song.SongCreateRequestDto
import io.github.alelk.pws.api.contract.song.SongUpdateRequestDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.core.getOrElse
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.command.UpdateSongCommand

fun CreateSongCommand.toRequestDto(): SongCreateRequestDto =
  SongCreateRequestDto(
    id = id.toDto(),
    locale = locale.toDto(),
    name = name.value,
    lyric = lyric.toDto(),
    author = author?.toDto(),
    translator = translator?.toDto(),
    composer = composer?.toDto(),
    tonalities = tonalities?.map { it.toDto() },
    year = year?.toDto(),
    bibleRef = bibleRef?.text,
    edited = edited,
    numbersInBook = null
  )


fun UpdateSongCommand.toRequestDto(): SongUpdateRequestDto =
  SongUpdateRequestDto(
    id = id.toDto(),
    locale = locale?.toDto(),
    name = name?.value,
    lyric = lyric?.toDto(),
    author = author.getOrElse { null }?.toDto(),
    translator = translator.getOrElse { null }?.toDto(),
    composer = composer.getOrElse { null }?.toDto(),
    tonalities = tonalities.getOrElse { null }?.map { it.toDto() },
    year = year.getOrElse { null }?.toDto(),
    bibleRef = bibleRef.getOrElse { null }?.text,
    expectedVersion = expectVersion?.toDto()
  )