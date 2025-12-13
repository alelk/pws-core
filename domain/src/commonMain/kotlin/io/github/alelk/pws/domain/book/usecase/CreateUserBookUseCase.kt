package io.github.alelk.pws.domain.book.usecase

import io.github.alelk.pws.domain.book.command.CreateBookCommand
import io.github.alelk.pws.domain.book.repository.UserBookWriteRepository
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner

/**
 * Create a new user book.
 */
class CreateUserBookUseCase(
  private val writeRepository: UserBookWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, command: CreateBookCommand): CreateResourceResult<BookId> =
    txRunner.inRwTransaction { writeRepository.createBook(userId, command) }
}

