package io.github.alelk.pws.domain.songtag.usecase

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songtag.repository.SongTagWriteRepository

/**
 * Use case: update tags for a song (set all at once).
 */
class UpdateSongTagsUseCase(
  private val songTagRepository: SongTagWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(songNumberId: SongNumberId, tagIds: Set<TagId>) {
    txRunner.inRwTransaction { songTagRepository.setTagsForSong(songNumberId, tagIds) }
  }
}

