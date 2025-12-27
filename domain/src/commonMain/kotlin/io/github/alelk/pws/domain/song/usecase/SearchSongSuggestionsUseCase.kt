package io.github.alelk.pws.domain.song.usecase

import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.model.SongSearchSuggestion
import io.github.alelk.pws.domain.song.repository.SongSearchRepository

/**
 * Use case for getting song search suggestions.
 * Used for autocomplete functionality.
 */
class SearchSongSuggestionsUseCase(
  private val searchRepository: SongSearchRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(
    query: String,
    limit: Int = 10
  ): List<SongSearchSuggestion> = txRunner.inRoTransaction {
    searchRepository.searchSuggestions(query, limit)
  }
}

