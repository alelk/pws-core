package io.github.alelk.pws.domain.favorite.model

import io.github.alelk.pws.domain.core.ids.SongNumberId

/**
 * Favorite song entry.
 */
data class Favorite(
  val id: Long,
  val songNumberId: SongNumberId,
  val addedAt: Long // timestamp in millis
)

