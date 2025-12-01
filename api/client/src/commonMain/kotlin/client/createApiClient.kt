package io.github.alelk.pws.api.client.client

import io.github.alelk.pws.api.client.api.BookApi
import io.github.alelk.pws.api.client.api.BookApiImpl
import io.github.alelk.pws.api.client.api.SongApi
import io.github.alelk.pws.api.client.api.SongApiImpl
import io.github.alelk.pws.api.client.config.NetworkConfig
import io.github.alelk.pws.api.client.http.createHttpClient
import io.github.alelk.pws.api.client.repository.RemoteBookReadRepository
import io.github.alelk.pws.api.client.repository.RemoteBookWriteRepository
import io.github.alelk.pws.api.client.repository.RemoteSongReadRepository
import io.github.alelk.pws.api.client.repository.RemoteSongWriteRepository
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
  httpClient: HttpClient? = null,
  engineBuilder: (io.ktor.client.HttpClientConfig<*>.() -> Unit)? = null
): ApiClientContainer {
  val owns = httpClient == null
  val client = httpClient ?: createHttpClient(network, engineBuilder)

  val songApi: SongApi = SongApiImpl(client)
  val bookApi: BookApi = BookApiImpl(client)

  val songReadRepo: SongReadRepository = RemoteSongReadRepository(songApi)
  val songWriteRepo: SongWriteRepository = RemoteSongWriteRepository(songApi)
  val bookReadRepo: BookReadRepository = RemoteBookReadRepository(bookApi)
  val bookWriteRepo: BookWriteRepository = RemoteBookWriteRepository(bookApi)

  return ApiClientContainer(
    httpClient = client,
    songApi = songApi,
    bookApi = bookApi,
    songReadRepository = songReadRepo,
    songWriteRepository = songWriteRepo,
    bookReadRepository = bookReadRepo,
    bookWriteRepository = bookWriteRepo,
    ownsClient = owns
  )
}
