package io.github.alelk.pws.core.navigation

import cafe.adriel.voyager.core.registry.ScreenProvider
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongNumberId

sealed interface SharedScreens : ScreenProvider {

  data object Settings : SharedScreens

  data object Books : SharedScreens

  data class BookSongs(val bookId: BookId) : SharedScreens

  data class Song(val songNumberId: SongNumberId) : SharedScreens
}