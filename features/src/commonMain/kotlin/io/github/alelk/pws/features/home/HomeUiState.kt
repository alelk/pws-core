package io.github.alelk.pws.features.home

import androidx.compose.runtime.Immutable
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.history.model.HistoryEntry

/**
 * UI State for Home Screen.
 */
sealed interface HomeUiState {
  data object Loading : HomeUiState

  @Immutable
  data class Content(
    val books: List<BookSummary>,
    val recentSongs: List<HistoryEntry> = emptyList(),
    val isSearchActive: Boolean = false
  ) : HomeUiState

  data object Error : HomeUiState
}

/**
 * Search mode for quick actions.
 */
enum class SearchMode {
  /** Search by song number */
  NUMBER,
  /** Search by text/lyrics */
  TEXT
}

/**
 * Events from UI to ViewModel.
 */
sealed interface HomeEvent {
  /** User clicked on search field */
  data object SearchClicked : HomeEvent

  /** User clicked quick search by number */
  data object QuickNumberSearchClicked : HomeEvent

  /** User clicked quick search by text */
  data object QuickTextSearchClicked : HomeEvent

  /** User selected a book */
  data class BookClicked(val book: BookSummary) : HomeEvent
}
