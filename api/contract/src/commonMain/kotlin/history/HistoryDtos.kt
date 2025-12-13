package io.github.alelk.pws.api.contract.history

import io.github.alelk.pws.api.contract.core.ids.SongNumberIdDto
import kotlinx.serialization.Serializable

/**
 * History entry response.
 */
@Serializable
data class HistoryEntryDto(
  val songNumberId: SongNumberIdDto,
  val viewedAt: Long // epoch millis
)

/**
 * Record view result.
 */
@Serializable
data class RecordViewResultDto(
  val viewedAt: Long // epoch millis
)

