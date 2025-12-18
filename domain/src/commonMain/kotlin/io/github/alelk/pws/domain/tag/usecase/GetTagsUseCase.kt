package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.model.TagSummary
import io.github.alelk.pws.domain.tag.query.TagSort
import io.github.alelk.pws.domain.tag.repository.TagReadRepository

/**
 * Use case: get all tags.
 */
class GetTagsUseCase(
  private val tagRepository: TagReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(sort: TagSort = TagSort.ByPriority): List<TagSummary> =
    txRunner.inRoTransaction { tagRepository.getAll(sort) }
}

