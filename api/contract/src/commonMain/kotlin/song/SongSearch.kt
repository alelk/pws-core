package io.github.alelk.pws.api.contract.song

import io.ktor.resources.Resource

@Resource("/v1/songs/search")
class SongSearch(
  val query: String,
  val type: SearchTypeDto? = null,
  val limit: Int? = null,
  val offset: Int? = null,
  val highlight: Boolean? = null
)