package io.github.alelk.pws.features.search

import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.NoopTransactionRunner
import io.github.alelk.pws.domain.song.model.SongBookReference
import io.github.alelk.pws.domain.song.model.SongSearchResponse
import io.github.alelk.pws.domain.song.model.SongSearchSuggestion
import io.github.alelk.pws.domain.song.query.SearchQuery
import io.github.alelk.pws.domain.song.repository.SongSearchRepository
import io.github.alelk.pws.domain.song.usecase.SearchSongSuggestionsUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SearchScreenModelTest : FunSpec({

  var dispatcher = StandardTestDispatcher()
  beforeTest { dispatcher = StandardTestDispatcher(); Dispatchers.setMain(dispatcher) }
  afterTest { Dispatchers.resetMain() }

  fun suggestion(name: String) = SongSearchSuggestion(
    id = SongId(1L),
    name = NonEmptyString(name),
    bookReferences = listOf(SongBookReference(BookId.parse("Book-1"), NonEmptyString("B1"), 1)),
    snippet = "snip",
  )

  // Fake repository capturing the queries it was asked to search.
  class FakeSearchRepository(
    val result: List<SongSearchSuggestion> = emptyList(),
    val throwError: Boolean = false,
  ) : SongSearchRepository {
    val queries = mutableListOf<String>()
    override suspend fun searchSuggestions(query: String, userId: UserId?, bookId: BookId?, limit: Int): List<SongSearchSuggestion> {
      queries += query
      if (throwError) throw RuntimeException("boom")
      return result
    }
    override suspend fun search(searchQuery: SearchQuery, userId: UserId?, bookId: BookId?): SongSearchResponse =
      SongSearchResponse(results = emptyList(), totalCount = 0, hasMore = false)
  }

  fun model(repo: SongSearchRepository) =
    SearchScreenModel(SearchSongSuggestionsUseCase(repo, NoopTransactionRunner()))

  test("blank query yields Idle without searching") {
    runTest(dispatcher) {
      val repo = FakeSearchRepository()
      val sm = model(repo)
      sm.onEvent(SearchEvent.QueryChanged("   "))
      advanceUntilIdle()
      sm.state.value shouldBe SearchUiState.Idle
      repo.queries shouldBe emptyList()
    }
  }

  test("non-blank query shows Loading immediately then Suggestions after debounce") {
    runTest(dispatcher) {
      val repo = FakeSearchRepository(result = listOf(suggestion("Amazing Grace")))
      val sm = model(repo)

      sm.onEvent(SearchEvent.QueryChanged("grace"))
      sm.state.value shouldBe SearchUiState.Loading  // synchronous, before debounce

      advanceUntilIdle()
      val state = sm.state.value.shouldBeInstanceOf<SearchUiState.Suggestions>()
      state.query shouldBe "grace"
      state.items.single().songName shouldBe "Amazing Grace"
    }
  }

  test("debounce coalesces rapid keystrokes into a single search for the final query") {
    runTest(dispatcher) {
      val repo = FakeSearchRepository(result = listOf(suggestion("Song")))
      val sm = model(repo)

      sm.onEvent(SearchEvent.QueryChanged("g"))
      sm.onEvent(SearchEvent.QueryChanged("gr"))
      sm.onEvent(SearchEvent.QueryChanged("gra"))
      advanceUntilIdle()

      repo.queries shouldBe listOf("gra")
    }
  }

  test("use-case failure produces an Error state") {
    runTest(dispatcher) {
      val sm = model(FakeSearchRepository(throwError = true))
      sm.onEvent(SearchEvent.QueryChanged("grace"))
      advanceUntilIdle()
      sm.state.value.shouldBeInstanceOf<SearchUiState.Error>()
    }
  }

  test("ClearQuery resets state to Idle and clears the query text") {
    runTest(dispatcher) {
      val sm = model(FakeSearchRepository(result = listOf(suggestion("Song"))))
      sm.onEvent(SearchEvent.QueryChanged("grace"))
      advanceUntilIdle()

      sm.onEvent(SearchEvent.ClearQuery)
      advanceUntilIdle()
      sm.state.value shouldBe SearchUiState.Idle
      sm.query.value shouldBe ""
    }
  }
})
