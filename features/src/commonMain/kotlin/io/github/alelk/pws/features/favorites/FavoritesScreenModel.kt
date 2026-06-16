package io.github.alelk.pws.features.favorites

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.favorite.model.FavoriteSong
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.usecase.ObserveFavoritesUseCase
import io.github.alelk.pws.domain.favorite.usecase.RemoveFavoriteUseCase
import io.github.alelk.pws.features.app.UiMessage
import io.github.alelk.pws.features.song.detail.FavoritesDisplaySettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

/**
 * ScreenModel for favorites screen.
 */
class FavoritesScreenModel(
  private val observeFavoritesUseCase: ObserveFavoritesUseCase,
  private val removeFavoriteUseCase: RemoveFavoriteUseCase,
  /** Injected scope for testing with virtual time; null = use [screenModelScope]. */
  private val coroutineScope: CoroutineScope? = null,
) : StateScreenModel<FavoritesUiState>(FavoritesUiState.Loading) {

  private val scope: CoroutineScope get() = coroutineScope ?: screenModelScope

  private data class SortConfig(val mode: FavoriteSortMode, val ascending: Boolean)

  private val sortConfig = MutableStateFlow(SortConfig(FavoriteSortMode.ADDED_DATE, ascending = false))
  private var displaySettings: FavoritesDisplaySettings? = null

  sealed interface Effect {
    data class NavigateToSong(val song: FavoriteSongUi) : Effect
    data class ShowUndoSnackbar(val song: FavoriteSongUi) : Effect
    data class ShowError(val message: UiMessage) : Effect
  }

  private val _effects = MutableSharedFlow<Effect>()
  val effects = _effects.asSharedFlow()

  init {
    // Combine upstream favourites with the current sort config — sort changes
    // re-emit a sorted slice without restarting the upstream observe.
    combine(observeFavoritesUseCase(), sortConfig) { favorites, cfg ->
      val uiList = favorites.map { it.toUi() }
      if (uiList.isEmpty()) {
        FavoritesUiState.Empty
      } else {
        FavoritesUiState.Content(
          songs = sort(uiList, cfg.mode, cfg.ascending),
          sortMode = cfg.mode,
          ascending = cfg.ascending,
        )
      }
    }
      .onEach { mutableState.value = it }
      .catch { mutableState.value = FavoritesUiState.Error(UiMessage.Failure(it.message)) }
      .launchIn(scope)
  }

  fun setDisplaySettings(settings: FavoritesDisplaySettings) {
    if (displaySettings != null) return
    displaySettings = settings
    val initialMode = runCatching { FavoriteSortMode.valueOf(settings.sortMode) }
      .getOrDefault(FavoriteSortMode.ADDED_DATE)
    sortConfig.value = SortConfig(initialMode, settings.ascending)
  }

  fun onEvent(event: FavoritesEvent) {
    when (event) {
      is FavoritesEvent.SongClicked -> {
        scope.launch {
          _effects.emit(Effect.NavigateToSong(event.song))
        }
      }

      is FavoritesEvent.RemoveFromFavorites -> removeFromFavorites(event.song)

      is FavoritesEvent.ChangeSortMode -> {
        val current = sortConfig.value
        val modeChanged = current.mode != event.mode
        // ADDED_DATE → descending (newest first); other modes → ascending.
        val nextAscending = if (modeChanged) event.mode != FavoriteSortMode.ADDED_DATE else current.ascending
        sortConfig.value = SortConfig(event.mode, nextAscending)
        if (modeChanged) displaySettings?.onAscendingChange?.invoke(nextAscending)
        displaySettings?.onSortModeChange?.invoke(event.mode.name)
      }

      FavoritesEvent.ToggleSortDirection -> {
        val current = sortConfig.value
        val next = current.copy(ascending = !current.ascending)
        sortConfig.value = next
        displaySettings?.onAscendingChange?.invoke(next.ascending)
      }
    }
  }

  private fun removeFromFavorites(song: FavoriteSongUi) {
    scope.launch {
      try {
        removeFavoriteUseCase(song.subject)
        _effects.emit(Effect.ShowUndoSnackbar(song))
      } catch (e: Exception) {
        _effects.emit(Effect.ShowError(UiMessage.Failure(e.message)))
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
