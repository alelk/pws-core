package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.command.UpdateTagCommand
import io.github.alelk.pws.domain.tag.repository.TagWriteRepository

/**
 * Use case: update an existing tag.
 */
class UpdateTagUseCase(
  private val tagRepository: TagWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(command: UpdateTagCommand): UpdateResourceResult<TagId> =
    txRunner.inRwTransaction { tagRepository.update(command) }
}

