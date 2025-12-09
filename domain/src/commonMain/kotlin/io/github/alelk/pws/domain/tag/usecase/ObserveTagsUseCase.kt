package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.tag.model.TagSummary
import io.github.alelk.pws.domain.tag.query.TagSort
import io.github.alelk.pws.domain.tag.repository.TagObserveRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case: observe all tags reactively.
 */
class ObserveTagsUseCase(
  private val tagRepository: TagObserveRepository
) {
  operator fun invoke(sort: TagSort = TagSort.ByPriority): Flow<List<TagSummary>> =
    tagRepository.observeAll(sort)
}

