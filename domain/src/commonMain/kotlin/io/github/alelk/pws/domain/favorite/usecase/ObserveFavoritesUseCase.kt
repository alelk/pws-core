package io.github.alelk.pws.domain.favorite.usecase

import io.github.alelk.pws.domain.favorite.model.FavoriteSong
import io.github.alelk.pws.domain.favorite.repository.FavoriteObserveRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case: observe all favorite songs.
 */
class ObserveFavoritesUseCase(
  private val favoriteRepository: FavoriteObserveRepository
) {
  operator fun invoke(): Flow<List<FavoriteSong>> =
    favoriteRepository.observeAll()
}

