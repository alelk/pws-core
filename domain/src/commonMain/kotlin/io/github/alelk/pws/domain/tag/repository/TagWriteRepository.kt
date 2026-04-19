package io.github.alelk.pws.domain.tag.repository

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.tag.command.CreateTagCommand
import io.github.alelk.pws.domain.tag.command.UpdateTagCommand

/**
 * Mutation operations for Tag aggregate.
 * @param ID The type of TagId this repository works with
 */
interface TagWriteRepository<ID : TagId> {
  suspend fun create(command: CreateTagCommand<ID>): Either<CreateError, ID>
  suspend fun update(command: UpdateTagCommand<ID>): Either<UpdateError, ID>
  suspend fun delete(id: ID): Either<DeleteError, ID>
}
