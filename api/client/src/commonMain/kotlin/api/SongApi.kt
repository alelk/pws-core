package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SongCreateRequestDto
import io.github.alelk.pws.api.contract.song.SongDetailDto
import io.github.alelk.pws.api.contract.song.Songs
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody

interface SongApi {
  suspend fun get(id: SongIdDto): SongDetailDto?
  suspend fun create(request: SongCreateRequestDto): SongDetailDto
}

class SongApiImpl(client: io.ktor.client.HttpClient) : BaseResourceApi(client), SongApi {
  override suspend fun get(id: SongIdDto): SongDetailDto? =
    executeGet<SongDetailDto> { client.get(Songs.ById(id = id)) }.getOrThrow()

  override suspend fun create(request: SongCreateRequestDto): SongDetailDto =
    execute<SongDetailDto> { client.post(Songs()) { setBody(request) } }.mapCatching { body -> body }.getOrThrow()
}
