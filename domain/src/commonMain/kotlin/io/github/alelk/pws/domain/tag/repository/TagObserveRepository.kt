package io.github.alelk.pws.domain.tag.repository

import io.github.alelk.pws.domain.tag.Tag
import io.github.alelk.pws.domain.tag.query.TagSort
import kotlinx.coroutines.flow.Flow

/**
 * Observe operations for Tag aggregate.
 */
interface TagObserveRepository {
  fun observeAll(sort: TagSort = TagSort.ByPriority): Flow<List<Tag>>
}

