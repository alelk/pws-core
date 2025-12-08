package io.github.alelk.pws.features.song.edit

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId

@Immutable
data class EditableTagUi(
  val id: TagId,
  val name: String,
  val color: Color,
  val isSelected: Boolean
)

sealed interface SongEditUiState {
  data object Loading : SongEditUiState

  @Immutable
  data class Content(
    val songId: SongId,
    val title: String,
    val number: String,
    val text: String,
    val allTags: List<EditableTagUi>,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val validationError: String? = null
  ) : SongEditUiState

  data class Error(val message: String) : SongEditUiState
}

sealed interface SongEditEvent {
  data class TitleChanged(val title: String) : SongEditEvent
  data class NumberChanged(val number: String) : SongEditEvent
  data class TextChanged(val text: String) : SongEditEvent
  data class TagToggled(val tagId: TagId) : SongEditEvent
  data object SaveClicked : SongEditEvent
  data object CancelClicked : SongEditEvent
  data object DiscardChangesConfirmed : SongEditEvent
  data object DismissDiscardDialog : SongEditEvent
}

