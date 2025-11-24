package io.github.alelk.pws.api.client.client

import io.github.alelk.pws.api.client.api.BookApi
import io.github.alelk.pws.api.client.api.SongApi
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
    val songApi: SongApi,
    val bookApi: BookApi,
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