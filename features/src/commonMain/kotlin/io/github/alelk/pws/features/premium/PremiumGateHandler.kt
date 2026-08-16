package io.github.alelk.pws.features.premium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * UI-side helper for gating click handlers behind [PremiumGate].
 *
 * Usage:
 * ```
 * val premium = rememberPremiumGate()
 * IconButton(onClick = premium.gated { navigator.push(SongEditScreen(id)) }) { ... }
 * ```
 * With the free builds' always-active entitlement the gated lambda runs immediately; with a store
 * flavor it runs only when premium is active, otherwise a paywall request is emitted for the shell.
 */
class PremiumActionRunner(
  private val gate: PremiumGate,
  private val scope: CoroutineScope,
) {
  /** Runs [action] on the composition scope (main dispatcher) if premium is active, else requests the paywall. */
  fun run(action: () -> Unit) {
    scope.launch { gate.requirePremium { action() } }
  }

  /** Convenience for building a gated `onClick` lambda. */
  fun gated(action: () -> Unit): () -> Unit = { run(action) }
}

/** Remembers a [PremiumActionRunner] bound to the injected [PremiumGate] and the composition scope. */
@Composable
fun rememberPremiumGate(): PremiumActionRunner {
  val gate = koinInject<PremiumGate>()
  val scope = rememberCoroutineScope()
  return remember(gate, scope) { PremiumActionRunner(gate, scope) }
}
