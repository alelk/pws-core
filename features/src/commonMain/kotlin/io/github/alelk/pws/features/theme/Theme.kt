package io.github.alelk.pws.features.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun AppTheme(
  themeMode: ThemeMode = ThemeMode.DEFAULT,
  content: @Composable () -> Unit
) {
  val colors = when (themeMode) {
    ThemeMode.LIGHT -> LightColors
    ThemeMode.DARK -> DarkColors
    ThemeMode.BLACK -> BlackColors
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
