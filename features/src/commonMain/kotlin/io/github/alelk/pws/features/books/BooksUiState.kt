package io.github.alelk.pws.features.books

import io.github.alelk.pws.domain.book.model.BookSummary

sealed interface BooksUiState {
    data class Content(val books: List<BookSummary>) : BooksUiState
    object Loading : BooksUiState
    object Error : BooksUiState
}
