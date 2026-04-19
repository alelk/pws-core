package io.github.alelk.pws.api.client.api

import arrow.core.Either
import io.github.alelk.pws.api.contract.admin.AdminTags
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import io.github.alelk.pws.api.contract.tag.TagCreateRequestDto
import io.github.alelk.pws.api.contract.tag.TagDetailDto
import io.github.alelk.pws.api.contract.tag.TagSortDto
import io.github.alelk.pws.api.contract.tag.TagSummaryDto
import io.github.alelk.pws.api.contract.tag.TagUpdateRequestDto
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

/**
 * Admin Tag API client for managing global tags.
 * Requires admin role.
 */
interface AdminTagApi {
  suspend fun list(sort: TagSortDto? = null): List<TagSummaryDto>
  suspend fun get(id: TagIdDto): TagDetailDto?
  suspend fun create(request: TagCreateRequestDto): Either<CreateError, TagIdDto>
  suspend fun update(id: TagIdDto, request: TagUpdateRequestDto): Either<UpdateError, TagIdDto>
  suspend fun delete(id: TagIdDto): Either<DeleteError, TagIdDto>
  suspend fun listSongs(id: TagIdDto): List<SongIdDto>
}

internal class AdminTagApiImpl(client: HttpClient) : BaseResourceApi(client), AdminTagApi {

  override suspend fun list(sort: TagSortDto?): List<TagSummaryDto> =
    execute<List<TagSummaryDto>> { client.get(AdminTags(sort = sort)) }.getOrThrow()

  override suspend fun get(id: TagIdDto): TagDetailDto? =
    executeGet<TagDetailDto> { client.get(AdminTags.ById(id = id)) }.getOrThrow()

  override suspend fun create(request: TagCreateRequestDto): Either<CreateError, TagIdDto> =
    executeCreate<TagDetailDto, TagIdDto>(resource = request.id) {
      client.post(AdminTags()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }

  override suspend fun update(id: TagIdDto, request: TagUpdateRequestDto): Either<UpdateError, TagIdDto> =
    executeUpdate<TagDetailDto, TagIdDto>(resourceId = id) {
      client.put(AdminTags.ById(id = id)) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }

  override suspend fun delete(id: TagIdDto): Either<DeleteError, TagIdDto> =
    executeDelete<TagIdDto>(resourceId = id) {
      client.delete(AdminTags.ById(id = id))
    }

  override suspend fun listSongs(id: TagIdDto): List<SongIdDto> =
    execute<List<SongIdDto>> {
      client.get(AdminTags.ById.Songs(parent = AdminTags.ById(id = id)))
    }.getOrThrow()
}
