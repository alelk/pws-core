package io.github.alelk.pws.domain.history.model

import io.github.alelk.pws.domain.core.ids.SongNumberId

/**
 * History entry for a viewed song.
 */
data class HistoryEntry(
  val id: Long,
  val songNumberId: SongNumberId,
  val viewedAt: Long // timestamp in millis
)

