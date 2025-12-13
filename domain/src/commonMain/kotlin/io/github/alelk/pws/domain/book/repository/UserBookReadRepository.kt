package io.github.alelk.pws.domain.book.repository

import io.github.alelk.pws.domain.book.model.BookDetail
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.UserId

/**
 * Read operations for user-created books.
 */
interface UserBookReadRepository {
  /**
   * Get user's book by ID.
   */
  suspend fun getBook(userId: UserId, bookId: BookId): BookDetail?

  /**
   * Get all user's books.
   */
  suspend fun getAllBooks(userId: UserId): List<BookSummary>

  /**
   * Count user's books.
   */
  suspend fun countBooks(userId: UserId): Long

  /**
   * Check if book belongs to user.
   */
  suspend fun isUserBook(userId: UserId, bookId: BookId): Boolean
}

