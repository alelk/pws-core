package io.github.alelk.pws.api.client.api

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
  suspend fun createBook(request: BookCreateRequestDto): ResourceCreateResult<BookIdDto>
  suspend fun updateBook(id: BookIdDto, request: BookUpdateRequestDto): ResourceUpdateResult<BookIdDto>
  suspend fun deleteBook(id: BookIdDto): ResourceDeleteResult<BookIdDto>

  // Song operations
  suspend fun listSongs(bookId: BookIdDto): List<SongSummaryDto>
  suspend fun getSong(bookId: BookIdDto, songId: SongIdDto): SongDetailDto?
  suspend fun createSong(bookId: BookIdDto, request: SongCreateRequestDto): ResourceCreateResult<SongIdDto>
  suspend fun updateSong(bookId: BookIdDto, songId: SongIdDto, request: SongUpdateRequestDto): ResourceUpdateResult<SongIdDto>
  suspend fun deleteSong(bookId: BookIdDto, songId: SongIdDto): ResourceDeleteResult<SongIdDto>
}

internal class UserBookApiImpl(client: HttpClient) : BaseResourceApi(client), UserBookApi {

  // Book operations

  override suspend fun listBooks(sort: BookSortDto?): List<BookSummaryDto> =
    execute<List<BookSummaryDto>> { client.get(UserBooks(sort = sort)) }.getOrThrow()

  override suspend fun getBook(id: BookIdDto): BookDetailDto? =
    executeGet<BookDetailDto> { client.get(UserBooks.ById(id = id)) }.getOrThrow()

  override suspend fun createBook(request: BookCreateRequestDto): ResourceCreateResult<BookIdDto> =
    executeCreate<BookIdDto, BookIdDto>(resource = request.id) {
      client.post(UserBooks()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun updateBook(id: BookIdDto, request: BookUpdateRequestDto): ResourceUpdateResult<BookIdDto> =
    executeUpdate<BookIdDto, BookIdDto>(resourceId = id) {
      client.patch(UserBooks.ById(id = id)) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun deleteBook(id: BookIdDto): ResourceDeleteResult<BookIdDto> =
    executeDelete<BookIdDto>(resourceId = id) {
      client.delete(UserBooks.ById(id = id))
    }.getOrThrow()

  // Song operations

  override suspend fun listSongs(bookId: BookIdDto): List<SongSummaryDto> =
    execute<List<SongSummaryDto>> {
      client.get(UserBooks.ById.Songs(parent = UserBooks.ById(id = bookId)))
    }.getOrThrow()

  override suspend fun getSong(bookId: BookIdDto, songId: SongIdDto): SongDetailDto? =
    executeGet<SongDetailDto> {
      client.get(UserBooks.ById.Songs.BySongId(parent = UserBooks.ById.Songs(parent = UserBooks.ById(id = bookId)), songId = songId))
    }.getOrThrow()

  override suspend fun createSong(bookId: BookIdDto, request: SongCreateRequestDto): ResourceCreateResult<SongIdDto> =
    executeCreate<SongIdDto, SongIdDto>(resource = request.id) {
      client.post(UserBooks.ById.Songs(parent = UserBooks.ById(id = bookId))) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun updateSong(bookId: BookIdDto, songId: SongIdDto, request: SongUpdateRequestDto): ResourceUpdateResult<SongIdDto> =
    executeUpdate<SongIdDto, SongIdDto>(resourceId = songId) {
      client.patch(UserBooks.ById.Songs.BySongId(parent = UserBooks.ById.Songs(parent = UserBooks.ById(id = bookId)), songId = songId)) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun deleteSong(bookId: BookIdDto, songId: SongIdDto): ResourceDeleteResult<SongIdDto> =
    executeDelete<SongIdDto>(resourceId = songId) {
      client.delete(UserBooks.ById.Songs.BySongId(parent = UserBooks.ById.Songs(parent = UserBooks.ById(id = bookId)), songId = songId))
    }.getOrThrow()
}

