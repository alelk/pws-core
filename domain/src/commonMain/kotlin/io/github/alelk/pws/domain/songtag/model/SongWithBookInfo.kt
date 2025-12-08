package io.github.alelk.pws.domain.songtag.model

import io.github.alelk.pws.domain.core.ids.SongNumberId

/**
 * Song with book info for tag songs screen.
 */
data class SongWithBookInfo(
  val songNumberId: SongNumberId,
  val songNumber: Int,
  val songName: String,
  val bookDisplayName: String
)

