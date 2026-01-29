package io.github.alelk.pws.domain.favorite.model

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId

/**
 * Identifies what is favorited.
 *
 * Songs can be favorited either:
 * - As part of a book (with book context and song number)
 * - As a standalone song (without book association)
 */
sealed interface FavoriteSubject {

  /** The song ID (always present). */
  val songId: SongId

  /**
   * A song favorited in context of a specific book.
   * Contains the book ID and the song's number within that book.
   */
  data class BookedSong(val songNumberId: SongNumberId) : FavoriteSubject {
    override val songId: SongId get() = songNumberId.songId
  }

  /**
   * A standalone song favorited without book context.
   * Used for songs that don't belong to any book or were accessed directly.
   */
  data class StandaloneSong(override val songId: SongId) : FavoriteSubject
}
