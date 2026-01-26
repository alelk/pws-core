package io.github.alelk.pws.features.song.detail

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.usecase.ObserveSongUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ScreenModel for SongDetailBySongIdScreen.
 * Same as SongDetailScreenModel but accepts SongId directly.
 */
class SongDetailBySongIdScreenModel(
  val songId: SongId,
  private val observeSong: ObserveSongUseCase
) : StateScreenModel<SongDetailUiState>(SongDetailUiState.Loading) {

  init {
    screenModelScope.launch(context = CoroutineExceptionHandler { _, _ -> mutableState.value = SongDetailUiState.Error }) {
      observeSong(songId).collectLatest { detail: SongDetail? ->
        mutableState.value = detail?.let { SongDetailUiState.Content(it) } ?: SongDetailUiState.Error
      }
    }
  }
}

