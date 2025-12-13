package io.github.alelk.pws.api.contract.admin

import io.github.alelk.pws.api.contract.book.BookSortDto
import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.ktor.resources.Resource

/**
 * Admin API for managing global books.
 * Requires admin role.
 */
@Resource("/v1/admin/books")
class AdminBooks(
  val locale: LocaleDto? = null,
  val enabled: Boolean? = null,
  val minPriority: Int? = null,
  val sort: BookSortDto? = null
) {
  @Resource("{id}")
  class ById(val parent: AdminBooks = AdminBooks(), val id: BookIdDto) {

    @Resource("songs")
    class Songs(val parent: ById) {

      @Resource("{songId}")
      class BySongId(val parent: Songs, val songId: SongIdDto)
    }
  }
}

