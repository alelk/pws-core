package io.github.alelk.pws.api.contract.tag

import io.github.alelk.pws.api.contract.core.ColorDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import kotlinx.serialization.Serializable

/**
 * Tag summary for list display.
 */
@Serializable
data class TagSummaryDto(
  val id: TagIdDto,
  val name: String,
  val color: ColorDto,
  val songCount: Int,
  val predefined: Boolean
)

