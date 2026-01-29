package io.github.alelk.pws.api.mapping.history

import io.github.alelk.pws.api.contract.history.HistorySubjectDto
import io.github.alelk.pws.api.mapping.core.toDomain
import io.github.alelk.pws.domain.history.model.HistorySubject

fun HistorySubjectDto.toDomain(): HistorySubject = when (this) {
  is HistorySubjectDto.BookedSong -> HistorySubject.BookedSong(songNumberId.toDomain())
  is HistorySubjectDto.StandaloneSong -> HistorySubject.StandaloneSong(songId.toDomain())
}
