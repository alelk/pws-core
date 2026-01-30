package io.github.alelk.pws.features.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

fun Modifier.clickableWithScale(
  onClick: () -> Unit,
  interactionSource: MutableInteractionSource? = null,
  enabled: Boolean = true,
  indication: Indication? = null
): Modifier = composed {
  val src = interactionSource ?: remember { MutableInteractionSource() }
  val isPressed by src.collectIsPressedAsState()
  val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f)
  val resolvedIndication = indication ?: LocalIndication.current

  this
    .graphicsLayer {
      scaleX = scale
      scaleY = scale
    }
    .clickable(
      interactionSource = src,
      indication = resolvedIndication,
      enabled = enabled,
      onClick = onClick
    )
}

fun Modifier.clickableWithScaleAndClip(
  shape: Shape,
  onClick: () -> Unit,
  interactionSource: MutableInteractionSource? = null,
  enabled: Boolean = true,
  indication: Indication? = null
): Modifier = composed {
  this
    .clip(shape)
    .clickableWithScale(
      onClick = onClick,
      interactionSource = interactionSource,
      enabled = enabled,
      indication = indication
    )
}
