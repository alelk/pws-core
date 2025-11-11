package io.github.alelk.pws.api.client.repository

import io.github.alelk.pws.api.client.api.BookApi
import io.github.alelk.pws.api.client.api.SongApi
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.api.mapping.song.toDomain
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.song.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoteSongRepository(private val api: SongApi, private val bookApi: BookApi) : SongRepository {
  override fun observe(id: SongId): Flow<SongDetail?> = flow { emit(api.get(id.toDto())?.toDomain()) }

  override fun observeAllInBook(bookId: BookId): Flow<Map<Int, SongSummary>> =
    flow {
      emit(bookApi.listBookSongs(bookId.toDto())?.mapValues { (_, songDto) -> songDto.toDomain() } ?: emptyMap())
    }

  override suspend fun get(id: SongId): SongDetail? = api.get(id.toDto())?.toDomain()

  override suspend fun delete(id: SongId) = throw NotImplementedError("RemoteSongRepository does not support delete")
}

