package io.github.alelk.pws.domain.book.usecase

import io.github.alelk.pws.domain.book.model.BookDetail
import io.github.alelk.pws.domain.book.repository.UserBookReadRepository
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner

/**
 * Get user-created book detail by ID.
 */
class GetUserBookDetailUseCase(
  private val readRepository: UserBookReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, bookId: BookId): BookDetail? =
    txRunner.inRoTransaction { readRepository.getBook(userId, bookId) }
}

