package io.github.alelk.pws.domain.favorite.usecase

import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.favorite.model.FavoriteSong
import io.github.alelk.pws.domain.favorite.repository.FavoriteReadRepository

/**
 * Use case: get favorite songs (for API/backend).
 */
class GetFavoritesUseCase(
  private val favoriteRepository: FavoriteReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(limit: Int? = null, offset: Int = 0): List<FavoriteSong> =
    txRunner.inRoTransaction { favoriteRepository.getAll(limit, offset) }
}
