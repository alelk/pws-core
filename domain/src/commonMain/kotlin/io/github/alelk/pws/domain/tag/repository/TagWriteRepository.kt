package io.github.alelk.pws.domain.tag.repository

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.tag.command.CreateTagCommand
import io.github.alelk.pws.domain.tag.command.UpdateTagCommand

/**
 * Mutation operations for Tag aggregate.
 * @param ID The type of TagId this repository works with
 */
interface TagWriteRepository<ID : TagId> {
  suspend fun create(command: CreateTagCommand<ID>): CreateResourceResult<ID>
  suspend fun update(command: UpdateTagCommand<ID>): UpdateResourceResult<ID>
  suspend fun delete(id: ID): DeleteResourceResult<ID>
}

