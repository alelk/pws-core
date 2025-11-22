package io.github.alelk.pws.api.client.di

import io.github.alelk.pws.api.client.api.*
import io.github.alelk.pws.api.client.config.NetworkConfig
import io.github.alelk.pws.api.client.http.createHttpClient
import io.github.alelk.pws.api.client.repository.RemoteBookObserveRepository
import io.github.alelk.pws.api.client.repository.RemoteSongObserveRepository
import io.github.alelk.pws.domain.book.repository.BookObserveRepository
import io.github.alelk.pws.domain.song.repository.SongObserveRepository
import io.ktor.http.Url
import org.koin.core.module.Module
import org.koin.dsl.module

fun apiClientModule(baseUrl: Url): Module = module {
  single { NetworkConfig(baseUrl = baseUrl) }
  single { createHttpClient(get()) }
  single<SongApi> { SongApiImpl(get()) }
  single<BookApi> { BookApiImpl(get()) }
  single<SongObserveRepository> { RemoteSongObserveRepository(get(), get()) }
  single<BookObserveRepository> { RemoteBookObserveRepository(get()) }
}

