package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SongDetailDto
import io.github.alelk.pws.api.contract.song.Songs
import io.ktor.client.plugins.resources.get

interface SongApi {
  suspend fun get(id: SongIdDto): SongDetailDto?
}

class SongApiImpl(client: io.ktor.client.HttpClient) : BaseResourceApi(client), SongApi {
  override suspend fun get(id: SongIdDto): SongDetailDto? = executeGet<SongDetailDto> { client.get(Songs.ById(id = id)) }.getOrThrow()
}

