package io.github.alelk.pws.domain.song.repository

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.ClearResourcesResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.song.command.OverrideSongCommand

/**
 * Write operations for user song overrides.
 */
interface UserSongOverrideWriteRepository {
  /**
   * Apply override to a global song for user.
   * Creates or updates the override.
   */
  suspend fun overrideSong(userId: UserId, command: OverrideSongCommand): UpdateResourceResult<SongId>

  /**
   * Reset all overrides for a specific song.
   */
  suspend fun resetOverrides(userId: UserId, songId: SongId): DeleteResourceResult<SongId>

  /**
   * Clear all song overrides for user.
   */
  suspend fun clearAllOverrides(userId: UserId): ClearResourcesResult
}

