package io.github.alelk.pws.domain.favorite.model

import io.github.alelk.pws.domain.core.ids.SongNumberId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Favorite song entry.
 */
@OptIn(ExperimentalTime::class)
data class Favorite(
  val songNumberId: SongNumberId,
  val addedAt: Instant
)

