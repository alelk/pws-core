package io.github.alelk.pws.domain.songtag.usecase

import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.songtag.repository.SongTagObserveRepository
import io.github.alelk.pws.domain.tag.model.Tag
import kotlinx.coroutines.flow.Flow

/**
 * Use case: observe tags for a song.
 * @param ID The type of TagId this use case works with
 */
class ObserveTagsForSongUseCase<out ID : TagId>(
  private val songTagRepository: SongTagObserveRepository<ID>
) {
  operator fun invoke(songNumberId: SongNumberId): Flow<List<Tag<ID>>> =
    songTagRepository.observeTagsForSong(songNumberId)
}

