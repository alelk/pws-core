package io.github.alelk.pws.domain.songtag.usecase

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.songtag.model.SongWithBookInfo
import io.github.alelk.pws.domain.songtag.repository.SongTagObserveRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case: observe songs by tag.
 */
class ObserveSongsByTagUseCase(
  private val songTagRepository: SongTagObserveRepository
) {
  operator fun invoke(tagId: TagId): Flow<List<SongWithBookInfo>> =
    songTagRepository.observeSongsByTag(tagId)
}

