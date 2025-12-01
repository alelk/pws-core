package io.github.alelk.pws.api.contract.book.songnumber

import kotlinx.serialization.Serializable
import io.github.alelk.pws.api.contract.book.songnumber.SongNumberLinkDto

@Serializable
data class ReplaceAllBookSongNumbersResult(
  val created: List<SongNumberLinkDto>,
  val updated: List<SongNumberLinkDto>,
  val unchanged: List<SongNumberLinkDto>,
  val deleted: List<SongNumberLinkDto>
)
