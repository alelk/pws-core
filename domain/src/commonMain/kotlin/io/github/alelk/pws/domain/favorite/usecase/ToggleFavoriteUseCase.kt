package io.github.alelk.pws.domain.favorite.usecase

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.favorite.repository.FavoriteWriteRepository

/**
 * Use case: toggle favorite status for a song.
 */
class ToggleFavoriteUseCase(
  private val favoriteRepository: FavoriteWriteRepository,
  private val txRunner: TransactionRunner
) {
  /**
   * @return true if song is now favorite, false if removed from favorites.
   */
  suspend operator fun invoke(songNumberId: SongNumberId): Boolean =
    txRunner.inRwTransaction { favoriteRepository.toggle(songNumberId) }
}

