package io.github.alelk.pws.domain.song.usecase

import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.model.SongSearchResponse
import io.github.alelk.pws.domain.song.query.SearchQuery
import io.github.alelk.pws.domain.song.repository.SongSearchRepository

/**
 * Use case for full-text search in user's book songs.
 */
class SearchUserBookSongsUseCase(
  private val searchRepository: SongSearchRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(
    userId: UserId,
    searchQuery: SearchQuery,
    bookId: BookId? = null
  ): SongSearchResponse = txRunner.inRoTransaction {
    searchRepository.searchInUserBooks(userId, searchQuery, bookId)
  }
}

