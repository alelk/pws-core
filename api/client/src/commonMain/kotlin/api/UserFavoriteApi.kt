package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.favorite.FavoriteDto
import io.github.alelk.pws.api.contract.favorite.FavoriteStatusDto
import io.github.alelk.pws.api.contract.favorite.FavoriteSubjectDto
import io.github.alelk.pws.api.contract.favorite.FavoriteToggleResultDto
import io.github.alelk.pws.api.contract.favorite.UserFavorites
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * User Favorites API client for managing user's favorite songs.
 * Requires user authentication.
 */
interface UserFavoriteApi {
  /** Get favorites list (newest first). */
  suspend fun list(limit: Int = 50, offset: Int = 0): List<FavoriteDto>

  /** Clear all favorites. */
  suspend fun clearAll()

  /** Add song to favorites. Returns the created favorite. */
  suspend fun add(subject: FavoriteSubjectDto): ResourceUpsertResult<FavoriteDto>

  /** Remove song from favorites. Returns the removed subject. */
  suspend fun remove(subject: FavoriteSubjectDto): ResourceDeleteResult<FavoriteSubjectDto>

  /** Get favorite status for a song. */
  suspend fun getStatus(subject: FavoriteSubjectDto): FavoriteStatusDto?

  /** Toggle favorite status for a song. */
  suspend fun toggle(subject: FavoriteSubjectDto): FavoriteToggleResultDto
}

internal class UserFavoriteApiImpl(client: HttpClient) : BaseResourceApi(client), UserFavoriteApi {

  override suspend fun list(limit: Int, offset: Int): List<FavoriteDto> =
    execute<List<FavoriteDto>> { client.get(UserFavorites(limit = limit, offset = offset)) }.getOrThrow()

  override suspend fun clearAll() {
    execute<Unit> { client.delete(UserFavorites()) }.getOrThrow()
  }

  override suspend fun add(subject: FavoriteSubjectDto): ResourceUpsertResult<FavoriteDto> =
    executeUpsert<FavoriteDto> {
      client.post(UserFavorites()) {
        contentType(ContentType.Application.Json)
        setBody(subject)
      }
    }.getOrThrow()

  override suspend fun remove(subject: FavoriteSubjectDto): ResourceDeleteResult<FavoriteSubjectDto> =
    executeDelete(subject) {
      client.delete(UserFavorites.Entry()) {
        contentType(ContentType.Application.Json)
        setBody(subject)
      }
    }.getOrThrow()

  override suspend fun getStatus(subject: FavoriteSubjectDto): FavoriteStatusDto? =
    executeGet<FavoriteStatusDto> {
      client.post(UserFavorites.Status()) {
        contentType(ContentType.Application.Json)
        setBody(subject)
      }
    }.getOrThrow()

  override suspend fun toggle(subject: FavoriteSubjectDto): FavoriteToggleResultDto =
    execute<FavoriteToggleResultDto> {
      client.post(UserFavorites.Toggle()) {
        contentType(ContentType.Application.Json)
        setBody(subject)
      }
    }.getOrThrow()
}

