package io.github.alelk.pws.domain.donationprompt.usecase

import io.github.alelk.pws.domain.donationprompt.config.DonationConfig
import io.github.alelk.pws.domain.history.repository.HistoryReadRepository

/**
 * Returns true if the user is considered "loyal" (≥ [DonationConfig.firstShowThreshold] history entries)
 * and the donation feature is enabled.
 *
 * Used to decide whether to surface the donation entry point in Settings.
 */
class IsLoyalUserUseCase(
  private val config: DonationConfig,
  private val historyReadRepository: HistoryReadRepository,
) {
  suspend operator fun invoke(): Boolean {
    if (!config.enabled) return false
    return historyReadRepository.count() >= config.firstShowThreshold
  }
}

