package io.github.alelk.pws.domain.song.usecase

import arrow.core.Either
import arrow.core.right
import io.github.alelk.pws.domain.core.error.ReadError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.repository.UserSongOverrideReadRepository

/**
 * Get list of song IDs that have user overrides.
 */
class GetOverriddenSongIdsUseCase(
  private val overrideReadRepository: UserSongOverrideReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId): Either<ReadError, List<SongId>> = txRunner.inRoTransaction {
    overrideReadRepository.getOverriddenSongIds(userId).right()
  }
}

