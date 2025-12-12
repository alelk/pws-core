package io.github.alelk.pws.domain.favorite.model

import io.github.alelk.pws.domain.core.ids.SongNumberId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Favorite song with song and book details for display.
 */
@OptIn(ExperimentalTime::class)
data class FavoriteWithSongInfo(
  val songNumberId: SongNumberId,
  val songNumber: Int,
  val songName: String,
  val bookDisplayName: String,
  val addedAt: Instant
)

