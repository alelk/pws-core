package io.github.alelk.pws.domain.songtag.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository

/**
 * Use case: get all song IDs for a tag.
 * @param ID The type of TagId this use case works with
 */
class GetTagSongIdsUseCase<ID : TagId>(
  private val songTagRepository: SongTagReadRepository<ID>,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(tagId: ID): Set<SongId> =
    txRunner.inRoTransaction { songTagRepository.getSongIdsByTagId(tagId) }
}

