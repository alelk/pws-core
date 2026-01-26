package io.github.alelk.pws.features.home

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.book.usecase.ObserveBooksUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * ScreenModel for Home Screen.
 * Manages books list and search navigation.
 */
class HomeScreenModel(
  observeBooksUseCase: ObserveBooksUseCase
) : StateScreenModel<HomeUiState>(HomeUiState.Loading) {

  init {
    observeBooksUseCase()
      .onEach { books ->
        mutableState.value = HomeUiState.Content(books = books)
      }
      .catch {
        mutableState.value = HomeUiState.Error
      }
      .launchIn(screenModelScope)
  }
}
