package io.github.alelk.pws.domain.songtag.repository

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId

/**
 * Read operations for Song-Tag associations.
 */
interface SongTagReadRepository {
  /**
   * Get all tag IDs for a song.
   */
  suspend fun getTagIdsBySongId(songId: SongId): Set<TagId>

  /**
   * Get all song IDs for a tag.
   */
  suspend fun getSongIdsByTagId(tagId: TagId): Set<SongId>

  /**
   * Check if a song-tag association exists.
   */
  suspend fun exists(songId: SongId, tagId: TagId): Boolean
}

