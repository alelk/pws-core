package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.book.BookDetailDto
import io.github.alelk.pws.api.contract.book.BookSortDto
import io.github.alelk.pws.api.contract.book.BookSummaryDto
import io.github.alelk.pws.api.contract.book.Books
import io.github.alelk.pws.api.contract.book.songnumber.SongNumberLinkDto
import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get

/**
 * Public read-only Book API client.
 * For write operations, use [AdminBookApi].
 */
interface BookApi {
  suspend fun get(id: BookIdDto): BookDetailDto?
  suspend fun list(locale: LocaleDto? = null, enabled: Boolean? = null, minPriority: Int? = null, sort: BookSortDto? = null): List<BookSummaryDto>
  suspend fun listBookSongs(id: BookIdDto): List<SongNumberLinkDto>
}

internal class BookApiImpl(client: HttpClient) : BaseResourceApi(client), BookApi {

  override suspend fun get(id: BookIdDto): BookDetailDto? =
    executeGet<BookDetailDto> { client.get(Books.ById(id = id)) }.getOrThrow()

  override suspend fun list(locale: LocaleDto?, enabled: Boolean?, minPriority: Int?, sort: BookSortDto?): List<BookSummaryDto> =
    execute<List<BookSummaryDto>> { client.get(Books(locale = locale, enabled = enabled, minPriority = minPriority, sort = sort)) }.getOrThrow()

  override suspend fun listBookSongs(id: BookIdDto): List<SongNumberLinkDto> =
    execute<List<SongNumberLinkDto>> { client.get(Books.ById.Songs(parent = Books.ById(id = id))) }.getOrThrow()
}
