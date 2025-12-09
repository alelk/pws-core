package io.github.alelk.pws.features.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Custom shapes for the app following Material 3 guidelines.
 * Slightly softer corners for a modern, friendly appearance.
 */
val AppShapes = Shapes(
  // Used for small components like chips, buttons
  extraSmall = RoundedCornerShape(8.dp),
  // Used for text fields, small cards
  small = RoundedCornerShape(12.dp),
  // Used for cards, dialogs
  medium = RoundedCornerShape(16.dp),
  // Used for bottom sheets, large cards
  large = RoundedCornerShape(20.dp),
  // Used for full-screen dialogs
  extraLarge = RoundedCornerShape(28.dp),
)

