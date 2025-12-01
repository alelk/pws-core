package io.github.alelk.pws.api.contract.book.songnumber

import kotlinx.serialization.Serializable

@Serializable
data class ReplaceAllBookSongNumbersResult(
  val created: List<SongNumberLinkDto>,
  val updated: List<SongNumberLinkDto>,
  val unchanged: List<SongNumberLinkDto>,
  val deleted: List<SongNumberLinkDto>
)
