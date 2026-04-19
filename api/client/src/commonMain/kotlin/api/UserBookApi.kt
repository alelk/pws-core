package io.github.alelk.pws.api.client.api

import arrow.core.Either
import io.github.alelk.pws.api.contract.book.BookCreateRequestDto
import io.github.alelk.pws.api.contract.book.BookDetailDto
import io.github.alelk.pws.api.contract.book.BookSortDto
import io.github.alelk.pws.api.contract.book.BookSummaryDto
import io.github.alelk.pws.api.contract.book.BookUpdateRequestDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SongCreateRequestDto
import io.github.alelk.pws.api.contract.song.SongDetailDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
import io.github.alelk.pws.api.contract.song.SongUpdateRequestDto
import io.github.alelk.pws.api.contract.userbook.UserBooks
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.patch
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

/**
 * User Book API client for managing user's personal books.
 * Requires user authentication.
 */
interface UserBookApi {
  // Book operations
  suspend fun listBooks(sort: BookSortDto? = null): List<BookSummaryDto>
  suspend fun getBook(id: BookIdDto): BookDetailDto?
  suspend fun createBook(request: BookCreateRequestDto): Either<CreateError, BookIdDto>
  suspend fun updateBook(id: BookIdDto, request: BookUpdateRequestDto): Either<UpdateError, BookIdDto>
  suspend fun deleteBook(id: BookIdDto): Either<DeleteError, BookIdDto>

  // Song operations
  suspend fun listSongs(bookId: BookIdDto): List<SongSummaryDto>
  suspend fun getSong(bookId: BookIdDto, songId: SongIdDto): SongDetailDto?
  suspend fun createSong(bookId: BookIdDto, request: SongCreateRequestDto): Either<CreateError, SongIdDto>
  suspend fun updateSong(bookId: BookIdDto, songId: SongIdDto, request: SongUpdateRequestDto): Either<UpdateError, SongIdDto>
  suspend fun deleteSong(bookId: BookIdDto, songId: SongIdDto): Either<DeleteError, SongIdDto>
}

internal class UserBookApiImpl(client: HttpClient) : BaseResourceApi(client), UserBookApi {

  // Book operations

  override suspend fun listBooks(sort: BookSortDto?): List<BookSummaryDto> =
    execute<List<BookSummaryDto>> { client.get(UserBooks(sort = sort)) }.getOrThrow()

  override suspend fun getBook(id: BookIdDto): BookDetailDto? =
    executeGet<BookDetailDto> { client.get(UserBooks.ById(id = id)) }.getOrThrow()

  override suspend fun createBook(request: BookCreateRequestDto): Either<CreateError, BookIdDto> =
    executeCreate<BookIdDto, BookIdDto>(resource = request.id) {
      client.post(UserBooks()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }

  override suspend fun updateBook(id: BookIdDto, request: BookUpdateRequestDto): Either<UpdateError, BookIdDto> =
    executeUpdate<BookIdDto, BookIdDto>(resourceId = id) {
      client.patch(UserBooks.ById(id = id)) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }

  override suspend fun deleteBook(id: BookIdDto): Either<DeleteError, BookIdDto> =
    executeDelete<BookIdDto>(resourceId = id) {
      client.delete(UserBooks.ById(id = id))
    }

  // Song operations

  override suspend fun listSongs(bookId: BookIdDto): List<SongSummaryDto> =
    execute<List<SongSummaryDto>> {
      client.get(UserBooks.ById.Songs(parent = UserBooks.ById(id = bookId)))
    }.getOrThrow()

  override suspend fun getSong(bookId: BookIdDto, songId: SongIdDto): SongDetailDto? =
    executeGet<SongDetailDto> {
      client.get(UserBooks.ById.Songs.BySongId(parent = UserBooks.ById.Songs(parent = UserBooks.ById(id = bookId)), songId = songId))
    }.getOrThrow()

  override suspend fun createSong(bookId: BookIdDto, request: SongCreateRequestDto): Either<CreateError, SongIdDto> =
    executeCreate<SongIdDto, SongIdDto>(resource = request.id) {
      client.post(UserBooks.ById.Songs(parent = UserBooks.ById(id = bookId))) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }

  override suspend fun updateSong(bookId: BookIdDto, songId: SongIdDto, request: SongUpdateRequestDto): Either<UpdateError, SongIdDto> =
    executeUpdate<SongIdDto, SongIdDto>(resourceId = songId) {
      client.patch(UserBooks.ById.Songs.BySongId(parent = UserBooks.ById.Songs(parent = UserBooks.ById(id = bookId)), songId = songId)) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }

  override suspend fun deleteSong(bookId: BookIdDto, songId: SongIdDto): Either<DeleteError, SongIdDto> =
    executeDelete<SongIdDto>(resourceId = songId) {
      client.delete(UserBooks.ById.Songs.BySongId(parent = UserBooks.ById.Songs(parent = UserBooks.ById(id = bookId)), songId = songId))
    }
}
