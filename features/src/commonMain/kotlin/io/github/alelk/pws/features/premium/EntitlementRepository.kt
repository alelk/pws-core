package io.github.alelk.pws.features.premium

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Premium entitlement state, resolved from whatever source a given build wires in.
 *
 * This type is intentionally payment-agnostic: it says *whether* premium features are
 * unlocked, never *how* they were paid for. Store-specific integrations live outside this
 * module (in the app shell / product flavors) and feed their result in through
 * [EntitlementRepository].
 */
enum class PremiumStatus {
  /** Premium features are unlocked. */
  Active,

  /** Premium features are locked (no purchase / expired subscription). */
  Inactive,

  /** Not yet resolved (e.g. first read in progress). Callers should wait for a definite value. */
  Unknown,
}

/**
 * Single source of truth for the current [PremiumStatus].
 *
 * The default binding ([AlwaysActiveEntitlementRepository]) reports [PremiumStatus.Active], which
 * makes every premium gate transparent — the behaviour of the free Google Play builds. A store
 * flavor overrides this binding with an implementation backed by its own purchase state.
 */
interface EntitlementRepository {
  val status: StateFlow<PremiumStatus>
}

/**
 * Default entitlement source: everything is unlocked. Used by the free builds where there is no
 * paywall at all, so premium gates never block.
 */
class AlwaysActiveEntitlementRepository : EntitlementRepository {
  override val status: StateFlow<PremiumStatus> =
    MutableStateFlow(PremiumStatus.Active).asStateFlow()
}
