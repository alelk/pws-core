package io.github.alelk.pws.api.contract.song

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.ktor.resources.Resource

/**
 * Get search suggestions for autocomplete.
 *
 * - If user is authenticated: searches both global songs and user's songbooks
 * - If user is not authenticated: searches only global songs
 */
@Resource("/v1/songs/search/suggestions")
class SongSearchSuggestions(
  val query: String,
  val bookId: BookIdDto? = null,
  val limit: Int? = null
)