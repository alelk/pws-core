package io.github.alelk.pws.api.client.repository

import io.github.alelk.pws.api.client.api.SongApi
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.api.mapping.song.toDomain
import io.github.alelk.pws.api.mapping.song.toDto
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.song.query.SongQuery
import io.github.alelk.pws.domain.song.query.SongSort
import io.github.alelk.pws.domain.song.repository.SongReadRepository

class RemoteSongReadRepository(private val api: SongApi) : SongReadRepository {

  override suspend fun get(id: SongId): SongDetail? =
    api.get(id.toDto())?.toDomain()

  override suspend fun getMany(query: SongQuery, sort: SongSort): List<SongSummary> =
    api.list(
      bookId = query.bookId?.toDto(),
      minNumber = query.minNumber,
      maxNumber = query.maxNumber,
      sort = sort.toDto()
    ).map { it.toDomain() }
}

