package io.github.alelk.pws.domain.songtag.repository

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.songtag.model.SongTagAssociation

/**
 * Mutation operations for Song-Tag associations.
 */
interface SongTagWriteRepository {
  /**
   * Add a tag to a song.
   */
  suspend fun create(songId: SongId, tagId: TagId): CreateResourceResult<SongTagAssociation>

  /**
   * Remove a tag from a song.
   */
  suspend fun delete(songId: SongId, tagId: TagId): DeleteResourceResult<SongTagAssociation>
}

