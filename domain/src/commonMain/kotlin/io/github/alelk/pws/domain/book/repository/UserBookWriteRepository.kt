package io.github.alelk.pws.domain.book.repository

import io.github.alelk.pws.domain.book.command.CreateBookCommand
import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.UpdateResourceResult

/**
 * Write operations for user-created books.
 */
interface UserBookWriteRepository {
  /**
   * Create a new user book.
   */
  suspend fun createBook(userId: UserId, command: CreateBookCommand): CreateResourceResult<BookId>

  /**
   * Update user's book.
   */
  suspend fun updateBook(userId: UserId, command: UpdateBookCommand): UpdateResourceResult<BookId>

  /**
   * Delete user's book and all its songs.
   */
  suspend fun deleteBook(userId: UserId, bookId: BookId): DeleteResourceResult<BookId>
}

