package io.github.alelk.pws.api.client.repository

import io.github.alelk.pws.api.client.api.BookApi
import io.github.alelk.pws.api.mapping.book.toCreateRequestDto
import io.github.alelk.pws.api.mapping.book.toUpdateRequestDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.book.command.CreateBookCommand
import io.github.alelk.pws.domain.book.command.UpdateBookCommand
import io.github.alelk.pws.domain.book.repository.BookWriteRepository

class RemoteBookWriteRepository(private val api: BookApi) : BookWriteRepository {

  override suspend fun create(bookCommand: CreateBookCommand) {
    api.create(bookCommand.toCreateRequestDto())
  }

  override suspend fun update(bookCommand: UpdateBookCommand): Boolean {
    api.update(bookCommand.id.toDto(), bookCommand.toUpdateRequestDto())
    return true
  }
}