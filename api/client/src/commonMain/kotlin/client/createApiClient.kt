package io.github.alelk.pws.api.client.client

import io.github.alelk.pws.api.client.api.AdminBookApi
import io.github.alelk.pws.api.client.api.AdminBookApiImpl
import io.github.alelk.pws.api.client.api.AdminSongApi
import io.github.alelk.pws.api.client.api.AdminSongApiImpl
import io.github.alelk.pws.api.client.api.AdminSongReferenceApi
import io.github.alelk.pws.api.client.api.AdminSongReferenceApiImpl
import io.github.alelk.pws.api.client.api.AdminTagApi
import io.github.alelk.pws.api.client.api.AdminTagApiImpl
import io.github.alelk.pws.api.client.api.AuthApi
import io.github.alelk.pws.api.client.api.AuthApiImpl
import io.github.alelk.pws.api.client.api.BookApi
import io.github.alelk.pws.api.client.api.BookApiImpl
import io.github.alelk.pws.api.client.api.SongApi
import io.github.alelk.pws.api.client.api.SongApiImpl
import io.github.alelk.pws.api.client.api.SongSearchApi
import io.github.alelk.pws.api.client.api.SongSearchApiImpl
import io.github.alelk.pws.api.client.api.UserBookApi
import io.github.alelk.pws.api.client.api.UserBookApiImpl
import io.github.alelk.pws.api.client.api.UserFavoriteApi
import io.github.alelk.pws.api.client.api.UserFavoriteApiImpl
import io.github.alelk.pws.api.client.api.UserHistoryApi
import io.github.alelk.pws.api.client.api.UserHistoryApiImpl
import io.github.alelk.pws.api.client.config.NetworkConfig
import io.github.alelk.pws.api.client.http.createHttpClient
import io.github.alelk.pws.domain.auth.storage.TokenStorage
import io.github.alelk.pws.api.client.repository.RemoteBookReadRepository
import io.github.alelk.pws.api.client.repository.RemoteBookWriteRepository
import io.github.alelk.pws.api.client.repository.RemoteSongReadRepository
import io.github.alelk.pws.api.client.repository.RemoteSongWriteRepository
import io.github.alelk.pws.domain.auth.storage.InMemoryTokenStorage
import io.github.alelk.pws.domain.book.repository.BookReadRepository
import io.github.alelk.pws.domain.book.repository.BookWriteRepository
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.repository.SongWriteRepository
import io.ktor.client.HttpClient

/**
 * Create a ready-to-use ApiClientContainer. The factory will create a HttpClient if none provided.
 *
 * @param network network configuration (base url, timeouts, logging)
 * @param httpClient optional HttpClient instance. If provided, factory will not close it.
 * @param engineBuilder optional extension to further configure HttpClient (applied only when factory creates client)
 */
fun createApiClient(
  network: NetworkConfig,
  tokenStorage: TokenStorage = InMemoryTokenStorage(),
  httpClient: HttpClient? = null,
  engineBuilder: (io.ktor.client.HttpClientConfig<*>.() -> Unit)? = null
): ApiClientContainer {
  val owns = httpClient == null
  val client = httpClient ?: createHttpClient(network, tokenStorage, engineBuilder)

  // Public read-only APIs
  val songApi: SongApi = SongApiImpl(client)
  val bookApi: BookApi = BookApiImpl(client)
  val songSearchApi: SongSearchApi = SongSearchApiImpl(client)

  // Auth API
  val authApi: AuthApi = AuthApiImpl(client, tokenStorage)

  // Admin APIs
  val adminBookApi: AdminBookApi = AdminBookApiImpl(client)
  val adminSongApi: AdminSongApi = AdminSongApiImpl(client)
  val adminSongReferenceApi: AdminSongReferenceApi = AdminSongReferenceApiImpl(client)
  val adminTagApi: AdminTagApi = AdminTagApiImpl(client)

  // User APIs
  val userBookApi: UserBookApi = UserBookApiImpl(client)
  val userFavoriteApi: UserFavoriteApi = UserFavoriteApiImpl(client)
  val userHistoryApi: UserHistoryApi = UserHistoryApiImpl(client)

  // Repositories (using admin APIs for write operations)
  val songReadRepo: SongReadRepository = RemoteSongReadRepository(songApi)
  val songWriteRepo: SongWriteRepository = RemoteSongWriteRepository(adminSongApi)
  val bookReadRepo: BookReadRepository = RemoteBookReadRepository(bookApi)
  val bookWriteRepo: BookWriteRepository = RemoteBookWriteRepository(adminBookApi)

  return ApiClientContainer(
    httpClient = client,
    songApi = songApi,
    bookApi = bookApi,
    songSearchApi = songSearchApi,
    authApi = authApi,
    adminBookApi = adminBookApi,
    adminSongApi = adminSongApi,
    adminSongReferenceApi = adminSongReferenceApi,
    adminTagApi = adminTagApi,
    userBookApi = userBookApi,
    userFavoriteApi = userFavoriteApi,
    userHistoryApi = userHistoryApi,
    songReadRepository = songReadRepo,
    songWriteRepository = songWriteRepo,
    bookReadRepository = bookReadRepo,
    bookWriteRepository = bookWriteRepo,
    ownsClient = owns
  )
}
