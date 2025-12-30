package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.song.SearchScopeDto
import io.github.alelk.pws.api.contract.song.SearchTypeDto
import io.github.alelk.pws.api.contract.song.SongSearch
import io.github.alelk.pws.api.contract.song.SongSearchResponseDto
import io.github.alelk.pws.api.contract.song.SongSearchSuggestionDto
import io.github.alelk.pws.api.contract.song.SongSearchSuggestions
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get

/**
 * Song Search API client for full-text search on songs.
 *
 * Provides:
 * - [suggestions] - autocomplete suggestions for song search
 * - [search] - full-text search with ranking and highlighting
 *
 * Search behavior depends on authentication:
 * - Anonymous users: search only global songs
 * - Authenticated users: can search both global and user's songbooks
 */
interface SongSearchApi {

  /**
   * Get search suggestions for autocomplete.
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

  /**
   * Full-text search on songs.
   *
   * @param query Search text or song number
   * @param type Search type (ALL, NAME, LYRIC, NUMBER). Default: ALL
   * @param bookId Optional filter by specific book ID
   * @param scope Search scope (ALL, GLOBAL, USER_BOOKS). Default: ALL
   * @param limit Maximum results (default: 20, max: 100)
   * @param offset Pagination offset (default: 0)
   * @param highlight Enable snippet highlighting (default: true)
   * @return Search response with results and pagination info
   */
  suspend fun search(
    query: String,
    type: SearchTypeDto? = null,
    bookId: BookIdDto? = null,
    scope: SearchScopeDto? = null,
    limit: Int? = null,
    offset: Int? = null,
    highlight: Boolean? = null
  ): SongSearchResponseDto
}

internal class SongSearchApiImpl(client: HttpClient) : BaseResourceApi(client), SongSearchApi {

  override suspend fun suggestions(
    query: String,
    bookId: BookIdDto?,
    limit: Int?
  ): List<SongSearchSuggestionDto> =
    execute<List<SongSearchSuggestionDto>> {
      client.get(SongSearchSuggestions(query = query, bookId = bookId, limit = limit))
    }.getOrThrow()

  override suspend fun search(
    query: String,
    type: SearchTypeDto?,
    bookId: BookIdDto?,
    scope: SearchScopeDto?,
    limit: Int?,
    offset: Int?,
    highlight: Boolean?
  ): SongSearchResponseDto =
    execute<SongSearchResponseDto> {
      client.get(SongSearch(
        query = query,
        type = type,
        bookId = bookId,
        scope = scope,
        limit = limit,
        offset = offset,
        highlight = highlight
      ))
    }.getOrThrow()
}

