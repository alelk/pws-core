package io.github.alelk.pws.features.tags

import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.command.CreateTagCommand
import io.github.alelk.pws.domain.tag.command.UpdateTagCommand
import io.github.alelk.pws.domain.tag.usecase.CreateTagUseCase
import io.github.alelk.pws.domain.tag.usecase.DeleteTagUseCase
import io.github.alelk.pws.domain.tag.usecase.ObserveTagsUseCase
import io.github.alelk.pws.domain.tag.usecase.UpdateTagUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import io.github.alelk.pws.domain.core.Color as DomainColor

/**
 * ScreenModel for tags management screen.
 */
class TagsScreenModel(
  private val observeTagsUseCase: ObserveTagsUseCase<TagId>,
  private val createTagUseCase: CreateTagUseCase<TagId>,
  private val updateTagUseCase: UpdateTagUseCase<TagId>,
  private val deleteTagUseCase: DeleteTagUseCase<TagId>
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
        observeTagsUseCase().collect { tags ->
          val uiTags = tags.map { it.toUi() }
          mutableState.value = if (uiTags.isEmpty()) {
            TagsUiState.Empty
          } else {
            TagsUiState.Content(uiTags)
          }
        }
      } catch (e: Exception) {
        mutableState.value = TagsUiState.Error("Ошибка загрузки: ${e.message}")
      }
    }
  }

  @OptIn(ExperimentalTime::class)
  private fun saveTag(name: String, color: Color) {
    val currentState = mutableState.value
    if (currentState !is TagsUiState.Content) return

    screenModelScope.launch {
      try {
        val editingTag = currentState.editingTag
        val domainColor = color.toDomain()
        if (editingTag != null) {
          // Update existing tag
          val command = UpdateTagCommand(
            id = editingTag.id,
            name = name,
            color = domainColor
          )
          updateTagUseCase(command)
          _effects.emit(Effect.ShowSnackbar("Категория обновлена"))
        } else {
          // Create new tag - use TagId.Custom for user-created tags
          val tagId = TagId.Custom.random()
          val command = CreateTagCommand(
            id = tagId,
            name = name,
            color = domainColor
          )
          createTagUseCase(command)
          _effects.emit(Effect.ShowSnackbar("Категория создана"))
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
        deleteTagUseCase(tag.id)
        updateState { state ->
          if (state is TagsUiState.Content) {
            state.copy(showDeleteConfirmation = null)
          } else state
        }
        val message = if (tag.isPredefined) "Категория скрыта" else "Категория удалена"
        _effects.emit(Effect.ShowSnackbar(message))
      } catch (e: Exception) {
        _effects.emit(Effect.ShowSnackbar("Ошибка: ${e.message}"))
      }
    }
  }

  private inline fun updateState(transform: (TagsUiState) -> TagsUiState) {
    mutableState.value = transform(mutableState.value)
  }

  private fun Tag<*>.toUi() = TagUi(
    id = id,
    name = name,
    color = color.toCompose(),
    songCount = 0, // Tag doesn't have songCount, only TagDetail does
    isPredefined = this is Tag.Predefined
  )

  private fun DomainColor.toCompose() = Color(r / 255f, g / 255f, b / 255f)

  private fun Color.toDomain() = DomainColor(
    r = (red * 255).toInt(),
    g = (green * 255).toInt(),
    b = (blue * 255).toInt()
  )
}

