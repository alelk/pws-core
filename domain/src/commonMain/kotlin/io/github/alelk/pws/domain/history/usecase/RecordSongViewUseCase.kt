package io.github.alelk.pws.domain.history.usecase

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.history.repository.HistoryWriteRepository

/**
 * Use case: record a song view in history.
 */
class RecordSongViewUseCase(
  private val historyRepository: HistoryWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(songNumberId: SongNumberId): Long =
    txRunner.inRwTransaction { historyRepository.recordView(songNumberId) }
}

