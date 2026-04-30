package io.github.alelk.pws.features.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun AppTheme(
  themeMode: ThemeMode = ThemeMode.DEFAULT,
  content: @Composable () -> Unit
) {
  val isDark = when (themeMode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.BLACK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
  }

  val colors = when {
    themeMode == ThemeMode.BLACK -> BlackColors
    isDark -> DarkColors
    else -> LightColors
  }

  CompositionLocalProvider(LocalSpacing provides Spacing()) {
    MaterialTheme(
      colorScheme = colors,
      typography = AppTypography,
      shapes = AppShapes,
      content = content
    )
  }
}
