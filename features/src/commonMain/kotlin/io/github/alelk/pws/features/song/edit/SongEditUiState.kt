package io.github.alelk.pws.features.song.edit

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId

@Immutable
data class EditableTagUi(
  val id: TagId,
  val name: String,
  val color: Color,
  val isSelected: Boolean
)

sealed interface SongEditValidationMessage {
  data object TitleRequired : SongEditValidationMessage
  data object TextRequired : SongEditValidationMessage
  data class InvalidTextFormat(val details: String?) : SongEditValidationMessage
  data class SaveError(val details: String?) : SongEditValidationMessage
}

sealed interface SongEditSnackbarMessage {
  data object Saved : SongEditSnackbarMessage
}

sealed interface SongEditUiState {
  data object Loading : SongEditUiState

  @Immutable
  data class Content(
    val songId: SongId,
    val locale: Locale,
    val title: String,
    val number: String,
    val text: String,
    val author: String = "",
    val composer: String = "",
    val translator: String = "",
    val year: String = "",
    val bibleRef: String = "",
    val tonalities: List<io.github.alelk.pws.domain.tonality.Tonality> = emptyList(),
    val allTags: List<EditableTagUi>,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val validationMessage: SongEditValidationMessage? = null
  ) : SongEditUiState

  data class Error(val message: String) : SongEditUiState
}

sealed interface SongEditEvent {
  data class TitleChanged(val title: String) : SongEditEvent
  data class NumberChanged(val number: String) : SongEditEvent
  data class TextChanged(val text: String) : SongEditEvent
  data class AuthorChanged(val author: String) : SongEditEvent
  data class ComposerChanged(val composer: String) : SongEditEvent
  data class YearChanged(val year: String) : SongEditEvent
  data class BibleRefChanged(val bibleRef: String) : SongEditEvent
  data class TranslatorChanged(val translator: String) : SongEditEvent
  data class TonalityToggled(val tonality: io.github.alelk.pws.domain.tonality.Tonality) : SongEditEvent
  data class TagToggled(val tagId: TagId) : SongEditEvent
  data object SaveClicked : SongEditEvent
  data object CancelClicked : SongEditEvent
  data object DiscardChangesConfirmed : SongEditEvent
  data object DismissDiscardDialog : SongEditEvent
}
