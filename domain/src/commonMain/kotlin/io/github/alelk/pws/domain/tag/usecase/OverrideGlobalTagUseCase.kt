package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.Color
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.repository.UserTagWriteRepository

/**
 * Use case: override global tag settings for a user (hide, color, priority).
 */
class OverrideGlobalTagUseCase(
  private val userTagRepository: UserTagWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend fun hide(userId: UserId, tagId: TagId): UpdateResourceResult<TagId> =
    txRunner.inRwTransaction { userTagRepository.hideGlobalTag(userId, tagId) }

  suspend fun unhide(userId: UserId, tagId: TagId): UpdateResourceResult<TagId> =
    txRunner.inRwTransaction { userTagRepository.unhideGlobalTag(userId, tagId) }

  suspend fun overrideColor(userId: UserId, tagId: TagId, color: Color): UpdateResourceResult<TagId> =
    txRunner.inRwTransaction { userTagRepository.overrideGlobalTagColor(userId, tagId, color) }

  suspend fun reset(userId: UserId, tagId: TagId): UpdateResourceResult<TagId> =
    txRunner.inRwTransaction { userTagRepository.resetGlobalTagOverride(userId, tagId) }
}

