package io.github.alelk.pws.domain.donationprompt.repository

import io.github.alelk.pws.domain.donationprompt.model.DonationPromptState

/**
 * Write access to the persisted donation prompt state.
 */
interface DonationPromptStateWriteRepository {
  suspend fun save(state: DonationPromptState)
}

