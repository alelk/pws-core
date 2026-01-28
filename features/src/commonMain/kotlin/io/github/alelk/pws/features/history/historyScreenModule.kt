package io.github.alelk.pws.features.history

import cafe.adriel.voyager.core.registry.screenModule
import io.github.alelk.pws.core.navigation.SharedScreens

val historyScreenModule = screenModule {
  register<SharedScreens.History> { HistoryScreen() }
}
