package io.github.alelk.pws.api.client.client

import io.github.alelk.pws.api.client.api.AdminBookApi
import io.github.alelk.pws.api.client.api.AdminSongApi
import io.github.alelk.pws.api.client.api.AdminSongReferenceApi
import io.github.alelk.pws.api.client.api.AdminTagApi
import io.github.alelk.pws.api.client.api.AuthApi
import io.github.alelk.pws.api.client.api.BookApi
import io.github.alelk.pws.api.client.api.SongApi
import io.github.alelk.pws.api.client.api.UserBookApi
import io.github.alelk.pws.api.client.api.UserFavoriteApi
import io.github.alelk.pws.api.client.api.UserHistoryApi
import io.github.alelk.pws.api.client.api.UserSongApi
import io.github.alelk.pws.domain.book.repository.BookReadRepository
import io.github.alelk.pws.domain.book.repository.BookWriteRepository
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.repository.SongWriteRepository
import io.ktor.client.HttpClient

/**
 * Container holding prewired API and repository instances produced by the factory.
 * If [ownsClient] is true, [close] will close the underlying [io.ktor.client.HttpClient].
 */
data class ApiClientContainer(
  val httpClient: HttpClient,
  // Public read-only APIs (SongApi includes search methods)
  val songApi: SongApi,
  val bookApi: BookApi,
  // Auth API
  val authApi: AuthApi,
  // Admin APIs (require admin role)
  val adminBookApi: AdminBookApi,
  val adminSongApi: AdminSongApi,
  val adminSongReferenceApi: AdminSongReferenceApi,
  val adminTagApi: AdminTagApi,
  // User APIs (require authentication)
  val userBookApi: UserBookApi,
  val userFavoriteApi: UserFavoriteApi,
  val userHistoryApi: UserHistoryApi,
  val userSongApi: UserSongApi,
  // Repositories (for domain layer integration)
  val songReadRepository: SongReadRepository,
  val songWriteRepository: SongWriteRepository,
  val bookReadRepository: BookReadRepository,
  val bookWriteRepository: BookWriteRepository,
  val ownsClient: Boolean
) : AutoCloseable {
  override fun close() {
    if (ownsClient) {
      try { httpClient.close() } catch (_: Throwable) { /* ignore */ }
    }
  }
}