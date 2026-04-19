package io.github.alelk.pws.domain.book.repository

import arrow.core.Either
import io.github.alelk.pws.domain.book.command.CreateBookCommand
import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.UserId

/** Write operations for user-created books. */
interface UserBookWriteRepository {
  suspend fun createBook(userId: UserId, command: CreateBookCommand): Either<CreateError, BookId>
  suspend fun updateBook(userId: UserId, command: UpdateBookCommand): Either<UpdateError, BookId>
  suspend fun deleteBook(userId: UserId, bookId: BookId): Either<DeleteError, BookId>
}
