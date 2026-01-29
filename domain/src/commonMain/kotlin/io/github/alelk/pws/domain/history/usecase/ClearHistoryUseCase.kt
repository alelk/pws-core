package io.github.alelk.pws.domain.history.usecase

import io.github.alelk.pws.domain.core.result.ClearResourcesResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.history.repository.HistoryWriteRepository

/**
 * Use case: clear all history entries.
 */
class ClearHistoryUseCase(
  private val historyRepository: HistoryWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(): ClearResourcesResult =
    txRunner.inRwTransaction { historyRepository.clearAll() }
}

