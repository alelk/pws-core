package io.github.alelk.pws.domain.song.model

import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.ids.SongId

/**
 * Song search suggestion for autocomplete.
 *
 * @property id The song ID.
 * @property name The song name.
 * @property bookReferences List of books containing this song with song numbers.
 *                          Empty if the song is not linked to any book.
 * @property snippet Optional highlighted snippet from matched text.
 */
data class SongSearchSuggestion(
  val id: SongId,
  val name: NonEmptyString,
  val bookReferences: List<SongBookReference> = emptyList(),
  val snippet: String? = null
)