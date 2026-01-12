package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.usersong.UserSongDetailDto
import io.github.alelk.pws.api.contract.usersong.UserSongOverrideRequestDto
import io.github.alelk.pws.api.contract.usersong.UserSongs
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.patch
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

/**
 * User Song API client for managing user's song overrides.
 *
 * Provides unified view of songs with user overrides applied:
 * - Get merged song (global + user overrides)
 * - Apply override to global song
 * - Reset override (restore to global)
 *
 * Requires user authentication.
 */
interface UserSongApi {
  /**
   * Get song with user's overrides applied (merged view).
   * Returns null if song doesn't exist.
   */
  suspend fun getSong(id: SongIdDto): UserSongDetailDto?

  /**
   * Get list of song IDs that have user overrides.
   */
  suspend fun getOverriddenSongIds(): List<SongIdDto>

  /**
   * Apply override to a global song.
   * Creates or updates user's override for this song.
   */
  suspend fun overrideSong(id: SongIdDto, request: UserSongOverrideRequestDto): ResourceUpdateResult<SongIdDto>

  /**
   * Reset user's overrides for a song (restore to global version).
   */
  suspend fun resetOverride(id: SongIdDto): ResourceDeleteResult<SongIdDto>
}

internal class UserSongApiImpl(client: HttpClient) : BaseResourceApi(client), UserSongApi {

  override suspend fun getSong(id: SongIdDto): UserSongDetailDto? =
    executeGet<UserSongDetailDto> {
      client.get(UserSongs.ById(id = id))
    }.getOrThrow()

  override suspend fun getOverriddenSongIds(): List<SongIdDto> =
    execute<List<SongIdDto>> {
      client.get(UserSongs(overriddenOnly = true))
    }.getOrThrow()

  override suspend fun overrideSong(id: SongIdDto, request: UserSongOverrideRequestDto): ResourceUpdateResult<SongIdDto> =
    executeUpdate<SongIdDto, SongIdDto>(resourceId = id) {
      client.patch(UserSongs.ById(id = id)) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(request)
      }
    }.getOrThrow()

  override suspend fun resetOverride(id: SongIdDto): ResourceDeleteResult<SongIdDto> =
    executeDelete<SongIdDto>(resourceId = id) {
      client.delete(UserSongs.ById.Override(parent = UserSongs.ById(id = id)))
    }.getOrThrow()
}

