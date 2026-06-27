package io.github.alelk.pws.core.navigation

import cafe.adriel.voyager.core.registry.ScreenProvider
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId

sealed interface SharedScreens : ScreenProvider {

  // Main entry point
  data object Home : SharedScreens

  // Main tabs
  data object Books : SharedScreens
  data object Tags : SharedScreens
  data object Search : SharedScreens
  data object Favorites : SharedScreens
  data object History : SharedScreens
  data object Settings : SharedScreens

  // Book related
  data class BookSongs(val bookIdString: String) : SharedScreens {
    val bookId: BookId get() = BookId.parse(bookIdString)
  }

  // Song related
  data class Song(val songNumberIdString: String) : SharedScreens {
    val songNumberId: SongNumberId get() = SongNumberId.parse(songNumberIdString)
  }
  data class SongById(val songIdLong: Long) : SharedScreens {
    val songId: SongId get() = SongId(songIdLong)
  }
  data class SongEdit(val songIdLong: Long) : SharedScreens {
    val songId: SongId get() = SongId(songIdLong)
  }

  // Tag related
  data class TagSongs(val tagIdString: String) : SharedScreens {
    val tagId: TagId get() = TagId.parse(tagIdString)
  }

  // Book library
  data object BookLibrary : SharedScreens

  // Search
  data class SearchResults(val query: String) : SharedScreens

  companion object {
    fun bookSongs(bookId: BookId) = BookSongs(bookId.identifier)
    fun song(songNumberId: SongNumberId) = Song(songNumberId.identifier)
    fun songById(songId: SongId) = SongById(songId.value)
    fun songEdit(songId: SongId) = SongEdit(songId.value)
    fun tagSongs(tagId: TagId) = TagSongs(tagId.identifier)
  }
}

// Top-level factory helpers to construct SharedScreens from domain ID types
fun sharedScreenBookSongs(bookId: BookId) = SharedScreens.BookSongs(bookId.identifier)
fun sharedScreenSong(songNumberId: SongNumberId) = SharedScreens.Song(songNumberId.identifier)
fun sharedScreenSongById(songId: SongId) = SharedScreens.SongById(songId.value)
fun sharedScreenSongEdit(songId: SongId) = SharedScreens.SongEdit(songId.value)
fun sharedScreenTagSongs(tagId: TagId) = SharedScreens.TagSongs(tagId.identifier)
