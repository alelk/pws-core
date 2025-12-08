package io.github.alelk.pws.features.di

import cafe.adriel.voyager.core.registry.screenModule
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.features.books.BooksScreen
import io.github.alelk.pws.features.book.songs.BookSongsScreen
import io.github.alelk.pws.features.favorites.FavoritesScreen
import io.github.alelk.pws.features.history.HistoryScreen
import io.github.alelk.pws.features.search.SearchScreen
import io.github.alelk.pws.features.song.detail.SongDetailScreen
import io.github.alelk.pws.features.song.edit.SongEditScreen
import io.github.alelk.pws.features.tags.TagsScreen
import io.github.alelk.pws.features.tags.songs.TagSongsScreen

/**
 * Voyager screen module registering all app screens.
 */
val appScreenModule = screenModule {
  // Main tabs
  register<SharedScreens.Books> { BooksScreen() }
  register<SharedScreens.Tags> { TagsScreen() }
  register<SharedScreens.Search> { SearchScreen() }
  register<SharedScreens.Favorites> { FavoritesScreen() }
  register<SharedScreens.History> { HistoryScreen() }

  // Book screens
  register<SharedScreens.BookSongs> { provider ->
    BookSongsScreen(provider.bookId)
  }

  // Song screens
  register<SharedScreens.Song> { provider ->
    SongDetailScreen(provider.songNumberId)
  }

  register<SharedScreens.SongEdit> { provider ->
    SongEditScreen(provider.songId)
  }

  // Tag screens
  register<SharedScreens.TagSongs> { provider ->
    TagSongsScreen(provider.tagId)
  }

  // TODO: Implement search results screen
  // register<SharedScreens.SearchResults> { provider ->
  //   SearchResultsScreen(provider.query)
  // }
}

