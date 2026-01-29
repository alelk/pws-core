package io.github.alelk.pws.domain.history.usecase

import io.github.alelk.pws.domain.core.result.UpsertResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.history.model.HistoryEntry
import io.github.alelk.pws.domain.history.model.HistorySubject
import io.github.alelk.pws.domain.history.repository.HistoryWriteRepository

/**
 * Use case: record a song view in history.
 */
class RecordSongViewUseCase(
  private val historyRepository: HistoryWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(subject: HistorySubject): UpsertResourceResult<HistoryEntry> =
    txRunner.inRwTransaction { historyRepository.recordView(subject) }
}

