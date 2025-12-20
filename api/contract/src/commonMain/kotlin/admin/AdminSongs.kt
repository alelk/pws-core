package io.github.alelk.pws.api.contract.admin

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SongSortDto
import io.ktor.resources.Resource

/**
 * Admin API for managing global songs.
 * Requires admin role.
 */
@Resource("/v1/admin/songs")
class AdminSongs(
  val bookId: BookIdDto? = null,
  val minNumber: Int? = null,
  val maxNumber: Int? = null,
  val sort: SongSortDto? = null
) {
  @Resource("{id}")
  class ById(val parent: AdminSongs = AdminSongs(), val id: SongIdDto) {

    @Resource("tags")
    class Tags(val parent: ById)
  }
}

