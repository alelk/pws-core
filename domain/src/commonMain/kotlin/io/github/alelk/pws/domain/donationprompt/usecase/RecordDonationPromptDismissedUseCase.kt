package io.github.alelk.pws.domain.donationprompt.usecase

import io.github.alelk.pws.domain.donationprompt.config.DonationConfig
import io.github.alelk.pws.domain.donationprompt.repository.DonationPromptStateReadRepository
import io.github.alelk.pws.domain.donationprompt.repository.DonationPromptStateWriteRepository
import io.github.alelk.pws.domain.history.repository.HistoryReadRepository

/**
 * Records that the user dismissed the donation prompt ("Later" button).
 *
 * Suppresses the prompt for the next [DonationConfig.shortSuppressViews] song views
 * and increments the dismiss counter.
 */
class RecordDonationPromptDismissedUseCase(
  private val config: DonationConfig,
  private val historyReadRepository: HistoryReadRepository,
  private val donationStateReadRepository: DonationPromptStateReadRepository,
  private val donationStateWriteRepository: DonationPromptStateWriteRepository,
) {
  suspend operator fun invoke() {
    val totalViews = historyReadRepository.count()
    val current = donationStateReadRepository.get()
    donationStateWriteRepository.save(
      current.copy(
        suppressUntilViewCount = totalViews + config.shortSuppressViews,
        dismissedCount = current.dismissedCount + 1,
      )
    )
  }
}

