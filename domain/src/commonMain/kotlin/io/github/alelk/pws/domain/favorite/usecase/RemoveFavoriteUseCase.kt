package io.github.alelk.pws.domain.favorite.usecase

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.favorite.repository.FavoriteWriteRepository

/**
 * Use case: remove song from favorites.
 */
class RemoveFavoriteUseCase(
  private val favoriteRepository: FavoriteWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(songNumberId: SongNumberId): Boolean =
    txRunner.inRwTransaction { favoriteRepository.remove(songNumberId) }
}

