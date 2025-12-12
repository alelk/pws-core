package io.github.alelk.pws.domain.history.repository

import io.github.alelk.pws.domain.core.ids.SongNumberId

/**
 * Mutation operations for History.
 */
interface HistoryWriteRepository {
  /**
   * Add or update entry for a song view.
   * If entry exists, updates viewedAt. Otherwise creates new entry.
   * @return ID of the history entry.
   */
  suspend fun recordView(songNumberId: SongNumberId): Long

  /**
   * Remove a single history entry.
   */
  suspend fun remove(id:  SongNumberId): Boolean

  /**
   * Clear all history.
   * @return Number of entries removed.
   */
  suspend fun clearAll(): Int
}

