package io.github.alelk.pws.features.song.edit

import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.core.BibleRef
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.core.Year
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.person.Person
import io.github.alelk.pws.domain.song.command.UpdateSongCommand
import io.github.alelk.pws.domain.song.lyric.Lyric
import io.github.alelk.pws.domain.song.usecase.GetSongDetailUseCase
import io.github.alelk.pws.domain.song.usecase.UpdateSongUseCase
import io.github.alelk.pws.features.app.UiMessage
import io.github.alelk.pws.domain.songtag.usecase.GetSongTagIdsUseCase
import io.github.alelk.pws.domain.songtag.usecase.ReplaceAllSongTagsUseCase
import io.github.alelk.pws.domain.tag.usecase.ObserveTagsUseCase
import io.github.alelk.pws.domain.lyric.format.toText
import io.github.alelk.pws.domain.lyric.format.parseLyric
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
  private val observeTagsUseCase: ObserveTagsUseCase<TagId>,
  private val getSongTagIdsUseCase: GetSongTagIdsUseCase<TagId>,
  private val replaceAllSongTagsUseCase: ReplaceAllSongTagsUseCase<TagId>,
) : StateScreenModel<SongEditUiState>(SongEditUiState.Loading) {


  sealed interface Effect {
    data object NavigateBack : Effect
    data class ShowSnackbar(val message: SongEditSnackbarMessage) : Effect
  }

  private val _effects = MutableSharedFlow<Effect>()
  val effects = _effects.asSharedFlow()

  private val _showDiscardDialog = MutableStateFlow(false)
  val showDiscardDialog = _showDiscardDialog.asStateFlow()

  private var originalTitle = ""
  private var originalText = ""
  private var originalAuthor = ""
  private var originalComposer = ""
  private var originalTranslator = ""
  private var originalYear = ""
  private var originalBibleRef = ""
  private var originalTonalities = emptyList<io.github.alelk.pws.domain.tonality.Tonality>()
  private var originalTagIds = emptySet<TagId>()

  init {
    loadSong()
  }

  fun onEvent(event: SongEditEvent) {
    when (event) {
      is SongEditEvent.TitleChanged -> updateTitle(event.title)
      is SongEditEvent.NumberChanged -> { /* Number is read-only from SongDetail */ }
      is SongEditEvent.TextChanged -> updateText(event.text)
      is SongEditEvent.AuthorChanged -> updateAuthor(event.author)
      is SongEditEvent.ComposerChanged -> updateComposer(event.composer)
      is SongEditEvent.YearChanged -> updateYear(event.year)
      is SongEditEvent.TranslatorChanged -> updateTranslator(event.translator)
      is SongEditEvent.BibleRefChanged -> updateBibleRef(event.bibleRef)
      is SongEditEvent.TonalityToggled -> toggleTonality(event.tonality)
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
        val song = getSongDetailUseCase(songId).fold(
          ifLeft = {
            mutableState.value = SongEditUiState.Error(UiMessage.SongNotFound)
            return@launch
          },
          ifRight = { it }
        )

        val allTags = observeTagsUseCase().first()
        val songTagIds = getSongTagIdsUseCase(songId).fold(
          ifLeft = { emptySet() },
          ifRight = { it }
        )

        originalTitle = song.name.value
        originalText = song.lyric.toText(song.locale)
        originalAuthor = song.author?.name ?: ""
        originalComposer = song.composer?.name ?: ""
        originalTranslator = song.translator?.name ?: ""
        originalYear = song.year?.toString() ?: ""
        originalBibleRef = song.bibleRef?.toString() ?: ""
        originalTonalities = song.tonalities ?: emptyList()
        originalTagIds = songTagIds

        mutableState.value = SongEditUiState.Content(
          songId = songId,
          locale = song.locale,
          title = originalTitle,
          number = "", // not used for now
          text = originalText,
          author = originalAuthor,
          composer = originalComposer,
          translator = originalTranslator,
          year = originalYear,
          bibleRef = originalBibleRef,
          tonalities = originalTonalities,
          allTags = allTags.map { tag ->
            EditableTagUi(
              id = tag.id,
              name = tag.name,
              color = tag.color.toCompose(),
              isSelected = tag.id in songTagIds
            )
          }
        )
      } catch (e: Exception) {
        mutableState.value = SongEditUiState.Error(UiMessage.Failure(e.message))
      }
    }
  }

  private fun updateTitle(title: String) {
    updateContent { it.copy(title = title, hasUnsavedChanges = hasChanges(title = title)) }
  }

  private fun updateText(text: String) {
    updateContent { it.copy(text = text, hasUnsavedChanges = hasChanges(text = text)) }
  }

  private fun updateAuthor(author: String) {
    updateContent { it.copy(author = author, hasUnsavedChanges = hasChanges(author = author)) }
  }

  private fun updateComposer(composer: String) {
    updateContent { it.copy(composer = composer, hasUnsavedChanges = hasChanges(composer = composer)) }
  }

  private fun updateTranslator(translator: String) {
    updateContent { it.copy(translator = translator, hasUnsavedChanges = hasChanges(translator = translator)) }
  }

  private fun updateYear(year: String) {
    updateContent { it.copy(year = year, hasUnsavedChanges = hasChanges(year = year)) }
  }

  private fun updateBibleRef(bibleRef: String) {
    updateContent { it.copy(bibleRef = bibleRef, hasUnsavedChanges = hasChanges(bibleRef = bibleRef)) }
  }

  private fun toggleTonality(tonality: io.github.alelk.pws.domain.tonality.Tonality) {
    updateContent { content ->
      val updatedTonalities = if (tonality in content.tonalities) {
        content.tonalities - tonality
      } else {
        content.tonalities + tonality
      }
      content.copy(tonalities = updatedTonalities, hasUnsavedChanges = hasChanges(tonalities = updatedTonalities))
    }
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
    author: String? = null,
    composer: String? = null,
    translator: String? = null,
    year: String? = null,
    bibleRef: String? = null,
    tonalities: List<io.github.alelk.pws.domain.tonality.Tonality>? = null,
    tags: List<EditableTagUi>? = null
  ): Boolean {
    val currentState = mutableState.value as? SongEditUiState.Content ?: return false
    val t = title ?: currentState.title
    val tx = text ?: currentState.text
    val au = author ?: currentState.author
    val co = composer ?: currentState.composer
    val tr = translator ?: currentState.translator
    val yr = year ?: currentState.year
    val br = bibleRef ?: currentState.bibleRef
    val ton = tonalities ?: currentState.tonalities
    val currentTagIds = (tags ?: currentState.allTags).filter { it.isSelected }.map { it.id }.toSet()
    return t != originalTitle || tx != originalText || au != originalAuthor ||
      co != originalComposer || tr != originalTranslator || yr != originalYear || br != originalBibleRef ||
      ton != originalTonalities || currentTagIds != originalTagIds
  }

  private fun save() {
    val currentState = mutableState.value as? SongEditUiState.Content ?: return

    if (currentState.title.isBlank()) {
      updateContent { it.copy(validationMessage = SongEditValidationMessage.TitleRequired) }
      return
    }
    if (currentState.text.isBlank()) {
      updateContent { it.copy(validationMessage = SongEditValidationMessage.TextRequired) }
      return
    }

    screenModelScope.launch {
      updateContent { it.copy(isSaving = true, validationMessage = null) }
      try {
        val lyric = parseLyric(currentState.text, currentState.locale)

        val command = UpdateSongCommand(
          id = songId,
          name = NonEmptyString(currentState.title),
          lyric = lyric,
          author = if (currentState.author != originalAuthor) {
            OptionalField.Set(currentState.author.takeIf { it.isNotBlank() }?.let { Person(it) })
          } else OptionalField.Unchanged,
          composer = if (currentState.composer != originalComposer) {
            OptionalField.Set(currentState.composer.takeIf { it.isNotBlank() }?.let { Person(it) })
          } else OptionalField.Unchanged,
          translator = if (currentState.translator != originalTranslator) {
            OptionalField.Set(currentState.translator.takeIf { it.isNotBlank() }?.let { Person(it) })
          } else OptionalField.Unchanged,
          year = if (currentState.year != originalYear) {
            OptionalField.Set(currentState.year.takeIf { it.isNotBlank() }?.toIntOrNull()?.let { Year(it) })
          } else OptionalField.Unchanged,
          bibleRef = if (currentState.bibleRef != originalBibleRef) {
            OptionalField.Set(currentState.bibleRef.takeIf { it.isNotBlank() }?.let { BibleRef(it) })
          } else OptionalField.Unchanged,
          tonalities = if (currentState.tonalities != originalTonalities) {
            OptionalField.Set(currentState.tonalities.takeIf { it.isNotEmpty() })
          } else OptionalField.Unchanged,
        )
        val updateError = updateSongUseCase(command).fold(
          ifLeft = { it },
          ifRight = { null }
        )
        if (updateError != null) {
          updateContent {
            it.copy(
              isSaving = false,
              validationMessage = SongEditValidationMessage.SaveError(updateError.message)
            )
          }
          return@launch
        }

        val selectedTagIds = currentState.allTags.filter { it.isSelected }.map { it.id }.toSet()
        val replaceError = replaceAllSongTagsUseCase(songId, selectedTagIds).fold(
          ifLeft = { it },
          ifRight = { null }
        )
        if (replaceError != null) {
          updateContent {
            it.copy(
              isSaving = false,
              validationMessage = SongEditValidationMessage.SaveError(replaceError.message)
            )
          }
          return@launch
        }

        _effects.emit(Effect.ShowSnackbar(SongEditSnackbarMessage.Saved))
        _effects.emit(Effect.NavigateBack)
      } catch (e: Exception) {
        updateContent {
          val message = when (e) {
            is IllegalArgumentException -> SongEditValidationMessage.InvalidTextFormat(e.message)
            else -> SongEditValidationMessage.SaveError(e.message)
          }
          it.copy(
            isSaving = false,
            validationMessage = message
          )
        }
      }
    }
  }

  private fun handleCancel() {
    val currentState = mutableState.value as? SongEditUiState.Content ?: return
    if (currentState.hasUnsavedChanges) {
      _showDiscardDialog.value = true
    } else {
      screenModelScope.launch { _effects.emit(Effect.NavigateBack) }
    }
  }

  private fun discardAndNavigateBack() {
    _showDiscardDialog.value = false
    screenModelScope.launch { _effects.emit(Effect.NavigateBack) }
  }

  private inline fun updateContent(transform: (SongEditUiState.Content) -> SongEditUiState.Content) {
    val current = mutableState.value
    if (current is SongEditUiState.Content) {
      mutableState.value = transform(current)
    }
  }

  private fun DomainColor.toCompose() = Color(r / 255f, g / 255f, b / 255f)
}
