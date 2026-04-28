package io.github.alelk.pws.data.repository.room.favorite

import io.github.alelk.pws.database.favorite.FavoriteSongProjection
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.model.FavoriteSong
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun FavoriteSongProjection.toDomain() = FavoriteSong(
  subject = FavoriteSubject.BookedSong(SongNumberId(bookId, songId)),
  songName = songName,
  songNumber = songNumber,
  bookDisplayName = bookDisplayName,
  // FavoriteEntity has no timestamp — use position as sort key via epoch millis hack
  addedAt = Clock.System.now() // will be replaced when DB has timestamp
)

