package io.github.alelk.pws.domain.songreference.usecase

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
  suspend operator fun invoke(songId: SongId): List<SongReference> =
    txRunner.inRoTransaction { repository.getReferencesForSong(songId) }
}

