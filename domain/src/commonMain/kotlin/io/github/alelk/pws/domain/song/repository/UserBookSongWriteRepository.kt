package io.github.alelk.pws.domain.song.repository

import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.ClearResourcesResult
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.command.UpdateSongCommand

/**
 * Write operations for songs in user-created books.
 */
interface UserBookSongWriteRepository {
  /**
   * Create a new song in user's book.
   */
  suspend fun createSong(userId: UserId, bookId: BookId, command: CreateSongCommand): CreateResourceResult<SongId>

  /**
   * Update song in user's book.
   */
  suspend fun updateSong(userId: UserId, command: UpdateSongCommand): UpdateResourceResult<SongId>

  /**
   * Delete song from user's book.
   */
  suspend fun deleteSong(userId: UserId, songId: SongId): DeleteResourceResult<SongId>

  /**
   * Delete all songs in user's book.
   */
  suspend fun deleteAllSongs(userId: UserId, bookId: BookId): ClearResourcesResult
}

