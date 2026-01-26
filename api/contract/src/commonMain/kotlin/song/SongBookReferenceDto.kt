package io.github.alelk.pws.api.contract.song

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import kotlinx.serialization.Serializable

/**
 * Reference to a song in a specific book.
 *
 * Used in search results to show which books contain the song
 * and what number the song has in each book.
 */
@Serializable
data class SongBookReferenceDto(
  val bookId: BookIdDto,
  val displayShortName: String,
  val songNumber: Int
) {
  init {
    require(songNumber > 0) { "songNumber must be positive: $songNumber" }
  }
}
