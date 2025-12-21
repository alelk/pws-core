package io.github.alelk.pws.domain.songreference.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songreference.model.SongReference
import io.github.alelk.pws.domain.songreference.repository.SongReferenceWriteRepository

/**
 * Use case: delete a song reference.
 */
class DeleteSongReferenceUseCase(
  private val repository: SongReferenceWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(songId: SongId, refSongId: SongId): DeleteResourceResult<SongReference> =
    txRunner.inRwTransaction { repository.delete(songId, refSongId) }
}

