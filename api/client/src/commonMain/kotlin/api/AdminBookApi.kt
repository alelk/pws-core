package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.admin.AdminBooks
import io.github.alelk.pws.api.contract.book.BookCreateRequestDto
import io.github.alelk.pws.api.contract.book.BookDetailDto
import io.github.alelk.pws.api.contract.book.BookSortDto
import io.github.alelk.pws.api.contract.book.BookSummaryDto
import io.github.alelk.pws.api.contract.book.BookUpdateRequestDto
import io.github.alelk.pws.api.contract.book.songnumber.ReplaceAllBookSongNumbersResult
import io.github.alelk.pws.api.contract.book.songnumber.SongNumberLinkDto
import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.patch
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

/**
 * Admin Book API client for managing global books.
 * Requires admin role.
 */
interface AdminBookApi {
  suspend fun get(id: BookIdDto): BookDetailDto?
  suspend fun list(locale: LocaleDto? = null, enabled: Boolean? = null, minPriority: Int? = null, sort: BookSortDto? = null): List<BookSummaryDto>
  suspend fun create(request: BookCreateRequestDto): ResourceCreateResult<BookIdDto>
  suspend fun update(id: BookIdDto, request: BookUpdateRequestDto): ResourceUpdateResult<BookIdDto>
  suspend fun delete(id: BookIdDto): ResourceDeleteResult<BookIdDto>
  suspend fun listSongs(id: BookIdDto): List<SongNumberLinkDto>
  suspend fun addSongs(id: BookIdDto, songs: List<SongNumberLinkDto>): ResourceBatchCreateResult<SongNumberLinkDto>
  suspend fun replaceSongs(id: BookIdDto, songs: List<SongNumberLinkDto>): ReplaceAllBookSongNumbersResult
}

internal class AdminBookApiImpl(client: HttpClient) : BaseResourceApi(client), AdminBookApi {

  override suspend fun get(id: BookIdDto): BookDetailDto? =
    executeGet<BookDetailDto> { client.get(AdminBooks.ById(id = id)) }.getOrThrow()

  override suspend fun list(locale: LocaleDto?, enabled: Boolean?, minPriority: Int?, sort: BookSortDto?): List<BookSummaryDto> =
    execute<List<BookSummaryDto>> {
      client.get(AdminBooks(locale = locale, enabled = enabled, minPriority = minPriority, sort = sort))
    }.getOrThrow()

  override suspend fun create(request: BookCreateRequestDto): ResourceCreateResult<BookIdDto> =
    executeCreate<BookIdDto, BookIdDto>(resource = request.id) {
      client.post(AdminBooks()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun update(id: BookIdDto, request: BookUpdateRequestDto): ResourceUpdateResult<BookIdDto> =
    executeUpdate<BookIdDto, BookIdDto>(resourceId = id) {
      client.patch(AdminBooks.ById(id = id)) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun delete(id: BookIdDto): ResourceDeleteResult<BookIdDto> =
    executeDelete<BookIdDto>(resourceId = id) {
      client.delete(AdminBooks.ById(id = id))
    }.getOrThrow()

  override suspend fun listSongs(id: BookIdDto): List<SongNumberLinkDto> =
    execute<List<SongNumberLinkDto>> {
      client.get(AdminBooks.ById.Songs(parent = AdminBooks.ById(id = id)))
    }.getOrThrow()

  override suspend fun addSongs(id: BookIdDto, songs: List<SongNumberLinkDto>): ResourceBatchCreateResult<SongNumberLinkDto> =
    executeBatchCreate<List<SongNumberLinkDto>, SongNumberLinkDto>(
      resources = songs,
      resourceIdParser = { SongNumberLinkDto.parse(it) }
    ) {
      client.post(AdminBooks.ById.Songs(parent = AdminBooks.ById(id = id))) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(songs)
      }
    }.getOrThrow()

  override suspend fun replaceSongs(id: BookIdDto, songs: List<SongNumberLinkDto>): ReplaceAllBookSongNumbersResult =
    execute<ReplaceAllBookSongNumbersResult> {
      client.put(AdminBooks.ById.Songs(parent = AdminBooks.ById(id = id))) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(songs)
      }
    }.getOrThrow()
}

