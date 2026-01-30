package io.github.alelk.pws.domain.tag.repository

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.tag.Tag
import io.github.alelk.pws.domain.tag.model.TagDetail
import io.github.alelk.pws.domain.tag.query.TagSort

/**
 * Read operations for Tag aggregate.
 */
interface TagReadRepository {
  suspend fun get(id: TagId): TagDetail?
  suspend fun getAll(sort: TagSort = TagSort.ByPriority): List<Tag>
}

