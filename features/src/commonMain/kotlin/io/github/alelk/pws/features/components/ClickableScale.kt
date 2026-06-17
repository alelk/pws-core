package io.github.alelk.pws.features.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Composable factory replacement for the legacy `composed { }` modifier.
 *
 * The previous implementation used `Modifier.composed { }`, which is on the
 * deprecation slope in Compose 1.6+. The recommended replacement is a Composable
 * function returning a modifier — same call-site ergonomics, no `composed` overhead.
 */
@Composable
fun Modifier.clickableWithScale(
  onClick: () -> Unit,
  interactionSource: MutableInteractionSource? = null,
  enabled: Boolean = true,
  indication: Indication? = null,
): Modifier {
  val source = interactionSource ?: remember { MutableInteractionSource() }
  val isPressed by source.collectIsPressedAsState()
  val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f)
  val resolvedIndication = indication ?: LocalIndication.current

  return this
    .graphicsLayer {
      scaleX = scale
      scaleY = scale
    }
    .clickable(
      interactionSource = source,
      indication = resolvedIndication,
      enabled = enabled,
      onClick = onClick,
    )
}

@Composable
fun Modifier.clickableWithScaleAndClip(
  shape: Shape,
  onClick: () -> Unit,
  interactionSource: MutableInteractionSource? = null,
  enabled: Boolean = true,
  indication: Indication? = null,
): Modifier = this
  .clip(shape)
  .clickableWithScale(
    onClick = onClick,
    interactionSource = interactionSource,
    enabled = enabled,
    indication = indication,
  )
