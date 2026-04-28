package io.github.alelk.pws.database.favorite

import androidx.room.ColumnInfo
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId

/** Projection for displaying favorites list with song and book names. */
data class FavoriteSongProjection(
  @ColumnInfo(name = "book_id") val bookId: BookId,
  @ColumnInfo(name = "song_id") val songId: SongId,
  @ColumnInfo(name = "position") val position: Int,
  @ColumnInfo(name = "song_name") val songName: String,
  @ColumnInfo(name = "song_number") val songNumber: Int,
  @ColumnInfo(name = "book_display_name") val bookDisplayName: String,
)

