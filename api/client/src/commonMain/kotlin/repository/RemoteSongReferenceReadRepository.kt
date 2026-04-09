package io.github.alelk.pws.api.client.repository

import io.github.alelk.pws.api.client.api.SongReferenceApi
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.api.mapping.songreference.toDomain
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.songreference.model.SongReference
import io.github.alelk.pws.domain.songreference.repository.SongReferenceReadRepository

class RemoteSongReferenceReadRepository(private val api: SongReferenceApi) : SongReferenceReadRepository {

  override suspend fun get(songId: SongId, refSongId: SongId): SongReference? =
    api.list(songId.toDto()).map { it.toDomain() }
      .firstOrNull { it.songId == songId && it.refSongId == refSongId }

  override suspend fun getReferencesForSong(songId: SongId): List<SongReference> =
    api.list(songId.toDto()).map { it.toDomain() }
      .filter { it.songId == songId }

  override suspend fun getReferencesToSong(refSongId: SongId): List<SongReference> =
    api.list(refSongId.toDto()).map { it.toDomain() }
      .filter { it.refSongId == refSongId }

  override suspend fun exists(songId: SongId, refSongId: SongId): Boolean =
    get(songId, refSongId) != null

  override suspend fun count(): Long =
    throw UnsupportedOperationException("count() is not supported by RemoteSongReferenceReadRepository")
}

