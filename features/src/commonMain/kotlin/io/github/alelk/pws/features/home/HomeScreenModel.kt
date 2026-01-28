package io.github.alelk.pws.features.home

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
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

  private var searchJob: Job? = null

  init {
    // Observe books and history
    combine(
      observeBooksUseCase(),
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

  private fun performSearch(query: String) {
    searchJob?.cancel()
    searchJob = screenModelScope.launch {
      try {
        _isSearching.value = true
        val results = searchSuggestionsUseCase(query)
        _suggestions.value = results.map { it.toUi() }
      } catch (e: Exception) {
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
