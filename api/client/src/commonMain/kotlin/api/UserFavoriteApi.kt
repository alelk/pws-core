package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.favorite.FavoriteDto
import io.github.alelk.pws.api.contract.favorite.FavoriteStatusDto
import io.github.alelk.pws.api.contract.favorite.FavoriteToggleResultDto
import io.github.alelk.pws.api.contract.favorite.UserFavorites
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put

/**
 * User Favorites API client for managing user's favorite songs.
 * Requires user authentication.
 */
interface UserFavoriteApi {
  suspend fun list(limit: Int = 50, offset: Int = 0): List<FavoriteDto>
  suspend fun clearAll()
  suspend fun getStatus(bookId: BookIdDto, songNumber: Int): FavoriteStatusDto
  suspend fun add(bookId: BookIdDto, songNumber: Int): FavoriteStatusDto
  suspend fun remove(bookId: BookIdDto, songNumber: Int)
  suspend fun toggle(bookId: BookIdDto, songNumber: Int): FavoriteToggleResultDto
}

internal class UserFavoriteApiImpl(client: HttpClient) : BaseResourceApi(client), UserFavoriteApi {

  override suspend fun list(limit: Int, offset: Int): List<FavoriteDto> =
    execute<List<FavoriteDto>> { client.get(UserFavorites(limit = limit, offset = offset)) }.getOrThrow()

  override suspend fun clearAll() {
    execute<Unit> { client.delete(UserFavorites()) }.getOrThrow()
  }

  override suspend fun getStatus(bookId: BookIdDto, songNumber: Int): FavoriteStatusDto =
    execute<FavoriteStatusDto> {
      client.get(UserFavorites.BySongNumber(bookId = bookId, songNumber = songNumber))
    }.getOrThrow()

  override suspend fun add(bookId: BookIdDto, songNumber: Int): FavoriteStatusDto =
    execute<FavoriteStatusDto> {
      client.put(UserFavorites.BySongNumber(bookId = bookId, songNumber = songNumber))
    }.getOrThrow()

  override suspend fun remove(bookId: BookIdDto, songNumber: Int) {
    execute<Unit> {
      client.delete(UserFavorites.BySongNumber(bookId = bookId, songNumber = songNumber))
    }.getOrThrow()
  }

  override suspend fun toggle(bookId: BookIdDto, songNumber: Int): FavoriteToggleResultDto =
    execute<FavoriteToggleResultDto> {
      client.post(UserFavorites.BySongNumber.Toggle(parent = UserFavorites.BySongNumber(bookId = bookId, songNumber = songNumber)))
    }.getOrThrow()
}

