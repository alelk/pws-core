package io.github.alelk.pws.data.repository.room.di

import io.github.alelk.pws.data.repository.room.book.BookObserveRepositoryImpl
import io.github.alelk.pws.data.repository.room.book.BookWriteRepositoryImpl
import io.github.alelk.pws.data.repository.room.bookstatistic.BookStatisticRepositoryImpl
import io.github.alelk.pws.data.repository.room.favorite.FavoriteRepositoryImpl
import io.github.alelk.pws.data.repository.room.history.HistoryRepositoryImpl
import io.github.alelk.pws.data.repository.room.song.SongRepositoryImpl
import io.github.alelk.pws.data.repository.room.song.SongSearchRepositoryImpl
import io.github.alelk.pws.data.repository.room.songnumber.SongNumberRepositoryImpl
import io.github.alelk.pws.data.repository.room.songreference.SongReferenceRepositoryImpl
import io.github.alelk.pws.data.repository.room.songtag.SongTagRepositoryImpl
import io.github.alelk.pws.data.repository.room.tag.TagRepositoryImpl
import io.github.alelk.pws.database.PwsDatabase
import io.github.alelk.pws.domain.book.repository.BookObserveRepository
import io.github.alelk.pws.domain.book.repository.BookReadRepository
import io.github.alelk.pws.domain.book.repository.BookWriteRepository
import io.github.alelk.pws.domain.bookstatistic.repository.BookStatisticRepository
import io.github.alelk.pws.data.repository.room.transaction.RoomTransactionRunner
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.favorite.repository.FavoriteObserveRepository
import io.github.alelk.pws.domain.favorite.repository.FavoriteReadRepository
import io.github.alelk.pws.domain.favorite.repository.FavoriteWriteRepository
import io.github.alelk.pws.domain.history.repository.HistoryObserveRepository
import io.github.alelk.pws.domain.history.repository.HistoryReadRepository
import io.github.alelk.pws.domain.history.repository.HistoryWriteRepository
import io.github.alelk.pws.domain.song.repository.SongObserveRepository
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.repository.SongWriteRepository
import io.github.alelk.pws.domain.song.repository.SongSearchRepository
import io.github.alelk.pws.domain.songnumber.repository.SongNumberReadRepository
import io.github.alelk.pws.domain.songnumber.repository.SongNumberWriteRepository
import io.github.alelk.pws.domain.songreference.repository.SongReferenceReadRepository
import io.github.alelk.pws.domain.songreference.repository.SongReferenceWriteRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagObserveRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagWriteRepository
import io.github.alelk.pws.domain.tag.repository.TagObserveRepository
import io.github.alelk.pws.domain.tag.repository.TagReadRepository
import io.github.alelk.pws.domain.tag.repository.TagWriteRepository
import org.koin.dsl.binds
import org.koin.dsl.module

/**
 * Koin module for all Room repository implementations.
 * Requires [PwsDatabase] and [TransactionRunner] to be provided externally.
 */
val repoRoomModule = module {

  // TransactionRunner
  single<TransactionRunner> { RoomTransactionRunner(get()) }

  // Book
  single {
    BookObserveRepositoryImpl(get<PwsDatabase>().bookDao())
  } binds arrayOf(BookObserveRepository::class, BookReadRepository::class)

  // Book Write
  single {
    BookWriteRepositoryImpl(get<PwsDatabase>().bookDao())
  } binds arrayOf(BookWriteRepository::class)

  // Book Statistic
  single {
    BookStatisticRepositoryImpl(get<PwsDatabase>().bookStatisticDao())
  } binds arrayOf(BookStatisticRepository::class)

  // Song
  single {
    SongRepositoryImpl(get<PwsDatabase>().songDao(), get<PwsDatabase>().songNumberDao())
  } binds arrayOf(SongReadRepository::class, SongObserveRepository::class, SongWriteRepository::class)

  // Song Search
  single {
    SongSearchRepositoryImpl(get<PwsDatabase>().songDao())
  } binds arrayOf(SongSearchRepository::class)

  // Favorite
  single {
    FavoriteRepositoryImpl(get<PwsDatabase>().favoriteDao())
  } binds arrayOf(FavoriteReadRepository::class, FavoriteObserveRepository::class, FavoriteWriteRepository::class)

  // History
  single {
    HistoryRepositoryImpl(get<PwsDatabase>().historyDao())
  } binds arrayOf(HistoryReadRepository::class, HistoryObserveRepository::class, HistoryWriteRepository::class)

  // Tag
  single {
    TagRepositoryImpl(get<PwsDatabase>().tagDao(), get<PwsDatabase>().songTagDao())
  } binds arrayOf(
    TagReadRepository::class,
    TagObserveRepository::class,
    TagWriteRepository::class
  )

  // SongTag
  single {
    SongTagRepositoryImpl(
      get<PwsDatabase>().songTagDao(),
      get<PwsDatabase>().tagDao(),
      get<PwsDatabase>().songDao(),
      get<PwsDatabase>().songNumberDao()
    )
  } binds arrayOf(
    SongTagReadRepository::class,
    SongTagObserveRepository::class,
    SongTagWriteRepository::class
  )

  // SongReference
  single {
    SongReferenceRepositoryImpl(get<PwsDatabase>().songReferenceDao())
  } binds arrayOf(SongReferenceReadRepository::class, SongReferenceWriteRepository::class)

  // SongNumber
  single {
    SongNumberRepositoryImpl(get<PwsDatabase>().songNumberDao())
  } binds arrayOf(SongNumberReadRepository::class, SongNumberWriteRepository::class)
}
