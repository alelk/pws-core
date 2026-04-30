package io.github.alelk.pws.features.favorites

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.favorite.model.FavoriteSong
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.usecase.ObserveFavoritesUseCase
import io.github.alelk.pws.domain.favorite.usecase.RemoveFavoriteUseCase
import io.github.alelk.pws.features.song.detail.FavoritesDisplaySettings
import kotlinx.coroutines.Job
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

  private var currentSortMode: FavoriteSortMode = FavoriteSortMode.ADDED_DATE
  private var currentAscending: Boolean = false
  private var displaySettings: FavoritesDisplaySettings? = null

  sealed interface Effect {
    data class NavigateToSong(val song: FavoriteSongUi) : Effect
    data class ShowUndoSnackbar(val song: FavoriteSongUi) : Effect
  }

  private val _effects = MutableSharedFlow<Effect>()
  val effects = _effects.asSharedFlow()
  private var loadJob: Job? = null

  init {
    loadFavorites()
  }

  fun setDisplaySettings(settings: FavoritesDisplaySettings) {
    if (displaySettings != null) return
    displaySettings = settings
    currentSortMode = runCatching { FavoriteSortMode.valueOf(settings.sortMode) }.getOrDefault(FavoriteSortMode.ADDED_DATE)
    currentAscending = settings.ascending
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

      is FavoritesEvent.ChangeSortMode -> {
        currentSortMode = event.mode
        displaySettings?.onSortModeChange?.invoke(event.mode.name)
        loadFavorites()
      }

      FavoritesEvent.ToggleSortDirection -> {
        currentAscending = !currentAscending
        displaySettings?.onAscendingChange?.invoke(currentAscending)
        loadFavorites()
      }
    }
  }

  private fun loadFavorites() {
    loadJob?.cancel()
    loadJob = screenModelScope.launch {
      try {
        observeFavoritesUseCase().collect { favorites ->
          val uiList = favorites.map { it.toUi() }
          val sorted = sort(uiList, currentSortMode, currentAscending)
          mutableState.value = if (uiList.isEmpty()) {
            FavoritesUiState.Empty
          } else {
            FavoritesUiState.Content(
              songs = sorted,
              sortMode = currentSortMode,
              ascending = currentAscending
            )
          }
        }
      } catch (e: Exception) {
        mutableState.value = FavoritesUiState.Error(e.message ?: "Unknown error")
      }
    }
  }

  private fun removeFromFavorites(song: FavoriteSongUi) {
    screenModelScope.launch {
      try {
        removeFavoriteUseCase(song.subject)
        _effects.emit(Effect.ShowUndoSnackbar(song))
      } catch (e: Exception) {
        // Handle error
      }
    }
  }

  @OptIn(ExperimentalTime::class)
  private fun FavoriteSong.toUi(): FavoriteSongUi = when (val s = subject) {
    is FavoriteSubject.BookedSong -> FavoriteSongUi.BookedSong(
      subject = s,
      songNumber = songNumber ?: 0,
      songName = songName,
      bookDisplayName = bookDisplayName ?: "",
      addedAt = addedAt
    )
    is FavoriteSubject.StandaloneSong -> FavoriteSongUi.StandaloneSong(
      subject = s,
      songName = songName,
      addedAt = addedAt
    )
  }

  private fun sort(items: List<FavoriteSongUi>, mode: FavoriteSortMode, ascending: Boolean): List<FavoriteSongUi> {
    val comparator = when (mode) {
      FavoriteSortMode.ADDED_DATE -> compareBy<FavoriteSongUi> { it.addedAt }
      FavoriteSortMode.SONG_NUMBER -> compareBy<FavoriteSongUi> {
        when (it) {
          is FavoriteSongUi.BookedSong -> it.songNumber
          is FavoriteSongUi.StandaloneSong -> Int.MAX_VALUE
        }
      }
      FavoriteSortMode.SONG_NAME -> compareBy { it.songName.lowercase() }
    }
    return if (ascending) items.sortedWith(comparator) else items.sortedWith(comparator.reversed())
  }
}
