package io.github.alelk.pws.domain.book.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.book.repository.BookWriteRepository
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner

/** Use case: update a book (patch semantics). */
class UpdateBookUseCase(
  private val writeRepository: BookWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(command: UpdateBookCommand): Either<UpdateError, BookId> =
    txRunner.inRwTransaction { writeRepository.update(command) }
}
