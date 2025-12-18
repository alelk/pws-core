package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.command.UpdateTagCommand
import io.github.alelk.pws.domain.tag.repository.UserTagWriteRepository

/**
 * Use case: update a user's custom tag.
 */
class UpdateUserTagUseCase(
  private val userTagRepository: UserTagWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, command: UpdateTagCommand): UpdateResourceResult<TagId> =
    txRunner.inRwTransaction { userTagRepository.updateUserTag(userId, command) }
}

