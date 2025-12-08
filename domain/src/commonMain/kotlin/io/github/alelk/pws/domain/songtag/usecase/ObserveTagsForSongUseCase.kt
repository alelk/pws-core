package io.github.alelk.pws.domain.songtag.usecase

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.songtag.repository.SongTagObserveRepository
import io.github.alelk.pws.domain.tag.model.TagSummary
import kotlinx.coroutines.flow.Flow

/**
 * Use case: observe tags for a song.
 */
class ObserveTagsForSongUseCase(
  private val songTagRepository: SongTagObserveRepository
) {
  operator fun invoke(songNumberId: SongNumberId): Flow<List<TagSummary>> =
    songTagRepository.observeTagsForSong(songNumberId)
}

