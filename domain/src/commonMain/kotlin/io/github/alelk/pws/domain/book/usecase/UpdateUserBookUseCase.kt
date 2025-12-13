package io.github.alelk.pws.domain.book.usecase

import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.book.repository.UserBookWriteRepository
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner

/**
 * Update a user book.
 */
class UpdateUserBookUseCase(
  private val writeRepository: UserBookWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, command: UpdateBookCommand): UpdateResourceResult<BookId> =
    txRunner.inRwTransaction { writeRepository.updateBook(userId, command) }
}

