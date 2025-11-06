package io.github.alelk.pws.features.book.songs

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.cross.projection.BookWithSongs
import io.github.alelk.pws.domain.cross.usecase.ObserveBookWithSongsUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookSongsScreenModel(
  val bookId: BookId,
  private val observeBookWithSongs: ObserveBookWithSongsUseCase
) : StateScreenModel<BookSongsUiState>(BookSongsUiState.Loading) {

  init {
    screenModelScope.launch(context = CoroutineExceptionHandler { _, _ -> mutableState.value = BookSongsUiState.Error }) {
      observeBookWithSongs(bookId).collectLatest { book: BookWithSongs? ->
        mutableState.value = book?.let { BookSongsUiState.Content(it) } ?: BookSongsUiState.Error
      }
    }
  }
}