package io.github.alelk.pws.domain.tag.repository

import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.model.TagDetail
import io.github.alelk.pws.domain.tag.query.TagSort

/**
 * Read operations for Tag aggregate.
 * @param ID The type of TagId this repository works with
 */
interface TagReadRepository<ID : TagId> {
  suspend fun get(id: ID): TagDetail<ID>?
  suspend fun getAll(sort: TagSort = TagSort.ByPriority): List<Tag<ID>>
}

