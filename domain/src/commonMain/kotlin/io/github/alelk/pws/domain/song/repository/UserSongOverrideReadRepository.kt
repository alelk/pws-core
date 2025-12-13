package io.github.alelk.pws.domain.song.repository

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.song.model.SongDetail

/**
 * Read operations for user song overrides.
 * Returns songs with user's overrides applied.
 */
interface UserSongOverrideReadRepository {
  /**
   * Get song with user's overrides applied.
   * Returns null if song doesn't exist.
   */
  suspend fun getSongWithOverrides(userId: UserId, songId: SongId): SongDetail?

  /**
   * Get all song IDs that have overrides for this user.
   */
  suspend fun getOverriddenSongIds(userId: UserId): List<SongId>

  /**
   * Check if user has any overrides for this song.
   */
  suspend fun hasOverrides(userId: UserId, songId: SongId): Boolean
}

