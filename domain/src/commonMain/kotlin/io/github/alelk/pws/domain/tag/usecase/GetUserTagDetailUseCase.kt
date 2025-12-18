package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.model.TagDetail
import io.github.alelk.pws.domain.tag.repository.UserTagReadRepository

/**
 * Use case: get tag detail for a user (with overrides applied).
 */
class GetUserTagDetailUseCase(
  private val userTagRepository: UserTagReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, tagId: TagId): TagDetail? =
    txRunner.inRoTransaction { userTagRepository.getTag(userId, tagId) }
}

