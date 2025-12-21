package io.github.alelk.pws.domain.songreference.command

import io.github.alelk.pws.domain.core.SongRefReason
import io.github.alelk.pws.domain.core.ids.SongId

/**
 * Command to create a new song reference.
 */
data class CreateSongReferenceCommand(
  val songId: SongId,
  val refSongId: SongId,
  val reason: SongRefReason,
  val volume: Int,
  val priority: Int = 0
) {
  init {
    require(songId != refSongId) { "Song cannot reference itself" }
    require(volume in 0..100) { "Volume must be between 0 and 100" }
    require(priority >= 0) { "Priority must be non-negative" }
  }
}

