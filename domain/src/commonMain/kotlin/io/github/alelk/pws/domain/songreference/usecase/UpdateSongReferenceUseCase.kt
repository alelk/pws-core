package io.github.alelk.pws.domain.songreference.usecase

import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songreference.command.UpdateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.model.SongReference
import io.github.alelk.pws.domain.songreference.repository.SongReferenceWriteRepository

/**
 * Use case: update an existing song reference.
 */
class UpdateSongReferenceUseCase(
  private val repository: SongReferenceWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(command: UpdateSongReferenceCommand): UpdateResourceResult<SongReference> =
    txRunner.inRwTransaction { repository.update(command) }
}

