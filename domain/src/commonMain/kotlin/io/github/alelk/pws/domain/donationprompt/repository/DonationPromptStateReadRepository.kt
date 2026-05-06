package io.github.alelk.pws.domain.donationprompt.repository

import io.github.alelk.pws.domain.donationprompt.model.DonationPromptState

/**
 * Read access to the persisted donation prompt state.
 * Returns a default [DonationPromptState] when no state has been saved yet.
 */
interface DonationPromptStateReadRepository {
  suspend fun get(): DonationPromptState
}

