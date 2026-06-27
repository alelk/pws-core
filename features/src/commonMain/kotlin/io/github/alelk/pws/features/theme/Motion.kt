package io.github.alelk.pws.features.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Motion tokens — единые spring-параметры для всех анимаций приложения.
 *
 * Принцип: spring(damping, stiffness), а не tween(duration).
 * Spring даёт «iOS-feel» — естественный отскок, физика, а не линейная интерполяция.
 *
 * Использование:
 * ```
 * val scale by animateFloatAsState(targetValue = ..., animationSpec = Motion.fast())
 * ```
 *
 * Generic helper'ы (`fast<Float>()`, `standard<Color>()`) — потому что AnimateAsState
 * требует разные T для разных типов значений.
 */
object Motion {
  /** Tap / chip / ripple — резкая, почти мгновенная реакция. */
  fun <T> fast(): SpringSpec<T> = spring(dampingRatio = 0.85f, stiffness = 600f)

  /** State transitions, color, alpha — стандартная «упругость». */
  fun <T> standard(): SpringSpec<T> = spring(dampingRatio = 0.75f, stiffness = 380f)

  /** Sheet, FAB, важные появления — заметный bounce. */
  fun <T> emphasized(): SpringSpec<T> = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 300f)

  /** Период шиммер-цикла (loading skeletons). */
  const val shimmerCycleMs: Int = 1400
}
