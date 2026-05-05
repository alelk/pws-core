package io.github.alelk.pws.features.di

import io.github.alelk.pws.features.books.BooksScreenModel
import io.github.alelk.pws.features.book.songs.BookSongsScreenModel
import io.github.alelk.pws.features.favorites.FavoritesScreenModel
import io.github.alelk.pws.features.history.HistoryScreenModel
import io.github.alelk.pws.features.home.HomeScreenModel
import io.github.alelk.pws.features.search.SearchScreenModel
import io.github.alelk.pws.features.settings.SettingsScreenModel
import io.github.alelk.pws.features.song.detail.SongDetailScreenModel
import io.github.alelk.pws.features.song.detail.SongDetailBySongIdScreenModel
import io.github.alelk.pws.features.song.edit.SongEditScreenModel
import io.github.alelk.pws.features.tags.TagsScreenModel
import io.github.alelk.pws.features.tags.songs.TagSongsScreenModel
import io.github.alelk.pws.domain.donationprompt.config.DonationConfig
import io.github.alelk.pws.domain.donationprompt.usecase.IsLoyalUserUseCase
import io.github.alelk.pws.domain.donationprompt.usecase.RecordDonationClickedUseCase
import io.github.alelk.pws.domain.donationprompt.usecase.RecordDonationPromptDismissedUseCase
import io.github.alelk.pws.domain.donationprompt.usecase.ShouldShowDonationPromptUseCase
import io.github.alelk.pws.domain.book.usecase.ObserveBooksUseCase
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.favorite.usecase.ObserveIsFavoriteUseCase
import io.github.alelk.pws.domain.favorite.usecase.ToggleFavoriteUseCase
import io.github.alelk.pws.domain.history.usecase.RecordSongViewUseCase
import io.github.alelk.pws.domain.song.repository.SongObserveRepository
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.usecase.ObserveSongUseCase
import io.github.alelk.pws.domain.songnumber.repository.SongNumberReadRepository
import io.github.alelk.pws.domain.songreference.repository.SongReferenceReadRepository
import io.github.alelk.pws.domain.songreference.usecase.GetSongReferencesWithDetailsUseCase
import io.github.alelk.pws.domain.songtag.usecase.ObserveSongsByTagUseCase
import io.github.alelk.pws.domain.songtag.usecase.ObserveTagsForSongUseCase
import io.github.alelk.pws.domain.songtag.usecase.GetSongTagIdsUseCase
import io.github.alelk.pws.domain.songtag.usecase.ReplaceAllSongTagsUseCase
import io.github.alelk.pws.domain.tag.usecase.CreateTagUseCase
import io.github.alelk.pws.domain.tag.usecase.DeleteTagUseCase
import io.github.alelk.pws.domain.tag.usecase.GetTagDetailUseCase
import io.github.alelk.pws.domain.tag.usecase.ObserveTagsUseCase
import io.github.alelk.pws.domain.tag.usecase.UpdateTagUseCase
import org.koin.dsl.module

/**
 * Koin module for all features screen models.
 */
val featuresModule = module {
  // DonationSessionGuard — process-lifetime singleton for per-session dedup
  single { DonationSessionGuard() }

  // Home
  factory { HomeScreenModel(get(), get(), get()) }

  // Books
  factory { BooksScreenModel(get()) }

  // Book Songs
  factory { (bookId: BookId) -> BookSongsScreenModel(bookId, get()) }

  // Song references with details
  factory { GetSongReferencesWithDetailsUseCase(get(), get(), get()) }

  // Song Detail
  factory { (songNumberId: SongNumberId) ->
    SongDetailScreenModel(
      songNumberId = songNumberId,
      observeSong = get<ObserveSongUseCase>(),
      observeBooks = get<ObserveBooksUseCase>(),
      songObserveRepository = get<SongObserveRepository>(),
      recordSongView = get<RecordSongViewUseCase>(),
      observeIsFavorite = get<ObserveIsFavoriteUseCase>(),
      toggleFavorite = get<ToggleFavoriteUseCase>(),
      getSongReferences = get<GetSongReferencesWithDetailsUseCase>(),
      songNumberReadRepository = get<SongNumberReadRepository>(),
      observeTagsForSong = get<ObserveTagsForSongUseCase<TagId>>(),
      observeAllTags = get<ObserveTagsUseCase<TagId>>(),
      replaceAllSongTags = get<ReplaceAllSongTagsUseCase<TagId>>(),
      shouldShowDonationPrompt = get<ShouldShowDonationPromptUseCase>(),
      recordDonationDismissed = get<RecordDonationPromptDismissedUseCase>(),
      recordDonationClicked = get<RecordDonationClickedUseCase>(),
      donationConfig = get<DonationConfig>(),
      donationSessionGuard = get<DonationSessionGuard>(),
    )
  }

  // Song Detail by SongId (for search results navigation)
  factory { (songId: SongId) ->
    SongDetailBySongIdScreenModel(
      songId = songId,
      observeSong = get<ObserveSongUseCase>(),
      observeBooks = get<ObserveBooksUseCase>(),
      songObserveRepository = get<SongObserveRepository>(),
      recordSongView = get<RecordSongViewUseCase>(),
      observeIsFavorite = get<ObserveIsFavoriteUseCase>(),
      toggleFavorite = get<ToggleFavoriteUseCase>(),
      getSongReferences = get<GetSongReferencesWithDetailsUseCase>(),
      songNumberReadRepository = get<SongNumberReadRepository>(),
      observeTagsForSong = get<ObserveTagsForSongUseCase<TagId>>(),
      observeAllTags = get<ObserveTagsUseCase<TagId>>(),
      replaceAllSongTags = get<ReplaceAllSongTagsUseCase<TagId>>(),
      shouldShowDonationPrompt = get<ShouldShowDonationPromptUseCase>(),
      recordDonationDismissed = get<RecordDonationPromptDismissedUseCase>(),
      recordDonationClicked = get<RecordDonationClickedUseCase>(),
      donationConfig = get<DonationConfig>(),
      donationSessionGuard = get<DonationSessionGuard>(),
    )
  }

  // Song Edit
  factory { (songId: SongId) ->
    SongEditScreenModel(
      songId = songId,
      getSongDetailUseCase = get(),
      updateSongUseCase = get(),
      observeTagsUseCase = get<ObserveTagsUseCase<TagId>>(),
      getSongTagIdsUseCase = get<GetSongTagIdsUseCase<TagId>>(),
      replaceAllSongTagsUseCase = get<ReplaceAllSongTagsUseCase<TagId>>()
    )
  }

  // Search
  factory { SearchScreenModel(get()) }

  // Favorites
  factory { FavoritesScreenModel(get(), get()) }

  // History
  factory { HistoryScreenModel(get(), get(), get()) }

  // Settings
  factory { SettingsScreenModel(get(), get(), getOrNull(), getOrNull<IsLoyalUserUseCase>(), getOrNull<DonationConfig>()) }

  // Tags
  factory { TagsScreenModel(get<ObserveTagsUseCase<TagId>>(), get<CreateTagUseCase<TagId>>(), get<UpdateTagUseCase<TagId>>(), get<DeleteTagUseCase<TagId>>()) }

  // Tag Songs
  factory { (tagId: TagId) -> TagSongsScreenModel(tagId, get<GetTagDetailUseCase<TagId>>(), get<ObserveSongsByTagUseCase<TagId>>()) }
}
