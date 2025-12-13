package io.github.alelk.pws.api.contract.favorite

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.ktor.resources.Resource

/**
 * User favorites API endpoints.
 */
@Resource("/v1/user/favorites")
class UserFavorites(
  val limit: Int = 50,
  val offset: Int = 0
) {
  @Resource("{bookId}/{songNumber}")
  class BySongNumber(
    val parent: UserFavorites = UserFavorites(),
    val bookId: BookIdDto,
    val songNumber: Int
  ) {
    @Resource("toggle")
    class Toggle(val parent: BySongNumber)
  }
}

