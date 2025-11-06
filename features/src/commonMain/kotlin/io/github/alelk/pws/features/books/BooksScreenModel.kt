package io.github.alelk.pws.features.books

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.book.usecase.ObserveBooksUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ScreenModel for books list. Observes all available books and exposes simplified UI state.
 * Assumes a dependency providing a Flow<List<BookSummary>> via constructor.
 */
class BooksScreenModel(
  private val observeBooks: ObserveBooksUseCase
) : StateScreenModel<BooksUiState>(BooksUiState.Loading) {

  init {
    screenModelScope.launch(context = CoroutineExceptionHandler { _, _ -> mutableState.value = BooksUiState.Error }) {
      observeBooks().collectLatest { list: List<BookSummary> ->
        mutableState.value = BooksUiState.Content(list)
      }
    }
  }
}
