package io.github.alelk.pws.domain.songtag.repository

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.songtag.model.SongTagAssociation

/**
 * Mutation operations for Song-Tag associations.
 * @param ID The type of TagId this repository works with
 */
interface SongTagWriteRepository<ID : TagId> {
  /**
   * Add a tag to a song.
   */
  suspend fun create(songId: SongId, tagId: ID): CreateResourceResult<SongTagAssociation<ID>>

  /**
   * Remove a tag from a song.
   */
  suspend fun delete(songId: SongId, tagId: ID): DeleteResourceResult<SongTagAssociation<ID>>
}

