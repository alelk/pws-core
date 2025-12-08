package io.github.alelk.pws.domain.favorite.model

import io.github.alelk.pws.domain.core.ids.SongNumberId

/**
 * Favorite song with song and book details for display.
 */
data class FavoriteWithSongInfo(
  val id: Long,
  val songNumberId: SongNumberId,
  val songNumber: Int,
  val songName: String,
  val bookDisplayName: String,
  val addedAt: Long
)

