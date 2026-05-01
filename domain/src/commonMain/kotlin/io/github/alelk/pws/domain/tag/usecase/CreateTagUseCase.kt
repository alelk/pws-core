package io.github.alelk.pws.domain.tag.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.command.CreateTagCommand
import io.github.alelk.pws.domain.tag.repository.TagReadRepository
import io.github.alelk.pws.domain.tag.repository.TagWriteRepository

/**
 * Use case: create a new tag.
 * @param ID The type of TagId this use case works with
 */
class CreateTagUseCase<ID : TagId>(
  private val tagReadRepository: TagReadRepository<ID>,
  private val tagWriteRepository: TagWriteRepository<ID>,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(command: CreateTagCommand<ID>): Either<CreateError, ID> =
    txRunner.inRwTransaction {
      if (tagReadRepository.exists(command.id)) {
        return@inRwTransaction Either.Left(CreateError.AlreadyExists())
      }
      if (tagReadRepository.existsByName(command.name)) {
        return@inRwTransaction Either.Left(CreateError.ValidationError("Tag name '${command.name}' already exists"))
      }
      tagWriteRepository.create(command)
    }
}
