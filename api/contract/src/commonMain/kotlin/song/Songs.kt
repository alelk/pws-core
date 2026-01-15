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

  /**
   * Full-text search on global songs.
   *
   * Searches only in the global songs catalog.
   * For searching both global and user's songs, use `/v1/user/songs/search`.
   */
  @Resource("search")
  class Search(
    val parent: Songs = Songs(),
    val query: String,
    val type: SearchTypeDto? = null,
    val bookId: BookIdDto? = null,
    val limit: Int? = null,
    val offset: Int? = null,
    val highlight: Boolean? = null
  )

  /**
   * Get search suggestions for autocomplete from global songs.
   *
   * Searches only in the global songs catalog.
   * For searching both global and user's songs, use `/v1/user/songs/search/suggestions`.
   */
  @Resource("search/suggestions")
  class SearchSuggestions(
    val parent: Songs = Songs(),
    val query: String,
    val bookId: BookIdDto? = null,
    val limit: Int? = null
  )
}
