package io.github.alelk.pws.features.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Motion tokens — shared spring parameters for every animation in the app.
 *
 * Principle: spring(damping, stiffness), not tween(duration).
 * Springs give the "iOS feel" — natural physics instead of linear interpolation.
 *
 * Usage:
 * ```
 * val scale by animateFloatAsState(targetValue = ..., animationSpec = Motion.fast())
 * ```
 *
 * Generic helpers (`fast<Float>()`, `standard<Color>()`) exist because AnimateAsState
 * needs a different T per value type.
 */
object Motion {
  /** Tap / chip / ripple — sharp, almost instant response. */
  fun <T> fast(): SpringSpec<T> = spring(dampingRatio = 0.85f, stiffness = 600f)

  /** State transitions, color, alpha — the standard springiness. */
  fun <T> standard(): SpringSpec<T> = spring(dampingRatio = 0.75f, stiffness = 380f)

  /** Sheets, FABs, prominent entrances — a noticeable bounce. */
  fun <T> emphasized(): SpringSpec<T> = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 300f)

  /** Shimmer cycle period (loading skeletons). */
  const val shimmerCycleMs: Int = 1400
}
