package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SongDetailDto
import io.github.alelk.pws.api.contract.song.SongSortDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
import io.github.alelk.pws.api.contract.song.Songs
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get

/**
 * Public read-only Song API client.
 * For write operations, use [AdminSongApi].
 */
interface SongApi {
  suspend fun get(id: SongIdDto): SongDetailDto?
  suspend fun list(
    bookId: BookIdDto? = null,
    minNumber: Int? = null,
    maxNumber: Int? = null,
    sort: SongSortDto? = null
  ): List<SongSummaryDto>
}

internal class SongApiImpl(client: HttpClient) : BaseResourceApi(client), SongApi {

  override suspend fun get(id: SongIdDto): SongDetailDto? =
    executeGet<SongDetailDto> { client.get(Songs.ById(id = id)) }.getOrThrow()

  override suspend fun list(
    bookId: BookIdDto?,
    minNumber: Int?,
    maxNumber: Int?,
    sort: SongSortDto?
  ): List<SongSummaryDto> =
    execute<List<SongSummaryDto>> { client.get(Songs(bookId = bookId, minNumber = minNumber, maxNumber = maxNumber, sort = sort)) }
      .getOrThrow()
}
