package io.github.alelk.pws.domain.song.query

/**
 * Search query parameters.
 */
data class SearchQuery(
  val query: String,
  val type: SearchType = SearchType.ALL,
  val scope: SearchScope = SearchScope.ALL,
  val limit: Int = 20,
  val offset: Int = 0,
  val highlight: Boolean = true
) {
  companion object {
    const val MAX_LIMIT = 100
    const val DEFAULT_SUGGESTION_LIMIT = 10
  }
}

