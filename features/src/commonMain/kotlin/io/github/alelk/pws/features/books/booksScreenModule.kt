package io.github.alelk.pws.features.books

import cafe.adriel.voyager.core.registry.screenModule
import io.github.alelk.pws.core.navigation.SharedScreens

val booksScreenModule = screenModule {
  register<SharedScreens.Books> { BooksScreen() }
}