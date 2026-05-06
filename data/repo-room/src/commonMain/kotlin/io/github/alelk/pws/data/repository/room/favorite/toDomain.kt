package io.github.alelk.pws.data.repository.room.favorite

import io.github.alelk.pws.database.favorite.FavoriteSongProjection
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.model.FavoriteSong
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun FavoriteSongProjection.toDomain() = FavoriteSong(
  subject = FavoriteSubject.BookedSong(SongNumberId(bookId, songId)),
  songName = songName,
  songNumber = songNumber,
  bookDisplayName = bookDisplayName,
  // FavoriteEntity has no real timestamp — position is monotonically increasing,
  // so convert it to a fake Instant to preserve correct "newest first" sort order.
  addedAt = Instant.fromEpochMilliseconds(position.toLong())
)

