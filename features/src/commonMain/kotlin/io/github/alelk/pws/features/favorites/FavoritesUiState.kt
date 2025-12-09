package io.github.alelk.pws.features.favorites

import androidx.compose.runtime.Immutable
import io.github.alelk.pws.domain.core.ids.SongNumberId

@Immutable
data class FavoriteSongUi(
  val songNumberId: SongNumberId,
  val songNumber: Int,
  val songName: String,
  val bookDisplayName: String,
  val addedAt: Long // timestamp
)

sealed interface FavoritesUiState {
  data object Loading : FavoritesUiState

  @Immutable
  data class Content(
    val songs: List<FavoriteSongUi>
  ) : FavoritesUiState

  data object Empty : FavoritesUiState
  data class Error(val message: String) : FavoritesUiState
}

sealed interface FavoritesEvent {
  data class SongClicked(val song: FavoriteSongUi) : FavoritesEvent
  data class RemoveFromFavorites(val song: FavoriteSongUi) : FavoritesEvent
}

