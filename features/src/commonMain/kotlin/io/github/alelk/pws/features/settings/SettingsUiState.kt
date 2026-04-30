package io.github.alelk.pws.features.settings

import androidx.compose.runtime.Immutable
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.features.theme.ThemeMode
import org.jetbrains.compose.resources.StringResource

sealed interface SettingsUiState {
  data object Loading : SettingsUiState

  @Immutable
  data class Content(
    val selectedTheme: ThemeMode,
    val themes: List<ThemeItemUi>,
    val developers: List<DeveloperItemUi>,
    val books: List<BookToggleUi>,
    val appVersion: String = ""
  ) : SettingsUiState
}

@Immutable
data class ThemeItemUi(
  val themeMode: ThemeMode,
  val titleRes: StringResource
)

@Immutable
data class DeveloperItemUi(
  val nameRes: StringResource,
  val roleRes: StringResource,
  val contact: DeveloperContact
)

sealed interface DeveloperContact {
  data class Web(val url: String) : DeveloperContact
  data class Email(val mailto: String) : DeveloperContact
}

@Immutable
data class BookToggleUi(
  val id: BookId,
  val title: String,
  val enabled: Boolean
)

sealed interface SettingsEvent {
  data class SyncTheme(val themeMode: ThemeMode) : SettingsEvent
  data class SetTheme(val themeMode: ThemeMode) : SettingsEvent
  data class ToggleBook(val id: BookId, val enabled: Boolean) : SettingsEvent
  data class OpenDeveloperContact(val contact: DeveloperContact) : SettingsEvent
  data object ExportData : SettingsEvent
  data object ImportData : SettingsEvent
  data object Back : SettingsEvent
}


