package io.github.alelk.pws.api.client.api

import SongUpdateRequestDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SongCreateRequestDto
import io.github.alelk.pws.api.contract.song.SongDetailDto
import io.github.alelk.pws.api.contract.song.SongSortDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
import io.github.alelk.pws.api.contract.song.Songs
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.patch
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

interface SongApi {
  suspend fun get(id: SongIdDto): SongDetailDto?
  suspend fun list(
    bookId: BookIdDto? = null,
    minNumber: Int? = null,
    maxNumber: Int? = null,
    sort: SongSortDto? = null
  ): List<SongSummaryDto>
  suspend fun create(request: SongCreateRequestDto): ResourceCreateResult<SongIdDto>

  suspend fun update(request: SongUpdateRequestDto): ResourceUpdateResult<SongIdDto>
}

internal class SongApiImpl(client: HttpClient) : BaseResourceApi(client), SongApi {

  override suspend fun get(id: SongIdDto): SongDetailDto? =
    executeGet<SongDetailDto> { client.get(Songs.ById(id = id)) }.getOrThrow()

  override suspend fun create(request: SongCreateRequestDto): ResourceCreateResult<SongIdDto> =
    executeCreate<String, SongIdDto>(resource = request.id) {
      client.post(Songs()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun update(request: SongUpdateRequestDto): ResourceUpdateResult<SongIdDto> =
    executeUpdate<String, SongIdDto>(resourceId = request.id) {
      client.patch(Songs.ById(id = request.id)) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun list(
    bookId: BookIdDto?,
    minNumber: Int?,
    maxNumber: Int?,
    sort: SongSortDto?
  ): List<SongSummaryDto> =
    execute<List<SongSummaryDto>> { client.get(Songs(bookId = bookId, minNumber = minNumber, maxNumber = maxNumber, sort = sort)) }
      .getOrThrow()
}
