package io.github.alelk.pws.domain.favorite.repository

import io.github.alelk.pws.domain.favorite.model.FavoriteSong
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject

/**
 * Read operations for Favorites (single fetch, no reactive stream).
 */
interface FavoriteReadRepository {
  /**
   * Get favorite songs ordered by addedAt desc.
   * @param limit Maximum number of entries to return (null = no limit).
   * @param offset Number of entries to skip (default = 0).
   */
  suspend fun getAll(limit: Int? = null, offset: Int = 0): List<FavoriteSong>

  /**
   * Check if a song is in favorites.
   */
  suspend fun isFavorite(subject: FavoriteSubject): Boolean

  /**
   * Get total number of favorites.
   */
  suspend fun count(): Long
}
