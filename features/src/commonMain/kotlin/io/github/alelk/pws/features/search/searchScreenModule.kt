package io.github.alelk.pws.features.search

import cafe.adriel.voyager.core.registry.screenModule
import io.github.alelk.pws.core.navigation.SharedScreens

val searchScreenModule = screenModule {
  register<SharedScreens.Search> { SearchScreen() }
}
