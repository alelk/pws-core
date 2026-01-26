package io.github.alelk.pws.features.home

import cafe.adriel.voyager.core.registry.screenModule
import io.github.alelk.pws.core.navigation.SharedScreens

val homeScreenModule = screenModule {
  register<SharedScreens.Home> { HomeScreen() }
}
