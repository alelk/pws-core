package io.github.alelk.pws.features.home

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.book.query.BookQuery
import io.github.alelk.pws.domain.book.usecase.ObserveBooksUseCase
import io.github.alelk.pws.domain.song.model.SongSearchSuggestion
import io.github.alelk.pws.domain.song.usecase.SearchSongSuggestionsUseCase
import io.github.alelk.pws.features.search.BookReferenceUi
import io.github.alelk.pws.features.search.SearchSuggestion
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

import io.github.alelk.pws.domain.history.usecase.ObserveHistoryUseCase
import kotlinx.coroutines.flow.combine

/**
 * ScreenModel for Home Screen.
 * Manages books list and inline search with suggestions.
 */
@OptIn(FlowPreview::class)
class HomeScreenModel(
  observeBooksUseCase: ObserveBooksUseCase,
  private val searchSuggestionsUseCase: SearchSongSuggestionsUseCase,
  observeHistoryUseCase: ObserveHistoryUseCase
) : StateScreenModel<HomeUiState>(HomeUiState.Loading) {

  /** Current search query - updated immediately for responsive UI */
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  /** Search suggestions */
  private val _suggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
  val suggestions: StateFlow<List<SearchSuggestion>> = _suggestions.asStateFlow()

  /** Whether suggestions are loading */
  private val _isSearching = MutableStateFlow(false)
  val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

  // Number search state
  private val _numberQuery = MutableStateFlow("")
  val numberQuery: StateFlow<String> = _numberQuery.asStateFlow()

  private val _numberSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
  val numberSuggestions: StateFlow<List<SearchSuggestion>> = _numberSuggestions.asStateFlow()

  private val _isNumberSearching = MutableStateFlow(false)
  val isNumberSearching: StateFlow<Boolean> = _isNumberSearching.asStateFlow()

  private var searchJob: Job? = null
  private var numberSearchJob: Job? = null

  init {
    // Observe books and history
    combine(
      observeBooksUseCase(query = BookQuery(enabled = true)),
      observeHistoryUseCase(limit = 10)
    ) { books, history ->
      HomeUiState.Content(
        books = books,
        recentSongs = history
      )
    }
      .onEach { content ->
        mutableState.value = content
      }
      .catch {
        mutableState.value = HomeUiState.Error
      }
      .launchIn(screenModelScope)

    // Debounced search
    _searchQuery
      .debounce(300)
      .distinctUntilChanged()
      .onEach { query ->
        if (query.isBlank()) {
          _suggestions.value = emptyList()
          _isSearching.value = false
        } else {
          performSearch(query)
        }
      }
      .launchIn(screenModelScope)

    // Debounced number search
    _numberQuery
      .debounce(200)
      .distinctUntilChanged()
      .onEach { query ->
        if (query.isBlank()) {
          _numberSuggestions.value = emptyList()
          _isNumberSearching.value = false
        } else {
          performNumberSearch(query)
        }
      }
      .launchIn(screenModelScope)
  }

  fun onSearchQueryChange(query: String) {
    _searchQuery.value = query
    if (query.isNotBlank()) {
      _isSearching.value = true
    } else {
      _suggestions.value = emptyList()
      _isSearching.value = false
    }
  }

  fun onClearSearch() {
    _searchQuery.value = ""
    _suggestions.value = emptyList()
    _isSearching.value = false
  }

  fun onNumberQueryChange(query: String) {
    val normalized = query.filter { it.isDigit() }.take(4)
    _numberQuery.value = normalized

    if (normalized.isBlank()) {
      numberSearchJob?.cancel()
      _numberSuggestions.value = emptyList()
      _isNumberSearching.value = false
    } else {
      _isNumberSearching.value = true
    }
  }

  fun onClearNumberSearch() {
    _numberQuery.value = ""
    _numberSuggestions.value = emptyList()
    _isNumberSearching.value = false
    numberSearchJob?.cancel()
  }

  private fun performNumberSearch(query: String) {
    numberSearchJob?.cancel()
    numberSearchJob = screenModelScope.launch {
      try {
        _isNumberSearching.value = true
        val results = searchSuggestionsUseCase(query, limit = 15)
        _numberSuggestions.value = results.fold(
          ifLeft = { emptyList() },
          ifRight = { list -> list.map { it.toUi() } }
        )
      } catch (_: Exception) {
        _numberSuggestions.value = emptyList()
      } finally {
        _isNumberSearching.value = false
      }
    }
  }

  private fun performSearch(query: String) {
    searchJob?.cancel()
    searchJob = screenModelScope.launch {
      try {
        _isSearching.value = true
        val results = searchSuggestionsUseCase(query)
        _suggestions.value = results.fold(
          ifLeft = { emptyList() },
          ifRight = { list -> list.map { it.toUi() } }
        )
      } catch (_: Exception) {
        _suggestions.value = emptyList()
      } finally {
        _isSearching.value = false
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
        songNumber = ref.songNumber
      )
    },
    snippet = snippet
  )
}
