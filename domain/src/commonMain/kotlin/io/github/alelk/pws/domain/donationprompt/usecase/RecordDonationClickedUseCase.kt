package io.github.alelk.pws.domain.donationprompt.usecase

import io.github.alelk.pws.domain.donationprompt.repository.DonationPromptStateReadRepository
import io.github.alelk.pws.domain.donationprompt.repository.DonationPromptStateWriteRepository

/**
 * Records that the user tapped "Support on Boosty".
 *
 * Sets [DonationPromptState.hasClickedDonate] to true and suppresses the prompt permanently
 * by setting [DonationPromptState.suppressUntilViewCount] to [Long.MAX_VALUE].
 * The dialog will never appear again after this call.
 */
class RecordDonationClickedUseCase(
  private val donationStateReadRepository: DonationPromptStateReadRepository,
  private val donationStateWriteRepository: DonationPromptStateWriteRepository,
) {
  suspend operator fun invoke() {
    val current = donationStateReadRepository.get()
    donationStateWriteRepository.save(
      current.copy(
        hasClickedDonate = true,
        suppressUntilViewCount = Long.MAX_VALUE,
      )
    )
  }
}

