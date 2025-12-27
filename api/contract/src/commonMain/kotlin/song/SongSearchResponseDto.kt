package io.github.alelk.pws.api.contract.song

import kotlinx.serialization.Serializable

@Serializable
data class SongSearchResponseDto(
  val results: List<SongSearchResultDto>,
  val totalCount: Long,
  val hasMore: Boolean
)

