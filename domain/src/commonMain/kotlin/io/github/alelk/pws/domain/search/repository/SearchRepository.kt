package io.github.alelk.pws.domain.search.repository

import io.github.alelk.pws.domain.search.model.SearchResult

/**
 * Search operations for songs.
 */
interface SearchRepository {
  /**
   * Search songs by query string.
   * Searches in song name and lyrics.
   * @param query Search query.
   * @param limit Maximum number of results.
   * @return List of search results ordered by relevance.
   */
  suspend fun search(query: String, limit: Int = 50): List<SearchResult>

  /**
   * Get search suggestions as user types.
   * Faster than full search, may use prefix matching.
   * @param query Partial search query.
   * @param limit Maximum number of suggestions.
   */
  suspend fun getSuggestions(query: String, limit: Int = 10): List<SearchResult>
}

