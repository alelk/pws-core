package io.github.alelk.pws.features.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun AppTheme(
  useDarkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colors = if (useDarkTheme) {
    DarkColors
  } else {
    LightColors
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
