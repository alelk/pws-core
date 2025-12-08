package io.github.alelk.pws.features.tags

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.github.alelk.pws.domain.core.ids.TagId

@Immutable
data class TagUi(
  val id: TagId,
  val name: String,
  val color: Color,
  val songCount: Int,
  val isPredefined: Boolean
)

sealed interface TagsUiState {
  data object Loading : TagsUiState

  @Immutable
  data class Content(
    val tags: List<TagUi>,
    val editingTag: TagUi? = null,
    val showAddDialog: Boolean = false,
    val showDeleteConfirmation: TagUi? = null
  ) : TagsUiState

  data object Empty : TagsUiState
  data class Error(val message: String) : TagsUiState
}

sealed interface TagsEvent {
  data class TagClicked(val tag: TagUi) : TagsEvent
  data class EditTag(val tag: TagUi) : TagsEvent
  data class DeleteTag(val tag: TagUi) : TagsEvent
  data class ConfirmDeleteTag(val tag: TagUi) : TagsEvent
  data object DismissDeleteConfirmation : TagsEvent
  data object AddTagClicked : TagsEvent
  data class SaveTag(val name: String, val color: Color) : TagsEvent
  data object DismissDialog : TagsEvent
}

