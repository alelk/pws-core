package io.github.alelk.pws.features.home

import androidx.compose.runtime.Immutable
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.history.model.HistoryEntry
import io.github.alelk.pws.features.search.SearchSuggestion

/**
 * Single composite UI state for the Home screen. The skill (`compose-multiplatform-ui`)
 * mandates one sealed `Loading / Content / Error` state per screen — all transient
 * fields (search query, suggestions, loading spinners) live inside [Content].
 */
sealed interface HomeUiState {
  data object Loading : HomeUiState

  @Immutable
  data class Content(
    val books: List<BookSummary>,
    val recentSongs: List<HistoryEntry> = emptyList(),
    val searchQuery: String = "",
    val searchSuggestions: List<SearchSuggestion> = emptyList(),
    val isSearching: Boolean = false,
    val numberQuery: String = "",
    val numberSuggestions: List<SearchSuggestion> = emptyList(),
    val isNumberSearching: Boolean = false,
  ) : HomeUiState

  data object Error : HomeUiState
}

/** User-driven events accepted by [HomeScreenModel]. */
sealed interface HomeEvent {
  data class SearchQueryChanged(val query: String) : HomeEvent
  data object SearchCleared : HomeEvent
  data class NumberQueryChanged(val query: String) : HomeEvent
  data object NumberCleared : HomeEvent
}
