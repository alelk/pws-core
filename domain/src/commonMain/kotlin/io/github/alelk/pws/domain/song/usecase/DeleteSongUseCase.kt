package io.github.alelk.pws.domain.song.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.repository.SongWriteRepository

/** Use case: delete a song. */
class DeleteSongUseCase(
  private val writeRepository: SongWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(songId: SongId): Either<DeleteError, SongId> =
    txRunner.inRwTransaction { writeRepository.delete(songId) }
}
