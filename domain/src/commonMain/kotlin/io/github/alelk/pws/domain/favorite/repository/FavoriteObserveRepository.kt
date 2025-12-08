package io.github.alelk.pws.domain.favorite.repository

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.favorite.model.FavoriteWithSongInfo
import kotlinx.coroutines.flow.Flow

/**
 * Observe operations for Favorites.
 */
interface FavoriteObserveRepository {
  /**
   * Observe all favorites ordered by addedAt desc.
   */
  fun observeAll(): Flow<List<FavoriteWithSongInfo>>

  /**
   * Check if a song is in favorites.
   */
  fun observeIsFavorite(songNumberId: SongNumberId): Flow<Boolean>
}

