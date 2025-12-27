package io.github.alelk.pws.domain.song.usecase

import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.model.SongSearchResponse
import io.github.alelk.pws.domain.song.query.SearchQuery
import io.github.alelk.pws.domain.song.repository.SongSearchRepository

/**
 * Use case for full-text search on songs.
 */
class SearchSongsUseCase(
  private val searchRepository: SongSearchRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(
    searchQuery: SearchQuery
  ): SongSearchResponse = txRunner.inRoTransaction {
    searchRepository.search(searchQuery)
  }
}

