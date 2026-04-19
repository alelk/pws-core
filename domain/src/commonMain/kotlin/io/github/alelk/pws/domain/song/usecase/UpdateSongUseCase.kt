package io.github.alelk.pws.domain.song.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.command.UpdateSongCommand
import io.github.alelk.pws.domain.song.repository.SongWriteRepository

/** Use case: update a song (patch semantics). */
class UpdateSongUseCase(
  private val writeRepository: SongWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(command: UpdateSongCommand): Either<UpdateError, SongId> =
    txRunner.inRwTransaction { writeRepository.update(command) }
}
