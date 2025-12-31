package io.github.alelk.pws.domain.history.model

import io.github.alelk.pws.domain.core.ids.SongNumberId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * History entry with song and book details for display.
 */
@OptIn(ExperimentalTime::class)
data class SongHistorySummary(
  val id: Long,
  val songNumberId: SongNumberId,
  val songNumber: Int,
  val songName: String,
  val bookDisplayName: String,
  val viewedAt: Instant
)

