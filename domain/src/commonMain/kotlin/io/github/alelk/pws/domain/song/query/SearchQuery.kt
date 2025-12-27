package io.github.alelk.pws.domain.song.query

/**
 * Type of search to perform.
 */
enum class SearchType {
  /** Search in all fields */
  ALL,

  /** Search only in song name */
  NAME,

  /** Search only in song lyric */
  LYRIC,

  /** Search by song number */
  NUMBER
}

/**
 * Search query parameters.
 */
data class SearchQuery(
  val query: String,
  val type: SearchType = SearchType.ALL,
  val limit: Int = 20,
  val offset: Int = 0,
  val highlight: Boolean = true
) {
  companion object {
    const val MAX_LIMIT = 100
    const val DEFAULT_SUGGESTION_LIMIT = 10
  }
}

