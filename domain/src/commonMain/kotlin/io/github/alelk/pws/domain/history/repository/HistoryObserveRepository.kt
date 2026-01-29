package io.github.alelk.pws.domain.history.repository

import io.github.alelk.pws.domain.history.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * Observe operations for History (reactive stream, for UI).
 */
interface HistoryObserveRepository {
  /**
   * Observe history entries ordered by viewedAt desc.
   * @param limit Maximum number of entries to return (null = no limit).
   * @param offset Number of entries to skip (default = 0).
   */
  fun observeAll(limit: Int? = null, offset: Int = 0): Flow<List<HistoryEntry>>
}

