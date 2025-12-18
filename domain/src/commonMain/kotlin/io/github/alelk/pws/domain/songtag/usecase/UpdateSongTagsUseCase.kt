package io.github.alelk.pws.domain.songtag.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.result.ReplaceAllResourcesResult
import io.github.alelk.pws.domain.core.transaction.TransactionRunner
import io.github.alelk.pws.domain.songtag.model.SongTagAssociation
import io.github.alelk.pws.domain.songtag.repository.SongTagWriteRepository

/**
 * Use case: update tags for a song (set all at once).
 */
class UpdateSongTagsUseCase(
  private val songTagRepository: SongTagWriteRepository,
  private val txRunner: TransactionRunner
) {
  suspend operator fun invoke(songId: SongId, tagIds: Set<TagId>): ReplaceAllResourcesResult<SongTagAssociation> {
    return txRunner.inRwTransaction { songTagRepository.setTagsForSong(songId, tagIds) }
  }
}

