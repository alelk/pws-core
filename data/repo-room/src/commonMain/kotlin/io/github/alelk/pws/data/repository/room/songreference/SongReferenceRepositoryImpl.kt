package io.github.alelk.pws.data.repository.room.songreference

import arrow.core.Either
import io.github.alelk.pws.database.song_reference.SongReferenceDao
import io.github.alelk.pws.database.song_reference.SongReferenceEntity
import io.github.alelk.pws.domain.core.SongRefReason as DomainSongRefReason
import io.github.alelk.pws.database.song_reference.SongRefReason as DbSongRefReason
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.songreference.command.CreateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.command.UpdateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.model.SongReference
import io.github.alelk.pws.domain.songreference.repository.SongReferenceReadRepository
import io.github.alelk.pws.domain.songreference.repository.SongReferenceWriteRepository

class SongReferenceRepositoryImpl(
  private val songReferenceDao: SongReferenceDao,
) : SongReferenceReadRepository, SongReferenceWriteRepository {

  // --- Read ---

  override suspend fun get(songId: SongId, refSongId: SongId): SongReference? =
    songReferenceDao.getById(songId, refSongId)?.toDomain()

  override suspend fun getReferencesForSong(songId: SongId): List<SongReference> =
    songReferenceDao.getBySongId(songId).map { it.toDomain() }

  override suspend fun getReferencesToSong(refSongId: SongId): List<SongReference> =
    songReferenceDao.getBySongIds(listOf(refSongId)).map { it.toDomain() }

  override suspend fun exists(songId: SongId, refSongId: SongId): Boolean =
    songReferenceDao.getById(songId, refSongId) != null

  override suspend fun count(): Long = songReferenceDao.count().toLong()

  // --- Write ---

  override suspend fun create(command: CreateSongReferenceCommand): Either<CreateError, SongReference> =
    runCatching {
      val entity = SongReferenceEntity(
        songId = command.songId,
        refSongId = command.refSongId,
        reason = DbSongRefReason.fromIdentifier(command.reason.identifier),
        volume = command.volume,
        priority = command.priority,
      )
      songReferenceDao.insert(entity)
      Either.Right(entity.toDomain())
    }.getOrElse { Either.Left(CreateError.UnknownError(it)) }

  override suspend fun update(command: UpdateSongReferenceCommand): Either<UpdateError, SongReference> =
    runCatching {
      val existing = songReferenceDao.getById(command.songId, command.refSongId)
        ?: return Either.Left(UpdateError.NotFound)
      val updated = existing.copy(
        reason = command.reason?.let { DbSongRefReason.fromIdentifier(it.identifier) } ?: existing.reason,
        volume = command.volume ?: existing.volume,
        priority = command.priority ?: existing.priority,
      )
      songReferenceDao.update(updated)
      Either.Right(updated.toDomain())
    }.getOrElse { Either.Left(UpdateError.UnknownError(it)) }

  override suspend fun delete(songId: SongId, refSongId: SongId): Either<DeleteError, SongReference> =
    runCatching {
      val existing = songReferenceDao.getById(songId, refSongId)
        ?: return Either.Left(DeleteError.NotFound)
      songReferenceDao.delete(existing)
      Either.Right(existing.toDomain())
    }.getOrElse { Either.Left(DeleteError.UnknownError(it)) }

  override suspend fun deleteAllForSong(songId: SongId): Int {
    val toDelete = songReferenceDao.getBySongId(songId)
    toDelete.forEach { songReferenceDao.delete(it) }
    return toDelete.size
  }
}

private fun SongReferenceEntity.toDomain() = SongReference(
  songId = songId,
  refSongId = refSongId,
  reason = DomainSongRefReason.fromIdentifier(reason.identifier),
  volume = volume,
  priority = priority,
)
