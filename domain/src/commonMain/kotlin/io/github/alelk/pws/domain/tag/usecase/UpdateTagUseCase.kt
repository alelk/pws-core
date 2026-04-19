package io.github.alelk.pws.domain.tag.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.command.UpdateTagCommand
import io.github.alelk.pws.domain.tag.repository.TagWriteRepository

/**
 * Use case: update an existing tag.
 * @param ID The type of TagId this use case works with
 */
class UpdateTagUseCase<ID : TagId>(
  private val tagRepository: TagWriteRepository<ID>,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(command: UpdateTagCommand<ID>): Either<UpdateError, ID> =
    txRunner.inRwTransaction { tagRepository.update(command) }
}
