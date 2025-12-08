package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.repository.TagWriteRepository

/**
 * Use case: delete a tag.
 */
class DeleteTagUseCase(
  private val tagRepository: TagWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(id: TagId): DeleteResourceResult<TagId> =
    txRunner.inRwTransaction { tagRepository.delete(id) }
}

