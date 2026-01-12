package io.github.alelk.pws.domain.song.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.repository.UserSongOverrideWriteRepository

/**
 * Reset user's overrides for a song (restore to global version).
 */
class ResetSongOverrideUseCase(
  private val overrideWriteRepository: UserSongOverrideWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, songId: SongId): DeleteResourceResult<SongId> =
    txRunner.inRwTransaction {
      overrideWriteRepository.resetOverrides(userId, songId)
    }
}

