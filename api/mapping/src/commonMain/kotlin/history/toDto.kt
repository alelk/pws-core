package io.github.alelk.pws.api.mapping.history

import io.github.alelk.pws.api.contract.history.HistoryEntryDto
import io.github.alelk.pws.api.contract.history.HistorySubjectDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.history.model.HistoryEntry
import io.github.alelk.pws.domain.history.model.HistorySubject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun HistoryEntry.toDto(): HistoryEntryDto = when (val s = subject) {
  is HistorySubject.BookedSong -> HistoryEntryDto.BookedSong(
    songNumberId = s.songNumberId.toDto(),
    songNumber = songNumber ?: 0,
    bookDisplayName = bookDisplayName ?: "",
    songName = songName,
    viewedAt = viewedAt,
    viewCount = viewCount
  )
  is HistorySubject.StandaloneSong -> HistoryEntryDto.StandaloneSong(
    songId = s.songId.toDto(),
    songName = songName,
    viewedAt = viewedAt,
    viewCount = viewCount
  )
}

fun HistorySubject.toDto(): HistorySubjectDto = when (this) {
  is HistorySubject.BookedSong -> HistorySubjectDto.BookedSong(songNumberId.toDto())
  is HistorySubject.StandaloneSong -> HistorySubjectDto.StandaloneSong(songId.toDto())
}

