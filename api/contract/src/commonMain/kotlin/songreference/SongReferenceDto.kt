package io.github.alelk.pws.api.contract.songreference

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import kotlinx.serialization.Serializable

/**
 * DTO representing a song-to-song reference.
 */
@Serializable
data class SongReferenceDto(
  val songId: SongIdDto,
  val refSongId: SongIdDto,
  val reason: SongRefReasonDto,
  val volume: Int,
  val priority: Int = 0
) {
  init {
    require(songId != refSongId) { "Song cannot reference itself" }
    require(volume in 0..100) { "Volume must be between 0 and 100" }
    require(priority >= 0) { "Priority must be non-negative" }
  }
}
