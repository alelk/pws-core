package io.github.alelk.pws.api.contract.song

import kotlinx.serialization.Serializable

/**
 * Search scope determines which song sources to search.
 */
@Serializable
enum class SearchScopeDto {
  /** Search only in user's custom songbooks (requires authentication) */
  USER_BOOKS,
  /** Search only in global songs catalog */
  GLOBAL,
  /** Search in both sources with unified ranking (default) */
  ALL
}

