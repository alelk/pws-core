package io.github.alelk.pws.api.contract.song

import kotlinx.serialization.Serializable

@Serializable
data class SongSearchResultDto(
  val song: SongSummaryDto,
  val snippet: String,
  val rank: Float,
  val matchedFields: List<MatchedFieldDto>,
  val bookReferences: List<SongBookReferenceDto> = emptyList()
)