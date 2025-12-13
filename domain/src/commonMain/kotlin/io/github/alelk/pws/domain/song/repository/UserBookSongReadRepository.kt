package io.github.alelk.pws.domain.song.repository

import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.model.SongSummary

/**
 * Read operations for songs in user-created books.
 */
interface UserBookSongReadRepository {
  /**
   * Get song by ID from user's book.
   */
  suspend fun getSong(userId: UserId, songId: SongId): SongDetail?

  /**
   * Get song by number in user's book.
   */
  suspend fun getSongByNumber(userId: UserId, bookId: BookId, number: Int): SongDetail?

  /**
   * Get all songs in user's book.
   */
  suspend fun getSongsByBook(userId: UserId, bookId: BookId): List<SongSummary>

  /**
   * Count songs in user's book.
   */
  suspend fun countSongs(userId: UserId, bookId: BookId): Long
}

