package io.github.alelk.pws.domain.tag.repository

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.UpdateResourceResult
import io.github.alelk.pws.domain.tag.command.CreateTagCommand
import io.github.alelk.pws.domain.tag.command.UpdateTagCommand

/**
 * Mutation operations for Tag aggregate.
 */
interface TagWriteRepository {
  suspend fun create(command: CreateTagCommand): CreateResourceResult<TagId>
  suspend fun update(command: UpdateTagCommand): UpdateResourceResult<TagId>
  suspend fun delete(id: TagId): DeleteResourceResult<TagId>
}

