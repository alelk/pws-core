package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import io.github.alelk.pws.api.contract.tag.TagDetailDto
import io.github.alelk.pws.api.contract.tag.TagSortDto
import io.github.alelk.pws.api.contract.tag.TagSummaryDto
import io.github.alelk.pws.api.contract.tag.Tags
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get

/**
 * Tag API client for reading global tags (public, read-only).
 */
interface TagApi {
  suspend fun list(sort: TagSortDto? = null): List<TagSummaryDto>
  suspend fun get(id: TagIdDto): TagDetailDto?
  suspend fun listSongs(id: TagIdDto): List<SongIdDto>
}

internal class TagApiImpl(client: HttpClient) : BaseResourceApi(client), TagApi {

  override suspend fun list(sort: TagSortDto?): List<TagSummaryDto> =
    execute<List<TagSummaryDto>> { client.get(Tags(sort = sort)) }.getOrThrow()

  override suspend fun get(id: TagIdDto): TagDetailDto? =
    executeGet<TagDetailDto> { client.get(Tags.ById(id = id)) }.getOrThrow()

  override suspend fun listSongs(id: TagIdDto): List<SongIdDto> =
    execute<List<SongIdDto>> {
      client.get(Tags.ById.Songs(parent = Tags.ById(id = id)))
    }.getOrThrow()
}

