package io.github.alelk.pws.domain.song.usecase

import arrow.core.Either
import arrow.core.flatMap
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.command.OverrideSongCommand
import io.github.alelk.pws.domain.song.model.MergedSongDetail
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.repository.UserSongOverrideWriteRepository

/**
 * Override a global song for user.
 *
 * This use case:
 * 1. Validates the song exists
 * 2. Creates or updates user's override
 * 3. Returns the merged song with overrides applied
 */
class OverrideSongUseCase(
  private val songReadRepository: SongReadRepository,
  private val overrideWriteRepository: UserSongOverrideWriteRepository,
  private val getMergedSongDetail: GetMergedSongDetailUseCase,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, command: OverrideSongCommand): Either<UpdateError, MergedSongDetail> {
    txRunner.inRoTransaction { songReadRepository.get(command.songId) }
      ?: return Either.Left(UpdateError.NotFound)

    if (!command.hasChanges()) {
      return Either.Left(UpdateError.ValidationError("No changes specified in override command"))
    }

    return txRunner.inRwTransaction {
      overrideWriteRepository.overrideSong(userId, command).flatMap {
        getMergedSongDetail(userId, command.songId)?.let { Either.Right(it) }
          ?: Either.Left(UpdateError.UnknownError(null, "Failed to get merged song after override"))
      }
    }
  }
}
