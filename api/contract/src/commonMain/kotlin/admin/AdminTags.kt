package io.github.alelk.pws.api.contract.admin

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import io.github.alelk.pws.api.contract.tag.TagSortDto
import io.ktor.resources.Resource

/**
 * Admin API for managing global tags.
 * Requires admin role.
 */
@Resource("/v1/admin/tags")
class AdminTags(
  val sort: TagSortDto? = null
) {
  @Resource("{id}")
  class ById(val parent: AdminTags = AdminTags(), val id: TagIdDto) {

    @Resource("songs")
    class Songs(val parent: ById) {

      @Resource("{songId}")
      class BySongId(val parent: Songs, val songId: SongIdDto)
    }
  }
}

