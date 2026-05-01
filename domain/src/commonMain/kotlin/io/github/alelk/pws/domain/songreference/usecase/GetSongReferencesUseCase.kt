package io.github.alelk.pws.domain.songreference.usecase

import arrow.core.Either
import arrow.core.right
import io.github.alelk.pws.domain.core.error.ReadError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songreference.model.SongReference
import io.github.alelk.pws.domain.songreference.repository.SongReferenceReadRepository

/**
 * Use case: get all references for a song.
 */
class GetSongReferencesUseCase(
  private val repository: SongReferenceReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(songId: SongId): Either<ReadError, List<SongReference>> =
    txRunner.inRoTransaction { repository.getReferencesForSong(songId).right() }
}

