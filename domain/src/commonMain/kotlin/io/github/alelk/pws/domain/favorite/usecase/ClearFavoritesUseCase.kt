package io.github.alelk.pws.domain.favorite.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.ClearError
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.favorite.repository.FavoriteWriteRepository

/**
 * Use case: clear all favorites.
 */
class ClearFavoritesUseCase(
  private val favoriteRepository: FavoriteWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(): Either<ClearError, Int> =
    txRunner.inRwTransaction { favoriteRepository.clearAll() }
}
