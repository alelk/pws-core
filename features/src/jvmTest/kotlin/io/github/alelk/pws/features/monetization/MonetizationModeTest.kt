package io.github.alelk.pws.features.monetization

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MonetizationModeTest : FunSpec({

  test("Donations mode enables the prompt and sells nothing") {
    MonetizationMode.Donations.donationsEnabled shouldBe true
    MonetizationMode.Donations.premiumSalesEnabled shouldBe false
  }

  test("PremiumSales mode sells premium and suppresses the prompt") {
    MonetizationMode.PremiumSales.donationsEnabled shouldBe false
    MonetizationMode.PremiumSales.premiumSalesEnabled shouldBe true
  }

  test("None mode enables neither") {
    MonetizationMode.None.donationsEnabled shouldBe false
    MonetizationMode.None.premiumSalesEnabled shouldBe false
  }

  // The core product invariant this enum exists to guarantee: no build ever shows a donation
  // prompt to a user who can (or did) pay for premium. Guards against a future mode being added
  // that flips both flags on.
  test("no mode enables donations and premium sales at the same time") {
    MonetizationMode.entries.forEach { mode ->
      (mode.donationsEnabled && mode.premiumSalesEnabled) shouldBe false
    }
  }
})
