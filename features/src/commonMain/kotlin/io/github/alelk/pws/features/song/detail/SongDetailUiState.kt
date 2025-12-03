package io.github.alelk.pws.features.song.detail

import io.github.alelk.pws.domain.song.model.SongDetail

sealed interface SongDetailUiState {
  data class Content(val song: SongDetail) : SongDetailUiState
  object Loading : SongDetailUiState
  object Error : SongDetailUiState
}

