package io.github.alelk.pws.domain.history.usecase

import io.github.alelk.pws.domain.history.model.SongHistorySummary
import io.github.alelk.pws.domain.history.repository.HistoryObserveRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case: observe history entries.
 */
class ObserveHistoryUseCase(
  private val historyRepository: HistoryObserveRepository
) {
  operator fun invoke(limit: Int? = null): Flow<List<SongHistorySummary>> =
    historyRepository.observeAll(limit)
}

