package io.github.alelk.pws.domain.auth.repository

import io.github.alelk.pws.domain.auth.command.CreateUserCommand
import io.github.alelk.pws.domain.auth.command.UpdateUserCommand
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.UpdateResourceResult

interface UserWriteRepository {
  suspend fun create(command: CreateUserCommand): CreateResourceResult<UserId>
  suspend fun update(command: UpdateUserCommand): UpdateResourceResult<UserId>
  suspend fun delete(id: UserId): DeleteResourceResult<UserId>

  suspend fun linkTelegramAccount(userId: UserId, telegramProviderId: String): UpdateResourceResult<UserId>
}

