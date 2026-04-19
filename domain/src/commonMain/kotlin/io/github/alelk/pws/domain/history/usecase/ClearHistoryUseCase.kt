package io.github.alelk.pws.domain.history.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.ClearError
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.history.repository.HistoryWriteRepository

/**
 * Use case: clear all history entries.
 */
class ClearHistoryUseCase(
  private val historyRepository: HistoryWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(): Either<ClearError, Int> =
    txRunner.inRwTransaction { historyRepository.clearAll() }
}
