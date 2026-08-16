package io.github.alelk.pws.features.monetization

/**
 * How a given build monetizes — the single axis that a product flavor / shell picks once and
 * everything else derives from.
 *
 * The modes are mutually exclusive by design: a build either asks for donations or sells premium
 * (or neither), never both. This is a deliberate product decision — a paying user should not also
 * see donation prompts, and a free user has nothing to buy. If a future build ever needs both at
 * once, replace this enum with two independent booleans.
 *
 * This type is payment-agnostic: it says *what* the build monetizes, never *how* (which store SDK,
 * which entitlement source). The concrete wiring lives in the shell's flavor source set.
 */
enum class MonetizationMode {
  /** Free build: shows the donation prompt, no paywall. (Google Play `ru`/`uk`/`full`.) */
  Donations,

  /** Paid build: sells premium via a store SDK, no donation prompt. (`rustore`.) */
  PremiumSales,

  /** Neither donations nor sales — every premium gate is transparent and no prompts appear. */
  None;

  /** True when the donation prompt should be active for this build. */
  val donationsEnabled: Boolean get() = this == Donations

  /** True when this build sells premium (controls the paywall entry and paywall requests). */
  val premiumSalesEnabled: Boolean get() = this == PremiumSales
}
