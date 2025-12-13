package io.github.alelk.pws.api.contract.favorite

import io.github.alelk.pws.api.contract.core.ids.SongNumberIdDto
import kotlinx.serialization.Serializable

/**
 * Favorite entry response.
 */
@Serializable
data class FavoriteDto(
  val songNumberId: SongNumberIdDto,
  val addedAt: Long // epoch millis
)

/**
 * Favorite status response.
 */
@Serializable
data class FavoriteStatusDto(
  val isFavorite: Boolean
)

/**
 * Toggle favorite result.
 */
@Serializable
data class FavoriteToggleResultDto(
  val isFavorite: Boolean
)

