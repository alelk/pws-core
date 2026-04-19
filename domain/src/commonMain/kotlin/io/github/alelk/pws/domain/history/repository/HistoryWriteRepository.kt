package io.github.alelk.pws.domain.history.repository

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.ClearError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpsertError
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
  suspend fun recordView(subject: HistorySubject): Either<UpsertError, HistoryEntry>

  /**
   * Remove a history entry.
   */
  suspend fun remove(subject: HistorySubject): Either<DeleteError, HistorySubject>

  /**
   * Clear all history.
   */
  suspend fun clearAll(): Either<ClearError, Int>
}
