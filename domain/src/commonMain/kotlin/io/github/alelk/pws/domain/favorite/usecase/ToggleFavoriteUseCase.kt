package io.github.alelk.pws.domain.favorite.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.ToggleError
import io.github.alelk.pws.domain.core.model.ToggleResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.repository.FavoriteWriteRepository

/**
 * Use case: toggle favorite status for a song.
 */
class ToggleFavoriteUseCase(
  private val favoriteRepository: FavoriteWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(subject: FavoriteSubject): Either<ToggleError, ToggleResult<FavoriteSubject>> =
    txRunner.inRwTransaction { favoriteRepository.toggle(subject) }
}
