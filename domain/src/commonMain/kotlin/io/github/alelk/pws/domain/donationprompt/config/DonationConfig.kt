package io.github.alelk.pws.domain.donationprompt.config

/**
 * Feature flag and configuration for the donation prompt.
 *
 * @property enabled Whether donation prompts are active for this build/platform.
 *   Set to false for web/Telegram builds where prompts are not needed.
 * @property boostyUrl URL to open when the user taps "Support on Boosty".
 * @property firstShowThreshold Minimum number of history entries before showing the first prompt.
 * @property shortSuppressViews How many more song views to suppress the prompt after "Later" is tapped.
 */
data class DonationConfig(
  val enabled: Boolean,
  val boostyUrl: String,
  val firstShowThreshold: Long = 10L,
  val shortSuppressViews: Long = 10L,
  /** How many times the card may be shown per session before it auto-suppresses (no persist). */
  val maxShowsPerSession: Int = 3,
)

