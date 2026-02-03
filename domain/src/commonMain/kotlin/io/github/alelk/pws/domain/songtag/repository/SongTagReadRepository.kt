package io.github.alelk.pws.domain.songtag.repository

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.songtag.model.SongWithBookInfo
import io.github.alelk.pws.domain.tag.model.Tag

/**
 * Read operations for Song-Tag associations.
 * @param ID The type of TagId this repository works with
 */
interface SongTagReadRepository<ID : TagId> {
  /**
   * Get all songs for a specific tag.
   */
  suspend fun getSongsByTag(tagId: ID): List<SongWithBookInfo>

  /**
   * Get all tags for a specific song.
   */
  suspend fun getTagsForSong(songId: SongId): List<Tag<ID>>

  /**
   * Get all tag IDs for a song.
   */
  suspend fun getTagIdsBySongId(songId: SongId): Set<ID>

  /**
   * Get all song IDs for a tag.
   */
  suspend fun getSongIdsByTagId(tagId: ID): Set<SongId>

  /**
   * Check if a song-tag association exists.
   */
  suspend fun exists(songId: SongId, tagId: ID): Boolean
}

