package io.github.alelk.pws.data.repository.room.history

import io.github.alelk.pws.database.history.HistoryEntryProjection
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.history.model.HistoryEntry
import io.github.alelk.pws.domain.history.model.HistorySubject
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun HistoryEntryProjection.toDomain() = HistoryEntry(
  id = id,
  subject = HistorySubject.BookedSong(SongNumberId(bookId, songId)),
  songName = songName,
  songNumber = songNumber,
  bookDisplayName = bookDisplayName,
  viewedAt = accessTimestamp.toInstant(TimeZone.currentSystemDefault()),
  viewCount = 1
)

