package io.github.alelk.pws.api.client.repository

import io.github.alelk.pws.api.client.api.BookApi
import io.github.alelk.pws.api.client.api.ResourceCreateResult
import io.github.alelk.pws.api.client.api.ResourceUpdateResult
import io.github.alelk.pws.api.mapping.book.toCreateRequestDto
import io.github.alelk.pws.api.mapping.book.toUpdateRequestDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.book.command.CreateBookCommand
import io.github.alelk.pws.domain.book.command.CreateBookResult
import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.book.command.UpdateBookResult
import io.github.alelk.pws.domain.book.repository.BookWriteRepository

class RemoteBookWriteRepository(private val api: BookApi) : BookWriteRepository {

  override suspend fun create(bookCommand: CreateBookCommand): CreateBookResult =
    runCatching {
      when (val result = api.create(bookCommand.toCreateRequestDto())) {
        is ResourceCreateResult.AlreadyExists<*> -> CreateBookResult.AlreadyExists(bookCommand.id)
        is ResourceCreateResult.Success<*> -> CreateBookResult.Success(bookCommand.id)
        is ResourceCreateResult.ValidationError -> CreateBookResult.ValidationError(result.message)
      }
    }.getOrElse { exc -> CreateBookResult.UnknownError(exc.message ?: "Unknown error: ${exc::class}", exc) }

  override suspend fun update(bookCommand: UpdateBookCommand): UpdateBookResult =
    runCatching {
      when (val result = api.update(bookCommand.id.toDto(), bookCommand.toUpdateRequestDto())) {
        is ResourceUpdateResult.NotFound<*> -> UpdateBookResult.NotFound(bookCommand.id)
        is ResourceUpdateResult.Success<*> -> UpdateBookResult.Success(bookCommand.id)
        is ResourceUpdateResult.ValidationError -> UpdateBookResult.ValidationError(result.message)
      }
    }.getOrElse { exc -> UpdateBookResult.UnknownError(exc.message ?: "Unknown error: ${exc::class}", exc) }
}
