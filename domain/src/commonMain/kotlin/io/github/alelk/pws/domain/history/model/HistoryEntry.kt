package io.github.alelk.pws.domain.history.model

import io.github.alelk.pws.domain.core.ids.SongNumberId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * History entry for a viewed song.
 */
@OptIn(ExperimentalTime::class)
data class HistoryEntry(
  val songNumberId: SongNumberId,
  val viewedAt: Instant
)

