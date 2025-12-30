package io.github.alelk.pws.domain.song.repository

import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.song.model.SongSearchResponse
import io.github.alelk.pws.domain.song.model.SongSearchSuggestion
import io.github.alelk.pws.domain.song.query.SearchQuery

/**
 * Repository for full-text search operations on songs.
 */
interface SongSearchRepository {

  /**
   * Get search suggestions for autocomplete.
   *
   * @param query Search text
   * @param userId User ID for personalized results (null for anonymous)
   * @param bookId Optional filter by specific book
   * @param limit Maximum number of suggestions
   * @return List of suggestions with unified ranking
   */
  suspend fun searchSuggestions(
    query: String,
    userId: UserId? = null,
    bookId: BookId? = null,
    limit: Int = 10
  ): List<SongSearchSuggestion>

  /**
   * Perform full-text search on songs.
   *
   * Search scope is determined by [SearchQuery.scope]:
   * - ALL: searches both global and user's songs (if userId provided) with unified ranking
   * - GLOBAL: searches only global songs catalog
   * - USER_BOOKS: searches only user's songbooks (requires userId)
   *
   * @param searchQuery Search parameters including query, type, scope, pagination
   * @param userId User ID for personalized search and USER_BOOKS scope (null for anonymous)
   * @param bookId Optional filter by specific book
   * @return Search response with results and pagination info
   */
  suspend fun search(
    searchQuery: SearchQuery,
    userId: UserId? = null,
    bookId: BookId? = null
  ): SongSearchResponse
}

