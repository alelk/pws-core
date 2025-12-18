package io.github.alelk.pws.api.contract.tag

import io.github.alelk.pws.api.contract.core.ColorDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import kotlinx.serialization.Serializable

/**
 * Request to create a new tag.
 */
@Serializable
data class TagCreateRequestDto(
  val id: TagIdDto,
  val name: String,
  val color: ColorDto,
  val priority: Int = 0
)

