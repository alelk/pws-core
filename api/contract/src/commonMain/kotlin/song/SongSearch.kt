package io.github.alelk.pws.api.contract.song

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.ktor.resources.Resource

/**
 * Full-text search on songs.
 *
 * - If user is authenticated and scope=ALL (default): searches both global songs
 *   and user's songbooks with unified ranking
 * - If user is not authenticated: searches only global songs
 * - scope=USER_BOOKS: searches only in user's songbooks (requires auth)
 * - scope=GLOBAL: searches only in global songs catalog
 */
@Resource("/v1/songs/search")
class SongSearch(
  val query: String,
  val type: SearchTypeDto? = null,
  val bookId: BookIdDto? = null,
  val scope: SearchScopeDto? = null,
  val limit: Int? = null,
  val offset: Int? = null,
  val highlight: Boolean? = null
)