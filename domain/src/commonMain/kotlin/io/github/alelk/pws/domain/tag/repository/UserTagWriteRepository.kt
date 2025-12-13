package io.github.alelk.pws.domain.tag.repository

import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.tag.command.CreateTagCommand
import io.github.alelk.pws.domain.tag.command.UpdateTagCommand

/**
 * Write operations for user-personalized tags.
 */
interface UserTagWriteRepository {

  // ============ User custom tags operations ============

  /**
   * Create a new custom tag for user.
   */
  suspend fun createUserTag(userId: UserId, command: CreateTagCommand): CreateResourceResult<TagId>

  /**
   * Update user's custom tag.
   */
  suspend fun updateUserTag(userId: UserId, command: UpdateTagCommand): UpdateResourceResult<TagId>

  /**
   * Delete user's custom tag.
   * Also removes all song-tag associations for this tag.
   */
  suspend fun deleteUserTag(userId: UserId, tagId: TagId): DeleteResourceResult<TagId>

  // ============ Global tag override operations ============

  /**
   * Hide a global tag for user.
   * The tag won't appear in user's tag list or song tags.
   */
  suspend fun hideGlobalTag(userId: UserId, tagId: TagId): UpdateResourceResult<TagId>

  /**
   * Unhide a previously hidden global tag.
   */
  suspend fun unhideGlobalTag(userId: UserId, tagId: TagId): UpdateResourceResult<TagId>

  /**
   * Override color for a global tag (user-specific).
   */
  suspend fun overrideGlobalTagColor(userId: UserId, tagId: TagId, color: Color): UpdateResourceResult<TagId>

  /**
   * Override priority for a global tag (user-specific).
   */
  suspend fun overrideGlobalTagPriority(userId: UserId, tagId: TagId, priority: Int): UpdateResourceResult<TagId>

  /**
   * Reset all overrides for a global tag (restore to global defaults).
   */
  suspend fun resetGlobalTagOverride(userId: UserId, tagId: TagId): UpdateResourceResult<TagId>

  // ============ Song-tag association operations ============

  /**
   * Add a tag to a song for user.
   * Works for both custom and global tags.
   * For global tags: if association doesn't exist globally, creates user-specific association.
   * For custom tags: creates association in user_song_tags.
   * @param tagId Tag ID (can be global or custom tag).
   */
  suspend fun addTagToSong(userId: UserId, songId: SongId, tagId: TagId): CreateResourceResult<TagId>

  /**
   * Remove a tag from a song for user.
   * For global tags: hides the global association (doesn't delete it).
   * For custom tags: removes the association.
   * For user-added global tags: removes the user addition.
   */
  suspend fun removeTagFromSong(userId: UserId, songId: SongId, tagId: TagId): DeleteResourceResult<TagId>

  /**
   * Hide a global song-tag association for user.
   */
  suspend fun hideGlobalSongTag(userId: UserId, songId: SongId, tagId: TagId): UpdateResourceResult<TagId>

  /**
   * Unhide a previously hidden global song-tag association.
   */
  suspend fun unhideGlobalSongTag(userId: UserId, songId: SongId, tagId: TagId): UpdateResourceResult<TagId>
}

