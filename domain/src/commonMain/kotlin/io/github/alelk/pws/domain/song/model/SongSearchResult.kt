package io.github.alelk.pws.domain.song.model

/**
 * Song search result with highlighting and ranking.
 */
data class SongSearchResult(
  val song: SongSummary,
  val snippet: String,
  val rank: Float,
  val matchedFields: List<MatchedField>
)