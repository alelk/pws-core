package io.github.alelk.pws.domain.favorite.repository

import io.github.alelk.pws.domain.core.ids.SongNumberId

/**
 * Mutation operations for Favorites.
 */
interface FavoriteWriteRepository {
  /**
   * Add song to favorites.
   * @return ID of the created favorite entry, or null if already exists.
   */
  suspend fun add(songNumberId: SongNumberId): Long?

  /**
   * Remove song from favorites.
   * @return true if removed, false if not found.
   */
  suspend fun remove(songNumberId: SongNumberId): Boolean

  /**
   * Toggle favorite status.
   * @return true if now favorite, false if removed from favorites.
   */
  suspend fun toggle(songNumberId: SongNumberId): Boolean
}

