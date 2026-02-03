package io.github.alelk.pws.domain.songtag.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository

/**
 * Use case: get all tag IDs for a song.
 * @param ID The type of TagId this use case works with
 */
class GetSongTagIdsUseCase<ID : TagId>(
  private val songTagRepository: SongTagReadRepository<ID>,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(songId: SongId): Set<ID> =
    txRunner.inRoTransaction { songTagRepository.getTagIdsBySongId(songId) }
}

