package io.github.alelk.pws.api.contract.userbook

import io.github.alelk.pws.api.contract.book.BookSortDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.ktor.resources.Resource

/**
 * User books API endpoints.
 * User books are custom songbooks created by users.
 */
@Resource("/v1/user/books")
class UserBooks(
  val sort: BookSortDto? = null
) {
  @Resource("{id}")
  class ById(val parent: UserBooks = UserBooks(), val id: BookIdDto) {

    @Resource("songs")
    class Songs(val parent: ById) {

      @Resource("{songId}")
      class BySongId(val parent: Songs, val songId: SongIdDto)
    }
  }
}

