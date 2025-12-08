package io.github.alelk.pws.domain.search.usecase

import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.search.model.SearchResult
import io.github.alelk.pws.domain.search.repository.SearchRepository

/**
 * Use case: search songs by query.
 */
class SearchSongsUseCase(
  private val searchRepository: SearchRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(query: String, limit: Int = 50): List<SearchResult> =
    txRunner.inRoTransaction { searchRepository.search(query, limit) }
}

