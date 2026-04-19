package io.github.alelk.pws.domain.auth.repository

import arrow.core.Either
import io.github.alelk.pws.domain.auth.command.CreateUserCommand
import io.github.alelk.pws.domain.auth.command.UpdateUserCommand
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.UserId

interface UserWriteRepository {
  suspend fun create(command: CreateUserCommand): Either<CreateError, UserId>
  suspend fun update(command: UpdateUserCommand): Either<UpdateError, UserId>
  suspend fun delete(id: UserId): Either<DeleteError, UserId>
  suspend fun linkTelegramAccount(userId: UserId, telegramProviderId: String): Either<UpdateError, UserId>
}
