package io.github.alelk.pws.domain.song.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.alelk.pws.domain.core.error.ReadError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.repository.UserBookSongReadRepository

/**
 * Get song detail from a user book.
 */
class GetUserBookSongDetailUseCase(
  private val readRepository: UserBookSongReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, songId: SongId): Either<ReadError, SongDetail> =
    txRunner.inRoTransaction { readRepository.getSong(userId, songId)?.right() ?: ReadError.NotFound().left() }
  
  suspend fun byNumber(userId: UserId, bookId: BookId, number: Int): Either<ReadError, SongDetail> =
    txRunner.inRoTransaction { readRepository.getSongByNumber(userId, bookId, number)?.right() ?: ReadError.NotFound().left() }
}
