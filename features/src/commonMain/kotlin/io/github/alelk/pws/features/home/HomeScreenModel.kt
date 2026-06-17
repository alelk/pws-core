package io.github.alelk.pws.features.home

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.book.query.BookQuery
import io.github.alelk.pws.domain.book.usecase.ObserveBooksUseCase
import io.github.alelk.pws.domain.history.usecase.ObserveHistoryUseCase
import io.github.alelk.pws.domain.song.model.SongSearchSuggestion
import io.github.alelk.pws.domain.song.usecase.SearchSongSuggestionsUseCase
import io.github.alelk.pws.features.search.BookReferenceUi
import io.github.alelk.pws.features.search.SearchSuggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ScreenModel for the Home screen. Exposes a single composite [HomeUiState]; the
 * search query and suggestions live inside [HomeUiState.Content] so the UI sees
 * atomic updates instead of three independent flows racing.
 */
@OptIn(FlowPreview::class)
class HomeScreenModel(
  observeBooksUseCase: ObserveBooksUseCase,
  private val searchSuggestionsUseCase: SearchSongSuggestionsUseCase,
  observeHistoryUseCase: ObserveHistoryUseCase,
  /** Injected scope for testing with virtual time; null = use [screenModelScope]. */
  private val coroutineScope: CoroutineScope? = null,
) : StateScreenModel<HomeUiState>(HomeUiState.Loading) {

  private val scope: CoroutineScope get() = coroutineScope ?: screenModelScope

  // Internal "user inputs" — wired into the composite state via combine() below.
  private val searchQuery = MutableStateFlow("")
  private val searchSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
  private val isSearching = MutableStateFlow(false)

  private val numberQuery = MutableStateFlow("")
  private val numberSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
  private val isNumberSearching = MutableStateFlow(false)

  private var searchJob: Job? = null
  private var numberSearchJob: Job? = null

  init {
    // Observe books + history and fold all transient fields into one Content state.
    combine(
      observeBooksUseCase(query = BookQuery(enabled = true)),
      observeHistoryUseCase(limit = 10),
      searchQuery,
      searchSuggestions,
      isSearching,
      numberQuery,
      numberSuggestions,
      isNumberSearching,
    ) { values ->
      @Suppress("UNCHECKED_CAST")
      HomeUiState.Content(
        books = values[0] as List<io.github.alelk.pws.domain.book.model.BookSummary>,
        recentSongs = values[1] as List<io.github.alelk.pws.domain.history.model.HistoryEntry>,
        searchQuery = values[2] as String,
        searchSuggestions = values[3] as List<SearchSuggestion>,
        isSearching = values[4] as Boolean,
        numberQuery = values[5] as String,
        numberSuggestions = values[6] as List<SearchSuggestion>,
        isNumberSearching = values[7] as Boolean,
      )
    }
      .onEach { mutableState.value = it }
      .catch { mutableState.value = HomeUiState.Error }
      .launchIn(scope)

    // Debounced search side-channels — write back into the StateFlows above.
    searchQuery
      .debounce(300)
      .distinctUntilChanged()
      .onEach { q ->
        if (q.isBlank()) {
          searchSuggestions.value = emptyList()
          isSearching.value = false
        } else {
          performSearch(q)
        }
      }
      .launchIn(scope)

    numberQuery
      .debounce(200)
      .distinctUntilChanged()
      .onEach { q ->
        if (q.isBlank()) {
          numberSuggestions.value = emptyList()
          isNumberSearching.value = false
        } else {
          performNumberSearch(q)
        }
      }
      .launchIn(scope)
  }

  fun onEvent(event: HomeEvent) {
    when (event) {
      is HomeEvent.SearchQueryChanged -> {
        searchQuery.value = event.query
        if (event.query.isNotBlank()) isSearching.value = true
        else {
          searchSuggestions.value = emptyList()
          isSearching.value = false
        }
      }

      HomeEvent.SearchCleared -> {
        searchQuery.value = ""
        searchSuggestions.value = emptyList()
        isSearching.value = false
      }

      is HomeEvent.NumberQueryChanged -> {
        val normalized = event.query.filter { it.isDigit() }.take(4)
        numberQuery.value = normalized
        if (normalized.isBlank()) {
          numberSearchJob?.cancel()
          numberSuggestions.value = emptyList()
          isNumberSearching.value = false
        } else {
          isNumberSearching.value = true
        }
      }

      HomeEvent.NumberCleared -> {
        numberQuery.value = ""
        numberSuggestions.value = emptyList()
        isNumberSearching.value = false
        numberSearchJob?.cancel()
      }
    }
  }

  private fun performSearch(query: String) {
    searchJob?.cancel()
    searchJob = scope.launch {
      try {
        isSearching.value = true
        val results = searchSuggestionsUseCase(query)
        searchSuggestions.value = results.fold(
          ifLeft = { emptyList() },
          ifRight = { list -> list.map { it.toUi() } },
        )
      } catch (_: Exception) {
        searchSuggestions.value = emptyList()
      } finally {
        isSearching.value = false
      }
    }
  }

  private fun performNumberSearch(query: String) {
    numberSearchJob?.cancel()
    numberSearchJob = scope.launch {
      try {
        isNumberSearching.value = true
        val results = searchSuggestionsUseCase(query, limit = 15)
        numberSuggestions.value = results.fold(
          ifLeft = { emptyList() },
          ifRight = { list -> list.map { it.toUi() } },
        )
      } catch (_: Exception) {
        numberSuggestions.value = emptyList()
      } finally {
        isNumberSearching.value = false
      }
    }
  }

  private fun SongSearchSuggestion.toUi() = SearchSuggestion(
    songId = id,
    songName = name.value,
    bookReferences = bookReferences.map { ref ->
      BookReferenceUi(
        bookId = ref.bookId,
        displayShortName = ref.displayShortName.value,
        songNumber = ref.songNumber,
      )
    },
    snippet = snippet,
  )
}
