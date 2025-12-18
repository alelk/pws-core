package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.model.TagSummary
import io.github.alelk.pws.domain.tag.query.TagSort
import io.github.alelk.pws.domain.tag.repository.UserTagReadRepository

/**
 * Use case: get all tags for a user (global + custom, with overrides applied).
 */
class GetUserTagsUseCase(
  private val userTagRepository: UserTagReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, sort: TagSort = TagSort.ByPriority): List<TagSummary> =
    txRunner.inRoTransaction { userTagRepository.getAllTags(userId, sort) }
}

