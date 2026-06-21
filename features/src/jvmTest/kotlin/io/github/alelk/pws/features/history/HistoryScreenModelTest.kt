package io.github.alelk.pws.features.history

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.ClearError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpsertError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.transaction.NoopTransactionRunner
import io.github.alelk.pws.domain.history.model.HistoryEntry
import io.github.alelk.pws.domain.history.model.HistorySubject
import io.github.alelk.pws.domain.history.repository.HistoryObserveRepository
import io.github.alelk.pws.domain.history.repository.HistoryWriteRepository
import io.github.alelk.pws.domain.history.usecase.ClearHistoryUseCase
import io.github.alelk.pws.domain.history.usecase.ObserveHistoryUseCase
import io.github.alelk.pws.domain.history.usecase.RemoveHistoryEntryUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class HistoryScreenModelTest : FunSpec({

  var dispatcher = StandardTestDispatcher()
  beforeTest { dispatcher = StandardTestDispatcher(); Dispatchers.setMain(dispatcher) }
  afterTest { Dispatchers.resetMain() }

  fun entry(id: Long, name: String): HistoryEntry = HistoryEntry(
    id = id,
    subject = HistorySubject.BookedSong(SongNumberId(BookId.parse("Book-1"), SongId(id))),
    songName = name,
    songNumber = id.toInt(),
    bookDisplayName = "B1",
    viewedAt = Instant.fromEpochMilliseconds(id * 1000),
  )

  var cleared = false

  val writeRepo = object : HistoryWriteRepository {
    override suspend fun recordView(subject: HistorySubject): Either<UpsertError, HistoryEntry> = Either.Left(UpsertError.UnknownError())
    override suspend fun remove(subject: HistorySubject): Either<DeleteError, HistorySubject> = Either.Right(subject)
    override suspend fun clearAll(): Either<ClearError, Int> { cleared = true; return Either.Right(1) }
  }

  fun model(history: Flow<List<HistoryEntry>>): HistoryScreenModel {
    val observeRepo = object : HistoryObserveRepository {
      override fun observeAll(limit: Int?, offset: Int): Flow<List<HistoryEntry>> = history
    }
    return HistoryScreenModel(
      ObserveHistoryUseCase(observeRepo),
      RemoveHistoryEntryUseCase(writeRepo, NoopTransactionRunner()),
      ClearHistoryUseCase(writeRepo, NoopTransactionRunner()),
    )
  }

  test("empty history yields the Empty state") {
    runTest {
      val sm = model(MutableStateFlow(emptyList()))
      advanceUntilIdle()
      sm.state.value shouldBe HistoryUiState.Empty
    }
  }

  test("non-empty history yields Content with mapped items") {
    runTest {
      val sm = model(MutableStateFlow(listOf(entry(1, "First"), entry(2, "Second"))))
      advanceUntilIdle()
      sm.state.value.shouldBeInstanceOf<HistoryUiState.Content>().items.map { it.songName } shouldBe listOf("First", "Second")
    }
  }

  test("ClearAll shows the dialog; ConfirmClearAll clears and empties; state resets") {
    runTest {
      cleared = false
      val sm = model(MutableStateFlow(listOf(entry(1, "First"))))
      advanceUntilIdle()

      sm.onEvent(HistoryEvent.ClearAll)
      sm.showClearDialog.value shouldBe true

      sm.onEvent(HistoryEvent.ConfirmClearAll)
      advanceUntilIdle()
      sm.showClearDialog.value shouldBe false
      cleared shouldBe true
      sm.state.value shouldBe HistoryUiState.Empty
    }
  }

  test("DismissClearDialog hides the dialog without clearing") {
    runTest {
      cleared = false
      val sm = model(MutableStateFlow(listOf(entry(1, "First"))))
      advanceUntilIdle()

      sm.onEvent(HistoryEvent.ClearAll)
      sm.onEvent(HistoryEvent.DismissClearDialog)
      sm.showClearDialog.value shouldBe false
      cleared shouldBe false
    }
  }

  test("an error in the history stream produces an Error state") {
    runTest {
      val sm = model(flow { throw RuntimeException("db down") })
      advanceUntilIdle()
      sm.state.value.shouldBeInstanceOf<HistoryUiState.Error>()
    }
  }
})
