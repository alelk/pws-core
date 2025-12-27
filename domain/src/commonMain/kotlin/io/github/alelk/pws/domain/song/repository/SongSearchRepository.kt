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
   * Fast search with minimal details.
   */
  suspend fun searchSuggestions(
    query: String,
    limit: Int = 10
  ): List<SongSearchSuggestion>

  /**
   * Perform full-text search on songs.
   */
  suspend fun search(
    searchQuery: SearchQuery
  ): SongSearchResponse

  /**
   * Search in user's book songs.
   */
  suspend fun searchInUserBooks(
    userId: UserId,
    searchQuery: SearchQuery,
    bookId: BookId? = null
  ): SongSearchResponse
}

