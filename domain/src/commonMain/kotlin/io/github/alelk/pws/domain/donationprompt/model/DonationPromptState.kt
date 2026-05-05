package io.github.alelk.pws.domain.donationprompt.model

/**
 * Persisted UX state for controlling when the donation prompt is shown.
 *
 * @property suppressUntilViewCount Prompt is suppressed while total history count <= this value.
 * @property hasClickedDonate True once the user tapped "Support on Boosty". Permanently suppresses the dialog.
 * @property dismissedCount How many times the user has tapped "Later".
 */
data class DonationPromptState(
  val suppressUntilViewCount: Long = 0L,
  val hasClickedDonate: Boolean = false,
  val dismissedCount: Int = 0,
)

