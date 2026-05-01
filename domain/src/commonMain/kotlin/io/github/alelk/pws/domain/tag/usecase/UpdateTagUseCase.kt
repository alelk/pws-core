package io.github.alelk.pws.domain.tag.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.command.UpdateTagCommand
import io.github.alelk.pws.domain.tag.repository.TagReadRepository
import io.github.alelk.pws.domain.tag.repository.TagWriteRepository

/**
 * Use case: update an existing tag.
 * @param ID The type of TagId this use case works with
 */
class UpdateTagUseCase<ID : TagId>(
  private val tagReadRepository: TagReadRepository<ID>,
  private val tagWriteRepository: TagWriteRepository<ID>,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(command: UpdateTagCommand<ID>): Either<UpdateError, ID> =
    txRunner.inRwTransaction {
      if (!command.hasChanges()) return@inRwTransaction Either.Right(command.id)

      if (!tagReadRepository.exists(command.id)) {
        return@inRwTransaction Either.Left(UpdateError.NotFound)
      }

      command.name?.let { name ->
        if (tagReadRepository.existsByName(name, excludeId = command.id)) {
          return@inRwTransaction Either.Left(UpdateError.ValidationError("Tag name '$name' already exists"))
        }
      }

      tagWriteRepository.update(command)
    }
}
