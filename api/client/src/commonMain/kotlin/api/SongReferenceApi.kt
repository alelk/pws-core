package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.Songs
import io.github.alelk.pws.api.contract.songreference.SongReferenceDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get

/**
 * Public read-only API for song-to-song references.
 * Fetches both outgoing and incoming references for a given song.
 */
interface SongReferenceApi {
  /** Get all references (outgoing + incoming) for a song. Returns empty list if not found. */
  suspend fun list(songId: SongIdDto): List<SongReferenceDto>
}

internal class SongReferenceApiImpl(client: HttpClient) : BaseResourceApi(client), SongReferenceApi {

  override suspend fun list(songId: SongIdDto): List<SongReferenceDto> =
    execute<List<SongReferenceDto>> {
      client.get(Songs.ById.References(parent = Songs.ById(id = songId)))
    }.getOrElse { emptyList() }
}


