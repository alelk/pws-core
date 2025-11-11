package io.github.alelk.pws.api.contract.book

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.ktor.resources.Resource

@Resource("/v1/books")
class Books(
  val locale: LocaleDto? = null,
  val enabled: Boolean? = null,
  val minPriority: Int? = null
) {

  @Resource("{id}")
  class ById(val parent: Books = Books(), val id: BookIdDto)
}