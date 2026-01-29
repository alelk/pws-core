package io.github.alelk.pws.domain.history.model

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId

/**
 * Identifies what was viewed in history.
 *
 * Songs can be viewed either:
 * - As part of a book (with book context and song number)
 * - As a standalone song (without book association)
 */
sealed interface HistorySubject {

  /** The song ID (always present). */
  val songId: SongId

  /**
   * A song viewed in context of a specific book.
   * Contains the book ID and the song's number within that book.
   */
  data class BookedSong(val songNumberId: SongNumberId) : HistorySubject {
    override val songId: SongId get() = songNumberId.songId
  }

  /**
   * A standalone song viewed without book context.
   * Used for songs that don't belong to any book or were accessed directly.
   */
  data class StandaloneSong(override val songId: SongId) : HistorySubject
}
