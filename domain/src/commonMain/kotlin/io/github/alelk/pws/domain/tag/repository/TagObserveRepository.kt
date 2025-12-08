package io.github.alelk.pws.domain.tag.repository

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.tag.model.TagSummary
import io.github.alelk.pws.domain.tag.query.TagSort
import kotlinx.coroutines.flow.Flow

/**
 * Observe operations for Tag aggregate.
 */
interface TagObserveRepository {
  fun observeAll(sort: TagSort = TagSort.ByPriority): Flow<List<TagSummary>>
}

