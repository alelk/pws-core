package io.github.alelk.pws.api.contract.songreference

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import kotlinx.serialization.Serializable

/**
 * Request DTO for creating a song reference.
 */
@Serializable
data class SongReferenceCreateRequestDto(
  val refSongId: SongIdDto,
  val reason: SongRefReasonDto,
  val volume: Int,
  val priority: Int = 0
) {
  init {
    require(volume in 0..100) { "Volume must be between 0 and 100" }
    require(priority >= 0) { "Priority must be non-negative" }
  }
}
