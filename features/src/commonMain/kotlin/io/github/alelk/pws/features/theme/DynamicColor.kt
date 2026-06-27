package io.github.alelk.pws.features.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Material You — platform-specific dynamic colour scheme.
 *
 * Android 12+ (SDK 31): wraps `dynamicLightColorScheme(context)` / `dynamicDarkColorScheme(context)`.
 * Other platforms / older Android: returns `null`.
 *
 * Used by [AppTheme] when `useDynamicColor = true` in [ThemeSettings].
 */
@Composable
expect fun platformDynamicColorSchemeOrNull(dark: Boolean): ColorScheme?
