package io.github.alelk.pws.data.repository.room.songreference

import io.github.alelk.pws.database.song_reference.SongReferenceDao
import io.github.alelk.pws.domain.core.SongRefReason as DomainSongRefReason
import io.github.alelk.pws.database.song_reference.SongRefReason as DbSongRefReason
import io.github.alelk.pws.database.song_reference.SongReferenceEntity
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.songreference.model.SongReference
import io.github.alelk.pws.domain.songreference.repository.SongReferenceReadRepository

class SongReferenceRepositoryImpl(
  private val songReferenceDao: SongReferenceDao,
) : SongReferenceReadRepository {

  override suspend fun get(songId: SongId, refSongId: SongId): SongReference? =
    songReferenceDao.getById(songId, refSongId)?.toDomain()

  override suspend fun getReferencesForSong(songId: SongId): List<SongReference> =
    songReferenceDao.getBySongId(songId).map { it.toDomain() }

  override suspend fun getReferencesToSong(refSongId: SongId): List<SongReference> =
    songReferenceDao.getBySongIds(listOf(refSongId)).map { it.toDomain() }

  override suspend fun exists(songId: SongId, refSongId: SongId): Boolean =
    songReferenceDao.getById(songId, refSongId) != null

  override suspend fun count(): Long = songReferenceDao.count().toLong()
}

private fun io.github.alelk.pws.database.song_reference.SongReferenceEntity.toDomain() = SongReference(
  songId = songId,
  refSongId = refSongId,
  reason = DomainSongRefReason.fromIdentifier(reason.identifier),
  volume = volume,
  priority = priority,
)




