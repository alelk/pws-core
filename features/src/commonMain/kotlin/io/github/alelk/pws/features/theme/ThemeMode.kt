package io.github.alelk.pws.features.theme

import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeMode(val identifier: String) {
  LIGHT("light"),
  DARK("dark"),
  BLACK("black"),
  SYSTEM("system");

  companion object {
    val DEFAULT: ThemeMode = SYSTEM

    fun byIdentifier(value: String?): ThemeMode = entries.firstOrNull { it.identifier == value } ?: DEFAULT
  }
}

data class ThemeSettings(
  val themeMode: ThemeMode,
  val onThemeModeChange: (ThemeMode) -> Unit,
)

val LocalThemeSettings = staticCompositionLocalOf<ThemeSettings?> { null }
