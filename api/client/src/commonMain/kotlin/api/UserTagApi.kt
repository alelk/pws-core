package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import io.github.alelk.pws.api.contract.tag.TagCreateRequestDto
import io.github.alelk.pws.api.contract.tag.TagDetailDto
import io.github.alelk.pws.api.contract.tag.TagSortDto
import io.github.alelk.pws.api.contract.tag.TagSummaryDto
import io.github.alelk.pws.api.contract.tag.TagUpdateRequestDto
import io.github.alelk.pws.api.contract.usertag.UserTags
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
 * User Tag API client for managing user's tags.
 * Users can:
 * - View all tags (global + custom, with user overrides applied)
 * - Create custom tags
 * - Update custom tags or override global tag properties (via PUT)
 * - Delete custom tags or hide global tags (via DELETE)
 * - Manage song-tag associations
 */
interface UserTagApi {
  // Tag operations
  suspend fun list(sort: TagSortDto? = null): List<TagSummaryDto>
  suspend fun get(id: TagIdDto): TagDetailDto?
  suspend fun create(request: TagCreateRequestDto): ResourceCreateResult<TagIdDto>
  /** Update tag. For custom tags - updates the tag. For predefined tags - creates/updates override. */
  suspend fun update(id: TagIdDto, request: TagUpdateRequestDto): ResourceUpdateResult<TagIdDto>
  /** Delete tag. For custom tags - deletes. For predefined tags - hides for user. */
  suspend fun delete(id: TagIdDto): ResourceDeleteResult<TagIdDto>

  // Song-tag associations
  suspend fun listSongs(id: TagIdDto): List<SongIdDto>
  suspend fun addSongTag(tagId: TagIdDto, songId: SongIdDto): ResourceCreateResult<Unit>
  suspend fun removeSongTag(tagId: TagIdDto, songId: SongIdDto): ResourceDeleteResult<Unit>
}

internal class UserTagApiImpl(client: HttpClient) : BaseResourceApi(client), UserTagApi {

  // Tag operations

  override suspend fun list(sort: TagSortDto?): List<TagSummaryDto> =
    execute<List<TagSummaryDto>> { client.get(UserTags(sort = sort)) }.getOrThrow()

  override suspend fun get(id: TagIdDto): TagDetailDto? =
    executeGet<TagDetailDto> { client.get(UserTags.ById(id = id)) }.getOrThrow()

  override suspend fun create(request: TagCreateRequestDto): ResourceCreateResult<TagIdDto> =
    executeCreate<TagDetailDto, TagIdDto>(resource = request.id) {
      client.post(UserTags()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun update(id: TagIdDto, request: TagUpdateRequestDto): ResourceUpdateResult<TagIdDto> =
    executeUpdate<TagDetailDto, TagIdDto>(resourceId = id) {
      client.put(UserTags.ById(id = id)) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun delete(id: TagIdDto): ResourceDeleteResult<TagIdDto> =
    executeDelete<TagIdDto>(resourceId = id) {
      client.delete(UserTags.ById(id = id))
    }.getOrThrow()

  // Song-tag associations

  override suspend fun listSongs(id: TagIdDto): List<SongIdDto> =
    execute<List<SongIdDto>> {
      client.get(UserTags.ById.Songs(parent = UserTags.ById(id = id)))
    }.getOrThrow()

  override suspend fun addSongTag(tagId: TagIdDto, songId: SongIdDto): ResourceCreateResult<Unit> =
    executeCreate<Unit, Unit>(resource = Unit) {
      client.post(UserTags.ById.Songs.BySongId(parent = UserTags.ById.Songs(parent = UserTags.ById(id = tagId)), songId = songId))
    }.getOrThrow()

  override suspend fun removeSongTag(tagId: TagIdDto, songId: SongIdDto): ResourceDeleteResult<Unit> =
    executeDelete<Unit>(resourceId = Unit) {
      client.delete(UserTags.ById.Songs.BySongId(parent = UserTags.ById.Songs(parent = UserTags.ById(id = tagId)), songId = songId))
    }.getOrThrow()
}
