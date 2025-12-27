package io.github.alelk.pws.api.contract.song

import io.ktor.resources.Resource

@Resource("/v1/songs/search/suggestions")
class SongSearchSuggestions(
  val query: String,
  val limit: Int? = null
)