package io.github.alelk.pws.domain.favorite.usecase

import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.repository.FavoriteWriteRepository

/**
 * Use case: remove song from favorites.
 */
class RemoveFavoriteUseCase(
  private val favoriteRepository: FavoriteWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(subject: FavoriteSubject): DeleteResourceResult<FavoriteSubject> =
    txRunner.inRwTransaction { favoriteRepository.remove(subject) }
}

