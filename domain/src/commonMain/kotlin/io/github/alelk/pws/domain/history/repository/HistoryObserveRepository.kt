package io.github.alelk.pws.domain.history.repository

import io.github.alelk.pws.domain.history.model.HistoryEntryWithSongInfo
import kotlinx.coroutines.flow.Flow

/**
 * Observe operations for History.
 */
interface HistoryObserveRepository {
  /**
   * Observe all history entries ordered by viewedAt desc.
   * @param limit Maximum number of entries to return (null = no limit).
   */
  fun observeAll(limit: Int? = null): Flow<List<HistoryEntryWithSongInfo>>
}

