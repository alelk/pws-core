package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.repository.UserTagWriteRepository

/**
 * Use case: delete a user's custom tag.
 */
class DeleteUserTagUseCase(
  private val userTagRepository: UserTagWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, tagId: TagId): DeleteResourceResult<TagId> =
    txRunner.inRwTransaction { userTagRepository.deleteUserTag(userId, tagId) }
}

