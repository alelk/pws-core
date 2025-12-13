package io.github.alelk.pws.domain.book.usecase

import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.book.repository.UserBookReadRepository
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner

/**
 * Get all user-created books.
 */
class GetUserBooksUseCase(
  private val readRepository: UserBookReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId): List<BookSummary> =
    txRunner.inRoTransaction { readRepository.getAllBooks(userId) }
}

