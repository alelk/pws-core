package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.model.TagDetail
import io.github.alelk.pws.domain.tag.repository.TagReadRepository

/**
 * Use case: get tag details by id.
 */
class GetTagDetailUseCase(
  private val tagRepository: TagReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(id: TagId): TagDetail? =
    txRunner.inRoTransaction { tagRepository.get(id) }
}

