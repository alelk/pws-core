package io.github.alelk.pws.domain.songtag.usecase

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.songtag.model.SongWithBookInfo
import io.github.alelk.pws.domain.songtag.repository.SongTagObserveRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case: observe songs by tag.
 * @param ID The type of TagId this use case works with
 */
class ObserveSongsByTagUseCase<ID : TagId>(
  private val songTagRepository: SongTagObserveRepository<ID>
) {
  operator fun invoke(tagId: ID): Flow<List<SongWithBookInfo>> =
    songTagRepository.observeSongsByTag(tagId)
}

