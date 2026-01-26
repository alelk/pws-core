package io.github.alelk.pws.features.di

import io.github.alelk.pws.features.books.BooksScreenModel
import io.github.alelk.pws.features.book.songs.BookSongsScreenModel
import io.github.alelk.pws.features.favorites.FavoritesScreenModel
import io.github.alelk.pws.features.history.HistoryScreenModel
import io.github.alelk.pws.features.search.SearchScreenModel
import io.github.alelk.pws.features.song.detail.SongDetailScreenModel
import io.github.alelk.pws.features.song.detail.SongDetailBySongIdScreenModel
import io.github.alelk.pws.features.song.edit.SongEditScreenModel
import io.github.alelk.pws.features.tags.TagsScreenModel
import io.github.alelk.pws.features.tags.songs.TagSongsScreenModel
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId
import org.koin.dsl.module

/**
 * Koin module for all features screen models.
 */
val featuresModule = module {
  // Books
  factory { BooksScreenModel(get()) }

  // Book Songs
  factory { (bookId: BookId) -> BookSongsScreenModel(bookId, get()) }

  // Song Detail
  factory { (songNumberId: SongNumberId) -> SongDetailScreenModel(songNumberId, get()) }

  // Song Detail by SongId (for search results navigation)
  factory { (songId: SongId) -> SongDetailBySongIdScreenModel(songId, get()) }

  // Song Edit
  factory { (songId: SongId) -> SongEditScreenModel(songId, get(), get(), get()) }

  // Search
  factory { SearchScreenModel(get()) }

  // Favorites
  factory { FavoritesScreenModel(get(), get()) }

  // History
  factory { HistoryScreenModel(get(), get(), get()) }

  // Tags
  factory { TagsScreenModel(get(), get(), get(), get()) }

  // Tag Songs
  factory { (tagId: TagId) -> TagSongsScreenModel(tagId, get(), get()) }
}

