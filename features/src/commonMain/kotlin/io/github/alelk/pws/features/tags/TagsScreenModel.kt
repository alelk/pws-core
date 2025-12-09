package io.github.alelk.pws.features.tags

import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * ScreenModel for tags management screen.
 */
class TagsScreenModel(
  // TODO: inject tags repository/use cases
  // private val observeTagsUseCase: ObserveTagsUseCase,
  // private val createTagUseCase: CreateTagUseCase,
  // private val updateTagUseCase: UpdateTagUseCase,
  // private val deleteTagUseCase: DeleteTagUseCase
) : StateScreenModel<TagsUiState>(TagsUiState.Loading) {

  sealed interface Effect {
    data class NavigateToTagSongs(val tag: TagUi) : Effect
    data class ShowSnackbar(val message: String) : Effect
  }

  private val _effects = MutableSharedFlow<Effect>()
  val effects = _effects.asSharedFlow()

  init {
    loadTags()
  }

  fun onEvent(event: TagsEvent) {
    when (event) {
      is TagsEvent.TagClicked -> {
        screenModelScope.launch {
          _effects.emit(Effect.NavigateToTagSongs(event.tag))
        }
      }

      is TagsEvent.EditTag -> {
        updateState { state ->
          if (state is TagsUiState.Content) {
            state.copy(editingTag = event.tag, showAddDialog = true)
          } else state
        }
      }

      is TagsEvent.DeleteTag -> {
        updateState { state ->
          if (state is TagsUiState.Content) {
            state.copy(showDeleteConfirmation = event.tag)
          } else state
        }
      }

      is TagsEvent.ConfirmDeleteTag -> {
        deleteTag(event.tag)
      }

      TagsEvent.DismissDeleteConfirmation -> {
        updateState { state ->
          if (state is TagsUiState.Content) {
            state.copy(showDeleteConfirmation = null)
          } else state
        }
      }

      TagsEvent.AddTagClicked -> {
        updateState { state ->
          if (state is TagsUiState.Content) {
            state.copy(showAddDialog = true, editingTag = null)
          } else state
        }
      }

      is TagsEvent.SaveTag -> {
        saveTag(event.name, event.color)
      }

      TagsEvent.DismissDialog -> {
        updateState { state ->
          if (state is TagsUiState.Content) {
            state.copy(showAddDialog = false, editingTag = null)
          } else state
        }
      }
    }
  }

  private fun loadTags() {
    screenModelScope.launch {
      try {
        // TODO: Replace with actual implementation
        // observeTagsUseCase().collect { tags ->
        //   mutableState.value = if (tags.isEmpty()) {
        //     TagsUiState.Empty
        //   } else {
        //     TagsUiState.Content(tags)
        //   }
        // }

        // Placeholder
        mutableState.value = TagsUiState.Empty
      } catch (e: Exception) {
        mutableState.value = TagsUiState.Error("Ошибка загрузки: ${e.message}")
      }
    }
  }

  private fun saveTag(name: String, color: Color) {
    val currentState = mutableState.value
    if (currentState !is TagsUiState.Content) return

    screenModelScope.launch {
      try {
        val editingTag = currentState.editingTag
        if (editingTag != null) {
          // Update existing tag
          // updateTagUseCase(editingTag.id, name, color)
          _effects.emit(Effect.ShowSnackbar("Тег обновлён"))
        } else {
          // Create new tag
          // createTagUseCase(name, color)
          _effects.emit(Effect.ShowSnackbar("Тег создан"))
        }

        updateState { state ->
          if (state is TagsUiState.Content) {
            state.copy(showAddDialog = false, editingTag = null)
          } else state
        }
      } catch (e: Exception) {
        _effects.emit(Effect.ShowSnackbar("Ошибка: ${e.message}"))
      }
    }
  }

  private fun deleteTag(tag: TagUi) {
    screenModelScope.launch {
      try {
        // deleteTagUseCase(tag.id)
        updateState { state ->
          if (state is TagsUiState.Content) {
            state.copy(showDeleteConfirmation = null)
          } else state
        }
        _effects.emit(Effect.ShowSnackbar("Тег удалён"))
      } catch (e: Exception) {
        _effects.emit(Effect.ShowSnackbar("Ошибка: ${e.message}"))
      }
    }
  }

  private inline fun updateState(transform: (TagsUiState) -> TagsUiState) {
    mutableState.value = transform(mutableState.value)
  }
}

