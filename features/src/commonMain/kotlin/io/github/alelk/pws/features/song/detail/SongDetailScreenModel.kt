package io.github.alelk.pws.features.song.detail

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.usecase.ObserveIsFavoriteUseCase
import io.github.alelk.pws.domain.favorite.usecase.ToggleFavoriteUseCase
import io.github.alelk.pws.domain.history.model.HistorySubject
import io.github.alelk.pws.domain.history.usecase.RecordSongViewUseCase
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.usecase.ObserveSongUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SongDetailScreenModel(
  val songNumberId: SongNumberId,
  private val observeSong: ObserveSongUseCase,
  private val recordSongView: RecordSongViewUseCase,
  private val observeIsFavorite: ObserveIsFavoriteUseCase,
  private val toggleFavorite: ToggleFavoriteUseCase
) : StateScreenModel<SongDetailUiState>(SongDetailUiState.Loading) {

  private val _isFavorite = MutableStateFlow(false)
  val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

  private val favoriteSubject = FavoriteSubject.BookedSong(songNumberId)

  init {
    screenModelScope.launch(context = CoroutineExceptionHandler { _, _ -> mutableState.value = SongDetailUiState.Error }) {
      observeSong(songNumberId.songId).collectLatest { detail: SongDetail? ->
        mutableState.value = detail?.let { SongDetailUiState.Content(it) } ?: SongDetailUiState.Error
      }
    }

    // Observe favorite status
    screenModelScope.launch {
      observeIsFavorite(favoriteSubject).collectLatest { isFav ->
        _isFavorite.value = isFav
      }
    }
  }

  fun onSongViewed() {
    screenModelScope.launch {
      try {
        recordSongView(HistorySubject.BookedSong(songNumberId))
      } catch (e: Exception) {
        // Ignore errors when recording view
      }
    }
  }

  fun onToggleFavorite() {
    screenModelScope.launch {
      try {
        toggleFavorite(favoriteSubject)
      } catch (e: Exception) {
        // Handle error if needed
      }
    }
  }
}
