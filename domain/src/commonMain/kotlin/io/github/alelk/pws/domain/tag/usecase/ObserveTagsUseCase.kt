package io.github.alelk.pws.domain.tag.usecase

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.query.TagSort
import io.github.alelk.pws.domain.tag.repository.TagObserveRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case: observe all tags reactively.
 * @param ID The type of TagId this use case works with
 */
class ObserveTagsUseCase<out ID : TagId>(
  private val tagRepository: TagObserveRepository<ID>
) {
  operator fun invoke(sort: TagSort = TagSort.ByPriority): Flow<List<Tag<ID>>> =
    tagRepository.observeAll(sort)
}

