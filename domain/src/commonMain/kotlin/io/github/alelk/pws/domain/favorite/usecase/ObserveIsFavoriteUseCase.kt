package io.github.alelk.pws.domain.favorite.usecase

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.favorite.repository.FavoriteObserveRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case: check if a song is in favorites.
 */
class ObserveIsFavoriteUseCase(
  private val favoriteRepository: FavoriteObserveRepository
) {
  operator fun invoke(songNumberId: SongNumberId): Flow<Boolean> =
    favoriteRepository.observeIsFavorite(songNumberId)
}

