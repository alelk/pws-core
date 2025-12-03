package io.github.alelk.pws.api.client.repository

import io.github.alelk.pws.api.client.api.BookApi
import io.github.alelk.pws.api.client.api.ResourceBatchCreateResult
import io.github.alelk.pws.api.mapping.book.songnumber.toDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.songnumber.model.SongNumberLink
import io.github.alelk.pws.domain.songnumber.repository.SongNumberWriteRepository

class RemoteSongNumberWriteRepository(val bookApi: BookApi) : SongNumberWriteRepository {
  override suspend fun create(bookId: BookId, link: SongNumberLink): CreateResourceResult<SongNumberLink> =
    runCatching {
      when (val result = bookApi.createBookSongs(bookId.toDto(), listOf(link.toDto()))) {
        is ResourceBatchCreateResult.AlreadyExists<*> -> CreateResourceResult.AlreadyExists(link)
        is ResourceBatchCreateResult.Success<*> -> CreateResourceResult.Success(link)
        is ResourceBatchCreateResult.ValidationError -> CreateResourceResult.ValidationError(link, message = result.message)
      }
    }.getOrElse { exc -> CreateResourceResult.UnknownError(link, exc) }

  override suspend fun update(
    bookId: BookId,
    link: SongNumberLink
  ): UpdateResourceResult<SongNumberLink> {
    TODO("Not yet implemented")
  }

  override suspend fun delete(
    bookId: BookId,
    songId: SongId
  ): DeleteResourceResult<SongNumberId> {
    TODO("Not yet implemented")
  }
}