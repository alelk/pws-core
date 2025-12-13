package io.github.alelk.pws.domain.song.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.repository.UserBookSongWriteRepository

/**
 * Delete a song from a user book.
 */
class DeleteUserBookSongUseCase(
  private val writeRepository: UserBookSongWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, songId: SongId): DeleteResourceResult<SongId> =
    txRunner.inRwTransaction { writeRepository.deleteSong(userId, songId) }
}

