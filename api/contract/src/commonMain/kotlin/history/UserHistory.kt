package io.github.alelk.pws.api.contract.history

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.ktor.resources.Resource

/**
 * User history API endpoints.
 */
@Resource("/v1/user/history")
class UserHistory(
  val limit: Int = 50,
  val offset: Int = 0
) {
  @Resource("{bookId}/{songNumber}")
  class BySongNumber(
    val parent: UserHistory = UserHistory(),
    val bookId: BookIdDto,
    val songNumber: Int
  )
}
