package io.github.alelk.pws.domain.favorite.repository

import io.github.alelk.pws.domain.core.result.ClearResourcesResult
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.ToggleResourceResult
import io.github.alelk.pws.domain.favorite.model.Favorite
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject

/**
 * Mutation operations for Favorites.
 */
interface FavoriteWriteRepository {
  /**
   * Add song to favorites.
   */
  suspend fun add(subject: FavoriteSubject): CreateResourceResult<Favorite>

  /**
   * Remove song from favorites.
   */
  suspend fun remove(subject: FavoriteSubject): DeleteResourceResult<FavoriteSubject>

  /**
   * Toggle favorite status.
   */
  suspend fun toggle(subject: FavoriteSubject): ToggleResourceResult<FavoriteSubject>

  /**
   * Clear all favorites.
   */
  suspend fun clearAll(): ClearResourcesResult
}

