package io.github.alelk.pws.features.telemetry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import io.github.alelk.pws.domain.telemetry.Telemetry
import io.github.alelk.pws.domain.telemetry.TelemetryAttr
import io.github.alelk.pws.domain.telemetry.TelemetryEvent
import org.koin.compose.koinInject

/** Injects the bound [Telemetry] for use inside composables. */
@Composable
fun rememberTelemetry(): Telemetry = koinInject()

/**
 * Reports a `screen_view` whenever [navigator]'s top screen changes.
 *
 * Attached once per [Navigator] — one per bottom-nav tab, plus onboarding — this covers every
 * pushed screen without touching individual `Screen` implementations, which is the single
 * instrumentation point the monitoring plan calls for.
 *
 * The reported name is the screen's class name, so the shell must keep those names in release
 * builds (see the Voyager `-keepnames` rule in `app-compose/proguard-rules.pro`).
 */
@Composable
fun TrackScreenViews(navigator: Navigator, telemetry: Telemetry = rememberTelemetry()) {
  val current: Screen? = navigator.lastItemOrNull
  LaunchedEffect(current) {
    val name = current?.let { it::class.simpleName ?: it.key } ?: return@LaunchedEffect
    telemetry.event(TelemetryEvent.SCREEN_VIEW, mapOf(TelemetryAttr.SCREEN to name))
  }
}

/**
 * User-facing telemetry controls, supplied by the shell (consent lives in platform preferences).
 * `null` means the build has no telemetry provider at all — the settings screen then hides the
 * privacy section instead of showing a dead toggle.
 */
data class TelemetrySettings(
  /** Whether crash reports and anonymous statistics may be sent. */
  val dataSendingEnabled: Boolean,
  /** Persists the new consent value and applies it to the provider immediately. */
  val onDataSendingEnabledChange: (Boolean) -> Unit,
  /** Public URL of the privacy policy, shown next to the toggle. */
  val privacyPolicyUrl: String,
)

val LocalTelemetrySettings = staticCompositionLocalOf<TelemetrySettings?> { null }
