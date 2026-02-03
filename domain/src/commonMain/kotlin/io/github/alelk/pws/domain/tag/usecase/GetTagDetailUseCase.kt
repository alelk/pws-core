package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.model.TagDetail
import io.github.alelk.pws.domain.tag.repository.TagReadRepository

/**
 * Use case: get tag details by id.
 * @param ID The type of TagId this use case works with
 */
class GetTagDetailUseCase<ID : TagId>(
  private val tagRepository: TagReadRepository<ID>,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(id: ID): TagDetail<ID>? =
    txRunner.inRoTransaction { tagRepository.get(id) }
}

