package io.github.alelk.pws.domain.song.usecase

import arrow.core.Either
import arrow.core.right
import io.github.alelk.pws.domain.core.error.ReadError
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.song.query.SongQuery
import io.github.alelk.pws.domain.song.query.SongSort
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.core.transaction.TransactionRunner

/**
 * Read use case: fetch multiple SongSummary items by query & sort.
 * Always executed inside a read-only transaction for snapshot consistency.
 */
class GetSongsUseCase(
  private val readRepository: SongReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(
    query: SongQuery = SongQuery.Empty,
    sort: SongSort = SongSort.ById
  ): Either<ReadError, List<SongSummary>> =
    txRunner.inRoTransaction { readRepository.getMany(query, sort).right() }
}

