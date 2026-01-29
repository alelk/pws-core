package io.github.alelk.pws.domain.favorite.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Favorite song with song and book details for display.
 *
 * @property subject What is favorited (booked or standalone song).
 * @property songName Name of the song.
 * @property songNumber Number of the song in the book (null for standalone songs).
 * @property bookDisplayName Display name of the book (null for standalone songs).
 * @property addedAt When the song was added to favorites.
 */
@OptIn(ExperimentalTime::class)
data class FavoriteSong(
  val subject: FavoriteSubject,
  val songName: String,
  val songNumber: Int?,
  val bookDisplayName: String?,
  val addedAt: Instant
)

