// ...existing code...
package io.github.alelk.pws.api.client.repository

import io.github.alelk.pws.api.client.api.BookApi
import io.github.alelk.pws.api.client.api.SongApi
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.api.mapping.song.toDomain
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.song.query.SongQuery
import io.github.alelk.pws.domain.song.query.SongSort
import io.github.alelk.pws.domain.song.repository.SongReadRepository

class RemoteSongReadRepository(
  private val api: SongApi,
  private val bookApi: BookApi
) : SongReadRepository {

  override suspend fun get(id: SongId): SongDetail? =
    api.get(id.toDto())?.toDomain()

  override suspend fun getMany(query: SongQuery, sort: SongSort): List<SongSummary> {
    query.bookId?.let { bookId ->
      val map = bookApi.listBookSongs(bookId.toDto())
      val items = map?.values?.map { it.toDomain() } ?: emptyList()
      // Note: server sorts by number; we return as-is. If sort by number requested, client may need to re-order.
      return items
    }
    // No generic songs list endpoint available in API
    error("bookId parameter required")
  }
}

