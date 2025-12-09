package io.github.alelk.pws.features.tags.songs

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId

@Immutable
data class TagSongUi(
  val songNumberId: SongNumberId,
  val songNumber: Int,
  val songName: String,
  val bookDisplayName: String
)

@Immutable
data class TagInfoUi(
  val id: TagId,
  val name: String,
  val color: Color
)

sealed interface TagSongsUiState {
  data object Loading : TagSongsUiState

  @Immutable
  data class Content(
    val tag: TagInfoUi,
    val songs: List<TagSongUi>
  ) : TagSongsUiState

  data object Empty : TagSongsUiState
  data class Error(val message: String) : TagSongsUiState
}

