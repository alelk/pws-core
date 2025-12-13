package io.github.alelk.pws.features.favorites

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.favorite.model.FavoriteWithSongInfo
import io.github.alelk.pws.domain.favorite.usecase.ObserveFavoritesUseCase
import io.github.alelk.pws.domain.favorite.usecase.RemoveFavoriteUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

/**
 * ScreenModel for favorites screen.
 */
class FavoritesScreenModel(
  private val observeFavoritesUseCase: ObserveFavoritesUseCase,
  private val removeFavoriteUseCase: RemoveFavoriteUseCase
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
        observeFavoritesUseCase().collect { favorites ->
          val uiList = favorites.map { it.toUi() }
          mutableState.value = if (uiList.isEmpty()) {
            FavoritesUiState.Empty
          } else {
            FavoritesUiState.Content(uiList)
          }
        }
      } catch (e: Exception) {
        mutableState.value = FavoritesUiState.Error("Ошибка загрузки: ${e.message}")
      }
    }
  }

  private fun removeFromFavorites(song: FavoriteSongUi) {
    screenModelScope.launch {
      try {
        removeFavoriteUseCase(song.songNumberId)
        _effects.emit(Effect.ShowUndoSnackbar(song))
      } catch (e: Exception) {
        // Handle error
      }
    }
  }

  @OptIn(ExperimentalTime::class)
  private fun FavoriteWithSongInfo.toUi() = FavoriteSongUi(
    songNumberId = songNumberId,
    songNumber = songNumber,
    songName = songName,
    bookDisplayName = bookDisplayName,
    addedAt = addedAt
  )
}

