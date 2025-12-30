package io.github.alelk.pws.domain.song.usecase

import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.model.SongSearchResponse
import io.github.alelk.pws.domain.song.query.SearchQuery
import io.github.alelk.pws.domain.song.query.SearchScope
import io.github.alelk.pws.domain.song.repository.SongSearchRepository

/**
 * Use case for full-text search on songs.
 *
 * Search behavior depends on [SearchQuery.scope] and userId:
 * - scope=ALL + userId: searches both global and user's songs with unified ranking
 * - scope=ALL + no userId: searches only global songs
 * - scope=GLOBAL: searches only global songs catalog
 * - scope=USER_BOOKS + userId: searches only user's songbooks
 * - scope=USER_BOOKS + no userId: returns empty results
 */
class SearchSongsUseCase(
  private val searchRepository: SongSearchRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(
    searchQuery: SearchQuery,
    userId: UserId? = null,
    bookId: BookId? = null
  ): SongSearchResponse = txRunner.inRoTransaction {
    // If scope is USER_BOOKS but no userId, return empty result
    if (searchQuery.scope == SearchScope.USER_BOOKS && userId == null) {
      return@inRoTransaction SongSearchResponse(emptyList(), 0, false)
    }
    searchRepository.search(searchQuery, userId, bookId)
  }
}

