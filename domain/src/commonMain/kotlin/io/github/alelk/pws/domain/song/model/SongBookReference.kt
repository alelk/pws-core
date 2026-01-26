package io.github.alelk.pws.domain.song.model

import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.ids.BookId

/**
 * Reference to a song in a specific book.
 *
 * Used in search results to show which books contain the song
 * and what number the song has in each book.
 */
data class SongBookReference(
  val bookId: BookId,
  val displayShortName: NonEmptyString,
  val songNumber: Int
) {
  init {
    require(songNumber > 0) { "songNumber must be positive: $songNumber" }
  }
}
