package io.github.alelk.pws.domain.songtag.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository

/**
 * Use case: get all song IDs for a tag.
 */
class GetTagSongsUseCase(
  private val songTagRepository: SongTagReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(tagId: TagId): Set<SongId> =
    txRunner.inRoTransaction { songTagRepository.getSongIdsByTagId(tagId) }
}

