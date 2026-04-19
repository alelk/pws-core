package io.github.alelk.pws.domain.history.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.DeleteError
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
  suspend operator fun invoke(subject: HistorySubject): Either<DeleteError, HistorySubject> =
    txRunner.inRwTransaction { historyRepository.remove(subject) }
}
