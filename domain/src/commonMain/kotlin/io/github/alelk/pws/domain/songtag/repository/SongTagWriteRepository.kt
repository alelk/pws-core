package io.github.alelk.pws.domain.songtag.repository

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.songtag.model.SongTagAssociation

/** Mutation operations for Song-Tag associations. */
interface SongTagWriteRepository<ID : TagId> {
  suspend fun create(songId: SongId, tagId: ID): Either<CreateError, SongTagAssociation<ID>>
  suspend fun delete(songId: SongId, tagId: ID): Either<DeleteError, SongTagAssociation<ID>>
}
