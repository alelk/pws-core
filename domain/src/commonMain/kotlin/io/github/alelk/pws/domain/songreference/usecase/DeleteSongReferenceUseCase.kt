package io.github.alelk.pws.domain.songreference.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.ids.SongId
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
  suspend operator fun invoke(songId: SongId, refSongId: SongId): Either<DeleteError, SongReference> =
    txRunner.inRwTransaction { repository.delete(songId, refSongId) }
}
