package io.github.alelk.pws.api.contract.songreference

import kotlinx.serialization.Serializable

/**
 * Request DTO for updating a song reference.
 */
@Serializable
data class SongReferenceUpdateRequestDto(
  val reason: SongRefReasonDto? = null,
  val volume: Int? = null,
  val priority: Int? = null
) {
  init {
    volume?.let { require(it in 0..100) { "Volume must be between 0 and 100" } }
    priority?.let { require(it >= 0) { "Priority must be non-negative" } }
  }
}
