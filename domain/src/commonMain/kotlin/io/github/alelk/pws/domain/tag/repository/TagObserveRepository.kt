package io.github.alelk.pws.domain.tag.repository

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.query.TagSort
import kotlinx.coroutines.flow.Flow

/**
 * Observe operations for Tag aggregate.
 * @param ID The type of TagId this repository works with
 */
interface TagObserveRepository<out ID : TagId> {
  fun observeAll(sort: TagSort = TagSort.ByPriority): Flow<List<Tag<ID>>>
}

