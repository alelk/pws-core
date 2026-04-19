package io.github.alelk.pws.domain.book.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.book.repository.UserBookWriteRepository
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner

/**
 * Delete a user book and all its songs.
 */
class DeleteUserBookUseCase(
  private val writeRepository: UserBookWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, bookId: BookId): Either<DeleteError, BookId> =
    txRunner.inRwTransaction { writeRepository.deleteBook(userId, bookId) }
}
