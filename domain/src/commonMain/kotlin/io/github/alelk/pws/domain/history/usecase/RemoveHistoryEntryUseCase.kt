package io.github.alelk.pws.domain.history.usecase

import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.history.repository.HistoryWriteRepository

/**
 * Use case: remove a single history entry.
 */
class RemoveHistoryEntryUseCase(
  private val historyRepository: HistoryWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(id: Long): Boolean =
    txRunner.inRwTransaction { historyRepository.remove(id) }
}

