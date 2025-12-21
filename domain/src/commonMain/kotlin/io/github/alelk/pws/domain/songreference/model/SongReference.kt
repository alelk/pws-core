package io.github.alelk.pws.domain.songreference.model

import io.github.alelk.pws.domain.core.SongRefReason
import io.github.alelk.pws.domain.core.ids.SongId

/**
 * Represents a reference between two songs.
 * This is a global association (not user-specific).
 */
data class SongReference(
  val songId: SongId,
  val refSongId: SongId,
  val reason: SongRefReason,
  /** Strength/confidence of the reference (0-100) */
  val volume: Int,
  /** Display priority (higher = shown first) */
  val priority: Int = 0
) {
  init {
    require(songId != refSongId) { "Song cannot reference itself" }
    require(volume in 0..100) { "Volume must be between 0 and 100" }
    require(priority >= 0) { "Priority must be non-negative" }
  }
}

