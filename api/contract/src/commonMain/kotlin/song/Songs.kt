package io.github.alelk.pws.api.contract.song

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.ktor.resources.Resource

@Resource("/v1/songs")
class Songs(
  val bookId: BookIdDto? = null,
  val minNumber: Int? = null,
  val maxNumber: Int? = null,
  val sort: SongSortDto? = null
) {
  @Resource("{id}")
  class ById(val parent: Songs = Songs(), val id: SongIdDto)
}
