package io.github.alelk.pws.domain.song.model

/**
 * Search response with pagination info.
 */
data class SongSearchResponse(
  val results: List<SongSearchResult>,
  val totalCount: Long,
  val hasMore: Boolean
)

