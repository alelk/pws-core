package io.github.alelk.pws.domain.history.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * History entry with full song and optional book details for display.
 *
 * @property id Unique history entry ID.
 * @property subject What was viewed (booked or standalone song).
 * @property songName Name of the song.
 * @property songNumber Number of the song in the book (null for standalone songs).
 * @property bookDisplayName Display name of the book (null for standalone songs).
 * @property viewedAt When the song was last viewed.
 * @property viewCount How many times the song was viewed.
 */
@OptIn(ExperimentalTime::class)
data class HistoryEntry(
  val id: Long,
  val subject: HistorySubject,
  val songName: String,
  val songNumber: Int?,
  val bookDisplayName: String?,
  val viewedAt: Instant,
  val viewCount: Int = 1
)

