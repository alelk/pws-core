package io.github.alelk.pws.api.client.api

import SongUpdateRequestDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SongCreateRequestDto
import io.github.alelk.pws.api.contract.song.SongDetailDto
import io.github.alelk.pws.api.contract.song.Songs
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

interface SongApi {
  suspend fun get(id: SongIdDto): SongDetailDto?
  suspend fun create(request: SongCreateRequestDto): ResourceCreateResult<SongIdDto>

  suspend fun update(request: SongUpdateRequestDto): ResourceUpdateResult<SongIdDto>
}

internal class SongApiImpl(client: HttpClient) : BaseResourceApi(client), SongApi {

  override suspend fun get(id: SongIdDto): SongDetailDto? =
    executeGet<SongDetailDto> { client.get(Songs.ById(id = id)) }.getOrThrow()

  override suspend fun create(request: SongCreateRequestDto): ResourceCreateResult<SongIdDto> =
    executeCreate<String, SongIdDto>(resourceId = request.id) {
      client.post(Songs.Create()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun update(request: SongUpdateRequestDto): ResourceUpdateResult<SongIdDto> =
    executeUpdate<String, SongIdDto>(resourceId = request.id) {
      client.put(Songs.ById.Update(parent = Songs.ById(id = request.id))) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()
}
