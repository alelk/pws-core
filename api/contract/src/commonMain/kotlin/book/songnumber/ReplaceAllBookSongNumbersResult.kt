package book.songnumber

import io.github.alelk.pws.api.contract.book.songnumber.SongNumberLinkDto

data class ReplaceAllBookSongNumbersResult(
  val created: List<SongNumberLinkDto>,
  val updated: List<SongNumberLinkDto>,
  val unchanged: List<SongNumberLinkDto>,
  val deleted: List<SongNumberLinkDto>
)
