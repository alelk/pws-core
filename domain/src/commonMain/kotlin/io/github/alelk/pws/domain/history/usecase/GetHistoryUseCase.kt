package io.github.alelk.pws.domain.history.usecase

import arrow.core.Either
import arrow.core.right
import io.github.alelk.pws.domain.core.error.ReadError
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.history.model.HistoryEntry
import io.github.alelk.pws.domain.history.repository.HistoryReadRepository

/**
 * Use case: get history entries (single fetch, for API/backend).
 */
class GetHistoryUseCase(
  private val historyRepository: HistoryReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(limit: Int? = null, offset: Int = 0): Either<ReadError, List<HistoryEntry>> =
    txRunner.inRoTransaction { historyRepository.getAll(limit, offset).right() }
}
