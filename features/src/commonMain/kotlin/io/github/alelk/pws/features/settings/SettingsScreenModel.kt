package io.github.alelk.pws.features.settings

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.book.usecase.ObserveBooksUseCase
import io.github.alelk.pws.domain.bookstatistic.command.UpdateBookStatisticCommand
import io.github.alelk.pws.domain.bookstatistic.usecase.UpdateBookStatisticUseCase
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.app_name
import io.github.alelk.pws.features.resources.settings_developer_alex_name
import io.github.alelk.pws.features.resources.settings_developer_alex_role
import io.github.alelk.pws.features.resources.settings_developer_vera_name
import io.github.alelk.pws.features.resources.settings_developer_vera_role
import io.github.alelk.pws.features.resources.settings_open
import io.github.alelk.pws.features.resources.theme_black
import io.github.alelk.pws.features.resources.theme_dark
import io.github.alelk.pws.features.resources.theme_light
import io.github.alelk.pws.features.resources.theme_system
import io.github.alelk.pws.features.app.PwsAppInfo
import io.github.alelk.pws.features.theme.ThemeMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SettingsScreenModel(
  observeBooksUseCase: ObserveBooksUseCase,
  private val updateBookStatisticUseCase: UpdateBookStatisticUseCase,
  private val appInfo: PwsAppInfo?,
) : StateScreenModel<SettingsUiState>(SettingsUiState.Loading) {

  companion object {
    private const val DEFAULT_ENABLED_PRIORITY = 1
  }

  sealed interface Effect {
    data class ApplyTheme(val themeMode: ThemeMode) : Effect
    data class OpenUrl(val url: String) : Effect
    data class SendEmail(val mailto: String) : Effect
    data object ExportData : Effect
    data object ImportData : Effect
    data object NavigateBack : Effect
  }

  private val _effects = MutableSharedFlow<Effect>()
  val effects = _effects.asSharedFlow()

  private val updateMutex = Mutex()
  private val lastKnownPriorities = mutableMapOf<BookId, Int>()

  private val themeItems = listOf(
    ThemeItemUi(ThemeMode.SYSTEM, Res.string.theme_system),
    ThemeItemUi(ThemeMode.LIGHT, Res.string.theme_light),
    ThemeItemUi(ThemeMode.DARK, Res.string.theme_dark),
    ThemeItemUi(ThemeMode.BLACK, Res.string.theme_black),
  )

  private val developerItems = listOf(
    DeveloperItemUi(
      nameRes = Res.string.app_name,
      roleRes = Res.string.settings_open,
      contact = DeveloperContact.Web(url = "https://alelk.github.io")
    ),
    DeveloperItemUi(
      nameRes = Res.string.settings_developer_alex_name,
      roleRes = Res.string.settings_developer_alex_role,
      contact = DeveloperContact.Web(url = "https://alelk.github.io")
    ),
    DeveloperItemUi(
      nameRes = Res.string.settings_developer_vera_name,
      roleRes = Res.string.settings_developer_vera_role,
      contact = DeveloperContact.Email(mailto = "mailto:alelkdev@gmail.com?subject=PW%20Songs%20-%20Suggest%20content%20edit")
    ),
  )

  init {
    mutableState.value = SettingsUiState.Content(
      selectedTheme = ThemeMode.DEFAULT,
      themes = themeItems,
      developers = developerItems,
      books = emptyList(),
      appVersion = appInfo?.version ?: ""
    )

    screenModelScope.launch {
      observeBooksUseCase().collect { books ->
        books.forEach { book -> lastKnownPriorities[book.id] = book.priority }
        updateContent {
          copy(books = books.toRows())
        }
      }
    }
  }

  fun onEvent(event: SettingsEvent) {
    when (event) {
      is SettingsEvent.SyncTheme -> {
        updateContent { copy(selectedTheme = event.themeMode) }
      }

      is SettingsEvent.SetTheme -> {
        updateContent { copy(selectedTheme = event.themeMode) }
        screenModelScope.launch {
          _effects.emit(Effect.ApplyTheme(event.themeMode))
        }
      }

      is SettingsEvent.ToggleBook -> setBookEnabled(event.id, event.enabled)

      is SettingsEvent.OpenDeveloperContact -> {
        screenModelScope.launch {
          when (val contact = event.contact) {
            is DeveloperContact.Email -> _effects.emit(Effect.SendEmail(contact.mailto))
            is DeveloperContact.Web -> _effects.emit(Effect.OpenUrl(contact.url))
          }
        }
      }

      SettingsEvent.ExportData -> screenModelScope.launch { _effects.emit(Effect.ExportData) }
      SettingsEvent.ImportData -> screenModelScope.launch { _effects.emit(Effect.ImportData) }
      SettingsEvent.Back -> screenModelScope.launch { _effects.emit(Effect.NavigateBack) }
    }
  }

  private fun setBookEnabled(id: BookId, enabled: Boolean) {
    screenModelScope.launch {
      updateMutex.withLock {
        updateContent {
          copy(books = books.map { if (it.id == id) it.copy(enabled = enabled) else it })
        }

        runCatching {
          val priority = if (enabled) lastKnownPriorities[id]?.takeIf { it > 0 } ?: DEFAULT_ENABLED_PRIORITY else 0
          updateBookStatisticUseCase(UpdateBookStatisticCommand(id = id, priority = priority))
        }.onFailure {
          updateContent {
            copy(books = books.map { if (it.id == id) it.copy(enabled = !enabled) else it })
          }
        }
      }
    }
  }

  private fun List<BookSummary>.toRows(): List<BookToggleUi> =
    map { book ->
      BookToggleUi(
        id = book.id,
        title = book.displayName.value,
        enabled = book.enabled,
      )
    }

  private fun updateContent(transform: SettingsUiState.Content.() -> SettingsUiState.Content) {
    val current = mutableState.value
    mutableState.value = if (current is SettingsUiState.Content) {
      transform(current)
    } else {
      SettingsUiState.Content(
        selectedTheme = ThemeMode.DEFAULT,
        themes = themeItems,
        developers = developerItems,
        books = emptyList(),
        appVersion = appInfo?.version ?: ""
      )
    }
  }
}



