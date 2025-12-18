package io.github.alelk.pws.domain.songtag.repository

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.ReplaceAllResourcesResult
import io.github.alelk.pws.domain.songtag.model.SongTagAssociation

/**
 * Mutation operations for Song-Tag associations.
 */
interface SongTagWriteRepository {
  /**
   * Add a tag to a song.
   */
  suspend fun addTagToSong(songId: SongId, tagId: TagId): CreateResourceResult<SongTagAssociation>

  /**
   * Remove a tag from a song.
   */
  suspend fun removeTagFromSong(songId: SongId, tagId: TagId): DeleteResourceResult<SongTagAssociation>

  /**
   * Set all tags for a song (replaces existing).
   * Returns details about created, unchanged and deleted associations.
   */
  suspend fun setTagsForSong(songId: SongId, tagIds: Set<TagId>): ReplaceAllResourcesResult<SongTagAssociation>
}

