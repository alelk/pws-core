package io.github.alelk.pws.api.contract.tag

import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import io.ktor.resources.Resource

/**
 * Public read-only API for tags.
 * For write operations, use admin routes at /v1/admin/tags.
 */
@Resource("/v1/tags")
class Tags(
  val sort: TagSortDto? = null
) {
  @Resource("{id}")
  class ById(val parent: Tags = Tags(), val id: TagIdDto) {

    @Resource("songs")
    class Songs(val parent: ById)
  }
}

