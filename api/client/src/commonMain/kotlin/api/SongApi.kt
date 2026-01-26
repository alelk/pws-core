package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SearchTypeDto
import io.github.alelk.pws.api.contract.song.SongDetailDto
import io.github.alelk.pws.api.contract.song.SongSearchResponseDto
import io.github.alelk.pws.api.contract.song.SongSearchSuggestionDto
import io.github.alelk.pws.api.contract.song.SongSortDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
import io.github.alelk.pws.api.contract.song.Songs
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get

/**
 * Public read-only Song API client.
 *
 * Provides:
 * - [get], [list] - basic CRUD operations
 * - [search], [suggestions] - full-text search on global songs
 *
 * This API searches only in global songs catalog.
 * For searching user's songs, use [UserSongApi].
 * For write operations, use [AdminSongApi].
 */
interface SongApi {
  /** Get song details by ID. Returns null if not found. */
  suspend fun get(id: SongIdDto): SongDetailDto?

  /** List songs with optional filtering and sorting. */
  suspend fun list(
    bookId: BookIdDto? = null,
    minNumber: Int? = null,
    maxNumber: Int? = null,
    sort: SongSortDto? = null
  ): List<SongSummaryDto>

  /**
   * Full-text search on global songs.
   *
   * @param query Search text or song number
   * @param type Search type (ALL, NAME, LYRIC, NUMBER). Default: ALL
   * @param bookId Optional filter by specific book ID
   * @param limit Maximum results (default: 20, max: 100)
   * @param offset Pagination offset (default: 0)
   * @param highlight Enable snippet highlighting (default: true)
   * @return Search response with results and pagination info
   */
  suspend fun search(
    query: String,
    type: SearchTypeDto? = null,
    bookId: BookIdDto? = null,
    limit: Int? = null,
    offset: Int? = null,
    highlight: Boolean? = null
  ): SongSearchResponseDto

  /**
   * Get search suggestions for autocomplete from global songs.
   *
   * @param query Search text or song number
   * @param bookId Optional filter by specific book ID
   * @param limit Maximum suggestions (default: 10, max: 50)
   * @return List of song search suggestions
   */
  suspend fun suggestions(
    query: String,
    bookId: BookIdDto? = null,
    limit: Int? = null
  ): List<SongSearchSuggestionDto>
}

internal class SongApiImpl(client: HttpClient) : BaseResourceApi(client), SongApi {

  override suspend fun get(id: SongIdDto): SongDetailDto? =
    executeGet<SongDetailDto> { client.get(Songs.ById(id = id)) }.getOrThrow()

  override suspend fun list(
    bookId: BookIdDto?,
    minNumber: Int?,
    maxNumber: Int?,
    sort: SongSortDto?
  ): List<SongSummaryDto> =
    execute<List<SongSummaryDto>> { client.get(Songs(bookId = bookId, minNumber = minNumber, maxNumber = maxNumber, sort = sort)) }
      .getOrThrow()

  override suspend fun search(
    query: String,
    type: SearchTypeDto?,
    bookId: BookIdDto?,
    limit: Int?,
    offset: Int?,
    highlight: Boolean?
  ): SongSearchResponseDto =
    execute<SongSearchResponseDto> {
      client.get(Songs.Search(
        query = query,
        type = type,
        bookId = bookId,
        limit = limit,
        offset = offset,
        highlight = highlight
      ))
    }.getOrThrow()

  override suspend fun suggestions(
    query: String,
    bookId: BookIdDto?,
    limit: Int?
  ): List<SongSearchSuggestionDto> =
    execute<List<SongSearchSuggestionDto>> {
      client.get(Songs.SearchSuggestions(query = query, bookId = bookId, limit = limit))
    }.getOrThrow()
}
