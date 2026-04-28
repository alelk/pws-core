package io.github.alelk.pws.database.history

import androidx.room.ColumnInfo
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import kotlinx.datetime.LocalDateTime

/** Projection for displaying history list with song and book names. */
data class HistoryEntryProjection(
  @ColumnInfo(name = "id") val id: Long,
  @ColumnInfo(name = "book_id") val bookId: BookId,
  @ColumnInfo(name = "song_id") val songId: SongId,
  @ColumnInfo(name = "access_timestamp") val accessTimestamp: LocalDateTime,
  @ColumnInfo(name = "song_name") val songName: String,
  @ColumnInfo(name = "song_number") val songNumber: Int,
  @ColumnInfo(name = "book_display_name") val bookDisplayName: String,
)

