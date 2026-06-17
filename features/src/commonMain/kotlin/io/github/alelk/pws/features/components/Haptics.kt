package io.github.alelk.pws.features.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Haptic feedback policy for the app.
 *
 * Light — short taps on chips, buttons, list rows. The lightest available type.
 * Confirm — destructive confirmations, irreversible actions. Stronger so the user
 *           feels they "committed" to something.
 *
 * Reserve [HapticFeedbackType.TextHandleMove] for true long-press gestures only.
 */
@Composable
@ReadOnlyComposable
fun rememberLightTapHaptic(): () -> Unit {
  val haptic = LocalHapticFeedback.current
  return { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
}

fun HapticFeedback.lightTap() = performHapticFeedback(HapticFeedbackType.TextHandleMove)
fun HapticFeedback.confirm() = performHapticFeedback(HapticFeedbackType.TextHandleMove)
