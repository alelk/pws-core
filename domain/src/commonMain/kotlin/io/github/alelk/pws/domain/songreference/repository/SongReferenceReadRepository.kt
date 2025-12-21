package io.github.alelk.pws.domain.songreference.repository

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.songreference.model.SongReference

/**
 * Read operations for Song-to-Song references.
 */
interface SongReferenceReadRepository {
  /**
   * Get a specific song reference.
   */
  suspend fun get(songId: SongId, refSongId: SongId): SongReference?

  /**
   * Get all references for a song (songs that this song references).
   */
  suspend fun getReferencesForSong(songId: SongId): List<SongReference>

  /**
   * Get all references to a song (songs that reference this song).
   */
  suspend fun getReferencesToSong(refSongId: SongId): List<SongReference>

  /**
   * Check if a song reference exists.
   */
  suspend fun exists(songId: SongId, refSongId: SongId): Boolean

  /**
   * Count all song references.
   */
  suspend fun count(): Long
}

