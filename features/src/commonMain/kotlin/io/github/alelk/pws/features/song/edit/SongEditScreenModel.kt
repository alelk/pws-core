package io.github.alelk.pws.features.song.edit

import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.song.command.UpdateSongCommand
import io.github.alelk.pws.domain.song.usecase.GetSongDetailUseCase
import io.github.alelk.pws.domain.song.usecase.UpdateSongUseCase
import io.github.alelk.pws.domain.tag.usecase.ObserveTagsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import io.github.alelk.pws.domain.core.Color as DomainColor

/**
 * ScreenModel for song editing.
 */
class SongEditScreenModel(
  private val songId: SongId,
  private val getSongDetailUseCase: GetSongDetailUseCase,
  private val updateSongUseCase: UpdateSongUseCase,
  private val observeTagsUseCase: ObserveTagsUseCase<TagId>
) : StateScreenModel<SongEditUiState>(SongEditUiState.Loading) {

  sealed interface Effect {
    data object NavigateBack : Effect
    data class ShowSnackbar(val message: String) : Effect
  }

  private val _effects = MutableSharedFlow<Effect>()
  val effects = _effects.asSharedFlow()

  private val _showDiscardDialog = MutableStateFlow(false)
  val showDiscardDialog = _showDiscardDialog.asStateFlow()

  private var originalTitle = ""
  private var originalText = ""

  init {
    loadSong()
  }

  fun onEvent(event: SongEditEvent) {
    when (event) {
      is SongEditEvent.TitleChanged -> updateTitle(event.title)
      is SongEditEvent.NumberChanged -> { /* Number is read-only from SongDetail */ }
      is SongEditEvent.TextChanged -> updateText(event.text)
      is SongEditEvent.TagToggled -> toggleTag(event.tagId)
      SongEditEvent.SaveClicked -> save()
      SongEditEvent.CancelClicked -> handleCancel()
      SongEditEvent.DiscardChangesConfirmed -> discardAndNavigateBack()
      SongEditEvent.DismissDiscardDialog -> _showDiscardDialog.value = false
    }
  }

  private fun loadSong() {
    screenModelScope.launch {
      try {
        val song = getSongDetailUseCase(songId)
        if (song == null) {
          mutableState.value = SongEditUiState.Error("Песня не найдена")
          return@launch
        }

        val allTags = observeTagsUseCase().first()

        // Extract name and lyric text
        val name = song.name.value
        val lyricText = song.lyric.toString()

        originalTitle = name
        originalText = lyricText

        mutableState.value = SongEditUiState.Content(
          songId = songId,
          title = name,
          number = "", // Song number comes from SongNumber, not SongDetail
          text = lyricText,
          allTags = allTags.map { tag ->
            EditableTagUi(
              id = tag.id,
              name = tag.name,
              color = tag.color.toCompose(),
              isSelected = false // Tags for song are managed separately
            )
          }
        )
      } catch (e: Exception) {
        mutableState.value = SongEditUiState.Error("Ошибка загрузки: ${e.message}")
      }
    }
  }

  private fun updateTitle(title: String) {
    updateContent { it.copy(title = title, hasUnsavedChanges = hasChanges(title = title)) }
  }

  private fun updateText(text: String) {
    updateContent { it.copy(text = text, hasUnsavedChanges = hasChanges(text = text)) }
  }

  private fun toggleTag(tagId: TagId) {
    updateContent { content ->
      val updatedTags = content.allTags.map { tag ->
        if (tag.id == tagId) tag.copy(isSelected = !tag.isSelected) else tag
      }
      content.copy(allTags = updatedTags, hasUnsavedChanges = hasChanges(tags = updatedTags))
    }
  }

  private fun hasChanges(
    title: String? = null,
    text: String? = null,
    tags: List<EditableTagUi>? = null
  ): Boolean {
    val currentState = mutableState.value as? SongEditUiState.Content ?: return false
    val t = title ?: currentState.title
    val tx = text ?: currentState.text

    return t != originalTitle || tx != originalText
  }

  private fun save() {
    val currentState = mutableState.value as? SongEditUiState.Content ?: return

    // Validate
    if (currentState.title.isBlank()) {
      updateContent { it.copy(validationError = "Название не может быть пустым") }
      return
    }
    if (currentState.text.isBlank()) {
      updateContent { it.copy(validationError = "Текст песни не может быть пустым") }
      return
    }

    screenModelScope.launch {
      updateContent { it.copy(isSaving = true, validationError = null) }
      try {
        // TODO: add lyric parsing when parser is ready
        val command = UpdateSongCommand(
          id = songId,
          name = NonEmptyString(currentState.title)
        )
        updateSongUseCase(command)

        _effects.emit(Effect.ShowSnackbar("Изменения сохранены"))
        _effects.emit(Effect.NavigateBack)
      } catch (e: Exception) {
        updateContent { it.copy(isSaving = false, validationError = "Ошибка сохранения: ${e.message}") }
      }
    }
  }

  private fun handleCancel() {
    val currentState = mutableState.value as? SongEditUiState.Content ?: return
    if (currentState.hasUnsavedChanges) {
      _showDiscardDialog.value = true
    } else {
      screenModelScope.launch {
        _effects.emit(Effect.NavigateBack)
      }
    }
  }

  private fun discardAndNavigateBack() {
    _showDiscardDialog.value = false
    screenModelScope.launch {
      _effects.emit(Effect.NavigateBack)
    }
  }

  private inline fun updateContent(transform: (SongEditUiState.Content) -> SongEditUiState.Content) {
    val current = mutableState.value
    if (current is SongEditUiState.Content) {
      mutableState.value = transform(current)
    }
  }

  private fun DomainColor.toCompose() = Color(r / 255f, g / 255f, b / 255f)
}
