package io.github.alelk.pws.features.favorites

import org.koin.dsl.module

val favoritesScreenModelModule = module {
  factory { FavoritesScreenModel(get(), get()) }
}
