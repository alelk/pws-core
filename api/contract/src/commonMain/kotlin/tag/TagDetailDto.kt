package io.github.alelk.pws.api.contract.tag

import io.github.alelk.pws.api.contract.core.ColorDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import kotlinx.serialization.Serializable

/**
 * Detailed tag view.
 */
@Serializable
data class TagDetailDto(
  val id: TagIdDto,
  val name: String,
  val priority: Int,
  val color: ColorDto,
  val predefined: Boolean,
  val songCount: Int
)

