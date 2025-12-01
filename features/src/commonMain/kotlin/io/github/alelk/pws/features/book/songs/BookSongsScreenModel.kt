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
    // Safety timeout to avoid infinite Loading if upstream flow never emits on JS
    screenModelScope.launch {
      kotlinx.coroutines.delay(8000)
      if (mutableState.value == BookSongsUiState.Loading) {
        println("[BookSongsScreenModel] Timeout waiting for data, showing error")
        mutableState.value = BookSongsUiState.Error
      }
    }

    screenModelScope.launch(context = CoroutineExceptionHandler { _, t ->
      println("[BookSongsScreenModel] Exception in collection: ${t?.message}")
      mutableState.value = BookSongsUiState.Error
    }) {
      println("[BookSongsScreenModel] Start collecting book with songs for bookId=$bookId")
      observeBookWithSongs(bookId).collectLatest { book: BookWithSongs? ->
        val newState = book?.let { BookSongsUiState.Content(it) } ?: BookSongsUiState.Error
        println("[BookSongsScreenModel] New state: ${newState::class.simpleName}")
        mutableState.value = newState
      }
    }
  }
}