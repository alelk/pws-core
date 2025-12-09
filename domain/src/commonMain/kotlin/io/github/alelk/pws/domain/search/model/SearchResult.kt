package io.github.alelk.pws.domain.search.model

import io.github.alelk.pws.domain.core.ids.SongNumberId

/**
 * Search result item with song info.
 */
data class SearchResult(
  val songNumberId: SongNumberId,
  val songNumber: Int,
  val songName: String,
  val bookDisplayName: String,
  val relevance: Float = 1.0f
)

