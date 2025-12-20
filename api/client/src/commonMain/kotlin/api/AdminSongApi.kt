package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.admin.AdminSongs
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import io.github.alelk.pws.api.contract.song.SongCreateRequestDto
import io.github.alelk.pws.api.contract.song.SongDetailDto
import io.github.alelk.pws.api.contract.song.SongSortDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
import io.github.alelk.pws.api.contract.song.SongUpdateRequestDto
import io.github.alelk.pws.api.contract.tag.songtag.ReplaceAllSongTagsResultDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.patch
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

/**
 * Admin Song API client for managing global songs.
 * Requires admin role.
 */
interface AdminSongApi {
  suspend fun get(id: SongIdDto): SongDetailDto?
  suspend fun list(
    bookId: BookIdDto? = null,
    minNumber: Int? = null,
    maxNumber: Int? = null,
    sort: SongSortDto? = null
  ): List<SongSummaryDto>
  suspend fun create(request: SongCreateRequestDto): ResourceCreateResult<SongIdDto>
  suspend fun update(id: SongIdDto, request: SongUpdateRequestDto): ResourceUpdateResult<SongIdDto>
  suspend fun delete(id: SongIdDto): ResourceDeleteResult<SongIdDto>

  // Tag operations
  suspend fun listTags(id: SongIdDto): List<TagIdDto>
  suspend fun replaceTags(id: SongIdDto, tagIds: List<TagIdDto>): ReplaceAllSongTagsResultDto
}

internal class AdminSongApiImpl(client: HttpClient) : BaseResourceApi(client), AdminSongApi {

  override suspend fun get(id: SongIdDto): SongDetailDto? =
    executeGet<SongDetailDto> { client.get(AdminSongs.ById(id = id)) }.getOrThrow()

  override suspend fun list(
    bookId: BookIdDto?,
    minNumber: Int?,
    maxNumber: Int?,
    sort: SongSortDto?
  ): List<SongSummaryDto> =
    execute<List<SongSummaryDto>> {
      client.get(AdminSongs(bookId = bookId, minNumber = minNumber, maxNumber = maxNumber, sort = sort))
    }.getOrThrow()

  override suspend fun create(request: SongCreateRequestDto): ResourceCreateResult<SongIdDto> =
    executeCreate<SongIdDto, SongIdDto>(resource = request.id) {
      client.post(AdminSongs()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun update(id: SongIdDto, request: SongUpdateRequestDto): ResourceUpdateResult<SongIdDto> =
    executeUpdate<SongIdDto, SongIdDto>(resourceId = id) {
      client.patch(AdminSongs.ById(id = id)) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun delete(id: SongIdDto): ResourceDeleteResult<SongIdDto> =
    executeDelete<SongIdDto>(resourceId = id) {
      client.delete(AdminSongs.ById(id = id))
    }.getOrThrow()

  // Tag operations

  override suspend fun listTags(id: SongIdDto): List<TagIdDto> =
    execute<List<TagIdDto>> {
      client.get(AdminSongs.ById.Tags(parent = AdminSongs.ById(id = id)))
    }.getOrThrow()

  override suspend fun replaceTags(id: SongIdDto, tagIds: List<TagIdDto>): ReplaceAllSongTagsResultDto =
    execute<ReplaceAllSongTagsResultDto> {
      client.put(AdminSongs.ById.Tags(parent = AdminSongs.ById(id = id))) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(tagIds)
      }
    }.getOrThrow()
}

