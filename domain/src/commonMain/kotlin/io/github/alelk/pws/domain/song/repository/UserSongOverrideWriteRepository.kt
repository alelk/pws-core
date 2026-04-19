package io.github.alelk.pws.domain.song.repository

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.ClearError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.song.command.OverrideSongCommand

/** Write operations for user song overrides. */
interface UserSongOverrideWriteRepository {
  /** Creates or updates the override. */
  suspend fun overrideSong(userId: UserId, command: OverrideSongCommand): Either<UpdateError, SongId>

  /** Reset all overrides for a specific song. */
  suspend fun resetOverrides(userId: UserId, songId: SongId): Either<DeleteError, SongId>

  /** Clear all song overrides for user. */
  suspend fun clearAllOverrides(userId: UserId): Either<ClearError, Int>
}
