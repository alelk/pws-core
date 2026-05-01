package io.github.alelk.pws.domain.tag.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.repository.TagReadRepository
import io.github.alelk.pws.domain.tag.repository.TagWriteRepository

/**
 * Use case: delete a tag.
 * @param ID The type of TagId this use case works with
 */
class DeleteTagUseCase<ID : TagId>(
  private val tagReadRepository: TagReadRepository<ID>,
  private val tagWriteRepository: TagWriteRepository<ID>,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(id: ID): Either<DeleteError, ID> =
    txRunner.inRwTransaction {
      if (!tagReadRepository.exists(id)) {
        return@inRwTransaction Either.Left(DeleteError.NotFound)
      }
      tagWriteRepository.delete(id)
    }
}
