package io.github.alelk.pws.domain.songreference.repository

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.songreference.command.CreateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.command.UpdateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.model.SongReference

/** Write operations for Song-to-Song references. */
interface SongReferenceWriteRepository {
  suspend fun create(command: CreateSongReferenceCommand): Either<CreateError, SongReference>
  suspend fun update(command: UpdateSongReferenceCommand): Either<UpdateError, SongReference>
  suspend fun delete(songId: SongId, refSongId: SongId): Either<DeleteError, SongReference>

  /** Delete all references for a song. */
  suspend fun deleteAllForSong(songId: SongId): Int
}
