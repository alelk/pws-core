package io.github.alelk.pws.api.client.repository

import arrow.core.Either
import io.github.alelk.pws.api.client.api.AdminBookApi
import io.github.alelk.pws.api.mapping.book.songnumber.toDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.core.error.BulkCreateError
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.songnumber.model.SongNumberLink
import io.github.alelk.pws.domain.songnumber.repository.SongNumberWriteRepository

class RemoteSongNumberWriteRepository(val bookApi: AdminBookApi) : SongNumberWriteRepository {
  override suspend fun create(bookId: BookId, link: SongNumberLink): Either<CreateError, SongNumberLink> =
    bookApi.addSongs(bookId.toDto(), listOf(link.toDto()))
      .map { link }
      .mapLeft { err ->
        when (err) {
          is BulkCreateError.AlreadyExists -> CreateError.AlreadyExists()
          is BulkCreateError.ValidationError -> CreateError.ValidationError(err.message)
          is BulkCreateError.UnknownError -> CreateError.UnknownError(err.cause)
        }
      }

  override suspend fun update(bookId: BookId, link: SongNumberLink): Either<UpdateError, SongNumberLink> {
    TODO("Not yet implemented")
  }

  override suspend fun delete(bookId: BookId, songId: SongId): Either<DeleteError, SongNumberId> {
    TODO("Not yet implemented")
  }
}