package io.github.alelk.pws.features.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing system for consistent layout across the app.
 * Based on 4dp grid system.
 */
data class Spacing(
  /** Extra extra small spacing: 2dp */
  val xxs: Dp = 2.dp,
  /** Extra small spacing: 4dp */
  val xs: Dp = 4.dp,
  /** Small spacing: 8dp */
  val sm: Dp = 8.dp,
  /** Medium spacing: 12dp */
  val md: Dp = 12.dp,
  /** Large spacing: 16dp */
  val lg: Dp = 16.dp,
  /** Extra large spacing: 24dp */
  val xl: Dp = 24.dp,
  /** Extra extra large spacing: 32dp */
  val xxl: Dp = 32.dp,
  /** Section spacing: 48dp */
  val section: Dp = 48.dp,

  /** Card horizontal padding */
  val cardHorizontal: Dp = 16.dp,
  /** Card vertical padding */
  val cardVertical: Dp = 12.dp,
  /** Screen horizontal padding */
  val screenHorizontal: Dp = 16.dp,
  /** Screen vertical padding */
  val screenVertical: Dp = 16.dp,

  /** List item horizontal padding */
  val listItemHorizontal: Dp = 16.dp,
  /** List item vertical padding */
  val listItemVertical: Dp = 12.dp,

  /** Icon size - small */
  val iconSm: Dp = 20.dp,
  /** Icon size - medium */
  val iconMd: Dp = 24.dp,
  /** Icon size - large */
  val iconLg: Dp = 32.dp,
  /** Icon size - extra large */
  val iconXl: Dp = 48.dp,

  /** Minimum touch target size (accessibility) */
  val touchTarget: Dp = 48.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

/**
 * Access the spacing system via MaterialTheme.
 */
val androidx.compose.material3.MaterialTheme.spacing: Spacing
  @Composable
  @ReadOnlyComposable
  get() = LocalSpacing.current

