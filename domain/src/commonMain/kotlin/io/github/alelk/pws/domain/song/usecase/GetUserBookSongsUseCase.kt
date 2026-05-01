package io.github.alelk.pws.domain.song.usecase

import arrow.core.Either
import arrow.core.right
import io.github.alelk.pws.domain.core.error.ReadError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.song.repository.UserBookSongReadRepository

/**
 * Get all songs in a user book.
 */
class GetUserBookSongsUseCase(
  private val readRepository: UserBookSongReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, bookId: BookId): Either<ReadError, List<SongSummary>> =
    txRunner.inRoTransaction { readRepository.getSongsByBook(userId, bookId).right() }
}
