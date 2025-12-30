package io.github.alelk.pws.domain.song.usecase

import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.model.SongSearchSuggestion
import io.github.alelk.pws.domain.song.repository.SongSearchRepository

/**
 * Use case for getting song search suggestions.
 * Used for autocomplete functionality.
 *
 * If userId is provided, searches both global songs and user's songbooks
 * with unified ranking. Otherwise, searches only global songs.
 */
class SearchSongSuggestionsUseCase(
  private val searchRepository: SongSearchRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(
    query: String,
    userId: UserId? = null,
    bookId: BookId? = null,
    limit: Int = 10
  ): List<SongSearchSuggestion> = txRunner.inRoTransaction {
    searchRepository.searchSuggestions(query, userId, bookId, limit)
  }
}

