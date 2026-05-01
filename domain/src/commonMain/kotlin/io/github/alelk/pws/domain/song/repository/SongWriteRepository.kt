package io.github.alelk.pws.domain.song.repository

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.command.UpdateSongCommand

/** Mutation operations for Song aggregate. */
interface SongWriteRepository {
  /** Create a new song. */
  suspend fun create(command: CreateSongCommand): Either<CreateError, SongId>

  /** Save/Update song entity. */
  suspend fun update(song: SongDetail): Either<UpdateError, SongId>

  suspend fun delete(id: SongId): Either<DeleteError, SongId>
}
