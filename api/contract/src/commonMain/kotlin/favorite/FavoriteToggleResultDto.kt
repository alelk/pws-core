package io.github.alelk.pws.api.contract.favorite

import kotlinx.serialization.Serializable

/**
 * Toggle favorite result.
 */
@Serializable
data class FavoriteToggleResultDto(
  val isFavorite: Boolean
)