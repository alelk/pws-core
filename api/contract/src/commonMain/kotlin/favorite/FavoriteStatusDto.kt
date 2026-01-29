package io.github.alelk.pws.api.contract.favorite

import kotlinx.serialization.Serializable

/**
 * Favorite status response.
 */
@Serializable
data class FavoriteStatusDto(
  val isFavorite: Boolean
)