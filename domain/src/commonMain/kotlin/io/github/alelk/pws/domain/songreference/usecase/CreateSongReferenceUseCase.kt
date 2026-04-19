package io.github.alelk.pws.domain.songreference.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songreference.command.CreateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.model.SongReference
import io.github.alelk.pws.domain.songreference.repository.SongReferenceWriteRepository

/**
 * Use case: create a new song reference.
 */
class CreateSongReferenceUseCase(
  private val repository: SongReferenceWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(command: CreateSongReferenceCommand): Either<CreateError, SongReference> =
    txRunner.inRwTransaction { repository.create(command) }
}
