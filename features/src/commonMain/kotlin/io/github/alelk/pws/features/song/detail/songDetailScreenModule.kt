package io.github.alelk.pws.features.song.detail

import cafe.adriel.voyager.core.registry.screenModule
import io.github.alelk.pws.core.navigation.SharedScreens

val songDetailScreenModule = screenModule {
  register<SharedScreens.Song> { SongDetailScreen(it.songNumberId) }
}

