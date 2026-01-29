package io.github.alelk.pws.domain.favorite.repository

import io.github.alelk.pws.domain.favorite.model.FavoriteSong
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import kotlinx.coroutines.flow.Flow

/**
 * Observe operations for Favorites.
 */
interface FavoriteObserveRepository {
  /**
   * Observe all favorites ordered by addedAt desc.
   */
  fun observeAll(limit: Int? = null, offset: Int = 0): Flow<List<FavoriteSong>>

  /**
   * Check if a song is in favorites.
   */
  fun observeIsFavorite(subject: FavoriteSubject): Flow<Boolean>
}

