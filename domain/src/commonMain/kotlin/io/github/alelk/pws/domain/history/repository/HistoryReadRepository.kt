package io.github.alelk.pws.domain.history.repository

import io.github.alelk.pws.domain.history.model.HistoryEntry
import io.github.alelk.pws.domain.history.model.HistorySubject

/**
 * Read operations for History (single fetch, no reactive stream).
 */
interface HistoryReadRepository {
  /**
   * Get history entries ordered by viewedAt desc.
   * @param limit Maximum number of entries to return (null = no limit).
   * @param offset Number of entries to skip (default = 0).
   */
  suspend fun getAll(limit: Int? = null, offset: Int = 0): List<HistoryEntry>

  /**
   * Get view count for a song.
   */
  suspend fun getViewCount(subject: HistorySubject): Int

  /**
   * Get total number of history entries.
   */
  suspend fun count(): Long
}
