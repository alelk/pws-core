package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.book.BookCreateRequestDto
import io.github.alelk.pws.api.contract.book.BookDetailDto
import io.github.alelk.pws.api.contract.book.BookSortDto
import io.github.alelk.pws.api.contract.book.BookSummaryDto
import io.github.alelk.pws.api.contract.book.BookUpdateRequestDto
import io.github.alelk.pws.api.contract.book.Books
import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

interface BookApi {
  suspend fun get(id: BookIdDto): BookDetailDto?
  suspend fun list(locale: LocaleDto? = null, enabled: Boolean? = null, minPriority: Int? = null, sort: BookSortDto? = null): List<BookSummaryDto>

  suspend fun listBookSongs(id: BookIdDto): Map<Int, SongSummaryDto>?

  suspend fun create(request: BookCreateRequestDto): ResourceCreateResult<BookIdDto>

  suspend fun update(id: BookIdDto, request: BookUpdateRequestDto): ResourceUpdateResult<BookIdDto>
}

internal class BookApiImpl(client: HttpClient) : BaseResourceApi(client), BookApi {
  override suspend fun get(id: BookIdDto): BookDetailDto? =
    executeGet<BookDetailDto> { client.get(Books.ById(id = id)) }.getOrThrow()

  override suspend fun list(locale: LocaleDto?, enabled: Boolean?, minPriority: Int?, sort: BookSortDto?): List<BookSummaryDto> =
    execute<List<BookSummaryDto>> { client.get(Books(locale = locale, enabled = enabled, minPriority = minPriority, sort = sort)) }.getOrThrow()

  override suspend fun listBookSongs(id: BookIdDto): Map<Int, SongSummaryDto>? =
    executeGet<Map<Int, SongSummaryDto>> { client.get(Books.ById.Songs(parent = Books.ById(id = id))) }.getOrThrow()

  override suspend fun create(request: BookCreateRequestDto): ResourceCreateResult<BookIdDto> =
    executeCreate<String, BookIdDto>(resourceId = request.id) {
      client.post(Books.Create()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun update(id: BookIdDto, request: BookUpdateRequestDto): ResourceUpdateResult<BookIdDto> =
    executeUpdate<String, BookIdDto>(resourceId = id) {
      client.put(Books.ById.Update(parent = Books.ById(id = id))) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()
}
