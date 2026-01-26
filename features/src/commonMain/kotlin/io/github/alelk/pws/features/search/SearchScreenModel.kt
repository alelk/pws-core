package io.github.alelk.pws.features.search

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.song.model.SongSearchSuggestion
import io.github.alelk.pws.domain.song.usecase.SearchSongSuggestionsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ScreenModel for search functionality.
 * Handles search query changes with debounce and provides suggestions.
 */
@OptIn(FlowPreview::class)
class SearchScreenModel(
  private val searchSuggestionsUseCase: SearchSongSuggestionsUseCase
) : StateScreenModel<SearchUiState>(SearchUiState.Idle) {

  /** Current query text - updated immediately for responsive UI */
  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query.asStateFlow()

  private var searchJob: Job? = null

  sealed interface Effect {
    data class NavigateToSong(val suggestion: SearchSuggestion) : Effect
    data class NavigateToResults(val query: String) : Effect
  }

  private val _effects = MutableSharedFlow<Effect>()
  val effects = _effects.asSharedFlow()

  init {
    // Debounce search queries - only for API calls, not for UI updates
    _query
      .debounce(300)
      .distinctUntilChanged()
      .onEach { query ->
        if (query.isBlank()) {
          mutableState.value = SearchUiState.Idle
        } else {
          performSearch(query)
        }
      }
      .launchIn(screenModelScope)
  }

  fun onEvent(event: SearchEvent) {
    when (event) {
      is SearchEvent.QueryChanged -> {
        // Update query immediately for responsive UI
        _query.value = event.query
        if (event.query.isNotBlank()) {
          mutableState.value = SearchUiState.Loading
        } else {
          mutableState.value = SearchUiState.Idle
        }
      }

      is SearchEvent.SuggestionClicked -> {
        screenModelScope.launch {
          _effects.emit(Effect.NavigateToSong(event.suggestion))
        }
      }

      SearchEvent.SearchSubmitted -> {
        val currentQuery = _query.value
        if (currentQuery.isNotBlank()) {
          screenModelScope.launch {
            _effects.emit(Effect.NavigateToResults(currentQuery))
          }
        }
      }

      SearchEvent.ClearQuery -> {
        _query.value = ""
        mutableState.value = SearchUiState.Idle
      }
    }
  }

  private fun performSearch(query: String) {
    searchJob?.cancel()
    searchJob = screenModelScope.launch {
      try {
        val results = searchSuggestionsUseCase(query)
        mutableState.value = SearchUiState.Suggestions(query, results.map { it.toUi() })
      } catch (e: Exception) {
        mutableState.value = SearchUiState.Error("Search error: ${e.message}")
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

