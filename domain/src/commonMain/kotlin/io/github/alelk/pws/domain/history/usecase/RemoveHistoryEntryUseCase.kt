package io.github.alelk.pws.domain.history.usecase

import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.history.model.HistorySubject
import io.github.alelk.pws.domain.history.repository.HistoryWriteRepository

/**
 * Use case: remove a single history entry.
 */
class RemoveHistoryEntryUseCase(
  private val historyRepository: HistoryWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(subject: HistorySubject): DeleteResourceResult<HistorySubject> =
    txRunner.inRwTransaction { historyRepository.remove(subject) }
}

