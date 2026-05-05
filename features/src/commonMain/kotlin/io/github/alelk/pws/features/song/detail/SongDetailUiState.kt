package io.github.alelk.pws.features.song.detail

import androidx.compose.runtime.Immutable
import io.github.alelk.pws.domain.song.model.SongDetail

sealed interface SongDetailUiState {
  @Immutable
  data class DisplayContext(
    val songNumber: Int? = null,
    val bookTitle: String? = null,
  )

  data class Content(
    val song: SongDetail,
    val context: DisplayContext = DisplayContext(),
    /** True when the donation banner should be shown at the bottom of the song content. */
    val showDonationCard: Boolean = false,
    val donationBoostyUrl: String = "",
  ) : SongDetailUiState

  object Loading : SongDetailUiState
  object Error : SongDetailUiState
}

