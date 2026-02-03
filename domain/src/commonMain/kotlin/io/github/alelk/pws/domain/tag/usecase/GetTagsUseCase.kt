package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.query.TagSort
import io.github.alelk.pws.domain.tag.repository.TagReadRepository

/**
 * Use case: get all tags.
 * @param ID The type of TagId this use case works with
 */
class GetTagsUseCase<ID : TagId>(
  private val tagRepository: TagReadRepository<ID>,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(sort: TagSort = TagSort.ByPriority): List<Tag<ID>> =
    txRunner.inRoTransaction { tagRepository.getAll(sort) }
}

