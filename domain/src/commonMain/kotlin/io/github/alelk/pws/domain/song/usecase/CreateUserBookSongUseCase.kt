package io.github.alelk.pws.domain.song.usecase

import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.repository.UserBookSongWriteRepository

/**
 * Create a song in a user book.
 */
class CreateUserBookSongUseCase(
  private val writeRepository: UserBookSongWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, bookId: BookId, command: CreateSongCommand): CreateResourceResult<SongId> =
    txRunner.inRwTransaction { writeRepository.createSong(userId, bookId, command) }
}
