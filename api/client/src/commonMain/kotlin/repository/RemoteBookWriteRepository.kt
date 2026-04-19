package io.github.alelk.pws.api.client.repository

import arrow.core.Either
import io.github.alelk.pws.api.client.api.AdminBookApi
import io.github.alelk.pws.api.mapping.book.toRequestDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.book.command.CreateBookCommand
import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.book.repository.BookWriteRepository
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.BookId

class RemoteBookWriteRepository(private val api: AdminBookApi) : BookWriteRepository {

  override suspend fun create(bookCommand: CreateBookCommand): Either<CreateError, BookId> =
    api.create(bookCommand.toRequestDto()).map { bookCommand.id }

  override suspend fun update(bookCommand: UpdateBookCommand): Either<UpdateError, BookId> =
    api.update(bookCommand.id.toDto(), bookCommand.toRequestDto()).map { bookCommand.id }

  override suspend fun delete(bookId: BookId): Either<DeleteError, BookId> =
    api.delete(bookId.toDto()).map { bookId }
}
