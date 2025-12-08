package io.github.alelk.pws.domain.history.usecase

import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.history.repository.HistoryWriteRepository

/**
 * Use case: clear all history entries.
 */
class ClearHistoryUseCase(
  private val historyRepository: HistoryWriteRepository,
  private val txRunner: TransactionRunner
) {
  /**
   * @return Number of entries removed.
   */
  suspend operator fun invoke(): Int =
    txRunner.inRwTransaction { historyRepository.clearAll() }
}

