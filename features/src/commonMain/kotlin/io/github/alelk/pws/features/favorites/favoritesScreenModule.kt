package io.github.alelk.pws.features.favorites

import cafe.adriel.voyager.core.registry.screenModule
import io.github.alelk.pws.core.navigation.SharedScreens

val favoritesScreenModule = screenModule {
  register<SharedScreens.Favorites> { FavoritesScreen() }
}
