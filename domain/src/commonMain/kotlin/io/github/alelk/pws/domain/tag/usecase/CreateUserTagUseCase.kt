package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.tag.command.CreateTagCommand
import io.github.alelk.pws.domain.tag.repository.UserTagWriteRepository

/**
 * Use case: create a custom tag for a user.
 */
class CreateUserTagUseCase(
  private val userTagRepository: UserTagWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(userId: UserId, command: CreateTagCommand): CreateResourceResult<TagId> =
    txRunner.inRwTransaction { userTagRepository.createUserTag(userId, command) }
}

