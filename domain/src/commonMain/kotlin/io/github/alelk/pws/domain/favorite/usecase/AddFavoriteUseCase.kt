package io.github.alelk.pws.domain.favorite.usecase

import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.favorite.model.Favorite
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.repository.FavoriteWriteRepository

/**
 * Use case: add song to favorites.
 */
class AddFavoriteUseCase(
  private val favoriteRepository: FavoriteWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(subject: FavoriteSubject): CreateResourceResult<Favorite> =
    txRunner.inRwTransaction { favoriteRepository.add(subject) }
}
