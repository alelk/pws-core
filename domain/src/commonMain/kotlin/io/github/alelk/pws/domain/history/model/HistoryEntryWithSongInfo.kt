package io.github.alelk.pws.domain.history.model

import io.github.alelk.pws.domain.core.ids.SongNumberId

/**
 * History entry with song and book details for display.
 */
data class HistoryEntryWithSongInfo(
  val id: Long,
  val songNumberId: SongNumberId,
  val songNumber: Int,
  val songName: String,
  val bookDisplayName: String,
  val viewedAt: Long
)

