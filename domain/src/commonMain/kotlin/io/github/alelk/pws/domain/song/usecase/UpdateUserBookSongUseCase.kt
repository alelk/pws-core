package io.github.alelk.pws.domain.song.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.command.UpdateSongCommand
import io.github.alelk.pws.domain.song.repository.UserBookSongWriteRepository

/**
 * Update a song in a user book.
 */
class UpdateUserBookSongUseCase(
  private val writeRepository: UserBookSongWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, command: UpdateSongCommand): UpdateResourceResult<SongId> =
    txRunner.inRwTransaction { writeRepository.updateSong(userId, command) }
}
