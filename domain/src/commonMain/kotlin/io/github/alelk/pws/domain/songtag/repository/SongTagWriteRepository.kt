package io.github.alelk.pws.domain.songtag.repository

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId

/**
 * Mutation operations for Song-Tag associations.
 */
interface SongTagWriteRepository {
  /**
   * Add a tag to a song.
   * @return true if added, false if already exists.
   */
  suspend fun addTagToSong(songNumberId: SongNumberId, tagId: TagId): Boolean

  /**
   * Remove a tag from a song.
   * @return true if removed, false if not found.
   */
  suspend fun removeTagFromSong(songNumberId: SongNumberId, tagId: TagId): Boolean

  /**
   * Set all tags for a song (replaces existing).
   */
  suspend fun setTagsForSong(songNumberId: SongNumberId, tagIds: Set<TagId>)
}

