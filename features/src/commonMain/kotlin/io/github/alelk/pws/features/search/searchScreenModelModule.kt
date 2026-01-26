package io.github.alelk.pws.features.search

import org.koin.dsl.module

val searchScreenModelModule = module {
  factory { SearchScreenModel(get()) }
}
