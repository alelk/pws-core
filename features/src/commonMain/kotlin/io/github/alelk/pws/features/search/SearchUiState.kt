package io.github.alelk.pws.features.search

import androidx.compose.runtime.Immutable
import io.github.alelk.pws.domain.core.ids.SongId

@Immutable
data class SearchSuggestion(
  val songId: SongId,
  val songName: String,
  val books: List<String>,
  val snippet: String? = null
)

sealed interface SearchUiState {
  data object Idle : SearchUiState
  data object Loading : SearchUiState

  @Immutable
  data class Suggestions(
    val query: String,
    val items: List<SearchSuggestion>
  ) : SearchUiState

  @Immutable
  data class Results(
    val query: String,
    val items: List<SearchSuggestion>,
    val isLoading: Boolean = false
  ) : SearchUiState

  data class Error(val message: String) : SearchUiState
}

sealed interface SearchEvent {
  data class QueryChanged(val query: String) : SearchEvent
  data class SuggestionClicked(val suggestion: SearchSuggestion) : SearchEvent
  data object SearchSubmitted : SearchEvent
  data object ClearQuery : SearchEvent
}

