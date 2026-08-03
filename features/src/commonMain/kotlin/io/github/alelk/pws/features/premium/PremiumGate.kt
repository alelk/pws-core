package io.github.alelk.pws.features.premium

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first

/**
 * Guards premium-only actions. Callers wrap a gated action in [requirePremium]; if entitlement is
 * active the action runs, otherwise the action is skipped and a paywall request is emitted on
 * [paywallRequests] for the app shell to react to (show the store-specific paywall).
 *
 * The gate is payment-agnostic — it only knows [PremiumStatus] via [EntitlementRepository]. In the
 * free builds the repository is [AlwaysActiveEntitlementRepository], so [requirePremium] always runs
 * the action and [paywallRequests] never emits.
 */
interface PremiumGate {
  /** Emits once whenever a gated action was blocked because premium is not active. */
  val paywallRequests: SharedFlow<Unit>

  /**
   * Runs [action] and returns its result if premium is active; otherwise emits a paywall request
   * and returns `null`. Suspends until the entitlement status is definite (not [PremiumStatus.Unknown]).
   */
  suspend fun <T> requirePremium(action: suspend () -> T): T?
}

/**
 * Default [PremiumGate] backed by an [EntitlementRepository]. Works for every build: with the free
 * builds' always-active repository it is fully transparent; with a store flavor's repository it
 * blocks and requests the paywall when premium is inactive.
 */
class DefaultPremiumGate(
  private val entitlements: EntitlementRepository,
) : PremiumGate {

  private val _paywallRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  override val paywallRequests: SharedFlow<Unit> = _paywallRequests.asSharedFlow()

  override suspend fun <T> requirePremium(action: suspend () -> T): T? {
    // Wait for a definite status so we don't wrongly block on the initial Unknown value.
    val resolved = entitlements.status.first { it != PremiumStatus.Unknown }
    return if (resolved == PremiumStatus.Active) {
      action()
    } else {
      _paywallRequests.emit(Unit)
      null
    }
  }
}
