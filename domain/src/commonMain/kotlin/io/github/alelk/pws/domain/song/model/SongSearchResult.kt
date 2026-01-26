package io.github.alelk.pws.domain.song.model

/**
 * Song search result with highlighting and ranking.
 *
 * @property song The song summary.
 * @property snippet Highlighted snippet from the matched text.
 * @property rank Search rank/relevance score.
 * @property matchedFields Which fields matched the search query.
 * @property bookReferences List of books containing this song with song numbers.
 *                          Empty if the song is not linked to any book.
 */
data class SongSearchResult(
  val song: SongSummary,
  val snippet: String,
  val rank: Float,
  val matchedFields: List<MatchedField>,
  val bookReferences: List<SongBookReference> = emptyList()
)