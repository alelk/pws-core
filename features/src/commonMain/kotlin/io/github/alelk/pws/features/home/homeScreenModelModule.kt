package io.github.alelk.pws.features.home

import org.koin.dsl.module

val homeScreenModelModule = module {
  factory { HomeScreenModel(get(), get(), get()) }
}
