package io.github.alelk.pws.api.contract.book

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.ktor.resources.Resource

@Resource("/v1/books")
class Books(
  val locale: LocaleDto? = null,
  val enabled: Boolean? = null,
  val minPriority: Int? = null,
  val sort: BookSortDto? = null
) {
  @Resource("{id}")
  class ById(val parent: Books = Books(), val id: BookIdDto) {

    @Resource("songs")
    class Songs(val parent: ById) {

      @Resource("{id}")
      class SongById(val parent: Songs, val id: SongIdDto)
    }
  }
}