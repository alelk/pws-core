package io.github.alelk.pws.features.di

import io.github.alelk.pws.domain.book.repository.BookObserveRepository
import io.github.alelk.pws.domain.book.usecase.ObserveBooksUseCase
import io.github.alelk.pws.domain.bookstatistic.repository.BookStatisticRepository
import io.github.alelk.pws.domain.bookstatistic.usecase.UpdateBookStatisticUseCase
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.cross.usecase.ObserveBookWithSongsUseCase
import io.github.alelk.pws.domain.favorite.repository.FavoriteObserveRepository
import io.github.alelk.pws.domain.favorite.repository.FavoriteWriteRepository
import io.github.alelk.pws.domain.favorite.usecase.ObserveFavoritesUseCase
import io.github.alelk.pws.domain.favorite.usecase.ObserveIsFavoriteUseCase
import io.github.alelk.pws.domain.favorite.usecase.RemoveFavoriteUseCase
import io.github.alelk.pws.domain.favorite.usecase.ToggleFavoriteUseCase
import io.github.alelk.pws.domain.history.repository.HistoryObserveRepository
import io.github.alelk.pws.domain.history.repository.HistoryWriteRepository
import io.github.alelk.pws.domain.history.usecase.ClearHistoryUseCase
import io.github.alelk.pws.domain.history.usecase.ObserveHistoryUseCase
import io.github.alelk.pws.domain.history.usecase.RecordSongViewUseCase
import io.github.alelk.pws.domain.history.usecase.RemoveHistoryEntryUseCase
import io.github.alelk.pws.domain.song.repository.SongObserveRepository
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.repository.SongSearchRepository
import io.github.alelk.pws.domain.song.repository.SongWriteRepository
import io.github.alelk.pws.domain.song.usecase.GetSongDetailUseCase
import io.github.alelk.pws.domain.song.usecase.ObserveSongUseCase
import io.github.alelk.pws.domain.song.usecase.SearchSongSuggestionsUseCase
import io.github.alelk.pws.domain.song.usecase.UpdateSongUseCase
import io.github.alelk.pws.domain.songtag.repository.SongTagObserveRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagWriteRepository
import io.github.alelk.pws.domain.songtag.usecase.GetSongTagIdsUseCase
import io.github.alelk.pws.domain.songtag.usecase.ObserveSongsByTagUseCase
import io.github.alelk.pws.domain.songtag.usecase.ObserveTagsForSongUseCase
import io.github.alelk.pws.domain.songtag.usecase.ReplaceAllSongTagsUseCase
import io.github.alelk.pws.domain.tag.repository.TagObserveRepository
import io.github.alelk.pws.domain.tag.repository.TagReadRepository
import io.github.alelk.pws.domain.tag.repository.TagWriteRepository
import io.github.alelk.pws.domain.tag.usecase.CreateTagUseCase
import io.github.alelk.pws.domain.tag.usecase.DeleteTagUseCase
import io.github.alelk.pws.domain.tag.usecase.GetTagDetailUseCase
import io.github.alelk.pws.domain.tag.usecase.ObserveTagsUseCase
import io.github.alelk.pws.domain.tag.usecase.UpdateTagUseCase
import io.github.alelk.pws.domain.donationprompt.config.DonationConfig
import io.github.alelk.pws.domain.donationprompt.repository.DonationPromptStateReadRepository
import io.github.alelk.pws.domain.donationprompt.repository.DonationPromptStateWriteRepository
import io.github.alelk.pws.domain.donationprompt.usecase.IsLoyalUserUseCase
import io.github.alelk.pws.domain.donationprompt.usecase.ShouldShowDonationPromptUseCase
import io.github.alelk.pws.domain.donationprompt.usecase.RecordDonationPromptDismissedUseCase
import io.github.alelk.pws.domain.donationprompt.usecase.RecordDonationClickedUseCase
import io.github.alelk.pws.domain.booklibrary.repository.BookCatalogRepository
import io.github.alelk.pws.domain.booklibrary.repository.InstalledBookRepository
import io.github.alelk.pws.domain.booklibrary.usecase.GetBookCatalogUseCase
import io.github.alelk.pws.domain.history.repository.HistoryReadRepository
import org.koin.dsl.module

/**
 * Koin module providing all domain use cases.
 * Requires repository bindings from repoRoomModule and a TransactionRunner.
 */
val useCasesModule = module {

  // Book
  factory { ObserveBooksUseCase(get<BookObserveRepository>()) }
  factory { UpdateBookStatisticUseCase(get<BookStatisticRepository>(), get<TransactionRunner>()) }
  factory { ObserveBookWithSongsUseCase(get<BookObserveRepository>(), get<SongObserveRepository>()) }

  // Song
  factory { ObserveSongUseCase(get<SongObserveRepository>()) }
  factory { GetSongDetailUseCase(get<SongReadRepository>(), get<TransactionRunner>()) }
  factory { UpdateSongUseCase(get<SongReadRepository>(), get<SongWriteRepository>(), get<TransactionRunner>()) }
  factory { SearchSongSuggestionsUseCase(get<SongSearchRepository>(), get<TransactionRunner>()) }

  // Favorite
  factory { ObserveFavoritesUseCase(get<FavoriteObserveRepository>()) }
  factory { ObserveIsFavoriteUseCase(get<FavoriteObserveRepository>()) }
  factory { ToggleFavoriteUseCase(get<FavoriteWriteRepository>(), get<TransactionRunner>()) }
  factory { RemoveFavoriteUseCase(get<FavoriteWriteRepository>(), get<TransactionRunner>()) }

  // History
  factory { ObserveHistoryUseCase(get<HistoryObserveRepository>()) }
  factory { RecordSongViewUseCase(get<HistoryWriteRepository>(), get<TransactionRunner>()) }
  factory { RemoveHistoryEntryUseCase(get<HistoryWriteRepository>(), get<TransactionRunner>()) }
  factory { ClearHistoryUseCase(get<HistoryWriteRepository>(), get<TransactionRunner>()) }

  // Tag (generic over TagId)
  factory<ObserveTagsUseCase<TagId>> { ObserveTagsUseCase(get<TagObserveRepository<TagId>>()) }
  factory<GetTagDetailUseCase<TagId>> { GetTagDetailUseCase(get<TagReadRepository<TagId>>(), get<TransactionRunner>()) }
  factory<CreateTagUseCase<TagId>> { CreateTagUseCase(get<TagReadRepository<TagId>>(), get<TagWriteRepository<TagId>>(), get<TransactionRunner>()) }
  factory<UpdateTagUseCase<TagId>> { UpdateTagUseCase(get<TagReadRepository<TagId>>(), get<TagWriteRepository<TagId>>(), get<TransactionRunner>()) }
  factory<DeleteTagUseCase<TagId>> { DeleteTagUseCase(get<TagReadRepository<TagId>>(), get<TagWriteRepository<TagId>>(), get<TransactionRunner>()) }

  // SongTag (generic over TagId)
  factory<ObserveTagsForSongUseCase<TagId>> { ObserveTagsForSongUseCase(get<SongTagObserveRepository<TagId>>()) }
  factory<ObserveSongsByTagUseCase<TagId>> { ObserveSongsByTagUseCase(get<SongTagObserveRepository<TagId>>()) }
  factory<GetSongTagIdsUseCase<TagId>> { GetSongTagIdsUseCase(get<SongTagReadRepository<TagId>>(), get<TransactionRunner>()) }
  factory<ReplaceAllSongTagsUseCase<TagId>> {
    ReplaceAllSongTagsUseCase(get<SongTagReadRepository<TagId>>(), get<SongTagWriteRepository<TagId>>(), get<TransactionRunner>())
  }

  // Book library
  factory { GetBookCatalogUseCase(get<BookCatalogRepository>(), get<InstalledBookRepository>()) }

  // Donation prompt
  factory { IsLoyalUserUseCase(get<DonationConfig>(), get<HistoryReadRepository>()) }
  factory { ShouldShowDonationPromptUseCase(get<DonationConfig>(), get<HistoryReadRepository>(), get<DonationPromptStateReadRepository>(), get<DonationSessionGuard>()) }
  factory { RecordDonationPromptDismissedUseCase(get<DonationConfig>(), get<HistoryReadRepository>(), get<DonationPromptStateReadRepository>(), get<DonationPromptStateWriteRepository>()) }
  factory { RecordDonationClickedUseCase(get<DonationPromptStateReadRepository>(), get<DonationPromptStateWriteRepository>()) }
}

