package io.github.alelk.pws.domain.favorite.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Favorite song entry.
 */
@OptIn(ExperimentalTime::class)
data class Favorite(
  val subject: FavoriteSubject,
  val addedAt: Instant
)

