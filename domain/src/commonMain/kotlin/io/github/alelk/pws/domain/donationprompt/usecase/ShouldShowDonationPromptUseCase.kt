package io.github.alelk.pws.domain.donationprompt.usecase

import io.github.alelk.pws.domain.donationprompt.config.DonationConfig
import io.github.alelk.pws.domain.donationprompt.repository.DonationPromptStateReadRepository
import io.github.alelk.pws.domain.history.repository.HistoryReadRepository

/**
 * Determines whether the donation prompt dialog should be shown right now.
 *
 * All of the following must be true:
 *  - [DonationConfig.enabled] is true
 *  - The session guard says the prompt has not yet been shown this process lifetime
 *  - Total history count ≥ [DonationConfig.firstShowThreshold]
 *  - The user has not previously clicked "Support on Boosty" ([DonationPromptState.hasClickedDonate])
 *  - Total history count > [DonationPromptState.suppressUntilViewCount] (suppress window passed)
 *
 * [sessionGuard] is an object (plain class) from the features layer — it is passed as a
 * constructor argument so the domain layer stays free of Koin or any DI framework.
 */
class ShouldShowDonationPromptUseCase(
  private val config: DonationConfig,
  private val historyReadRepository: HistoryReadRepository,
  private val donationStateReadRepository: DonationPromptStateReadRepository,
  private val sessionGuard: SessionGuard,
) {
  /**
   * Minimal interface for the session-level deduplication guard.
   * Implemented by [io.github.alelk.pws.features.di.DonationSessionGuard].
   */
  interface SessionGuard {
    val shownThisSession: Boolean
  }

  suspend operator fun invoke(): Boolean {
    if (!config.enabled) return false
    if (sessionGuard.shownThisSession) return false

    val totalViews = historyReadRepository.count()
    if (totalViews < config.firstShowThreshold) return false

    val state = donationStateReadRepository.get()
    if (state.hasClickedDonate) return false
    if (totalViews <= state.suppressUntilViewCount) return false

    return true
  }
}


