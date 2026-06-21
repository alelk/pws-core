package io.github.alelk.pws.features.favorites

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.ClearError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.ToggleError
import io.github.alelk.pws.domain.core.error.UpsertError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.model.ToggleResult
import io.github.alelk.pws.domain.core.transaction.NoopTransactionRunner
import io.github.alelk.pws.domain.favorite.model.Favorite
import io.github.alelk.pws.domain.favorite.model.FavoriteSong
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.repository.FavoriteObserveRepository
import io.github.alelk.pws.domain.favorite.repository.FavoriteWriteRepository
import io.github.alelk.pws.domain.favorite.usecase.ObserveFavoritesUseCase
import io.github.alelk.pws.domain.favorite.usecase.RemoveFavoriteUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class FavoritesScreenModelTest : FunSpec({

  fun booked(songId: Long, number: Int, name: String, addedAtMs: Long): FavoriteSong =
    FavoriteSong(
      subject = FavoriteSubject.BookedSong(SongNumberId(BookId.parse("Book-1"), SongId(songId))),
      songName = name,
      songNumber = number,
      bookDisplayName = "B1",
      addedAt = Instant.fromEpochMilliseconds(addedAtMs),
    )

  val writeRepo = object : FavoriteWriteRepository {
    override suspend fun add(subject: FavoriteSubject): Either<UpsertError, Favorite> = Either.Left(UpsertError.UnknownError())
    override suspend fun remove(subject: FavoriteSubject): Either<DeleteError, FavoriteSubject> = Either.Right(subject)
    override suspend fun toggle(subject: FavoriteSubject): Either<ToggleError, ToggleResult<FavoriteSubject>> = Either.Left(ToggleError.UnknownError())
    override suspend fun clearAll(): Either<ClearError, Int> = Either.Left(ClearError.UnknownError())
  }

  fun TestScope.model(favorites: List<FavoriteSong>): FavoritesScreenModel {
    val observeRepo = object : FavoriteObserveRepository {
      override fun observeAll(limit: Int?, offset: Int): Flow<List<FavoriteSong>> = MutableStateFlow(favorites)
      override fun observeIsFavorite(subject: FavoriteSubject): Flow<Boolean> = MutableStateFlow(false)
    }
    return FavoritesScreenModel(
      ObserveFavoritesUseCase(observeRepo),
      RemoveFavoriteUseCase(writeRepo, NoopTransactionRunner()),
      coroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
    )
  }

  // a=oldest "Beta" #3, b=newest "Alpha" #1, c=middle "Gamma" #2
  val a = { booked(1, 3, "Beta", 1000) }
  val b = { booked(2, 1, "Alpha", 3000) }
  val c = { booked(3, 2, "Gamma", 2000) }

  test("empty favourites yield the Empty state") {
    runTest {
      val sm = model(emptyList())
      sm.state.value shouldBe FavoritesUiState.Empty
    }
  }

  test("default sort is ADDED_DATE descending (newest first)") {
    runTest {
      val sm = model(listOf(a(), b(), c()))
      val content = sm.state.value.shouldBeInstanceOf<FavoritesUiState.Content>()
      content.sortMode shouldBe FavoriteSortMode.ADDED_DATE
      content.ascending shouldBe false
      content.songs.map { it.songName } shouldBe listOf("Alpha", "Gamma", "Beta") // 3000,2000,1000
    }
  }

  test("changing sort mode to SONG_NAME sorts ascending by name") {
    runTest {
      val sm = model(listOf(a(), b(), c()))
      sm.onEvent(FavoritesEvent.ChangeSortMode(FavoriteSortMode.SONG_NAME))
      val content = sm.state.value.shouldBeInstanceOf<FavoritesUiState.Content>()
      content.ascending shouldBe true
      content.songs.map { it.songName } shouldBe listOf("Alpha", "Beta", "Gamma")
    }
  }

  test("changing sort mode to SONG_NUMBER sorts ascending by number") {
    runTest {
      val sm = model(listOf(a(), b(), c()))
      sm.onEvent(FavoritesEvent.ChangeSortMode(FavoriteSortMode.SONG_NUMBER))
      sm.state.value.shouldBeInstanceOf<FavoritesUiState.Content>()
        .songs.map { it.songName } shouldBe listOf("Alpha", "Gamma", "Beta") // #1,#2,#3
    }
  }

  test("toggling sort direction reverses the order") {
    runTest {
      val sm = model(listOf(a(), b(), c()))
      sm.onEvent(FavoritesEvent.ToggleSortDirection)  // ADDED_DATE now ascending
      val content = sm.state.value.shouldBeInstanceOf<FavoritesUiState.Content>()
      content.ascending shouldBe true
      content.songs.map { it.songName } shouldBe listOf("Beta", "Gamma", "Alpha") // 1000,2000,3000
    }
  }
})
