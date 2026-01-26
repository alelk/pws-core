package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SearchTypeDto
import io.github.alelk.pws.api.contract.song.SongSearchResponseDto
import io.github.alelk.pws.api.contract.song.SongSearchSuggestionDto
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
 * User Song API client for managing user's song overrides and search.
 *
 * Provides unified view of songs with user overrides applied:
 * - Get merged song (global + user overrides)
 * - Apply override to global song
 * - Reset override (restore to global)
 * - Search in both global and user's songs (merged)
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

  /**
   * Full-text search on user's songs (merged: global + user's songs).
   *
   * Searches both global songs catalog and user's custom songbooks
   * with unified ranking.
   *
   * @param query Search text or song number
   * @param type Search type (ALL, NAME, LYRIC, NUMBER). Default: ALL
   * @param bookId Optional filter by specific book ID
   * @param limit Maximum results (default: 20, max: 100)
   * @param offset Pagination offset (default: 0)
   * @param highlight Enable snippet highlighting (default: true)
   * @return Search response with results and pagination info
   */
  suspend fun search(
    query: String,
    type: SearchTypeDto? = null,
    bookId: BookIdDto? = null,
    limit: Int? = null,
    offset: Int? = null,
    highlight: Boolean? = null
  ): SongSearchResponseDto

  /**
   * Get search suggestions for autocomplete (merged: global + user's songs).
   *
   * @param query Search text or song number
   * @param bookId Optional filter by specific book ID
   * @param limit Maximum suggestions (default: 10, max: 50)
   * @return List of song search suggestions
   */
  suspend fun suggestions(
    query: String,
    bookId: BookIdDto? = null,
    limit: Int? = null
  ): List<SongSearchSuggestionDto>
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

  override suspend fun search(
    query: String,
    type: SearchTypeDto?,
    bookId: BookIdDto?,
    limit: Int?,
    offset: Int?,
    highlight: Boolean?
  ): SongSearchResponseDto =
    execute<SongSearchResponseDto> {
      client.get(UserSongs.Search(
        query = query,
        type = type,
        bookId = bookId,
        limit = limit,
        offset = offset,
        highlight = highlight
      ))
    }.getOrThrow()

  override suspend fun suggestions(
    query: String,
    bookId: BookIdDto?,
    limit: Int?
  ): List<SongSearchSuggestionDto> =
    execute<List<SongSearchSuggestionDto>> {
      client.get(UserSongs.SearchSuggestions(
        query = query,
        bookId = bookId,
        limit = limit
      ))
    }.getOrThrow()
}

