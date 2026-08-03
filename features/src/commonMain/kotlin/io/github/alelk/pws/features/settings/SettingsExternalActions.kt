package io.github.alelk.pws.features.settings

import androidx.compose.runtime.staticCompositionLocalOf

data class SettingsExternalActions(
  val openUrl: (String) -> Unit,
  val sendEmail: (String) -> Unit,
  val exportBackup: () -> Unit,
  val importBackup: () -> Unit,
  /**
   * Opens the store paywall. Non-null only in builds that sell premium features (a store flavor);
   * when null the Purchases settings entry is hidden — the free builds have no paywall.
   */
  val openPaywall: (() -> Unit)? = null,
)

val LocalSettingsExternalActions = staticCompositionLocalOf<SettingsExternalActions?> { null }

