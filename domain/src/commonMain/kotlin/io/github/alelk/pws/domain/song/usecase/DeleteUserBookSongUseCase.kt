package io.github.alelk.pws.domain.song.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.repository.UserBookSongWriteRepository

/**
 * Delete a song from a user book.
 */
class DeleteUserBookSongUseCase(
  private val writeRepository: UserBookSongWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, songId: SongId): Either<DeleteError, SongId> =
    txRunner.inRwTransaction { writeRepository.deleteSong(userId, songId) }
}
