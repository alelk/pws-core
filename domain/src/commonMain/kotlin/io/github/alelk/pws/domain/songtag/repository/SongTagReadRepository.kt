package io.github.alelk.pws.domain.songtag.repository

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.songtag.model.SongWithBookInfo
import io.github.alelk.pws.domain.tag.Tag

/**
 * Read operations for Song-Tag associations.
 */
interface SongTagReadRepository {
  /**
   * Get all songs for a specific tag.
   */
  suspend fun getSongsByTag(tagId: TagId): List<SongWithBookInfo>

  /**
   * Get all tags for a specific song.
   */
  suspend fun getTagsForSong(songId: SongId): List<Tag>

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

