package io.github.alelk.pws.features.history

import org.koin.dsl.module

val historyScreenModelModule = module {
  factory { HistoryScreenModel(get(), get(), get()) }
}
