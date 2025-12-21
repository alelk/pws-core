package io.github.alelk.pws.domain.songreference.repository

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.songreference.command.CreateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.command.UpdateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.model.SongReference

/**
 * Write operations for Song-to-Song references.
 */
interface SongReferenceWriteRepository {
  /**
   * Create a new song reference.
   */
  suspend fun create(command: CreateSongReferenceCommand): CreateResourceResult<SongReference>

  /**
   * Update an existing song reference.
   */
  suspend fun update(command: UpdateSongReferenceCommand): UpdateResourceResult<SongReference>

  /**
   * Delete a song reference.
   */
  suspend fun delete(songId: SongId, refSongId: SongId): DeleteResourceResult<SongReference>

  /**
   * Delete all references for a song.
   */
  suspend fun deleteAllForSong(songId: SongId): Int
}

