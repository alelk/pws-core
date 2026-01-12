package io.github.alelk.pws.domain.song.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
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
  suspend operator fun invoke(
    userId: UserId,
    command: OverrideSongCommand
  ): UpdateResourceResult<MergedSongDetail> {
    // Verify song exists
    val song = txRunner.inRoTransaction { songReadRepository.get(command.songId) }
      ?: return UpdateResourceResult.NotFound(placeholder(command.songId))

    // Check if command has any changes
    if (!command.hasChanges()) {
      return UpdateResourceResult.ValidationError(placeholder(command.songId), "No changes specified in override command")
    }

    // Apply override
    val overrideResult = txRunner.inRwTransaction {
      overrideWriteRepository.overrideSong(userId, command)
    }

    return when (overrideResult) {
      is UpdateResourceResult.Success -> {
        // Get the merged song to return
        val merged = getMergedSongDetail(userId, command.songId)
          ?: return UpdateResourceResult.UnknownError(placeholder(command.songId), null, "Failed to get merged song after override")
        UpdateResourceResult.Success(merged)
      }
      is UpdateResourceResult.NotFound -> UpdateResourceResult.NotFound(placeholder(command.songId))
      is UpdateResourceResult.ValidationError -> UpdateResourceResult.ValidationError(placeholder(command.songId), overrideResult.message)
      is UpdateResourceResult.UnknownError -> UpdateResourceResult.UnknownError(placeholder(command.songId), overrideResult.exception, overrideResult.message)
    }
  }

  /** Create a placeholder MergedSongDetail for error cases */
  private fun placeholder(songId: SongId): MergedSongDetail = MergedSongDetail(
    id = songId,
    version = io.github.alelk.pws.domain.core.Version(0, 0),
    locale = io.github.alelk.pws.domain.core.Locale.EN,
    name = io.github.alelk.pws.domain.core.NonEmptyString("unknown"),
    lyric = io.github.alelk.pws.domain.song.lyric.Lyric(emptyList()),
    source = io.github.alelk.pws.domain.song.model.SongSource.GLOBAL
  )
}

