package io.github.alelk.pws.domain.book.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.book.repository.BookWriteRepository
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner

/** Use case: delete a book. */
class DeleteBookUseCase(
  private val writeRepository: BookWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(bookId: BookId): Either<DeleteError, BookId> =
    txRunner.inRwTransaction { writeRepository.delete(bookId) }
}
