package io.github.alelk.pws.features.settings

import androidx.compose.runtime.staticCompositionLocalOf

data class SettingsExternalActions(
  val openUrl: (String) -> Unit,
  val sendEmail: (String) -> Unit,
  val exportBackup: () -> Unit,
  val importBackup: () -> Unit,
)

val LocalSettingsExternalActions = staticCompositionLocalOf<SettingsExternalActions?> { null }

