package io.github.alelk.pws.domain.song.repository

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.ClearError
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.command.UpdateSongCommand

/**
 * Write operations for songs in user-created books.
 */
interface UserBookSongWriteRepository {
  /**
   * Create a new song in user's book.
   */
  suspend fun createSong(userId: UserId, bookId: BookId, command: CreateSongCommand): Either<CreateError, SongId>

  /**
   * Update song in user's book.
   */
  suspend fun updateSong(userId: UserId, command: UpdateSongCommand): Either<UpdateError, SongId>

  /**
   * Delete song from user's book.
   */
  suspend fun deleteSong(userId: UserId, songId: SongId): Either<DeleteError, SongId>

  /**
   * Delete all songs in user's book.
   */
  suspend fun deleteAllSongs(userId: UserId, bookId: BookId): Either<ClearError, Int>
}
