package io.github.alelk.pws.domain.song.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.alelk.pws.domain.core.error.ReadError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.repository.SongReadRepository

/**
 * Read use case: fetch a single SongDetail by id inside a read-only transaction.
 */
class GetSongDetailUseCase(
  private val readRepository: SongReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(id: SongId): Either<ReadError, SongDetail> =
    txRunner.inRoTransaction {
      readRepository.get(id)?.right() ?: ReadError.NotFound().left()
    }
}
