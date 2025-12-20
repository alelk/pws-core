package io.github.alelk.pws.domain.songtag.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository

/**
 * Use case: get all tag IDs for a song.
 */
class GetSongTagsUseCase(
  private val songTagRepository: SongTagReadRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(songId: SongId): Set<TagId> =
    txRunner.inRoTransaction { songTagRepository.getTagIdsBySongId(songId) }
}

