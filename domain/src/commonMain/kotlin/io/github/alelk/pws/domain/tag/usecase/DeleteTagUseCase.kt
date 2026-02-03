package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.repository.TagWriteRepository

/**
 * Use case: delete a tag.
 * @param ID The type of TagId this use case works with
 */
class DeleteTagUseCase<ID : TagId>(
  private val tagRepository: TagWriteRepository<ID>,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(id: ID): DeleteResourceResult<ID> =
    txRunner.inRwTransaction { tagRepository.delete(id) }
}

