package io.github.alelk.pws.api.contract.favorite

import io.ktor.resources.Resource

/**
 * User favorites API endpoints.
 *
 * GET    /v1/user/favorites         - list favorites
 * POST   /v1/user/favorites         - add to favorites (body: FavoriteSubjectDto)
 * DELETE /v1/user/favorites         - clear all favorites
 * DELETE /v1/user/favorites/entry   - remove from favorites (body: FavoriteSubjectDto)
 * POST   /v1/user/favorites/status  - get favorite status (body: FavoriteSubjectDto)
 * POST   /v1/user/favorites/toggle  - toggle favorite (body: FavoriteSubjectDto)
 */
@Resource("/v1/user/favorites")
class UserFavorites(
  val limit: Int = 50,
  val offset: Int = 0
) {
  /** Remove specific favorite entry */
  @Resource("entry")
  class Entry(val parent: UserFavorites = UserFavorites())

  /** Get favorite status for a song */
  @Resource("status")
  class Status(val parent: UserFavorites = UserFavorites())

  /** Toggle favorite status for a song */
  @Resource("toggle")
  class Toggle(val parent: UserFavorites = UserFavorites())
}

