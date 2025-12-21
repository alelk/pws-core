package io.github.alelk.pws.domain.songreference.usecase

import io.github.alelk.pws.domain.core.result.CreateResourceResult
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
  suspend operator fun invoke(command: CreateSongReferenceCommand): CreateResourceResult<SongReference> =
    txRunner.inRwTransaction { repository.create(command) }
}

