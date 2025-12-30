package io.github.alelk.pws.domain.song.query

/**
 * Search scope determines which song sources to search.
 */
enum class SearchScope {
  /** Search only in user's custom songbooks (requires authentication) */
  USER_BOOKS,

  /** Search only in global songs catalog */
  GLOBAL,

  /** Search in both sources with unified ranking (default) */
  ALL
}