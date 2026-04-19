package io.github.alelk.pws.domain.book.repository

import arrow.core.Either
import io.github.alelk.pws.domain.book.command.CreateBookCommand
import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.BookId

interface BookWriteRepository {
  suspend fun create(bookCommand: CreateBookCommand): Either<CreateError, BookId>
  suspend fun update(bookCommand: UpdateBookCommand): Either<UpdateError, BookId>
  suspend fun delete(bookId: BookId): Either<DeleteError, BookId>
}