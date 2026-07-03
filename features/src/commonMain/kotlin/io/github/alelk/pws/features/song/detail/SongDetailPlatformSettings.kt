package io.github.alelk.pws.features.song.detail

import androidx.compose.runtime.staticCompositionLocalOf

/** Host-provided actions that require platform APIs. */
data class SongDetailExternalActions(
  val shareText: (String) -> Unit,
)

/** Host-provided persisted display settings for song detail screen. */
data class SongDetailDisplaySettings(
  val fontScale: Float,
  val expandedText: Boolean,
  val onFontScaleChange: (Float) -> Unit,
  val onExpandedTextChange: (Boolean) -> Unit,
  /** Line height multiplier (0.9 .. 1.4 .. 1.8). 1.0 is the standard. */
  val lineHeightMultiplier: Float = 1.0f,
  val onLineHeightMultiplierChange: (Float) -> Unit = {},
  /** Literary serif ("Serif font" option) for song text; the default is the grotesque. */
  val serifFont: Boolean = false,
  val onSerifFontChange: (Boolean) -> Unit = {},
  /** Prev/next buttons in the header (can be toggled; swipe stays on touch platforms). */
  val showNavigationButtons: Boolean = PlatformDefaultShowSongNavButtons,
  val onShowNavigationButtonsChange: (Boolean) -> Unit = {},
)

/** Host-provided persisted display settings for favorites screen. */
data class FavoritesDisplaySettings(
  val sortMode: String,
  val ascending: Boolean,
  val onSortModeChange: (String) -> Unit,
  val onAscendingChange: (Boolean) -> Unit,
)

val LocalSongDetailExternalActions = staticCompositionLocalOf<SongDetailExternalActions?> { null }
val LocalSongDetailDisplaySettings = staticCompositionLocalOf<SongDetailDisplaySettings?> { null }
val LocalFavoritesDisplaySettings = staticCompositionLocalOf<FavoritesDisplaySettings?> { null }
