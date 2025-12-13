package io.github.alelk.pws.domain.tag.repository

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.tag.Tag
import io.github.alelk.pws.domain.tag.model.TagDetail
import io.github.alelk.pws.domain.tag.model.TagSummary
import io.github.alelk.pws.domain.tag.query.TagSort

/**
 * Read operations for user-personalized tags.
 * Combines global tags with user overrides and custom tags.
 */
interface UserTagReadRepository {
  /**
   * Get all visible tags for user.
   * Includes: global tags (with overrides applied, excluding hidden) + user custom tags.
   */
  suspend fun getAllTags(userId: UserId, sort: TagSort = TagSort.ByPriority): List<TagSummary>

  /**
   * Get specific tag by id (with user overrides applied if applicable).
   * Returns null if tag doesn't exist or is hidden for this user.
   */
  suspend fun getTag(userId: UserId, tagId: TagId): TagDetail?

  /**
   * Get all tags for a song (for user's view).
   * Includes: global song-tags (with overrides, excluding hidden) + user's custom song-tags + user's added global tags.
   */
  suspend fun getSongTags(userId: UserId, songId: SongId): List<Tag>

  /**
   * Get all song IDs that have a specific tag (for user's view).
   */
  suspend fun getSongIdsByTag(userId: UserId, tagId: TagId, limit: Int? = null, offset: Int = 0): List<SongId>

  /**
   * Count songs with a specific tag (for user's view).
   */
  suspend fun countSongsByTag(userId: UserId, tagId: TagId): Long

  /**
   * Check if a tag is visible for user (not hidden).
   */
  suspend fun isTagVisible(userId: UserId, tagId: TagId): Boolean

  /**
   * Check if a song has a specific tag (for user's view).
   */
  suspend fun hasSongTag(userId: UserId, songId: SongId, tagId: TagId): Boolean
}

