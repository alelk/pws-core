package io.github.alelk.pws.api.client.api

import arrow.core.Either
import io.github.alelk.pws.api.contract.admin.AdminSongReferences
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.songreference.ReplaceAllSongReferencesResultDto
import io.github.alelk.pws.api.contract.songreference.SongReferenceCreateRequestDto
import io.github.alelk.pws.api.contract.songreference.SongReferenceDto
import io.github.alelk.pws.api.contract.songreference.SongReferenceUpdateRequestDto
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
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
 * Admin Song Reference API client for managing song-to-song references.
 * Requires admin role.
 */
interface AdminSongReferenceApi {
  suspend fun list(songId: SongIdDto): List<SongReferenceDto>
  suspend fun create(songId: SongIdDto, request: SongReferenceCreateRequestDto): Either<CreateError, SongReferenceDto>
  suspend fun replace(songId: SongIdDto, references: List<SongReferenceDto>): ReplaceAllSongReferencesResultDto
  suspend fun update(songId: SongIdDto, refSongId: SongIdDto, request: SongReferenceUpdateRequestDto): Either<UpdateError, SongReferenceDto>
  suspend fun delete(songId: SongIdDto, refSongId: SongIdDto): Either<DeleteError, SongReferenceDto>
}

internal class AdminSongReferenceApiImpl(client: HttpClient) : BaseResourceApi(client), AdminSongReferenceApi {

  override suspend fun list(songId: SongIdDto): List<SongReferenceDto> =
    execute<List<SongReferenceDto>> {
      client.get(AdminSongReferences(songId = songId))
    }.getOrThrow()

  override suspend fun create(songId: SongIdDto, request: SongReferenceCreateRequestDto): Either<CreateError, SongReferenceDto> =
    executeCreate<SongReferenceDto, SongReferenceDto>(
      resource = SongReferenceDto(
        songId = songId,
        refSongId = request.refSongId,
        reason = request.reason,
        volume = request.volume,
        priority = request.priority
      )
    ) {
      client.post(AdminSongReferences(songId = songId)) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }

  override suspend fun replace(songId: SongIdDto, references: List<SongReferenceDto>): ReplaceAllSongReferencesResultDto =
    execute<ReplaceAllSongReferencesResultDto> {
      client.put(AdminSongReferences(songId = songId)) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(references)
      }
    }.getOrThrow()

  override suspend fun update(songId: SongIdDto, refSongId: SongIdDto, request: SongReferenceUpdateRequestDto): Either<UpdateError, SongReferenceDto> =
    executeUpdate<SongReferenceDto, SongReferenceDto>(
      resourceId = SongReferenceDto(
        songId = songId,
        refSongId = refSongId,
        reason = request.reason ?: throw IllegalArgumentException("reason is required for update result"),
        volume = request.volume ?: throw IllegalArgumentException("volume is required for update result"),
        priority = request.priority ?: 0
      )
    ) {
      client.patch(AdminSongReferences.ByRefSongId(parent = AdminSongReferences(songId = songId), refSongId = refSongId)) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }

  override suspend fun delete(songId: SongIdDto, refSongId: SongIdDto): Either<DeleteError, SongReferenceDto> =
    executeDelete<SongReferenceDto>(
      resourceId = SongReferenceDto(
        songId = songId,
        refSongId = refSongId,
        reason = io.github.alelk.pws.api.contract.songreference.SongRefReasonDto.VARIATION,
        volume = 0,
        priority = 0
      )
    ) {
      client.delete(AdminSongReferences.ByRefSongId(parent = AdminSongReferences(songId = songId), refSongId = refSongId))
    }
}
