package io.github.alelk.pws.domain.favorite.usecase

import io.github.alelk.pws.domain.core.result.ClearResourcesResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.favorite.repository.FavoriteWriteRepository

/**
 * Use case: clear all favorites.
 */
class ClearFavoritesUseCase(
  private val favoriteRepository: FavoriteWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(): ClearResourcesResult =
    txRunner.inRwTransaction { favoriteRepository.clearAll() }
}
