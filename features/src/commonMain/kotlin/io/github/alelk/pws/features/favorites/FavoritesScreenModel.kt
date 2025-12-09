package io.github.alelk.pws.features.favorites

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * ScreenModel for favorites screen.
 */
class FavoritesScreenModel(
  // TODO: inject favorites repository/use case
  // private val observeFavoritesUseCase: ObserveFavoritesUseCase,
  // private val removeFavoriteUseCase: RemoveFavoriteUseCase
) : StateScreenModel<FavoritesUiState>(FavoritesUiState.Loading) {

  sealed interface Effect {
    data class NavigateToSong(val song: FavoriteSongUi) : Effect
    data class ShowUndoSnackbar(val song: FavoriteSongUi) : Effect
  }

  private val _effects = MutableSharedFlow<Effect>()
  val effects = _effects.asSharedFlow()

  init {
    loadFavorites()
  }

  fun onEvent(event: FavoritesEvent) {
    when (event) {
      is FavoritesEvent.SongClicked -> {
        screenModelScope.launch {
          _effects.emit(Effect.NavigateToSong(event.song))
        }
      }

      is FavoritesEvent.RemoveFromFavorites -> {
        removeFromFavorites(event.song)
      }
    }
  }

  private fun loadFavorites() {
    screenModelScope.launch {
      try {
        // TODO: Replace with actual implementation
        // observeFavoritesUseCase().collect { favorites ->
        //   mutableState.value = if (favorites.isEmpty()) {
        //     FavoritesUiState.Empty
        //   } else {
        //     FavoritesUiState.Content(favorites)
        //   }
        // }

        // Placeholder
        mutableState.value = FavoritesUiState.Empty
      } catch (e: Exception) {
        mutableState.value = FavoritesUiState.Error("Ошибка загрузки: ${e.message}")
      }
    }
  }

  private fun removeFromFavorites(song: FavoriteSongUi) {
    screenModelScope.launch {
      try {
        // TODO: Replace with actual implementation
        // removeFavoriteUseCase(song.songNumberId)
        _effects.emit(Effect.ShowUndoSnackbar(song))
      } catch (e: Exception) {
        // Handle error
      }
    }
  }
}

