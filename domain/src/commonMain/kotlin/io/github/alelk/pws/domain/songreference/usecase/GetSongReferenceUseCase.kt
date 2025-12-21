package io.github.alelk.pws.domain.songreference.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songreference.model.SongReference
import io.github.alelk.pws.domain.songreference.repository.SongReferenceReadRepository

/**
 * Use case: get a specific song reference.
 */
class GetSongReferenceUseCase(
  private val repository: SongReferenceReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(songId: SongId, refSongId: SongId): SongReference? =
    txRunner.inRoTransaction { repository.get(songId, refSongId) }
}

