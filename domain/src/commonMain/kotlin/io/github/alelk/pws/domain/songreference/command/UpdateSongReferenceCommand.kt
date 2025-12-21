package io.github.alelk.pws.domain.songreference.command

import io.github.alelk.pws.domain.core.SongRefReason
import io.github.alelk.pws.domain.core.ids.SongId

/**
 * Command to update an existing song reference.
 */
data class UpdateSongReferenceCommand(
  val songId: SongId,
  val refSongId: SongId,
  val reason: SongRefReason? = null,
  val volume: Int? = null,
  val priority: Int? = null
) {
  init {
    volume?.let { require(it in 0..100) { "Volume must be between 0 and 100" } }
    priority?.let { require(it >= 0) { "Priority must be non-negative" } }
  }
}

