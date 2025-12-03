package io.github.alelk.pws.features.book.songs

import io.github.alelk.pws.domain.cross.projection.BookWithSongs

sealed interface BookSongsUiState {
  data class Content(val book: BookWithSongs) : BookSongsUiState

  object Loading : BookSongsUiState
  object Error : BookSongsUiState
}