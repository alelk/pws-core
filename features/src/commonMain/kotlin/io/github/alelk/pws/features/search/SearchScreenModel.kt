package io.github.alelk.pws.features.search

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.song.model.SongSearchSuggestion
import io.github.alelk.pws.domain.song.usecase.SearchSongSuggestionsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

  private val queryFlow = MutableStateFlow("")
  private var searchJob: Job? = null

  sealed interface Effect {
    data class NavigateToSong(val suggestion: SearchSuggestion) : Effect
    data class NavigateToResults(val query: String) : Effect
  }

  private val _effects = MutableSharedFlow<Effect>()
  val effects = _effects.asSharedFlow()

  init {
    // Debounce search queries
    queryFlow
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
        queryFlow.value = event.query
        if (event.query.isNotBlank()) {
          mutableState.value = SearchUiState.Loading
        }
      }

      is SearchEvent.SuggestionClicked -> {
        screenModelScope.launch {
          _effects.emit(Effect.NavigateToSong(event.suggestion))
        }
      }

      SearchEvent.SearchSubmitted -> {
        val query = queryFlow.value
        if (query.isNotBlank()) {
          screenModelScope.launch {
            _effects.emit(Effect.NavigateToResults(query))
          }
        }
      }

      SearchEvent.ClearQuery -> {
        queryFlow.value = ""
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
    books = books,
    snippet = snippet
  )
}

