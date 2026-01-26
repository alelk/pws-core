package io.github.alelk.pws.features.search

import androidx.compose.runtime.Immutable
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId

/**
 * Book reference in search suggestion for navigation.
 */
@Immutable
data class BookReferenceUi(
  val bookId: BookId,
  val displayShortName: String,
  val songNumber: Int
)

/**
 * Search suggestion UI model.
 *
 * Contains book references for navigation:
 * - If bookReferences is not empty, navigate to song in first book context
 * - If bookReferences is empty, navigate to song by id
 */
@Immutable
data class SearchSuggestion(
  val songId: SongId,
  val songName: String,
  val bookReferences: List<BookReferenceUi> = emptyList(),
  val snippet: String? = null
) {
  /** Display string for books (e.g., "HYM, PSA") */
  val booksDisplayText: String get() = bookReferences.joinToString(", ") { it.displayShortName }
}

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

