package io.github.alelk.pws.domain.songtag.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository
import io.github.alelk.pws.domain.tag.model.Tag

/**
 * Use case: get all tags (with full data — name, color, etc.) for a song.
 * @param ID The type of TagId this use case works with
 */
class GetSongTagsUseCase<ID : TagId>(
  private val songTagRepository: SongTagReadRepository<ID>,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(songId: SongId): List<Tag<ID>> =
    txRunner.inRoTransaction { songTagRepository.getTagsForSong(songId) }
}

