package io.github.alelk.pws.features.song.detail

import androidx.compose.runtime.Immutable
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.songreference.usecase.SongReferenceDetail
import io.github.alelk.pws.domain.tag.model.Tag

sealed interface SongDetailUiState {
  @Immutable
  data class DisplayContext(
    val songNumber: Int? = null,
    val bookTitle: String? = null,
  )

  /**
   * Aggregates everything that depends on the currently shown song.
   * Pager-related data (book navigation list, jump-by-number map) and the global
   * `allTags` list are still exposed as separate StateFlows on the ScreenModel —
   * they're independent of which song is displayed.
   */
  @Immutable
  data class Content(
    val song: SongDetail,
    val context: DisplayContext = DisplayContext(),
    val isFavorite: Boolean = false,
    val songTags: List<Tag<TagId>> = emptyList(),
    val references: List<SongReferenceDetail> = emptyList(),
    val referenceBookContexts: Map<SongId, List<SongDetailScreenModel.ReferenceBookContextUi>> = emptyMap(),
    /** True when the donation banner should be shown at the bottom of the song content. */
    val showDonationCard: Boolean = false,
    val donationBoostyUrl: String = "",
  ) : SongDetailUiState

  data object Loading : SongDetailUiState
  data object Error : SongDetailUiState
}
