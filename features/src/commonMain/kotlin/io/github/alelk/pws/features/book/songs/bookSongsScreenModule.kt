package io.github.alelk.pws.features.book.songs

import cafe.adriel.voyager.core.registry.screenModule
import io.github.alelk.pws.core.navigation.SharedScreens

val bookSongsScreenModule = screenModule {
  register<SharedScreens.BookSongs> { BookSongsScreen(it.bookId) }
}