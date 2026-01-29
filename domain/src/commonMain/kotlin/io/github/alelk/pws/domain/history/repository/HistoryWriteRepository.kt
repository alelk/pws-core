package io.github.alelk.pws.domain.history.repository

import io.github.alelk.pws.domain.core.result.ClearResourcesResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.UpsertResourceResult
import io.github.alelk.pws.domain.history.model.HistoryEntry
import io.github.alelk.pws.domain.history.model.HistorySubject

/**
 * Mutation operations for History.
 */
interface HistoryWriteRepository {
  /**
   * Record a view for a song.
   * If entry exists, updates viewedAt and increments viewCount.
   * Otherwise creates new entry.
   * @return Upserted history entry on success.
   */
  suspend fun recordView(subject: HistorySubject): UpsertResourceResult<HistoryEntry>

  /**
   * Remove a history entry.
   */
  suspend fun remove(subject: HistorySubject): DeleteResourceResult<HistorySubject>

  /**
   * Clear all history.
   */
  suspend fun clearAll(): ClearResourcesResult
}

