package io.github.alelk.pws.core.navigation

import cafe.adriel.voyager.core.registry.ScreenProvider
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId

sealed interface SharedScreens : ScreenProvider {

  // Main tabs
  data object Books : SharedScreens
  data object Tags : SharedScreens
  data object Search : SharedScreens
  data object Favorites : SharedScreens
  data object History : SharedScreens
  data object Settings : SharedScreens

  // Book related
  data class BookSongs(val bookId: BookId) : SharedScreens

  // Song related
  data class Song(val songNumberId: SongNumberId) : SharedScreens
  data class SongEdit(val songId: SongId) : SharedScreens

  // Tag related
  data class TagSongs(val tagId: TagId) : SharedScreens

  // Search
  data class SearchResults(val query: String) : SharedScreens
}