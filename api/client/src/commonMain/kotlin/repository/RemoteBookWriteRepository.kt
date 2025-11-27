package io.github.alelk.pws.api.client.repository

import io.github.alelk.pws.api.client.api.BookApi
import io.github.alelk.pws.api.client.api.ResourceCreateResult
import io.github.alelk.pws.api.client.api.ResourceUpdateResult
import io.github.alelk.pws.api.mapping.book.toRequestDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.book.command.CreateBookCommand
import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.book.repository.BookWriteRepository
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.UpdateResourceResult

class RemoteBookWriteRepository(private val api: BookApi) : BookWriteRepository {

  override suspend fun create(bookCommand: CreateBookCommand): CreateResourceResult<BookId> =
    runCatching {
      when (val result = api.create(bookCommand.toRequestDto())) {
        is ResourceCreateResult.AlreadyExists<*> -> CreateResourceResult.AlreadyExists(bookCommand.id)
        is ResourceCreateResult.Success<*> -> CreateResourceResult.Success(bookCommand.id)
        is ResourceCreateResult.ValidationError -> CreateResourceResult.ValidationError(bookCommand.id, result.message)
      }
    }.getOrElse { exc -> CreateResourceResult.UnknownError(bookCommand.id, exc) }

  override suspend fun update(bookCommand: UpdateBookCommand): UpdateResourceResult<BookId> =
    runCatching {
      when (val result = api.update(bookCommand.id.toDto(), bookCommand.toRequestDto())) {
        is ResourceUpdateResult.NotFound<*> -> UpdateResourceResult.NotFound(bookCommand.id)
        is ResourceUpdateResult.Success<*> -> UpdateResourceResult.Success(bookCommand.id)
        is ResourceUpdateResult.ValidationError -> UpdateResourceResult.ValidationError(bookCommand.id, result.message)
      }
    }.getOrElse { exc -> UpdateResourceResult.UnknownError(bookCommand.id, exc) }

  override suspend fun delete(bookId: BookId): DeleteResourceResult<BookId> = TODO("not implemented in api")
}
