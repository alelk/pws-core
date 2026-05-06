package io.github.alelk.pws.features.di

import io.github.alelk.pws.domain.donationprompt.usecase.ShouldShowDonationPromptUseCase

/**
 * In-memory session guard for the donation prompt.
 *
 * Registered as a Koin [single] so all screen models share the same instance
 * within one app process. Ensures the prompt is shown at most [DonationConfig.maxShowsPerSession]
 * times per session (or until the user presses ×).
 *
 * Implements [ShouldShowDonationPromptUseCase.SessionGuard] so it can be passed
 * into the domain use case without introducing a framework dependency there.
 */
class DonationSessionGuard : ShouldShowDonationPromptUseCase.SessionGuard {
  /** True once the user pressed × or the per-session show limit was reached. */
  override var shownThisSession: Boolean = false

  /** How many times the donation card has been shown this session (without any interaction). */
  var seenCountThisSession: Int = 0
}

