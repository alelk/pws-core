package io.github.alelk.pws.api.client.di

import io.github.alelk.pws.api.client.client.createApiClient
import io.github.alelk.pws.api.client.config.NetworkConfig
import io.github.alelk.pws.api.client.http.createHttpClient
import io.github.alelk.pws.api.client.repository.RemoteBookReadRepository
import io.github.alelk.pws.api.client.repository.RemoteBookWriteRepository
import io.github.alelk.pws.api.client.repository.RemoteSongReadRepository
import io.github.alelk.pws.api.client.repository.RemoteSongWriteRepository
import io.github.alelk.pws.api.client.api.BookApi
import io.github.alelk.pws.api.client.api.SongApi
import io.github.alelk.pws.api.client.client.ApiClientContainer
import io.github.alelk.pws.domain.book.repository.BookReadRepository
import io.github.alelk.pws.domain.book.repository.BookWriteRepository
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.repository.SongWriteRepository
import io.ktor.client.HttpClient
import io.ktor.http.Url
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for api client. Provides two options:
 * - consumers can use prewired repos/apis (registered below),
 * - or retrieve ApiClientContainer via `get()` if they prefer manual lifecycle management.
 */
fun apiClientKoinModule(baseUrl: Url): Module = module {

  single { NetworkConfig(baseUrl = baseUrl) }
  single<HttpClient> { createHttpClient(get()) }
  single<ApiClientContainer> { createApiClient(get(), get()) }

  single<SongApi> { get<ApiClientContainer>().songApi }
  single<BookApi> { get<ApiClientContainer>().bookApi }

  single<SongReadRepository> { RemoteSongReadRepository(get()) }
  single<SongWriteRepository> { RemoteSongWriteRepository(get()) }
  single<BookReadRepository> { RemoteBookReadRepository(get()) }
  single<BookWriteRepository> { RemoteBookWriteRepository(get()) }

}