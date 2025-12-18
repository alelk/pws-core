package io.github.alelk.pws.api.contract.usertag

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import io.github.alelk.pws.api.contract.tag.TagSortDto
import io.ktor.resources.Resource

/**
 * User tags API endpoints.
 * User can:
 * - Create custom tags
 * - Override global tag properties (color, visibility)
 * - Manage song-tag associations (add/remove tags from songs)
 */
@Resource("/v1/user/tags")
class UserTags(
  val sort: TagSortDto? = null
) {
  @Resource("{id}")
  class ById(val parent: UserTags = UserTags(), val id: TagIdDto) {

    @Resource("songs")
    class Songs(val parent: ById) {

      @Resource("{songId}")
      class BySongId(val parent: Songs, val songId: SongIdDto)
    }
  }

  /**
   * Manage global tag overrides (hide/show, change color).
   */
  @Resource("overrides")
  class Overrides(val parent: UserTags = UserTags()) {

    @Resource("{id}")
    class ById(val parent: Overrides, val id: TagIdDto)
  }
}

