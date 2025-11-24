package io.github.alelk.pws.api.client.di

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
import io.ktor.http.Url
import org.koin.core.module.Module
import org.koin.dsl.module

fun apiClientModule(baseUrl: Url): Module = module {
  single { NetworkConfig(baseUrl = baseUrl) }
  single { createHttpClient(get()) }
  single<SongApi> { SongApiImpl(get()) }
  single<BookApi> { BookApiImpl(get()) }
  single<SongReadRepository> { RemoteSongReadRepository(get(), get()) }
  single<SongWriteRepository> { RemoteSongWriteRepository(get()) }
  single<BookReadRepository> { RemoteBookReadRepository(get()) }
  single<BookWriteRepository> { RemoteBookWriteRepository(get()) }
}
