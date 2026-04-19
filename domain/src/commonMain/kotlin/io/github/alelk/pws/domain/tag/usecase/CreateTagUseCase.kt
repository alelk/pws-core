package io.github.alelk.pws.domain.tag.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.command.CreateTagCommand
import io.github.alelk.pws.domain.tag.repository.TagWriteRepository

/**
 * Use case: create a new tag.
 * @param ID The type of TagId this use case works with
 */
class CreateTagUseCase<ID : TagId>(
  private val tagRepository: TagWriteRepository<ID>,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(command: CreateTagCommand<ID>): Either<CreateError, ID> =
    txRunner.inRwTransaction { tagRepository.create(command) }
}
